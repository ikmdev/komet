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

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryConnectionTesterTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/repository/example/";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void returnsTrueFor2xxResponse() throws Exception {
        server.createContext("/repository/example/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        assertTrue(RepositoryConnectionTester.testConnection(HttpClient.newHttpClient(), baseUrl));
    }

    @Test
    void returnsFalseFor401Response() throws Exception {
        server.createContext("/repository/example/", exchange -> {
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });

        assertFalse(RepositoryConnectionTester.testConnection(HttpClient.newHttpClient(), baseUrl));
    }

    @Test
    void sendsBasicAuthorizationWhenCredentialsProvided() throws Exception {
        AtomicReference<String> receivedAuthHeader = new AtomicReference<>();
        server.createContext("/repository/example/", exchange -> {
            receivedAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        Credentials credentials = new Credentials("build-user", "s3cret".toCharArray());
        String expectedHeader = "Basic " + Base64.getEncoder().encodeToString("build-user:s3cret".getBytes(StandardCharsets.UTF_8));

        assertTrue(RepositoryConnectionTester.testConnection(HttpClient.newHttpClient(), baseUrl, credentials));
        assertEquals(expectedHeader, receivedAuthHeader.get());
    }

    @Test
    void sendsNoAuthorizationHeaderWithoutCredentials() throws Exception {
        AtomicReference<String> receivedAuthHeader = new AtomicReference<>("unset");
        server.createContext("/repository/example/", exchange -> {
            receivedAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        assertTrue(RepositoryConnectionTester.testConnection(HttpClient.newHttpClient(), baseUrl, null));
        assertNull(receivedAuthHeader.get());
    }

    @Test
    void throwsOnConnectionFailure() {
        String unreachableUrl = "http://localhost:1/repository/example/";
        assertThrows(Exception.class, () -> RepositoryConnectionTester.testConnection(HttpClient.newHttpClient(), unreachableUrl));
    }
}
