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
package dev.ikm.komet.framework.search;

import dev.ikm.tinkar.terms.KernelTerm;
import dev.ikm.komet.framework.ComponentLookup;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.OptionalLong;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The identifier forms of a search query, and the form a search node stores
 * ({@code IKE-Network/ike-issues#1174}): a query that names components by nid is stored by UUID,
 * so no nid is kept in preferences, and the stored form finds the same components when it is
 * run again.
 *
 * <p>Runs against an ephemeral store. The remote branch of {@link ComponentLookup} — a
 * knowledge base served over gRPC, where a component not yet fetched is fetched to find out
 * whether it exists — needs a server and is not exercised here.
 */
class IdentifierQueryTest {

    /** A nid the store has assigned to no component: it numbers components up from the bottom of the int range. */
    private static final long UNASSIGNED_NID = -5;

    private static long englishNid;
    private static long languageNid;
    private static UUID english;
    private static UUID language;

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("identifier-query-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        englishNid = KernelTerm.ENGLISH_LANGUAGE.nid();
        languageNid = KernelTerm.LANGUAGE.nid();
        // The UUID storedForm writes for each: the least of its UUIDs (ENGLISH_LANGUAGE has three).
        english = KernelTerm.ENGLISH_LANGUAGE.publicId().leastUuid();
        language = KernelTerm.LANGUAGE.publicId().leastUuid();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    void textIsNotAnIdentifierQuery() {
        for (String text : new String[]{"aspirin", "73211009", "-abc", "- 42", "[a TO b]", "[hello]", "[]",
                "[1,,2]", "[" + english + ", aspirin]", "", "   "}) {
            assertTrue(IdentifierQuery.parse(text).isEmpty(), "a text query: " + text);
        }
        assertTrue(IdentifierQuery.parse(null).isEmpty());
    }

    @Test
    void aNegativeIntegerIsANid() {
        assertArrayEquals(new long[]{-42}, IdentifierQuery.parse("-42").orElseThrow().nids());
        assertArrayEquals(new long[]{englishNid}, IdentifierQuery.parse("  " + englishNid + " ").orElseThrow().nids(),
                "surrounding space is ignored");
    }

    @Test
    void aUuidFindsItsComponent() {
        assertArrayEquals(new long[]{englishNid}, IdentifierQuery.parse(english.toString()).orElseThrow().nids());
        for (UUID any : KernelTerm.ENGLISH_LANGUAGE.publicId().asUuidArray()) {
            assertArrayEquals(new long[]{englishNid}, IdentifierQuery.parse(any.toString()).orElseThrow().nids(),
                    "any of a component's UUIDs finds it: " + any);
        }
    }

    @Test
    void aUuidTheStoreDoesNotHoldFindsNothingAndIsAssignedNoNid() {
        UUID unknown = UUID.randomUUID();

        assertEquals(0, IdentifierQuery.parse(unknown.toString()).orElseThrow().nids().length);
        assertEquals(OptionalLong.empty(), ComponentLookup.nid(unknown));

        assertFalse(PrimitiveData.get().hasUuid(unknown),
                "looking a UUID up must not assign a nid to one the knowledge base does not hold");
    }

    @Test
    void aBracketedListMayHoldNidsAndUuidsInAnyOrder() {
        String query = "[" + languageNid + ", " + english + " ," + UUID.randomUUID() + "]";

        assertArrayEquals(new long[]{languageNid, englishNid}, IdentifierQuery.parse(query).orElseThrow().nids(),
                "in the order written, without the UUID the store does not hold");
        assertArrayEquals(new long[]{5, -7}, IdentifierQuery.parse("[5, -7]").orElseThrow().nids(),
                "inside brackets an integer of either sign is a nid");
    }

    @Test
    void aNidQueryIsStoredByUuid() {
        assertEquals(english.toString(), IdentifierQuery.storedForm(Long.toString(englishNid)));
        assertEquals("[" + english + ", " + language + "]",
                IdentifierQuery.storedForm("[" + englishNid + ", " + languageNid + "]"));
        assertEquals("[" + language + ", " + english + "]",
                IdentifierQuery.storedForm("[" + language + ", " + englishNid + "]"),
                "a UUID is stored as written, a nid as its component's UUID");
    }

    @Test
    void theStoredFormHoldsNoNidAndFindsTheSameComponents() {
        String typed = "[" + englishNid + ", " + languageNid + "]";
        String stored = IdentifierQuery.storedForm(typed);

        assertFalse(stored.contains(Long.toString(englishNid)), stored);
        assertFalse(stored.contains(Long.toString(languageNid)), stored);
        assertArrayEquals(IdentifierQuery.parse(typed).orElseThrow().nids(),
                IdentifierQuery.parse(stored).orElseThrow().nids(),
                "run again from preferences, the stored query finds what the typed one found");
        assertEquals(stored, IdentifierQuery.storedForm(stored), "storing it again changes nothing");
    }

    @Test
    void aNidThatNamesNothingIsNotStored() {
        assertEquals("", IdentifierQuery.storedForm(Long.toString(UNASSIGNED_NID)),
                "with nothing left, the query is stored as empty");
        assertEquals(english.toString(),
                IdentifierQuery.storedForm("[" + englishNid + ", " + UNASSIGNED_NID + "]"),
                "what does name a component is kept");
    }

    @Test
    void aTextQueryIsStoredAsItIs() {
        assertEquals("aspirin", IdentifierQuery.storedForm("aspirin"));
        assertEquals("[a TO b]", IdentifierQuery.storedForm("[a TO b]"));
        assertEquals("73211009", IdentifierQuery.storedForm("73211009"));
        assertNull(IdentifierQuery.storedForm(null));
    }

    @Test
    void aPublicIdTheStoreHoldsIsLookedUpToItsNid() {
        assertEquals(OptionalLong.of(englishNid), ComponentLookup.nid(english));
        assertEquals(OptionalLong.of(languageNid), ComponentLookup.nid(KernelTerm.LANGUAGE.publicId()));
    }
}
