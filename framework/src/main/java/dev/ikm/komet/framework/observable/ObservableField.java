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
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.transaction.Transaction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public class ObservableField<T> implements Field<T> {

    SimpleObjectProperty<FieldRecord<T>> fieldProperty = new SimpleObjectProperty<>();
    SimpleObjectProperty<T> valueProperty = new SimpleObjectProperty<>();

    public final BooleanProperty refreshProperties = new SimpleBooleanProperty(false);
    public final boolean writeOnEveryChange;

    public ObservableField(FieldRecord<T> fieldRecord, boolean writeOnEveryChange) {
        this.writeOnEveryChange = writeOnEveryChange;
        fieldProperty.set(fieldRecord);
        if (fieldRecord != null) {
            valueProperty.set(fieldRecord.value());
        }
        valueProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleValueChange(newValue);
                fieldProperty.set(field().withValue(newValue));
            }
        });
        refreshProperties.addListener((observable, oldValue, newValue) -> {
            if(!newValue){
                writeToDataBase();
            }
        });

    }
    public ObservableField(FieldRecord<T> fieldRecord) {
        this(fieldRecord, true);
    }

    private void handleValueChange(Object newValue) {
        if (writeOnEveryChange && !refreshProperties.get()) {
            writeToDatabase(newValue);
        }
    }

    public void writeToDataBase() {
        this.writeToDatabase(value());
    }

    public void writeToDatabase(Object newValue) {
        StampRecord stamp = Entity.getStamp(fieldProperty.get().versionStampNid());
        // Get current version
        SemanticVersionRecord version = Entity.getVersionFast(field().nid(), field().versionStampNid());
        SemanticRecord semantic = Entity.getFast(field().nid());
        MutableList fieldsForNewVersion = Lists.mutable.of(version.fieldValues().toArray());
        fieldsForNewVersion.set(fieldIndex(), newValue);

        if (stamp.lastVersion().committed()) {

            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), version.entity());

            // Create new version...
            SemanticVersionRecord newVersion = version.with().fieldValues(fieldsForNewVersion.toImmutable()).stampNid(newStamp.nid()).build();

            SemanticRecord analogue = semantic.with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        } else {
            SemanticVersionRecord newVersion = version.withFieldValues(fieldsForNewVersion.toImmutable());
            // if a version with the same stamp as newVersion exists, that version will be removed
            // prior to adding the new version so you don't get duplicate versions with the same stamp.
            SemanticRecord analogue = semantic.with(newVersion).build();
            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        }
    }

    public FieldRecord<T> field() {
        return fieldProperty.get();
    }

    @Override
    public T value() {
        return field().value();
    }

    @Override
    public FieldDataType fieldDataType() {
        return field().fieldDataType();
    }

    @Override
    public int meaningNid() {
        return field().meaningNid();
    }

    @Override
    public int purposeNid() {
        return field().purposeNid();
    }

    @Override
    public int dataTypeNid() {
        return field().dataTypeNid();
    }

    @Override
    public int fieldIndex() {
        return field().fieldIndex();
    }

    public ObjectProperty<T> valueProperty() {
        return valueProperty;
    }

    public ObjectProperty<FieldRecord<T>> fieldProperty() {
        return fieldProperty;
    }

}
