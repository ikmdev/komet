/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.pluginresolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lists artifact versions already present in a local Maven repository directory
 * (e.g. {@code ~/.m2/repository}), by walking the repository layout directly rather than
 * trusting {@code maven-metadata-local.xml} (which can be stale or absent). Performs no
 * network I/O.
 */
public final class LocalVersionResolver {

    /**
     * Matches the {@code <fileVersion>-<classifier>} shape a cached snapshot's classified zip
     * filenames use, once the {@code <artifactId>-} prefix and {@code .zip} suffix are already
     * stripped — e.g. {@code "1.0.0-20260714.135548-4-reasoned-sa"} splits into file version
     * {@code "1.0.0-20260714.135548-4"} (group 1) and classifier {@code "reasoned-sa"} (group 2).
     *
     * <p>Anchored on Maven's fixed-shape {@code <timestamp>.<buildNumber>} snapshot suffix
     * ({@code \d{8}\.\d{6}-\d+}) rather than assuming anything about what comes before it — an
     * earlier version of this pattern assumed a plain dotted-decimal base version
     * ({@code \d+(\.\d+)*}) and silently failed to match real ones that don't fit that shape
     * (e.g. {@code dev.ikm.tinkar.data:rxnorm}'s {@code "2024-04-10+1.0.0"}, or
     * {@code dev.ikm.tinkar.data:loinc}'s artifactId-prefixed {@code "loinc-2.82+1.0.0"}) —
     * confirmed against both live artifacts, 2026-07-22. The base version can contain
     * hyphens/dots/plus signs/anything; only the timestamp suffix's shape is fixed.
     */
    private static final Pattern SNAPSHOT_FILE_VERSION_AND_CLASSIFIER =
            Pattern.compile("^(.+-\\d{8}\\.\\d{6}-\\d+)-(.+)$");

    private final Path localRepositoryRoot;

    /**
     * Creates a resolver over the given local repository root.
     *
     * @param localRepositoryRoot the local repository root directory (e.g. {@code ~/.m2/repository})
     * @throws NullPointerException if {@code localRepositoryRoot} is {@code null}
     */
    public LocalVersionResolver(Path localRepositoryRoot) {
        this.localRepositoryRoot = Objects.requireNonNull(localRepositoryRoot, "localRepositoryRoot");
    }

    /**
     * Lists the versions of {@code coordinates} present in the local repository, sorted
     * lexicographically. A version is considered present when its directory contains either
     * a {@code .pom} or a {@code .jar} file matching the standard
     * {@code <artifactId>-<version>.<extension>} naming convention.
     *
     * @param coordinates the artifact to look up
     * @return the locally-present versions, sorted lexicographically; empty if the artifact's
     *         directory does not exist under the local repository
     * @throws NullPointerException if {@code coordinates} is {@code null}
     * @throws UncheckedIOException if the artifact directory cannot be listed
     */
    public List<String> localVersions(ArtifactCoordinates coordinates) {
        Objects.requireNonNull(coordinates, "coordinates");
        Path artifactDirectory = artifactDirectory(coordinates);
        if (!Files.isDirectory(artifactDirectory)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(artifactDirectory)) {
            return children
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(version -> isPublishedVersion(artifactDirectory, coordinates.artifactId(), version))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list local versions under " + artifactDirectory, e);
        }
    }

    /**
     * As {@link #localVersions(ArtifactCoordinates)}, but keeping only versions that have at
     * least one classifier in {@code classifierCandidates} present on disk — the local,
     * filesystem-authoritative counterpart of {@code NexusSearchClient.compatibleVersions}. This
     * gives {@code ~/.m2} browsing the same "only offer versions with a usable variant" guarantee
     * the Nexus path already provides (IKE-Network/ike-issues#882, #932): a cached version whose
     * classifiers don't intersect the candidates is never surfaced, rather than being surfaced
     * only to be rejected once selected. Order is preserved from {@link #localVersions}
     * (lexicographic).
     *
     * <p>Each version's classifiers are read exactly as {@link #availableClassifiers} reads them,
     * so a {@code -SNAPSHOT} version whose files carry a resolved timestamp rather than the literal
     * {@code -SNAPSHOT} string is matched correctly and kept when it has a compatible variant.
     *
     * @param coordinates the artifact to look up
     * @param classifierCandidates the classifiers considered compatible, in any order (e.g.
     *         {@code ProviderArtifactQualifier.classifierCandidates(flow)}); a version is kept if
     *         any one of them is present for it
     * @return the locally-present versions that have a compatible classifier, in
     *         {@link #localVersions} order; empty if none qualify or the artifact isn't cached
     * @throws NullPointerException if either argument is {@code null}
     * @throws UncheckedIOException if a version directory cannot be listed
     */
    public List<String> compatibleVersions(ArtifactCoordinates coordinates, List<String> classifierCandidates) {
        Objects.requireNonNull(coordinates, "coordinates");
        Objects.requireNonNull(classifierCandidates, "classifierCandidates");
        return localVersions(coordinates).stream()
                .filter(version -> hasCompatibleClassifier(coordinates, version, classifierCandidates))
                .toList();
    }

