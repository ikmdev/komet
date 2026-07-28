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

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the platform-trust union against a real TLS server whose certificate chain is
 * signed by a private CA — the exact shape a TLS-inspecting corporate network produces.
 *
 * <p>Fixtures ({@code src/test/resources}): {@code tls-test-server-keystore.p12} holds a
 * {@code CN=localhost} server key whose chain is signed by the private
 * {@code CN=IKE Test Interception CA}; {@code tls-test-interception-truststore.p12} holds
 * just that CA — standing in for an operating-system trust store that IT provisioned. Both
 * PKCS12, storepass {@code changeit}, 100-year validity, generated with {@code keytool}
 * ({@code -genkeypair}/{@code -certreq}/{@code -gencert} with
 * {@code san=dns:localhost,ip:127.0.0.1}).
 */
class PlatformTrustTest {

    private static final char[] FIXTURE_STOREPASS = "changeit".toCharArray();

    private HttpsServer server;
    private String baseUrl;

    @BeforeEach
    void startPrivatelySignedServer() throws Exception {
        KeyStore serverKeyStore = loadFixture("/tls-test-server-keystore.p12");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(serverKeyStore, FIXTURE_STOREPASS);
        SSLContext serverContext = SSLContext.getInstance("TLS");
        serverContext.init(keyManagerFactory.getKeyManagers(), null, null);

        server = HttpsServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(serverContext));
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        baseUrl = "https://localhost:" + server.getAddress().getPort() + "/";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void bundledTrustAloneRejectsThePrivatelySignedChain() {
        HttpClient bundledOnly = HttpClient.newHttpClient();

        IOException failure = assertThrows(IOException.class,
                () -> RepositoryConnectionTester.testConnection(bundledOnly, baseUrl));

        assertNotNull(findCause(failure, SSLHandshakeException.class),
                "expected a TLS handshake failure, got: " + failure);
    }

    @Test
    void unionWithThePlatformStoreAcceptsTheChain() throws Exception {
        SSLContext unionContext = PlatformTrust.sslContext(List.of(loadFixture("/tls-test-interception-truststore.p12")));
        HttpClient unionClient = HttpClient.newBuilder().sslContext(unionContext).build();

        assertTrue(RepositoryConnectionTester.testConnection(unionClient, baseUrl));
    }

    @Test
    void rejectionNamesTheServedChainsIssuerAndDescribesInterception() {
        // Bundled CA set only, but through the composite — its terminal rejection is the
        // message users ultimately see, so it must name who actually signed the served chain.
        SSLContext compositeBundledOnly = PlatformTrust.sslContext(List.of());
        HttpClient client = HttpClient.newBuilder().sslContext(compositeBundledOnly).build();

        IOException failure = assertThrows(IOException.class,
                () -> RepositoryConnectionTester.testConnection(client, baseUrl));

        CertificateException rejection = findCause(failure, CertificateException.class);
        assertNotNull(rejection, "expected a certificate rejection in the cause chain, got: " + failure);
        assertTrue(rejection.getMessage().contains("IKE Test Interception CA"),
                "rejection should name the served chain's issuer: " + rejection.getMessage());

        String described = ConnectionFailures.describe(failure);
        assertTrue(described.contains("operating-system trust store"),
                "description should explain interception: " + described);
        assertTrue(described.contains("IKE Test Interception CA"),
                "description should carry the issuer detail: " + described);
    }

    @Test
    void realPlatformStoresLoadWithoutFailing() {
        // Whatever this machine's OS offers must load (or be skipped) without throwing, and
        // the union context must build on top of it — the production path end to end.
        assertNotNull(PlatformTrust.sslContext(PlatformTrust.platformKeyStores()));
        assertNotNull(PlatformTrust.sslContext());
    }

    private static KeyStore loadFixture(String resource) throws Exception {
        try (InputStream in = PlatformTrustTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "missing test fixture " + resource);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(in, FIXTURE_STOREPASS);
            return keyStore;
        }
    }

    private static <T extends Throwable> T findCause(Throwable failure, Class<T> type) {
        Throwable current = failure;
        for (int depth = 0; current != null && depth < 16; depth++) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        return null;
    }
}
