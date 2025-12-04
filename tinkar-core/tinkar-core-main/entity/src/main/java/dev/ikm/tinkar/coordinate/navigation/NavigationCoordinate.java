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
package dev.ikm.tinkar.coordinate.navigation;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;

import java.util.ArrayList;
import java.util.UUID;

/**
 * In mathematics, and more specifically in graph theory, a directed graph (or digraph)
 * is a graph that is made up of a set of vertices connected by edges, where the edges
 * have a direction associated with them.
 * <p>
 * TODO change Graph NODE to Vertex everywhere since node is overloaded with JavaFx Node (which means something else)...
 */
public interface NavigationCoordinate {

    static IntIdSet defaultNavigationConceptIdentifierNids() {
        return IntIds.set.of(TinkarTerm.INFERRED_NAVIGATION.nid());
    }

    default UUID getNavigationCoordinateUuid() {
        return getNavigationCoordinateUuid(this);
    }

    static UUID getNavigationCoordinateUuid(NavigationCoordinate navigationCoordinate) {
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (int nid : navigationCoordinate.navigationPatternNids().toArray()) {
            Entity.provider().addSortedUuids(uuidList, nid);
        }
        return UUID.nameUUIDFromBytes(uuidList.toString().getBytes());
    }

    //---------------------------

    IntIdSet navigationPatternNids();

    /**
     * Priority list of patterns used to sort vertices. If empty, and sortVertices() is true,
     * then the natural order of the concept description as defined by the language coordinate is used.
     *
     * @return the priority list of patterns to use to sort the vertices.
     */
    default ImmutableList<PatternFacade> verticesSortPatternList() {
        return Lists.immutable.of(verticesSortPatternNidList().intStream()
                .mapToObj(nid -> (PatternFacade) EntityProxy.Pattern.make(nid)).toArray(PatternFacade[]::new));
    }

    /**
     * Priority list of patterns used to sort vertices. If empty, and sortVertices() is true,
     * then the natural order of the concept description as defined by the language coordinate is used.
     *
     * @return the priority list of patterns to use to sort the vertices.
     */
    IntIdList verticesSortPatternNidList();

    default ImmutableSet<Concept> getNavigationIdentifierConcepts() {
        return IntSets.immutable.of(navigationPatternNids().toArray()).collect(nid -> Entity.getFast(nid));
    }

    NavigationCoordinateRecord toNavigationCoordinateRecord();

    default String toUserString() {
        StringBuilder sb = new StringBuilder("Navigators: ");
        for (int nid : navigationPatternNids().toArray()) {
            sb.append("\n     ").append(PrimitiveData.text(nid));
        }
        sb.append("\n\nVertex states:\n").append(vertexStates());
        if (sortVertices()) {
            sb.append("\n\nSort: \n");
            for (int patternNid : verticesSortPatternNidList().toArray()) {
                sb.append("  ");
                sb.append(PrimitiveData.text(patternNid));
                sb.append("\n");
            }
            sb.append("  natural order\n");
        } else {
            sb.append("\n\nSort: none\n");
        }
        return sb.toString();
    }

    StateSet vertexStates();

    /**
     * Sort occurs first by the vertices sort pattern list, and then by the natural order of the concept description
     * as defined by the language coordinate. If the sort pattern list is empty, then the natural order
     * of the concept description as defined by the language coordinate.
     *
     * @return true if vertices will be sorted.
     */
    boolean sortVertices();

}
