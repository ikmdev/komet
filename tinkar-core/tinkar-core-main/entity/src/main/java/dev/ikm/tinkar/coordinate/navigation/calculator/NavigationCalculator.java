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
package dev.ikm.tinkar.coordinate.navigation.calculator;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorDelegate;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorDelegate;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.ImmutableList;

public interface NavigationCalculator extends StampCalculatorDelegate, LanguageCalculatorDelegate {

    StampCalculatorWithCache vertexStampCalculator();

    StateSet allowedVertexStates();

    default IntIdList parentsOf(ConceptFacade concept) {
        return parentsOf(concept.nid());
    }

    default IntIdList parentsOf(int conceptNid) {
        if (sortVertices()) {
            return sortedParentsOf(conceptNid);
        }
        return unsortedParentsOf(conceptNid);
    }

    boolean sortVertices();

    IntIdList sortedParentsOf(int conceptNid);

    IntIdList unsortedParentsOf(int conceptNid);

    default IntIdSet descendentsOf(ConceptFacade concept) {
        return descendentsOf(concept.nid());
    }

    IntIdSet descendentsOf(int conceptNid);

    default IntIdSet ancestorsOf(ConceptFacade concept) {
        return descendentsOf(concept.nid());
    }

    IntIdSet ancestorsOf(int conceptNid);

    default IntIdSet kindOf(ConceptFacade concept) {
        return kindOf(concept.nid());
    }

    IntIdSet kindOf(int conceptNid);

    default ImmutableList<Edge> parentEdges(ConceptFacade concept) {
        return childEdges(concept.nid());
    }

    default ImmutableList<Edge> childEdges(int conceptNid) {
        if (sortVertices()) {
            return sortedChildEdges(conceptNid);
        }
        return unsortedChildEdges(conceptNid);
    }

    ImmutableList<Edge> sortedChildEdges(int conceptNid);

    ImmutableList<Edge> unsortedChildEdges(int conceptNid);

    default ImmutableList<Edge> parentEdges(int conceptNid) {
        if (sortVertices()) {
            return sortedParentEdges(conceptNid);
        }
        return unsortedParentEdges(conceptNid);
    }

    ImmutableList<Edge> sortedParentEdges(int conceptNid);

    ImmutableList<Edge> unsortedParentEdges(int conceptNid);

    default ImmutableList<Edge> sortedParentEdges(ConceptFacade concept) {
        return sortedParentEdges(concept.nid());
    }

    default ImmutableList<Edge> unsortedParentEdges(ConceptFacade concept) {
        return unsortedParentEdges(concept.nid());
    }

    default ImmutableList<Edge> childEdges(ConceptFacade concept) {
        return childEdges(concept.nid());
    }

    default ImmutableList<Edge> sortedChildEdges(ConceptFacade concept) {
        return sortedChildEdges(concept.nid());
    }

    default ImmutableList<Edge> unsortedChildEdges(ConceptFacade concept) {
        return unsortedChildEdges(concept.nid());
    }

    default IntIdList childrenOf(ConceptFacade concept) {
        return childrenOf(concept.nid());
    }

    default IntIdList childrenOf(int conceptNid) {
        if (sortVertices()) {
            return sortedChildrenOf(conceptNid);
        }
        return unsortedChildrenOf(conceptNid);
    }

    IntIdList sortedChildrenOf(int conceptNid);

    IntIdList unsortedChildrenOf(int conceptNid);

    IntIdList unsortedUnversionedChildrenOf(int conceptNid);

    IntIdList unsortedUnversionedParentsOf(int conceptNid);

    default IntIdList sortedParentsOf(ConceptFacade concept) {
        return sortedParentsOf(concept.nid());
    }

    default IntIdList sortedChildrenOf(ConceptFacade concept) {
        return sortedChildrenOf(concept.nid());
    }

    default IntIdList unsortedChildrenOf(ConceptFacade concept) {
        return unsortedChildrenOf(concept.nid());
    }

    default IntIdList unsortedParentsOf(ConceptFacade concept) {
        return unsortedParentsOf(concept.nid());
    }

    default IntIdList toSortedList(IntIdSet inputSet) {
        // TODO add pattern sort to implementation...
        return toSortedList(IntIds.list.of(inputSet.toArray()));
    }

    IntIdList toSortedList(IntIdList inputList);

    NavigationCoordinateRecord navigationCoordinate();


    default IntIdList unsortedParentsOf(ConceptFacade concept, PatternFacade patternFacade) {
        return unsortedParentsOf(concept.nid(), patternFacade.nid());
    }

    IntIdList unsortedParentsOf(int conceptNid, int patternNid);


    default boolean isMultiparent(EntityFacade facade) {
        return isMultiparent(facade.nid());
    }

    default boolean isMultiparent(int conceptNid) {
        if (conceptNid == -1
                || conceptNid == TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
            return false;
        }
        return parentsOf(conceptNid).size() > 1;
    }

}
