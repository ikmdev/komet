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
}
