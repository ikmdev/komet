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

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.util.time.MultipleEndpointTimer;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.component.graph.Vertex;
import dev.ikm.tinkar.entity.graph.isomorphic.IsomorphicResults;
import dev.ikm.tinkar.entity.graph.isomorphic.IsomorphicResultsLeafHash;
import dev.ikm.tinkar.terms.TinkarTerm;
import io.activej.bytebuf.ByteBuf;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntIntMap;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class DiTreeEntity extends DiTreeAbstract<EntityVertex> {
    private static final Logger LOG = LoggerFactory.getLogger(DiTreeEntity.class);

    public DiTreeEntity(EntityVertex root,
                        ImmutableList<EntityVertex> vertexMap,
                        ImmutableIntObjectMap<ImmutableIntList> successorMap,
                        ImmutableIntIntMap predecessorMap) {
        super(root, vertexMap, successorMap, predecessorMap);
    }

    /**
     * Make a correlated tree, where the vertex ids of this tree will be preserved when the ids are
     * correlated by an isomorphic analysis.
     *
     * @param that The tree that is considered secondary with respect to preserving vertex ids.
     * @return a copy of that which is updated with correlated vertex ids based on isomorphic analysis.
     */
    public DiTreeEntity makeCorrelatedTree(DiTreeEntity that, int referencedConceptNid, MultipleEndpointTimer.Stopwatch stopwatch) {

        // A special case for correlation when this and that are equal and vertexes are indexed the same.
        if (this.vertexMap.size() == that.vertexMap.size()) {
            boolean maybeEqual = true;
            for (int index = 0; index < this.vertexMap.size(); index++) {
                if (this.vertexMap.get(index) == null || that.vertexMap.get(index) == null) {
                    if (this.vertexMap.get(index) != that.vertexMap.get(index)) {
                        maybeEqual = false;
                        break;
                    }
                }
                if (!this.vertexMap.get(index).equivalent(that.vertexMap.get(index))) {
                    maybeEqual = false;
                    break;
                }
                if (this.predecessorMap.containsKey(index) != that.predecessorMap.containsKey(index)) {
                    maybeEqual = false;
                    break;
                }
                if (this.predecessorMap.containsKey(index) && that.predecessorMap.containsKey(index)) {
                    if (this.predecessorMap.get(index) != that.predecessorMap.get(index)) {
                        maybeEqual = false;
                        break;
                    }
                }
            }
            if (maybeEqual) {
                // return this as the correlated tree
                stopwatch.end(IsomorphicResults.EndPoints.INDEXES_EQUAL);
                return this;
            }
        }
        try {
            IsomorphicResultsLeafHash isomorphicResult = new IsomorphicResultsLeafHash(this, that, referencedConceptNid, stopwatch);
            IsomorphicResults results = isomorphicResult.call();
            return results.getIsomorphicTree();
        } catch (Exception e) {
            AlertStreams.dispatchToRoot(e);
        }
        throw new IllegalStateException("No result");
    }

    public final VertexVisitData breadthFirstProcess() {
        VertexVisitData vertexVisitData = new VertexVisitData(this.vertexMap.size());
        breadthFirstProcess(this.root.vertexIndex, vertexVisitData);
        return vertexVisitData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiTreeEntity another) {
            return isomorphic(another);
        }
        return super.equals(obj);
    }

    // TODO Consider using JGraphT Isomorphic package here, and compare performance.
    // TODO: Note implementation assumes no null values in vertex map.
    public boolean isomorphic(DiTreeEntity another) {
        if (this.vertexMap.size() != another.vertexMap.size()) {
            return false;
        }
        IsomorphicResultsLeafHash isomorphicResult = new IsomorphicResultsLeafHash(this, another,
                TinkarTerm.UNINITIALIZED_COMPONENT.nid());
        try {
            isomorphicResult.call();
            return isomorphicResult.equivalent();
        } catch (Exception e) {
            AlertStreams.dispatchToRoot(e);
        }
        return false;
    }

    public static DiTreeEntity make(DiTree<Vertex> tree) {
        ImmutableList<EntityVertex> vertexMap = getVertexEntities(tree);
        EntityVertex root = vertexMap.get(tree.root().vertexIndex());
        return new DiTreeEntity(root, vertexMap, tree.successorMap(), tree.predecessorMap());
    }

    public static DiTreeEntity make(ByteBuf readBuf, byte entityFormatVersion) {
        if (entityFormatVersion != ENTITY_FORMAT_VERSION) {
            throw new IllegalStateException("Unsupported entity format version: " + entityFormatVersion);
        }

        ImmutableList<EntityVertex> vertexMap = readVertexEntities(readBuf, entityFormatVersion);
        ImmutableIntObjectMap<ImmutableIntList> successorMap = readIntIntListMap(readBuf);

        int predecessorMapSize = readBuf.readInt();
        MutableIntIntMap predecessorMap = IntIntMaps.mutable.ofInitialCapacity(predecessorMapSize);
        for (int i = 0; i < predecessorMapSize; i++) {
            predecessorMap.put(readBuf.readInt(), readBuf.readInt());
        }

        int rootVertexIndex = readBuf.readInt();

        return new DiTreeEntity(vertexMap.get(rootVertexIndex), vertexMap,
                successorMap, predecessorMap.toImmutable());

    }

    public static DiTreeEntity.Builder builder() {
        return new DiTreeEntity.Builder();
    }

    public static DiTreeEntity.Builder builder(DiTree<EntityVertex> treeToCopy) {
        Builder builder = new Builder();
        builder.addVertexes(treeToCopy);
        return builder;
    }

    /**
     * Called to generate an isomorphicExpression and a mergedExpression.
     *
     * @param another  the existing DiTreeEntity to add nodes from.
     * @param solution an array mapping from the vertexIndexes in another to the vertexIndexes
     *                 in this expression. If the value of the solution element == -1, that node
     *                 is not added to this tree, otherwise the value of the
     *                 solution element is used for the vertexIndex in this tree.
     */
    public static DiTreeEntity.Builder addVertexesFromSolution(DiTreeEntity another, ImmutableIntList solution) {
        Builder builder = new Builder();
        builder.addVertexesWithMap(another, solution.toArray(), new int[another.vertexCount()], another.root.vertexIndex);
        return builder;
    }

    /**
     * Called to generate an isomorphicExpression and a mergedExpression.
     *
     * @param another                     the logical expression to add nodes from.
     * @param solution                    an array mapping from the nodeId in another to the nodeId
     *                                    in this expression. If the value of the solution element == -1, that node
     *                                    is not added to this logical expression, otherwise the value of the
     *                                    solution element is used for the nodeId in this logical expression.
     * @param anotherToThisVertexIndexMap contains a mapping from nodeId in another
     *                                    to nodeId in this constructed tree.
     */
    public static DiTreeEntity.Builder addVertexesFromSolution(DiTreeEntity another, ImmutableIntList solution, int[] anotherToThisVertexIndexMap) {
        Builder builder = new Builder();
        builder.addVertexesWithMap(another, solution.toArray(), anotherToThisVertexIndexMap, another.root.vertexIndex);
        return builder;
    }
    /**
     * Remove vertex and all recursive successor indexes.
     * @param vertex vertex to begin removal at...
     * @return a builder with the specified vertices removed.
     */
    public DiTreeEntity.Builder removeVertex(Vertex vertex) {
        return removeVertex(vertex.vertexIndex());
    }

    /**
     * Remove vertex and all recursive successor indexes.
     * @param vertexIndex
     * @return a builder with the specified vertices removed.
     */
    public DiTreeEntity.Builder removeVertex(int vertexIndex) {
        // Create a solution that removes the vertex and its successors
        MutableIntList mutableSolution = IntLists.mutable.ofAll(IntStream.range(0, this.vertexMap.size()));
        removeFromSolutionRecursive(mutableSolution, vertexIndex);

        // Create a builder with just the vertices in the solution.
        Builder builder = new Builder();
        builder.addVertexesWithMap(this, mutableSolution.toArray(),
                new int[this.vertexCount()], root.vertexIndex);
        return builder;
    }

    private void removeFromSolutionRecursive(MutableIntList solution, int vertexIndex) {
        solution.set(vertexIndex, -1);
        if (successorMap.containsKey(vertexIndex)) {
            successorMap.get(vertexIndex).forEach(successorIndex -> {
                removeFromSolutionRecursive(solution, successorIndex);
            });
        }
    }


    public static class Builder extends DiTreeAbstract.Builder<EntityVertex> {
        protected Builder() {
        }

        /**
         * Build can be called multiple times, and each build will reflect any updates, and not otherwise modify the
         * builder.
         * @return
         */
        public DiTreeEntity build() {

            MutableIntObjectMap<ImmutableIntList> intermediateSuccessorMap = IntObjectMaps.mutable.ofInitialCapacity(successorMap.size());
            successorMap.forEachKeyValue((vertex, successorList) -> intermediateSuccessorMap.put(vertex, successorList.toImmutable()));



            return new DiTreeEntity(root,
                    vertexMap.toImmutable(),
                    intermediateSuccessorMap.toImmutable(),
                    predecessorMap.toImmutable());
        }

        public int vertexCount() {
            return this.vertexMap.size();
        }

        /**
         * Adds the vertexes with map.
         *
         * @param anotherTree                   the tree to add nodes from.
         * @param solution                      an array mapping from the vertexIndex in anotherTree to the vertexIndex
         *                                      in this tree. If the value of the solution element == -1, that vertex
         *                                      is not added to this tree, otherwise the value of the
         *                                      solution element is used for the vertexIndex in this directed tree.
         * @param vertexIndexesToAddFromAnother the list of vertexIndexes in anotherTree
         *                                      to add to this tree on this invocation. Note that
         *                                      children of the nodes indicated by vertexIndexesToAddFromAnother may be added by recursive calls
         *                                      to this method, if the vertexIndexesToAddFromAnother index in the solution array is >= 0.
         * @return the EntityVertex elements added as a result of this instance of the
         * call, not including any children EntityVertexes added by recursive
         * calls. Those children EntityVertex elements can be retrieved by recursively
         * traversing the children of these returned EntityVertexes.
         */
        public EntityVertex[] addVertexesWithMap(DiTreeEntity anotherTree,
                                                 int[] solution,
                                                 int... vertexIndexesToAddFromAnother) {
            return addVertexesWithMap(anotherTree, solution, null, vertexIndexesToAddFromAnother);
        }

        /**
         * Adds the nodes with map.
         *
         * @param anotherTree                 the tree to add nodes from.
         * @param solution                    an array mapping from the vertexIndex in anotherTree to the vertexIndex
         *                                    in this tree. If the value of the solution element == -1, that vertex
         *                                    is not added to this tree, otherwise the value of the
         *                                    solution element is used for the vertexIndex in this directed tree.
         * @param anotherToThisVertexIndexMap if not null, contains a mapping from vertexIndex in anotherTree
         *                                    to vertexIndex in this constructed expression. The mapping is populated by this routine, it
         *                                    is an output parameter.
         * @param anotherVertexIndexesToAdd   the list of vertexIndexes in anotherTree
         *                                    to add to this tree on this invocation. Note that
         *                                    children of the nodes indicated by anotherVertexIndexesToAdd may be added by recursive calls
         *                                    to this method, if the anotherVertexIndexesToAdd index in the solution array is >= 0.
         * @return the EntityVertex elements added as a result of this instance of the
         * call, not including any children EntityVertexes added by recursive
         * calls. Those children EntityVertex elements can be retrieved by recursively
         * traversing the children of these returned EntityVertexes.
         */
        public EntityVertex[] addVertexesWithMap(DiTreeEntity anotherTree,
                                                 int[] solution,
                                                 int[] anotherToThisVertexIndexMap,
                                                 int... anotherVertexIndexesToAdd) {
            final EntityVertex[] results = new EntityVertex[anotherVertexIndexesToAdd.length];

            for (int i = 0; i < anotherVertexIndexesToAdd.length; i++) {
                final EntityVertex oldVertex = anotherTree.vertex(anotherVertexIndexesToAdd[i]);
                EntityVertex newVertex = oldVertex.copyWithUnassignedIndex();
                this.addVertex(newVertex);
                if (oldVertex.vertexIndex == anotherTree.root.vertexIndex) {
                    this.setRoot(newVertex);
                }
                results[i] = newVertex;
                //  filter successor vertex indexes not in solution, then recursive call to add filtered children...
                int[] filteredSuccessorVertexIndexes = anotherTree.successors(oldVertex.vertexIndex)
                        .primitiveStream().filter(oldVertexIndex -> solution[oldVertexIndex] >= 0).toArray();

                if (filteredSuccessorVertexIndexes.length > 0) {
                    EntityVertex[] successorVertexes = addVertexesWithMap(anotherTree,
                            solution,
                            anotherToThisVertexIndexMap,
                            filteredSuccessorVertexIndexes);

                    // Add filtered successors to builder
                    for (EntityVertex successorVertex : successorVertexes) {
                        addEdge(successorVertex.vertexIndex, newVertex.vertexIndex);
                    }
                }

                // update anotherToThisVertexIndexMap...
                if (anotherToThisVertexIndexMap != null) {
                    anotherToThisVertexIndexMap[oldVertex.vertexIndex] = results[i].vertexIndex;
                }
            }
            return results;
        }

        /**
         * Adds vertexes from another tree to this tree, with the edges of those vertexes.
         *
         * @param anotherTree the tree to add nodes from.
         */
        private void addVertexes(DiTree<EntityVertex> anotherTree) {
            int vertexCount = anotherTree.vertexMap().size();
            for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
                final EntityVertex oldVertex = anotherTree.vertex(vertexIndex);
                EntityVertex newVertex = oldVertex.copyWithUnassignedIndex();
                this.addVertex(newVertex);
                if (oldVertex.vertexIndex == anotherTree.root().vertexIndex) {
                    this.setRoot(newVertex);
                }
            }
            for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
                final EntityVertex oldVertex = anotherTree.vertex(vertexIndex);
                anotherTree.successors(oldVertex.vertexIndex).forEach(successorIndex -> {
                    addEdge(successorIndex, oldVertex.vertexIndex);
                });
            }
        }

        private void removeSuccessor(int vertexIndexToRemove, int predecessorIndex) {
            if (this.successorMap.containsKey(predecessorIndex)) {
                if (this.successorMap.get(predecessorIndex).size() == 1 &&
                    this.successorMap.get(predecessorIndex).contains(vertexIndexToRemove)) {
                    this.successorMap.removeKey(predecessorIndex);
                } else {
                    this.successorMap.get(predecessorIndex).remove(vertexIndexToRemove);
                }
            }
        }

        public void removeNotRecursive(EntityVertex vertexToRemove) {
            removeNotRecursive(vertexToRemove.vertexIndex);
        }

        public void removeNotRecursive(int vertexIndexToRemove) {
            if (vertexIndexToRemove == root.vertexIndex) {
                throw new UnsupportedOperationException("Cannot remove the root: " + vertexIndexToRemove);
            }
            if (this.predecessorMap.containsKey(vertexIndexToRemove)) {
                removeSuccessor(vertexIndexToRemove, this.predecessorMap.get(vertexIndexToRemove));
            }
            this.vertexMap.set(vertexIndexToRemove, null);
            this.successorMap.removeKey(vertexIndexToRemove);
            this.predecessorMap.remove(vertexIndexToRemove);
        }

        /**
         * Remove null elements from the array and sequentially fill those elements with the next
         * non-null element and renumber existing references to reflect this change.
         */
        public void compress() {

            MutableIntIntMap oldNewIndexMap = IntIntMaps.mutable.ofInitialCapacity(this.vertexMap.size());
            IntStream.range(0, this.vertexMap.size()).forEach(i -> oldNewIndexMap.put(i, -1));
            AtomicInteger nextIndex = new AtomicInteger(0);
            oldNewIndexMap.updateValues((left, right) -> {
                if (vertexMap.get(left) == null) {
                    return -1;
                } else {
                    return nextIndex.getAndIncrement();
                }
            });
            LOG.debug("Old to new index map: {}", oldNewIndexMap);
            //Adaptors need to be adjusted...
            int newTreeSize = nextIndex.get();
            final MutableList<EntityVertex> compressedVertexMap = Lists.mutable.ofInitialCapacity(newTreeSize);
            final MutableIntObjectMap<MutableIntList> compressedSuccessorMap = IntObjectMaps.mutable.ofInitialCapacity(newTreeSize);
            final MutableIntIntMap compressedPredecessorMap = IntIntMaps.mutable.ofInitialCapacity(newTreeSize);

            for (int i = 0; i < this.vertexMap.size(); i++) {
                if (oldNewIndexMap.get(i) != -1) {
                    int newIndex = oldNewIndexMap.get(i);
                    compressedVertexMap.add(newIndex, this.vertexMap.get(i).copyWithNewIndex(newIndex));
                    if (successorMap.containsKey(i)) {
                        MutableIntList compressedSuccessorList =  IntLists.mutable.empty();
                        this.successorMap.get(i).forEach(oldSuccessorIndex -> {
                            if (oldSuccessorIndex != -1) {
                                compressedSuccessorList.add(oldNewIndexMap.get(oldSuccessorIndex));
                            }
                        });
                        compressedSuccessorMap.put(oldNewIndexMap.get(i), compressedSuccessorList);
                    }
                    if (this.predecessorMap.containsKey(i) && oldNewIndexMap.get(i) != -1) {
                        compressedPredecessorMap.put(oldNewIndexMap.get(i), oldNewIndexMap.get(this.predecessorMap.get(i)));
                    }
                }
            }
            LOG.debug("Compressed vertex map: {}", compressedVertexMap);
            LOG.debug("Compressed successor map: {}", compressedSuccessorMap);
            LOG.debug("Compressed predecessor map: {}", compressedPredecessorMap);
            this.vertexMap.clear();
            this.vertexMap.addAll(compressedVertexMap);
            this.successorMap.clear();
            this.successorMap.putAll(compressedSuccessorMap);
            this.predecessorMap.clear();
            this.predecessorMap.putAll(compressedPredecessorMap);
            this.root = compressedVertexMap.get(root.vertexIndex);
            LOG.debug("New tree: {}", this);
            LOG.debug("New tree size: {}", this.vertexMap.size());
            LOG.debug("New tree root: {}", this.root);
            LOG.debug("New tree successor map: {}", this.successorMap);
        }

        @Override
        public String toString() {
            return "DiTreeEntity.Builder{\n" +
                    build().toString() +
                    "\n}";
        }
    }

}
