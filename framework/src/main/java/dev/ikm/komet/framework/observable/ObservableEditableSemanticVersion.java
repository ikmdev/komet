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
import dev.ikm.tinkar.entity.StampEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
        extends ObservableEditableVersion<ObservableSemantic, ObservableSemanticVersion, SemanticVersionRecord> {

    private final MutableList<ObservableEditableField<?>> editableFields;
    private final ObservableList<ObservableEditableField<?>> unmodifiableFieldList;

    ObservableEditableSemanticVersion(ObservableSemantic observableSemantic, ObservableSemanticVersion observableVersion, StampEntity editStamp) {
        super(observableSemantic, observableVersion, editStamp);

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

        // Wrap as unmodifiable ObservableList for JavaFX integration
        // The list structure is immutable - fields cannot be added or removed during semantic editing.
        // Only field VALUES are editable via each ObservableEditableField's properties.
        this.unmodifiableFieldList = FXCollections.unmodifiableObservableList(
            FXCollections.observableArrayList(editableFields)
        );
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
    public static ObservableEditableSemanticVersion getOrCreate(ObservableSemantic observableSemantic, ObservableSemanticVersion observableVersion, StampEntity editStamp) {
        return ObservableEditableVersion.getOrCreate(observableSemantic, observableVersion, editStamp, ObservableEditableSemanticVersion::new);
    }

    /**
     * Returns the editable fields for JavaFX UI binding and display.
     * <p>
     * <b>List Structure Immutability:</b> The returned list has a fixed structure - fields cannot be
     * added or removed. This is by design: new fields can only be added by editing a {@link ObservablePattern},
     * not by editing individual semantic versions. Attempting to call {@code add()}, {@code remove()}, or
     * similar mutation operations on the returned list will throw {@link UnsupportedOperationException}.
     * <p>
     * <b>Field Value Mutability:</b> While the list structure is immutable, each {@link ObservableEditableField}
     * within the list contains observable properties that are fully editable. These properties can be bound
     * bidirectionally to UI controls for editing field values.
     * <p>
     * <b>Usage Patterns:</b>
     * <pre>{@code
     * // Pattern 1: Display in ListView/TableView
     * ObservableList<ObservableEditableField<?>> fields = editableVersion.getEditableFields();
     * fieldListView.setItems(fields); // List is observable for JavaFX controls
     *
     * // Pattern 2: Iterate and bind individual field properties
     * for (int i = 0; i < fields.size(); i++) {
     *     ObservableEditableField<?> field = fields.get(i);
     *     TextField textField = textFields.get(i);
     *
     *     // Bidirectional binding to field value property
     *     textField.textProperty().bindBidirectional(
     *         (Property<String>) field.editableValueProperty()
     *     );
     * }
     *
     * // Pattern 3: Indexed access (preferred for performance)
     * ObservableEditableField<?> field = editableVersion.getEditableField(0);
     * textField.textProperty().bind(field.editableValueProperty().asString());
     * }</pre>
     * <p>
     * <b>Return Type Rationale:</b> Returns {@link ObservableList} (rather than Eclipse Collections
     * {@link ImmutableList}) to provide seamless JavaFX integration. This allows the list to be used
     * directly with JavaFX controls like {@link javafx.scene.control.ListView} and
     * {@link javafx.scene.control.TableView}. The unmodifiable wrapper ensures structural immutability
     * while maintaining JavaFX observability for UI updates.
     * <p>
     * <b>Performance Note:</b> The returned list is created once during construction and cached.
     * Multiple calls to this method return the same canonical list instance.
     * <p>
     * <b>Thread Safety:</b> Must be called from the JavaFX application thread, consistent with
     * the Observable framework's threading requirements.
     * <p>
     * Symmetric to {@link ObservableSemanticVersion#fields()}, which returns read-only fields.
     *
     * @return an unmodifiable {@link ObservableList} of editable fields. The list structure is
     *         immutable (fixed size), but each field's value properties are observable and editable.
     * @see #getEditableField(int) for indexed field access
     * @see ObservableEditableField#editableValueProperty() for binding to individual field values
     * @see ObservableComposer for transaction management and commit workflow
     */
    public ObservableList<ObservableEditableField<?>> getEditableFields() {
        return unmodifiableFieldList;
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
