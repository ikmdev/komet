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

import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.ConceptToDataType;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.concurrent.atomic.AtomicReference;

public final class ObservableFeatureDefinition
        implements FieldDefinitionForEntity, Feature<ObservableFeatureDefinition> {
    final ObservableComponent containingComponent;
    final FeatureKey locator;
    final AtomicReference<FieldDefinitionRecord> fieldDefinitionReference;
    final SimpleObjectProperty<EntityFacade> dataTypeProperty = new SimpleObjectProperty<>(this, "Field data type");
    final SimpleObjectProperty<EntityFacade> purposeProperty = new SimpleObjectProperty<>(this, "Field purpose");
    final SimpleObjectProperty<EntityFacade> meaningProperty = new SimpleObjectProperty<>(this, "Field meaning");
    private final ReadOnlyProperty<? extends Feature<ObservableFeatureDefinition>> featureProperty = new ReadOnlyObjectWrapper<>(this).getReadOnlyProperty();

    public ObservableFeatureDefinition(FieldDefinitionForEntity fieldDefinition, ObservableComponent containingComponent, FeatureKey locator) {
         this(switch (fieldDefinition) {
            case FieldDefinitionRecord fieldDefinitionRecord -> fieldDefinitionRecord;
            case ObservableFeatureDefinition observableFeatureDefinition ->
                    observableFeatureDefinition.fieldDefinitionReference.get();
            default ->
                    throw new IllegalStateException("Unexpected value: " + fieldDefinition.getClass().getSimpleName());
        }, containingComponent, locator);
    }

    public ObservableFeatureDefinition(FieldDefinitionRecord fieldDefinitionRecord, ObservableComponent containingComponent, FeatureKey locator) {
        this.containingComponent = containingComponent;
        this.locator = locator;
        fieldDefinitionReference = new AtomicReference<>(fieldDefinitionRecord);
        dataTypeProperty.set(Entity.getFast(fieldDefinitionRecord.dataTypeNid()));
        dataTypeProperty.addListener(this::dataTypeChanged);
        purposeProperty.set(Entity.getFast(fieldDefinitionRecord.purposeNid()));
        purposeProperty.addListener(this::purposeChanged);
        meaningProperty.set(Entity.getFast(fieldDefinitionRecord.meaningNid()));
        meaningProperty.addListener(this::meaningChanged);
    }

     public FeatureKey featureKey() {
         return this.locator;
    }

    @Override
    public int patternNid() {
        return fieldDefinitionReference.get().patternNid();
    }

    @Override
    public PatternFacade pattern() {
        return FieldDefinitionForEntity.super.pattern();
    }

    @Override
    public int patternVersionStampNid() {
        return fieldDefinitionReference.get().patternVersionStampNid();
    }

    public ObservableComponent containingComponent() {
        return containingComponent;
    }

    @Override
    public ConceptEntity dataType() {
        return FieldDefinitionForEntity.super.dataType();
    }

    @Override
    public FieldDataType fieldDataType() {
        return ConceptToDataType.convert(dataType());
    }

    @Override
    public ConceptEntity meaning() {
        return FieldDefinitionForEntity.super.meaning();
    }

    private void dataTypeChanged(ObservableValue<? extends EntityFacade> observableDataType, EntityFacade oldDataType, EntityFacade newDataType) {
        handleVersionChange(FIELD.DATATYPE, observableDataType, newDataType);
    }

    private void purposeChanged(ObservableValue<? extends EntityFacade> observablePurpose, EntityFacade oldPurpose, EntityFacade newPurpose) {
        handleVersionChange(FIELD.PURPOSE, observablePurpose, newPurpose);
    }

    private void meaningChanged(ObservableValue<? extends EntityFacade> observableMeaning, EntityFacade oldMeaning, EntityFacade newMeaning) {
        handleVersionChange(FIELD.MEANING, observableMeaning, newMeaning);
    }

    private void handleVersionChange(FIELD changedField, ObservableValue<? extends EntityFacade> observableValue, EntityFacade newValue) {
        StampRecord oldStamp = Entity.getStamp(fieldDefinitionReference.get().patternVersionStampNid());
        FieldDefinitionRecord oldFieldDefinition = fieldDefinitionReference.get();
        PatternRecord patternRecord = Entity.getFast(oldFieldDefinition.patternNid());
        PatternVersionRecord oldPatternVersion = patternRecord.getVersionFast(oldStamp.nid());
        if (oldStamp.lastVersion().committed()) {
            // Create new version...
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity newStamp = t.getStampForEntities(oldStamp.state(), oldStamp.authorNid(), oldStamp.moduleNid(),
                    oldStamp.pathNid(), patternRecord);

            FieldDefinitionRecord newFieldDefinition = getFieldDefinitionRecord(changedField, newValue, oldFieldDefinition, newStamp);

            fieldDefinitionReference.set(newFieldDefinition);
            int fieldCount = oldPatternVersion.fieldDefinitions().size();
            MutableList<FieldDefinitionRecord> newFieldDefinitionMutableList = Lists.mutable.withInitialCapacity(fieldCount);
            ImmutableList<FieldDefinitionRecord> oldFieldDefinitionList = oldPatternVersion.fieldDefinitions();
            for (int fieldIndexInPattern = 0; fieldIndexInPattern < fieldCount; fieldIndexInPattern++) {
                if (fieldIndexInPattern == newFieldDefinition.indexInPattern()) {
                    newFieldDefinitionMutableList.add(newFieldDefinition);
                } else {
                    newFieldDefinitionMutableList.add(oldFieldDefinitionList.get(fieldIndexInPattern)
                            .withPatternVersionStampNid(newStamp.nid()));
                }
            }
            PatternVersionRecord newVersionRecord = oldPatternVersion.with()
                    .stampNid(newStamp.nid())
                    .fieldDefinitions(newFieldDefinitionMutableList.toImmutable())
                    .build();
            PatternRecord newPattern = patternRecord.analogueBuilder().add(newVersionRecord).build();
            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(newPattern);
        } else {
            FieldDefinitionRecord newFieldDefinition = getFieldDefinitionRecord(changedField, newValue, oldFieldDefinition);
            fieldDefinitionReference.set(newFieldDefinition);
            int fieldCount = oldPatternVersion.fieldDefinitions().size();
            MutableList<FieldDefinitionRecord> newFieldDefinitionMutableList = Lists.mutable.withInitialCapacity(fieldCount);
            ImmutableList<FieldDefinitionRecord> oldFieldDefinitionList = oldPatternVersion.fieldDefinitions();
            for (int fieldIndexInPattern = 0; fieldIndexInPattern < fieldCount; fieldIndexInPattern++) {
                if (fieldIndexInPattern == newFieldDefinition.indexInPattern()) {
                    newFieldDefinitionMutableList.add(newFieldDefinition);
                } else {
                    newFieldDefinitionMutableList.add(oldFieldDefinitionList.get(fieldIndexInPattern));
                }
            }
            PatternVersionRecord newVersionRecord = oldPatternVersion.with()
                    .fieldDefinitions(newFieldDefinitionMutableList.toImmutable())
                    .build();
            PatternRecord newPattern = patternRecord.analogueBuilder().add(newVersionRecord).build();
            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(newPattern);

        }
    }


    private FieldDefinitionRecord getFieldDefinitionRecord(FIELD changedField, EntityFacade newValue, FieldDefinitionRecord oldFieldDefinition, StampEntity newStamp) {
        FieldDefinitionRecord newFieldDefinition = switch (changedField) {
            case DATATYPE ->
                    oldFieldDefinition.with().patternVersionStampNid(newStamp.nid()).dataTypeNid(newValue.nid()).build();
            case PURPOSE ->
                    oldFieldDefinition.with().patternVersionStampNid(newStamp.nid()).purposeNid(newValue.nid()).build();
            case MEANING ->
                    oldFieldDefinition.with().patternVersionStampNid(newStamp.nid()).meaningNid(newValue.nid()).build();
        };
        return newFieldDefinition;
    }


    private FieldDefinitionRecord getFieldDefinitionRecord(FIELD changedField, EntityFacade newValue, FieldDefinitionRecord oldFieldDefinition) {
        FieldDefinitionRecord newFieldDefinition = switch (changedField) {
            case DATATYPE -> oldFieldDefinition.with().dataTypeNid(newValue.nid()).build();
            case PURPOSE -> oldFieldDefinition.with().purposeNid(newValue.nid()).build();
            case MEANING -> oldFieldDefinition.with().meaningNid(newValue.nid()).build();
        };
        return newFieldDefinition;
    }

    public SimpleObjectProperty<EntityFacade> dataTypeProperty() {
        return dataTypeProperty;
    }

    public SimpleObjectProperty<EntityFacade> purposeProperty() {
        return purposeProperty;
    }

    public SimpleObjectProperty<EntityFacade> meaningProperty() {
        return meaningProperty;
    }

    @Override
    public int dataTypeNid() {
        return dataTypeProperty.get().nid();
    }

    @Override
    public ConceptEntity purpose() {
        return EntityHandle.getConceptOrThrow(purposeNid());
    }

    @Override
    public int purposeNid() {
        return purposeProperty.get().nid();
    }

    @Override
    public int meaningNid() {
        return meaningProperty.get().nid();
    }

    @Override
    public int indexInPattern() {
        return fieldDefinitionReference.get().indexInPattern();
    }

    @Override
    public ReadOnlyProperty<? extends Feature<ObservableFeatureDefinition>> featureProperty() {
        return this.featureProperty;
    }

    enum FIELD {DATATYPE, PURPOSE, MEANING}

    /**
     * Editable feature definition wrapper for pattern version field definitions.
     * <p>
     * Provides an editable counterpart to {@link ObservableFeatureDefinition}, though with a simpler
     * implementation since pattern field definitions are typically edited as complete metadata units
     * rather than individual field values.
     * <p>
     * <b>Design Note:</b> Unlike {@link ObservableField.Editable} which extends {@link ObservableFeature.Editable},
     * this class is standalone because {@link ObservableFeatureDefinition} does not extend
     * {@link ObservableFeature} - it directly implements the Feature interface. This maintains
     * architectural consistency while providing API symmetry.
     * <p>
     * <b>Usage Context:</b> For pattern editing, the purpose and meaning are typically edited through
     * {@link ObservablePatternVersion.Editable} properties rather than through individual field definition
     * editors. This class exists primarily for API completeness and potential future enhancements where
     * fine-grained field definition editing may be needed.
     * <p>
     * <b>Example Usage:</b>
     * <pre>{@code
     * // Typically accessed through pattern version editing
     * ObservablePatternVersion.Editable editablePattern = pattern.getEditableVersion(stamp);
     *
     * // Field definitions are part of the pattern version structure
     * // This class provides symmetry with other editable observable components
     * }</pre>
     *
     * @see ObservableFeatureDefinition
     * @see ObservablePatternVersion.Editable
     * @see ObservableFeature.Editable
     */
    public static final class Editable {

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
        Editable(ObservableFeatureDefinition observableFeatureDef, ObservableFeatureDefinition initialValue, int featureIndex) {
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
}
