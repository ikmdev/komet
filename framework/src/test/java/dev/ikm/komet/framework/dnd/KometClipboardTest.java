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
package dev.ikm.komet.framework.dnd;

import dev.ikm.tinkar.terms.EntityProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;
import java.util.Set;

import static dev.ikm.komet.framework.dnd.KometClipboard.COMPONENT_DRAG_FORMAT;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_CONCEPT_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_PATTERN_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_SEMANTIC_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_STAMP_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_STAMP_VERSION_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.STAMP_TYPES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Store-independent contract for the typed-payload layer of {@link KometClipboard}
 * (Increment 1, IKE-Network/ike-issues#734). Exercises the parts that do not require a
 * populated datastore — atom→format mapping, the completed four-atom set including STAMP,
 * and the null/blank/malformed safety of the concept-proxy decoder. The behavioural
 * round-trip ({@code forConcept(nid)} → {@code conceptNid(dragboard)}) is store-dependent
 * (nids are assigned by the store) and is covered by a starter-data {@code *ITestFX}.
 */
@DisplayName("KometClipboard typed-payload contract (store-independent)")
class KometClipboardTest {

    @Nested
    @DisplayName("formatFor maps each atom proxy to its clipboard format")
    class FormatFor {

        @Test
        @DisplayName("concept proxy → concept format")
        void concept() {
            assertEquals(KOMET_CONCEPT_PROXY, KometClipboard.formatFor(EntityProxy.Concept.make(1)));
        }

        @Test
        @DisplayName("pattern proxy → pattern format")
        void pattern() {
            assertEquals(KOMET_PATTERN_PROXY, KometClipboard.formatFor(EntityProxy.Pattern.make(1)));
        }

        @Test
        @DisplayName("semantic proxy → semantic format")
        void semantic() {
            assertEquals(KOMET_SEMANTIC_PROXY, KometClipboard.formatFor(EntityProxy.Semantic.make(1)));
        }

        @Test
        @DisplayName("stamp proxy → stamp format")
        void stamp() {
            assertEquals(KOMET_STAMP_PROXY, KometClipboard.formatFor(EntityProxy.Stamp.make(1)));
        }

        @Test
        @DisplayName("bare proxy → component drag format")
        void bareProxyFallsBackToComponentFormat() {
            assertEquals(COMPONENT_DRAG_FORMAT, KometClipboard.formatFor(EntityProxy.make(1)));
        }
    }

    @Nested
    @DisplayName("STAMP completes the four atoms")
    class StampAtom {

        @Test
        @DisplayName("STAMP_TYPES is exactly the two stamp formats")
        void stampTypesAreTheTwoStampFormats() {
            assertTrue(STAMP_TYPES.contains(KOMET_STAMP_PROXY));
            assertTrue(STAMP_TYPES.contains(KOMET_STAMP_VERSION_PROXY));
            assertEquals(2, STAMP_TYPES.size());
        }

        @Test
        @DisplayName("the four atom proxy formats are distinct")
        void atomFormatsAreDistinct() {
            assertEquals(4, Set.of(KOMET_CONCEPT_PROXY, KOMET_PATTERN_PROXY,
                    KOMET_SEMANTIC_PROXY, KOMET_STAMP_PROXY).size());
        }
    }

    @Nested
    @DisplayName("nidFromProxyXml is null/blank/malformed-safe")
    class Decoder {

        @Test
        @DisplayName("null fragment → empty")
        void nullIsEmpty() {
            assertEquals(OptionalInt.empty(), KometClipboard.nidFromProxyXml(null));
        }

        @Test
        @DisplayName("blank fragment → empty")
        void blankIsEmpty() {
            assertEquals(OptionalInt.empty(), KometClipboard.nidFromProxyXml("   "));
        }

        @Test
        @DisplayName("unparseable fragment → empty (not thrown)")
        void malformedIsEmpty() {
            assertEquals(OptionalInt.empty(), KometClipboard.nidFromProxyXml("not-a-proxy"));
        }
    }
}
