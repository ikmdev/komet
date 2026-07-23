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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Free-text search against a Nexus 3 repository's
 * <a href="https://help.sonatype.com/en/rest-and-integration-api.html">REST Search API</a>
 * ({@code /service/rest/v1/search}), returning the distinct groupId:artifactId pairs among the
 * matches — for browsing what's available rather than requiring a user to already know exact
 * coordinates. Nexus-specific: repositories that aren't served by Nexus (e.g. Maven Central)
 * have no equivalent endpoint, so {@link #searchUri(String, String)} returns empty for any URL
 * that doesn't look like a Nexus repository URL, and callers should treat that — and any
 * request failure — as "search unavailable here", not a hard error.
 */
public final class NexusSearchClient {

    private static final String REPOSITORY_SEGMENT = "/repository/";

    private NexusSearchClient() {
    }

    /**
     * Derives the Nexus search URI for {@code query} against the repository identified by
     * {@code repositoryBaseUrl}, e.g. {@code https://nexus.tinkar.org/repository/ike-public/}
     * plus query {@code starter-data} becomes {@code https://nexus.tinkar.org/service/rest/v1/
     * search?repository=ike-public&q=starter-data*}.
     *
     * <p>A {@code groupId:artifactPattern} query (colon-separated) is routed through Nexus's
     * Maven-format-specific {@code maven.groupId}/{@code maven.artifactId} search fields
     * instead of the free-text {@code q} field, so it matches precisely rather than depending
     * on the free-text analyzer tokenizing a colon-joined string usefully:
     * <ul>
     * <li>{@code "network.ike:"} (blank artifact part) — every artifact under that exact
     *     group.</li>
     * <li>{@code "network.ike:ike-starter"} — artifacts under that group whose id starts with
     *     {@code ike-starter} (a {@code *} is appended automatically for a partial/prefix
     *     match unless the pattern already contains one).</li>
     * <li>{@code "network.ike:*starter*"} — an explicit wildcard pattern is used verbatim.</li>
     * </ul>
     * A query with no colon is used against the free-text {@code q} field, matching across
     * group, name, and other indexed fields — with the same trailing-{@code *} treatment as
     * above, since Nexus appears to match {@code q} on whole tokens by default (a search for
     * {@code "snomed"} alone won't find an artifact named {@code tinkar-snomedct-starter-data}
     * without one).
     *
     * @param repositoryBaseUrl the configured repository URL
     * @param query the search term — free text, or {@code groupId:artifactPattern}
     * @return the search URI, or empty if {@code repositoryBaseUrl} doesn't contain a
     *         {@code /repository/<name>/} segment (not a Nexus repository URL) or either
     *         argument is blank
     */
    public static Optional<URI> searchUri(String repositoryBaseUrl, String query) {
        return searchUri(repositoryBaseUrl, query, null);
    }

    /**
     * As {@link #searchUri(String, String)}, additionally scoped to Nexus's Maven-format
     * {@code maven.classifier} search field — confirmed real and effective against a live
     * repository, 2026-07-22 (e.g. {@code q=rxnorm&maven.classifier=reasoned-sa} excludes a
     * matching artifact that publishes no {@code reasoned-sa} variant at all). This is what lets
     * {@link #search(HttpClient, String, String, List)} filter out artifacts with no compatible
     * classifier before they're ever shown as a search result, rather than only discovering that
     * once a result is picked and its versions are listed.
     *
     * @param repositoryBaseUrl the configured repository URL
     * @param query the search term — free text, or {@code groupId:artifactPattern}
     * @param classifier the classifier to require, or {@code null} for none
     * @return the search URI, or empty under the same conditions as {@link #searchUri(String, String)}
     */
    public static Optional<URI> searchUri(String repositoryBaseUrl, String query, String classifier) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        return location(repositoryBaseUrl).map(location -> {
            String queryParams = "repository=" + location.encodedRepositoryName() + "&" + searchFieldParams(query.strip());
            if (classifier != null && !classifier.isBlank()) {
                queryParams += "&maven.classifier=" + URLEncoder.encode(classifier, StandardCharsets.UTF_8);
            }
            return URI.create(location.nexusBaseUrl() + "/service/rest/v1/search?" + queryParams);
        });
    }

    /**
     * Derives the Nexus REST Search API URI for an exact groupId:artifactId:version lookup —
     * used to discover the real, currently-published classifiers for one specific artifact
     * version ({@link #componentAssets}), rather than free-text/wildcard browsing.
     *
     * @param repositoryBaseUrl the configured repository URL
     * @param coordinates the exact groupId:artifactId
     * @param version the exact version
     * @return the search URI, or empty if {@code repositoryBaseUrl} isn't a Nexus repository URL
     */
    public static Optional<URI> componentSearchUri(String repositoryBaseUrl, ArtifactCoordinates coordinates, String version) {
        Objects.requireNonNull(coordinates, "coordinates");
        Objects.requireNonNull(version, "version");
        return location(repositoryBaseUrl).map(location -> {
            String queryParams = "repository=" + location.encodedRepositoryName()
                    + "&group=" + URLEncoder.encode(coordinates.groupId(), StandardCharsets.UTF_8)
                    + "&name=" + URLEncoder.encode(coordinates.artifactId(), StandardCharsets.UTF_8)
                    + "&version=" + URLEncoder.encode(version, StandardCharsets.UTF_8);
            return URI.create(location.nexusBaseUrl() + "/service/rest/v1/search?" + queryParams);
        });
    }

    private record NexusLocation(String nexusBaseUrl, String encodedRepositoryName) {
    }

    private static Optional<NexusLocation> location(String repositoryBaseUrl) {
        if (repositoryBaseUrl == null || repositoryBaseUrl.isBlank()) {
            return Optional.empty();
        }
        String normalized = repositoryBaseUrl.endsWith("/")
                ? repositoryBaseUrl.substring(0, repositoryBaseUrl.length() - 1) : repositoryBaseUrl;
        int repositorySegmentIndex = normalized.indexOf(REPOSITORY_SEGMENT);
        if (repositorySegmentIndex < 0) {
            return Optional.empty();
        }
        String nexusBaseUrl = normalized.substring(0, repositorySegmentIndex);
        String afterRepositorySegment = normalized.substring(repositorySegmentIndex + REPOSITORY_SEGMENT.length());
        int nextSlash = afterRepositorySegment.indexOf('/');
        String repositoryName = nextSlash >= 0 ? afterRepositorySegment.substring(0, nextSlash) : afterRepositorySegment;
        if (repositoryName.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new NexusLocation(nexusBaseUrl, URLEncoder.encode(repositoryName, StandardCharsets.UTF_8)));
    }

    private static String searchFieldParams(String query) {
        int colonIndex = query.indexOf(':');
        if (colonIndex < 0) {
            return "q=" + URLEncoder.encode(wildcardPattern(query), StandardCharsets.UTF_8);
        }
        String groupId = query.substring(0, colonIndex).strip();
        if (groupId.isBlank()) {
            return "q=" + URLEncoder.encode(wildcardPattern(query), StandardCharsets.UTF_8);
        }
        String artifactIdPattern = query.substring(colonIndex + 1).strip();
        String params = "maven.groupId=" + URLEncoder.encode(groupId, StandardCharsets.UTF_8);
        if (!artifactIdPattern.isBlank()) {
            params += "&maven.artifactId=" + URLEncoder.encode(wildcardPattern(artifactIdPattern), StandardCharsets.UTF_8);
        }
        return params;
    }

    /**
     * Appends a trailing {@code *} for a partial/prefix match, unless {@code pattern} already
     * contains a wildcard — Nexus's search fields (both the free-text {@code q} field and the
     * Maven-format-specific {@code maven.groupId}/{@code maven.artifactId} fields) appear to
     * match on whole tokens by default, so a search for {@code "snomed"} alone won't find an
     * artifact named {@code tinkar-snomedct-starter-data} without an explicit wildcard.
     */
    private static String wildcardPattern(String pattern) {
        return pattern.contains("*") ? pattern : pattern + "*";
    }

    /**
     * Searches for {@code query} against the repository identified by {@code repositoryBaseUrl}.
     *
     * @param httpClient the HTTP client to search with
     * @param repositoryBaseUrl the configured repository URL
     * @param query the free-text search term
     * @return the distinct matching groupId:artifactId pairs, in the order Nexus returned them
     * @throws IOException if {@code repositoryBaseUrl} isn't a Nexus repository URL, the
     *         request fails, or the response isn't the expected shape
     * @throws InterruptedException if the request is interrupted
     */
    public static List<ArtifactCoordinates> search(HttpClient httpClient, String repositoryBaseUrl, String query)
            throws IOException, InterruptedException {
        return search(httpClient, repositoryBaseUrl, query, (Credentials) null);
    }

    /**
     * As {@link #search(HttpClient, String, String)}, authenticating with {@code credentials}.
     *
     * @param httpClient the HTTP client to search with
     * @param repositoryBaseUrl the configured repository URL
     * @param query the free-text search term
     * @param credentials the credentials to authenticate with, or {@code null} for none
     * @return the distinct matching groupId:artifactId pairs, in the order Nexus returned them
     * @throws IOException if {@code repositoryBaseUrl} isn't a Nexus repository URL, the
     *         request fails, or the response isn't the expected shape
     * @throws InterruptedException if the request is interrupted
     */
    public static List<ArtifactCoordinates> search(HttpClient httpClient, String repositoryBaseUrl, String query,
                                                     Credentials credentials) throws IOException, InterruptedException {
        URI uri = searchUri(repositoryBaseUrl, query)
                .orElseThrow(() -> new IOException("Not a Nexus repository URL (no /repository/<name>/ segment): " + repositoryBaseUrl));
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).GET();
        if (credentials != null) {
            requestBuilder.header("Authorization", BasicAuth.header(credentials));
        }
        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Unexpected HTTP " + response.statusCode() + " searching " + uri);
        }
        return parseItems(response.body());
    }

    /**
     * As {@link #search(HttpClient, String, String)}, but excluding any artifact that doesn't
     * publish at least one of {@code classifierCandidates} on <em>some</em> version — per
     * IKE-Network/ike-issues#882, an artifact with no variant compatible with the flow the user
     * is browsing for must not be presented as an option at all, not merely left undownloadable
     * once picked. Implemented as one {@code maven.classifier}-scoped search per candidate (see
     * {@link #searchUri(String, String, String)}), run concurrently and unioned — a small,
     * <em>fixed</em> number of extra requests (one per {@link ProviderArtifactQualifier
     * #classifierCandidates candidate}, typically 2–4), not one per result, and not per keystroke
     * beyond that fixed cost.
     *
     * @param httpClient the HTTP client to search with
     * @param repositoryBaseUrl the configured repository URL
     * @param query the free-text search term
     * @param classifierCandidates the classifiers an artifact must publish at least one of to
     *         be included, in priority order (only the union of matches matters here — order
     *         doesn't affect which artifacts appear, only {@code ProviderArtifactQualifier
     *         .pickBestClassifier} cares about it later)
     * @return the distinct matching groupId:artifactId pairs that publish a compatible classifier
     * @throws IOException if {@code repositoryBaseUrl} isn't a Nexus repository URL, any request
     *         fails, or a response isn't the expected shape
     * @throws InterruptedException if a request is interrupted
     */
    public static List<ArtifactCoordinates> search(HttpClient httpClient, String repositoryBaseUrl, String query,
                                                     List<String> classifierCandidates) throws IOException, InterruptedException {
        return search(httpClient, repositoryBaseUrl, query, classifierCandidates, null);
    }

    /**
     * As {@link #search(HttpClient, String, String, List)}, authenticating with {@code credentials}.
     *
     * @param httpClient the HTTP client to search with
     * @param repositoryBaseUrl the configured repository URL
     * @param query the free-text search term
     * @param classifierCandidates the classifiers an artifact must publish at least one of
     * @param credentials the credentials to authenticate with, or {@code null} for none
     * @return the distinct matching groupId:artifactId pairs that publish a compatible classifier
     * @throws IOException if {@code repositoryBaseUrl} isn't a Nexus repository URL, any request
     *         fails, or a response isn't the expected shape
     * @throws InterruptedException if a request is interrupted
     */
    public static List<ArtifactCoordinates> search(HttpClient httpClient, String repositoryBaseUrl, String query,
                                                     List<String> classifierCandidates, Credentials credentials)
            throws IOException, InterruptedException {
        return searchWithCompatibleVersions(httpClient, repositoryBaseUrl, query, classifierCandidates, credentials)
                .stream().map(SearchMatch::coordinates).toList();
    }

    /**
     * A search match together with one specific version of that artifact — confirmed, via the
     * very request that surfaced this match, to actually publish a compatible classifier. Exists
     * because {@link #search(HttpClient, String, String, List, Credentials)} alone throws that
     * version away, leaving nothing to stop a caller from defaulting to "most recent" once the
     * match is picked — and the most recent version is not guaranteed to be the one (or one of
     * the ones) that made this artifact match in the first place. Per IKE-Network/ike-issues#882
     * ("no indication ... does it just get overwritten" raised the same theme for downloads):
     * presenting an artifact as a search result only for its auto-selected version to turn out
     * incompatible is exactly the kind of bait-and-switch the classifier-scoped search was meant
     * to prevent — it prevents it for <em>some</em> version existing, but not for which version a
     * caller then actually offers.
     *
     * <p>{@code compatibleVersion} is the <em>asset's own resolved version</em>, not the
     * component version — for a {@code -SNAPSHOT} they differ, and the difference is the whole
     * point. Nexus's classifier-scoped search returns a component ({@code 1.0.0-SNAPSHOT}) whose
     * matching asset lives at a specific timestamped build ({@code 1.0.0-20260615.120000-2}); the
     * component version tells you nothing about which build, and a repository's
     * {@code maven-metadata.xml} can point at an entirely different build that never published the
     * classifier at all (observed live for {@code dev.ikm.tinkar.data:SOLOR}, 2026-07-22). Taking
     * the matching asset's version directly is the only reliable way to probe the exact build the
     * search actually proved compatible.
     *
     * @param coordinates the matching groupId:artifactId
     * @param compatibleVersion the resolved version of the specific asset that carried a searched
     *         classifier — the first one encountered, if more than one matched; not necessarily
     *         the most recent
     */
    public record SearchMatch(ArtifactCoordinates coordinates, String compatibleVersion) {
        public SearchMatch {
            Objects.requireNonNull(coordinates, "coordinates");
            Objects.requireNonNull(compatibleVersion, "compatibleVersion");
        }
    }

    /**
     * As {@link #search(HttpClient, String, String, List, Credentials)}, but keeping — for each
     * matching artifact — one specific version confirmed compatible, instead of discarding it.
     * Requests, unioning, and ordering are otherwise identical; the only difference is retaining
     * the {@code version} field {@link #search} throws away.
     *
     * @param httpClient the HTTP client to search with
     * @param repositoryBaseUrl the configured repository URL
     * @param query the free-text search term
     * @param classifierCandidates the classifiers an artifact must publish at least one of
     * @param credentials the credentials to authenticate with, or {@code null} for none
     * @return the distinct matching groupId:artifactId pairs, each with one confirmed-compatible
     *         version, in the order first encountered across the unioned responses
     * @throws IOException if {@code repositoryBaseUrl} isn't a Nexus repository URL, any request
     *         fails, or a response isn't the expected shape
     * @throws InterruptedException if a request is interrupted
     */
    public static List<SearchMatch> searchWithCompatibleVersions(HttpClient httpClient, String repositoryBaseUrl, String query,
                                                                   List<String> classifierCandidates, Credentials credentials)
            throws IOException, InterruptedException {
        Objects.requireNonNull(classifierCandidates, "classifierCandidates");
        // Each request is scoped to exactly one classifier — kept paired with it, because the
        // authoritative "which build has this classifier" answer is the matching asset's own
        // maven2.version in that response, and knowing which classifier we asked for is what lets
        // us pick the right asset out of an item that may carry several.
        List<String> scopedClassifiers = new ArrayList<>();
        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
        for (String classifier : classifierCandidates) {
            Optional<URI> uri = searchUri(repositoryBaseUrl, query, classifier);
            if (uri.isEmpty()) {
                continue;
            }
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri.get()).GET();
            if (credentials != null) {
                requestBuilder.header("Authorization", BasicAuth.header(credentials));
            }
            scopedClassifiers.add(classifier);
            futures.add(httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()));
        }
        if (futures.isEmpty()) {
            throw new IOException("Not a Nexus repository URL (no /repository/<name>/ segment): " + repositoryBaseUrl);
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException io) {
                throw io;
            }
            throw new IOException("A classifier-scoped search request failed", e.getCause());
        }
        Map<ArtifactCoordinates, String> compatibleVersionByCoordinates = new LinkedHashMap<>();
        for (int i = 0; i < futures.size(); i++) {
            HttpResponse<String> response = futures.get(i).join();
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected HTTP " + response.statusCode() + " searching " + response.uri());
            }
            for (ClassifiedAsset asset : parseClassifiedAssets(response.body(), scopedClassifiers.get(i))) {
                asset.asset().resolvedVersion().ifPresent(resolved ->
                        compatibleVersionByCoordinates.putIfAbsent(asset.coordinates(), resolved));
            }
        }
        return compatibleVersionByCoordinates.entrySet().stream()
                .map(entry -> new SearchMatch(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * One asset's known size and SHA-256 checksum, as reported directly by Nexus's search
     * response (a {@code checksum.sha256} field present on every asset, confirmed against a
     * live repository, 2026-07-21) — no separate {@code HEAD} request or {@code .sha256} file
     * fetch needed just to get either.
     *
     * @param size the file size in bytes, if Nexus reported one
     * @param sha256 the expected SHA-256 checksum (lowercase hex), if Nexus reported one
     * @param resolvedVersion the asset's own {@code maven2.version}, if reported — for a snapshot
     *         this is the concrete timestamped build (e.g. {@code "1.0.0-20260615.120000-2"}) the
     *         asset actually lives at, distinct from the {@code -SNAPSHOT} component version it was
     *         looked up under, and exactly what a download URL's filename must embed. Empty for a
     *         local-directory lookup (no Nexus response to read it from) or a response that omits it
     */
    public record AssetInfo(OptionalLong size, Optional<String> sha256, Optional<String> resolvedVersion) {
        public AssetInfo {
            Objects.requireNonNull(size, "size");
            Objects.requireNonNull(sha256, "sha256");
            Objects.requireNonNull(resolvedVersion, "resolvedVersion");
        }
    }

    /**
     * The real, currently-published assets for one exact groupId:artifactId:version: every
     * classified {@code .zip} (the authoritative source of truth for "does a compatible variant
     * of this artifact exist", as opposed to guessing from a fixed candidate list and probing
     * each one), plus the artifact's own main POM — a component search response includes every
     * asset under that exact version, so a caller that needs classifiers, sizes, checksums,
     * <em>and</em> the POM only ever needs the one search.
     *
     * @param classifiers each published classifier's {@code .zip} asset info
     * @param pom the artifact's main POM asset info — the {@code .pom} with no classifier,
     *         distinct from any classifier-qualified {@code .pom} a build might also publish
     *         (e.g. one with classifier {@code "build"}) — empty if not published
     */
    public record ComponentAssets(Map<String, AssetInfo> classifiers, Optional<AssetInfo> pom) {
        public ComponentAssets {
            Objects.requireNonNull(classifiers, "classifiers");
            Objects.requireNonNull(pom, "pom");
        }
    }

    /**
     * Looks up every published asset for one exact groupId:artifactId:version.
     *
     * @param httpClient the HTTP client to search with
     * @param repositoryBaseUrl the configured repository URL
     * @param coordinates the exact groupId:artifactId
     * @param version the exact version
     * @return the component's assets; both empty if the component doesn't exist
     * @throws IOException if {@code repositoryBaseUrl} isn't a Nexus repository URL, the
     *         request fails, or the response isn't the expected shape
     * @throws InterruptedException if the request is interrupted
     */
    public static ComponentAssets componentAssets(HttpClient httpClient, String repositoryBaseUrl,
                                                    ArtifactCoordinates coordinates, String version) throws IOException, InterruptedException {
        return componentAssets(httpClient, repositoryBaseUrl, coordinates, version, null);
    }

    /**
     * As {@link #componentAssets(HttpClient, String, ArtifactCoordinates, String)}, authenticating
     * with {@code credentials}.
     *
     * @param httpClient the HTTP client to search with
     * @param repositoryBaseUrl the configured repository URL
     * @param coordinates the exact groupId:artifactId
     * @param version the exact version
     * @param credentials the credentials to authenticate with, or {@code null} for none
     * @return the component's assets
     * @throws IOException if {@code repositoryBaseUrl} isn't a Nexus repository URL, the
     *         request fails, or the response isn't the expected shape
     * @throws InterruptedException if the request is interrupted
     */
    public static ComponentAssets componentAssets(HttpClient httpClient, String repositoryBaseUrl,
                                                    ArtifactCoordinates coordinates, String version, Credentials credentials)
            throws IOException, InterruptedException {
        URI uri = componentSearchUri(repositoryBaseUrl, coordinates, version)
                .orElseThrow(() -> new IOException("Not a Nexus repository URL (no /repository/<name>/ segment): " + repositoryBaseUrl));
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).GET();
        if (credentials != null) {
            requestBuilder.header("Authorization", BasicAuth.header(credentials));
        }
        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Unexpected HTTP " + response.statusCode() + " searching " + uri);
        }
        return parseComponentAssets(response.body());
    }

    /**
     * Every published version of one exact groupId:artifactId that carries at least one of
     * {@code classifierCandidates} as a {@code .zip} — i.e. every version that actually has a
     * downloadable variant for the flow being browsed, newest last. This is what the version
     * picker should offer: a version with no compatible classified zip (SOLOR's POM-only
     * {@code 1.0.0-SNAPSHOT}, a version whose only variant is a {@code .xml}, etc.) must never be
     * presented, since selecting it can only dead-end (per IKE-Network/ike-issues#882: don't
     * offer what can't be used).
     *
     * <p>Derived from the live asset index via the {@code maven.classifier} filter (one request
     * per candidate classifier, run concurrently) — <em>not</em> from {@code maven-metadata.xml},
     * whose version list can be incomplete or list versions that publish no usable asset. Ordered
     * by each version's newest matching asset's own resolved version, ascending, so the most
     * recent sorts last (matching the picker's "select the last" default). Distinct.
     *
     * @param httpClient the HTTP client to search with
     * @param repositoryBaseUrl the configured repository URL
     * @param coordinates the exact groupId:artifactId
     * @param classifierCandidates the classifiers a version must publish at least one of
     * @param credentials the credentials to authenticate with, or {@code null} for none
     * @return the distinct compatible versions, newest last; empty if none
     * @throws IOException if {@code repositoryBaseUrl} isn't a Nexus repository URL, any request
     *         fails, or a response isn't the expected shape
     * @throws InterruptedException if a request is interrupted
     */
    public static List<String> compatibleVersions(HttpClient httpClient, String repositoryBaseUrl,
                                                    ArtifactCoordinates coordinates, List<String> classifierCandidates,
                                                    Credentials credentials) throws IOException, InterruptedException {
        Objects.requireNonNull(coordinates, "coordinates");
        Objects.requireNonNull(classifierCandidates, "classifierCandidates");
        List<String> scopedClassifiers = new ArrayList<>();
        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
        for (String classifier : classifierCandidates) {
            Optional<URI> uri = classifierAssetsUri(repositoryBaseUrl, coordinates, classifier);
            if (uri.isEmpty()) {
                continue;
            }
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri.get()).GET();
            if (credentials != null) {
                requestBuilder.header("Authorization", BasicAuth.header(credentials));
            }
            scopedClassifiers.add(classifier);
            futures.add(httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()));
        }
        if (futures.isEmpty()) {
            throw new IOException("Not a Nexus repository URL (no /repository/<name>/ segment): " + repositoryBaseUrl);
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException io) {
                throw io;
            }
            throw new IOException("A classifier-scoped asset search request failed", e.getCause());
        }
        java.util.TreeSet<String> versions = new java.util.TreeSet<>();
        for (int i = 0; i < futures.size(); i++) {
            HttpResponse<String> response = futures.get(i).join();
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected HTTP " + response.statusCode() + " searching " + response.uri());
            }
            for (ClassifiedAsset asset : parseClassifiedAssets(response.body(), scopedClassifiers.get(i))) {
                if (asset.coordinates().equals(coordinates)) {
                    asset.asset().resolvedVersion().ifPresent(versions::add);
                }
            }
        }
        return List.copyOf(versions);
    }

    /**
     * The URI listing every {@code .zip} asset carrying {@code classifier} for one exact
     * groupId:artifactId — {@code group=…&name=…&maven.classifier=…&maven.extension=zip}, no
     * version constraint, so it spans every published version. This is the SAME {@code maven.classifier}
     * asset filter a browse search uses (confirmed to match assets against the live repository),
     * as opposed to a {@code version=}-scoped component search — which for a snapshot returns no
     * classified zips at all (SOLOR, 2026-07-22).
     */
    private static Optional<URI> classifierAssetsUri(String repositoryBaseUrl, ArtifactCoordinates coordinates, String classifier) {
        return location(repositoryBaseUrl).map(location -> URI.create(location.nexusBaseUrl()
                + "/service/rest/v1/search?repository=" + location.encodedRepositoryName()
                + "&group=" + URLEncoder.encode(coordinates.groupId(), StandardCharsets.UTF_8)
                + "&name=" + URLEncoder.encode(coordinates.artifactId(), StandardCharsets.UTF_8)
                + "&maven.extension=zip"
                + "&maven.classifier=" + URLEncoder.encode(classifier, StandardCharsets.UTF_8)));
    }

    private static ComponentAssets parseComponentAssets(String responseBody) throws IOException {
        Object parsed = MinimalJsonParser.parse(responseBody);
        if (!(parsed instanceof Map<?, ?> root)) {
            throw new IOException("Unexpected Nexus search response shape: expected a JSON object");
        }
        Object items = root.get("items");
        if (!(items instanceof List<?> itemList)) {
            throw new IOException("Unexpected Nexus search response shape: expected an \"items\" array");
        }
        Map<String, AssetInfo> classifiers = new LinkedHashMap<>();
        AssetInfo pom = null;
        for (Object item : itemList) {
            if (!(item instanceof Map<?, ?> itemMap) || !(itemMap.get("assets") instanceof List<?> assets)) {
                continue;
            }
            for (Object asset : assets) {
                if (!(asset instanceof Map<?, ?> assetMap) || !(assetMap.get("maven2") instanceof Map<?, ?> maven2)) {
                    continue;
                }
                String extension = maven2.get("extension") instanceof String value ? value : null;
                String classifier = maven2.get("classifier") instanceof String value && !value.isBlank() ? value : null;
                if ("zip".equals(extension) && classifier != null) {
                    // A -SNAPSHOT component accumulates every timestamped build's assets under the
                    // one component version, so the same classifier can appear more than once (one
                    // per build). Keep the newest — a timestamped build version sorts
                    // lexicographically in chronological order for a fixed base version, so a plain
                    // string comparison picks the most recent deploy.
                    classifiers.merge(classifier, assetInfo(assetMap), NexusSearchClient::newerAsset);
                } else if ("pom".equals(extension) && classifier == null) {
                    pom = newerAsset(pom, assetInfo(assetMap));
                }
            }
        }
        return new ComponentAssets(Map.copyOf(classifiers), Optional.ofNullable(pom));
    }

    /** The asset with the greater {@code resolvedVersion} — the newest timestamped build, when a classifier repeats. */
    private static AssetInfo newerAsset(AssetInfo existing, AssetInfo candidate) {
        if (existing == null) {
            return candidate;
        }
        String existingVersion = existing.resolvedVersion().orElse("");
        String candidateVersion = candidate.resolvedVersion().orElse("");
        return candidateVersion.compareTo(existingVersion) >= 0 ? candidate : existing;
    }

    private static AssetInfo assetInfo(Map<?, ?> assetMap) {
        OptionalLong size = assetMap.get("fileSize") instanceof Number fileSize
                ? OptionalLong.of(fileSize.longValue()) : OptionalLong.empty();
        Optional<String> sha256 = assetMap.get("checksum") instanceof Map<?, ?> checksum
                && checksum.get("sha256") instanceof String sha256Value && !sha256Value.isBlank()
                ? Optional.of(sha256Value) : Optional.empty();
        Optional<String> resolvedVersion = assetMap.get("maven2") instanceof Map<?, ?> maven2
                && maven2.get("version") instanceof String versionValue && !versionValue.isBlank()
                ? Optional.of(versionValue) : Optional.empty();
        return new AssetInfo(size, sha256, resolvedVersion);
    }

    private static List<ArtifactCoordinates> parseItems(String responseBody) throws IOException {
        Object parsed = MinimalJsonParser.parse(responseBody);
        if (!(parsed instanceof Map<?, ?> root)) {
            throw new IOException("Unexpected Nexus search response shape: expected a JSON object");
        }
        Object items = root.get("items");
        if (!(items instanceof List<?> itemList)) {
            throw new IOException("Unexpected Nexus search response shape: expected an \"items\" array");
        }
        Set<ArtifactCoordinates> results = new LinkedHashSet<>();
        for (Object item : itemList) {
            if (item instanceof Map<?, ?> itemMap
                    && itemMap.get("group") instanceof String groupId && itemMap.get("name") instanceof String artifactId
                    && !groupId.isBlank() && !artifactId.isBlank()) {
                results.add(new ArtifactCoordinates(groupId, artifactId));
            }
        }
        return List.copyOf(results);
    }

    /**
     * One matching {@code .zip} asset that carries a searched classifier — its groupId:artifactId
     * and its full {@link AssetInfo} (size, checksum, and its own {@code maven2.version}, which for
     * a snapshot is a concrete timestamped build, not the component's {@code -SNAPSHOT} version).
     */
    private record ClassifiedAsset(ArtifactCoordinates coordinates, AssetInfo asset) {
    }

    /**
     * Every asset in a classifier-scoped search response that actually carries {@code classifier}
     * as a {@code .zip}, paired with its full {@link AssetInfo}. Nexus's {@code maven.classifier}
     * filter matches at the <em>component</em> level, so a returned item can include assets that
     * don't have the searched classifier — this looks inside each item's {@code assets} and keeps
     * only the ones that genuinely do.
     */
    private static List<ClassifiedAsset> parseClassifiedAssets(String responseBody, String classifier) throws IOException {
        Object parsed = MinimalJsonParser.parse(responseBody);
        if (!(parsed instanceof Map<?, ?> root)) {
            throw new IOException("Unexpected Nexus search response shape: expected a JSON object");
        }
        Object items = root.get("items");
        if (!(items instanceof List<?> itemList)) {
            throw new IOException("Unexpected Nexus search response shape: expected an \"items\" array");
        }
        List<ClassifiedAsset> results = new ArrayList<>();
        for (Object item : itemList) {
            if (!(item instanceof Map<?, ?> itemMap)
                    || !(itemMap.get("group") instanceof String groupId) || groupId.isBlank()
                    || !(itemMap.get("name") instanceof String artifactId) || artifactId.isBlank()
                    || !(itemMap.get("assets") instanceof List<?> assets)) {
                continue;
            }
            for (Object asset : assets) {
                if (!(asset instanceof Map<?, ?> assetMap) || !(assetMap.get("maven2") instanceof Map<?, ?> maven2)) {
                    continue;
                }
                boolean isZip = "zip".equals(maven2.get("extension") instanceof String value ? value : null);
                boolean matchesClassifier = classifier.equals(maven2.get("classifier") instanceof String value ? value : null);
                if (isZip && matchesClassifier) {
                    results.add(new ClassifiedAsset(new ArtifactCoordinates(groupId, artifactId), assetInfo(assetMap)));
                }
            }
        }
        return results;
    }

}
