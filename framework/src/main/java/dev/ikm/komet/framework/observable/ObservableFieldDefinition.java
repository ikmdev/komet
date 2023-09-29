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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.concurrent.atomic.AtomicReference;

public class ObservableFieldDefinition
        implements FieldDefinitionForEntity {
    final AtomicReference<FieldDefinitionRecord> fieldDefinitionReference;
    final SimpleObjectProperty<EntityFacade> dataTypeProperty = new SimpleObjectProperty<>(this, "Field data type");
    final SimpleObjectProperty<EntityFacade> purposeProperty = new SimpleObjectProperty<>(this, "Field purpose");
    final SimpleObjectProperty<EntityFacade> meaningProperty = new SimpleObjectProperty<>(this, "Field meaning");

    public ObservableFieldDefinition(FieldDefinitionRecord fieldDefinitionRecord) {
        fieldDefinitionReference = new AtomicReference<>(fieldDefinitionRecord);
        dataTypeProperty.set(Entity.getFast(fieldDefinitionRecord.dataTypeNid()));
        dataTypeProperty.addListener(this::dataTypeChanged);
        purposeProperty.set(Entity.getFast(fieldDefinitionRecord.purposeNid()));
        purposeProperty.addListener(this::purposeChanged);
        meaningProperty.set(Entity.getFast(fieldDefinitionRecord.meaningNid()));
        meaningProperty.addListener(this::meaningChanged);
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
            case DATATYPE -> oldFieldDefinition.with().patternVersionStampNid(newStamp.nid()).dataTypeNid(newValue.nid()).build();
            case PURPOSE -> oldFieldDefinition.with().patternVersionStampNid(newStamp.nid()).purposeNid(newValue.nid()).build();
            case MEANING -> oldFieldDefinition.with().patternVersionStampNid(newStamp.nid()).meaningNid(newValue.nid()).build();
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

    enum FIELD {DATATYPE, PURPOSE, MEANING}
}
