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
package dev.ikm.tinkar.integration.snomed.relationship;

import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.integration.KeyValueProviderExtension;
import dev.ikm.tinkar.integration.OpenSpinedArrayKeyValueProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static dev.ikm.tinkar.integration.snomed.core.MockEntity.getNid;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.RELATIONSHIP_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.RELATIONSHIP_PATTERN_UUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.openSession;
import static dev.ikm.tinkar.integration.snomed.relationship.SnomedCTRelationshipSemantic.createRelationshipSemantics;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(OpenSpinedArrayKeyValueProvider.class)
public class TestRelationshipSemantics {

    @Test
    @DisplayName("Test Relationship Semantic with active for Snomed to Entity Relationship.")
    @Order(1)
    public void testCreateRelationshipSemantic(){
        openSession((mockedStaticEntity) -> {
            List<SemanticRecord> semanticRecords =  createRelationshipSemantics(this,"sct2_Relationship_Full_US1000124_20220901_1.txt", RELATIONSHIP_PATTERN);
            SemanticRecord semanticRecord = semanticRecords.get(0);
            assertEquals(1, semanticRecord.versions().size(), "Has more than one row");
        });
    }

    @Test
    @DisplayName("Test Relationship Semantic For Identifier pattern.")
    @Order(2)
    public void testReltionshipPatternIdentifierSemantic(){
        openSession((mockedStaticEntity) -> {
            List<SemanticRecord> semanticRecords =  createRelationshipSemantics(this,"sct2_Relationship_Full_US1000124_20220901_1.txt", RELATIONSHIP_PATTERN);
            SemanticRecord semanticRecord = semanticRecords.get(0);
            assertEquals(getNid(RELATIONSHIP_PATTERN_UUID), semanticRecord.patternNid(), "Has more than one row");
        });
   }
}