    /**
     * Whether {@code version} has at least one of {@code classifierCandidates} present on disk.
     *
     * @param coordinates the artifact
     * @param version the exact version directory to inspect
     * @param classifierCandidates the classifiers considered compatible
     * @return {@code true} if any candidate classifier is present for {@code version}
     */
    private boolean hasCompatibleClassifier(ArtifactCoordinates coordinates, String version, List<String> classifierCandidates) {
        Set<String> present = availableClassifiers(coordinates, version);
        return classifierCandidates.stream().anyMatch(present::contains);
    }

    /**
     * The directory a given artifact's version subdirectories live under, following the
     * standard Maven 2 repository layout ({@code <root>/<groupPath>/<artifactId>}).
     *
     * @param coordinates the artifact to locate
     * @return the artifact's directory, which may not exist
     */
    public Path artifactDirectory(ArtifactCoordinates coordinates) {
        return localRepositoryRoot.resolve(coordinates.groupPath()).resolve(coordinates.artifactId());
    }

    private boolean isPublishedVersion(Path artifactDirectory, String artifactId, String version) {
        return isPublishedVersionDirectory(artifactDirectory.resolve(version), artifactId, version);
    }

    /**
     * Whether {@code versionDirectory} really holds a published version of {@code artifactId} —
     * either the standard {@code .pom}/{@code .jar} signal, or (since a store-snapshot/changeset
     * artifact fetched via {@code MavenDataStoreDownloadTask} has no accompanying {@code .pom},
     * only its classified {@code .zip}) any classified {@code .zip} matching the release or
     * snapshot filename shape. Shared with {@link LocalRepositorySearch}, which needs the exact
     * same "is this a real artifact/version" test while walking the repository tree.
     *
     * @param versionDirectory the candidate version directory
     * @param artifactId the artifact id this directory is expected to belong to
     * @param version the version this directory is named after
     * @return {@code true} if this looks like a real published version
     */
    static boolean isPublishedVersionDirectory(Path versionDirectory, String artifactId, String version) {
        Path pom = versionDirectory.resolve(artifactId + "-" + version + ".pom");
        Path jar = versionDirectory.resolve(artifactId + "-" + version + ".jar");
        if (Files.exists(pom) || Files.exists(jar)) {
            return true;
        }
        if (!Files.isDirectory(versionDirectory)) {
            return false;
        }
        try (Stream<Path> children = Files.list(versionDirectory)) {
            List<String> zipNames = children.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".zip"))
                    .toList();
            if (isSnapshot(version)) {
                return zipNames.stream().anyMatch(name -> snapshotFileVersionAndClassifier(name, artifactId).isPresent());
            }
            String classifiedPrefix = artifactId + "-" + version + "-";
            return zipNames.stream().anyMatch(name -> name.startsWith(classifiedPrefix));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * The classifiers actually present, as {@code .zip} files, for one specific artifact
     * version already cached locally — the filesystem-authoritative equivalent of
     * {@code NexusSearchClient.classifiersFor}, used to decide whether a locally-cached
     * artifact has a variant compatible with a given flow without guessing.
     *
     * <p>For a {@code -SNAPSHOT} {@code version}, the version <em>directory</em> keeps the
     * {@code -SNAPSHOT} name but the files inside it are named with the concrete, uniquely
     * -timestamped file version a snapshot repository actually resolves it to (e.g.
     * {@code example-plugin-1.0.0-20260714.135548-4-reasoned-sa.zip} under the
     * {@code 1.0.0-SNAPSHOT} directory) — {@link #isSnapshot} routes to
     * {@link #SNAPSHOT_FILE_VERSION_AND_CLASSIFIER} to extract the classifier correctly in that
     * shape instead of the plain {@code <artifactId>-<version>-} prefix strip used for a release.
     *
     * @param coordinates the artifact to look up
     * @param version the exact version
     * @return the classifiers present in that version's directory; empty if the directory
     *         doesn't exist or has no matching files
     * @throws NullPointerException if either argument is {@code null}
     * @throws UncheckedIOException if the version directory cannot be listed
     */
    public Set<String> availableClassifiers(ArtifactCoordinates coordinates, String version) {
        Objects.requireNonNull(coordinates, "coordinates");
        Objects.requireNonNull(version, "version");
        return classifiersInDirectory(artifactDirectory(coordinates).resolve(version), coordinates.artifactId(), version);
    }

    /**
     * As {@link #availableClassifiers(ArtifactCoordinates, String)}, but taking the version
     * directory directly rather than deriving it from a {@link #localRepositoryRoot} — shared
     * with {@link LocalRepositorySearch}, which walks directories it hasn't wrapped in an
     * {@code ArtifactCoordinates} yet.
     *
     * @param versionDirectory the candidate version directory
     * @param artifactId the artifact id this directory is expected to belong to
     * @param version the version this directory is named after
     * @return the classifiers present in that version's directory
     */
    static Set<String> classifiersInDirectory(Path versionDirectory, String artifactId, String version) {
        if (!Files.isDirectory(versionDirectory)) {
            return Set.of();
        }
        try (Stream<Path> children = Files.list(versionDirectory)) {
            List<String> fileNames = children.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".zip"))
                    .toList();
            return isSnapshot(version)
                    ? classifiersFromSnapshotFileNames(fileNames, artifactId)
                    : classifiersFromReleaseFileNames(fileNames, artifactId, version);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list classifiers under " + versionDirectory, e);
        }
    }

