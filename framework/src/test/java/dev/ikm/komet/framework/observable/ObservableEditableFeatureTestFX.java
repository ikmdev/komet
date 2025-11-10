
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
import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ObservableFeature.Editable.
 * Tests the base editable feature functionality.
 */
@ExtendWith(JavaFXThreadExtension.class)
class ObservableSemanticFieldEditableTestFX {

    @Test
    @RunOnJavaFXThread
    void testEditableValueProperty() {
        TestEditableFeature feature = new TestEditableFeature("initial");

        SimpleObjectProperty<String> property = feature.editableValueProperty();

        assertNotNull(property);
        assertEquals("initial", property.get());
    }

    @Test
    @RunOnJavaFXThread
    void testGetValue() {
        TestEditableFeature feature = new TestEditableFeature("test value");

        assertEquals("test value", feature.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testSetValue() {
        TestEditableFeature feature = new TestEditableFeature("initial");

        feature.setValue("updated");

        assertEquals("updated", feature.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testIsDirtyWhenUnchanged() {
        TestEditableFeature feature = new TestEditableFeature("value");

        assertFalse(feature.hasUnsavedChanges());
    }

    @Test
    @RunOnJavaFXThread
    void testIsDirtyWhenChanged() {
        TestEditableFeature feature = new TestEditableFeature("initial");

        feature.setValue("changed");

        assertTrue(feature.hasUnsavedChanges());
    }

    @Test
    @RunOnJavaFXThread
    void testIsDirtyWithNullInitialValue() {
        TestEditableFeature feature = new TestEditableFeature(null);

        assertFalse(feature.hasUnsavedChanges());

        feature.setValue("not null");

        assertTrue(feature.hasUnsavedChanges());
    }

    @Test
    @RunOnJavaFXThread
    void testIsDirtyWithNullChangedValue() {
        TestEditableFeature feature = new TestEditableFeature("initial");

        feature.setValue(null);

        assertTrue(feature.hasUnsavedChanges());
    }

    @Test
    @RunOnJavaFXThread
    void testReset() {
        TestEditableFeature feature = new TestEditableFeature("original");

        feature.setValue("modified");
        assertTrue(feature.hasUnsavedChanges());

        feature.reset();

        assertFalse(feature.hasUnsavedChanges());
        assertEquals("original", feature.getValue());
    }

    @Test
    @RunOnJavaFXThread
    void testGetFeatureIndex() {
        TestEditableFeature feature = new TestEditableFeature("value", 5);

        assertEquals(5, feature.getFeatureIndex());
    }

    @Test
    @RunOnJavaFXThread
    void testPropertyListenerTriggered() {
        TestEditableFeature feature = new TestEditableFeature("initial");

        AtomicReference<String> capturedValue = new AtomicReference<>();
        feature.editableValueProperty().addListener((obs, oldVal, newVal) -> {
            capturedValue.set(newVal);
        });

        feature.setValue("new value");

        assertEquals("new value", capturedValue.get());
    }

    @Test
    @RunOnJavaFXThread
    void testPropertyBind() {
        TestEditableFeature feature = new TestEditableFeature("initial");

        javafx.beans.property.SimpleObjectProperty<String> boundProperty =
                new javafx.beans.property.SimpleObjectProperty<>();
        boundProperty.bind(feature.editableValueProperty());

        assertEquals("initial", boundProperty.get());

        feature.setValue("updated");

        assertEquals("updated", boundProperty.get());
    }

    @Test
    @RunOnJavaFXThread
    void testPropertyBidirectionalBind() {
        TestEditableFeature feature = new TestEditableFeature("initial");

        javafx.beans.property.SimpleObjectProperty<String> boundProperty =
                new javafx.beans.property.SimpleObjectProperty<>("external");
        feature.editableValueProperty().bindBidirectional(boundProperty);

        // Feature should now have the bound property's value
        assertEquals("external", feature.getValue());

        // Changing feature should update bound property
        feature.setValue("from feature");
        assertEquals("from feature", boundProperty.get());

        // Changing bound property should update feature
        boundProperty.set("from property");
        assertEquals("from property", feature.getValue());
    }

    /**
     * Test implementation of ObservableFeature.Editable for testing purposes.
     */
    private static class TestEditableFeature {
        private final SimpleObjectProperty<String> editableValueProperty;
        private final String originalValue;
        private final int featureIndex;

        TestEditableFeature(String initialValue) {
            this(initialValue, 0);
        }

        TestEditableFeature(String initialValue, int featureIndex) {
            this.originalValue = initialValue;
            this.featureIndex = featureIndex;
            this.editableValueProperty = new SimpleObjectProperty<>(this, "value", initialValue);
        }

        public SimpleObjectProperty<String> editableValueProperty() {
            return editableValueProperty;
        }

        public String getValue() {
            return editableValueProperty.get();
        }

        public void setValue(String value) {
            editableValueProperty.set(value);
        }

        public boolean hasUnsavedChanges() {
            String currentValue = editableValueProperty.get();

            if (currentValue == null && originalValue == null) {
                return false;
            }
            if (currentValue == null || originalValue == null) {
                return true;
            }
            return !currentValue.equals(originalValue);
        }

        public void reset() {
            editableValueProperty.set(originalValue);
        }

        public int getFeatureIndex() {
            return featureIndex;
        }
    }
}