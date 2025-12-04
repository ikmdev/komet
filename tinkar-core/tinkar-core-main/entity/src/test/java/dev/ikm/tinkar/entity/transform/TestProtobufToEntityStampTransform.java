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
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.StampVersion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.function.Consumer;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled("Java 23")
public class TestProtobufToEntityStampTransform {
    /**
     * Testing the transformation of a StampVersion Protobuf object to an Entity
     */
    @Test
    @DisplayName("Transform a Stamp Version With All Fields Present")
    public void stampVersionTransformWithStatusTimeAuthorModulePathPresent() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBStampVersion
            long expectedTime = nowTimestamp();
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            Concept statusConcept = conceptMap.get(STATUS_CONCEPT_NAME);
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            StampVersion pbStampVersion = createPbStampVersion(expectedTime, statusConcept, authorConcept, moduleConcept, pathConcept);
            StampRecord mockStampRecord = mock(StampRecord.class);

            // When we transform PBStampVersion
            StampVersionRecord actualStampVersionRecord = TinkarSchemaToEntityTransformer.getInstance().transformStampVersion(pbStampVersion, mockStampRecord);

            // Then the resulting StampVersionRecord should match the original PBStampVersion
            assertEquals(nid(statusConcept), actualStampVersionRecord.stateNid(), "Status Nid did not match");
            assertEquals(expectedTime, actualStampVersionRecord.time(), "Time did not match");
            assertEquals(nid(authorConcept), actualStampVersionRecord.authorNid(), "Author Nid did not match");
            assertEquals(nid(moduleConcept), actualStampVersionRecord.moduleNid(), "Module Nid did not match");
            assertEquals(nid(pathConcept), actualStampVersionRecord.pathNid(), "Path Nid did not match");
            assertEquals(mockStampRecord, actualStampVersionRecord.chronology(), "Stamp Record did not match");
        });

    }

    @Test
    @DisplayName("Transform a Stamp Version With All Fields Present Two")
    public void stampVersionTransformWithStatusTimeAuthorModulePathPresent2() {

        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBStampVersion
            Concept statusConcept = conceptMap.get(STATUS_CONCEPT_NAME);
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);
            long expectedTime = nowTimestamp();

            StampVersion pbStampVersion = createPbStampVersion(conceptMap, expectedTime);
            StampRecord mockStampRecord = mock(StampRecord.class);

            // When we transform PBStampVersion
            StampVersionRecord actualStampVersionRecord = TinkarSchemaToEntityTransformer.getInstance().transformStampVersion(pbStampVersion, mockStampRecord);

            // Then the resulting StampVersionRecord should match the original PBStampVersion
            assertEquals(nid(statusConcept), actualStampVersionRecord.stateNid(), "Status Nid did not match");
            assertEquals(expectedTime, actualStampVersionRecord.time(), "Time did not match");
            assertEquals(nid(authorConcept), actualStampVersionRecord.authorNid(), "Author Nid did not match");
            assertEquals(nid(moduleConcept), actualStampVersionRecord.moduleNid(), "Module Nid did not match");
            assertEquals(nid(pathConcept), actualStampVersionRecord.pathNid(), "Path Nid did not match");
            assertEquals(mockStampRecord, actualStampVersionRecord.chronology(), "Stamp Record did not match");
        });

    }

    //TODO - Create unit tests testing for runtime exception for each blank UUID/Public ID in STAMP values
    /**
     * Testing the transformation of a StampVersion Protobuf object to an Entity with a missing Status.
     */
    @Test
    @DisplayName("Transform a Stamp Version With Status being Blank")
    public void stampVersionTransformWithStatusBeingBlankPublicId() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            StampRecord mockStampRecord = mock(StampRecord.class);
            // Given a PBStampVersion with a missing Public Id for Status
            StampVersion pbStampVersion = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId())
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(conceptMap.get(AUTHOR_CONCEPT_NAME)))
                    .setModulePublicId(createPBPublicId(conceptMap.get(MODULE_CONCEPT_NAME)))
                    .setPathPublicId(createPBPublicId(conceptMap.get(PATH_CONCEPT_NAME)))
                    .build();

            // When we transform PBStampVersion

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformStampVersion(pbStampVersion, mockStampRecord), "Not allowed to have empty UUID for status.");
        });
    }

    /**
     * Testing the transformation of a StampVersion Protobuf object to an Entity with a missing Author.
     */
    @Test
    @DisplayName("Transform a Stamp Version With Author being Blank")
    public void stampVersionTransformWithAuthorBeingBlankPublicId() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBStampVersion with a missing Public Id for Author
            StampRecord mockStampRecord = mock(StampRecord.class);
            StampVersion pbStampVersion = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(conceptMap.get(STATUS_CONCEPT_NAME)))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId())
                    .setModulePublicId(createPBPublicId(conceptMap.get(MODULE_CONCEPT_NAME)))
                    .setPathPublicId(createPBPublicId(conceptMap.get(PATH_CONCEPT_NAME)))
                    .build();

            // When we transform PBStampVersion

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformStampVersion(pbStampVersion, mockStampRecord), "Not allowed to have empty UUID for author.");
        });
    }

    /**
     * Testing the transformation of a StampVersion Protobuf object to an Entity with a missing Module.
     */
    @Test
    @DisplayName("Transform a Stamp Version With Module being Blank")
    public void stampVersionTransformWithModuleBeingBlankPublicId() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBStampVersion with a missing Public Id for Module
            StampRecord mockStampRecord = mock(StampRecord.class);
            StampVersion pbStampVersion = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(conceptMap.get(STATUS_CONCEPT_NAME)))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(conceptMap.get(AUTHOR_CONCEPT_NAME)))
                    .setModulePublicId(createPBPublicId())
                    .setPathPublicId(createPBPublicId(conceptMap.get(PATH_CONCEPT_NAME)))
                    .build();

            // When we transform PBStampVersion

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformStampVersion(pbStampVersion, mockStampRecord), "Not allowed to have empty UUID for module.");
        });
    }

    /**
     * Testing the transformation of a StampVersion Protobuf object to an Entity with a missing Path.
     */
    @Test
    @DisplayName("Transform a Stamp Version With Path being Blank")
    public void stampVersionTransformWithPathBeingBlankPublicId() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBStampVersion with a missing Public Id for Path
            StampRecord mockStampRecord = mock(StampRecord.class);
            StampVersion pbStampVersion = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(conceptMap.get(STATUS_CONCEPT_NAME)))
                    .setTime(nowTimestamp())
                    .setAuthorPublicId(createPBPublicId(conceptMap.get(AUTHOR_CONCEPT_NAME)))
                    .setModulePublicId(createPBPublicId(conceptMap.get(MODULE_CONCEPT_NAME)))
                    .setPathPublicId(createPBPublicId())
                    .build();

            // When we transform PBStampVersion

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformStampVersion(pbStampVersion, mockStampRecord), "Not allowed to have empty UUID for path.");
        });
    }

    /**
     * Testing the transformation of a StampChronology Protobuf object to an Entity with no versions present.
     *  TODO: THis should throw an exception but because we are creating the chonology with an empty list there must be a check in the transform
     */
    @Test
    @DisplayName("Transform a Stamp Chronology With No Versions")
    public void stampChronologyTransformWithZeroVersion(){
        openSession(this, (mockedEntityService, conceptMap) -> {
            Consumer<StampEntity<StampEntityVersion>> stampConsumer = (c) -> { };
            // Given a PBStampChronology with a no Stamp Versions present
            StampChronology pbStampChronology = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId())
                    .build();

            // When we transform PBStampChronology

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformStampChronology(pbStampChronology, stampConsumer), "Not allowed to have no stamp versions.");
        });
    }

    /**
     * Testing the transformation of a StampChronology Protobuf object to an Entity with one version present.
     */
    @Test
    @DisplayName("Transform a Stamp Chronology With One Version")
    @Disabled("Need entity provider and integration test DB")
    //TODO: Need to create a test DB with a Stamp Chronology with one version present
    public void stampChronologyTransformWithOneVersion(){
        openSession(this, (mockedEntityService, conceptMap) -> {
            Consumer<StampEntity<StampEntityVersion>> stampConsumer = (c) -> { };
            // Given a PBStampChronology with a one Stamp Version present
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            Concept statusConcept = conceptMap.get(STATUS_CONCEPT_NAME);
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            long expectedTime = nowTimestamp();
            StampVersion pbStampVersion = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(statusConcept))
                    .setTime(expectedTime)
                    .setAuthorPublicId(createPBPublicId(authorConcept))
                    .setModulePublicId(createPBPublicId(moduleConcept))
                    .setPathPublicId(createPBPublicId(pathConcept))
                    .build();

            StampChronology pbStampChronology = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId(conceptMap.get(TEST_CONCEPT_NAME)))
                    .setFirstStampVersion(pbStampVersion)
                    .build();

            // When we transform PBStampChronology
            StampRecord actualStampChronology = TinkarSchemaToEntityTransformer.getInstance().transformStampChronology(pbStampChronology, stampConsumer);

            // Then the resulting StampChronology should match the original PBStampChronology
            assertEquals(nid(testConcept), actualStampChronology.nid(), "Nid's did not match in Stamp Chronology.");
            assertTrue(PublicId.equals(testConcept.publicId(), actualStampChronology.publicId()), "Public Id's of the stamp chronology do not match.");
        });
    }


    @Test
    @DisplayName("Transform a Stamp Chronology With Two Versions")
    @Disabled("Need entity provider and integration test DB")
    //TODO: Need to create a test DB with a Stamp Chronology with one version present
    public void stampChronologyTransformWithTwoVersions(){
        openSession(this, (mockedEntityService, conceptMap) -> {
            Consumer<StampEntity<StampEntityVersion>> stampConsumer = (c) -> { };
            // Given a PBStampChronology with two Stamp Versions present
            long expectedTime1 = nowTimestamp();
            long expectedTime2 = nowTimestamp(60 * 1000);
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            Concept statusConcept = conceptMap.get(STATUS_CONCEPT_NAME);
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            StampVersion pbStampVersionOne = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(conceptMap.get(STATUS_CONCEPT_NAME)))
                    .setTime(expectedTime1)
                    .setAuthorPublicId(createPBPublicId(authorConcept))
                    .setModulePublicId(createPBPublicId(moduleConcept))
                    .setPathPublicId(createPBPublicId(pathConcept))
                    .build();
            StampVersion pbStampVersionTwo = StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(statusConcept))
                    .setTime(expectedTime2)
                    .setAuthorPublicId(createPBPublicId(authorConcept))
                    .setModulePublicId(createPBPublicId(moduleConcept))
                    .setPathPublicId(createPBPublicId(pathConcept))
                    .build();

            StampChronology pbStampChronology = StampChronology.newBuilder()
                    .setPublicId(createPBPublicId(testConcept))
                    .setFirstStampVersion(pbStampVersionOne)
                    .setSecondStampVersion(pbStampVersionTwo)
                    .build();

            // When we transform PBStampChronology
            StampRecord actualStampChronology = TinkarSchemaToEntityTransformer.getInstance().transformStampChronology(pbStampChronology, stampConsumer);

            // Then the resulting StampChronology should match the original PBStampChronology
            assertEquals(nid(testConcept), actualStampChronology.nid(), "Nid's did not match in Stamp Chronology.");
            assertTrue(PublicId.equals(testConcept.publicId(), actualStampChronology.publicId()), "Public Id's of the stamp chronology do not match.");
            assertEquals(2, actualStampChronology.versions().size(), "Versions are empty");
            assertEquals(nid(statusConcept), actualStampChronology.versions().get(0).stateNid(), "Status Nid did not match");
            assertEquals(expectedTime1, actualStampChronology.versions().get(0).time(), "Time did not match");
            assertEquals(nid(authorConcept), actualStampChronology.versions().get(0).authorNid(), "Author Nid did not match");
            assertEquals(nid(moduleConcept), actualStampChronology.versions().get(0).moduleNid(), "Module Nid did not match");
            assertEquals(nid(pathConcept), actualStampChronology.versions().get(0).pathNid(), "Path Nid did not match");
            assertEquals(nid(statusConcept), actualStampChronology.versions().get(1).stateNid(), "Status Nid did not match");
            assertEquals(expectedTime2, actualStampChronology.versions().get(1).time(), "Time did not match");
            assertEquals(nid(authorConcept), actualStampChronology.versions().get(1).authorNid(), "Author Nid did not match");
            assertEquals(nid(moduleConcept), actualStampChronology.versions().get(1).moduleNid(), "Module Nid did not match");
            assertEquals(nid(pathConcept), actualStampChronology.versions().get(1).pathNid(), "Path Nid did not match");
        });

        //TODO: Add test to check if a stamp chronology can be created with two stamp version of the same type (and time).
    }
}
