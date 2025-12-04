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

import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorDelegate;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.EntityFacade;

public interface LogicCalculatorDelegate extends LogicCalculator, StampCalculatorDelegate {
    @Override
    default LogicCoordinateRecord logicCoordinateRecord() {
        return logicCalculator().logicCoordinateRecord();
    }

    @Override
    default boolean hasSufficientSet(int nid) {
        return logicCalculator().hasSufficientSet(nid);
    }

    @Override
    default Latest<DiTreeEntity> getAxiomTreeForEntity(int entityNid, StampCalculator stampCalculator, PremiseType premiseType) {
        return logicCalculator().getAxiomTreeForEntity(entityNid, stampCalculator, premiseType);
    }

    default Latest<SemanticEntityVersion> getAxiomSemanticForEntity(int entityNid, StampCalculator stampCalculator, PremiseType premiseType) {
        return logicCalculator().getAxiomSemanticForEntity(entityNid, stampCalculator, premiseType);
    }

    LogicCalculator logicCalculator();

    default Latest<SemanticEntityVersion> getAxiomSemanticForEntity(int entityNid, PremiseType premiseType) {
        return logicCalculator().getAxiomSemanticForEntity(entityNid, stampCalculator(), premiseType);
    }

    default Latest<SemanticEntityVersion> getStatedAxiomSemanticForEntity(int entityNid) {
        return logicCalculator().getAxiomSemanticForEntity(entityNid, stampCalculator(), PremiseType.STATED);
    }

    default Latest<SemanticEntityVersion> getInferredAxiomSemanticForEntity(int entityNid) {
        return logicCalculator().getAxiomSemanticForEntity(entityNid, stampCalculator(), PremiseType.INFERRED);
    }

    default Latest<SemanticEntityVersion> getAxiomSemanticForEntity(EntityFacade entity, PremiseType premiseType) {
        return logicCalculator().getAxiomSemanticForEntity(entity.nid(), stampCalculator(), premiseType);
    }

    default Latest<SemanticEntityVersion> getStatedAxiomSemanticForEntity(EntityFacade entity) {
        return logicCalculator().getAxiomSemanticForEntity(entity.nid(), stampCalculator(), PremiseType.STATED);
    }

    default Latest<SemanticEntityVersion> getInferredAxiomSemanticForEntity(EntityFacade entity) {
        return logicCalculator().getAxiomSemanticForEntity(entity.nid(), stampCalculator(), PremiseType.INFERRED);
    }


    default Latest<DiTreeEntity> getAxiomTreeForEntity(int entityNid, PremiseType premiseType) {
        return logicCalculator().getAxiomTreeForEntity(entityNid, stampCalculator(), premiseType);
    }

    default Latest<DiTreeEntity> getStatedAxiomTreeForEntity(int entityNid) {
        return logicCalculator().getAxiomTreeForEntity(entityNid, stampCalculator(), PremiseType.STATED);
    }

    default Latest<DiTreeEntity> getInferredAxiomTreeForEntity(int entityNid) {
        return logicCalculator().getAxiomTreeForEntity(entityNid, stampCalculator(), PremiseType.INFERRED);
    }

    default Latest<DiTreeEntity> getAxiomTreeForEntity(EntityFacade entity, PremiseType premiseType) {
        return logicCalculator().getAxiomTreeForEntity(entity.nid(), stampCalculator(), premiseType);
    }

    default Latest<DiTreeEntity> getStatedAxiomTreeForEntity(EntityFacade entity) {
        return logicCalculator().getAxiomTreeForEntity(entity.nid(), stampCalculator(), PremiseType.STATED);
    }

    default Latest<DiTreeEntity> getInferredAxiomTreeForEntity(EntityFacade entity) {
        return logicCalculator().getAxiomTreeForEntity(entity.nid(), stampCalculator(), PremiseType.INFERRED);
    }

}
