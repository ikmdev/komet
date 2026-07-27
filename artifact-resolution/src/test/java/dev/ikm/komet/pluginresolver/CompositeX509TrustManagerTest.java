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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit-level checks of the union semantics, against the same private-CA fixtures
 * {@link PlatformTrustTest} uses (see its Javadoc for how they were generated).
 */
class CompositeX509TrustManagerTest {

    private static X509TrustManager bundledTrust;
    private static X509TrustManager interceptionTrust;
    private static X509Certificate interceptionCa;
    private static X509Certificate[] privatelySignedChain;

    @BeforeAll
    static void loadFixtures() throws Exception {
        bundledTrust = trustManagerFor(null);

        KeyStore truststore = loadFixture("/tls-test-interception-truststore.p12");
        interceptionTrust = trustManagerFor(truststore);
        interceptionCa = (X509Certificate) truststore.getCertificate("interception-ca");

        KeyStore serverKeyStore = loadFixture("/tls-test-server-keystore.p12");
        Certificate[] chain = serverKeyStore.getCertificateChain("server");
        privatelySignedChain = Arrays.copyOf(chain, chain.length, X509Certificate[].class);
    }

    @Test
    void acceptsWhenAnyDelegateTrustsTheChain() {
        CompositeX509TrustManager composite =
                new CompositeX509TrustManager(List.of(bundledTrust, interceptionTrust));

        assertDoesNotThrow(() -> composite.checkServerTrusted(privatelySignedChain, "RSA"));
    }

    @Test
    void rejectsOnlyWhenEveryDelegateRejects_namingTheServedChain() {
        CompositeX509TrustManager composite = new CompositeX509TrustManager(List.of(bundledTrust));

        CertificateException rejection = assertThrows(CertificateException.class,
                () -> composite.checkServerTrusted(privatelySignedChain, "RSA"));

        assertTrue(rejection.getMessage().contains("IKE Test Interception CA"),
                "rejection should name the served chain's issuer: " + rejection.getMessage());
        assertEquals(1, rejection.getSuppressed().length,
                "each delegate's own rejection should be attached as suppressed");
    }

    @Test
    void acceptedIssuersIsTheUnionOfAllDelegates() {
        CompositeX509TrustManager composite =
                new CompositeX509TrustManager(List.of(bundledTrust, interceptionTrust));

        List<X509Certificate> issuers = Arrays.asList(composite.getAcceptedIssuers());

        assertTrue(issuers.contains(interceptionCa), "union should include the platform store's CA");
        assertTrue(issuers.size() > bundledTrust.getAcceptedIssuers().length,
                "union should extend, not replace, the bundled CA set");
    }

    @Test
    void atLeastOneDelegateIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeX509TrustManager(List.of()));
    }

    private static X509TrustManager trustManagerFor(KeyStore keyStore) throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);
        for (TrustManager trustManager : factory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager x509TrustManager) {
                return x509TrustManager;
            }
        }
        throw new IllegalStateException("no X509TrustManager from " + keyStore);
    }

    private static KeyStore loadFixture(String resource) throws Exception {
        try (InputStream in = CompositeX509TrustManagerTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "missing test fixture " + resource);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(in, "changeit".toCharArray());
            return keyStore;
        }
    }
}
