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

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A trust manager that accepts a certificate chain if <em>any</em> of its delegates does —
 * union semantics over several trust sources (the JDK's bundled CA set, the operating
 * system's trust store, ...). A chain is rejected only when every delegate rejects it, and
 * the terminal {@link CertificateException} then names what the server actually served —
 * its subject and the issuer of the last chain element — so a failure on a TLS-inspecting
 * network reads as "chain issued by CN=&lt;the middlebox's CA&gt;" instead of an anonymous
 * path-building error. Every delegate's own rejection is attached as a suppressed exception.
 *
 * <p>Delegates are consulted in order, so the common case (a publicly-signed chain the
 * bundled CA set trusts) never touches the platform stores.
 */
final class CompositeX509TrustManager extends X509ExtendedTrustManager {

    private final List<X509TrustManager> delegates;

    /**
     * Creates the composite over {@code delegates}, consulted in list order.
     *
     * @param delegates the trust managers to consult
     * @throws NullPointerException if {@code delegates} is {@code null}
     * @throws IllegalArgumentException if {@code delegates} is empty
     */
    CompositeX509TrustManager(List<X509TrustManager> delegates) {
        Objects.requireNonNull(delegates, "delegates");
        if (delegates.isEmpty()) {
            throw new IllegalArgumentException("at least one delegate trust manager is required");
        }
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        trustAny(chain, delegate -> delegate.checkServerTrusted(chain, authType));
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        trustAny(chain, delegate -> {
            if (delegate instanceof X509ExtendedTrustManager extended) {
                extended.checkServerTrusted(chain, authType, socket);
            } else {
                delegate.checkServerTrusted(chain, authType);
            }
        });
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        trustAny(chain, delegate -> {
            if (delegate instanceof X509ExtendedTrustManager extended) {
                extended.checkServerTrusted(chain, authType, engine);
            } else {
                delegate.checkServerTrusted(chain, authType);
            }
        });
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        trustAny(chain, delegate -> delegate.checkClientTrusted(chain, authType));
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        trustAny(chain, delegate -> {
            if (delegate instanceof X509ExtendedTrustManager extended) {
                extended.checkClientTrusted(chain, authType, socket);
            } else {
                delegate.checkClientTrusted(chain, authType);
            }
        });
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        trustAny(chain, delegate -> {
            if (delegate instanceof X509ExtendedTrustManager extended) {
                extended.checkClientTrusted(chain, authType, engine);
            } else {
                delegate.checkClientTrusted(chain, authType);
            }
        });
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        Set<X509Certificate> issuers = new LinkedHashSet<>();
        for (X509TrustManager delegate : delegates) {
            issuers.addAll(Arrays.asList(delegate.getAcceptedIssuers()));
        }
        return issuers.toArray(new X509Certificate[0]);
    }

    @FunctionalInterface
    private interface TrustCheck {
        void check(X509TrustManager delegate) throws CertificateException;
    }

    private void trustAny(X509Certificate[] chain, TrustCheck check) throws CertificateException {
        List<CertificateException> rejections = new ArrayList<>();
        for (X509TrustManager delegate : delegates) {
            try {
                check.check(delegate);
                return;
            } catch (CertificateException rejection) {
                rejections.add(rejection);
            }
        }
        throw untrusted(chain, rejections);
    }

    private static CertificateException untrusted(X509Certificate[] chain, List<CertificateException> rejections) {
        String served;
        if (chain == null || chain.length == 0) {
            served = "no certificate chain was served";
        } else {
            served = "server=" + chain[0].getSubjectX500Principal()
                    + ", chain issued by " + chain[chain.length - 1].getIssuerX500Principal();
        }
        CertificateException failure = new CertificateException(
                "certificate chain is trusted by neither the bundled CA set nor the operating-system trust store ("
                        + served + ")");
        rejections.forEach(failure::addSuppressed);
        return failure;
    }
}
