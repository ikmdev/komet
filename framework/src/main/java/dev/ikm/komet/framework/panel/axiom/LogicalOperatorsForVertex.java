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

import dev.ikm.komet.terms.KometTerm;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.KernelTerm;

import java.util.Optional;

// TODO: can this class be replaced with LogicalAxiomSemantic?
public enum LogicalOperatorsForVertex {
    /**
     * The necessary set.
     */
    NECESSARY_SET(KernelTerm.NECESSARY_SET),

    /**
     * The sufficient set.
     */
    SUFFICIENT_SET(KernelTerm.SUFFICIENT_SET),

    /**
     * The and.
     */
    AND(KernelTerm.AND),

    /**
     * The or.
     */
    OR(KernelTerm.OR),

    /**
     * The disjoint with.
     */
    DISJOINT_WITH(KernelTerm.DISJOINT_WITH),

    /**
     * The definition root.
     */
    DEFINITION_ROOT(KernelTerm.DEFINITION_ROOT),

    /**
     * A role
     */
	ROLE(KernelTerm.ROLE),

	INTERVAL_ROLE(KernelTerm.INTERVAL_ROLE),

    /**
     * The concept.
     */
    CONCEPT(KernelTerm.CONCEPT_REFERENCE),

    /**
     * The feature.
     */
    FEATURE(KernelTerm.FEATURE),

    PROPERTY_SET(KernelTerm.PROPERTY_SET),
    DATA_PROPERTY_SET(KernelTerm.DATA_PROPERTY_SET),
    INTERVAL_PROPERTY_SET(KernelTerm.INTERVAL_PROPERTY_SET),

    // TODO: Retire property pattern implication when starter set stable.
    PROPERTY_PATTERN_IMPLICATION(KometTerm.PROPERTY_PATTERN_IMPLICATION),

    PROPERTY_SEQUENCE_IMPLICATION(KernelTerm.PROPERTY_SEQUENCE_IMPLICATION),

    INCLUSION_SET(KernelTerm.INCLUSION_SET);

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
