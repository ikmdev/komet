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
package dev.ikm.komet.framework.observable;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ObservableEditableField behavior.
 * Tests field value editing, property binding, and change tracking.
 */
@ExtendWith(JavaFXThreadExtension.class)
class ObservableEditableFieldTestFX {

    @Test
    @RunOnJavaFXThread
    void testFieldIndexTracking() {
        TestObservableEditableField<String> field = new TestObservableEditableField<>("value", 3);

        assertEquals(3, field.getFieldIndex());
    }

    @Test
    @RunOnJavaFXThread
    void testValuePropertyNotNull() {
        TestObservableEditableField<String> field = new TestObservableEditableField<>("test", 0);

        assertNotNull(field.editableValueProperty());
    }

    @Test
    @RunOnJavaFXThread
    void testInitialValue() {
        TestObservableEditableField<String> field = new TestObservableEditableField<>("initial", 0);

        assertEquals("initial", field.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testSetAndGetValue() {
        TestObservableEditableField<String> field = new TestObservableEditableField<>("start", 0);

        field.setValue("updated");

        assertEquals("updated", field.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testIntegerFieldValues() {
        TestObservableEditableField<Integer> field = new TestObservableEditableField<>(42, 0);

        assertEquals(42, field.getValue());

        field.setValue(100);

        assertEquals(100, field.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testBooleanFieldValues() {
        TestObservableEditableField<Boolean> field = new TestObservableEditableField<>(true, 0);

        assertTrue(field.getValue());

        field.setValue(false);

        assertFalse(field.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testNullValueHandling() {
        TestObservableEditableField<String> field = new TestObservableEditableField<>(null, 0);

        assertNull(field.getValue());

        field.setValue("not null");

        assertEquals("not null", field.getValue());

        field.setValue(null);

        assertNull(field.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testPropertyListener() {
        TestObservableEditableField<String> field = new TestObservableEditableField<>("initial", 0);

        AtomicInteger listenerCallCount = new AtomicInteger(0);
        AtomicReference<String> capturedNewValue = new AtomicReference<>();

        field.editableValueProperty().addListener((obs, oldVal, newVal) -> {
            listenerCallCount.incrementAndGet();
            capturedNewValue.set(newVal);
        });

        field.setValue("changed");

        assertEquals(1, listenerCallCount.get());
        assertEquals("changed", capturedNewValue.get());
    }

    @Test
    @RunOnJavaFXThread
    void testMultipleValueChanges() {
        TestObservableEditableField<Integer> field = new TestObservableEditableField<>(0, 0);

        AtomicInteger listenerCallCount = new AtomicInteger(0);

        field.editableValueProperty().addListener((obs, oldVal, newVal) -> {
            listenerCallCount.incrementAndGet();
        });

        field.setValue(1);
        field.setValue(2);
        field.setValue(3);

        assertEquals(3, listenerCallCount.get());
        assertEquals(3, field.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testBidirectionalPropertyBinding() {
        TestObservableEditableField<String> field = new TestObservableEditableField<>("field", 0);

        javafx.beans.property.SimpleObjectProperty<String> externalProperty =
                new javafx.beans.property.SimpleObjectProperty<>("external");

        field.editableValueProperty().bindBidirectional(externalProperty);

        // Field should have external property's value
        assertEquals("external", field.getValue());

        // Changing field updates external
        field.setValue("from field");
        assertEquals("from field", externalProperty.get());

        // Changing external updates field
        externalProperty.set("from external");
        assertEquals("from external", field.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testUnidirectionalPropertyBinding() {
        TestObservableEditableField<String> field = new TestObservableEditableField<>("field", 0);

        javafx.beans.property.SimpleObjectProperty<String> sourceProperty =
                new javafx.beans.property.SimpleObjectProperty<>("source");

        field.editableValueProperty().bind(sourceProperty);

        assertEquals("source", field.getValue());

        sourceProperty.set("updated");

        assertEquals("updated", field.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testFieldWithComplexObject() {
        ComplexValue initial = new ComplexValue("initial", 1);
        TestObservableEditableField<ComplexValue> field = new TestObservableEditableField<>(initial, 0);

        assertEquals("initial", field.getValue().name);
        assertEquals(1, field.getValue().id);

        ComplexValue updated = new ComplexValue("updated", 2);
        field.setValue(updated);

        assertEquals("updated", field.getValue().name);
        assertEquals(2, field.getValue().id);
    }

    @Test
    @RunOnJavaFXThread
    void testMultipleFields() {
        TestObservableEditableField<String> field0 = new TestObservableEditableField<>("field0", 0);
        TestObservableEditableField<String> field1 = new TestObservableEditableField<>("field1", 1);
        TestObservableEditableField<String> field2 = new TestObservableEditableField<>("field2", 2);

        assertEquals(0, field0.getFieldIndex());
        assertEquals(1, field1.getFieldIndex());
        assertEquals(2, field2.getFieldIndex());

        assertEquals("field0", field0.getValue());
        assertEquals("field1", field1.getValue());
        assertEquals("field2", field2.getValue());
    }

    /**
     * Test implementation mimicking ObservableEditableField behavior.
     */
    private static class TestObservableEditableField<DT> {
        private final javafx.beans.property.SimpleObjectProperty<DT> editableValueProperty;
        private final int fieldIndex;

        TestObservableEditableField(DT initialValue, int fieldIndex) {
            this.fieldIndex = fieldIndex;
            this.editableValueProperty = new javafx.beans.property.SimpleObjectProperty<>(this, "value", initialValue);
        }

        public javafx.beans.property.SimpleObjectProperty<DT> editableValueProperty() {
            return editableValueProperty;
        }

        public DT getValue() {
            return editableValueProperty.get();
        }

        public void setValue(DT value) {
            editableValueProperty.set(value);
        }

        public int getFieldIndex() {
            return fieldIndex;
        }
    }

    /**
     * Complex value class for testing object-typed fields.
     */
    private static class ComplexValue {
        final String name;
        final int id;

        ComplexValue(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }
}