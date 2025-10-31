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

import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * Editable version wrapper for ObservableSemanticVersion.
 * <p>
 * Provides editable fields ({@link ObservableEditableField}) that can be bound to GUI components.
 * Changes are cached until save() or commit() is called.
 * <p>
 * Symmetric counterpart to {@link ObservableSemanticVersion}:
 * <ul>
 *   <li>ObservableSemanticVersion → has {@link ObservableField} (read-only)</li>
 *   <li>ObservableEditableSemanticVersion → has {@link ObservableEditableField} (editable)</li>
 * </ul>
 */
public final class ObservableEditableSemanticVersion
        extends ObservableEditableVersion<ObservableSemanticVersion, SemanticVersionRecord> {

    private final MutableList<ObservableEditableField<?>> editableFields;

    ObservableEditableSemanticVersion(ObservableSemanticVersion observableVersion, ObservableStamp editStamp) {
        super(observableVersion, editStamp);

        // Create editable fields wrapping the read-only fields
        ImmutableList<ObservableField> fields = observableVersion.fields();
        this.editableFields = Lists.mutable.ofInitialCapacity(fields.size());

        for (int i = 0; i < fields.size(); i++) {
            ObservableField<?> observableField = fields.get(i);
            Object initialValue = observableVersion.fieldValues().get(i);

            // Create editable field
            ObservableEditableField<?> editableField = createEditableField(observableField, initialValue, i);

            // Add listener to update working version when field changes
            int fieldIndex = i;
            editableField.editableValueProperty().addListener((obs, oldValue, newValue) -> {
                updateFieldValue(fieldIndex, newValue);
            });

            editableFields.add(editableField);
        }
    }

    @SuppressWarnings("unchecked")
    private <DT> ObservableEditableField<DT> createEditableField(
            ObservableField<?> observableField,
            Object initialValue,
            int fieldIndex) {
        return new ObservableEditableField<>(
                (ObservableField<DT>) observableField,
                (DT) initialValue,
                fieldIndex
        );
    }

    /**
     * Gets or creates the canonical editable semantic version for the given stamp.
     * <p>
     * Returns the exact same instance for multiple calls with the same stamp, ensuring
     * a single canonical editable version per ObservableStamp.
     *
     * @param observableVersion the ObservableSemanticVersion to edit
     * @param editStamp the ObservableStamp (typically identifying the author)
     * @return the canonical editable semantic version for this stamp
     */
    public static ObservableEditableSemanticVersion getOrCreate(ObservableSemanticVersion observableVersion, ObservableStamp editStamp) {
        return ObservableEditableVersion.getOrCreate(observableVersion, editStamp, ObservableEditableSemanticVersion::new);
    }

    /**
     * Returns the editable fields for GUI binding.
     * <p>
     * Each {@link ObservableEditableField} wraps a read-only {@link ObservableField}
     * and provides editable properties that cache changes.
     * <p>
     * Symmetric to {@link ObservableSemanticVersion#fields()}.
     *
     * @return immutable list of editable fields
     */
    public ImmutableList<ObservableEditableField<?>> getEditableFields() {
        return editableFields.toImmutable();
    }

    /**
     * Gets the editable field at a specific index.
     *
     * @param index the field index
     * @return the editable field
     */
    public ObservableEditableField<?> getEditableField(int index) {
        return editableFields.get(index);
    }

    /**
     * Gets the editable property for a specific field index (convenience method).
     * <p>
     * Equivalent to {@code getEditableField(index).editableValueProperty()}.
     *
     * @param index the field index
     * @return the editable property for that field
     * @deprecated Use {@link #getEditableField(int)} for better API symmetry
     */
    @Deprecated(forRemoval = true)
    public javafx.beans.property.SimpleObjectProperty<Object> getFieldProperty(int index) {
        return (javafx.beans.property.SimpleObjectProperty<Object>) editableFields.get(index).editableValueProperty();
    }

    /**
     * Updates a field value and rebuilds the working version.
     */
    private void updateFieldValue(int fieldIndex, Object newValue) {
        MutableList<Object> newFieldValues = Lists.mutable.ofAll(workingVersion.fieldValues());
        newFieldValues.set(fieldIndex, newValue);

        workingVersion = workingVersion.withFieldValues(newFieldValues.toImmutable());
    }

    @Override
    protected SemanticVersionRecord createVersionWithStamp(SemanticVersionRecord version, int stampNid) {
        return version.withStampNid(stampNid);
    }

    @Override
    protected Entity<?> createAnalogue(SemanticVersionRecord version) {
        return version.chronology().with(version).build();
    }

    @Override
    public void reset() {
        super.reset();
        // Reset all editable fields to original values
        for (ObservableEditableField<?> editableField : editableFields) {
            editableField.reset();
        }
    }
}
