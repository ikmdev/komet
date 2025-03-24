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

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public final class ObservablePatternVersion
        extends ObservableVersion<PatternVersionRecord>
        implements PatternEntityVersion {
    final SimpleObjectProperty<EntityFacade> purposeProperty = new SimpleObjectProperty<>(this, "Pattern purpose");
    final SimpleObjectProperty<EntityFacade> meaningProperty = new SimpleObjectProperty<>(this, "Pattern meaning");
    final ImmutableList<ObservableFieldDefinition> observableFieldDefinitions;

    ObservablePatternVersion(PatternVersionRecord patternVersionRecord) {
        super(patternVersionRecord);
        purposeProperty.set(Entity.getFast(patternVersionRecord.semanticPurposeNid()));
        purposeProperty.addListener(this::purposeChanged);
        meaningProperty.set(Entity.getFast(patternVersionRecord.semanticMeaningNid()));
        meaningProperty.addListener(this::meaningChanged);
        MutableList<ObservableFieldDefinition> mutableFieldDefinitions = Lists.mutable.ofInitialCapacity(patternVersionRecord.fieldDefinitions().size());
        for (FieldDefinitionRecord fieldDefinition : patternVersionRecord.fieldDefinitions()) {
            mutableFieldDefinitions.add(new ObservableFieldDefinition(fieldDefinition));
        }
        this.observableFieldDefinitions = mutableFieldDefinitions.toImmutable();
    }

    private void purposeChanged(ObservableValue<? extends EntityFacade> observableValue, EntityFacade oldValue, EntityFacade newValue) {
        handleChange(FIELDS.PURPOSE, observableValue, newValue);
    }

    private void meaningChanged(ObservableValue<? extends EntityFacade> observableValue, EntityFacade oldValue, EntityFacade newValue) {
        handleChange(FIELDS.MEANING, observableValue, newValue);
    }

    private void handleChange(FIELDS field, ObservableValue<? extends EntityFacade> observableValue, EntityFacade newValue) {
        StampRecord stamp = Entity.getStamp(getVersionRecord().stampNid());
        PatternVersionRecord version = getVersionRecord();
        if (stamp.lastVersion().committed()) {

            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), version.entity());

            // Create new version...
            PatternVersionRecord newVersion = switch (field) {
                case MEANING -> version.with()
                        .semanticMeaningNid(meaningProperty.get().nid())
                        .stampNid(newStamp.nid()).build();
                case PURPOSE -> version.with()
                        .semanticPurposeNid(purposeProperty.get().nid())
                        .stampNid(newStamp.nid()).build();
            };

            PatternRecord analogue = newVersion.chronology().with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        } else {
            PatternVersionRecord newVersion = switch (field) {
                case MEANING -> version.withSemanticMeaningNid(meaningProperty.get().nid());
                case PURPOSE -> version.withSemanticPurposeNid(purposeProperty.get().nid());
            };

            PatternRecord analogue = newVersion.chronology().with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        }
    }

    public SimpleObjectProperty<EntityFacade> purposeProperty() {
        return purposeProperty;
    }

    public SimpleObjectProperty<EntityFacade> meaningProperty() {
        return meaningProperty;
    }

    @Override
    protected PatternVersionRecord withStampNid(int stampNid) {
        return version().withStampNid(stampNid);
    }

    @Override
    public PatternVersionRecord getVersionRecord() {
        return version();
    }

    /**
     * @param value
     * @param i
     * @param <T>
     */
    @Override
    public <T> void writeToDataBase(T value, int i) {

    }

    /**
     * @param value
     * @param i
     * @param <T>
     * @return
     */
    @Override
    public <T> ObservableVersion<PatternVersionRecord> addVersion(T value, int i) {
        return null;
    }

    @Override
    public ImmutableList<ObservableFieldDefinition> fieldDefinitions() {
        return this.observableFieldDefinitions;
    }

    @Override
    public int semanticPurposeNid() {
        return version().semanticPurposeNid();
    }

    @Override
    public int semanticMeaningNid() {
        return version().semanticMeaningNid();
    }

    enum FIELDS {PURPOSE, MEANING}


    @Override
    public ImmutableMap<FieldCategory, ObservableField> getObservableFields() {
        MutableMap<FieldCategory, ObservableField> fieldMap = Maps.mutable.empty();

        int firstStamp = StampCalculator.firstStampTimeOnly(this.entity().stampNids());

        for (FieldCategory field : FieldCategorySet.patternVersionFields()) {
            switch (field) {
                case PUBLIC_ID_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.publicId();
                    int dataTypeNid = TinkarTerm.IDENTIFIER_VALUE.nid();
                    int purposeNid = TinkarTerm.IDENTIFIER_VALUE.nid();
                    int meaningNid = TinkarTerm.IDENTIFIER_VALUE.nid();
                    Entity<EntityVersion> idPattern = Entity.getFast(TinkarTerm.IDENTIFIER_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(idPattern.stampNids());
                    int patternNid = TinkarTerm.IDENTIFIER_PATTERN.nid();
                    int indexInPattern = 0;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid, indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }
                case STATUS_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.state();
                    int dataTypeNid = TinkarTerm.COMPONENT_FIELD.nid();
                    int purposeNid = TinkarTerm.COMPONENT_FOR_SEMANTIC.nid();
                    int meaningNid = TinkarTerm.COMPONENT_FOR_SEMANTIC.nid();
                    Entity<EntityVersion> stampPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(stampPattern.stampNids());
                    int patternNid = TinkarTerm.STAMP_PATTERN.nid();
                    int indexInPattern = 0;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid, indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }
                case TIME_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.time();
                    int dataTypeNid = TinkarTerm.LONG_FIELD.nid();
                    int purposeNid = TinkarTerm.TIME_FOR_VERSION.nid();
                    int meaningNid = TinkarTerm.TIME_FOR_VERSION.nid();
                    Entity<EntityVersion> stampPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(stampPattern.stampNids());
                    int patternNid = TinkarTerm.STAMP_PATTERN.nid();
                    int indexInPattern = 1;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid, indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }
                case AUTHOR_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.authorNid();
                    int dataTypeNid = TinkarTerm.COMPONENT_FIELD.nid();
                    int purposeNid = TinkarTerm.AUTHOR_FOR_VERSION.nid();
                    int meaningNid = TinkarTerm.AUTHOR_FOR_VERSION.nid();
                    Entity<EntityVersion> stampPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(stampPattern.stampNids());
                    int patternNid = TinkarTerm.STAMP_PATTERN.nid();
                    int indexInPattern = 2;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid, indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));

                }
                case MODULE_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.moduleNid();
                    int dataTypeNid = TinkarTerm.COMPONENT_FIELD.nid();
                    int purposeNid = TinkarTerm.MODULE_FOR_VERSION.nid();
                    int meaningNid = TinkarTerm.MODULE_FOR_VERSION.nid();
                    Entity<EntityVersion> stampPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(stampPattern.stampNids());
                    int patternNid = TinkarTerm.STAMP_PATTERN.nid();
                    int indexInPattern = 3;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid, indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }
                case PATH_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.pathNid();
                    int dataTypeNid = TinkarTerm.COMPONENT_FIELD.nid();
                    int purposeNid = TinkarTerm.PATH_FOR_VERSION.nid();
                    int meaningNid = TinkarTerm.PATH_FOR_VERSION.nid();
                    Entity<EntityVersion> stampPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(stampPattern.stampNids());
                    int patternNid = TinkarTerm.STAMP_PATTERN.nid();
                    int indexInPattern = 4;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid, indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }

                case PATTERN_FIELD_DEFINITION_LIST -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.observableFieldDefinitions;
                    int dataTypeNid = TinkarTerm.POLYMORPHIC_FIELD.nid();
                    int purposeNid = TinkarTerm.DEFINITION_DESCRIPTION_TYPE.nid();
                    int meaningNid = TinkarTerm.DEFINITION_DESCRIPTION_TYPE.nid();
                    Entity<EntityVersion> stampPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(stampPattern.stampNids());
                    int patternNid = TinkarTerm.STAMP_PATTERN.nid();
                    int indexInPattern = 4;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid, indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }
            }
        }

        return fieldMap.toImmutable();
    }

}
