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

import dev.ikm.tinkar.common.service.TrackingCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Resolves a classified data-store artifact and materializes it into {@code ~/Solor} in the
 * shape the target {@code DataServiceController} expects — either unpacked into a directory
 * (a {@code -sa} store snapshot) or placed as a single file (a {@code -pb} changeset zip).
 *
 * <p>The artifact is always staged through {@link #localRepositoryPath}, the same
 * {@code ~/.m2/repository} location ordinary {@code mvn} caches it at: if that file already
 * exists, no network request is made at all — the cached copy is used directly (this is how
 * browsing an already-downloaded artifact from the "Local repository" source works, via
 * {@link #localUnpackTask}/{@link #localPlaceAsFileTask}); otherwise it's downloaded there first,
 * then materialized from that cached copy exactly as if it had already been present. Reports
 * progress via {@link TrackingCallable}, so callers get byte-accurate progress and an
 * estimated-time-remaining for free; cooperatively cancellable via {@link #cancel()}, checked
 * once per read-loop iteration.
 */
public final class MavenDataStoreDownloadTask extends TrackingCallable<Path> {

    private static final Logger LOG = LoggerFactory.getLogger(MavenDataStoreDownloadTask.class);

    /** How the cached zip is materialized at its destination. */
    public enum Mode {
        /** Unpack the zip's contents into a destination directory. */
        UNPACK,
        /** Use the cached zip as-is, copied to a destination file path. */
        PLACE_AS_FILE
    }

    /**
     * What to verify/fetch alongside the classified zip itself — both entirely optional, since
     * neither applies to {@link #localUnpackTask}/{@link #localPlaceAsFileTask} (already-local,
     * nothing to verify or fetch over the network).
     *
     * @param zipSha256 the zip's expected SHA-256 checksum, if known (typically read straight
     *         off the same Nexus search response that resolved the classifier — see
     *         {@code NexusSearchClient.componentAssets} — so no extra request is needed just to
     *         verify integrity). If present and it doesn't match what was actually downloaded,
     *         the download fails outright — a corrupted or tampered artifact must never be used.
     *         Never checked against an already-cached file; only a fresh download is verified.
     * @param pom where to also fetch the artifact's POM from/to, if wanted — real Maven tooling
     *         always fetches one alongside any artifact, and its presence is what lets
     *         {@code LocalRepositorySearch}/{@code LocalVersionResolver} recognize this as an
     *         ordinarily-cached artifact rather than needing their classified-zip fallback.
     *         Best-effort: a failure fetching or verifying the POM is logged and otherwise
     *         ignored, since the zip — the actually-needed payload — already succeeded.
     */
    public record AssetVerification(Optional<String> zipSha256, Optional<PomFetch> pom) {
        /** No checksum to verify, no POM to fetch — the common case for a first cut/test artifact. */
        public static final AssetVerification NONE = new AssetVerification(Optional.empty(), Optional.empty());

        public AssetVerification {
            Objects.requireNonNull(zipSha256, "zipSha256");
            Objects.requireNonNull(pom, "pom");
        }

        /**
         * @param pomUri the POM's direct download URI
         * @param localPomCacheFile where to cache it under {@code ~/.m2/repository}, alongside
         *         the zip ({@link #localRepositoryPath} with a {@code .pom} filename)
         * @param expectedSha256 the POM's expected SHA-256 checksum, if known
         */
        public record PomFetch(URI pomUri, Path localPomCacheFile, Optional<String> expectedSha256) {
            public PomFetch {
                Objects.requireNonNull(pomUri, "pomUri");
                Objects.requireNonNull(localPomCacheFile, "localPomCacheFile");
                Objects.requireNonNull(expectedSha256, "expectedSha256");
            }
        }
    }

    private static final int BUFFER_SIZE = 8192;

    /**
     * Maven's resolved-snapshot version form: a base version, the UTC deploy timestamp
     * {@code yyyyMMdd.HHmmss}, and the build number — {@code 1.0.0-20260724.000852-12}. Group 1 is
     * the base version, from which the {@code -SNAPSHOT} directory name is rebuilt. Anchoring the
     * timestamp's shape keeps a date-based release version (SOLOR's {@code 20250827}) from
     * matching.
     */
    private static final Pattern RESOLVED_SNAPSHOT_VERSION =
            Pattern.compile("^(.+)-(\\d{8}\\.\\d{6})-(\\d+)$");

    /**
     * How much of the overall [0,1] progress bar the download phase gets when a fresh download
     * is actually needed — the rest goes to materializing (unpack/copy), so a large multi-hundred
     * -MB zip's extraction time is visibly represented too, not silently absorbed after the bar
     * already reads 100%. When no download is needed (a verified cache hit, including every
     * "Local repository" browse), materializing gets the <em>entire</em> bar — fixing local
     * downloads showing no progress indicator at all, since previously only the download loop
     * ever called {@code updateProgress}.
     */
    private static final double DOWNLOAD_PROGRESS_WEIGHT = 0.85;

    private static final long PROGRESS_TOTAL = 1_000_000L;

    /**
     * Hidden marker file written inside every {@link #unpack}ped destination directory, recording
     * the SHA-256 of the source zip it was extracted from — the "changed since download" signal
     * for the {@code -sa}/{@code -rkb} flows, where (unlike {@link #placeAsFile}'s single
     * placed-as-is zip) the materialized result is a whole directory of extracted content that
     * can't be hashed back against the repository's reported zip checksum directly. Written
     * unconditionally on every successful unpack, whether or not {@link AssetVerification#zipSha256}
     * was actually known for this materialization — it always records the hash of whatever zip
     * really was unpacked, so a later caller can compare it against a freshly re-probed
     * repository checksum to detect drift, even for a purely local (never-verified) unpack.
     */
    private static final String SOURCE_CHECKSUM_MARKER_FILENAME = ".ike-source-sha256";

    /**
     * Hidden marker file recording a hash of the currently-extracted directory's own content —
     * distinct from {@link #SOURCE_CHECKSUM_MARKER_FILENAME}, which only records what the
     * <em>source zip</em> looked like, not what actually landed on disk. A source-zip match only
     * proves the repository hasn't changed since this was downloaded; it says nothing about
     * whether the extracted directory itself has since been edited, partially deleted, or
     * corrupted on disk. Comparing a freshly recomputed {@link #currentUnpackedContentSha256}
     * against this stored value is what actually verifies the current on-disk state.
     */
    private static final String CONTENT_CHECKSUM_MARKER_FILENAME = ".ike-content-sha256";

    private final HttpClient httpClient;
    private final Optional<URI> downloadUri;
    private final Path localRepositoryCacheFile;
    private final Mode mode;
    private final Path destination;
    private final Optional<Credentials> credentials;
    private final AssetVerification verification;

    private MavenDataStoreDownloadTask(HttpClient httpClient, Optional<URI> downloadUri, Path localRepositoryCacheFile,
                                        Mode mode, Path destination, Optional<Credentials> credentials, AssetVerification verification) {
        super(true, false);
        this.httpClient = httpClient;
        this.downloadUri = Objects.requireNonNull(downloadUri, "downloadUri");
        this.localRepositoryCacheFile = Objects.requireNonNull(localRepositoryCacheFile, "localRepositoryCacheFile");
        this.mode = Objects.requireNonNull(mode, "mode");
        this.destination = Objects.requireNonNull(destination, "destination");
        this.credentials = Objects.requireNonNull(credentials, "credentials");
        this.verification = Objects.requireNonNull(verification, "verification");
    }

    /**
     * Creates a task that ensures {@code downloadUri} is cached at {@code localRepositoryCacheFile}
     * (downloading it there first if it isn't already present) and unpacks it into
     * {@code destinationDirectory} (created if absent) — for a {@code -sa} store snapshot.
     *
     * @param httpClient the HTTP client to download with
     * @param downloadUri the artifact's direct download URI
     * @param localRepositoryCacheFile where this artifact is cached under {@code ~/.m2/repository}
     *         ({@link #localRepositoryPath}) — reused as-is if already present
     * @param destinationDirectory the directory to unpack into
     * @param verification checksum/POM handling — {@link AssetVerification#NONE} for neither
     * @return the new task
     */
    public static MavenDataStoreDownloadTask unpackTask(HttpClient httpClient, URI downloadUri, Path localRepositoryCacheFile,
                                                          Path destinationDirectory, AssetVerification verification) {
        return new MavenDataStoreDownloadTask(Objects.requireNonNull(httpClient, "httpClient"), Optional.of(downloadUri),
                localRepositoryCacheFile, Mode.UNPACK, destinationDirectory, Optional.empty(), verification);
    }

    /**
     * As {@link #unpackTask(HttpClient, URI, Path, Path, AssetVerification)}, authenticating with
     * {@code credentials}.
     *
     * @param httpClient the HTTP client to download with
     * @param downloadUri the artifact's direct download URI
     * @param localRepositoryCacheFile where this artifact is cached under {@code ~/.m2/repository}
     * @param destinationDirectory the directory to unpack into
     * @param verification checksum/POM handling — {@link AssetVerification#NONE} for neither
     * @param credentials the credentials to authenticate the download with
     * @return the new task
     */
    public static MavenDataStoreDownloadTask unpackTask(HttpClient httpClient, URI downloadUri, Path localRepositoryCacheFile,
                                                          Path destinationDirectory, AssetVerification verification, Credentials credentials) {
        return new MavenDataStoreDownloadTask(Objects.requireNonNull(httpClient, "httpClient"), Optional.of(downloadUri),
                localRepositoryCacheFile, Mode.UNPACK, destinationDirectory,
                Optional.of(Objects.requireNonNull(credentials, "credentials")), verification);
    }

    /**
     * Creates a task that ensures {@code downloadUri} is cached at {@code localRepositoryCacheFile}
     * (downloading it there first if it isn't already present) and copies it as-is to
     * {@code destinationFile} — for a {@code -pb} changeset zip.
     *
     * @param httpClient the HTTP client to download with
     * @param downloadUri the artifact's direct download URI
     * @param localRepositoryCacheFile where this artifact is cached under {@code ~/.m2/repository}
     * @param destinationFile the file path to copy the cached zip to
     * @param verification checksum/POM handling — {@link AssetVerification#NONE} for neither
     * @return the new task
     */
    public static MavenDataStoreDownloadTask placeAsFileTask(HttpClient httpClient, URI downloadUri, Path localRepositoryCacheFile,
                                                               Path destinationFile, AssetVerification verification) {
        return new MavenDataStoreDownloadTask(Objects.requireNonNull(httpClient, "httpClient"), Optional.of(downloadUri),
                localRepositoryCacheFile, Mode.PLACE_AS_FILE, destinationFile, Optional.empty(), verification);
    }

    /**
     * As {@link #placeAsFileTask(HttpClient, URI, Path, Path, AssetVerification)}, authenticating
     * with {@code credentials}.
     *
     * @param httpClient the HTTP client to download with
     * @param downloadUri the artifact's direct download URI
     * @param localRepositoryCacheFile where this artifact is cached under {@code ~/.m2/repository}
     * @param destinationFile the file path to copy the cached zip to
     * @param verification checksum/POM handling — {@link AssetVerification#NONE} for neither
     * @param credentials the credentials to authenticate the download with
     * @return the new task
     */
    public static MavenDataStoreDownloadTask placeAsFileTask(HttpClient httpClient, URI downloadUri, Path localRepositoryCacheFile,
                                                               Path destinationFile, AssetVerification verification, Credentials credentials) {
        return new MavenDataStoreDownloadTask(Objects.requireNonNull(httpClient, "httpClient"), Optional.of(downloadUri),
                localRepositoryCacheFile, Mode.PLACE_AS_FILE, destinationFile,
                Optional.of(Objects.requireNonNull(credentials, "credentials")), verification);
    }

    /**
     * Creates a task that unpacks an artifact already present at {@code localRepositoryCacheFile}
     * — no network access at all — for browsing the "Local repository" source, where the
     * artifact was found by walking {@code ~/.m2/repository} directly.
     *
     * @param localRepositoryCacheFile the artifact's already-cached location; must exist
     * @param destinationDirectory the directory to unpack into
     * @return the new task
     */
    public static MavenDataStoreDownloadTask localUnpackTask(Path localRepositoryCacheFile, Path destinationDirectory) {
        return new MavenDataStoreDownloadTask(null, Optional.empty(), localRepositoryCacheFile, Mode.UNPACK,
                destinationDirectory, Optional.empty(), AssetVerification.NONE);
    }

    /**
     * As {@link #localUnpackTask(Path, Path)}, copying the cached zip as-is rather than
     * unpacking it — for a {@code -pb} changeset zip already present locally.
     *
     * @param localRepositoryCacheFile the artifact's already-cached location; must exist
     * @param destinationFile the file path to copy the cached zip to
     * @return the new task
     */
    public static MavenDataStoreDownloadTask localPlaceAsFileTask(Path localRepositoryCacheFile, Path destinationFile) {
        return new MavenDataStoreDownloadTask(null, Optional.empty(), localRepositoryCacheFile, Mode.PLACE_AS_FILE,
                destinationFile, Optional.empty(), AssetVerification.NONE);
    }

    /**
     * The version <em>directory</em> a published artifact lives under, given any concrete version.
     *
     * <p>Maven publishes a snapshot build under the base {@code -SNAPSHOT} directory while the
     * filename carries the resolved timestamp: build
     * {@code 1.0.0-20260724.000852-12} is served from {@code 1.0.0-SNAPSHOT/}. A repository search
     * answers with the resolved build, so using that answer as the directory too requests a path
     * that does not exist — a 404 on an artifact that is present (ikmdev/komet-desktop#116).
     *
     * <p>Only Maven's resolved-snapshot form {@code <base>-<yyyyMMdd.HHmmss>-<buildNumber>} is
     * rewritten. A release — including a date-based one such as {@code 20250827} — is its own
     * directory and is returned unchanged.
     *
     * @param version a concrete version: a resolved snapshot build, a {@code -SNAPSHOT}, or a release
     * @return the version directory segment to request that version under
     */
    public static String directoryVersion(String version) {
        Objects.requireNonNull(version, "version");
        Matcher resolvedSnapshot = RESOLVED_SNAPSHOT_VERSION.matcher(version);
        return resolvedSnapshot.matches() ? resolvedSnapshot.group(1) + "-SNAPSHOT" : version;
    }

    /**
     * Builds the direct download URI for a classified release artifact, following the standard
     * Maven 2 repository layout: {@code <repositoryBaseUrl>/<groupPath>/<artifactId>/<version>/
     * <artifactId>-<version>-<classifier>.zip}. Equivalent to
     * {@link #downloadUri(String, ArtifactCoordinates, String, String, String)} with the same
     * value for both the directory and file version — correct for a release, where they're
     * always the same string. For a {@code -SNAPSHOT} version, use the 5-argument overload with
     * the resolved file version instead (see {@code RemoteVersionResolver.resolveSnapshotFileVersion}).
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact's groupId:artifactId
     * @param version the version to download
     * @param classifier the Maven classifier (e.g. {@code "reasoned-sa"}, {@code "reasoned-pb"})
     * @return the direct download URI
     */
    public static URI downloadUri(String repositoryBaseUrl, ArtifactCoordinates coordinates, String version, String classifier) {
        return downloadUri(repositoryBaseUrl, coordinates, version, version, classifier);
    }

    /**
     * As {@link #downloadUri(String, ArtifactCoordinates, String, String)}, but with the version
     * <em>directory</em> segment and the version embedded in the <em>filename</em> given
     * separately — needed for a {@code -SNAPSHOT} {@code directoryVersion} (e.g.
     * {@code "1.0.0-SNAPSHOT"}), whose published filename instead embeds the concrete,
     * uniquely-timestamped {@code fileVersion} a Nexus-hosted snapshot repository actually
     * resolves it to (e.g. {@code "1.0.0-20260714.135548-4"}) — the directory itself keeps the
     * {@code -SNAPSHOT} name.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact's groupId:artifactId
     * @param directoryVersion the version directory to look under
     * @param fileVersion the version embedded in the filename itself
     * @param classifier the Maven classifier
     * @return the direct download URI
     */
    public static URI downloadUri(String repositoryBaseUrl, ArtifactCoordinates coordinates, String directoryVersion,
                                   String fileVersion, String classifier) {
        String base = repositoryBaseUrl.endsWith("/") ? repositoryBaseUrl : repositoryBaseUrl + "/";
        String fileName = coordinates.artifactId() + "-" + fileVersion + "-" + classifier + ".zip";
        return URI.create(base + coordinates.groupPath() + "/" + coordinates.artifactId() + "/" + directoryVersion + "/" + fileName);
    }

    /**
     * The path a classified release artifact is cached at under a local Maven repository root
     * (e.g. {@code ~/.m2/repository}), following the same standard Maven 2 layout as
     * {@link #downloadUri(String, ArtifactCoordinates, String, String)} — the same location
     * ordinary {@code mvn} downloads would land at, and the same location the "Local repository"
     * browsing source looks for artifacts. Equivalent to
     * {@link #localRepositoryPath(Path, ArtifactCoordinates, String, String, String)} with the
     * same value for both the directory and file version.
     *
     * @param localRepositoryRoot the local repository root (e.g. {@code ~/.m2/repository})
     * @param coordinates the artifact's groupId:artifactId
     * @param version the version
     * @param classifier the Maven classifier
     * @return the cache file path
     */
    public static Path localRepositoryPath(Path localRepositoryRoot, ArtifactCoordinates coordinates, String version, String classifier) {
        return localRepositoryPath(localRepositoryRoot, coordinates, version, version, classifier);
    }

    /**
     * As {@link #localRepositoryPath(Path, ArtifactCoordinates, String, String)}, with the
     * version directory and the version embedded in the filename given separately — see
     * {@link #downloadUri(String, ArtifactCoordinates, String, String, String)} for why a
     * {@code -SNAPSHOT} {@code directoryVersion} needs a distinct, resolved {@code fileVersion}.
     *
     * @param localRepositoryRoot the local repository root (e.g. {@code ~/.m2/repository})
     * @param coordinates the artifact's groupId:artifactId
     * @param directoryVersion the version directory to look under
     * @param fileVersion the version embedded in the filename itself
     * @param classifier the Maven classifier
     * @return the cache file path
     */
    public static Path localRepositoryPath(Path localRepositoryRoot, ArtifactCoordinates coordinates, String directoryVersion,
                                            String fileVersion, String classifier) {
        String fileName = coordinates.artifactId() + "-" + fileVersion + "-" + classifier + ".zip";
        return localRepositoryRoot.resolve(coordinates.groupPath()).resolve(coordinates.artifactId())
                .resolve(directoryVersion).resolve(fileName);
    }

    /**
     * The direct download URI for an artifact's main POM (no classifier) — the standard Maven 2
     * layout, mirroring {@link #downloadUri(String, ArtifactCoordinates, String, String, String)}
     * but without a classifier and with {@code .pom} instead of {@code .zip}.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact's groupId:artifactId
     * @param directoryVersion the version directory to look under
     * @param fileVersion the version embedded in the filename itself
     * @return the direct download URI
     */
    public static URI pomUri(String repositoryBaseUrl, ArtifactCoordinates coordinates, String directoryVersion, String fileVersion) {
        String base = repositoryBaseUrl.endsWith("/") ? repositoryBaseUrl : repositoryBaseUrl + "/";
        String fileName = coordinates.artifactId() + "-" + fileVersion + ".pom";
        return URI.create(base + coordinates.groupPath() + "/" + coordinates.artifactId() + "/" + directoryVersion + "/" + fileName);
    }

    /**
     * The path an artifact's main POM is cached at under a local Maven repository root,
     * alongside where {@link #localRepositoryPath} caches its classified zip.
     *
     * @param localRepositoryRoot the local repository root (e.g. {@code ~/.m2/repository})
     * @param coordinates the artifact's groupId:artifactId
     * @param directoryVersion the version directory to look under
     * @param fileVersion the version embedded in the filename itself
     * @return the cache file path
     */
    public static Path localPomPath(Path localRepositoryRoot, ArtifactCoordinates coordinates, String directoryVersion, String fileVersion) {
        String fileName = coordinates.artifactId() + "-" + fileVersion + ".pom";
        return localRepositoryRoot.resolve(coordinates.groupPath()).resolve(coordinates.artifactId())
                .resolve(directoryVersion).resolve(fileName);
    }

    /**
     * Probes {@code uri} with an HTTP {@code HEAD} request and returns its
     * {@code Content-Length}, for displaying artifact size before downloading.
     *
     * @param httpClient the HTTP client to probe with
     * @param uri the artifact's direct download URI
     * @return the content length in bytes, or empty if the server didn't report one
     * @throws IOException if the request fails or returns a non-200 status
     * @throws InterruptedException if the request is interrupted
     */
    public static java.util.OptionalLong headContentLength(HttpClient httpClient, URI uri) throws IOException, InterruptedException {
        return headContentLength(httpClient, uri, null);
    }

    /**
     * As {@link #headContentLength(HttpClient, URI)}, authenticating with {@code credentials}.
     *
     * @param httpClient the HTTP client to probe with
     * @param uri the artifact's direct download URI
     * @param credentials the credentials to authenticate with, or {@code null} for none
     * @return the content length in bytes, or empty if the server didn't report one
     * @throws IOException if the request fails or returns a non-200 status
     * @throws InterruptedException if the request is interrupted
     */
    public static java.util.OptionalLong headContentLength(HttpClient httpClient, URI uri, Credentials credentials)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).method("HEAD", HttpRequest.BodyPublishers.noBody());
        if (credentials != null) {
            requestBuilder.header("Authorization", BasicAuth.header(credentials));
        }
        HttpResponse<Void> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() != 200) {
            throw new IOException("Unexpected HTTP " + response.statusCode() + " probing " + uri);
        }
        return response.headers().firstValueAsLong("Content-Length");
    }

    /**
     * Ensures {@link #localRepositoryCacheFile} is present and its content still matches what
     * the repository currently reports — downloading {@link #downloadUri} there if it's missing
     * <em>or</em> a known-cached copy's SHA-256 no longer matches {@link AssetVerification#zipSha256}
     * (the repository's content changed since it was cached; the stale copy is treated as if it
     * were never cached and re-fetched) — then fetches {@link AssetVerification#pom}, if
     * requested, and materializes the (now-verified) cached zip at {@link #destination} per
     * {@link #mode}. Progress spans the whole operation: when a download actually happens it
     * gets {@link #DOWNLOAD_PROGRESS_WEIGHT} of the bar and materializing gets the rest; when the
     * cache hit is already verified-fresh, materializing alone gets the entire bar (this is also
     * what makes a "Local repository" browse — never anything to download — show progress at all).
     *
     * @return the resulting path — {@link #destination} in both modes
     * @throws IOException if the download or materialization fails, the cache file is missing
     *         with no {@link #downloadUri} to fetch it from, or a freshly-downloaded zip's
     *         checksum doesn't match {@link AssetVerification#zipSha256}
     * @throws InterruptedException if the download is interrupted
     * @throws CancellationException if {@link #cancel()} was called before the operation
     *         completed; no partial file is left at {@link #localRepositoryCacheFile}, though
     *         {@link #destination} may hold a partially-materialized result if cancelled mid-unpack
     */
    @Override
    protected Path compute() throws Exception {
        boolean needsDownload = !Files.exists(localRepositoryCacheFile);
        if (!needsDownload && verification.zipSha256().isPresent()) {
            updateTitle("Verifying cached copy at " + localRepositoryCacheFile);
            String actualSha256 = sha256Hex(localRepositoryCacheFile);
            if (!actualSha256.equalsIgnoreCase(verification.zipSha256().get())) {
                LOG.info("Cached copy no longer matches the repository's reported checksum — refetching: {}", localRepositoryCacheFile);
                needsDownload = true;
            }
        }

        double downloadWeight = needsDownload ? DOWNLOAD_PROGRESS_WEIGHT : 0.0;
        String verificationNote;
        if (needsDownload) {
            if (downloadUri.isEmpty()) {
                throw new IOException("No local copy at " + localRepositoryCacheFile + " and no download URI provided");
            }
            updateTitle("Downloading " + downloadUri.get());
            boolean verified = downloadToCache(downloadUri.get(), localRepositoryCacheFile, verification.zipSha256(), downloadWeight);
            if (isCancelled()) {
                throw new CancellationException("Download cancelled: " + downloadUri.get());
            }
            verificationNote = verified
                    ? " (SHA-256 checksum verified ✓)"
                    : " (no checksum reported by the repository to verify against)";
        } else {
            updateTitle("Using cached copy at " + localRepositoryCacheFile);
            verificationNote = verification.zipSha256().isPresent()
                    ? " (SHA-256 checksum verified — source unchanged ✓)"
                    : " (already cached locally — not re-verified)";
        }
        verification.pom().ifPresent(this::fetchPomBestEffort);

        double materializeBase = downloadWeight;
        double materializeWeight = 1.0 - downloadWeight;
        Path result = switch (mode) {
            case UNPACK -> {
                updateTitle("Unpacking " + destination);
                yield unpack(localRepositoryCacheFile, destination, materializeBase, materializeWeight);
            }
            case PLACE_AS_FILE -> {
                updateTitle("Copying " + destination);
                yield placeAsFile(localRepositoryCacheFile, destination, materializeBase, materializeWeight);
            }
        };
        if (isCancelled()) {
            throw new CancellationException("Materialization cancelled: " + destination);
        }
        // Kept in the message shown once materializing completes (not a separate, instantly
        // -overwritten status) since that's the only window before this dialog closes on
        // success where the user can actually see a checksum-verification signal at all.
        updateMessage("Materialized " + destination + verificationNote);
        return result;
    }

    private void reportProgress(double overallFraction) {
        long workDone = Math.round(Math.min(Math.max(overallFraction, 0.0), 1.0) * PROGRESS_TOTAL);
        updateProgress(workDone, PROGRESS_TOTAL);
    }

    /**
     * Downloads {@code uri} to {@code cacheFile} (via a same-directory temp file, so a crash or
     * cancellation never leaves a partial file at {@code cacheFile} itself), verifying against
     * {@code expectedSha256} once the download completes if a value was given. Reports progress
     * scaled into {@code [0, progressWeight]} of the overall bar.
     *
     * @return {@code true} if a checksum was verified successfully; {@code false} if there was
     *         none to check against ({@code expectedSha256} was empty) — the caller surfaces
     *         this distinction to the user rather than treating both as silently equivalent
     * @throws IOException if the request fails, or the downloaded content's SHA-256 doesn't
     *         match {@code expectedSha256} — a corrupted or tampered download must never be used
     */
    private boolean downloadToCache(URI uri, Path cacheFile, Optional<String> expectedSha256, double progressWeight)
            throws IOException, InterruptedException {
        Files.createDirectories(cacheFile.getParent());
        Path tempFile = Files.createTempFile(cacheFile.getParent(), "maven-datastore-download", ".zip.tmp");
        try {
            MessageDigest digest = expectedSha256.isPresent() ? sha256Digest() : null;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).GET();
            credentials.ifPresent(creds -> requestBuilder.header("Authorization", BasicAuth.header(creds)));
            HttpResponse<InputStream> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected HTTP " + response.statusCode() + " downloading " + uri);
            }
            long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            long bytesRead = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(tempFile)) {
                int n = in.read(buffer);
                while (n != -1) {
                    if (isCancelled()) {
                        return false;
                    }
                    out.write(buffer, 0, n);
                    if (digest != null) {
                        digest.update(buffer, 0, n);
                    }
                    bytesRead += n;
                    if (contentLength > 0) {
                        reportProgress(progressWeight * (bytesRead / (double) contentLength));
                    }
                    n = in.read(buffer);
                }
            }
            if (isCancelled()) {
                return false;
            }
            boolean verified = false;
            if (digest != null) {
                String actualSha256 = HexFormat.of().formatHex(digest.digest());
                if (!actualSha256.equalsIgnoreCase(expectedSha256.get())) {
                    throw new IOException("SHA-256 checksum mismatch downloading " + uri + " — expected "
                            + expectedSha256.get() + " but got " + actualSha256
                            + "; refusing to use a download that doesn't match what the repository reported");
                }
                verified = true;
            }
            Files.move(tempFile, cacheFile, StandardCopyOption.REPLACE_EXISTING);
            return verified;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Fetches and caches {@code pomFetch}'s POM, verifying its checksum if known — best-effort:
     * any failure (a non-200 response, a checksum mismatch, a transient network error) is logged
     * and otherwise swallowed, since the zip — the actually-needed payload — already succeeded
     * by the time this runs, and a missing/discarded POM only affects
     * {@code LocalRepositorySearch}/{@code LocalVersionResolver}'s ability to recognize this as
     * an ordinarily-cached artifact later (both already fall back to recognizing the classified
     * zip alone).
     */
    private void fetchPomBestEffort(AssetVerification.PomFetch pomFetch) {
        if (Files.exists(pomFetch.localPomCacheFile())) {
            return;
        }
        updateMessage("Fetching POM " + pomFetch.pomUri());
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(pomFetch.pomUri()).GET();
            credentials.ifPresent(creds -> requestBuilder.header("Authorization", BasicAuth.header(creds)));
            HttpResponse<byte[]> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                LOG.warn("Failed to fetch POM (non-fatal, continuing without it): unexpected HTTP {} from {}",
                        response.statusCode(), pomFetch.pomUri());
                return;
            }
            byte[] body = response.body();
            if (pomFetch.expectedSha256().isPresent()) {
                String actualSha256 = HexFormat.of().formatHex(sha256Digest().digest(body));
                if (!actualSha256.equalsIgnoreCase(pomFetch.expectedSha256().get())) {
                    LOG.warn("POM checksum mismatch (non-fatal, discarding it): {} expected {} but got {}",
                            pomFetch.pomUri(), pomFetch.expectedSha256().get(), actualSha256);
                    return;
                }
            }
            Files.createDirectories(pomFetch.localPomCacheFile().getParent());
            Path tempFile = Files.createTempFile(pomFetch.localPomCacheFile().getParent(), "pom-download", ".pom.tmp");
            try {
                Files.write(tempFile, body);
                Files.move(tempFile, pomFetch.localPomCacheFile(), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } catch (IOException e) {
            LOG.warn("Failed to fetch POM (non-fatal, continuing without it): " + pomFetch.pomUri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is a JDK-guaranteed MessageDigest algorithm", e);
        }
    }

    /**
     * Hashes an existing local file — used to re-verify a cache hit, and by callers (e.g. the
     * download dialog, checking a {@link Mode#PLACE_AS_FILE} destination that may already exist
     * under {@code ~/Solor}) that want to compare an already-materialized result against a
     * freshly-resolved {@link AssetVerification#zipSha256} to detect drift before downloading.
     *
     * @param file the file to hash
     * @return the hex-encoded SHA-256 digest
     * @throws IOException if the file cannot be read
     */
    public static String sha256Hex(Path file) throws IOException {
        MessageDigest digest = sha256Digest();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n = in.read(buffer);
            while (n != -1) {
                digest.update(buffer, 0, n);
                n = in.read(buffer);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Reads back the {@link #SOURCE_CHECKSUM_MARKER_FILENAME} marker {@link #unpack} leaves
     * inside a destination directory — the source zip's SHA-256 at the time that directory was
     * last (re)materialized, for comparing against a freshly-resolved
     * {@link AssetVerification#zipSha256} to tell whether the repository's content has since
     * changed. Empty if {@code destinationDirectory} was never populated by {@link #unpack}
     * (including anything materialized before this marker existed), or the marker can't be read.
     *
     * @param destinationDirectory a {@code -sa}/{@code -rkb} destination directory previously
     *         passed to {@link #unpackTask}/{@link #localUnpackTask}
     * @return the recorded SHA-256, if present and readable
     */
    public static Optional<String> existingUnpackedSourceSha256(Path destinationDirectory) {
        try {
            return Optional.of(Files.readString(destinationDirectory.resolve(SOURCE_CHECKSUM_MARKER_FILENAME)).strip());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Reads back the {@link #CONTENT_CHECKSUM_MARKER_FILENAME} marker {@link #unpack} leaves
     * inside a destination directory — a hash of the directory's own content as it stood
     * immediately after extraction. Compare against {@link #currentUnpackedContentSha256} to
     * detect local drift (edited, partially deleted, or corrupted files) since then. Empty if
     * {@code destinationDirectory} was never populated by {@link #unpack} (including anything
     * materialized before this marker existed), or the marker can't be read.
     *
     * @param destinationDirectory a {@code -sa}/{@code -rkb} destination directory previously
     *         passed to {@link #unpackTask}/{@link #localUnpackTask}
     * @return the recorded content hash, if present and readable
     */
    public static Optional<String> existingUnpackedContentSha256(Path destinationDirectory) {
        try {
            return Optional.of(Files.readString(destinationDirectory.resolve(CONTENT_CHECKSUM_MARKER_FILENAME)).strip());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Recomputes {@code destinationDirectory}'s content hash right now, from what's actually on
     * disk — for comparing against {@link #existingUnpackedContentSha256}'s stored value to tell
     * whether the directory has drifted since it was last {@link #unpack}ped. Reads every regular
     * file under {@code destinationDirectory} (excluding the marker files themselves), so this is
     * proportional to the directory's total size — expect it to take real time for a large store.
     *
     * @param destinationDirectory a {@code -sa}/{@code -rkb} destination directory
     * @return the current content hash
     * @throws IOException if the directory can't be walked or a file can't be read
     */
    public static String currentUnpackedContentSha256(Path destinationDirectory) throws IOException {
        return hashDirectoryContent(destinationDirectory);
    }

    /**
     * Hashes {@code directory}'s current on-disk content: every regular file's path (relative to
     * {@code directory}) and bytes, visited in a fixed sorted order so the result depends only on
     * what's actually there, not filesystem iteration order. Skips
     * {@link #SOURCE_CHECKSUM_MARKER_FILENAME} and {@link #CONTENT_CHECKSUM_MARKER_FILENAME}
     * themselves — this hashes the payload, not its own provenance markers.
     */
    private static String hashDirectoryContent(Path directory) throws IOException {
        MessageDigest digest = sha256Digest();
        byte[] buffer = new byte[BUFFER_SIZE];
        List<Path> files;
        try (var walk = Files.walk(directory)) {
            files = walk.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return !name.equals(SOURCE_CHECKSUM_MARKER_FILENAME) && !name.equals(CONTENT_CHECKSUM_MARKER_FILENAME);
                    })
                    .sorted()
                    .toList();
        }
        for (Path file : files) {
            digest.update(directory.relativize(file).toString().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            try (InputStream in = Files.newInputStream(file)) {
                int n = in.read(buffer);
                while (n != -1) {
                    digest.update(buffer, 0, n);
                    n = in.read(buffer);
                }
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Unpacks {@code zipFile} into {@code destinationDirectory}.
     *
     * <p>Real published store snapshots (confirmed against a live artifact,
     * {@code dev.ikm.tinkar.data:loinc:1.0.0-SNAPSHOT}'s {@code reasoned-sa} classifier, 2026-07-21)
     * wrap every entry under a single top-level directory named after the artifact (e.g.
     * {@code loinc/nidToByteArrayMap/...}) rather than placing the store's files at the zip's
     * own root. Extracting that verbatim landed the real store one directory too deep, while
     * Komet's own SpinedArray bootstrap silently created a fresh, empty scaffold at
     * {@code destinationDirectory} itself and tried to open <em>that</em> — producing a store
     * that looked present but had no actual content, and NullPointerExceptions the moment
     * anything tried to read data that was never really there. {@link #commonTopLevelDirectory}
     * detects that single-wrapper shape and strips it, the same way {@code tar --strip-components=1}
     * would; a zip with no single common top-level directory (e.g. this class's own test
     * fixtures, entries like {@code nidToPatternNidMap/.keep} directly at the root) is extracted
     * as-is, unchanged from before.
     */
    private Path unpack(Path zipFile, Path destinationDirectory, double progressBase, double progressWeight) throws IOException {
        Files.createDirectories(destinationDirectory);
        Path normalizedDestination = destinationDirectory.normalize();
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            Optional<String> wrapperDirectory = commonTopLevelDirectory(zip);
            long totalUncompressedBytes = totalUncompressedBytes(zip);
            long bytesExtracted = 0;
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                if (isCancelled()) {
                    return destinationDirectory;
                }
                ZipEntry entry = entries.nextElement();
                String entryName = wrapperDirectory.map(wrapper -> stripLeadingDirectory(entry.getName(), wrapper))
                        .orElse(entry.getName());
                if (entryName.isEmpty()) {
                    continue; // the wrapper directory's own entry, once its name is fully stripped
                }
                Path target = normalizedDestination.resolve(entryName).normalize();
                if (!target.startsWith(normalizedDestination)) {
                    throw new IOException("Zip entry escapes destination directory: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    try (InputStream in = zip.getInputStream(entry)) {
                        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                    bytesExtracted += Math.max(entry.getSize(), 0);
                    if (totalUncompressedBytes > 0) {
                        reportProgress(progressBase + progressWeight * (bytesExtracted / (double) totalUncompressedBytes));
                    }
                }
            }
        }
        Files.writeString(destinationDirectory.resolve(SOURCE_CHECKSUM_MARKER_FILENAME), sha256Hex(zipFile));
        Files.writeString(destinationDirectory.resolve(CONTENT_CHECKSUM_MARKER_FILENAME), hashDirectoryContent(destinationDirectory));
        reportProgress(progressBase + progressWeight);
        return destinationDirectory;
    }

    /** The sum of every non-directory entry's uncompressed size — the "total work" for {@link #unpack}'s progress. */
    private static long totalUncompressedBytes(ZipFile zip) {
        long total = 0;
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                total += Math.max(entry.getSize(), 0);
            }
        }
        return total;
    }

    /**
     * The single top-level directory every entry in {@code zip} is nested under, if there is
     * one — e.g. {@code "loinc"} for a zip whose every entry starts with {@code loinc/}. Empty
     * if any entry sits directly at the zip's root, or entries are split across more than one
     * top-level directory (an actually-flat, multi-root, or already-unwrapped zip) — in either
     * case there's no single wrapper to strip.
     */
    private static Optional<String> commonTopLevelDirectory(ZipFile zip) {
        String commonPrefix = null;
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            int firstSlash = name.indexOf('/');
            if (firstSlash <= 0) {
                return Optional.empty();
            }
            String topLevelDirectory = name.substring(0, firstSlash);
            if (commonPrefix == null) {
                commonPrefix = topLevelDirectory;
            } else if (!commonPrefix.equals(topLevelDirectory)) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(commonPrefix);
    }

    private static String stripLeadingDirectory(String entryName, String directory) {
        String withoutDirectory = entryName.substring(directory.length());
        return withoutDirectory.startsWith("/") ? withoutDirectory.substring(1) : withoutDirectory;
    }

    private Path placeAsFile(Path zipFile, Path destinationFile, double progressBase, double progressWeight) throws IOException {
        Files.createDirectories(destinationFile.getParent());
        long totalBytes = Files.size(zipFile);
        long bytesCopied = 0;
        Path tempFile = Files.createTempFile(destinationFile.getParent(), "maven-datastore-place", ".tmp");
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            try (InputStream in = Files.newInputStream(zipFile); OutputStream out = Files.newOutputStream(tempFile)) {
                int n = in.read(buffer);
                while (n != -1) {
                    if (isCancelled()) {
                        return destinationFile;
                    }
                    out.write(buffer, 0, n);
                    bytesCopied += n;
                    if (totalBytes > 0) {
                        reportProgress(progressBase + progressWeight * (bytesCopied / (double) totalBytes));
                    }
                    n = in.read(buffer);
                }
            }
            Files.move(tempFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
        reportProgress(progressBase + progressWeight);
        return destinationFile;
    }
}
