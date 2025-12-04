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

import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.component.graph.GraphAdaptorFactory;
import dev.ikm.tinkar.terms.ConceptFacade;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntIntMap;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DiTreeAbstract<V extends EntityVertex> extends DiGraphAbstract<V> implements DiTree<V> {
    private static final Logger LOG = LoggerFactory.getLogger(DiTreeEntity.class);
    final V root;
    /**
     * Note that a get() method on IntInt map returns 0 as a default even if there is no key.
     * Thus you must test for containsKey()
     * TODO consider changing data structure to one that does not require
     */
    final ImmutableIntIntMap predecessorMap;

    public DiTreeAbstract(V root, ImmutableList<V> vertexMap,
                          ImmutableIntObjectMap<ImmutableIntList> successorMap, ImmutableIntIntMap predecessorMap) {
        super(vertexMap, successorMap);
        this.root = root;
        this.predecessorMap = predecessorMap;
    }

    @Override
    public V root() {
        return root;
    }

    public OptionalInt predecessor(int vertexIndex) {
        if (this.predecessorMap.containsKey(vertexIndex)) {
            return OptionalInt.of(vertex(this.predecessorMap.get(vertexIndex)).vertexIndex);
        }
        return OptionalInt.empty();
    }
    @Override
    public Optional<V> predecessor(EntityVertex vertex) {
        /**
         * Note that a get() method on IntInt map returns 0 as a default even if there is no key.
         * Thus you must test for containsKey()
         * TODO consider changing data structure to one that does not require
         */
        if (this.predecessorMap.containsKey(vertex.vertexIndex())) {
            return Optional.of(vertex(this.predecessorMap.get(vertex.vertexIndex())));
        }
        return Optional.empty();
    }

    /**
     * Note that a get() method on IntInt map returns 0 as a default even if there is no key.
     * Thus you must test for containsKey()
     * TODO consider changing data structure to one that does not require
     */
    @Override
    public ImmutableIntIntMap predecessorMap() {
        return predecessorMap;
    }

    public final byte[] getBytes() {
        int defaultSize = estimatedBytes();
        int bufSize = defaultSize;
        AtomicReference<ByteBuf> byteBufRef =
                new AtomicReference<>(ByteBufPool.allocate(bufSize));
        while (true) {
            try {
                ByteBuf byteBuf = byteBufRef.get();
                writeVertexMap(byteBuf);
                writeIntIntListMap(byteBuf, successorMap());

                byteBuf.writeInt(predecessorMap.size());
                predecessorMap.forEachKeyValue((vertex, predecessor) -> {
                    byteBuf.writeInt(vertex);
                    byteBuf.writeInt(predecessor);
                });

                byteBuf.writeInt(root.vertexIndex());
                return byteBuf.asArray();
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.info("Tree: " + e.getMessage());
                byteBufRef.get().recycle();
                bufSize = bufSize + defaultSize;
                byteBufRef.set(ByteBufPool.allocate(bufSize));
            }
        }
    }

    @Override
    public String toString() {
        return toString("");
    }
    public String toString(String idSuffix) {
        return toString(idSuffix, this.root.vertexIndex);
    }
    public String toString(String idSuffix, int rootIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("{\n");
        fragmentToString(idSuffix, vertex(rootIndex), sb, false);
        sb.append('}');

        return sb.toString();
    }
    /**
     * Fragment to string.
     *
     * @return A string representing the fragment of the tree
     * rooted in this vertex.
     */
    public String fragmentToString(EntityVertex fragmentRoot) {
        final StringBuilder builder = new StringBuilder();
        fragmentToString("", fragmentRoot, builder, false);
        return builder.toString();
    }

    public String fragmentToString(String nodeIdSuffix, EntityVertex fragmentRoot) {
        final StringBuilder builder = new StringBuilder();
        fragmentToString(nodeIdSuffix, fragmentRoot, builder, false);
        return builder.toString();
    }
    private void fragmentToString(String nodeIdSuffix, EntityVertex fragmentRoot, StringBuilder builder, boolean coverAllIndexes) {
        MutableIntSet coveredIndexes = IntSets.mutable.empty();
        int nextIndex = fragmentRoot.vertexIndex;
        while (nextIndex > -1) {
            dfsProcess(fragmentRoot.vertexIndex, builder, 1, nodeIdSuffix, coveredIndexes);

            if (coverAllIndexes && coveredIndexes.size() < vertexMap.size()) {
                for (int i = 0; i < vertexMap.size(); i++) {
                    if (!coveredIndexes.contains(i)) {
                        nextIndex = i;
                        break;
                    }
                }
            } else {
                nextIndex = -1;
            }
        }
    }


    private void dfsProcess(int start, StringBuilder sb, int depth, String idSuffix, MutableIntSet coveredIndexes) {
        EntityVertex vertex = vertexMap.get(start);
        coveredIndexes.add(start);
        sb.append(vertex.toGraphFormatString("  ".repeat(depth), idSuffix, this));
        Optional<ImmutableIntList> optionalSuccessors = successorNids(start);
        if (optionalSuccessors.isPresent()) {
            optionalSuccessors.get().forEach(i -> dfsProcess(i, sb, depth + 1, idSuffix, coveredIndexes));
        }
    }

    protected static abstract class Builder<V extends EntityVertex> implements DiTree<V> {
        protected final MutableList<V> vertexMap = Lists.mutable.empty();
        protected final MutableIntObjectMap<MutableIntList> successorMap = IntObjectMaps.mutable.empty();
        protected final MutableIntIntMap predecessorMap = IntIntMaps.mutable.empty();
        protected V root;

        protected Builder() {
        }
        /**
         * @param adaptorFactory
         * @param <A>
         * @return
         */
        @Override
        public <A> A adapt(GraphAdaptorFactory<A> adaptorFactory) {
            throw new UnsupportedOperationException("Builder does not adapt... ");
        }

        @Override
        public V vertex(UUID vertexId) {
            for (V vertex : this.vertexMap) {
                if (vertex.vertexId().asUuid().equals(vertexId)) {
                    return vertex;
                }
            }
            throw new NoSuchElementException("VertexId: " + vertexId);
        }

        @Override
        public V vertex(int vertexIndex) {
            return vertexMap.get(vertexIndex);
        }

        @Override
        public ImmutableList<V> vertexMap() {
            return vertexMap.toImmutable();
        }

        @Override
        public ImmutableIntObjectMap<ImmutableIntList> successorMap() {
            MutableIntObjectMap<ImmutableIntList> tempMap = IntObjectMaps.mutable.ofInitialCapacity(successorMap.size());
            successorMap.forEachKeyValue((i, mutableIntList) -> tempMap.put(i, mutableIntList.toImmutable()));
            return tempMap.toImmutable();
        }

        @Override
        public ImmutableList<V> successors(EntityVertex vertex) {
            MutableIntList successorList = successorMap.get(vertex.vertexIndex());
            if (successorList != null) {
                MutableList<V> successors = Lists.mutable.ofInitialCapacity(successorList.size());
                successorList.forEach(successorIndex -> {
                    successors.add(vertex(successorIndex));
                });
                return successors.toImmutable();
            }
            return Lists.immutable.empty();
        }

        public EntityVertex getRoot() {
            return root;
        }

        public DiTreeAbstract.Builder setRoot(V root) {
            addVertex(root);
            this.root = root;
            return this;
        }

        /**
         * If the vertex has an unassigned index, the index will be assigned to the size of the
         * vertex map.
         * @param vertex the vertex to add to the tree
         * @return the builder for fluent api.
         */
        public DiTreeAbstract.Builder addVertex(V vertex) {
            if (vertex.vertexIndex() > -1) {
                if (vertex.vertexIndex() < vertexMap.size() &&
                        vertex == vertexMap.get(vertex.vertexIndex())) {
                    // already in the list.
                } else {
                    while (vertexMap.size() <= vertex.vertexIndex()) {
                        vertexMap.add(null);
                    }
                    vertexMap.set(vertex.vertexIndex(), vertex);
                }
            } else {
                vertex.setVertexIndex(vertexMap.size());
                vertexMap.add(vertex.vertexIndex(), vertex);
            }
            return this;
        }
        public DiTreeAbstract.Builder replaceVertex(V vertex) {
            if (vertex.vertexIndex() > -1) {
                if (vertex.vertexIndex() < vertexMap.size()) {
                    vertexMap.set(vertex.vertexIndex, vertex);
                } else {
                    throw new IllegalStateException("Vertex index is greater than vertexMap.size(): " + vertex);
                }
            } else {
                throw new IllegalStateException("Vertex replacing old vertex must have its index set: " + vertex);
            }
            return this;
        }

        public DiTreeAbstract.Builder addEdge(V child, V parent) {
            addVertex(child);
            addVertex(parent);
            if (!successorMap.containsKey(parent.vertexIndex())) {
                successorMap.put(parent.vertexIndex(), IntLists.mutable.empty());
            }
            successorMap.get(parent.vertexIndex()).add(child.vertexIndex());
            predecessorMap.put(child.vertexIndex(), parent.vertexIndex());
            return this;
        }

        public DiTreeAbstract.Builder addEdge(int childIndex, int parentIndex) {
            if (vertexMap.get(childIndex) == null || vertexMap.get(parentIndex) == null) {
                throw new IllegalStateException("Child Vertex or Parent Vertex is null. Add to vertex map before adding edge. ");
            }
            if (!successorMap.containsKey(parentIndex)) {
                successorMap.put(parentIndex, IntLists.mutable.empty());
            }
            successorMap.get(parentIndex).add(childIndex);
            predecessorMap.put(childIndex, parentIndex);
            return this;
        }

        @Override
        public V root() {
            return root;
        }

        public OptionalInt predecessorIndex(int vertexIndex) {
            if (this.predecessorMap.containsKey(vertexIndex)) {
                return OptionalInt.of(this.predecessorMap.get(vertexIndex));
            }
            return OptionalInt.empty();
        }

        @Override
        public Optional<V> predecessor(EntityVertex vertex) {
            /**
             * Note that a get() method on IntInt map returns 0 as a default even if there is no key.
             * Thus you must test for containsKey()
             * TODO consider changing data structure to one that does not require
             */
            if (this.predecessorMap.containsKey(vertex.vertexIndex())) {
                return Optional.of(vertex(this.predecessorMap.get(vertex.vertexIndex())));
            }
            return Optional.empty();
        }

        @Override
        public ImmutableIntIntMap predecessorMap() {
            return predecessorMap.toImmutable();
        }

        @Override
        public ImmutableIntList successors(int vertexIndex) {
            return successorMap.get(vertexIndex).toImmutable();
        }

        public void setVertexIndex(EntityVertex changedSet, int vertexIndex) {
            changedSet.setVertexIndex(vertexIndex);
        }

        public void setPredecessorIndex(int vertexIndex, int predecessorIndex) {
            int oldPredecessorIndex = predecessorMap.get(vertexIndex);
            successorMap.get(oldPredecessorIndex).remove(vertexIndex);
            predecessorMap.put(vertexIndex, predecessorIndex);
            successorMap.get(predecessorIndex).add(vertexIndex);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Checks if there exists a predecessor vertex with the given meaningNid in the hierarchical structure of the graph.
     *
     * @param vertexIndex The index of the vertex to start the search from.
     * @param meaningNid  The meaningNid to search for.
     * @return {@code true} if a predecessor vertex with the given meaningNid exists, {@code false} otherwise.
     */
    public boolean hasPredecessorVertexWithMeaning(int vertexIndex, int meaningNid) {
        if (vertex(vertexIndex).meaningNid == meaningNid) {
            return true;
        }
        if (predecessorMap.containsKey(vertexIndex)) {
            return hasPredecessorVertexWithMeaning(predecessorMap.get(vertexIndex), meaningNid);
        }
        return false;
    }
    public boolean hasPredecessorVertexWithMeaning(EntityVertex vertex, ConceptFacade meaning) {
        return hasPredecessorVertexWithMeaning(vertex.vertexIndex, meaning.nid());
    }



    /**
     * Use to when printing out multiple expressions, and you want to differentiate the
     * identifiers so that they are unique across all fragments.
     *
     * @param nodeIdSuffix the identifier suffix for this expression.
     * @return A string representing the fragment of the expression
     * rooted in this node.
     */
}