    private static Set<String> classifiersFromReleaseFileNames(List<String> fileNames, String artifactId, String version) {
        String prefix = artifactId + "-" + version + "-";
        return fileNames.stream()
                .filter(name -> name.startsWith(prefix))
                .map(name -> name.substring(prefix.length(), name.length() - ".zip".length()))
                .filter(classifier -> !classifier.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> classifiersFromSnapshotFileNames(List<String> fileNames, String artifactId) {
        return fileNames.stream()
                .map(name -> snapshotFileVersionAndClassifier(name, artifactId))
                .flatMap(Optional::stream)
                .map(match -> match[1])
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * The concrete, uniquely-timestamped file version a locally-cached {@code -SNAPSHOT}
     * version's files actually use — see {@link #availableClassifiers}'s javadoc for why this
     * differs from {@code version} itself. Every classifier from the same deploy shares one file
     * version, so this returns whichever classified {@code .zip} is found first.
     *
     * @param coordinates the artifact to look up
     * @param version the {@code -SNAPSHOT} version directory
     * @return the resolved file version, or empty if the directory doesn't exist or has no
     *         classified {@code .zip} files matching the expected snapshot filename shape
     * @throws NullPointerException if either argument is {@code null}
     * @throws UncheckedIOException if the version directory cannot be listed
     */
    public Optional<String> resolveSnapshotFileVersion(ArtifactCoordinates coordinates, String version) {
        Objects.requireNonNull(coordinates, "coordinates");
        Objects.requireNonNull(version, "version");
        Path versionDirectory = artifactDirectory(coordinates).resolve(version);
        if (!Files.isDirectory(versionDirectory)) {
            return Optional.empty();
        }
        try (Stream<Path> children = Files.list(versionDirectory)) {
            return children.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".zip"))
                    .map(name -> snapshotFileVersionAndClassifier(name, coordinates.artifactId()))
                    .flatMap(Optional::stream)
                    .findFirst()
                    .map(match -> match[0]);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list " + versionDirectory, e);
        }
    }

    /**
     * @return {@code {fileVersion, classifier}} if {@code fileName} matches
     *         {@code <artifactId>-<fileVersion>-<classifier>.zip} in the snapshot shape, else empty
     */
    private static Optional<String[]> snapshotFileVersionAndClassifier(String fileName, String artifactId) {
        String prefix = artifactId + "-";
        if (!fileName.startsWith(prefix) || !fileName.endsWith(".zip")) {
            return Optional.empty();
        }
        String withoutPrefixOrSuffix = fileName.substring(prefix.length(), fileName.length() - ".zip".length());
        Matcher matcher = SNAPSHOT_FILE_VERSION_AND_CLASSIFIER.matcher(withoutPrefixOrSuffix);
        return matcher.matches() ? Optional.of(new String[] {matcher.group(1), matcher.group(2)}) : Optional.empty();
    }

    private static boolean isSnapshot(String version) {
        return version.endsWith("-SNAPSHOT");
    }
}
