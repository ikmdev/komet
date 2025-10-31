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

import dev.ikm.tinkar.component.FieldDefinition;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Editable feature definition wrapper for pattern version field definitions.
 * <p>
 * Provides an editable counterpart to {@link ObservableFeatureDefinition}, though with a simpler
 * implementation since pattern field definitions are typically edited as complete metadata units
 * rather than individual field values.
 * <p>
 * Note: Unlike {@link ObservableEditableField} which extends {@link ObservableEditableFeature},
 * this class is standalone because {@link ObservableFeatureDefinition} does not extend
 * {@link ObservableFeature} - it directly implements the Feature interface.
 * <p>
 * For pattern editing, typically only the purpose and meaning are edited through
 * {@link ObservableEditablePatternVersion} properties. This class exists for API symmetry
 * and potential future enhancements.
 */
public final class ObservableEditableFeatureDefinition {

    private final ObservableFeatureDefinition observableFeatureDefinition;
    private final SimpleObjectProperty<ObservableFeatureDefinition> editableValueProperty;
    private final int featureIndex;

    /**
     * Package-private constructor.
     *
     * @param observableFeatureDef the read-only feature definition to wrap
     * @param initialValue the initial value
     * @param featureIndex the index of this feature definition in the pattern version
     */
    ObservableEditableFeatureDefinition(ObservableFeatureDefinition observableFeatureDef, ObservableFeatureDefinition initialValue, int featureIndex) {
        this.observableFeatureDefinition = observableFeatureDef;
        this.featureIndex = featureIndex;
        this.editableValueProperty = new SimpleObjectProperty<>(this, "value", initialValue);
    }

    /**
     * Returns the original read-only ObservableFeatureDefinition.
     */
    public ObservableFeatureDefinition getObservableFeatureDefinition() {
        return observableFeatureDefinition;
    }

    /**
     * Returns the editable property for GUI binding.
     */
    public SimpleObjectProperty<ObservableFeatureDefinition> editableValueProperty() {
        return editableValueProperty;
    }

    /**
     * Returns the current cached value.
     */
    public ObservableFeatureDefinition getValue() {
        return editableValueProperty.get();
    }

    /**
     * Sets the cached value.
     */
    public void setValue(ObservableFeatureDefinition value) {
        editableValueProperty.set(value);
    }

    /**
     * Returns the index of this feature definition in the pattern version.
     */
    public int getFeatureDefinitionIndex() {
        return featureIndex;
    }

    /**
     * Returns whether this editable feature definition has unsaved changes.
     */
    public boolean isDirty() {
        ObservableFeatureDefinition currentValue = editableValueProperty.get();
        ObservableFeatureDefinition originalValue = observableFeatureDefinition;

        if (currentValue == null && originalValue == null) {
            return false;
        }
        if (currentValue == null || originalValue == null) {
            return true;
        }
        return !currentValue.equals(originalValue);
    }

    /**
     * Resets the editable value to match the original.
     */
    public void reset() {
        editableValueProperty.set(observableFeatureDefinition);
    }
}
