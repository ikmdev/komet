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

import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;

import java.util.Optional;

public final class ObservableSemantic
        extends ObservableEntity<ObservableSemanticVersion, SemanticVersionRecord>
        implements SemanticEntity<ObservableSemanticVersion> {
    ObservableSemantic(SemanticEntity<SemanticVersionRecord> semanticEntity) {
        super(semanticEntity);
    }

    @Override
    protected ObservableSemanticVersion wrap(SemanticVersionRecord version) {
        return new ObservableSemanticVersion(version);
    }

    @Override
    public ObservableSemanticSnapshot getSnapshot(ViewCalculator calculator) {
        return new ObservableSemanticSnapshot(calculator, this);
    }

    /**
     * @param value
     * @param fieldIndex
     * @param viewCalculator
     * @param <T>
     */
    public <T> void createNewVersionAndTransaction(T value, int fieldIndex, ViewCalculator viewCalculator) {
        ObservableSemanticSnapshot observableSemanticSnapshot = getSnapshot(viewCalculator);
        Latest<ObservableSemanticVersion> observableSemanticVersionLatest = observableSemanticSnapshot.getLatestVersion();
        ObservableSemanticVersion version = observableSemanticVersionLatest.get();
        MutableList<Object> fieldsForNewVersion = Lists.mutable.of(version.fieldValues().toArray());
        fieldsForNewVersion.set(fieldIndex, value);
        SemanticRecord semantic = (SemanticRecord) version.entity();
        StampRecord stamp = Entity.getStamp(version.stampNid());
        SemanticVersionRecord newVersion = null;
        if (!version.uncommitted()) {
            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity<?> newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), entity());
            // Create new version...
            newVersion = version.getVersionRecord().with().fieldValues(fieldsForNewVersion.toImmutable()).stampNid(newStamp.nid()).build();
        }else {
            newVersion = version.getVersionRecord().withFieldValues(fieldsForNewVersion.toImmutable());
        }
        // Create new version...
        //this.versionProperty.add(this.wrap(newVersion));
        SemanticRecord analogue = semantic.with(newVersion).build();
        // Entity provider will broadcast the nid of the changed entity.
        Entity.provider().putEntity(analogue);
        ObservableEntity.updateVersions(Entity.getFast(version.nid()), this);
    }
    @Override
    public int referencedComponentNid() {
        return ((SemanticEntity) entity()).referencedComponentNid();
    }

    @Override
    public int patternNid() {
        return ((SemanticEntity) entity()).patternNid();
    }

    public static ObservableSemanticSnapshot getSemanticSnapshot(int semanticNid, ViewCalculator calculator) {
        ObservableSemantic observableSemantic = get(semanticNid);
        return observableSemantic.getSnapshot(calculator);
    }

    public static Optional<ObservableSemanticSnapshot> getStatedAxiomSnapshot(int conceptNid, ViewCalculator calculator) {
        return getAxiomSnapshot(conceptNid, calculator.viewCoordinateRecord().logicCoordinate().statedAxiomsPatternNid(),
                calculator);
    }

    public static Optional<ObservableSemanticSnapshot> getInferredAxiomSnapshot(int conceptNid, ViewCalculator calculator) {
        return getAxiomSnapshot(conceptNid, calculator.viewCoordinateRecord().logicCoordinate().inferredAxiomsPatternNid(),
                calculator);
    }
    public static Optional<ObservableSemanticSnapshot> getAxiomSnapshot(int conceptNid, PremiseType premiseType, ViewCalculator calculator) {
        return switch (premiseType) {
            case STATED -> getAxiomSnapshot(conceptNid, calculator.viewCoordinateRecord().logicCoordinate().statedAxiomsPatternNid(),
                    calculator);
            case INFERRED -> getAxiomSnapshot(conceptNid, calculator.viewCoordinateRecord().logicCoordinate().inferredAxiomsPatternNid(),
                    calculator);
        };
    }

    public static Optional<ObservableSemanticSnapshot> getAxiomSnapshot(int conceptNid, int axiomPatterNid, ViewCalculator calculator) {


        int[] axiomSemanticNids = EntityService.get().semanticNidsForComponentOfPattern(conceptNid, axiomPatterNid);
        if (axiomSemanticNids.length == 0) {
            return Optional.empty();
        } else if (axiomSemanticNids.length > 1) {
            throw new IllegalStateException("To many axiom semantics in " +
                    calculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(axiomPatterNid) +
                    " for " + calculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptNid));
        }
        ObservableSemanticSnapshot axiomSemanticSnapshot = getSemanticSnapshot(axiomSemanticNids[0], calculator);
        return Optional.of(axiomSemanticSnapshot);
    }

    @Override
    public ImmutableMap<FieldCategory, ObservableField> getObservableFields() {
        MutableMap<FieldCategory, ObservableField> fieldMap = Maps.mutable.empty();

        int firstStamp = StampCalculator.firstStampTimeOnly(this.entity().stampNids());

        for (FieldCategory field: FieldCategorySet.semanticFields()) {
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
                            patternVersionStampNid, patternNid,  indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }
                case SEMANTIC_PATTERN_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.versions();
                    int dataTypeNid = TinkarTerm.COMPONENT_FIELD.nid();
                    int purposeNid = TinkarTerm.COMPONENT_FOR_SEMANTIC.nid();
                    int meaningNid = TinkarTerm.COMPONENT_FOR_SEMANTIC.nid();
                    Entity<EntityVersion> stampPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(stampPattern.stampNids());
                    int patternNid = TinkarTerm.STAMP_PATTERN.nid();
                    int indexInPattern = 0;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid,  indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }
                case SEMANTIC_REFERENCED_COMPONENT_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.versions();
                    int dataTypeNid = TinkarTerm.COMPONENT_FIELD.nid();
                    int purposeNid = TinkarTerm.ASSEMBLAGE.nid();
                    int meaningNid = TinkarTerm.ASSEMBLAGE.nid();
                    Entity<EntityVersion> stampPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(stampPattern.stampNids());
                    int patternNid = TinkarTerm.STAMP_PATTERN.nid();
                    int indexInPattern = 0;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid,  indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }

                case COMPONENT_VERSIONS_LIST -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.versions();
                    int dataTypeNid = TinkarTerm.VERSION_LIST_FOR_CHRONICLE.nid();
                    int purposeNid = TinkarTerm.VERSION_LIST_FOR_CHRONICLE.nid();
                    int meaningNid = TinkarTerm.VERSION_LIST_FOR_CHRONICLE.nid();
                    Entity<EntityVersion> idPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(idPattern.stampNids());
                    int patternNid = TinkarTerm.IDENTIFIER_PATTERN.nid();
                    int indexInPattern = 0;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid,  indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }

            }
        }

        return fieldMap.toImmutable();
    }

}
