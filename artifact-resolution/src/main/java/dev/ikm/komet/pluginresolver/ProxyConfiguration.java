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
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * An HTTP(S) proxy taken from {@code ~/.m2/settings.xml} {@code <proxies>}, in the form
 * {@link java.net.http.HttpClient} consumes: a {@link ProxySelector} honoring
 * {@code nonProxyHosts}, and — when the proxy declares credentials — an {@link Authenticator}
 * answering proxy challenges only. {@code java.net.http.HttpClient} ignores the operating
 * system's proxy settings just as it ignores the OS trust store, so on networks that require
 * an explicit proxy this is what makes the repository reachable at all; {@code settings.xml}
 * is where Maven users already declare it (IKE-Network/ike-issues#957).
 */
public final class ProxyConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyConfiguration.class);

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final List<Pattern> nonProxyHostPatterns;

    private ProxyConfiguration(String host, int port, String username, String password, String nonProxyHosts) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.nonProxyHostPatterns = nonProxyHostPatterns(nonProxyHosts);
    }

    /**
     * The first usable proxy among {@code proxies}: active, a non-blank host, and not a SOCKS
     * proxy ({@code java.net.http.HttpClient} tunnels through HTTP proxies only — a SOCKS-only
     * declaration is skipped with a WARN rather than silently mistranslated).
     *
     * @param proxies the {@code <proxy>} entries, in declaration order
     * @return the first usable proxy, or empty if there is none
     * @throws NullPointerException if {@code proxies} is {@code null}
     */
    public static Optional<ProxyConfiguration> from(List<SettingsXmlReader.Proxy> proxies) {
        Objects.requireNonNull(proxies, "proxies");
        for (SettingsXmlReader.Proxy proxy : proxies) {
            if (!proxy.active() || proxy.host() == null || proxy.host().isBlank()) {
                continue;
            }
            String protocol = proxy.protocol() == null ? "http" : proxy.protocol().toLowerCase(Locale.ROOT);
            if (protocol.startsWith("socks")) {
                LOG.warn("settings.xml proxy {} declares protocol {} — only HTTP proxies are supported, skipping it",
                        proxy.id(), proxy.protocol());
                continue;
            }
            return Optional.of(new ProxyConfiguration(
                    proxy.host().trim(), proxy.port(), proxy.username(), proxy.plaintextPassword(),
                    proxy.nonProxyHosts()));
        }
        return Optional.empty();
    }

    /**
     * A selector routing every request through this proxy, except hosts matched by the
     * proxy's {@code nonProxyHosts} patterns ({@code |}-separated, {@code *} wildcards —
     * e.g. {@code "localhost|*.internal.example.com"}), which connect directly.
     *
     * @return the selector
     */
    public ProxySelector selector() {
        InetSocketAddress proxyAddress = InetSocketAddress.createUnresolved(host, port);
        List<Proxy> viaProxy = List.of(new Proxy(Proxy.Type.HTTP, proxyAddress));
        List<Proxy> direct = List.of(Proxy.NO_PROXY);
        return new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                String requestHost = uri == null ? null : uri.getHost();
                if (requestHost != null && nonProxyHostPatterns.stream()
                        .anyMatch(pattern -> pattern.matcher(requestHost).matches())) {
                    return direct;
                }
                return viaProxy;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress socketAddress, IOException failure) {
                LOG.warn("Proxy {} unreachable for {}", socketAddress, uri, failure);
            }
        };
    }

    /**
     * An authenticator answering <em>proxy</em> authentication challenges with the proxy's
     * credentials — server challenges are left unanswered, since repository credentials are
     * sent explicitly per request (see {@link BasicAuth}). Present only when the proxy
     * declares both a username and a plain-text password.
     *
     * @return the proxy authenticator, or empty when the proxy has no usable credentials
     */
    public Optional<Authenticator> authenticator() {
        if (username == null || password == null) {
            return Optional.empty();
        }
        char[] passwordChars = password.toCharArray();
        return Optional.of(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (getRequestorType() == RequestorType.PROXY) {
                    return new PasswordAuthentication(username, passwordChars);
                }
                return null;
            }
        });
    }

    private static List<Pattern> nonProxyHostPatterns(String nonProxyHosts) {
        if (nonProxyHosts == null || nonProxyHosts.isBlank()) {
            return List.of();
        }
        List<Pattern> patterns = new ArrayList<>();
        for (String token : nonProxyHosts.split("\\|")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] literals = trimmed.split("\\*", -1);
            StringBuilder regex = new StringBuilder();
            for (int i = 0; i < literals.length; i++) {
                if (i > 0) {
                    regex.append(".*");
                }
                regex.append(Pattern.quote(literals[i]));
            }
            patterns.add(Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE));
        }
        return List.copyOf(patterns);
    }
}
