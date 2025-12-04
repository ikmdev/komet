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
package dev.ikm.tinkar.entity.graph;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class JGraphUtil {
    public static class EntityVertexEdge extends DefaultEdge {
        @Override
        public int hashCode() {
            return getSource().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof EntityVertexEdge that) {
                return this.getSource().equals(that.getSource()) &&
                        this.getTarget().equals(that.getTarget());
            }
            return false;
        }
    }

    public static <V extends EntityVertex> Graph<V, DefaultEdge> toJGraph(DiGraphAbstract<V> diGraph) {
        Graph<V, DefaultEdge> directedGraph = new DefaultDirectedGraph<>(EntityVertexEdge.class);
        for (V vertex: diGraph.vertexMap) {
            if (vertex != null) {
                directedGraph.addVertex(vertex);
            }
        }
        for (V sourceVertex: diGraph.vertexMap) {
            if (sourceVertex != null) {
                ImmutableIntList successorList = diGraph.successorMap.get(sourceVertex.vertexIndex);
                if (successorList != null) {
                    for (int destinationIndex: successorList.toArray()) {
                        directedGraph.addEdge(sourceVertex, diGraph.vertex(destinationIndex));
                    }
                }
            }
        }
        return directedGraph;
    }
}
