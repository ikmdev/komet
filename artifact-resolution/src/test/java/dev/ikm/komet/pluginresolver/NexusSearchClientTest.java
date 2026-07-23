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

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NexusSearchClientTest {

    private static final String SEARCH_RESPONSE = """
            {
              "items": [
                {"group": "dev.ikm.komet", "name": "komet-claude-plugin", "version": "1.0.0"},
                {"group": "dev.ikm.komet", "name": "komet-claude-plugin", "version": "1.1.0"},
                {"group": "dev.ikm.tinkar.data", "name": "starter-data-reasoned", "version": "2024.09.01"},
                {"group": "", "name": "ignored-blank-group", "version": "1.0.0"},
                {"format": "maven2"}
              ],
              "continuationToken": null
            }
            """;

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void searchUriDerivesNexusBaseAndRepositoryName() {
        Optional<URI> uri = NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-public/", "starter");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-public&q=starter*", uri.get().toString());
    }

    @Test
    void searchUriAppendsWildcardToFreeTextQueryForPartialMatch() {
        // Nexus's free-text q field matches whole tokens by default — "snomed" alone would
        // not find an artifact named tinkar-snomedct-starter-data without a wildcard.
        Optional<URI> uri = NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-public/", "snomed");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-public&q=snomed*", uri.get().toString());
    }

    @Test
    void searchUriHonorsExplicitWildcardInFreeTextQuery() {
        Optional<URI> uri = NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-public/", "*snomed*");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-public&q=*snomed*", uri.get().toString());
    }

    @Test
    void searchUriIsEmptyForNonNexusUrl() {
        assertTrue(NexusSearchClient.searchUri("https://repo.maven.apache.org/maven2/", "starter").isEmpty());
        assertTrue(NexusSearchClient.searchUri(null, "starter").isEmpty());
        assertTrue(NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-public/", "  ").isEmpty());
    }

    @Test
    void searchUriWithBlankArtifactPartMatchesEntireGroup() {
        Optional<URI> uri = NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-public/", "network.ike:");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-public&maven.groupId=network.ike",
                uri.get().toString());
    }

    @Test
    void searchUriWithArtifactPartAppendsWildcardForPartialMatch() {
        Optional<URI> uri = NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-public/", "network.ike:ike-starter");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-public"
                + "&maven.groupId=network.ike&maven.artifactId=ike-starter*", uri.get().toString());
    }

    @Test
    void searchUriHonorsExplicitWildcardInArtifactPart() {
        Optional<URI> uri = NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-public/", "network.ike:*starter*");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-public"
                + "&maven.groupId=network.ike&maven.artifactId=*starter*", uri.get().toString());
    }

    @Test
    void searchUriFallsBackToFreeTextWhenGroupPartIsBlank() {
        Optional<URI> uri = NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-public/", ":starter");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-public&q=%3Astarter*", uri.get().toString());
    }

    @Test
    void searchReturnsDistinctCoordinatesIgnoringMalformedItems() throws Exception {
        server.createContext("/service/rest/v1/search", exchange -> respond(exchange, SEARCH_RESPONSE));

        List<ArtifactCoordinates> results = NexusSearchClient.search(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-public/", "komet");

        assertEquals(List.of(
                new ArtifactCoordinates("dev.ikm.komet", "komet-claude-plugin"),
                new ArtifactCoordinates("dev.ikm.tinkar.data", "starter-data-reasoned")
        ), results);
    }

    @Test
    void searchSendsBasicAuthorizationWhenCredentialsProvided() throws Exception {
        AtomicReference<String> receivedAuthHeader = new AtomicReference<>();
        server.createContext("/service/rest/v1/search", exchange -> {
            receivedAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, SEARCH_RESPONSE);
        });
        Credentials credentials = new Credentials("build-user", "s3cret".toCharArray());
        String expectedHeader = "Basic " + Base64.getEncoder().encodeToString("build-user:s3cret".getBytes(StandardCharsets.UTF_8));

        NexusSearchClient.search(HttpClient.newHttpClient(), baseUrl + "/repository/ike-public/", "komet", credentials);

        assertEquals(expectedHeader, receivedAuthHeader.get());
    }

    @Test
    void searchThrowsForNonNexusRepositoryUrl() {
        assertThrows(IOException.class, () -> NexusSearchClient.search(HttpClient.newHttpClient(),
                "https://repo.maven.apache.org/maven2/", "komet"));
    }

    @Test
    void searchThrowsOnUnexpectedStatus() throws Exception {
        server.createContext("/service/rest/v1/search", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });

        assertThrows(IOException.class, () -> NexusSearchClient.search(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-public/", "komet"));
    }

    @Test
    void searchThrowsOnUnexpectedResponseShape() throws Exception {
        server.createContext("/service/rest/v1/search", exchange -> respond(exchange, "{\"notItems\": []}"));

        assertThrows(IOException.class, () -> NexusSearchClient.search(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-public/", "komet"));
    }

    @Test
    void searchReturnsEmptyListWhenNoItemsMatch() throws Exception {
        server.createContext("/service/rest/v1/search", exchange -> respond(exchange, "{\"items\": [], \"continuationToken\": null}"));

        List<ArtifactCoordinates> results = NexusSearchClient.search(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-public/", "nonexistent");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithClassifierScopesToMavenClassifierField() {
        Optional<URI> uri = NexusSearchClient.searchUri("https://nexus.tinkar.org/repository/ike-restricted/", "rxnorm", "reasoned-sa");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-restricted&q=rxnorm*&maven.classifier=reasoned-sa",
                uri.get().toString());
    }

    @Test
    void searchWithClassifierCandidatesUnionsResultsAcrossCandidates() throws Exception {
        // Real behavior confirmed against a live repository, 2026-07-22: scoping the same free
        // -text query by different maven.classifier values returns different result sets.
        server.createContext("/service/rest/v1/search", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query.contains("maven.classifier=reasoned-sa")) {
                respond(exchange, """
                        {"items": [{"group": "dev.ikm.tinkar.data", "name": "rxnorm", "version": "1.0.0",
                          "assets": [{"maven2": {"classifier": "reasoned-sa", "extension": "zip", "version": "1.0.0"}}]}], "continuationToken": null}
                        """);
            } else if (query.contains("maven.classifier=unreasoned-sa")) {
                respond(exchange, """
                        {"items": [
                          {"group": "dev.ikm.tinkar.data", "name": "rxnorm", "version": "1.0.0",
                            "assets": [{"maven2": {"classifier": "unreasoned-sa", "extension": "zip", "version": "1.0.0"}}]},
                          {"group": "dev.ikm.other", "name": "unreasoned-only", "version": "1.0.0",
                            "assets": [{"maven2": {"classifier": "unreasoned-sa", "extension": "zip", "version": "1.0.0"}}]}
                        ], "continuationToken": null}
                        """);
            } else {
                respond(exchange, "{\"items\": [], \"continuationToken\": null}");
            }
        });

        List<ArtifactCoordinates> results = NexusSearchClient.search(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/", "rxnorm", List.of("reasoned-sa", "unreasoned-sa"));

        assertEquals(Set.of(new ArtifactCoordinates("dev.ikm.tinkar.data", "rxnorm"),
                new ArtifactCoordinates("dev.ikm.other", "unreasoned-only")), Set.copyOf(results));
    }

    @Test
    void searchWithCompatibleVersionsReportsTheMatchingAssetsOwnResolvedBuildNotTheComponentVersion() throws Exception {
        // The whole bait-and-switch fix: a classifier-scoped search returns a -SNAPSHOT component
        // (version "1.0.0-SNAPSHOT"), but the asset that actually carries the classifier lives at
        // a specific timestamped build. Reporting the component version would send a caller back
        // to metadata resolution that can land on a different build with no such asset (observed
        // live for SOLOR). The matching ASSET's maven2.version is the authoritative build to use.
        server.createContext("/service/rest/v1/search", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query.contains("maven.classifier=reasoned-sa")) {
                respond(exchange, """
                        {"items": [{"group": "dev.ikm.tinkar.data", "name": "SOLOR", "version": "1.0.0-SNAPSHOT",
                          "assets": [
                            {"maven2": {"classifier": "reasoned-sa", "extension": "zip", "version": "1.0.0-20260615.120000-2"}},
                            {"maven2": {"extension": "pom", "version": "1.0.0-20260615.120000-2"}}
                          ]}], "continuationToken": null}
                        """);
            } else {
                respond(exchange, "{\"items\": [], \"continuationToken\": null}");
            }
        });

        List<NexusSearchClient.SearchMatch> results = NexusSearchClient.searchWithCompatibleVersions(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/", "rxnorm", List.of("reasoned-sa", "unreasoned-sa"), null);

        assertEquals(List.of(new NexusSearchClient.SearchMatch(
                new ArtifactCoordinates("dev.ikm.tinkar.data", "SOLOR"), "1.0.0-20260615.120000-2")), results);
    }

    @Test
    void searchWithCompatibleVersionsIgnoresAssetsThatDoNotActuallyCarryTheScopedClassifier() throws Exception {
        // Nexus's maven.classifier filter matches at the component level, so a returned item can
        // include assets that don't have the searched classifier at all (here a bare POM). Only
        // the asset that genuinely carries reasoned-sa should drive the reported build.
        server.createContext("/service/rest/v1/search", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query.contains("maven.classifier=reasoned-sa")) {
                respond(exchange, """
                        {"items": [{"group": "dev.ikm.tinkar.data", "name": "rxnorm", "version": "1.0.0-SNAPSHOT",
                          "assets": [
                            {"maven2": {"extension": "pom", "version": "1.0.0-20260701.093000-5"}},
                            {"maven2": {"classifier": "reasoned-sa", "extension": "zip", "version": "1.0.0-20260615.120000-2"}}
                          ]}], "continuationToken": null}
                        """);
            } else {
                respond(exchange, "{\"items\": [], \"continuationToken\": null}");
            }
        });

        List<NexusSearchClient.SearchMatch> results = NexusSearchClient.searchWithCompatibleVersions(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/", "rxnorm", List.of("reasoned-sa", "unreasoned-sa"), null);

        assertEquals(1, results.size());
        assertEquals("1.0.0-20260615.120000-2", results.getFirst().compatibleVersion());
    }

    @Test
    void searchWithClassifierCandidatesExcludesArtifactsWithNoCompatibleClassifier() throws Exception {
        // Simulates a real observed case: an artifact (e.g. rxnorm-integration) whose name
        // matches the free-text query but which publishes no SA-compatible classifier at all —
        // every classifier-scoped search comes back empty for it, so the union is empty too.
        server.createContext("/service/rest/v1/search", exchange -> respond(exchange, "{\"items\": [], \"continuationToken\": null}"));

        List<ArtifactCoordinates> results = NexusSearchClient.search(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/", "rxnorm", List.of("reasoned-sa", "unreasoned-sa", "spined-array", "sa"));

        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithClassifierCandidatesSendsBasicAuthorizationForEveryRequest() throws Exception {
        java.util.Set<String> authHeaders = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        server.createContext("/service/rest/v1/search", exchange -> {
            authHeaders.add(exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, "{\"items\": [], \"continuationToken\": null}");
        });
        Credentials credentials = new Credentials("build-user", "s3cret".toCharArray());
        String expectedHeader = "Basic " + Base64.getEncoder().encodeToString("build-user:s3cret".getBytes(StandardCharsets.UTF_8));

        NexusSearchClient.search(HttpClient.newHttpClient(), baseUrl + "/repository/ike-restricted/", "rxnorm",
                List.of("reasoned-sa", "unreasoned-sa"), credentials);

        assertEquals(Set.of(expectedHeader), authHeaders);
    }

    @Test
    void searchWithClassifierCandidatesThrowsForNonNexusRepositoryUrl() {
        assertThrows(IOException.class, () -> NexusSearchClient.search(HttpClient.newHttpClient(),
                "https://repo.maven.apache.org/maven2/", "rxnorm", List.of("reasoned-sa")));
    }

    @Test
    void componentSearchUriUsesExactGroupNameVersionFields() {
        Optional<URI> uri = NexusSearchClient.componentSearchUri("https://nexus.tinkar.org/repository/ike-restricted/",
                new ArtifactCoordinates("dev.ikm.tinkar.data", "snomedct-international"), "20250801T120000Z-1.0.3");
        assertTrue(uri.isPresent());
        assertEquals("https://nexus.tinkar.org/service/rest/v1/search?repository=ike-restricted"
                + "&group=dev.ikm.tinkar.data&name=snomedct-international&version=20250801T120000Z-1.0.3", uri.get().toString());
    }

    @Test
    void componentAssetsReturnsClassifiedZipsWithSizeAndChecksumButExcludesTheClassifiedPom() throws Exception {
        // Real shape confirmed against a live Nexus-hosted repository (dev.ikm.tinkar.data:
        // loinc), 2026-07-21: checksum.sha256 is present directly on every asset, and the main
        // POM (no classifier) is a separate asset from a classifier="build" pom some deploys
        // also publish for internal tooling metadata — only the unclassified one is "the" POM.
        String response = """
                {
                  "items": [
                    {
                      "group": "dev.ikm.tinkar.data",
                      "name": "snomedct-international",
                      "version": "20250801T120000Z-1.0.3",
                      "assets": [
                        {"fileSize": 699327562, "checksum": {"sha256": "aaa111"}, "maven2": {"classifier": "reasoned-sa", "extension": "zip"}},
                        {"fileSize": 698609900, "checksum": {"sha256": "bbb222"}, "maven2": {"classifier": "reasoned-pb", "extension": "zip"}},
                        {"fileSize": 12345, "checksum": {"sha256": "ccc333"}, "maven2": {"extension": "pom"}},
                        {"fileSize": 999, "checksum": {"sha256": "ddd444"}, "maven2": {"classifier": "build", "extension": "pom"}},
                        {"fileSize": 40, "maven2": {"classifier": "reasoned-sa", "extension": "zip.sha1"}}
                      ]
                    }
                  ],
                  "continuationToken": null
                }
                """;
        server.createContext("/service/rest/v1/search", exchange -> respond(exchange, response));

        NexusSearchClient.ComponentAssets assets = NexusSearchClient.componentAssets(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/",
                new ArtifactCoordinates("dev.ikm.tinkar.data", "snomedct-international"), "20250801T120000Z-1.0.3");

        assertEquals(Set.of("reasoned-sa", "reasoned-pb"), assets.classifiers().keySet());
        assertEquals(OptionalLong.of(699327562L), assets.classifiers().get("reasoned-sa").size());
        assertEquals(Optional.of("aaa111"), assets.classifiers().get("reasoned-sa").sha256());
        assertEquals(OptionalLong.of(698609900L), assets.classifiers().get("reasoned-pb").size());
        assertEquals(Optional.of("bbb222"), assets.classifiers().get("reasoned-pb").sha256());
        assertTrue(assets.pom().isPresent());
        assertEquals(OptionalLong.of(12345L), assets.pom().get().size());
        assertEquals(Optional.of("ccc333"), assets.pom().get().sha256());
    }

    @Test
    void componentAssetsSizeAndChecksumAreEmptyWhenNexusDoesNotReportThem() throws Exception {
        String response = """
                {
                  "items": [
                    {
                      "group": "dev.ikm.tinkar.data",
                      "name": "snomedct-international",
                      "version": "1.0.0",
                      "assets": [
                        {"maven2": {"classifier": "reasoned-sa", "extension": "zip"}}
                      ]
                    }
                  ],
                  "continuationToken": null
                }
                """;
        server.createContext("/service/rest/v1/search", exchange -> respond(exchange, response));

        NexusSearchClient.ComponentAssets assets = NexusSearchClient.componentAssets(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/",
                new ArtifactCoordinates("dev.ikm.tinkar.data", "snomedct-international"), "1.0.0");

        assertEquals(OptionalLong.empty(), assets.classifiers().get("reasoned-sa").size());
        assertEquals(Optional.empty(), assets.classifiers().get("reasoned-sa").sha256());
    }

    @Test
    void componentAssetsReturnsEmptyWhenComponentDoesNotExist() throws Exception {
        server.createContext("/service/rest/v1/search", exchange -> respond(exchange, "{\"items\": [], \"continuationToken\": null}"));

        NexusSearchClient.ComponentAssets assets = NexusSearchClient.componentAssets(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/",
                new ArtifactCoordinates("dev.ikm.tinkar.data", "nonexistent"), "1.0.0");

        assertTrue(assets.classifiers().isEmpty());
        assertTrue(assets.pom().isEmpty());
    }

    @Test
    void compatibleVersionsReturnsOnlyVersionsWithACompatibleClassifiedZipNewestLast() throws Exception {
        // Mirrors SOLOR's real shape (2026-07-22): reasoned-sa zips at 20250814 and 20250827; a
        // 20250725-1.0.0 whose reasoned-sa is an XML (not a zip); and a POM-only 1.0.0-SNAPSHOT.
        // Only the two real zip versions may be offered, and the POM-only / xml-only ones must
        // be filtered out — a version scoped by maven.classifier + maven.extension=zip only
        // returns items that genuinely have that zip, so the XML/POM versions never appear here.
        server.createContext("/service/rest/v1/search", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query.contains("maven.classifier=reasoned-sa")) {
                respond(exchange, """
                        {"items": [
                          {"group": "dev.ikm.tinkar.data", "name": "SOLOR", "version": "20250814",
                            "assets": [{"maven2": {"classifier": "reasoned-sa", "extension": "zip", "version": "20250814"}}]},
                          {"group": "dev.ikm.tinkar.data", "name": "SOLOR", "version": "20250827",
                            "assets": [{"maven2": {"classifier": "reasoned-sa", "extension": "zip", "version": "20250827"}}]}
                        ], "continuationToken": null}
                        """);
            } else {
                respond(exchange, "{\"items\": [], \"continuationToken\": null}");
            }
        });

        List<String> versions = NexusSearchClient.compatibleVersions(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/",
                new ArtifactCoordinates("dev.ikm.tinkar.data", "SOLOR"),
                List.of("reasoned-sa", "unreasoned-sa"), null);

        assertEquals(List.of("20250814", "20250827"), versions);
    }

    @Test
    void compatibleVersionsIsEmptyWhenNoVersionPublishesACompatibleZip() throws Exception {
        server.createContext("/service/rest/v1/search", exchange -> respond(exchange, "{\"items\": [], \"continuationToken\": null}"));

        List<String> versions = NexusSearchClient.compatibleVersions(HttpClient.newHttpClient(),
                baseUrl + "/repository/ike-restricted/",
                new ArtifactCoordinates("dev.ikm.tinkar.data", "SOLOR"),
                List.of("reasoned-sa", "unreasoned-sa"), null);

        assertTrue(versions.isEmpty());
    }

    @Test
    void compatibleVersionsThrowsForNonNexusRepositoryUrl() {
        assertThrows(IOException.class, () -> NexusSearchClient.compatibleVersions(HttpClient.newHttpClient(),
                "https://repo.maven.apache.org/maven2/",
                new ArtifactCoordinates("dev.ikm.tinkar.data", "SOLOR"), List.of("reasoned-sa"), null));
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
