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
package dev.ikm.tinkar.integration.snomed.description;

import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.MockDataType.ENTITYREF;
import static dev.ikm.tinkar.integration.snomed.core.MockEntity.getNid;
import static dev.ikm.tinkar.integration.snomed.core.MockEntity.populateMockData;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DESCRIPTION_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.IDENTIFIER_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_IDENTIFIER;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.loadSnomedFile;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.openSession;
import static dev.ikm.tinkar.integration.snomed.description.SnomedToEntityIdentifier.ID_INDEX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Stale")
public class SnomedToEntityIdentifierIT {

    SnomedToEntityIdentifier transformer;

    @BeforeEach
    public void beforeEach() {
        transformer = new SnomedToEntityIdentifier();
    }

    @Test
    @DisplayName("Creating identifier semantic from test file 3")
    public void createIndentifierSemanticFromFile(){
        // Given a snomed test file
        List<String> rows = loadSnomedFile(this.getClass(),"sct2_Description_Full-en_US1000124_20220901_3.txt");

        //When Creating an Identifier Semantic
        //Then the created Identifier Semantic should match expected values
        testAndCompareTransformation(rows);
    }


    private void testAndCompareTransformation(List<String> rows) {
        openSession((mockStaticEntityService, starterData) -> {

            //Create Description Semantic for every row of test file
            for (String row : rows) {

                //Create expected values and populate entity service with Nids
                String[] values = row.split("\t");
                UUID expectedStampUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, row.replaceAll("\t",""));
                UUID expectedPatternUUID = IDENTIFIER_PATTERN;
                UUID expectedSemanticUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE,expectedPatternUUID.toString()+values[ID_INDEX]);
                UUID expectedReferencedComponentUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE,DESCRIPTION_PATTERN.toString()+values[ID_INDEX]);


                populateMockData(expectedStampUUID.toString(), ENTITYREF);
                populateMockData(expectedPatternUUID.toString(), ENTITYREF);
                populateMockData(expectedSemanticUUID.toString(),ENTITYREF);
                populateMockData(expectedReferencedComponentUUID.toString(), ENTITYREF);

                //Create expected field values for SemanticVersionRecord
                Object[] expectedFields = {
                        values[ID_INDEX],
                        getNid(SNOMED_CT_IDENTIFIER)};


                //Create actual semantic description
                SemanticRecord actualRecord = transformer.createIdentifierSemantic(row);

                //Check SemanticRecord values against expected
                assertEquals(expectedSemanticUUID.getMostSignificantBits(), actualRecord.mostSignificantBits(), "SemanticRecord most significant bits do not match expected");
                assertEquals(expectedSemanticUUID.getLeastSignificantBits(), actualRecord.leastSignificantBits(), "SemanticRecord least significant bits do not match expected");
                assertEquals(getNid(expectedSemanticUUID), actualRecord.nid(), "SemanticRecord nid does not match expected");
                assertEquals(getNid(expectedPatternUUID), actualRecord.patternNid(), "SemanticRecord most patternNid does not match expected");
                assertEquals(getNid(expectedReferencedComponentUUID), actualRecord.referencedComponentNid(), "SemanticRecord referencedComponentNid does not match expected");
                assertTrue(actualRecord.versions().size() > 0, "No SemanticRecordVersions");


                SemanticVersionRecord actualVersionRecord = actualRecord.versions().get(0);
                assertEquals(actualVersionRecord.stampNid(), getNid(expectedStampUUID));

                //Check field values to expected
                assertEquals(expectedFields[0], actualVersionRecord.fieldValues().get(0), "SemanticVersionRecord id does not match expected");
                assertEquals(expectedFields[1], actualVersionRecord.fieldValues().get(1), "SemanticVersionRecord Identifier Nid does not match expected");
            }
        });
    }

}
