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

import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.Node;

import java.util.Optional;

public record AxiomSubjectRecord(int axiomIndex, DiTreeEntity axiomTree,
                                 ObservableSemanticVersion semanticContainingAxiom,
                                 PremiseType premiseType,
                                 Node nodeForPopover) {

    public int axiomMeaningNid() {
        return axiomTree.vertexMap().get(axiomIndex).getMeaningNid();
    }

    public ConceptFacade axiomMeaning() {
        return EntityProxy.Concept.make(axiomMeaningNid());
    }

    public boolean axiomMeaningMatchesAny(ConceptFacade... possibleMeanings) {
        for (ConceptFacade meaning: possibleMeanings) {
            if (axiomMeaningNid() == meaning.nid()) {
                return true;
            }
        }
        return false;
    }

    public EntityVertex getAxiomVertex() {
        return axiomTree.vertexMap().get(axiomIndex);
    }

    public Optional<Object> getVertexProperty(int propertyKeyNid) {
        return axiomTree.vertexMap().get(axiomIndex).property(propertyKeyNid);
    }
    public Optional<Object> getVertexProperty(ConceptFacade propertyKey) {
        return axiomTree.vertexMap().get(axiomIndex).property(propertyKey.nid());
    }

    public boolean vertexPropertyEquals(ConceptFacade propertyKey, ConceptFacade propertyValueToMatch) {
        Optional<Object> optionalPropertyValue = axiomTree.vertexMap().get(axiomIndex).property(propertyKey.nid());
        if (optionalPropertyValue.isPresent()) {
            Object currentPropertyValue = optionalPropertyValue.get();
            if (currentPropertyValue instanceof ConceptFacade conceptPropertyValue) {
                return conceptPropertyValue.nid() == propertyValueToMatch.nid();
            }
        }
        return false;
    }

}
