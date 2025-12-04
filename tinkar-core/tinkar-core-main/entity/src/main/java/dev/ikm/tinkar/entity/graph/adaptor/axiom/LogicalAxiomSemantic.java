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
package dev.ikm.tinkar.entity.graph.adaptor.axiom;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

/**
 * Provide a standard means to connect implementation classes and terminology.
 *
 * TODO: Not sure if this class is redundant given the sealed logical axiom interfaces. Consider consolidation.
 */
public enum LogicalAxiomSemantic {
    AND(TinkarTerm.AND, LogicalAxiom.Atom.Connective.And.class),
    CONCEPT(TinkarTerm.CONCEPT_REFERENCE, LogicalAxiom.Atom.ConceptAxiom.class),
    DEFINITION_ROOT(TinkarTerm.DEFINITION_ROOT, LogicalAxiom.Atom.DefinitionRoot.class),
    NECESSARY_SET(TinkarTerm.NECESSARY_SET, LogicalAxiom.Atom.LogicalSet.NecessarySet.class),
    OR(TinkarTerm.OR, LogicalAxiom.Atom.Connective.Or.class),
    PROPERTY_SEQUENCE_IMPLICATION(TinkarTerm.PROPERTY_SEQUENCE_IMPLICATION, LogicalAxiom.Atom.PropertySequenceImplication.class),
    PROPERTY_SET(TinkarTerm.PROPERTY_SET, LogicalAxiom.Atom.LogicalSet.PropertySet.class),
    DATA_PROPERTY_SET(TinkarTerm.DATA_PROPERTY_SET, LogicalAxiom.Atom.LogicalSet.DataPropertySet.class),
    INTERVAL_PROPERTY_SET(TinkarTerm.INTERVAL_PROPERTY_SET, LogicalAxiom.Atom.LogicalSet.IntervalPropertySet.class),
    ROLE(TinkarTerm.ROLE, LogicalAxiom.Atom.TypedAtom.Role.class),
    INTERVAL_ROLE(TinkarTerm.INTERVAL_ROLE, LogicalAxiom.Atom.TypedAtom.IntervalRole.class),
    SUFFICIENT_SET(TinkarTerm.SUFFICIENT_SET, LogicalAxiom.Atom.LogicalSet.SufficientSet.class),
    DISJOINT_WITH(TinkarTerm.DISJOINT_WITH, LogicalAxiom.Atom.DisjointWithAxiom.class),
    FEATURE(TinkarTerm.FEATURE, LogicalAxiom.Atom.TypedAtom.Feature.class),
    INCLUSION_SET(TinkarTerm.INCLUSION_SET, LogicalAxiom.Atom.LogicalSet.InclusionSet.class);

    public final int nid;
    public final Class<? extends LogicalAxiom> axiomClass;

    LogicalAxiomSemantic(ConceptFacade meaningFacade, Class<? extends LogicalAxiom> axiom) {
        this.nid = meaningFacade.nid();
        this.axiomClass = axiom;
    }

    public static LogicalAxiomSemantic get(ConceptFacade meaningFacade) {
        return get(meaningFacade.nid());
    }

    public static LogicalAxiomSemantic get(int meaningNid) {
        for (LogicalAxiomSemantic meaning : LogicalAxiomSemantic.values()) {
            if (meaning.nid == meaningNid) {
                return meaning;
            }
        }
        throw new IllegalStateException("No meaning for nid: " + meaningNid + " " + PrimitiveData.text(meaningNid));
    }

}
