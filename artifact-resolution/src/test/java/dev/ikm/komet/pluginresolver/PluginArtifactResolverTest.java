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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginArtifactResolverTest {

    private static final ArtifactCoordinates COORDINATES = new ArtifactCoordinates("network.ike.komet", "example-plugin");

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/network/ike/komet/example-plugin/2.0.0/example-plugin-2.0.0.jar",
                exchange -> respondBytes(exchange, "remote-jar-bytes-2.0.0"));
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void stagesFromLocalRepositoryWithoutNetworkAccess(@TempDir Path localRepo, @TempDir Path pluginDirectory) throws IOException, InterruptedException {
        writeLocalJar(localRepo, "1.0.0", "local-jar-bytes-1.0.0");

        PluginArtifactResolver resolver = new PluginArtifactResolver(localRepo);
        Path staged = resolver.resolveAndStage(COORDINATES, "1.0.0", null, pluginDirectory);

        assertEquals("example-plugin-1.0.0.jar", staged.getFileName().toString());
        assertEquals("local-jar-bytes-1.0.0", Files.readString(staged));
    }

    @Test
    void downloadsFromRemoteWhenNotPresentLocally(@TempDir Path localRepo, @TempDir Path pluginDirectory) throws IOException, InterruptedException {
        PluginArtifactResolver resolver = new PluginArtifactResolver(localRepo, new RemoteVersionResolver(), HttpClient.newHttpClient());
        Path staged = resolver.resolveAndStage(COORDINATES, "2.0.0", baseUrl, pluginDirectory);

        assertEquals("remote-jar-bytes-2.0.0", Files.readString(staged));
        // The downloaded artifact is cached into the local repository, same as a real Maven repo would.
        Path cachedInLocalRepo = localRepo.resolve("network/ike/komet/example-plugin/2.0.0/example-plugin-2.0.0.jar");
        assertTrue(Files.exists(cachedInLocalRepo));
    }

    @Test
    void restagingReplacesPreviouslyStagedVersionOfSameArtifact(@TempDir Path localRepo, @TempDir Path pluginDirectory) throws IOException, InterruptedException {
        writeLocalJar(localRepo, "1.0.0", "old-version");
        writeLocalJar(localRepo, "1.1.0", "new-version");
        PluginArtifactResolver resolver = new PluginArtifactResolver(localRepo);

        resolver.resolveAndStage(COORDINATES, "1.0.0", null, pluginDirectory);
        resolver.resolveAndStage(COORDINATES, "1.1.0", null, pluginDirectory);

        assertFalse(Files.exists(pluginDirectory.resolve("example-plugin-1.0.0.jar")),
                "the old version should have been removed before staging the new one");
        assertTrue(Files.exists(pluginDirectory.resolve("example-plugin-1.1.0.jar")));
    }

    private static void writeLocalJar(Path localRepo, String version, String content) throws IOException {
        Path jar = localRepo.resolve("network/ike/komet/example-plugin/" + version + "/example-plugin-" + version + ".jar");
        Files.createDirectories(jar.getParent());
        Files.writeString(jar, content);
    }

    private static void respondBytes(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
