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
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.schema.Field;
import dev.ikm.tinkar.schema.SemanticVersion;
import dev.ikm.tinkar.schema.StampChronology;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.TEST_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.createPBPublicId;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.openSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

// @Disabled("Java 23")
public class TestEntityToProtobufSemanticTransform {
    @Test
    @DisplayName("Transform a Entity Semantic Chronology With Zero Versions/Values Present")
    public void semanticChronologyTransformWithZeroVersion() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given an Entity Semantic Version
            // When we transform our Entity Semantic Version into a PBSemanticVersion
            // Then the resulting PBSemanticVersion should match the original entity value
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBSemanticVersions(RecordListBuilder.make().build()), "Not allowed to have an empty Semantic Version.");
        });
    }

    @Test
    @DisplayName("Transform a Entity Semantic Version with all values present")
    public void semanticChronologyTransformWithOneVersion() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given an Entity Semantic Version
            StampRecord stampRecord = mock(StampRecord.class);
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            PublicId expectedStampPublicId = testConcept.publicId();
            SemanticVersionRecord mockSemanticVersion = mock(SemanticVersionRecord.class);

            EntityToTinkarSchemaTransformer entityToTinkarSchemaTransformer = spy(EntityToTinkarSchemaTransformer.getInstance());

            when(mockSemanticVersion.stamp()).thenReturn(stampRecord);
            when(mockSemanticVersion.stamp().publicId()).thenReturn(expectedStampPublicId);
            doReturn(StampChronology.getDefaultInstance()).when(entityToTinkarSchemaTransformer).createPBStampChronology(any());
            doReturn(List.of(Field.getDefaultInstance())).when(entityToTinkarSchemaTransformer).createPBFields(any());

            // When we transform our Entity Semantic Version into a PBSemanticVersion
            List<SemanticVersion> actualPBSemanticVersion = entityToTinkarSchemaTransformer.createPBSemanticVersions(RecordListBuilder.make().add(mockSemanticVersion));

            // Then the resulting PBSemanticVersion should match the original entity value
//            verify(entityToTinkarSchemaTransformer, times(1)).createPBStampChronology(any());
//            verify(entityToTinkarSchemaTransformer, times(1)).createPBFields(any());
            assertEquals(createPBPublicId(expectedStampPublicId), actualPBSemanticVersion.get(0).getStampChronologyPublicId(), "The stamp public ID is missing from semantic version.");
            assertEquals(1, actualPBSemanticVersion.get(0).getFieldsCount(), "Field counts do not match for semantic version.");
            assertEquals(1, actualPBSemanticVersion.size(), "The versions are missing from semantic version.");
            assertTrue(actualPBSemanticVersion.get(0).hasStampChronologyPublicId(), "The Semantic Version is missing a stamp public ID.");
        });
    }

    @Test
    @DisplayName("Transform a Entity Semantic Version with all values present and two Fields")
    public void semanticChronologyTransformWitTwoFields() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given an Entity Semantic Version
            StampRecord stampRecord = mock(StampRecord.class);
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            PublicId expectedStampPublicId = testConcept.publicId();
            SemanticVersionRecord mockSemanticVersion = mock(SemanticVersionRecord.class);

            EntityToTinkarSchemaTransformer entityToTinkarSchemaTransformer = spy(EntityToTinkarSchemaTransformer.getInstance());

            when(mockSemanticVersion.stamp()).thenReturn(stampRecord);
            when(mockSemanticVersion.stamp().publicId()).thenReturn(expectedStampPublicId);
            doReturn(StampChronology.getDefaultInstance()).when(entityToTinkarSchemaTransformer).createPBStampChronology(any());
            doReturn(List.of(Field.getDefaultInstance())).when(entityToTinkarSchemaTransformer).createPBFields(any());
            doReturn(List.of(Field.getDefaultInstance())).when(entityToTinkarSchemaTransformer).createPBFields(any());

            // When we transform our Entity Semantic Version into a PBSemanticVersion
            List<SemanticVersion> actualPBSemanticVersion = entityToTinkarSchemaTransformer.createPBSemanticVersions(RecordListBuilder.make().add(mockSemanticVersion));

            // Then the resulting PBSemanticVersion should match the original entity value
//            verify(entityToTinkarSchemaTransformer, times(1)).createPBStampChronology(any());
//            verify(entityToTinkarSchemaTransformer, times(2)).createPBFields(any());
            assertEquals(createPBPublicId(expectedStampPublicId), actualPBSemanticVersion.get(0).getStampChronologyPublicId(), "The stamp public ID is missing from semantic version.");
            assertEquals(1, actualPBSemanticVersion.size(), "The versions are missing from semantic version.");
            assertTrue(actualPBSemanticVersion.get(0).hasStampChronologyPublicId(), "The Semantic Version is missing a stamp public ID.");
        });
    }

    @Test
    @DisplayName("Transform a Entity Semantic Version With a Missing Stamp")
    public void semanticVersionTransformWithAMissingStamp() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given an Entity Semantic Version
            SemanticVersionRecord mockSemanticVersion = mock(SemanticVersionRecord.class);

            EntityToTinkarSchemaTransformer entityToTinkarSchemaTransformer = spy(EntityToTinkarSchemaTransformer.getInstance());

            doReturn(List.of(Field.getDefaultInstance())).when(entityToTinkarSchemaTransformer).createPBFields(any());

            // When we transform our Entity Semantic Versions into a PBSemanticVersion

            // Then the resulting PBSemanticVersion should throw an exception
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBSemanticVersions(RecordListBuilder.make().add(mockSemanticVersion)), "Not allowed to have an empty Stamp in Semantic Version.");
        });
    }

    @Test
    @DisplayName("Transform a Entity Semantic Version With a Missing Field")
    public void semanticVersionTransformWithAMissingField() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given an Entity Semantic Version
            SemanticVersionRecord mockSemanticVersion = mock(SemanticVersionRecord.class);

            EntityToTinkarSchemaTransformer entityToTinkarSchemaTransformer = spy(EntityToTinkarSchemaTransformer.getInstance());

            doReturn(StampChronology.getDefaultInstance()).when(entityToTinkarSchemaTransformer).createPBStampChronology(any());
            // When we transform our Entity Semantic Versions into a PBSemanticVersion

            // Then the resulting PBSemanticVersion should throw an exception
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBSemanticVersions(RecordListBuilder.make().add(mockSemanticVersion)), "Not allowed to have an empty Field in Semantic Version.");
        });
    }
}

