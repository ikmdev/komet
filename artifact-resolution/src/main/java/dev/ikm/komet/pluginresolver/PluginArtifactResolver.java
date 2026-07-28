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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Facade for resolving a plugin (or data-store) artifact by version and staging it into a
 * directory — the entry point Komet UI code calls.
 *
 * <p>{@link #resolveAndStage} stages only the requested artifact's own jar, not its
 * transitive runtime dependencies — this matches existing practice in
 * {@code komet-desktop/pom.xml}, which manually lists each staged plugin's extra runtime
 * jars as separate {@code <artifactItem>} entries rather than resolving them automatically.
 * Any additional runtime dependencies a plugin needs must still be staged separately.
 *
 * <p>Because {@code IkeServiceManager} builds one shared {@code ModuleLayer} for every jar
 * in the plugin directory (duplicate module names fail resolution), {@link #resolveAndStage}
 * removes any previously-staged jar(s) for the same artifactId before staging the requested
 * version — there is no side-by-side multi-version support.
 */
public final class PluginArtifactResolver {

    private static final Logger LOG = LoggerFactory.getLogger(PluginArtifactResolver.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final Path localRepositoryRoot;
    private final LocalVersionResolver localVersionResolver;
    private final RemoteVersionResolver remoteVersionResolver;
    private final HttpClient httpClient;

    /**
     * Creates a resolver over the given local repository root, using default HTTP clients
     * for remote access.
     *
     * @param localRepositoryRoot the local repository root directory (e.g. {@code ~/.m2/repository})
     */
    public PluginArtifactResolver(Path localRepositoryRoot) {
        this(localRepositoryRoot, new RemoteVersionResolver(), HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build());
    }

    /**
     * Creates a resolver with explicit collaborators, for testing against a fixture server.
     *
     * @param localRepositoryRoot the local repository root directory
     * @param remoteVersionResolver the resolver to use for listing remote versions
     * @param httpClient the HTTP client to use for downloading artifact jars
     * @throws NullPointerException if any argument is {@code null}
     */
    public PluginArtifactResolver(Path localRepositoryRoot, RemoteVersionResolver remoteVersionResolver, HttpClient httpClient) {
        this.localRepositoryRoot = Objects.requireNonNull(localRepositoryRoot, "localRepositoryRoot");
        this.localVersionResolver = new LocalVersionResolver(localRepositoryRoot);
        this.remoteVersionResolver = Objects.requireNonNull(remoteVersionResolver, "remoteVersionResolver");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    /**
     * Lists the versions of {@code coordinates} already present in the local repository.
     *
     * @param coordinates the artifact to look up
     * @return the locally-present versions, sorted lexicographically
     */
    public List<String> localVersions(ArtifactCoordinates coordinates) {
        return localVersionResolver.localVersions(coordinates);
    }

    /**
     * Lists the versions of {@code coordinates} published under {@code repositoryBaseUrl},
     * with no authentication.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact to look up
     * @return the published versions
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    public List<String> remoteVersions(String repositoryBaseUrl, ArtifactCoordinates coordinates)
            throws IOException, InterruptedException {
        return remoteVersionResolver.remoteVersions(repositoryBaseUrl, coordinates);
    }

    /**
     * Lists the versions of {@code coordinates} published under {@code repositoryBaseUrl},
     * authenticating with {@code credentials}.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact to look up
     * @param credentials the credentials to authenticate with
     * @return the published versions
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    public List<String> remoteVersions(String repositoryBaseUrl, ArtifactCoordinates coordinates, Credentials credentials)
            throws IOException, InterruptedException {
        return remoteVersionResolver.remoteVersions(repositoryBaseUrl, coordinates, credentials);
    }

    /**
     * Resolves {@code coordinates} at {@code version} — from the local repository if
     * already present there, otherwise downloading it from {@code repositoryBaseUrl} into
     * the local repository — then stages the resulting jar into {@code pluginDirectory},
     * replacing any previously-staged jar(s) for the same artifactId.
     *
     * @param coordinates the artifact to resolve
     * @param version the version to resolve
     * @param repositoryBaseUrl the repository to download from if not already present
     *         locally; may be {@code null} only if the artifact is already present locally
     * @param pluginDirectory the directory to stage the resolved jar into
     * @return the path of the staged jar, inside {@code pluginDirectory}
     * @throws IOException if the download or staging fails
     * @throws InterruptedException if the download is interrupted
     * @throws NullPointerException if {@code coordinates}, {@code version}, or
     *         {@code pluginDirectory} is {@code null}
     */
    public Path resolveAndStage(ArtifactCoordinates coordinates, String version, String repositoryBaseUrl, Path pluginDirectory)
            throws IOException, InterruptedException {
        return resolveAndStage(coordinates, version, repositoryBaseUrl, Optional.empty(), pluginDirectory);
    }

    /**
     * As {@link #resolveAndStage(ArtifactCoordinates, String, String, Path)}, authenticating
     * a remote download (if needed) with {@code credentials}.
     *
     * @param coordinates the artifact to resolve
     * @param version the version to resolve
     * @param repositoryBaseUrl the repository to download from if not already present locally
     * @param credentials the credentials to authenticate the download with
     * @param pluginDirectory the directory to stage the resolved jar into
     * @return the path of the staged jar, inside {@code pluginDirectory}
     * @throws IOException if the download or staging fails
     * @throws InterruptedException if the download is interrupted
     * @throws NullPointerException if any argument other than {@code repositoryBaseUrl} is {@code null}
     */
    public Path resolveAndStage(ArtifactCoordinates coordinates, String version, String repositoryBaseUrl,
                                 Credentials credentials, Path pluginDirectory) throws IOException, InterruptedException {
        Objects.requireNonNull(credentials, "credentials");
        return resolveAndStage(coordinates, version, repositoryBaseUrl, Optional.of(credentials), pluginDirectory);
    }

    private Path resolveAndStage(ArtifactCoordinates coordinates, String version, String repositoryBaseUrl,
                                  Optional<Credentials> credentials, Path pluginDirectory) throws IOException, InterruptedException {
        Objects.requireNonNull(coordinates, "coordinates");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(pluginDirectory, "pluginDirectory");

        Path localJar = localVersionResolver.artifactDirectory(coordinates)
                .resolve(version)
                .resolve(coordinates.artifactId() + "-" + version + ".jar");

        if (!Files.exists(localJar)) {
            Objects.requireNonNull(repositoryBaseUrl,
                    "repositoryBaseUrl (required when " + coordinates + ":" + version + " is not already present locally)");
            downloadToLocalRepository(coordinates, version, repositoryBaseUrl, credentials, localJar);
        }

        removePreviouslyStagedVersions(pluginDirectory, coordinates.artifactId());
        Files.createDirectories(pluginDirectory);
        Path staged = pluginDirectory.resolve(localJar.getFileName());
        Files.copy(localJar, staged, StandardCopyOption.REPLACE_EXISTING);
        LOG.info("Staged {} {} into {}", coordinates, version, staged);
        return staged;
    }

    private void downloadToLocalRepository(ArtifactCoordinates coordinates, String version, String repositoryBaseUrl,
                                            Optional<Credentials> credentials, Path destination) throws IOException, InterruptedException {
        String base = repositoryBaseUrl.endsWith("/") ? repositoryBaseUrl : repositoryBaseUrl + "/";
        String jarFileName = coordinates.artifactId() + "-" + version + ".jar";
        URI jarUri = URI.create(base + coordinates.groupPath() + "/" + coordinates.artifactId() + "/" + version + "/" + jarFileName);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(jarUri).timeout(REQUEST_TIMEOUT).GET();
        credentials.ifPresent(creds -> requestBuilder.header("Authorization", basicAuthHeader(creds)));

        Files.createDirectories(destination.getParent());
        HttpResponse<Path> response = httpClient.send(requestBuilder.build(),
                HttpResponse.BodyHandlers.ofFile(destination));
        if (response.statusCode() != 200) {
            Files.deleteIfExists(destination);
            throw new IOException("Unexpected HTTP " + response.statusCode() + " downloading " + jarUri);
        }
        LOG.info("Downloaded {} {} from {} into local repository", coordinates, version, jarUri);
    }

    private static String basicAuthHeader(Credentials credentials) {
        String usernameAndPassword = credentials.username() + ":" + new String(credentials.password());
        return "Basic " + Base64.getEncoder().encodeToString(usernameAndPassword.getBytes(StandardCharsets.UTF_8));
    }

    private static void removePreviouslyStagedVersions(Path pluginDirectory, String artifactId) throws IOException {
        if (!Files.isDirectory(pluginDirectory)) {
            return;
        }
        String prefix = artifactId + "-";
        try (Stream<Path> children = Files.list(pluginDirectory)) {
            for (Path child : children.filter(path -> path.getFileName().toString().startsWith(prefix)).toList()) {
                Files.delete(child);
                LOG.info("Removed previously-staged {}", child);
            }
        }
    }
}
