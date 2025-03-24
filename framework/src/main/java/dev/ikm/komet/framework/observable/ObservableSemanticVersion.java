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

import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.tinkar.entity.*;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public final class ObservableSemanticVersion
        extends ObservableVersion<SemanticVersionRecord>
        implements SemanticEntityVersion {
    ObservableSemanticVersion(SemanticVersionRecord semanticVersionRecord) {
        super(semanticVersionRecord);
    }

    @Override
    protected SemanticVersionRecord withStampNid(int stampNid) {
        return version().withStampNid(stampNid);
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

    /**
     * @param value
     * @param fieldIndex
     * @param <T>
     */
    @Override
    public <T>  ObservableSemanticVersion addVersion(T value, int fieldIndex) {
        MutableList fieldsForNewVersion = Lists.mutable.of(fieldValues().toArray());
        fieldsForNewVersion.set(fieldIndex, value);
        StampRecord stamp = Entity.getStamp(stampNid());
        SemanticVersionRecord newVersion = null;
        if (stamp.lastVersion().committed()) {
            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity<?> newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), entity());
            // Create new version...
            newVersion = version().with().fieldValues(fieldsForNewVersion.toImmutable()).stampNid(newStamp.nid()).build();
        }else {
            newVersion = version().withFieldValues(fieldsForNewVersion.toImmutable());
        }
        return new ObservableSemanticVersion(newVersion);
    }

    /**
     * @param value
     * @param fieldIndex
     * @param <T>
     */
    @Override
    public <T> void writeToDataBase(T value, int fieldIndex) {
        MutableList fieldsForNewVersion = Lists.mutable.of(fieldValues().toArray());
        fieldsForNewVersion.set(fieldIndex, value);
        StampRecord stamp = Entity.getStamp(stampNid());
        int stampNidVersionRecord = getVersionRecord().stampNid();
        SemanticRecord semantic = Entity.getFast(nid());
        System.out.println("STAMP NIDS : stampNid() " + stampNid() + "  stampNidVersionRecord  " + stampNidVersionRecord);
        if (stamp.lastVersion().committed()) {
            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), entity());

            // Create new version...
            SemanticVersionRecord newVersion = version().with().fieldValues(fieldsForNewVersion.toImmutable()).stampNid(newStamp.nid()).build();

            SemanticRecord analogue = semantic.with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        }else {
            SemanticVersionRecord newVersion = null;
            System.out.println("Entity.getStamp(version().stampNid()).publicId() : " + Entity.getStamp(version().stampNid()).publicId());
            System.out.println("IS EMPTY : " + Transaction.forStamp(Entity.getStamp(version().stampNid()).publicId()).isEmpty());
            if(Transaction.forStamp(Entity.getStamp(version().stampNid()).publicId()).isEmpty()){
                Transaction t = Transaction.make();
                // newStamp already written to the entity store.
                StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), entity());
                newVersion = version().withFieldValues(fieldsForNewVersion.toImmutable()).withStampNid(newStamp.nid());
            }else{
                newVersion = version().withFieldValues(fieldsForNewVersion.toImmutable());
            }
            // if a version with the same stamp as newVersion exists, that version will be removed
            // prior to adding the new version so you don't get duplicate versions with the same stamp.
            SemanticRecord analogue = semantic.with(newVersion).build();
            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        }

    }

    @Override
    public ImmutableList<Object> fieldValues() {
        return version().fieldValues();
    }

    @Override
    public ImmutableList<ObservableField> fields(PatternEntityVersion patternVersion) {
        ObservableField[] fieldArray = new ObservableField[fieldValues().size()];
        for (int indexInPattern = 0; indexInPattern < fieldArray.length; indexInPattern++) {
            Object value = fieldValues().get(indexInPattern);
            FieldDefinitionForEntity fieldDef = patternVersion.fieldDefinitions().get(indexInPattern);
            FieldDefinitionRecord fieldDefinitionRecord = new FieldDefinitionRecord(fieldDef.dataTypeNid(),
                    fieldDef.purposeNid(), fieldDef.meaningNid(), patternVersion.stampNid(), patternVersion.nid(), indexInPattern);
            fieldArray[indexInPattern] = new ObservableField(new FieldRecord(value, this.nid(), this.stampNid(), fieldDefinitionRecord));
        }
        return Lists.immutable.of(fieldArray);
    }


    @Override
    public ImmutableMap<FieldCategory, ObservableField> getObservableFields() {
        MutableMap<FieldCategory, ObservableField> fieldMap = Maps.mutable.empty();

        int firstStamp = StampCalculator.firstStampTimeOnly(this.entity().stampNids());

        for (FieldCategory field : FieldCategorySet.semanticVersionFields()) {
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

                case SEMANTIC_FIELD_LIST -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    StampCalculatorWithCache calculator =
                            StampCalculatorWithCache.getCalculator(Coordinates.Stamp.DevelopmentLatest());
                    Latest<PatternEntityVersion> latestPattern = calculator.latestPatternEntityVersion(this.patternNid());
                    Object value = this.fields(latestPattern.get());
                    int dataTypeNid = TinkarTerm.POLYMORPHIC_FIELD.nid();
                    int purposeNid = TinkarTerm.SEMANTIC_FIELDS_ASSEMBLAGE.nid();
                    int meaningNid = TinkarTerm.SEMANTIC_FIELDS_ASSEMBLAGE.nid();
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
