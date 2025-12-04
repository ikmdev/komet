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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.TEST_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.openSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled("Java 23")
public class TestProtobufToEntityPublicIdTransform {

    /**
     * Testing the transformation of an empty Public ID Protobuf object to an Entity
     */
    @Test
    @DisplayName("Transform a Public ID With No UUIDs Present")
    public void publicIdTransformWithNoUUID() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBPublic ID with no UUID
            dev.ikm.tinkar.schema.PublicId pbPublicId = dev.ikm.tinkar.schema.PublicId .newBuilder().build();

            // When I try to transform it into a public ID protobuf message

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> TinkarSchemaToEntityTransformer.getInstance().transformPublicId(pbPublicId), "Not allowed to have empty UUID");
        });
    }

    /**
     * Testing the transformation of a Public ID Protobuf object to an Entity
     */
    @Test
    @DisplayName("Transform a Public ID With a Single UUID Present")
    public void publicIdTransformWithSingleUUID() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a PBPublic ID with one UUID
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            PublicId expectedPublicId = testConcept.publicId();
            dev.ikm.tinkar.schema.PublicId  pbPublicId = dev.ikm.tinkar.schema.PublicId.newBuilder().addUuids(expectedPublicId.asUuidList().get(0).toString()).build();

            // When I try to transform it into a public ID protobuf message
            PublicId actualPublicId = TinkarSchemaToEntityTransformer.getInstance().transformPublicId(pbPublicId);

            // Then we will check to verify that the transformed UUID (public ID) matches that of the original.
            assertEquals(actualPublicId, expectedPublicId, "Public ID's do not match.");
            assertEquals(actualPublicId.publicIdHash(), expectedPublicId.publicIdHash(), "Public ID's do not match.");
            assertEquals(actualPublicId.idString(), expectedPublicId.idString(), "Public ID's do not match.");
            assertEquals(actualPublicId.asUuidList().get(0), expectedPublicId.asUuidList().get(0), "Public ID's do not match.");
        });
    }

    /**
     * Testing the transformation of two Public ID Protobuf objects to Entities
     */
    @Test
    @DisplayName("Transform a Public ID With Two UUIDs Present")
    public void publicIdTransformWithTwoUUIDS() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given two PBPublic ID with two UUID
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            PublicId actualOnePublicId = testConcept.publicId();
            PublicId actualTwoPublicId = testConcept.publicId();
            PublicId expectedCombinedSource = PublicIds.of(actualOnePublicId.asUuidList().get(0), actualTwoPublicId.asUuidList().get(0));
            dev.ikm.tinkar.schema.PublicId  pbPublicId = dev.ikm.tinkar.schema.PublicId .newBuilder()
                    .addUuids(actualOnePublicId.asUuidList().get(0).toString())
                    .addUuids(actualTwoPublicId.asUuidList().get(0).toString())
                    .build();

            // When I try to transform them into a public ID protobuf message
            PublicId actualPublicId = TinkarSchemaToEntityTransformer.getInstance().transformPublicId(pbPublicId);

            // Then we will check to verify that the transformed UUIDs (public ID) matches that of the original.
            assertEquals(expectedCombinedSource, actualPublicId, "Public ID's do not match.");
            assertEquals(expectedCombinedSource.publicIdHash(), actualPublicId.publicIdHash(), "Public ID's hashes do not match.");
            assertEquals(expectedCombinedSource.idString(), actualPublicId.idString(), "Public ID's ID string do not match.");
            assertEquals(2, actualPublicId.asUuidList().size(), "Public ID's size do not match.");
            assertEquals(expectedCombinedSource.asUuidList().get(0), actualPublicId.asUuidList().get(0), "Public ID's UUID lists from index 0 do not match.");
            assertEquals(expectedCombinedSource.asUuidList().get(1), actualPublicId.asUuidList().get(1), "Public ID's UUID lists do not match from index 1.");
        });
    }
}
