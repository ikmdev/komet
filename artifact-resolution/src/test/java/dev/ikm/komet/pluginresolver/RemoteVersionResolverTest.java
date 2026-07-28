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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoteVersionResolverTest {

    private static final ArtifactCoordinates COORDINATES = new ArtifactCoordinates("network.ike.komet", "example-plugin");
    private static final String METADATA_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata>
                <groupId>network.ike.komet</groupId>
                <artifactId>example-plugin</artifactId>
                <versioning>
                    <release>2.0.0</release>
                    <latest>2.0.0</latest>
                    <versions>
                        <version>1.0.0</version>
                        <version>1.1.0</version>
                        <version>2.0.0</version>
                    </versions>
                </versioning>
            </metadata>
            """;
    private static final String METADATA_XML_NO_RELEASE_OR_LATEST_TAGS = """
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata>
                <groupId>network.ike.komet</groupId>
                <artifactId>example-plugin</artifactId>
                <versioning>
                    <versions>
                        <version>1.0.0</version>
                        <version>1.1.0</version>
                        <version>2.0.0</version>
                    </versions>
                </versioning>
            </metadata>
            """;
    private static final String METADATA_XML_LATEST_ONLY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata>
                <groupId>network.ike.komet</groupId>
                <artifactId>example-plugin</artifactId>
                <versioning>
                    <latest>2.0.0-SNAPSHOT</latest>
                    <versions>
                        <version>1.0.0</version>
                        <version>2.0.0-SNAPSHOT</version>
                    </versions>
                </versioning>
            </metadata>
            """;

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> lastAuthorizationHeader = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/network/ike/komet/example-plugin/maven-metadata.xml", exchange -> respondMetadata(exchange, METADATA_XML));
        server.createContext("/network/ike/komet/missing-plugin/maven-metadata.xml", RemoteVersionResolverTest::respondNotFound);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void listsVersionsFromRemoteMetadata() throws IOException, InterruptedException {
        RemoteVersionResolver resolver = new RemoteVersionResolver();
        List<String> versions = resolver.remoteVersions(baseUrl, COORDINATES);
        assertEquals(List.of("1.0.0", "1.1.0", "2.0.0"), versions);
    }

    @Test
    void returnsEmptyWhenMetadataIsMissing() throws IOException, InterruptedException {
        RemoteVersionResolver resolver = new RemoteVersionResolver();
        List<String> versions = resolver.remoteVersions(baseUrl, new ArtifactCoordinates("network.ike.komet", "missing-plugin"));
        assertTrue(versions.isEmpty());
    }

    @Test
    void sendsBasicAuthorizationHeaderWhenCredentialsProvided() throws IOException, InterruptedException {
        server.createContext("/auth-check/network/ike/komet/example-plugin/maven-metadata.xml", exchange -> {
            lastAuthorizationHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respondMetadata(exchange, METADATA_XML);
        });

        RemoteVersionResolver resolver = new RemoteVersionResolver();
        resolver.remoteVersions(baseUrl + "auth-check/", COORDINATES, new Credentials("build-user", "s3cret".toCharArray()));

        String expected = "Basic " + Base64.getEncoder().encodeToString("build-user:s3cret".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, lastAuthorizationHeader.get());
    }

    @Test
    void constructorAcceptsInjectedHttpClient() throws IOException, InterruptedException {
        RemoteVersionResolver resolver = new RemoteVersionResolver(HttpClient.newHttpClient());
        assertEquals(List.of("1.0.0", "1.1.0", "2.0.0"), resolver.remoteVersions(baseUrl, COORDINATES));
    }

    @Test
    void mostRecentPrefersReleaseTag() throws IOException, InterruptedException {
        RemoteVersionResolver resolver = new RemoteVersionResolver();
        RemoteVersionResolver.VersionListing listing = resolver.remoteVersionListing(baseUrl, COORDINATES);
        assertEquals(List.of("1.0.0", "1.1.0", "2.0.0"), listing.versions());
        assertEquals(Optional.of("2.0.0"), listing.mostRecent());
    }

    @Test
    void mostRecentFallsBackToLatestTagWhenReleaseAbsent() throws IOException, InterruptedException {
        server.createContext("/network/ike/komet/latest-only-plugin/maven-metadata.xml",
                exchange -> respondMetadata(exchange, METADATA_XML_LATEST_ONLY));

        RemoteVersionResolver resolver = new RemoteVersionResolver();
        RemoteVersionResolver.VersionListing listing = resolver.remoteVersionListing(baseUrl,
                new ArtifactCoordinates("network.ike.komet", "latest-only-plugin"));

        assertEquals(Optional.of("2.0.0-SNAPSHOT"), listing.mostRecent());
    }

    @Test
    void mostRecentFallsBackToLastVersionWhenNeitherTagPresent() throws IOException, InterruptedException {
        server.createContext("/network/ike/komet/no-release-tag-plugin/maven-metadata.xml",
                exchange -> respondMetadata(exchange, METADATA_XML_NO_RELEASE_OR_LATEST_TAGS));

        RemoteVersionResolver resolver = new RemoteVersionResolver();
        RemoteVersionResolver.VersionListing listing = resolver.remoteVersionListing(baseUrl,
                new ArtifactCoordinates("network.ike.komet", "no-release-tag-plugin"));

        assertEquals(Optional.of("2.0.0"), listing.mostRecent());
    }

    @Test
    void mostRecentIsEmptyWhenMetadataIsMissing() throws IOException, InterruptedException {
        RemoteVersionResolver resolver = new RemoteVersionResolver();
        RemoteVersionResolver.VersionListing listing = resolver.remoteVersionListing(baseUrl,
                new ArtifactCoordinates("network.ike.komet", "missing-plugin"));

        assertTrue(listing.versions().isEmpty());
        assertEquals(Optional.empty(), listing.mostRecent());
    }

    @Test
    void resolveSnapshotFileVersionUsesTimestampAndBuildNumber() throws IOException, InterruptedException {
        // Real shape confirmed against a live Nexus-hosted snapshot repository
        // (dev.ikm.loinc:loinc-data:1.0.0-SNAPSHOT), 2026-07-21.
        String snapshotMetadataXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <metadata modelVersion="1.1.0">
                  <groupId>network.ike.komet</groupId>
                  <artifactId>example-plugin</artifactId>
                  <versioning>
                    <lastUpdated>20260714135548</lastUpdated>
                    <snapshot>
                      <timestamp>20260714.135548</timestamp>
                      <buildNumber>4</buildNumber>
                    </snapshot>
                    <snapshotVersions>
                      <snapshotVersion>
                        <extension>pom</extension>
                        <value>1.0.0-20260714.135548-4</value>
                        <updated>20260714135548</updated>
                      </snapshotVersion>
                    </snapshotVersions>
                  </versioning>
                  <version>1.0.0-SNAPSHOT</version>
                </metadata>
                """;
        server.createContext("/network/ike/komet/example-plugin/1.0.0-SNAPSHOT/maven-metadata.xml",
                exchange -> respondMetadata(exchange, snapshotMetadataXml));

        RemoteVersionResolver resolver = new RemoteVersionResolver();
        Optional<String> resolved = resolver.resolveSnapshotFileVersion(baseUrl, COORDINATES, "1.0.0-SNAPSHOT");

        assertEquals(Optional.of("1.0.0-20260714.135548-4"), resolved);
    }

    @Test
    void resolveSnapshotFileVersionFallsBackToSnapshotVersionsValueWhenSnapshotBlockIsAbsent() throws IOException, InterruptedException {
        String snapshotMetadataXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <metadata modelVersion="1.1.0">
                  <groupId>network.ike.komet</groupId>
                  <artifactId>example-plugin</artifactId>
                  <versioning>
                    <snapshotVersions>
                      <snapshotVersion>
                        <classifier>reasoned-sa</classifier>
                        <extension>zip</extension>
                        <value>1.0.0-20260714.135548-4</value>
                        <updated>20260714135548</updated>
                      </snapshotVersion>
                    </snapshotVersions>
                  </versioning>
                  <version>1.0.0-SNAPSHOT</version>
                </metadata>
                """;
        server.createContext("/network/ike/komet/example-plugin/1.0.0-SNAPSHOT/maven-metadata.xml",
                exchange -> respondMetadata(exchange, snapshotMetadataXml));

        RemoteVersionResolver resolver = new RemoteVersionResolver();
        Optional<String> resolved = resolver.resolveSnapshotFileVersion(baseUrl, COORDINATES, "1.0.0-SNAPSHOT");

        assertEquals(Optional.of("1.0.0-20260714.135548-4"), resolved);
    }

    @Test
    void resolveSnapshotFileVersionIsEmptyWhenNoSnapshotMetadataExists() throws IOException, InterruptedException {
        server.createContext("/network/ike/komet/example-plugin/9.9.9-SNAPSHOT/maven-metadata.xml",
                RemoteVersionResolverTest::respondNotFound);

        RemoteVersionResolver resolver = new RemoteVersionResolver();
        Optional<String> resolved = resolver.resolveSnapshotFileVersion(baseUrl, COORDINATES, "9.9.9-SNAPSHOT");

        assertEquals(Optional.empty(), resolved);
    }

    @Test
    void resolveSnapshotFileVersionSendsBasicAuthorizationHeaderWhenCredentialsProvided() throws IOException, InterruptedException {
        String snapshotMetadataXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <metadata modelVersion="1.1.0">
                  <groupId>network.ike.komet</groupId>
                  <artifactId>example-plugin</artifactId>
                  <versioning>
                    <snapshot>
                      <timestamp>20260714.135548</timestamp>
                      <buildNumber>4</buildNumber>
                    </snapshot>
                  </versioning>
                  <version>1.0.0-SNAPSHOT</version>
                </metadata>
                """;
        server.createContext("/auth-check/network/ike/komet/example-plugin/1.0.0-SNAPSHOT/maven-metadata.xml", exchange -> {
            lastAuthorizationHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respondMetadata(exchange, snapshotMetadataXml);
        });

        RemoteVersionResolver resolver = new RemoteVersionResolver();
        resolver.resolveSnapshotFileVersion(baseUrl + "auth-check/", COORDINATES, "1.0.0-SNAPSHOT",
                new Credentials("build-user", "s3cret".toCharArray()));

        String expected = "Basic " + Base64.getEncoder().encodeToString("build-user:s3cret".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, lastAuthorizationHeader.get());
    }

    private static void respondMetadata(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/xml");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static void respondNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }
}
