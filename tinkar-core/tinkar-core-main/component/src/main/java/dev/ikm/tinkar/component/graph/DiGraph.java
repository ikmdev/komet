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

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;

public interface DiGraph<V extends Vertex> extends Graph<V> {
    enum VertexType {LEAF, PREDECESSOR};

    /**
     * A graph can have multiple roots.
     *
     * @return The roots of a directed graph.
     */
    ImmutableList<V> roots();

    /**
     * A directed graph can have multiple predecessors.
     *
     * @param vertex to get the predecessors for
     * @return The predecessors of the provided vertex. Empty list if a root node.
     */
    ImmutableList<V> predecessors(V vertex);

    ImmutableIntObjectMap<ImmutableIntList> predecessorMap();

    VertexType vertexType(V vertex);


    @Override
    default ImmutableIntList successors(int vertexIndex) {
        return successorMap().get(vertexIndex);
    }

}
