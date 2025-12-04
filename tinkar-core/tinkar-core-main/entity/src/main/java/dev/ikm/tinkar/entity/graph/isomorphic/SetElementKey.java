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

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.entity.graph.DiTreeAbstract;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiomSemantic;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;

import java.util.Objects;

/**
 * The Class SetElementKey. Equal when equivalent, ignoring vertexIndex.
 *
 *
 */
public class SetElementKey
        implements Comparable<SetElementKey> {
    /** The concepts referenced at node or below. */
    final int vertexIndex;
    final LogicalAxiomSemantic enclosingSetType;
    // Using IntIdList, so that toString method will include text of concept, not just nids.
    final IntIdList conceptsReferencedAtNodeOrBelow;
    final int hashCode;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new relationship key.
     *
     * @param vertexIndex the vertex id
     * @param expression the expression
     */
    public SetElementKey(int vertexIndex, DiTreeAbstract<EntityVertex> expression) {
        this.vertexIndex = vertexIndex;
        this.enclosingSetType = getEnclosingSetType(vertexIndex, expression);
        MutableIntSet conceptsReferencedAtNodeOrBelowCollector = IntSets.mutable.empty();
        processVertexAndChildren(vertexIndex, expression, conceptsReferencedAtNodeOrBelowCollector);
        this.conceptsReferencedAtNodeOrBelow = IntIds.list.of(conceptsReferencedAtNodeOrBelowCollector.toSortedList().toArray());
        this.hashCode = Objects.hash(enclosingSetType,
                IsomorphicResultsLeafHash.makeNidListHash(expression.vertex(vertexIndex).getMeaningNid(),
                        this.conceptsReferencedAtNodeOrBelow.toArray()));
    }

    /**
     * Determines the enclosing set type of a vertex within a given expression.
     *
     * @param vertexIndex  the index of the vertex
     * @param expression   the expression containing the vertex
     * @return the logical axiom semantic representing the enclosing set type
     * @throws IllegalStateException if the vertex is not contained within a known set type
     */
	private LogicalAxiomSemantic getEnclosingSetType(int vertexIndex, DiTreeAbstract<EntityVertex> expression) {
		if (expression.hasPredecessorVertexWithMeaning(vertexIndex, TinkarTerm.NECESSARY_SET.nid())) {
			return LogicalAxiomSemantic.NECESSARY_SET;
		}
		if (expression.hasPredecessorVertexWithMeaning(vertexIndex, TinkarTerm.SUFFICIENT_SET.nid())) {
			return LogicalAxiomSemantic.SUFFICIENT_SET;
		}
		if (expression.hasPredecessorVertexWithMeaning(vertexIndex, TinkarTerm.PROPERTY_SET.nid())) {
			return LogicalAxiomSemantic.PROPERTY_SET;
		}
		if (expression.hasPredecessorVertexWithMeaning(vertexIndex, TinkarTerm.DATA_PROPERTY_SET.nid())) {
			return LogicalAxiomSemantic.DATA_PROPERTY_SET;
		}
		if (expression.hasPredecessorVertexWithMeaning(vertexIndex, TinkarTerm.INTERVAL_PROPERTY_SET.nid())) {
			return LogicalAxiomSemantic.INTERVAL_PROPERTY_SET;
		}
		if (expression.hasPredecessorVertexWithMeaning(vertexIndex, TinkarTerm.INCLUSION_SET.nid())) {
			return LogicalAxiomSemantic.INCLUSION_SET;
		}
		throw new IllegalStateException(
				"vertex " + vertexIndex + " is not contained within a known set type: " + expression);
	}

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Adds the nodes.
     *
     * @param vertexIndex the vertex index
     * @param tree the tree
     */
    private void processVertexAndChildren(int vertexIndex, DiTreeAbstract<EntityVertex> tree, MutableIntSet conceptsReferencedAtNodeOrBelowCollector) {
        final EntityVertex vertex = tree.vertex(vertexIndex);

        tree.vertex(vertexIndex).addConceptsReferencedByVertex(conceptsReferencedAtNodeOrBelowCollector);
        for (EntityVertex childVertex: tree.successors(vertex)) {
            processVertexAndChildren(childVertex.vertexIndex(), tree, conceptsReferencedAtNodeOrBelowCollector);
        }
    }

    //~--- methods -------------------------------------------------------------

    /**
     * Compare to.
     *
     * @param o the o
     * @return the int
     */
    @Override
    public int compareTo(SetElementKey o) {
        if (this.hashCode != o.hashCode) {
            return Integer.compare(this.hashCode, o.hashCode);
        }
        if (this.enclosingSetType != o.enclosingSetType) {
            return enclosingSetType.compareTo(o.enclosingSetType);
        }
        int comparison = Integer.compare(this.conceptsReferencedAtNodeOrBelow.size(), o.conceptsReferencedAtNodeOrBelow.size());

        if (comparison != 0) {
            return comparison;
        }

        final int[] thisKeys  = this.conceptsReferencedAtNodeOrBelow.toArray();
        final int[] otherKeys = o.conceptsReferencedAtNodeOrBelow.toArray();

        for (int i = 0; i < thisKeys.length; i++) {
            if (thisKeys[i] != otherKeys[i]) {
                return Integer.compare(thisKeys[i], otherKeys[i]);
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SetElementKey otherKey) {
            return compareTo(otherKey) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return "SetElementKey{" + "vertexId=" + vertexIndex + ", in necessary set=" + enclosingSetType + ", conceptsReferencedAtNodeOrBelow=" + conceptsReferencedAtNodeOrBelow + '}';
    }
}
