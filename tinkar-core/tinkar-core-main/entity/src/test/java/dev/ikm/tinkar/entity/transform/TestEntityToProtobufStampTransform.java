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
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.StampVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.AUTHOR_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.MODULE_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.PATH_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.TEST_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.createPBPublicId;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.nowEpochMillis;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.openSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled("Java 23")
public class TestEntityToProtobufStampTransform {

    @Test
    @DisplayName("Transform a Entity Stamp Version With all Values Present")
    public void testEntitytoProtobufStampVersionTransformWithValuesPresent() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a Stamp Version Entity
            long expectedTime = nowEpochMillis();
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            //Mocking
            StampVersionRecord mockStampVersion = mock(StampVersionRecord.class);
            when(mockStampVersion.state()).thenReturn(State.ACTIVE);
            when(mockStampVersion.author()).thenReturn((ConceptFacade) authorConcept);
            when(mockStampVersion.module()).thenReturn((ConceptFacade) moduleConcept);
            when(mockStampVersion.path()).thenReturn((ConceptFacade) pathConcept);
            when(mockStampVersion.time()).thenReturn(expectedTime);

            // When we transform our StampVersion into a PBStampVersion
            StampVersion actualPBStampVersion = EntityToTinkarSchemaTransformer.getInstance().createPBStampVersion(mockStampVersion);

