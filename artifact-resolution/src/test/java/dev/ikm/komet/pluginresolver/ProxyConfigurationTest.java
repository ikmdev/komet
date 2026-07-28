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

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyConfigurationTest {

    private static SettingsXmlReader.Proxy proxy(String id, boolean active, String protocol, String host,
                                                 String username, String password, String nonProxyHosts) {
        return new SettingsXmlReader.Proxy(id, active, protocol, host, 3128, username, password, nonProxyHosts);
    }

    @Test
    void firstUsableProxyWins_skippingInactiveSocksAndHostless() {
        Optional<ProxyConfiguration> configuration = ProxyConfiguration.from(List.of(
                proxy("inactive", false, "http", "inactive.example.com", null, null, null),
                proxy("socks", true, "socks5", "socks.example.com", null, null, null),
                proxy("hostless", true, "http", "  ", null, null, null),
                proxy("corp", true, "http", "proxy.example.com", null, null, null)));

        ProxySelector selector = configuration.orElseThrow().selector();
        List<Proxy> route = selector.select(URI.create("https://nexus.tinkar.org/repository/ike-restricted/"));

        assertEquals(1, route.size());
        assertEquals(Proxy.Type.HTTP, route.getFirst().type());
        assertEquals(InetSocketAddress.createUnresolved("proxy.example.com", 3128), route.getFirst().address());
    }

    @Test
    void noUsableProxyMeansEmpty() {
        assertTrue(ProxyConfiguration.from(List.of()).isEmpty());
        assertTrue(ProxyConfiguration.from(List.of(
                proxy("inactive", false, "http", "proxy.example.com", null, null, null))).isEmpty());
    }

    @Test
    void nonProxyHostsConnectDirectly_wildcardsAndCaseInsensitive() {
        ProxySelector selector = ProxyConfiguration.from(List.of(
                        proxy("corp", true, "http", "proxy.example.com", null, null, "localhost|*.internal")))
                .orElseThrow().selector();

        assertEquals(List.of(Proxy.NO_PROXY), selector.select(URI.create("https://localhost:8443/nexus")));
        assertEquals(List.of(Proxy.NO_PROXY), selector.select(URI.create("https://svc.internal/repo")));
        assertEquals(List.of(Proxy.NO_PROXY), selector.select(URI.create("https://SVC.INTERNAL/repo")));
        assertEquals(Proxy.Type.HTTP, selector.select(URI.create("https://internal.example.com/")).getFirst().type());
    }

    @Test
    void authenticatorAnswersProxyChallengesOnly() {
        Authenticator authenticator = ProxyConfiguration.from(List.of(
                        proxy("corp", true, "http", "proxy.example.com", "proxy-user", "proxy-pass", null)))
                .orElseThrow().authenticator().orElseThrow();

        PasswordAuthentication proxyAnswer = authenticator.requestPasswordAuthenticationInstance(
                "proxy.example.com", null, 3128, "http", "proxy auth", "basic", null,
                Authenticator.RequestorType.PROXY);
        assertEquals("proxy-user", proxyAnswer.getUserName());
        assertEquals("proxy-pass", new String(proxyAnswer.getPassword()));

        PasswordAuthentication serverAnswer = authenticator.requestPasswordAuthenticationInstance(
                "nexus.tinkar.org", null, 443, "https", "server auth", "basic", null,
                Authenticator.RequestorType.SERVER);
        assertNull(serverAnswer, "server challenges are answered per request via BasicAuth, not the authenticator");
    }

    @Test
    void credentiallessProxyHasNoAuthenticator() {
        assertTrue(ProxyConfiguration.from(List.of(
                        proxy("corp", true, "http", "proxy.example.com", "user-without-password", null, null)))
                .orElseThrow().authenticator().isEmpty());
    }
}
