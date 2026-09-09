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
package dev.ikm.komet.kview.mvvm.view.genpurpose.test;

import dev.ikm.komet.kview.mvvm.view.genpurpose.StatedDefinitionSeeds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The seeded stated definitions. The ephemeral store is only there so the term nids in the
 * trees resolve.
 */
class StatedDefinitionSeedsTest {

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("stated-definition-seeds-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    @DisplayName("The necessary seed holds one necessary set and no sufficient set")
    void necessarySeedHoldsANecessarySet() {
        DiTreeEntity definition = StatedDefinitionSeeds.seedDefinition(true);

        assertTrue(definition.containsVertexWithMeaning(TinkarTerm.NECESSARY_SET));
        assertFalse(definition.containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET));
    }

    @Test
    @DisplayName("The sufficient seed holds one sufficient set and no necessary set")
    void sufficientSeedHoldsASufficientSet() {
        DiTreeEntity definition = StatedDefinitionSeeds.seedDefinition(false);

        assertTrue(definition.containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET));
        assertFalse(definition.containsVertexWithMeaning(TinkarTerm.NECESSARY_SET));
    }

    @Test
    @DisplayName("Either seed's set is an is-a to the anonymous placeholder concept")
    void seedsPointAtTheAnonymousConcept() {
        assertTrue(StatedDefinitionSeeds.seedDefinition(true).containsVertexWithMeaning(TinkarTerm.CONCEPT_REFERENCE));
        assertTrue(StatedDefinitionSeeds.seedDefinition(false).containsVertexWithMeaning(TinkarTerm.CONCEPT_REFERENCE));
    }
}
