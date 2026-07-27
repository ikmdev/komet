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

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionFailuresTest {

    @Test
    void ordinaryFailureKeepsItsOwnMessage() {
        assertEquals("Connection refused", ConnectionFailures.describe(new IOException("Connection refused")));
    }

    @Test
    void deepestCauseMessageWins() {
        IOException failure = new IOException("request failed", new IOException("nexus.example.com"));

        assertEquals("nexus.example.com", ConnectionFailures.describe(failure));
    }

    @Test
    void trustFailureIsExplainedAsInterception_carryingTheIssuerDetail() {
        SSLHandshakeException handshake = new SSLHandshakeException("PKIX path building failed");
        handshake.initCause(new CertificateException(
                "certificate chain is trusted by neither the bundled CA set nor the operating-system trust store"
                        + " (server=CN=localhost, chain issued by CN=Corporate Interception CA)"));
        IOException failure = new IOException(handshake);

        String described = ConnectionFailures.describe(failure);

        assertTrue(described.contains("operating-system trust store"), described);
        assertTrue(described.contains("corporate proxy or firewall"), described);
        assertTrue(described.contains("CN=Corporate Interception CA"), described);
    }

    @Test
    void validityFailurePointsAtTheClock() {
        SSLHandshakeException handshake = new SSLHandshakeException("PKIX path validation failed");
        handshake.initCause(new CertificateExpiredException("NotAfter: Thu Jan 01 00:00:00 UTC 2026"));
        IOException failure = new IOException(handshake);

        String described = ConnectionFailures.describe(failure);

        assertTrue(described.contains("date and time"), described);
        assertTrue(described.contains("NotAfter"), described);
    }

    @Test
    void messagelessFailureFallsBackToItsToString() {
        assertEquals("java.io.IOException", ConnectionFailures.describe(new IOException((String) null)));
    }
}
