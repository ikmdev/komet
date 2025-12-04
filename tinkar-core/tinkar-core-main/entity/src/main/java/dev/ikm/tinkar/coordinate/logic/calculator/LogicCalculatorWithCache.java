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
package dev.ikm.tinkar.coordinate.logic.calculator;

import dev.ikm.tinkar.collection.ConcurrentReferenceHashMap;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.PrimitiveDataRepair;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinate;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;

public class LogicCalculatorWithCache implements LogicCalculator {
    private static final Logger LOG = LoggerFactory.getLogger(LogicCalculatorWithCache.class);
    private static final ConcurrentReferenceHashMap<LogicAndStampCoordinate, LogicCalculatorWithCache> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    ;
    private final LogicCoordinateRecord logicCoordinateRecord;
    private final StampCoordinateRecord stampCoordinateRecord;
    private final StampCalculator stampCalculator;

    // TODO: add cache of axiom trees, sufficient sets, and similar.

    public LogicCalculatorWithCache(LogicCoordinate logicCoordinate, StampCoordinate stampCoordinate) {
        this.logicCoordinateRecord = logicCoordinate.toLogicCoordinateRecord();
        this.stampCoordinateRecord = stampCoordinate.toStampCoordinateRecord();
        this.stampCalculator = StampCalculatorWithCache.getCalculator(stampCoordinateRecord);
    }

    /**
     * Gets the stampCoordinateRecord.
     *
     * @return the stampCoordinateRecord
     */
    public static LogicCalculatorWithCache getCalculator(LogicCoordinate logicCoordinate, StampCoordinate stampCoordinate) {
        return SINGLETONS.computeIfAbsent(new LogicAndStampCoordinate(logicCoordinate.toLogicCoordinateRecord(),
                        stampCoordinate.toStampCoordinateRecord()),
                logicCoordinateRecord -> new LogicCalculatorWithCache(logicCoordinate, stampCoordinate));
    }

    record LogicAndStampCoordinate(LogicCoordinateRecord logicCoordinate, StampCoordinateRecord stampCoordinate) {
    }

    public static class CacheProvider implements CachingService {
        @Override
        public void reset() {
            SINGLETONS.clear();
        }
    }

    @Override
    public LogicCoordinateRecord logicCoordinateRecord() {
        return this.logicCoordinateRecord;
    }


    @Override
    public boolean hasSufficientSet(int nid) {
        int axiomsPatternNid = logicCoordinateRecord.statedAxiomsPatternNid();

        int[] semanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(nid, axiomsPatternNid);
        switch (semanticNids.length) {
            case 0:
                // TODO Raise an alert... ?
                return false;
            case 1:
                Latest<SemanticEntityVersion> latestAxioms = stampCalculator.latest(semanticNids[0]);
                if (latestAxioms.isPresent()) {
                    SemanticEntityVersion axioms = latestAxioms.get();
                    OptionalInt optionalIndexForMeaning = stampCalculator.getIndexForMeaning(axiomsPatternNid, TinkarTerm.EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS.nid());
                    if (optionalIndexForMeaning.isPresent()) {
                        DiTreeEntity axiomsField =
                                (DiTreeEntity) axioms.fieldValues().get(optionalIndexForMeaning.getAsInt());
                        for (EntityVertex vertex : axiomsField.vertexMap()) {
                            if (vertex.getMeaningNid() == TinkarTerm.SUFFICIENT_SET.nid()) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            default:
                // TODO: testing patch code here... Need to move elsewhere.
                if (semanticNids.length == 2) {
                    if (PrimitiveData.get() instanceof PrimitiveDataRepair primitiveDataRepair) {
                        primitiveDataRepair.mergeThenErase(semanticNids[0], semanticNids[1]);
                        return hasSufficientSet(nid);
                    };
                }
                // TODO Raise an alert...
                throw new IllegalStateException("More than one set of axioms for concept: " + Entity.getFast(nid));
        }
    }

    @Override
    public Latest<SemanticEntityVersion> getAxiomSemanticForEntity(int entityNid, StampCalculator stampCalculator, PremiseType premiseType) {
        int[] semanticNids = switch (premiseType) {
            case STATED -> {
                yield PrimitiveData.get().semanticNidsForComponentOfPattern(entityNid, logicCoordinateRecord().statedAxiomsPatternNid());
            }
            case INFERRED -> {
                yield PrimitiveData.get().semanticNidsForComponentOfPattern(entityNid, logicCoordinateRecord().inferredAxiomsPatternNid());
            }
            default -> {
                throw new IllegalStateException("Can't handle PremiseType: " + premiseType);
            }
        };
        if (semanticNids.length == 0) {
            return Latest.empty();
        }
        if (semanticNids.length > 1) {
            LOG.warn("More than one " + premiseType +
                    " logical expression for " + PrimitiveData.text(entityNid));
        }
        return stampCalculator.latest(semanticNids[0]);
    }

    @Override
    public Latest<DiTreeEntity> getAxiomTreeForEntity(int entityNid, StampCalculator stampCalculator, PremiseType premiseType) {
        int[] semanticNids = switch (premiseType) {
            case STATED -> {
                yield PrimitiveData.get().semanticNidsForComponentOfPattern(entityNid, logicCoordinateRecord().statedAxiomsPatternNid());
            }
            case INFERRED -> {
                yield PrimitiveData.get().semanticNidsForComponentOfPattern(entityNid, logicCoordinateRecord().inferredAxiomsPatternNid());
            }
            default -> {
                throw new IllegalStateException("Can't handle PremiseType: " + premiseType);
            }
        };
        if (semanticNids.length == 0) {
            return Latest.empty();
        }
        if (semanticNids.length > 1) {
            LOG.warn("More than one " + premiseType +
                    " logical expression for " + PrimitiveData.text(entityNid));
        }

        Latest<Field<DiTreeEntity>> latestAxiomField = switch (premiseType) {
            case INFERRED -> {
                yield stampCalculator.getFieldForSemanticWithMeaning(semanticNids[0], TinkarTerm.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS);
            }
            case STATED -> {
                yield stampCalculator.getFieldForSemanticWithMeaning(semanticNids[0], TinkarTerm.EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS);
            }
        };
        if (latestAxiomField.isPresent()) {
            Latest<DiTreeEntity> latestAxioms = new Latest<>(latestAxiomField.get().value());
            if (latestAxiomField.isContradicted()) {
                for (Field<DiTreeEntity> contradictedField : latestAxiomField.contradictions()) {
                    latestAxioms.addLatest(contradictedField.value());
                }
            }
            return latestAxioms;
        }
        return Latest.empty();
    }


}
