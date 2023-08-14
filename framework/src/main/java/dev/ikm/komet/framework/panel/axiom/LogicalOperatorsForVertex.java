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
package dev.ikm.komet.framework.panel.axiom;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.Optional;

public enum LogicalOperatorsForVertex {
    /**
     * The necessary set.
     */
    NECESSARY_SET(TinkarTerm.NECESSARY_SET),

    /**
     * The sufficient set.
     */
    SUFFICIENT_SET(TinkarTerm.SUFFICIENT_SET),

    /**
     * The and.
     */
    AND(TinkarTerm.AND),

    /**
     * The or.
     */
    OR(TinkarTerm.OR),

    /**
     * The disjoint with.
     */
    DISJOINT_WITH(TinkarTerm.DISJOINT_WITH),

    /**
     * The definition root.
     */
    DEFINITION_ROOT(TinkarTerm.DEFINITION_ROOT),

    /**
     * The role all.
     */
    ROLE(TinkarTerm.ROLE_TYPE),


    /**
     * The concept.
     */
    CONCEPT(TinkarTerm.CONCEPT_REFERENCE),

    /**
     * The feature.
     */
    FEATURE(TinkarTerm.FEATURE),

    /**
     * The literal boolean.
     */
    LITERAL(TinkarTerm.LITERAL_VALUE),

    PROPERTY_SET(TinkarTerm.PROPERTY_SET),

    PROPERTY_PATTERN_IMPLICATION(TinkarTerm.PROPERTY_PATTERN_IMPLICATION);

    final ConceptFacade logicalMeaning;

    LogicalOperatorsForVertex(ConceptFacade logicalMeaning) {
        this.logicalMeaning = logicalMeaning;
    }

    public static LogicalOperatorsForVertex get(EntityFacade facade) {
        return get(facade.nid());
    }

    public static LogicalOperatorsForVertex get(int meaningNid) {
        for (LogicalOperatorsForVertex logicalOperator : LogicalOperatorsForVertex.values()) {
            if (logicalOperator.logicalMeaning.nid() == meaningNid) {
                return logicalOperator;
            }
        }
        throw new IllegalStateException("No logical operator for: " + PrimitiveData.text(meaningNid));
    }

    public static LogicalOperatorsForVertex get(EntityVertex logicVertex) {
        return get(logicVertex.getMeaningNid());
    }

    public boolean semanticallyEqual(EntityFacade entityFacade) {
        return entityFacade.nid() == logicalMeaning.nid();
    }

    public boolean semanticallyEqual(int nid) {
        return nid == logicalMeaning.nid();
    }

    public ConceptFacade getPropertyFast(EntityVertex entityVertex) {
        return entityVertex.propertyFast(this.logicalMeaning);
    }

    public <T> Optional<T> getProperty(EntityVertex entityVertex) {
        return entityVertex.property(this.logicalMeaning);
    }
}
