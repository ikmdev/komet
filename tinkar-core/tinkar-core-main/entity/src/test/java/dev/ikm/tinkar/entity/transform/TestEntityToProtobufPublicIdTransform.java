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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.TEST_CONCEPT_NAME;
import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.openSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled("Java 23")
public class TestEntityToProtobufPublicIdTransform {

    @Test
    @DisplayName("Transform a Entity Public ID into a Protobuf message with no Public ID.")
    public void publicIdEntityTransformWithNoPublicID() {
        openSession(this, (mockedEntityService, conceptMap) -> {

            // Given a Public ID with no public id
            PublicId emptyPublicId = null;

            // When I try to transform it into a public ID message

            // Then we will throw a Runtime exception
            assertThrows(Throwable.class, () -> EntityToTinkarSchemaTransformer.getInstance().createPBPublicId(emptyPublicId), "Not allowed to have empty UUID");
        });
    }

    @Test
    @DisplayName("Transform a Entity Public ID into a Protobuf message with one Public ID.")
    public void publicIdEntityTransformWithAPublicId() {
        // Given a Public ID with a UUID
        openSession(this, (mockedEntityService, conceptMap) -> {

            // Given a Public ID with one public id
            Concept testConcept = conceptMap.get(TEST_CONCEPT_NAME);
            PublicId actualPublicId = testConcept.publicId();
            //Creating a Protobuf with the Expected value
            dev.ikm.tinkar.schema.PublicId expectedPBPublicId = dev.ikm.tinkar.schema.PublicId.newBuilder().addUuids(actualPublicId.asUuidList().get(0).toString()).build();

            // When I try to transform it into a public ID protobuf message
            dev.ikm.tinkar.schema.PublicId actualPBPublicId = EntityToTinkarSchemaTransformer.getInstance().createPBPublicId(actualPublicId);

            // Then we will check to verify that the transformed public ID matches that of the original.
            assertEquals(expectedPBPublicId, actualPBPublicId, "Protobuf Public ID's do not match.");
            assertEquals(expectedPBPublicId.hashCode(), actualPBPublicId.hashCode(), "Protobuf Public ID's hash codes not match.");
            assertEquals(expectedPBPublicId.getUuidsList(), actualPBPublicId.getUuidsList(), "Protobuf Public ID's lists not match.");
        });
    }

    //TODO: Finish unit testing coverage here
    @Test
    @Disabled
    @DisplayName("Transform a Entity Public ID into a Protobuf message with a list of public ID's.")
    public void publicIdEntityTransformWithPublicIDList() {
        // Given two Public ID's

        // When I try to transform it into a public ID protobuf message

        // Then we will check to verify that the transformed UUID (public ID) matches that of the original.
    }
}
