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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Browses {@code groupId:artifactId} pairs already present in a local Maven repository
 * directory (e.g. {@code ~/.m2/repository}), mirroring {@link NexusSearchClient#search}'s
 * {@code groupId:artifactPattern} / free-text query shape but by walking the repository layout
 * directly rather than querying a REST API. Performs no network I/O.
 */
public final class LocalRepositorySearch {

    private LocalRepositorySearch() {
    }

    /**
     * Searches {@code localRepositoryRoot} for {@code groupId:artifactId} pairs matching
     * {@code query}, excluding any artifact that doesn't publish at least one of
     * {@code classifierCandidates} on <em>some</em> cached version — per
     * IKE-Network/ike-issues#882, an artifact with no compatible variant must not be presented
     * as an option at all, matching {@code NexusSearchClient.search}'s classifier-aware overload
     * for the network case. Pass an empty list to skip this filter (every real artifact/version
     * directory matches, regardless of what it publishes).
     *
     * <p>A {@code groupId:artifactPattern} query (colon-separated) is scoped to exactly that
     * group's directory — fast, since it never walks the rest of the repository — and matches
     * artifact directories whose name contains {@code artifactPattern} (or every artifact under
     * the group, if the pattern half is blank). A query with no colon is matched as a
     * case-insensitive substring against every real {@code groupId:artifactId} found anywhere
     * under {@code localRepositoryRoot}.
     *
     * <p>A directory is only considered a real artifact directory — as opposed to just another
     * segment of a dotted groupId path — when at least one of its subdirectories is a real
     * published version by {@link LocalVersionResolver#isPublishedVersionDirectory}'s test (a
     * {@code .pom}/{@code .jar}, or a classified {@code .zip} for an artifact fetched with no
     * accompanying POM) <em>and</em>, when {@code classifierCandidates} is non-empty, that
     * version publishes at least one of them.
     *
     * @param localRepositoryRoot the local repository root directory (e.g. {@code ~/.m2/repository})
     * @param query the search term — {@code groupId:artifactPattern}, or free text
     * @param classifierCandidates the classifiers an artifact must publish at least one of to
     *         be included, or empty to not filter by classifier at all
     * @return the matching groupId:artifactId pairs, in directory-walk order
     * @throws NullPointerException if any argument is {@code null}
     * @throws UncheckedIOException if the repository cannot be walked
     */
    public static Set<ArtifactCoordinates> search(Path localRepositoryRoot, String query, List<String> classifierCandidates) {
        Objects.requireNonNull(localRepositoryRoot, "localRepositoryRoot");
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(classifierCandidates, "classifierCandidates");
        String strippedQuery = query.strip();
        if (strippedQuery.isEmpty()) {
            return Set.of();
        }

        int colonIndex = strippedQuery.indexOf(':');
        if (colonIndex >= 0) {
            String groupId = strippedQuery.substring(0, colonIndex).strip();
            String artifactPattern = strippedQuery.substring(colonIndex + 1).strip().toLowerCase(Locale.ROOT);
            if (groupId.isBlank()) {
                return searchWholeRepository(localRepositoryRoot, strippedQuery.toLowerCase(Locale.ROOT), classifierCandidates);
            }
            return searchWithinGroup(localRepositoryRoot, groupId, artifactPattern, classifierCandidates);
        }
        return searchWholeRepository(localRepositoryRoot, strippedQuery.toLowerCase(Locale.ROOT), classifierCandidates);
    }

    private static Set<ArtifactCoordinates> searchWithinGroup(Path localRepositoryRoot, String groupId, String artifactPattern,
                                                                List<String> classifierCandidates) {
        Path groupDirectory = localRepositoryRoot.resolve(groupId.replace('.', '/'));
        if (!Files.isDirectory(groupDirectory)) {
            return Set.of();
        }
        try (Stream<Path> children = Files.list(groupDirectory)) {
            return children
                    .filter(Files::isDirectory)
                    .filter(dir -> artifactPattern.isBlank()
                            || dir.getFileName().toString().toLowerCase(Locale.ROOT).contains(artifactPattern))
                    .filter(dir -> isArtifactDirectory(dir, classifierCandidates))
                    .map(dir -> new ArtifactCoordinates(groupId, dir.getFileName().toString()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list " + groupDirectory, e);
        }
    }

    private static Set<ArtifactCoordinates> searchWholeRepository(Path localRepositoryRoot, String query, List<String> classifierCandidates) {
        if (!Files.isDirectory(localRepositoryRoot)) {
            return Set.of();
        }
        Set<ArtifactCoordinates> results = new LinkedHashSet<>();
        try (Stream<Path> allDirectories = Files.walk(localRepositoryRoot)) {
            allDirectories
                    .filter(Files::isDirectory)
                    .filter(dir -> !dir.equals(localRepositoryRoot))
                    .filter(dir -> isArtifactDirectory(dir, classifierCandidates))
                    .forEach(dir -> {
                        String artifactId = dir.getFileName().toString();
                        String groupId = localRepositoryRoot.relativize(dir.getParent()).toString().replace('/', '.');
                        if (groupId.toLowerCase(Locale.ROOT).contains(query)
                                || artifactId.toLowerCase(Locale.ROOT).contains(query)) {
                            results.add(new ArtifactCoordinates(groupId, artifactId));
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to walk " + localRepositoryRoot, e);
        }
        return Set.copyOf(results);
    }

    private static boolean isArtifactDirectory(Path candidate, List<String> classifierCandidates) {
        String artifactId = candidate.getFileName().toString();
        try (Stream<Path> versionDirs = Files.list(candidate)) {
            return versionDirs.anyMatch(versionDir -> {
                if (!Files.isDirectory(versionDir)) {
                    return false;
                }
                String version = versionDir.getFileName().toString();
                if (!LocalVersionResolver.isPublishedVersionDirectory(versionDir, artifactId, version)) {
                    return false;
                }
                if (classifierCandidates.isEmpty()) {
                    return true;
                }
                Set<String> published = LocalVersionResolver.classifiersInDirectory(versionDir, artifactId, version);
                return classifierCandidates.stream().anyMatch(published::contains);
            });
        } catch (IOException e) {
            return false;
        }
    }
}
