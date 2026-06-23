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
package dev.ikm.komet.framework.controls;

import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ComponentKindResolver} against the Tinkar starter data, per the
 * coordinate-behaviour testing discipline (resolve real components, don't mock). Confirms the four
 * atoms map from the entity type, that a semantic on the view coordinate's description pattern is
 * {@link ComponentKind#DESCRIPTION} (and a plain {@link ComponentKind#SEMANTIC} otherwise), and that
 * an unresolvable id is {@link ComponentKind#UNKNOWN} — never silently a concept (ike-issues#638).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentKindResolverITestFX {

    private static final File TEST_DATA_DIR = new File("target/data");
    private static final File PB_STARTER_DATA = new File(TEST_DATA_DIR, "tinkar-starter-data-reasoned-pb.zip");

    private ViewCalculator calculator;

    @BeforeAll
    void setupDatabase() {
        assertTrue(PB_STARTER_DATA.exists(),
                "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath());
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        long count = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute().getTotalCount();
        assertTrue(count > 0, "Should load entities from the starter-data protobuf file");
        calculator = Calculators.View.Default();
    }

    @Test
    void aConceptResolvesToConcept() {
        assertEquals(ComponentKind.CONCEPT,
                ComponentKindResolver.resolve(TinkarTerm.ENGLISH_LANGUAGE.nid(), calculator));
    }

    @Test
    void aPatternResolvesToPattern() {
        assertEquals(ComponentKind.PATTERN,
                ComponentKindResolver.resolve(TinkarTerm.DESCRIPTION_PATTERN.nid(), calculator));
    }

    @Test
    void aDescriptionSemanticResolvesToDescriptionViaTheCoordinate() {
        int[] descriptionNids = EntityService.get().semanticNidsForComponentOfPattern(
                TinkarTerm.ENGLISH_LANGUAGE.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid());
        assertTrue(descriptionNids.length > 0, "English Language must carry description semantics");
        int descriptionNid = descriptionNids[0];

        assertEquals(ComponentKind.DESCRIPTION, ComponentKindResolver.resolve(descriptionNid, calculator),
                "a semantic on the coordinate's description pattern is a Description");
        // Without a view the coordinate cannot be asked, so it falls back to a plain Semantic — never
        // a wrong guess.
        assertEquals(ComponentKind.SEMANTIC, ComponentKindResolver.resolve(descriptionNid, null),
                "with no calculator a description is a plain Semantic, not a misclassification");
    }

    @Test
    void aNonDescriptionSemanticResolvesToSemantic() {
        int otherSemanticNid = Integer.MIN_VALUE;
        outer:
        for (ConceptFacade concept : new ConceptFacade[]{
                TinkarTerm.ENGLISH_LANGUAGE, TinkarTerm.DEVELOPMENT_PATH, TinkarTerm.USER}) {
            for (int semanticNid : EntityService.get().semanticNidsForComponent(concept.nid())) {
                Entity<?> entity = EntityHandle.get(semanticNid).entity().orElse(null);
                if (entity instanceof SemanticEntity<?> semantic
                        && semantic.patternNid() != TinkarTerm.DESCRIPTION_PATTERN.nid()) {
                    otherSemanticNid = semanticNid;
                    break outer;
                }
            }
        }
        assertNotEquals(Integer.MIN_VALUE, otherSemanticNid,
                "starter data should carry a non-description semantic");
        assertEquals(ComponentKind.SEMANTIC, ComponentKindResolver.resolve(otherSemanticNid, calculator));
    }

    @Test
    void aStampResolvesToStamp() {
        Entity<?> englishLanguage = EntityHandle.get(TinkarTerm.ENGLISH_LANGUAGE.nid()).entity().orElseThrow();
        int stampNid = englishLanguage.versions().get(0).stampNid();
        assertEquals(ComponentKind.STAMP, ComponentKindResolver.resolve(stampNid, calculator));
    }

    @Test
    void anUnresolvableIdResolvesToUnknownNotConcept() {
        assertEquals(ComponentKind.UNKNOWN, ComponentKindResolver.resolve(Integer.MAX_VALUE, calculator),
                "an id that does not resolve must be UNKNOWN — the bare 'concept' default is never a fallback");
        assertEquals(ComponentKind.UNKNOWN, ComponentKindResolver.resolve((EntityFacade) null, calculator));
    }
}
