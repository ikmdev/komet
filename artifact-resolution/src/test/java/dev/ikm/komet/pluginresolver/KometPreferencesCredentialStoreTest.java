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

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KometPreferencesCredentialStoreTest {

    private Preferences testRootNode;
    private KometPreferencesCredentialStore store;

    @BeforeEach
    void setUp() {
        // A uniquely-named node under the real java.util.prefs user root, isolated per test run.
        testRootNode = Preferences.userRoot().node("ike-test/" + getClass().getSimpleName() + "-" + UUID.randomUUID());
        KometPreferences userPreferences = new PreferencesWrapper(testRootNode);
        store = new KometPreferencesCredentialStore(userPreferences);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        testRootNode.removeNode();
        testRootNode.flush();
    }

    @Test
    void getReturnsEmptyWhenNoCredentialsStored() {
        assertTrue(store.get("unknown-repository").isEmpty());
    }

    @Test
    void putThenGetRoundTripsCredentials() {
        store.put("internal-nexus", new Credentials("build-user", "s3cret".toCharArray()));

        Optional<Credentials> retrieved = store.get("internal-nexus");

        assertTrue(retrieved.isPresent());
        assertEquals("build-user", retrieved.get().username());
        assertArrayEquals("s3cret".toCharArray(), retrieved.get().password());
    }

    @Test
    void putOverwritesPreviouslyStoredCredentials() {
        store.put("internal-nexus", new Credentials("first-user", "first-password".toCharArray()));
        store.put("internal-nexus", new Credentials("second-user", "second-password".toCharArray()));

        Optional<Credentials> retrieved = store.get("internal-nexus");

        assertEquals("second-user", retrieved.orElseThrow().username());
        assertArrayEquals("second-password".toCharArray(), retrieved.orElseThrow().password());
    }

    @Test
    void removeClearsStoredCredentials() {
        store.put("internal-nexus", new Credentials("build-user", "s3cret".toCharArray()));

        store.remove("internal-nexus");

        assertTrue(store.get("internal-nexus").isEmpty());
    }

    @Test
    void differentRepositoriesAreStoredIndependently() {
        store.put("repo-one", new Credentials("user-one", "password-one".toCharArray()));
        store.put("repo-two", new Credentials("user-two", "password-two".toCharArray()));

        assertEquals("user-one", store.get("repo-one").orElseThrow().username());
        assertEquals("user-two", store.get("repo-two").orElseThrow().username());
    }

    // A repository id is routinely a URL, which java.util.prefs reads as a node PATH: the `//`
    // after the scheme threw `IllegalArgumentException: Consecutive slashes in path` before the
    // store derived a valid node name (ikmdev/komet#881). These run against a real preferences
    // node, so they fail on the actual defect rather than on a stand-in for it.

    @Test
    void urlShapedRepositoryIdRoundTripsRatherThanThrowing() {
        String repositoryUrl = "https://nexus.tinkar.org/repository/ike-public/";

        store.put(repositoryUrl, new Credentials("build-user", "s3cret".toCharArray()));

        assertEquals("build-user", store.get(repositoryUrl).orElseThrow().username());
    }

    @Test
    void urlShapedRepositoryIdIsRemovable() {
        String repositoryUrl = "https://nexus.tinkar.org/repository/ike-restricted/";
        store.put(repositoryUrl, new Credentials("build-user", "s3cret".toCharArray()));

        store.remove(repositoryUrl);

        assertTrue(store.get(repositoryUrl).isEmpty());
    }

    @Test
    void repositoryUrlsDifferingOnlyByTrailingSlashDoNotShareCredentials() {
        store.put("https://nexus.example.org/repository/public",
                new Credentials("no-slash-user", "one".toCharArray()));
        store.put("https://nexus.example.org/repository/public/",
                new Credentials("slash-user", "two".toCharArray()));

        assertEquals("no-slash-user",
                store.get("https://nexus.example.org/repository/public").orElseThrow().username());
        assertEquals("slash-user",
                store.get("https://nexus.example.org/repository/public/").orElseThrow().username());
    }

    @Test
    void repositoryUrlsSharingALongPrefixDoNotCollideBeyondTheTruncationPoint() {
        // Both exceed the readable-prefix limit and are identical well past it: only the digest
        // of the whole id keeps them apart.
        String base = "https://nexus.example.org/repository/a-very-long-repository-name-segment/";
        store.put(base + "alpha", new Credentials("alpha-user", "one".toCharArray()));
        store.put(base + "beta", new Credentials("beta-user", "two".toCharArray()));

        assertEquals("alpha-user", store.get(base + "alpha").orElseThrow().username());
        assertEquals("beta-user", store.get(base + "beta").orElseThrow().username());
    }

    @Test
    void derivedNodeNameSatisfiesThePreferencesNodeNameContract() {
        String repositoryUrl = "https://nexus.example.org/repository/"
                + "a-segment-long-enough-to-exceed-the-eighty-character-node-name-limit-on-its-own/";

        String nodeName = KometPreferencesCredentialStore.nodeName(repositoryUrl);

        assertTrue(nodeName.indexOf('/') < 0, "a node name may not contain a path separator");
        assertTrue(nodeName.length() <= 80, "a node name is limited to 80 characters, was " + nodeName.length());
    }

    @Test
    void derivedNodeNameIsStableAcrossCalls() {
        String repositoryUrl = "https://nexus.example.org/repository/public/";

        assertEquals(KometPreferencesCredentialStore.nodeName(repositoryUrl),
                KometPreferencesCredentialStore.nodeName(repositoryUrl));
    }
}
