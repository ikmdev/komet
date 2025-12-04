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
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.schema.ConceptVersion;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.StampVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.*;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.AUTHOR_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.MODULE_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.PATH_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.createPbStampVersion;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.nowTimestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled("Java 23")
public class TestEntityToProtobufConceptTransform {

    @Test
    @DisplayName("createPBStampChronology function throws error When StampEntity is empty")
    public void testStampEntityWithEmptyVersions() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            PublicId randomPublicID = PublicIds.newRandom();



            StampEntity<StampVersionRecord> mockedStampEntityVersion = mock(StampEntity.class);

            StampRecord mockedStampChronology = mock(StampRecord.class);


            ImmutableList<StampVersionRecord> versions = Lists.immutable.empty();

            when(mockedStampChronology.versions()).thenReturn(versions);

            when(mockedStampEntityVersion.asUuidList()).thenReturn(randomPublicID.asUuidList());
            when(mockedStampEntityVersion.publicId()).thenReturn(randomPublicID);
            when(mockedStampEntityVersion.versions()).thenReturn(versions);

            //int testNid = PrimitiveData.get().nidForPublicId(mockedStampEntityVersion.publicId());

            assertThrows(RuntimeException.class, () -> EntityToTinkarSchemaTransformer.getInstance()
                    .createPBStampChronology(mockedStampEntityVersion), "Unexpected number of version size: 0 " +
                    " for stamp entity: " + randomPublicID);
        });
    }

    @Test
    @DisplayName("createPBStampChronology function throws error When StampEntity has more than 2 Versions")
    public void testStampEntityWithMoreThanTwoVersions() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            PublicId randomPublicID = PublicIds.newRandom();
            PublicId stampPublicID = PublicIds.newRandom();
            //System.out.println("Public ID: " + randomPublicID);


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

            ImmutableList<StampVersionRecord> versions = Lists.immutable.of(mockedStampVersion, mockedStampVersion, mockedStampVersion);

            when(mockedStampChronology.versions()).thenReturn(versions);

            when(mockedStampEntityVersion.asUuidList()).thenReturn(randomPublicID.asUuidList());
            when(mockedStampEntityVersion.publicId()).thenReturn(randomPublicID);
            when(mockedStampEntityVersion.versions()).thenReturn(versions);

            //int testNid = PrimitiveData.get().nidForPublicId(mockedStampEntityVersion.publicId());

            assertThrows(RuntimeException.class, () -> EntityToTinkarSchemaTransformer.getInstance()
                    .createPBStampChronology(mockedStampEntityVersion), "Unexpected number of version size: 3" +
                    " for stamp entity: " + randomPublicID);
        });
    }

    @Test
    @DisplayName("Transform a Entity Concept Chronology With Zero Versions/Values Present")
    public void conceptChronologyTransformWithZeroVersion() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given an Entity Concept Version
            // When we transform our Entity Concept Version into a PBConceptVersion
            // Then the resulting PBConceptVersion should match the original entity value
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBConceptVersions(RecordListBuilder.make().build()), "Not allowed to have an empty Concept Version.");
        });
    }

    @Test
    @DisplayName("Transform a Entity Concept Chronology with all values")
    public void conceptChronologyTransformWithOneVersion() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            //Given a concept chronology with all values
            ConceptEntity conceptPublic = mock(ConceptEntity.class);
            StampRecord stampRecord = mock(StampRecord.class);
            PublicId conceptPublicId = PublicIds.newRandom();
            when(conceptPublic.publicId()).thenReturn(conceptPublic);
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            PublicId expectedPublicId = testConcept.publicId();

            ConceptVersionRecord mockConceptVersion = mock(ConceptVersionRecord.class);
            when(mockConceptVersion.publicId()).thenReturn(conceptPublic);
            when(mockConceptVersion.stamp()).thenReturn(stampRecord);
            when(mockConceptVersion.stamp().publicId()).thenReturn(expectedPublicId);

            EntityToTinkarSchemaTransformer entityTransformer = spy(EntityToTinkarSchemaTransformer.getInstance());

            // When we transform our Entity Pattern Version into a PBPatternVersion
            List<ConceptVersion> actualPBConceptVersion = entityTransformer.createPBConceptVersions(RecordListBuilder.make().with(mockConceptVersion).build());

            // Then the resulting PBConceptVersion should match the original entity value
            assertEquals(1, actualPBConceptVersion.size(), "The size of the Concept Chronology does not match the expected.");
            assertEquals(createPBPublicId(expectedPublicId), actualPBConceptVersion.get(0).getStampChronologyPublicId(), "The Concept Chronology is missing a STAMP public ID.");
            assertFalse(actualPBConceptVersion.isEmpty(), "The Concept Version is empty.");
            assertTrue(actualPBConceptVersion.get(0).hasStampChronologyPublicId(), "The Concept Chronology is missing a STAMP public ID.");
        });
    }

    @Test
    @DisplayName("Transform a Entity Concept Version with two versions present")
    public void conceptVersionTransformWithTwoVersions() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            ConceptEntity conceptPublic = mock(ConceptEntity.class);
            StampRecord stampRecord = mock(StampRecord.class);
            PublicId conceptPublicId = PublicIds.newRandom();
            when(conceptPublic.publicId()).thenReturn(conceptPublic);
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            PublicId expectedPublicId = testConcept.publicId();

            ConceptVersionRecord mockConceptVersion = mock(ConceptVersionRecord.class);
            when(mockConceptVersion.publicId()).thenReturn(conceptPublic);
            when(mockConceptVersion.stamp()).thenReturn(stampRecord);
            when(mockConceptVersion.stamp().publicId()).thenReturn(expectedPublicId);

            EntityToTinkarSchemaTransformer entityTransformer = spy(EntityToTinkarSchemaTransformer.getInstance());

            doReturn(StampChronology.getDefaultInstance()).when(entityTransformer).createPBStampChronology(any());
            // When we transform our Entity Pattern Version into a PBPatternVersion
            List<ConceptVersion> actualPBConceptVersion = entityTransformer.createPBConceptVersions(RecordListBuilder.make().with(mockConceptVersion).addAndBuild(mockConceptVersion));

            // Then the resulting PBConceptVersion should match the original entity value
            assertEquals(2, actualPBConceptVersion.size(), "The size of the Concept Chronology does not match the expected.");
            assertFalse(actualPBConceptVersion.isEmpty(), "The Concept Version is empty.");
            assertTrue(actualPBConceptVersion.get(0).hasStampChronologyPublicId(), "The Concept Chronology is missing a STAMP public ID.");
            assertTrue(actualPBConceptVersion.get(1).hasStampChronologyPublicId(), "The Concept Chronology is missing a STAMP public ID.");
        });
    }
}
