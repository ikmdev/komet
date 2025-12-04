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
package dev.ikm.tinkar.entity.transform;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.schema.FieldDefinition;
import dev.ikm.tinkar.schema.PatternChronology;
import dev.ikm.tinkar.schema.PatternVersion;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.StampVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled("Java 23")
public class TestProtobufToEntityPatternTransform {
    private PatternEntity mockPatternEntity;


    @BeforeAll
    public void init() {
    }
    //TODO - Add unit tests for variations of Pattern Version transformation

    @Test
    @DisplayName("Transform a Pattern Chronology With Zero Versions Present")
    public void patternChronologyTransformWithZeroVersion() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBPatternChronology with no Pattern Versions present
            PatternChronology pbPatternChronology = PatternChronology.newBuilder()
                    .setPublicId(createPBPublicId(conceptMap.get(TEST_CONCEPT_NAME)))
                    .build();

            // When we transform PBPatternChronology

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformPatternChronology(pbPatternChronology), "Not allowed to have no pattern versions.");
        });

    }
    @Test
    @DisplayName("Transform a Pattern Chronology With One Version Present")
    public void patternChronologyTransformWithOneVersion(){
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBPatternChronology with a pattern version present
            Concept statusConcept = conceptMap.get(STATUS_CONCEPT_NAME);
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            Concept meaningConcept = conceptMap.get(MEANING_CONCEPT_NAME);
            Concept dataTypeConcept = conceptMap.get(DATATYPE_CONCEPT_NAME);
            Concept purposeConcept = conceptMap.get(PURPOSE_CONCEPT_NAME);
            Concept referencedComponentPurposeConcept = conceptMap.get(REF_COMP_PURPOSE_CONCEPT_NAME);
            Concept referencedComponentMeaningConcept = conceptMap.get(REF_COMP_MEANING_CONCEPT_NAME);

            StampVersion pbStampVersionOne = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(statusConcept))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(authorConcept))
                    .setModulePublicId(createPBPublicId(moduleConcept))
                    .setPathPublicId(createPBPublicId(pathConcept))
                    .build();

            StampChronology pbStampChronologyOne = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
                    //.addVersions(pbStampVersionOne)
                    .build();

            FieldDefinition pbFieldDefinitionOne = FieldDefinition.newBuilder()
                    .setMeaningPublicId(createPBPublicId(meaningConcept))
                    .setDataTypePublicId(createPBPublicId(dataTypeConcept))
                    .setPurposePublicId(createPBPublicId(purposeConcept))
                    .build();

            PatternVersion pbPatternVersionOne = PatternVersion.newBuilder()
                    .setStampChronologyPublicId(createPBPublicId(testConcept.publicId()))
                    .setReferencedComponentPurposePublicId(createPBPublicId(referencedComponentPurposeConcept))
                    .setReferencedComponentMeaningPublicId(createPBPublicId(referencedComponentMeaningConcept))
                    .addFieldDefinitions(pbFieldDefinitionOne)
                    .build();

            PatternChronology pbPatternChronologyOne = PatternChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
                    .addPatternVersions(pbPatternVersionOne)
                    .build();

            // When we transform PBPatternChronology
            PatternEntity actualPatternChronologyOne = TinkarSchemaToEntityTransformer.getInstance().transformPatternChronology(pbPatternChronologyOne);

            // Then the resulting PatternChronology should match the original PBPatternChronology
            assertEquals(nid(testConcept), actualPatternChronologyOne.nid(), "Nid's did not match in Pattern Chronology.");
            assertTrue(PublicId.equals(testConcept.publicId(), actualPatternChronologyOne.publicId()), "Public Id's of the pattern chronology do not match.");
            assertEquals(1, actualPatternChronologyOne.versions().size(), "Versions are empty");
        });


    }

    @Test
    @DisplayName("Transform a Pattern Chronology With Two Versions Present")
    public void patternChronologyTransformWithTwoVersions() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBPatternChronology with two pattern versions present
            Concept statusConcept = conceptMap.get(STATUS_CONCEPT_NAME);
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            Concept meaningConcept = conceptMap.get(MEANING_CONCEPT_NAME);
            Concept dataTypeConcept = conceptMap.get(DATATYPE_CONCEPT_NAME);
            Concept purposeConcept = conceptMap.get(PURPOSE_CONCEPT_NAME);
            Concept referencedComponentPurposeConcept = conceptMap.get(REF_COMP_PURPOSE_CONCEPT_NAME);
            Concept referencedComponentMeaningConcept = conceptMap.get(REF_COMP_MEANING_CONCEPT_NAME);

            StampVersion pbStampVersionTwo = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(statusConcept))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(authorConcept))
                    .setModulePublicId(createPBPublicId(moduleConcept))
                    .setPathPublicId(createPBPublicId(pathConcept))
                    .build();

            StampChronology pbStampChronologyTwo = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
