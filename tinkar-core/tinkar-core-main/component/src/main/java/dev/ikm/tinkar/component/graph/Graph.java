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
package dev.ikm.tinkar.component.graph;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;

import java.util.UUID;

/**
 * TODO: Consider removing and only going with entities...
 * @param <V>
 */
public interface Graph<V extends Vertex> {

    <A> A adapt(GraphAdaptorFactory<A> adaptorFactory);

    /**
     * @param vertexId a universally unique identifier for a vertex
     * @return the vertex associated with the identifier
     */
    V vertex(UUID vertexId);

    V vertex(int vertexIndex);

    ImmutableList<V> vertexMap();

    ImmutableIntObjectMap<ImmutableIntList> successorMap();

    default ImmutableList<V> descendents(V logicVertex) {
        MutableList<V> descendentList = Lists.mutable.empty();
        recursiveAddSuccessors(logicVertex, descendentList);
        return descendentList.toImmutable();
    }

    default void recursiveAddSuccessors(V logicVertex, MutableList<V> descendentList) {
        ImmutableList<V> successorList = successors(logicVertex);
        descendentList.addAllIterable(successorList);
        for (V successor : successorList) {
            recursiveAddSuccessors(successor, descendentList);
        }
    }

    /**
     * @param vertex a vertex to retrieve the successors of
     * @return the successors for the provided vertex
     */
    ImmutableList<V> successors(V vertex);

    ImmutableIntList successors(int vertexIndex);


}
