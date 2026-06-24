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

import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.input.DataFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.OptionalInt;

import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_CONCEPT_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_PATTERN_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_SEMANTIC_PROXY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the eager {@link KometClipboard#forComponent(int)} builder against the Tinkar
 * starter data (ike-issues#638). Verifies the centralized base-type matrix: every component advertises
 * its <em>actual</em> base type, and any concept-referencing component <em>also</em> advertises the
 * resolved concept proxy — so a concept drop target always finds a concept and a description-aware
 * target still sees the description, with no per-target conversion.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KometClipboardComponentITestFX {

    private static final File TEST_DATA_DIR = new File("target/data");
    private static final File PB_STARTER_DATA = new File(TEST_DATA_DIR, "tinkar-starter-data-reasoned-pb.zip");

    @BeforeAll
    void setupDatabase() {
        assertTrue(PB_STARTER_DATA.exists(),
                "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath());
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        long count = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute().getTotalCount();
        assertTrue(count > 0, "Should load entities from the starter-data protobuf file");
    }

    /** The nid the clipboard's {@code format} proxy decodes to, or empty when the format is absent. */
    private static OptionalInt nidOf(KometClipboard content, DataFormat format) {
        return content.containsKey(format)
                ? KometClipboard.nidFromProxyXml((String) content.get(format))
                : OptionalInt.empty();
    }

    @Test
    void aConceptAdvertisesItsConceptProxyAndNoSemantic() {
        int conceptNid = TinkarTerm.ENGLISH_LANGUAGE.nid();
        KometClipboard content = KometClipboard.forComponent(conceptNid);

        assertEquals(OptionalInt.of(conceptNid), nidOf(content, KOMET_CONCEPT_PROXY),
                "a concept advertises its concept proxy");
        assertFalse(content.containsKey(KOMET_SEMANTIC_PROXY), "a concept is not a semantic");
        assertTrue(content.containsKey(DataFormat.PLAIN_TEXT), "and carries a plain-text fallback");
    }

    @Test
    void aDescriptionAdvertisesBothItsSemanticProxyAndTheResolvedConcept() {
        int conceptNid = TinkarTerm.ENGLISH_LANGUAGE.nid();
        int[] descriptionNids = EntityService.get().semanticNidsForComponentOfPattern(
                conceptNid, TinkarTerm.DESCRIPTION_PATTERN.nid());
        assertTrue(descriptionNids.length > 0, "English Language must carry description semantics");
        int descriptionNid = descriptionNids[0];

        KometClipboard content = KometClipboard.forComponent(descriptionNid);

        assertEquals(OptionalInt.of(descriptionNid), nidOf(content, KOMET_SEMANTIC_PROXY),
                "a description advertises itself (the semantic) for a description-aware target");
        assertEquals(OptionalInt.of(conceptNid), nidOf(content, KOMET_CONCEPT_PROXY),
                "and the resolved concept it describes, so a concept drop target finds a concept");
    }

    @Test
    void aPatternAdvertisesItsPatternProxyButNoConcept() {
        int patternNid = TinkarTerm.DESCRIPTION_PATTERN.nid();
        KometClipboard content = KometClipboard.forComponent(patternNid);

        assertEquals(OptionalInt.of(patternNid), nidOf(content, KOMET_PATTERN_PROXY),
                "a pattern advertises its pattern proxy");
        assertFalse(content.containsKey(KOMET_CONCEPT_PROXY),
                "a pattern is not a concept and references none, so carries no concept proxy");
    }
}
