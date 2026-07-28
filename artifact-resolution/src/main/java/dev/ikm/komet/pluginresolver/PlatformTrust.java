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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * An {@link SSLContext} that trusts the union of the JDK's bundled CA set and the operating
 * system's trust store.
 *
 * <p>The app ships with a jlinked runtime, so the default {@code SSLContext} trusts only that
 * runtime's bundled {@code cacerts} — the operating system's trust store is never consulted.
 * On networks that inspect TLS traffic (a corporate proxy or firewall re-signing connections
 * with the organization's private CA), IT provisions that CA into the <em>operating-system</em>
 * trust store, which is why browsers on such machines work while the bundled-only default
 * fails every connection with a PKIX path-building error. Trusting the union restores parity
 * with the rest of the machine: publicly-signed chains keep validating against the bundled
 * set, and whatever the OS has been told to trust becomes acceptable too
 * (IKE-Network/ike-issues#957).
 *
 * <p>Platform sources, all optional and skipped with a WARN when unavailable:
 * <ul>
 *   <li><strong>macOS</strong> — {@code KeychainStore-ROOT} (Apple's built-in roots) and
 *       {@code KeychainStore} (the keychain search list, including
 *       {@code /Library/Keychains/System.keychain} where MDM-deployed CAs land). Both are
 *       served by the Apple provider in {@code java.base}, so no extra runtime module is
 *       needed.</li>
 *   <li><strong>Windows</strong> — {@code Windows-ROOT} (the system certificate store,
 *       including GPO/MDM-deployed enterprise roots). Served by SunMSCAPI from
 *       {@code jdk.crypto.mscapi}; the jlink assembly's {@code --bind-services} links
 *       provider modules into the image, and the INFO line logged per loaded store makes a
 *       runtime missing the provider immediately visible.</li>
 *   <li>Anything else — bundled {@code cacerts} only.</li>
 * </ul>
 */
public final class PlatformTrust {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformTrust.class);

    private PlatformTrust() {
    }

    /**
     * The process-wide union {@link SSLContext}: the JDK's bundled CA set first, then each
     * platform trust store available on this operating system. Built once on first use; if
     * building fails outright, falls back to the JDK default context so a trust-store problem
     * can never make connections less capable than they were without this class.
     *
     * @return the union context, or the JDK default context if the union cannot be built
     */
    public static SSLContext sslContext() {
        return Holder.CONTEXT;
    }

    /**
     * As {@link #sslContext()}, but trusting the union of the JDK's bundled CA set and
     * {@code platformStores} — the seam {@link #sslContext()} wires the real platform stores
     * through, and tests use to stand in a simulated one.
     *
     * @param platformStores the additional trust stores to union with the bundled CA set
     * @return an {@link SSLContext} trusting the union
     * @throws IllegalStateException if no trust manager at all can be created, or the TLS
     *         context cannot be initialized — both signal a broken JDK security configuration
     */
    static SSLContext sslContext(List<KeyStore> platformStores) {
        List<X509TrustManager> delegates = new ArrayList<>();
        trustManagerFor(null).ifPresent(delegates::add);
        for (KeyStore store : platformStores) {
            trustManagerFor(store).ifPresent(delegates::add);
        }
        if (delegates.isEmpty()) {
            throw new IllegalStateException("no trust manager could be created from any trust source");
        }
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {new CompositeX509TrustManager(delegates)}, null);
            return context;
        } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
            throw new IllegalStateException("TLS context could not be initialized", e);
        }
    }

    /**
     * The platform trust stores available on this operating system — see the class Javadoc
     * for which stores are consulted per platform. A store that fails to load is skipped with
     * a WARN; each loaded store is reported at INFO with its entry count, so a runtime image
     * missing a platform provider shows up in the log rather than as silently narrower trust.
     *
     * @return the loaded platform stores, possibly empty
     */
    static List<KeyStore> platformKeyStores() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        List<String> storeTypes;
        if (osName.contains("mac")) {
            storeTypes = List.of("KeychainStore-ROOT", "KeychainStore");
        } else if (osName.contains("win")) {
            storeTypes = List.of("Windows-ROOT");
        } else {
            storeTypes = List.of();
        }
        List<KeyStore> stores = new ArrayList<>();
        for (String storeType : storeTypes) {
            try {
                KeyStore store = KeyStore.getInstance(storeType);
                store.load(null, null);
                stores.add(store);
                LOG.info("Platform trust store {} loaded ({} entries)", storeType, store.size());
            } catch (Exception e) {
                LOG.warn("Platform trust store {} unavailable — continuing without it", storeType, e);
            }
        }
        return stores;
    }

    private static Optional<X509TrustManager> trustManagerFor(KeyStore store) {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(store);
            for (TrustManager trustManager : factory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager x509TrustManager) {
                    return Optional.of(x509TrustManager);
                }
            }
            LOG.warn("Trust source {} produced no X509TrustManager — skipping it",
                    store == null ? "(bundled CA set)" : store.getType());
            return Optional.empty();
        } catch (Exception e) {
            LOG.warn("Trust source {} could not be initialized — skipping it",
                    store == null ? "(bundled CA set)" : store.getType(), e);
            return Optional.empty();
        }
    }

    private static final class Holder {
        static final SSLContext CONTEXT = build();

        private static SSLContext build() {
            try {
                return sslContext(platformKeyStores());
            } catch (RuntimeException e) {
                LOG.warn("Platform trust union could not be built — falling back to the JDK default trust", e);
                try {
                    return SSLContext.getDefault();
                } catch (NoSuchAlgorithmException fatal) {
                    throw new IllegalStateException("JDK default SSLContext unavailable", fatal);
                }
            }
        }
    }
}