//                    .addVersions(pbStampVersionTwo)
                    .build();

            FieldDefinition pbFieldDefinitionTwo = FieldDefinition.newBuilder()
                    .setMeaningPublicId(createPBPublicId(meaningConcept))
                    .setDataTypePublicId(createPBPublicId(dataTypeConcept))
                    .setPurposePublicId(createPBPublicId(purposeConcept))
                    .build();

            StampVersion pbStampVersionOne = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(statusConcept))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(authorConcept))
                    .setModulePublicId(createPBPublicId(moduleConcept))
                    .setPathPublicId(createPBPublicId(pathConcept))
                    .build();

            StampChronology pbStampChronologyOne = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
//                    .addVersions(pbStampVersionOne)
                    .build();

            FieldDefinition pbFieldDefinitionOne = FieldDefinition.newBuilder()
                    .setMeaningPublicId(createPBPublicId(meaningConcept))
                    .setDataTypePublicId(createPBPublicId(dataTypeConcept))
                    .setPurposePublicId(createPBPublicId(purposeConcept))
                    .build();

            PatternVersion pbPatternVersionOne = PatternVersion.newBuilder()
                    .setStampChronologyPublicId(createPBPublicId(testConcept))
                    .setReferencedComponentPurposePublicId(createPBPublicId(referencedComponentPurposeConcept))
                    .setReferencedComponentMeaningPublicId(createPBPublicId(referencedComponentMeaningConcept))
                    .addFieldDefinitions(pbFieldDefinitionOne)
                    .build();

            PatternVersion pbPatternVersionTwo = PatternVersion.newBuilder()
                    .setStampChronologyPublicId(createPBPublicId(testConcept))
                    .setReferencedComponentPurposePublicId(createPBPublicId(referencedComponentPurposeConcept))
                    .setReferencedComponentMeaningPublicId(createPBPublicId(referencedComponentMeaningConcept))
                    .addFieldDefinitions(pbFieldDefinitionTwo)
                    .addFieldDefinitions(pbFieldDefinitionTwo)
                    .build();

            PatternChronology pbPatternChronologyOne = PatternChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
                    .addPatternVersions(pbPatternVersionOne)
                    .addPatternVersions(pbPatternVersionTwo)
                    .build();

            // When we transform PBPatternChronology
            PatternEntity actualPatternChronologyOne = TinkarSchemaToEntityTransformer.getInstance().transformPatternChronology(pbPatternChronologyOne);

            // Then the resulting PatternChronology should match the original PBPatternChronology
            assertEquals(nid(testConcept), actualPatternChronologyOne.nid(), "Nid's did not match in Pattern Chronology.");
            assertTrue(PublicId.equals(testConcept.publicId(), actualPatternChronologyOne.publicId()), "Public Id's of the pattern chronology do not match.");
            assertEquals(2, actualPatternChronologyOne.versions().size(), "Versions are empty");
        });
    }
}
