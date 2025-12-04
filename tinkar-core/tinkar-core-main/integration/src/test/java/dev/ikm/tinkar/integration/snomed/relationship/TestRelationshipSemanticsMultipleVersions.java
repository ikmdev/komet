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
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.integration.KeyValueProviderExtension;
import dev.ikm.tinkar.integration.OpenSpinedArrayKeyValueProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.RELATIONSHIP_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.openSession;
import static dev.ikm.tinkar.integration.snomed.relationship.SnomedCTRelationshipSemantic.createRelationshipSemantics;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(OpenSpinedArrayKeyValueProvider.class)
public class TestRelationshipSemanticsMultipleVersions {

    @Test
    @DisplayName("Test Relationship Semantic with multiple versions for Versions")
    @Order(1)
    public void testCreateRelationshipSemanticVersions(){
        openSession((mockedStaticEntity) -> {
            List<SemanticRecord> semanticRecords =  createRelationshipSemantics(this,"sct2_Relationship_Full_US1000124_20220901_5.txt", RELATIONSHIP_PATTERN);
            SemanticRecord semanticRecord = semanticRecords.get(0);
            assertEquals(2, semanticRecord.versions().size(), "Has more than one row");
        });
    }

    @Test
    @DisplayName("Test Relationship Semantic with multiple versions for Data.")
    @Order(2)
    public void testCreateRelationshipSemantic(){
        openSession((mockedStaticEntity) -> {
            List<SemanticRecord> semanticRecords =  createRelationshipSemantics(this,"sct2_Relationship_Full_US1000124_20220901_5.txt", RELATIONSHIP_PATTERN);
            SemanticRecord semanticRecord = semanticRecords.get(0);
            SemanticVersionRecord semanticVersionRecord0 = semanticRecord.versions().get(0);
            SemanticVersionRecord semanticVersionRecord1 = semanticRecord.versions().get(1);
            assertTrue((semanticVersionRecord0.stampNid() != semanticVersionRecord1.stampNid()), "The Stamp versions are same");
            assertEquals(semanticVersionRecord0.chronology().patternNid(), semanticVersionRecord1.chronology().patternNid(),"Chronology mismatch");

            Object [] fieldValues0 = (Object[]) semanticVersionRecord0.fieldValues().get(0);
            Object [] fieldValues1 = (Object[]) semanticVersionRecord1.fieldValues().get(0);
            assertArrayEquals(fieldValues0 , fieldValues1);
        });
    }
}
