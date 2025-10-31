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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ikm.tinkar.component.FieldDefinition;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Base class for editable features that cache field changes for GUI editing.
 * <p>
 * Provides the same symmetric API as {@link ObservableFeature}, but for editable scenarios
 * where changes are cached in an {@link ObservableEditableVersion} until save() or commit().
 * <p>
 * <b>Symmetry with ObservableFeature:</b>
 * <ul>
 *   <li>ObservableFeature → read-only, immediate DB writes</li>
 *   <li>ObservableEditableFeature → editable, cached changes</li>
 * </ul>
 * <p>
 * Subclasses:
 * <ul>
 *   <li>{@link ObservableEditableField} - for semantic version fields</li>
 * </ul>
 * <p>
 * Note: {@link ObservableEditableFeatureDefinition} is NOT a subclass of ObservableEditableFeature
 * because {@link ObservableFeatureDefinition} does not extend {@link ObservableFeature} - it directly
 * implements the Feature interface. However, ObservableEditableFeatureDefinition provides the same
 * API surface for consistency.
 *
 * @param <DT> the data type of the feature value
 */
public abstract sealed class ObservableEditableFeature<DT>
        permits ObservableEditableField {

    /**
     * Composite key for caching editable features.
     */
    record EditableFeatureKey(int editableVersionNid, int stampNid, int featureIndex) {}

    /**
     * Caffeine cache with weak values for canonical editable feature instances.
     * Ensures one editable feature per (editableVersion, stamp, featureIndex) combination.
     */
    private static final Cache<EditableFeatureKey, ObservableEditableFeature<?>> EDITABLE_FEATURE_CACHE =
            Caffeine.newBuilder()
                    .weakValues()
                    .build();

    protected final ObservableFeature<DT> observableFeature;
    protected final SimpleObjectProperty<DT> editableValueProperty;
    protected final int featureIndex;

    /**
     * Package-private constructor.
     *
     * @param observableFeature the read-only feature to wrap
     * @param initialValue the initial value for the editable property
     * @param featureIndex the index of this feature in its container
     */
    ObservableEditableFeature(ObservableFeature<DT> observableFeature, DT initialValue, int featureIndex) {
        this.observableFeature = observableFeature;
        this.featureIndex = featureIndex;
        this.editableValueProperty = new SimpleObjectProperty<>(this, "value", initialValue);
    }

    /**
     * Gets or creates a canonical editable feature.
     * Ensures the same editable feature instance is returned for the same key.
     */
    @SuppressWarnings("unchecked")
    static <DT, OEF extends ObservableEditableFeature<DT>> OEF getOrCreate(
            EditableFeatureKey key,
            EditableFeatureFactory<DT, OEF> factory) {
        return (OEF) EDITABLE_FEATURE_CACHE.get(key, k -> factory.create());
    }

    /**
     * Returns the original read-only ObservableFeature.
     */
    public ObservableFeature<DT> getObservableFeature() {
        return observableFeature;
    }

    /**
     * Returns the editable property for GUI binding.
     * Changes to this property are cached and don't immediately affect the database.
     */
    public SimpleObjectProperty<DT> editableValueProperty() {
        return editableValueProperty;
    }

    /**
     * Returns the current cached value.
     */
    public DT getValue() {
        return editableValueProperty.get();
    }

    /**
     * Sets the cached value.
     */
    public void setValue(DT value) {
        editableValueProperty.set(value);
    }

    /**
     * Returns the field definition for this feature.
     */
    public FieldDefinition fieldDefinition(StampCalculator calculator) {
        return observableFeature.fieldDefinition(calculator);
    }

    /**
     * Returns the index of this feature in its container.
     */
    public int getFeatureIndex() {
        return featureIndex;
    }

    /**
     * Returns whether this editable feature has unsaved changes.
     */
    public boolean isDirty() {
        DT currentValue = editableValueProperty.get();
        DT originalValue = observableFeature.value();

        if (currentValue == null && originalValue == null) {
            return false;
        }
        if (currentValue == null || originalValue == null) {
            return true;
        }
        return !currentValue.equals(originalValue);
    }

    /**
     * Resets the editable value to match the original read-only feature.
     */
    public void reset() {
        editableValueProperty.set(observableFeature.value());
    }

    /**
     * Factory interface for creating editable features.
     */
    @FunctionalInterface
    interface EditableFeatureFactory<DT, OEF extends ObservableEditableFeature<DT>> {
        OEF create();
    }
}
