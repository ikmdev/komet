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

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The one way repository-facing {@link HttpClient}s are built. {@link HttpClient#newHttpClient()}
 * trusts only the jlinked runtime's bundled {@code cacerts} and ignores any proxy the user's
 * environment requires — both of which strand the app on managed corporate networks that the
 * rest of the machine handles fine. Clients from this factory trust the
 * {@linkplain PlatformTrust platform union} and honor {@code ~/.m2/settings.xml}
 * {@code <proxies>} (IKE-Network/ike-issues#957).
 */
public final class RepositoryHttpClients {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryHttpClients.class);

    private RepositoryHttpClients() {
    }

    /**
     * A client trusting the union of the bundled CA set and the operating-system trust store,
     * routed through the {@code ~/.m2/settings.xml} proxy when one is declared.
     *
     * @return the client
     */
    public static HttpClient newClient() {
        return newClient(settingsXmlProxy());
    }

    /**
     * As {@link #newClient()} with an explicit proxy — the seam {@link #newClient()} wires the
     * {@code settings.xml} proxy through, and tests use directly.
     *
     * @param proxy the proxy to route through, or empty for direct connections
     * @return the client
     */
    static HttpClient newClient(Optional<ProxyConfiguration> proxy) {
        HttpClient.Builder builder = HttpClient.newBuilder().sslContext(PlatformTrust.sslContext());
        proxy.ifPresent(configuration -> {
            builder.proxy(configuration.selector());
            configuration.authenticator().ifPresent(builder::authenticator);
        });
        return builder.build();
    }

    private static Optional<ProxyConfiguration> settingsXmlProxy() {
        Path settingsXmlPath = Path.of(System.getProperty("user.home"), ".m2", "settings.xml");
        try {
            return ProxyConfiguration.from(SettingsXmlReader.read(settingsXmlPath).proxies());
        } catch (IOException e) {
            LOG.debug("No usable settings.xml at {} — connecting without a proxy", settingsXmlPath, e);
            return Optional.empty();
        }
    }
}
