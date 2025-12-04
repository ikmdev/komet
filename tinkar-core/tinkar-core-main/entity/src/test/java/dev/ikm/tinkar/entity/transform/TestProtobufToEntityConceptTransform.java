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
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.schema.ConceptChronology;
import dev.ikm.tinkar.schema.ConceptVersion;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.StampVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.AUTHOR_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.MODULE_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.PATH_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.STATUS_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.TEST_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.createPBPublicId;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.nid;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.nowTimestamp;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.openSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled("Java 23")
public class TestProtobufToEntityConceptTransform {

    @Test
    @DisplayName("Transform a Concept Chronology With Zero Public Id's")
    public void conceptChronologyTransformWithZeroPublicIds(){
        openSession(this, (mockedEntityService, conceptMap) -> {

            // Given a PBConceptChronology with a no Stamp Versions present
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);

            dev.ikm.tinkar.schema.StampVersion pbStampVersion = dev.ikm.tinkar.schema.StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(conceptMap.get(STATUS_CONCEPT_NAME)))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(conceptMap.get(AUTHOR_CONCEPT_NAME)))
                    .setModulePublicId(createPBPublicId(conceptMap.get(MODULE_CONCEPT_NAME)))
                    .setPathPublicId(createPBPublicId(conceptMap.get(PATH_CONCEPT_NAME)))
                    .build();

            StampChronology pbStampChronology = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
//                    .addVersions(pbStampVersion)
                    .build();

            ConceptVersion pbConceptVersion = ConceptVersion.newBuilder()
                    .setStampChronologyPublicId(createPBPublicId(testConcept))
                    .build();

            ConceptChronology pbConceptChronology = ConceptChronology.newBuilder()
                    .addConceptVersions(pbConceptVersion)
                    .build();

            // When we transform PBConceptChronology

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformConceptChronology(pbConceptChronology), "Not allowed to have no public id's.");
        });
    }

    @Test
    @DisplayName("Transform a Concept Chronology With Zero Versions")
    public void conceptChronologyTransformWithZeroVersion(){
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBConceptChronology with a no Stamp Versions present
            ConceptChronology pbConceptChronology = ConceptChronology.newBuilder()
                    .setPublicId(createPBPublicId(conceptMap.get(TEST_CONCEPT_NAME)))
                    .build();

            // When we transform PBConceptChronology

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformConceptChronology(pbConceptChronology), "Not allowed to have no stamp versions.");
        });
    }
    @Test
    @DisplayName("Transform a Concept Chronology With One Version")
    public void conceptChronologyTransformWithOneVersion(){
        openSession(this, (mockedEntity, conceptMap) -> {
            // Given a PBConceptChronology with a one Stamp Version present
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);

            StampVersion pbStampVersion = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(conceptMap.get(STATUS_CONCEPT_NAME)))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(conceptMap.get(AUTHOR_CONCEPT_NAME)))
                    .setModulePublicId(createPBPublicId(conceptMap.get(MODULE_CONCEPT_NAME)))
                    .setPathPublicId(createPBPublicId(conceptMap.get(PATH_CONCEPT_NAME)))
                    .build();

            StampChronology pbStampChronology = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
//                    .addVersions(pbStampVersion)
                    .build();

            ConceptVersion pbConceptVersion = ConceptVersion.newBuilder()
                    .setStampChronologyPublicId(createPBPublicId(testConcept))
                    .build();

            ConceptChronology pbConceptChronology = ConceptChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
                    .addConceptVersions(pbConceptVersion)
                    .build();

            // When we transform PBConceptChronology
            ConceptEntity actualConceptChronology = TinkarSchemaToEntityTransformer.getInstance().transformConceptChronology(pbConceptChronology);

            // Then the resulting ConceptChronology should match the original PBConceptChronology
            assertEquals(nid(testConcept), actualConceptChronology.nid(), "Nid's did not match in Concept Chronology.");
            assertTrue(PublicId.equals(testConcept.publicId(), actualConceptChronology.publicId()), "Public Id's of the concept chronology do not match.");
            assertEquals(1, actualConceptChronology.versions().size(), "Versions are empty");
            //TODO: do we need to test details of Stamp Version?
//            assertEquals(expectedTime, actualConceptChronology.versions().get(0).time(), "Time did not match");
        });

    }
@Test
    @DisplayName("Transform a Concept Chronology With Two Version")
    public void conceptChronologyTransformWithTwoVersions(){
        openSession(this, (mockedEntity, conceptMap) -> {
            // Given a PBConceptChronology with a one Stamp Version present
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);

            StampVersion pbStampVersion = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(conceptMap.get(STATUS_CONCEPT_NAME)))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(conceptMap.get(AUTHOR_CONCEPT_NAME)))
                    .setModulePublicId(createPBPublicId(conceptMap.get(MODULE_CONCEPT_NAME)))
                    .setPathPublicId(createPBPublicId(conceptMap.get(PATH_CONCEPT_NAME)))
                    .build();

            StampChronology pbStampChronology = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
//                    .addVersions(pbStampVersion)
                    .build();

            ConceptVersion pbConceptVersionOne = ConceptVersion.newBuilder()
                    .setStampChronologyPublicId(createPBPublicId(testConcept))
                    .build();

            ConceptVersion pbConceptVersionTwo = ConceptVersion.newBuilder()
                    .setStampChronologyPublicId(createPBPublicId(testConcept))
                    .build();

            ConceptChronology pbConceptChronology = ConceptChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
                    .addConceptVersions(pbConceptVersionOne)
                    .addConceptVersions(pbConceptVersionTwo)
                    .build();

            // When we transform PBConceptChronology
            ConceptEntity actualConceptChronology = TinkarSchemaToEntityTransformer.getInstance().transformConceptChronology(pbConceptChronology);

            // Then the resulting ConceptChronology should match the original PBConceptChronology
            assertEquals(nid(testConcept), actualConceptChronology.nid(), "Nid's did not match in Concept Chronology.");
            assertTrue(PublicId.equals(testConcept.publicId(), actualConceptChronology.publicId()), "Public Id's of the concept chronology do not match.");
            assertEquals(2, actualConceptChronology.versions().size(), "Versions are empty");
            //TODO: do we need to test details of Stamp Version?
//            assertEquals(expectedTime, actualConceptChronology.versions().get(0).time(), "Time did not match");
        });

    }
    // TODO write test to fail when stamp version is the same (time, author, etc.)
}
