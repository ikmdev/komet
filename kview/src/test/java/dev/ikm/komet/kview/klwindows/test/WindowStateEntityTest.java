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
package dev.ikm.komet.kview.klwindows.test;

import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesWrapper;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A window's state stores the entity it shows by UUID and never by nid, and a restored window
 * finds its entity through that UUID ({@code IKE-Network/ike-issues#1171}).
 *
 * <p>A nid is local to one knowledge base, so it never belongs in preferences: read against
 * another knowledge base, it names whatever component has that number there.
 */
class WindowStateEntityTest {

    private Preferences testRootNode;
    private KometPreferences preferences;

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("window-state-entity-test").toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void setUp() {
        testRootNode = Preferences.userRoot().node("ike-test/" + getClass().getSimpleName() + "-" + UUID.randomUUID());
        preferences = new PreferencesWrapper(testRootNode);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        testRootNode.removeNode();
        testRootNode.flush();
    }

    @Test
    void theEntityIsStoredByUuidAndNoNidIsWritten() throws BackingStoreException {
        UUID uuid = uuidOf(KernelConcept.ENGLISH_LANGUAGE);
        String nid = Long.toString(KernelConcept.ENGLISH_LANGUAGE.nid());

        assertTrue(stateFor(uuid).saveToPreferences(preferences));

        assertEquals(Optional.of(uuid.toString()), preferences.get(EntityKlWindowState.ENTITY_UUID));
        for (String key : testRootNode.keys()) {
            assertNotEquals(nid, testRootNode.get(key, null), "no nid is written, but " + key + " holds one");
        }
    }

    @Test
    void aRestoredWindowFindsItsEntityThroughTheUuid() {
        long nid = KernelConcept.ENGLISH_LANGUAGE.nid();
        stateFor(uuidOf(KernelConcept.ENGLISH_LANGUAGE)).saveToPreferences(preferences);

        EntityKlWindowState restored = EntityKlWindowState.fromPreferences(preferences);

        assertEquals(OptionalLong.of(nid), restored.resolveEntityNid());
    }

    @Test
    void anyOfTheEntitysUuidsFindsIt() {
        long nid = KernelConcept.ENGLISH_LANGUAGE.nid();
        for (UUID any : KernelConcept.ENGLISH_LANGUAGE.publicId().asUuidArray()) {
            stateFor(any).saveToPreferences(preferences);

            assertEquals(OptionalLong.of(nid), EntityKlWindowState.fromPreferences(preferences).resolveEntityNid(),
                    "restored through " + any);
        }
    }

    @Test
    void aUuidTheKnowledgeBaseDoesNotHoldResolvesToNothingAndIsAssignedNoNid() {
        UUID unknown = UUID.randomUUID();
        stateFor(unknown).saveToPreferences(preferences);

        EntityKlWindowState restored = EntityKlWindowState.fromPreferences(preferences);

        assertTrue(restored.resolveEntityNid().isEmpty(), "the window restores with no entity");
        assertFalse(PrimitiveData.get().hasUuid(unknown),
                "looking for the entity must not assign a nid to a UUID the knowledge base does not hold");
    }

    @Test
    void aWindowWithNoEntityResolvesToNothing() {
        stateFor(null).saveToPreferences(preferences);

        EntityKlWindowState restored = EntityKlWindowState.fromPreferences(preferences);

        assertTrue(restored.resolveEntityNid().isEmpty());
        assertTrue(preferences.get(EntityKlWindowState.ENTITY_UUID).isEmpty());
    }

    /** A concept window's state for the entity with the given UUID; {@code null} for no entity. */
    private static EntityKlWindowState stateFor(UUID entityUuid) {
        return EntityKlWindowState.builder()
                .windowId(UUID.randomUUID())
                .windowType(EntityKlWindowTypes.CONCEPT)
                .position(10, 20)
                .size(300, 400)
                .entityUuid(entityUuid)
                .build();
    }

    /** The UUID a window stores for its entity: the least of the entity's UUIDs. */
    private static UUID uuidOf(EntityFacade facade) {
        return facade.publicId().leastUuid();
    }
}
