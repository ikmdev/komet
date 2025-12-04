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
package dev.ikm.tinkar.entity.graph.isomorphic;
//~--- JDK imports ------------------------------------------------------------

import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;

import java.util.List;
import java.util.TreeSet;

//~--- interfaces -------------------------------------------------------------

/**
 * Computed results of an isomorphic comparison of two expressions: the
 * reference expression and the comparison expression.
 */
public interface IsomorphicResults {
    public enum EndPoints {
        INDEXES_EQUAL, FULL_COMPARISON
    }
    /**
     * Gets the added relationship roots.
     *
     * @return roots for connected nodes that comprise is-a, typed relationships, or relationship groups that are
     *  in the referenceExpression, but not in the comparisonExpression.
     */
    List<EntityVertex> getAddedSetElements();

    /**
     * Returns the TreeSet of SetElementKeys that represent the added set elements.
     * <p>
     * The SetElementKey class is used to uniquely identify set elements based on their vertex index and
     * the concepts referenced at or below the vertex.
     *
     * @return TreeSet of SetElementKeys representing the added set elements.
     */
    TreeSet<SetElementKey> getAddedSetElementKeys();
    /**
     * Gets the additional node roots.
     *
     * @return roots for connected nodes that are in the reference expression, but not in the
     * common expression.
     */
    List<EntityVertex> getAdditionalVertexRoots();

    /**
     * Gets the comparison expression.
     *
     * @return the expression that is compared to the reference expression to compute
     * isomorphic results.
     */
    DiTreeEntity getComparisonTree();

    /**
     * Gets the deleted node roots.
     *
     * @return roots for connected nodes that are in the comparison expression, but are not in
     * the common expression.
     */
    List<EntityVertex> getDeletedVertexRoots();

    /**
     * Gets the deleted relationship roots.
     *
     * @return roots for connected nodes that comprise is-a, typed relationships, or relationship groups that are
     * in the comparisonExpression, but not in the referenceExpression.
     */
    List<EntityVertex> getDeletedSetElements();

    /**
     * Retrieve the TreeSet of SetElementKeys that represent the deleted set elements.
     *
     * @return A TreeSet of SetElementKeys, where each key uniquely identifies a deleted set element
     *         based on its vertex index and the concepts referenced at or below the vertex.
     */
    TreeSet<SetElementKey> getDeletedSetElementKeys();

    /**
     * Gets the isomorphic expression.
     *
     * @return an expression containing only the connected set of nodes representing
     *  the maximal common isomorphism between the two expressions that are connected
     *  to their respective roots.
     */
    DiTreeEntity getIsomorphicTree();

    /**
     *
     *   @return an expression containing a merger of all the nodes in the reference and comparison expression.
     */
    DiTreeEntity getMergedTree();

    /**
     * Gets the reference expression.
     *
     * @return the expression that isomorphic results are computed with respect to.
     */
    DiTreeEntity getReferenceTree();

    /**
     * Gets the shared relationship roots.
     *
     * @return roots for connected nodes that comprise is-a, typed relationships, or relationship groups that are
     *  in both the referenceExpression and in the comparisonExpression.
     */
    List<EntityVertex> getSharedSetElements();

    /**
     *
     * @return true if the evaluation expressions are equivalent.
     */
    boolean equivalent();
}
