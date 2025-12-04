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
package dev.ikm.tinkar.integration.snomed.concept;

import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.integration.snomed.core.MockEntity;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.concept.SnomedTextToConcept.*;
import static dev.ikm.tinkar.integration.snomed.core.MockEntity.getNid;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.ACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.INACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_AUTHOR;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_TEXT_MODULE_ID;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.loadSnomedFile;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.openSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Stale")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SnomedTextToConceptIT {

    @Test
    @Order(1)
    @DisplayName("Test for one row of inactive stamp data in Concept File")
    public void testStampWithOneActiveRow() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> row = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_1.txt");
            StampRecord record = createStampChronology(row.get(0));
            assertEquals(1, record.versions().size(), "Has more than one row");
        });
    }

    @Test
    @Order(2)
    @DisplayName("Test for Active stamp in Concept File")
    public void testStampWithActiveTransformResult() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_1.txt");

            StampRecord record = createStampChronology(rows.get(0));
            UUID testStampUUID = getStampUUID(rows.get(0));

            // Assert that stamp is Active
            assertEquals(getNid(ACTIVE), record.stateNid(), "State is not active");
            assertEquals(getNid(SNOMED_CT_AUTHOR), record.authorNid(), "Author couldn't be referenced");
            assertEquals(getNid(DEVELOPMENT_PATH), record.pathNid(), "Path could not be referenced");
            assertEquals(getNid(SNOMED_TEXT_MODULE_ID), record.moduleNid(), "Module could ot be referenced");
            assertEquals(getNid(testStampUUID), record.nid(), "Stamp UUID was not populated");
        });

    }

    @Test
    @Order(3)
    @DisplayName("Test for one row of inactive stamp data in Concept File")
    public void testStampWithOneInactiveRow() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> row = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_2.txt");
            StampRecord record = createStampChronology(row.get(0));
            assertEquals(1, record.versions().size(), "Has more than one row");
        });
    }

    @Test
    @Order(4)
    @DisplayName("Test for Inactive stamp in Concept File")
    public void testStampWithInactiveTransformResult() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_2.txt");
            StampRecord record = createStampChronology(rows.get(0));
            UUID testStampUUID = getStampUUID(rows.get(0));

            // Assert that stamp is Active
            assertEquals(getNid(INACTIVE), record.stateNid(), "State is active");
            assertEquals(getNid(SNOMED_CT_AUTHOR), record.authorNid(), "Author couldn't be referenced");
            assertEquals(getNid(DEVELOPMENT_PATH), record.pathNid(), "Path could not be referenced");
            assertEquals(getNid(SNOMED_TEXT_MODULE_ID), record.moduleNid(), "Module could ot be referenced");
            assertEquals(getNid(testStampUUID), record.nid(), "Stamp " + testStampUUID + " UUID was not populated");
        });

    }

    @Test
    @Order(5)
    @DisplayName("Test for Concept with single version record")
    public void testConceptWithSingleVersion() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_3.txt");
            ConceptRecord conceptRecord = createConceptChronology(rows.get(0));
            ImmutableList<ConceptVersionRecord> conceptVersionsRecord = conceptRecord.versions();

            UUID testConceptUUID = getConceptUUID(rows.get(0));

            // Assert that stamp is Active
            assertEquals(1, conceptVersionsRecord.size(), "More than 1 concept versions exist");
            assertEquals(getNid(testConceptUUID), conceptRecord.nid(), "Concept " + testConceptUUID + " UUID was not populated");
        });

    }

    @Test
    @Order(6)
    @DisplayName("Test for Concept version record")
    public void testConceptVersions() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_3.txt");
            ConceptRecord conceptRecord = createConceptChronology(rows.get(0));
            ImmutableList<ConceptVersionRecord> conceptVersionsRecord = conceptRecord.versions();
            ConceptVersionRecord firstConceptVersionsRecord = conceptRecord.versions().get(0);

            UUID testStampUUID = getStampUUID(rows.get(0));
            UUID testConceptUuid = getConceptUUID(rows.get(0));

            assertEquals(getNid(testConceptUuid), conceptRecord.nid(), "Concept " + testConceptUuid + " UUID was not populated");
            assertEquals(getNid(testStampUUID), firstConceptVersionsRecord.stampNid(), "Stamp " + testStampUUID + " is not associated with given concept version");
            assertEquals(conceptRecord, firstConceptVersionsRecord.chronology(), "Concept was not referenced correctly in concept version record.");
        });
    }

    @Test
    @Order(7)
    @DisplayName("Test for Concept with multiple version concept record")
    public void testForSingleConceptMultipleVersion() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_4.txt");
            List<ConceptRecord> conceptRecord = createConceptFromMultipleVersions(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_4.txt");

            assertTrue(rows.size() > 1, "File with single or no rows exist");
            assertTrue(conceptRecord.size() == 1, "File with more than one concept exist");
        });

    }

    @Test
    @Order(8)
    @DisplayName("Test for ConceptVersion to refer to same parent concept")
    public void testConceptWithMultipleVersionofSameConcept() {
        openSession((mockStaticEntityService, starterData) -> {
            List<ConceptRecord> conceptRecord = createConceptFromMultipleVersions(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_4.txt");
            ConceptRecord singleConcept = conceptRecord.get(0);
            ImmutableList<ConceptVersionRecord> conceptVersionsRecord = singleConcept.versions();

            assertTrue(conceptVersionsRecord.size() == 2 , "0 or 1  concept version  exist");
            assertTrue(conceptVersionsRecord.get(0).chronology().equals(conceptVersionsRecord.get(1).chronology())  ,
                    "Both versions refer to different parent chronologies");
        });

    }

    @Test
    @Order(9)
    @DisplayName("Test for 8th text file to create multiple concepts with multiple version")
    public void testMultipleConceptsWithMultipleVersion() {
        openSession((mockStaticEntityService, starterData) -> {
            List<ConceptRecord> conceptRecord = createConceptFromMultipleVersions(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_8.txt");
            assertTrue(conceptRecord.size() > 1, "File with one concept exist");
        });

    }

    @Test
    @Order(10)
    @DisplayName("Test concept for identifier semantic")
    public void testConceptsWithIdentifierSemantic() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_5.txt");
            UUID identifierPatternUUID = getIdentifierPatternUUID();
            UUID identifierSemanticUUID = getIdentifierSemanticUUID(rows.get(0));
            UUID referenceComponentUUID = getReferenceComponentUUID(rows.get(0));

            // testing values
            SemanticRecord testIdentifierSemanticRecord = createConceptIdentifierSemantic(rows.get(0));

            assertTrue(rows.size() == 1, "File with no or more than one concept row exist");
            assertEquals(MockEntity.getNid(identifierSemanticUUID),testIdentifierSemanticRecord.nid(),  "Identifier Semantic nids doesnt match");
            assertEquals(MockEntity.getNid(identifierPatternUUID),testIdentifierSemanticRecord.patternNid(),  "Identifier Semantic pattern nids doesnt match");
            assertEquals(MockEntity.getNid(referenceComponentUUID),testIdentifierSemanticRecord.referencedComponentNid(),  "Identifier Semantic referencecomponent nids doesnt match");
        });
    }

    @Test
    @Order(11)
    @DisplayName("Test concept for single identifier semantic version")
    public void testConceptsWithIdentifierSemanticVersion() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_5.txt");
            UUID identifierPatternUUID = getIdentifierPatternUUID();
            UUID identifierSemanticUUID = getIdentifierSemanticUUID(rows.get(0));
            UUID referenceComponentUUID = getReferenceComponentUUID(rows.get(0));

            Integer expectedStampRecordNid = createStampChronology(rows.get(0)).nid();

            // testing values
            SemanticRecord testIdentifierSemanticRecord = createConceptIdentifierSemantic(rows.get(0));
            ImmutableList<SemanticVersionRecord> testSemanticVersionRecord = testIdentifierSemanticRecord.versions();

            assertTrue(testSemanticVersionRecord.size() == 1,  "No version or more than one version of identifier semantic pattern exist");
            assertEquals(expectedStampRecordNid,testSemanticVersionRecord.get(0).stampNid(),  "Doesnt point to same stamp entity");
            assertEquals(MockEntity.getNid(identifierPatternUUID),testSemanticVersionRecord.get(0).patternNid(),  "Do not reference expected parent pattern entity");
            assertEquals(MockEntity.getNid(identifierSemanticUUID),testSemanticVersionRecord.get(0).nid(),  "Do not reference expected semantic entity");
            assertEquals(MockEntity.getNid(referenceComponentUUID),testSemanticVersionRecord.get(0).referencedComponentNid(),  "Do not reference expected parent semantic entity");
        });

    }

    @Test
    @Order(12)
    @DisplayName("Test concept for definition status semantic")
    public void testConceptsWithDefinitionStatusSemantic() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_6.txt");
            UUID defintinitionStatusPatternUUID = getDefinitionStatusPatternUUID();
            UUID defintinitionStatusSemanticUUID = getDefinitionStatusSemanticUUID(rows.get(0));
            UUID referenceComponentUUID = getReferenceComponentUUID(rows.get(0));

            // testing values
            SemanticRecord testDefintinitionStatusSemanticRecord = createConceptDefinitionStatusSemantic(rows.get(0));

            assertTrue(rows.size() == 1, "File with no or more than one concept row exist");
            assertEquals(getNid(defintinitionStatusSemanticUUID),testDefintinitionStatusSemanticRecord.nid(),  "Definition Status Semantic nids doesnt match");
            assertEquals(getNid(defintinitionStatusPatternUUID),testDefintinitionStatusSemanticRecord.patternNid(),  "Definition Status Semantic pattern nids doesnt match");
            assertEquals(getNid(referenceComponentUUID),testDefintinitionStatusSemanticRecord.referencedComponentNid(),  "Definition Status Semantic referencecomponent nids doesnt match");
        });
    }

    @Test
    @Order(13)
    @DisplayName("Test concept for single definition status semantic version")
    public void testConceptsWithDefinitionStatusSemanticVersion() {
        openSession((mockStaticEntityService, starterData) -> {
            List<String> rows = loadSnomedFile(SnomedTextToConceptIT.class, "sct2_Concept_Full_US1000124_20220901_6.txt");
            UUID defintinitionStatusPatternUUID = getDefinitionStatusPatternUUID();
            UUID defintinitionStatusSemanticUUID = getDefinitionStatusSemanticUUID(rows.get(0));
            UUID referenceComponentUUID = getReferenceComponentUUID(rows.get(0));

            Integer expectedStampRecordNid = createStampChronology(rows.get(0)).nid();

            // testing values
            SemanticRecord testDefintinitionStatusSemanticRecord = createConceptDefinitionStatusSemantic(rows.get(0));
            ImmutableList<SemanticVersionRecord> testSemanticVersionRecord = testDefintinitionStatusSemanticRecord.versions();

            assertTrue(testSemanticVersionRecord.size() == 1,  "No version or more than one version of identifier semantic pattern exist");
            assertEquals(expectedStampRecordNid,testSemanticVersionRecord.get(0).stampNid(),  "Doesnt point to same stamp entity");
            assertEquals(MockEntity.getNid(defintinitionStatusPatternUUID),testSemanticVersionRecord.get(0).patternNid(),  "Do not reference expected parent pattern entity");
            assertEquals(MockEntity.getNid(defintinitionStatusSemanticUUID),testSemanticVersionRecord.get(0).nid(),  "Do not reference expected semantic entity");
            assertEquals(MockEntity.getNid(referenceComponentUUID),testSemanticVersionRecord.get(0).referencedComponentNid(),  "Do not reference expected parent semantic entity");
        });

    }

}
