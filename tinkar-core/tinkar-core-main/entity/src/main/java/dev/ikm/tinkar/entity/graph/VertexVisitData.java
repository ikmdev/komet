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

import dev.ikm.tinkar.common.util.ArrayUtil;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import java.util.BitSet;
import java.util.OptionalInt;

public class VertexVisitData {


    /** The leaf nodes. */
    private final BitSet leafNodes;
    private final BitSet necessarySets;
    private final BitSet sufficientSets;
    private final BitSet propertySets;
    private final BitSet inclusionSets;

    /** The max depth. */
    private int maxDepth = 0;

    /** The nodes visited. */
    private int vertexesVisited = 0;

    /** The distance list. For each node, the distance from the root is tracked in this list, where the node is
     represented by the index of the list, and the distance is represented by the value of the list at the index. */
    protected final int[] distanceArray;

    /** The discovery sequence list. For each node, the discovery sequence is tracked in this list, where the node is
     represented by the index of the list, and the discovery sequence is represented by the value of the list at the index.*/
    protected final int[] discoverySequenceArray;

    /** The finish sequence list. For each node, the finish sequence is tracked in this list, where the node is
     represented by the index of the list, and the finish sequence is represented by the value of the list at the index.*/
    protected final int[] finishSequenceArray;

    /** The predecessor index list. For each node, the identifier of it's predecessor is provided, where the node
     is represented by the index of the list, and the identifier of the predecessor is represented by the value of the
     list at the index. */
    protected final int[] predecessorIndexArray;

    /** The start index for this traversal. */
    private int startIndex = -1;

    /**
     * The sequence in the traversal.
     */
    private int sequence = 0;

    MutableIntObjectMap<BitSet> depthVertexMap = IntObjectMaps.mutable.empty();

    final VisitProcessor vertexStartConsumer;
    final VisitProcessor vertexEndConsumer;

    public VertexVisitData(int graphSize) {
        this(graphSize, null);
    }
    public VertexVisitData(int graphSize, VisitProcessor vertexStartConsumer) {
        this(graphSize, vertexStartConsumer, null);
    }
    public VertexVisitData(int graphSize, VisitProcessor vertexStartConsumer,
                           VisitProcessor vertexEndConsumer) {
        this.leafNodes = new BitSet(graphSize);
        this.necessarySets = new BitSet(graphSize);
        this.sufficientSets = new BitSet(graphSize);
        this.propertySets = new BitSet(graphSize);
        this.inclusionSets = new BitSet(graphSize);
        this.distanceArray = ArrayUtil.createAndFillWithMinusOne(graphSize);
        this.discoverySequenceArray = ArrayUtil.createAndFillWithMinusOne(graphSize);
        this.finishSequenceArray = ArrayUtil.createAndFillWithMinusOne(graphSize);
        this.predecessorIndexArray = ArrayUtil.createAndFillWithMinusOne(graphSize);
        this.vertexStartConsumer = vertexStartConsumer;
        this.vertexEndConsumer = vertexEndConsumer;
    }

    public void vertexStartProcess(EntityVertex vertex, DiGraphAbstract<EntityVertex> diGraph) {
        if (vertex.meaningNid == TinkarTerm.NECESSARY_SET.nid()) {
            this.necessarySets.set(vertex.vertexIndex);
        }
        if (vertex.meaningNid == TinkarTerm.SUFFICIENT_SET.nid()) {
            this.sufficientSets.set(vertex.vertexIndex);
        }
        if (vertex.meaningNid == TinkarTerm.PROPERTY_SET.nid()) {
            this.propertySets.set(vertex.vertexIndex);
        }
        if (vertex.meaningNid == TinkarTerm.INCLUSION_SET.nid()) {
            this.inclusionSets.set(vertex.vertexIndex);
        }

        if (vertexStartConsumer != null) {
            vertexStartConsumer.accept(vertex, diGraph, this);
        }
    }
    public void vertexEndProcess(EntityVertex vertex, DiGraphAbstract<EntityVertex> diGraph) {

        if (vertexEndConsumer != null) {
            vertexEndConsumer.accept(vertex, diGraph, this);
        }
    }