            // Then the resulting PBStampVersion should match the original entity value.
            assertEquals(createPBPublicId(State.ACTIVE.publicId()), actualPBStampVersion.getStatusPublicId(), "The States/Statuses do not match in PBStampVersion.");
            assertEquals(createPBPublicId(authorConcept.publicId()), actualPBStampVersion.getAuthorPublicId(), "The Authors do not match in PBStampVersion.");
            assertEquals(createPBPublicId(moduleConcept.publicId()), actualPBStampVersion.getModulePublicId(), "The Modules do not match in PBStampVersion.");
            assertEquals(createPBPublicId(pathConcept.publicId()), actualPBStampVersion.getPathPublicId(), "The Paths do not match in PBStampVersion.");
            assertEquals(expectedTime, actualPBStampVersion.getTime(), "The Timestamps do not match in PBStampVersion.");
        });
    }

    @Test
    @DisplayName("Transform a Entity Stamp Version With Status being Blank")
    public void stampVersionTransformWithStatusBeingBlank() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a Stamp Version Entity
            long expectedTime = nowEpochMillis();
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            //Mocking
            StampVersionRecord mockStampVersion = mock(StampVersionRecord.class);
            when(mockStampVersion.author()).thenReturn((ConceptFacade) authorConcept);
            when(mockStampVersion.module()).thenReturn((ConceptFacade) moduleConcept);
            when(mockStampVersion.path()).thenReturn((ConceptFacade) pathConcept);
            when(mockStampVersion.time()).thenReturn(expectedTime);

            // When we transform our StampVersion into a PBStampVersion

            // Then the resulting PBStampVersion should throw an exception if Status is not present.
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBStampVersion(mockStampVersion), "Not allowed to have an empty status in a STAMP.");
        });
    }

    /**
     * Testing the transformation of a StampVersion Protobuf object to a Protobuf Message with a missing Author.
     */
    @Test
    @DisplayName("Transform a Entity Stamp Version With Author being Blank")
    public void stampVersionTransformWithAuthorBeingBlank() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a Stamp Version Entity
            long expectedTime = nowEpochMillis();
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            //Mocking
            StampVersionRecord mockStampVersion = mock(StampVersionRecord.class);
            when(mockStampVersion.module()).thenReturn((ConceptFacade) moduleConcept);
            when(mockStampVersion.path()).thenReturn((ConceptFacade) pathConcept);
            when(mockStampVersion.time()).thenReturn(expectedTime);

            // When we transform our StampVersion into a PBStampVersion

            // Then the resulting PBStampVersion should throw an exception if Author is not present.
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBStampVersion(mockStampVersion), "Not allowed to have an empty author in a STAMP.");
        });
    }

    /**
     * Testing the transformation of a StampVersion Protobuf object to a Protobuf Message with a missing Module.
     */
    @Test
    @DisplayName("Transform a Entity Stamp Version With Module being Blank")
    public void stampVersionTransformWithModuleBeingBlank() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a Stamp Version Entity
            long expectedTime = nowEpochMillis();
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            //Mocking
            StampVersionRecord mockStampVersion = mock(StampVersionRecord.class);
            when(mockStampVersion.state()).thenReturn(State.ACTIVE);
            when(mockStampVersion.author()).thenReturn((ConceptFacade) authorConcept);
            when(mockStampVersion.path()).thenReturn((ConceptFacade) pathConcept);
            when(mockStampVersion.time()).thenReturn(expectedTime);

            // When we transform our StampVersion into a PBStampVersion

            // Then the resulting PBStampVersion should throw an exception if Module is not present.
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBStampVersion(mockStampVersion), "Not allowed to have an empty module in a STAMP.");
        });
    }

    /**
     * Testing the transformation of a StampVersion Protobuf object to a Protobuf Message with a missing Path.
     */
    @Test
    @DisplayName("Transform a Entity Stamp Version With Path being Blank")
    public void stampVersionTransformWithPathBeingBlank() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a Stamp Version Entity
            long expectedTime = nowEpochMillis();
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);

            //Mocking
            StampVersionRecord mockStampVersion = mock(StampVersionRecord.class);
            when(mockStampVersion.state()).thenReturn(State.ACTIVE);
            when(mockStampVersion.author()).thenReturn((ConceptFacade) authorConcept);
            when(mockStampVersion.module()).thenReturn((ConceptFacade) moduleConcept);
            when(mockStampVersion.time()).thenReturn(expectedTime);
            // When we transform our StampVersion into a PBStampVersion

            // Then the resulting PBStampVersion should throw an exception if Path is not present.
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBStampVersion(mockStampVersion), "Not allowed to have an empty Path in a STAMP.");
        });
    }

    //FIXME: Is there a better way to implement this test?
    @Test
    @DisplayName("Transform a Entity Stamp Chronology With No Versions")
    public void stampChronologyTransformWithZeroVersions(){
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a Stamp Chronology Entity
            StampEntity<StampVersionRecord> mockedStampEntity = mock(StampEntity.class);
            when(mockedStampEntity.nid()).thenReturn(21423);
            // When we transform our StampVersion into a PBStampVersion

            // Then the resulting PBStampVersion should throw an exception because there is an empty stamp version.
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBStampChronology(mockedStampEntity), "Not allowed to have an empty stamp version in a StampChronology.");

        });
    }

    @Test
    @DisplayName("Transform a Entity Stamp Chronology With One Version")
    public void stampChronologyTransformWithOneVersion(){
        openSession(this, (mockedEntityService, conceptMap) -> {
            PublicId randomPublicID = PublicIds.newRandom();
            PublicId stampPublicID = PublicIds.newRandom();

            long expectedTime = nowEpochMillis();
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            StampEntity<StampVersionRecord> mockedStampEntityVersion = mock(StampEntity.class);

            StampRecord mockedStampChronology = mock(StampRecord.class);

            StampVersionRecord mockedStampVersion = mock(StampVersionRecord.class);

            when(mockedStampVersion.publicId()).thenReturn(stampPublicID);
            when(mockedStampVersion.state()).thenReturn(State.ACTIVE);
            when(mockedStampVersion.author()).thenReturn((ConceptFacade) authorConcept);
            when(mockedStampVersion.module()).thenReturn((ConceptFacade) moduleConcept);
            when(mockedStampVersion.path()).thenReturn((ConceptFacade) pathConcept);
            when(mockedStampVersion.time()).thenReturn(expectedTime);
            when(mockedStampVersion.stamp()).thenReturn(mockedStampChronology);

            ImmutableList<StampVersionRecord> versions = Lists.immutable.of(mockedStampVersion);
            when(mockedStampChronology.versions()).thenReturn(versions);

            when(mockedStampEntityVersion.asUuidList()).thenReturn(randomPublicID.asUuidList());
            when(mockedStampEntityVersion.publicId()).thenReturn(randomPublicID);
            when(mockedStampEntityVersion.versions()).thenReturn(new RecordListBuilder<StampVersionRecord>().addAndBuild(mockedStampVersion));

            // When we perform the transform
            StampChronology actualPBStampChronology = EntityToTinkarSchemaTransformer.getInstance().createPBStampChronology(mockedStampEntityVersion);

            //TODO: Add in Mockito Verify statements here

            // Then we assure that the values match
            assertEquals(createPBPublicId(randomPublicID), actualPBStampChronology.getPublicId(), "The public ID's of the expected Stamp Chronology and actual do not match.");
            assertEquals(createPBPublicId(randomPublicID), actualPBStampChronology.getPublicId(), "The size of the Stamp Version do not match those expected in the Stamp Chronology");
            assertEquals(createPBPublicId(authorConcept.publicId()), actualPBStampChronology.getFirstStampVersion().getAuthorPublicId(), "The public ID's of the expected Stamp Chronology's Author and actual do not match.");
            assertEquals(createPBPublicId(moduleConcept.publicId()), actualPBStampChronology.getFirstStampVersion().getModulePublicId(), "The public ID's of the expected Stamp Chronology's Module and actual do not match.");
            assertEquals(createPBPublicId(pathConcept.publicId()), actualPBStampChronology.getFirstStampVersion().getPathPublicId(), "The public ID's of the expected Stamp Chronology's Path and actual do not match.");
        });
    }

    @Test
    @DisplayName("Transform a Entity Stamp Version With Two Versions")
    public void stampVersionTransformWithTwoVersions(){
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a Stamp Version Entity
            long expectedTimeOne = nowEpochMillis();
            long expectedTimeTwo = nowEpochMillis(5000);
            Concept authorConcept = conceptMap.get(AUTHOR_CONCEPT_NAME);
            Concept moduleConcept = conceptMap.get(MODULE_CONCEPT_NAME);
            Concept pathConcept = conceptMap.get(PATH_CONCEPT_NAME);

            //Mocking
            StampVersionRecord mockStampVersionOne = mock(StampVersionRecord.class);
            when(mockStampVersionOne.state()).thenReturn(State.ACTIVE);
            when(mockStampVersionOne.author()).thenReturn((ConceptFacade) authorConcept);
            when(mockStampVersionOne.module()).thenReturn((ConceptFacade) moduleConcept);
            when(mockStampVersionOne.path()).thenReturn((ConceptFacade) pathConcept);
            when(mockStampVersionOne.time()).thenReturn(expectedTimeOne);

            StampVersionRecord mockStampVersionTwo = mock(StampVersionRecord.class);
            when(mockStampVersionTwo.state()).thenReturn(State.ACTIVE);
            when(mockStampVersionTwo.author()).thenReturn((ConceptFacade) authorConcept);
            when(mockStampVersionTwo.module()).thenReturn((ConceptFacade) moduleConcept);
            when(mockStampVersionTwo.path()).thenReturn((ConceptFacade) pathConcept);
            when(mockStampVersionTwo.time()).thenReturn(expectedTimeTwo);

            // When we transform our StampVersion into a PBStampVersion
            StampVersion actualPBStampVersionOne = EntityToTinkarSchemaTransformer.getInstance().createPBStampVersion(mockStampVersionOne);
            StampVersion actualPBStampVersionTwo = EntityToTinkarSchemaTransformer.getInstance().createPBStampVersion(mockStampVersionTwo);

            // Then the resulting PBStampVersions should match the original entity value.
            assertEquals(createPBPublicId(State.ACTIVE.publicId()), actualPBStampVersionOne.getStatusPublicId(), "The States/Statuses do not match in PBStampVersionOne.");
            assertEquals(createPBPublicId(authorConcept.publicId()), actualPBStampVersionOne.getAuthorPublicId(), "The Authors do not match in PBStampVersionOne.");
            assertEquals(createPBPublicId(moduleConcept.publicId()), actualPBStampVersionOne.getModulePublicId(), "The Modules do not match in PBStampVersionOne.");
            assertEquals(createPBPublicId(pathConcept.publicId()), actualPBStampVersionOne.getPathPublicId(), "The Paths do not match in PBStampVersionOne.");
            assertEquals(expectedTimeOne, actualPBStampVersionOne.getTime(), "The Timestamps do not match in PBStampVersionOne.");
            assertEquals(createPBPublicId(State.ACTIVE.publicId()), actualPBStampVersionTwo.getStatusPublicId(), "The States/Statuses do not match in PBStampVersionTwo.");
            assertEquals(createPBPublicId(authorConcept.publicId()), actualPBStampVersionTwo.getAuthorPublicId(), "The Authors do not match in PBStampVersionTwo.");
            assertEquals(createPBPublicId(moduleConcept.publicId()), actualPBStampVersionTwo.getModulePublicId(), "The Modules do not match in PBStampVersionTwo.");
            assertEquals(createPBPublicId(pathConcept.publicId()), actualPBStampVersionTwo.getPathPublicId(), "The Paths do not match in PBStampVersionTwo.");
            assertEquals(expectedTimeTwo, actualPBStampVersionTwo.getTime(), "The Timestamps do not match in PBStampVersionTwo.");
        });
    }
    //TODO: Add test to check if a stamp chronology can be created with two stamp version of the same type (and time).
}
