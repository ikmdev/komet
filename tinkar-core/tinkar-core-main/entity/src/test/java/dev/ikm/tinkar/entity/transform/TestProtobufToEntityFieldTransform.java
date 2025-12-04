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

import dev.ikm.tinkar.schema.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static dev.ikm.tinkar.entity.transform.ProtobufToEntityTestHelper.openSession;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled("Java 23")
public class TestProtobufToEntityFieldTransform {
    @Test
    @DisplayName("Transform a Field With a String Value Present")
    public void testProtobufToEntityFieldTransformWithAStringValue() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a string value
            String expectedStringValue = "Testing Field Transformation with a string.";
            Field pbFieldString = Field.newBuilder()
                    .setStringValue(expectedStringValue)
                    .build();

            // When we transform our String Field value
            Object actualFieldString = TinkarSchemaToEntityTransformer.getInstance().transformField(pbFieldString);

            // Then the resulting Object should match the original passed in string value.
            assertEquals(expectedStringValue,actualFieldString.toString(), "The transformed string value does not match the expected.");
            assertEquals(expectedStringValue.toUpperCase(),actualFieldString.toString().toUpperCase(), "The transformed string uppercase value does not match the expected.");
            assertEquals(expectedStringValue.toLowerCase(),actualFieldString.toString().toLowerCase(), "The transformed string lowercase value does not match the expected.");
        });
    }

    @Test
    @DisplayName("Transform a Field With a Boolean Value Present")
    public void testProtobufToEntityFieldTransformWithABoolValue() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a boolean value
            Boolean expectedBoolValue = true;
            Field pbFieldBool = Field.newBuilder()
                    .setBooleanValue(expectedBoolValue)
                    .build();

            // When we transform our Boolean Field value
            Object actualFieldBool = TinkarSchemaToEntityTransformer.getInstance().transformField(pbFieldBool);

            // Then the resulting Object should match the original passed in boolean value.
            assertEquals(expectedBoolValue, actualFieldBool, "The transformed boolean value does not match the expected.");
        });
    }

    @Test
    @DisplayName("Transform a Field With a Integer Value Present")
    public void testProtobufToEntityFieldTransformWithAIntValue() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a integer value
            Integer expectedIntValue = 1568;
            Field pbFieldInt = Field.newBuilder()
                    .setIntValue(expectedIntValue)
                    .build();

            // When we transform our Integer Field value
            Object actualFieldInt = TinkarSchemaToEntityTransformer.getInstance().transformField(pbFieldInt);

            // Then the resulting Object should match the original passed in Integer value.
            assertEquals(expectedIntValue, actualFieldInt, "The transformed integer value does not match the expected.");
        });
    }

    @Test
    @DisplayName("Transform a Field With a Float Value Present")
    public void testProtobufToEntityFieldTransformWithAFloatValue() {
        openSession(this, (mockedEntityService, conceptMap) -> {
            // Given a float value
            Float expectedFloatValue = 1534.34f;
            Field pbFieldFloat = Field.newBuilder()
                    .setFloatValue(expectedFloatValue)
                    .build();

            // When we transform our Float Field value
            Object actualFieldFloat = TinkarSchemaToEntityTransformer.getInstance().transformField(pbFieldFloat);

            // Then the resulting Object should match the original passed in float value.
            assertEquals(expectedFloatValue, actualFieldFloat, "The transformed float value does not match the expected.");
        });
    }
}