    /**
     * End vertex visit.
     *
     * @param vertexIndex the vertex index
     */
    public void endVertexVisit(int vertexIndex) {
        this.finishSequenceArray[vertexIndex] = this.sequence++;
    }


    /**
     * Gets the discovery time.
     *
     * @param vertexIndex the vertex index
     * @return the discovery time
     */
    public int discoverySequence(int vertexIndex) {
        return this.discoverySequenceArray[vertexIndex];
    }

    /**
     * Gets the distance.
     *
     * @param vertexIndex the vertex index
     * @return the distance or -1;
     */
    public int distance(int vertexIndex) {
        return this.distanceArray[vertexIndex];
    }

    /**
     * Gets the finish time.
     *
     * @param vertexIndex the vertex index
     * @return the finish time or -1;
     */
    public int finishSequence(int vertexIndex) {
        return this.finishSequenceArray[vertexIndex];
    }

    /**
     * Gets the leaf vertexs.
     *
     * @return the leaf vertexs
     */
    public BitSet leafVertexIndexes() {
        return this.leafNodes;
    }
    public BitSet propertySetIndexes() {
        return this.propertySets;
    }
    public BitSet necessarySetIndexes() {
        return this.necessarySets;
    }
    public BitSet sufficientSetIndexes() {
        return this.sufficientSets;
    }
    public BitSet inclusionSetIndexes() { return this.inclusionSets; }

    /**
     * Gets the max depth.
     *
     * @return the max depth
     */
    public int maxDepth() {
        return maxDepth;
    }

    /**
     * Gets the vertex ids for depth.
     *
     * @param depth the depth
     * @return the vertex ids for depth
     */
    public BitSet vertexIndexesForDepth(int depth) {
        return depthVertexMap.getIfAbsentPut(depth, () -> new BitSet(this.distanceArray.length));
    }

    /**
     * Gets the vertex status.
     *
     * @param vertexIndex the vertex index
     * @return the vertex status
     */
    public VertexStatus vertexStatus(int vertexIndex) {
        if (this.discoverySequenceArray[vertexIndex] == -1) {
            return VertexStatus.UNDISCOVERED;
        }
        if (this.finishSequenceArray[vertexIndex] == -1) {
            return VertexStatus.PROCESSING;
        }
        return VertexStatus.FINISHED;
    }

    /**
     * Gets the vertexes visited.
     *
     * @return the vertexes visited
     */
    public int vertexesVisitedCount() {
        return this.vertexesVisited;
    }

    /**
     * Gets the predecessor nid or empty if no predecessor.
     *
     * @param vertexIndex the vertex index
     * @return the predecessor nid
     */
    public OptionalInt predecessorIndex(int vertexIndex) {
        if (this.predecessorIndexArray[vertexIndex] == -1) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(this.predecessorIndexArray[vertexIndex]);
    }

    /**
     * The start index for this traversal. If only one root, then this is the
     * index of the root vertex.
     * @return the start nid
     */
    public int startIndex() {
        return startIndex;
    }

    /**
     * Gets the sequence (proxy for time, sequence increments for visit start and visit compete on a vertex.
     *
     * @return the time as a sequence.
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the leaf vertex.
     *
     * @param vertexIndex the vertex index
     */
    public void setLeafVertex(int vertexIndex) {
        this.leafNodes.set(vertexIndex);
    }

   /**
     * Set predecessor nid.
     *
     * @param vertexIndex the vertex index
     * @param predecessorVertexIndex the predecessor vertex index
     */
   public void setPredecessorIndex(int vertexIndex, int predecessorVertexIndex) {
       this.predecessorIndexArray[vertexIndex] = predecessorVertexIndex;
   }


    /**
     * Start vertex visit.
     *
     * @param vertexIndex the vertex index
     * @param depth the depth
     */
    public void startVertexVisit(int vertexIndex, int depth) {
        if (depth == 0 && this.startIndex == -1) {
            this.startIndex = vertexIndex;
        }
        this.discoverySequenceArray[vertexIndex] = this.sequence++;
        this.distanceArray[vertexIndex] = depth;
        this.vertexesVisited++;
        this.maxDepth = Math.max(this.maxDepth, depth);
        vertexIndexesForDepth(depth).set(vertexIndex);
    }
    public int graphSize() {
        return this.distanceArray.length;
    }



}
