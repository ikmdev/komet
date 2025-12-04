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

import dev.ikm.tinkar.common.id.VertexId;
import dev.ikm.tinkar.component.Concept;
import org.eclipse.collections.api.RichIterable;

import java.util.Optional;

public interface Vertex {

    /**
     * @return universally unique identifier for this vertex
     */
    VertexId vertexId();

    /**
     * The index of this vertex within its graph. The index is locally
     * unique within a graph, but not across graphs, or different versions of the same graph.
     * Vertex index is not used in equality or hash calculations.
     *
     * @return the vertex index.
     */
    int vertexIndex();

    /**
     * Concept that represents the meaning of this vertex.
     *
     * @return
     */
    Concept meaning();

    /**
     * @param propertyConcept
     * @param <T>             Type of the property object
     * @return An optional object that is associated with the properly concept.
     */
    <T extends Object> Optional<T> property(Concept propertyConcept);


    Optional<? extends Concept> propertyAsConcept(Concept propertyConcept);


    /**
     * @param propertyConcept
     * @param <T>             Type of the property object
     * @return A possibly null object that is associated with the properly concept.
     */
    <T extends Object> T propertyFast(Concept propertyConcept);

    /**
     * @return keys for the populated properties.
     */
    <C extends Concept> RichIterable<C> propertyKeys();

}
