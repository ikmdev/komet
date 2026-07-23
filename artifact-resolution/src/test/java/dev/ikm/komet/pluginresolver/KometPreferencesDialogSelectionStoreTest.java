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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KometPreferencesDialogSelectionStoreTest {

    private Preferences testRootNode;
    private KometPreferencesDialogSelectionStore store;

    @BeforeEach
    void setUp() {
        testRootNode = Preferences.userRoot().node("ike-test/" + getClass().getSimpleName() + "-" + UUID.randomUUID());
        KometPreferences userPreferences = new PreferencesWrapper(testRootNode);
        store = new KometPreferencesDialogSelectionStore(userPreferences);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        testRootNode.removeNode();
        testRootNode.flush();
    }

    @Test
    void getReturnsEmptyWhenNothingRemembered() {
        assertTrue(store.get("SA").isEmpty());
    }

    @Test
    void putThenGetRoundTripsSelection() {
        DialogSelection selection = new DialogSelection("https://nexus.tinkar.org/repository/ike-restricted/");

        store.put("SA", selection);

        assertEquals(Optional.of(selection), store.get("SA"));
    }

    @Test
    void putOverwritesPreviouslyRememberedSelection() {
        store.put("SA", new DialogSelection("https://first.example.com/"));
        store.put("SA", new DialogSelection("https://second.example.com/"));

        assertEquals("https://second.example.com/", store.get("SA").orElseThrow().repositoryUrl());
    }

    @Test
    void differentFlowKeysAreRememberedIndependently() {
        store.put("SA", new DialogSelection("https://sa.example.com/"));
        store.put("PB", new DialogSelection("https://pb.example.com/"));

        assertEquals("https://sa.example.com/", store.get("SA").orElseThrow().repositoryUrl());
        assertEquals("https://pb.example.com/", store.get("PB").orElseThrow().repositoryUrl());
    }
}
