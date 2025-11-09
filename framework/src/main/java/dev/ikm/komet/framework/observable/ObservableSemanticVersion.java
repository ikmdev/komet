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

import dev.ikm.komet.framework.observable.binding.Binding;
import dev.ikm.tinkar.entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Concrete observable semantic version - fully type-reified, no generic parameters.
 * <p>
 * This is Layer 3 (Concrete) of the MGC pattern for semantic versions.
 */
public final class ObservableSemanticVersion
        extends ObservableEntityVersion<ObservableSemantic, SemanticVersionRecord>
        implements SemanticEntityVersion, ObservableVersion {

    ObservableSemanticVersion(ObservableSemantic observableSemantic, SemanticVersionRecord semanticVersionRecord) {
        super(observableSemantic, semanticVersionRecord);
    }

    @Override
    public SemanticEntity entity() {
        return version().entity();
    }

    @Override
    public SemanticEntity chronology() {
        return version().chronology();
    }

    @Override
    public SemanticVersionRecord getVersionRecord() {
        return version();
    }

    @Override
    public PatternEntity pattern() {
        return EntityHandle.getPatternOrThrow(patternNid());
    }

    @Override
    public int patternNid() {
        return version().patternNid();
    }

    @Override
    public int indexInPattern() {
        return Binding.Semantic.versionItemDefinitionIndex();
    }

    @Override
    public ImmutableList<Object> fieldValues() {
        return version().fieldValues();
    }

    @Override
    public ImmutableList<ObservableField> fields() {
        ObservableField[] fieldArray = new ObservableField[fieldValues().size()];
        for (int indexInPattern = 0; indexInPattern < fieldArray.length; indexInPattern++) {
            Object value = fieldValues().get(indexInPattern);

            FeatureKey.VersionFeature.Semantic.FieldListItem featureKey =
                    FeatureKey.Version.SemanticFieldListItem(nid(), indexInPattern, patternNid(), stampNid());

            fieldArray[indexInPattern] = new ObservableField(featureKey, new FieldRecord(value, featureKey.nid(), featureKey.stampNid(), featureKey.patternNid(), featureKey.index()), this);
        }
        return Lists.immutable.of(fieldArray);
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<Feature> fieldListReference = new AtomicReference<>();
    private Feature getFieldListFeature() {
        return fieldListReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeFieldListFeature());
    }
    private Feature makeFieldListFeature() {
        FeatureKey locator = FeatureKey.Version.SemanticFieldList(this.nid(), stampNid());
        return FeatureList.makeWithBackingList(this.fields(), locator, Binding.Semantic.Version.pattern(), Binding.Semantic.Version.semanticFieldsDefinitionIndex(), this);
    }

    @Override
    protected void addAdditionalVersionFeatures(MutableList<Feature<?>> features) {
        features.add(getFieldListFeature());
        for (ObservableField field : fields()) {
            features.add(field);
        }
    }

    @Override
    public Editable getEditableVersion(ObservableStamp editStamp) {
        return Editable.getOrCreate(getObservableEntity(), this, editStamp);
    }

    /**
     * Type-safe accessor for the containing semantic entity.
     */
    public ObservableSemantic getObservableSemantic() {
        return getObservableEntity();
    }

    /**
     * Editable version wrapper for ObservableSemanticVersion.
     * <p>
     * Implements {@link EditableVersion} marker
     * interface through the base {@link ObservableEntityVersion.Editable} class.
     * <p>
     * Provides field-level editing for semantic versions with proper validation
     * and type safety.
     * 
     * <h2>Semantic Field Editing</h2>
     * <p>Semantic versions have dynamic fields defined by their pattern. Use
     * {@link dev.ikm.komet.framework.propsheet.SheetItem#makeEditable} to create property sheet items for individual
     * fields:
     * <pre>{@code
     * EditableVersion editable = semanticVersion.getEditableVersion(editStamp);
     * 
     * if (editable instanceof ObservableSemanticVersion.Editable se) {
     *     // Create editable sheet items for each field
     *     for (int i = 0; i < pattern.fieldCount(); i++) {
     *         SheetItem<?> item = SheetItem.makeEditable(
     *             se, i, viewProperties);
     *         propertySheet.getItems().add(item);
     *     }
     * }
     * }</pre>
     */
    public static final class Editable
            extends ObservableEntityVersion.Editable<ObservableSemantic, ObservableSemanticVersion, SemanticVersionRecord>
            implements EditableVersion {
        // Already implements EditableVersion and EditableChronology via parent!

        private final MutableList<ObservableField.Editable<?>> editableFields;
        private final ObservableList<ObservableField.Editable<?>> unmodifiableFieldList;

        private Editable(ObservableSemantic observableSemantic, ObservableSemanticVersion observableVersion, ObservableStamp editStamp) {
            super(observableSemantic, observableVersion, editStamp);

            // Create editable fields wrapping the read-only fields
            ImmutableList<ObservableField> fields = observableVersion.fields();
            this.editableFields = Lists.mutable.ofInitialCapacity(fields.size());

            for (int i = 0; i < fields.size(); i++) {
                ObservableField<?> observableField = fields.get(i);
                Object initialValue = observableVersion.fieldValues().get(i);

                // Create editable field
                ObservableField.Editable<?> editableField = createEditableField(observableField, initialValue, i);

                // Add listener to update working version when field changes
                int fieldIndex = i;
                editableField.editableValueProperty().addListener((obs, oldValue, newValue) -> {
                    updateFieldValue(fieldIndex, newValue);
                });

                editableFields.add(editableField);
            }

            // Wrap as unmodifiable ObservableList for JavaFX integration
            // The list structure is immutable - fields cannot be added or removed during semantic editing.
            // Only field VALUES are editable via each ObservableField.Editable's properties.
            this.unmodifiableFieldList = FXCollections.unmodifiableObservableList(
                FXCollections.observableArrayList(editableFields)
            );
        }

        @SuppressWarnings("unchecked")
        private <DT> ObservableField.Editable<DT> createEditableField(
                ObservableField<?> observableField,
                Object initialValue,
                int fieldIndex) {
            return new ObservableField.Editable<>(
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
        public static Editable getOrCreate(ObservableSemantic observableSemantic, ObservableSemanticVersion observableVersion, ObservableStamp editStamp) {
            return ObservableEntityVersion.getOrCreate(observableSemantic, observableVersion, editStamp, Editable::new);
        }

        /**
         * Returns the editable fields for JavaFX UI binding and display.
         * <p>
         * <b>List Structure Immutability:</b> The returned list has a fixed structure - fields cannot be
         * added or removed. This is by design: new fields can only be added by editing a {@link ObservablePattern},
         * not by editing individual semantic versions. Attempting to call {@code add()}, {@code remove()}, or
         * similar mutation operations on the returned list will throw {@link UnsupportedOperationException}.
         * <p>
         * <b>Field Value Mutability:</b> While the list structure is immutable, each {@link ObservableField.Editable}
         * within the list contains observable properties that are fully editable. These properties can be bound
         * bidirectionally to UI controls for editing field values.
         * <p>
         * <b>Usage Patterns:</b>
         * <pre>{@code
         * // Pattern 1: Display in ListView/TableView
         * ObservableList<ObservableField.Editable<?>> fields = editableVersion.getEditableFields();
         * fieldListView.setItems(fields); // List is observable for JavaFX controls
         *
         * // Pattern 2: Iterate and bind individual field properties
         * for (int i = 0; i < fields.size(); i++) {
         *     ObservableField.Editable<?> field = fields.get(i);
         *     TextField textField = textFields.get(i);
         *
         *     // Bidirectional binding to field value property
         *     textField.textProperty().bindBidirectional(
         *         (Property<String>) field.editableValueProperty()
         *     );
         * }
         *
         * // Pattern 3: Indexed access (preferred for performance)
         * ObservableField.Editable<?> field = editableVersion.getEditableField(0);
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
         * @see ObservableField.Editable#editableValueProperty() for binding to individual field values
         * @see ObservableComposer for transaction management and commit workflow
         */
        public ObservableList<ObservableField.Editable<?>> getEditableFields() {
            return unmodifiableFieldList;
        }

        /**
         * Gets the editable field at a specific index.
         *
         * @param index the field index
         * @return the editable field
         */
        public ObservableField.Editable<?> getEditableField(int index) {
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
            for (ObservableField.Editable<?> editableField : editableFields) {
                editableField.reset();
            }
        }
    }
}
