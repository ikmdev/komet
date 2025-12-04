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

import dev.ikm.tinkar.component.graph.DiGraph;
import dev.ikm.tinkar.component.graph.Graph;
import dev.ikm.tinkar.component.graph.GraphAdaptorFactory;
import dev.ikm.tinkar.component.graph.Vertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import io.activej.bytebuf.ByteBuf;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DiGraphAbstract<V extends EntityVertex>  {


    public static final byte ENTITY_FORMAT_VERSION = 1;

    final ImmutableList<V> vertexMap;
    final ImmutableIntObjectMap<ImmutableIntList> successorMap;
    AtomicReference<ConcurrentHashMap<GraphAdaptorFactory, Object>> adaptorsReference = new AtomicReference<>();

    public DiGraphAbstract(ImmutableList<V> vertexMap,
                           ImmutableIntObjectMap<ImmutableIntList> successorMap) {
        this.vertexMap = vertexMap;
        this.successorMap = successorMap;
    }

    public DiGraph.VertexType vertexType(V vertex) {
        if (successorMap.get(vertex.vertexIndex) == null || successorMap.get(vertex.vertexIndex).isEmpty()) {
            return DiGraph.VertexType.LEAF;
        }
        return DiGraph.VertexType.PREDECESSOR;
    }
    public DiGraph.VertexType vertexType(int vertexIndex) {
        if (successorMap.get(vertexIndex) == null || successorMap.get(vertexIndex).isEmpty()) {
            return DiGraph.VertexType.LEAF;
        }
        return DiGraph.VertexType.PREDECESSOR;
    }

    protected static ImmutableList<EntityVertex> getVertexEntities(Graph<Vertex> tree) {
        MutableList<EntityVertex> vertexMap = Lists.mutable.ofInitialCapacity(tree.vertexMap().size());
        for (Vertex vertex : tree.vertexMap()) {
            vertexMap.add(EntityVertex.make(vertex));
        }
        return vertexMap.toImmutable();
    }

    protected static ImmutableList<EntityVertex> readVertexEntities(ByteBuf readBuf, byte entityFormatVersion) {
        int vertexMapSize = readBuf.readInt();
        MutableList<EntityVertex> vertexMap = Lists.mutable.ofInitialCapacity(vertexMapSize);
        for (int i = 0; i < vertexMapSize; i++) {
            EntityVertex entityVertex = EntityVertex.make(readBuf, entityFormatVersion);
            vertexMap.add(entityVertex);
        }
        return vertexMap.toImmutable();
    }

    protected static ImmutableIntObjectMap<ImmutableIntList> readIntIntListMap(ByteBuf readBuf) {
        int successorMapSize = readBuf.readInt();
        MutableIntObjectMap<ImmutableIntList> successorMap = IntObjectMaps.mutable.ofInitialCapacity(successorMapSize);
        for (int i = 0; i < successorMapSize; i++) {
            int vertexSequence = readBuf.readInt();
            int successorListSize = readBuf.readInt();
            MutableIntList successorList = IntLists.mutable.empty();
            for (int j = 0; j < successorListSize; j++) {
                successorList.add(readBuf.readInt());
            }
            successorMap.put(vertexSequence, successorList.toImmutable());
        }
        return successorMap.toImmutable();
    }

    /**
     * @param adaptorFactory
     * @param <A>
     * @return
     */
    public <A> A adapt(GraphAdaptorFactory<A> adaptorFactory) {
        adaptorsReference.accumulateAndGet(null, (prev, x) -> {
            if (x == null) {
                return new ConcurrentHashMap(4);
            }
            return x;
        });
        return (A) adaptorsReference.get().getIfAbsentPut(adaptorFactory, () -> adaptorFactory.adapt((Graph) this));
    }

    public ImmutableList<V> vertexMap() {
        return vertexMap;
    }

    public Optional<ImmutableIntList> successorNids(int vertexIndex) {
        return Optional.ofNullable(successorMap.get(vertexIndex));
    }
    public Optional<V> firstVertexWithMeaning(ConceptFacade vertexMeaning) {
        return firstVertexWithMeaning(vertexMeaning.nid());
    }
    public Optional<V>  firstVertexWithMeaning(int vertexMeaningNid) {
        for (V vertex : vertexMap) {
            if (vertex.meaningNid == vertexMeaningNid) {
                return Optional.of(vertex);
            }
        }
        return Optional.empty();
    }

    public boolean containsVertexWithMeaning(EntityFacade meaning) {
        return containsVertexWithMeaning(meaning.nid());
    }

    public boolean containsVertexWithMeaning(int meaningNid) {
        for (V vertex : vertexMap) {
            if (vertex.meaningNid == meaningNid) {
                return true;
            }
        }
        return false;
    }

    public V vertex(UUID vertexId) {
        for (V vertexEntity : this.vertexMap) {
            if (vertexEntity.vertexId().asUuid().equals(vertexId)) {
                return vertexEntity;
            }
        }
        throw new NoSuchElementException("VertexId: " + vertexId);
    }

    public ImmutableIntList successors(int vertexIndex) {
        ImmutableIntList successorList = successorMap.get(vertexIndex);
        if (successorList != null) {
            return successorList;
        }
        return IntLists.immutable.empty();
    }

    public ImmutableList<V> successors(V vertex) {
        ImmutableIntList successorList = successorMap.get(vertex.vertexIndex());
        if (successorList != null) {
            MutableList<V> successors = Lists.mutable.ofInitialCapacity(successorList.size());
            successorList.forEach(successorIndex -> {
                successors.add(vertex(successorIndex));
            });
            return successors.toImmutable();
        }
        return Lists.immutable.empty();
    }

    public V vertex(int vertexIndex) {
        return vertexMap.get(vertexIndex);
    }

    public int estimatedBytes() {
        // Empty vertex is 34 bytes
        return vertexMap.size() * 64;
    }

    protected void writeIntIntListMap(ByteBuf byteBuf, ImmutableIntObjectMap<ImmutableIntList> map) {
        byteBuf.writeInt(successorMap().size());
        map.forEachKeyValue((int vertexIndex, ImmutableIntList destinationVertexes) -> {
            byteBuf.writeInt(vertexIndex);
            byteBuf.writeInt(destinationVertexes.size());
            destinationVertexes.forEach(destinationIndex -> byteBuf.writeInt(destinationIndex));
        });
    }

    public ImmutableIntObjectMap<ImmutableIntList> successorMap() {
        return successorMap;
    }

    protected void writeVertexMap(ByteBuf byteBuf) {
        byteBuf.writeInt(vertexMap.size());
        vertexMap.forEach(vertexEntity -> {
            byteBuf.write(vertexEntity.getBytes());
        });
    }

    public final void breadthFirstProcess(int startNid, VertexVisitData vertexVisitData) {
        final Queue<Integer> bfsQueue      = new LinkedList<>();

        vertexVisitData.startVertexVisit(startNid, 0);
        bfsQueue.add(startNid);

        while (!bfsQueue.isEmpty()) {
            final int   currentIndex      = bfsQueue.remove();
            final int   currentDistance = vertexVisitData.distance(currentIndex);
            final EntityVertex currentVertex = vertex(currentIndex);
            final ImmutableIntList childIndexes       = successorMap.get(currentIndex);

            if (childIndexes.isEmpty()) {
                vertexVisitData.setLeafVertex(currentIndex);
            }

            vertexVisitData.vertexStartProcess(currentVertex, (DiGraphAbstract<EntityVertex>) this);

            childIndexes.forEach(childIndex -> {
                if (vertexVisitData.vertexStatus(childIndex) == VertexStatus.UNDISCOVERED) {
                    vertexVisitData.startVertexVisit(childIndex, currentDistance + 1);
                    vertexVisitData.setPredecessorIndex(childIndex, currentIndex);
                    bfsQueue.add(childIndex);
                } else {
                    // second path to node. Could be multi-parent or cycle...
                    // TODO decide if to put in generic cycle detection using this circumstance.
                }
            });

            vertexVisitData.vertexEndProcess(currentVertex, (DiGraphAbstract<EntityVertex>) this);
            vertexVisitData.endVertexVisit(currentIndex);
        }
    }
    public final void depthFirstProcess(int startNid, VertexVisitData vertexVisitData) {
        dfsVisit(startNid, vertexVisitData, 0);
    }
    private void dfsVisit(int index,
                          VertexVisitData vertexVisitData,
                          int depth) {
        if (depth > 100) {
            // toString depends on this method, so we can't include this.toString() in the exception...
            throw new RuntimeException("Depth limit exceeded: " + depth);  // + " in graph: " + this);
        }
        // Change to NodeStatus.PROCESSING
        vertexVisitData.startVertexVisit(index, depth);

        final ImmutableIntList childIndexes = successorMap.getIfAbsent(index, () -> IntLists.immutable.empty());

        if (childIndexes.isEmpty()) {
            vertexVisitData.setLeafVertex(index);
        }
        EntityVertex vertex = vertex(index);
        vertexVisitData.vertexStartProcess(vertex, (DiGraphAbstract<EntityVertex>) this);

        childIndexes.forEach(childIndex -> {
            if (vertexVisitData.vertexStatus(childIndex) == VertexStatus.UNDISCOVERED) {
                vertexVisitData.setPredecessorIndex(childIndex, index);
                dfsVisit(childIndex, vertexVisitData, depth + 1);
            } else {
                // second path to node. Could be multi-parent or cycle...
                // TODO decide if to put in generic cycle detection using this circumstance.
            }
        });

        vertexVisitData.vertexEndProcess(vertex, (DiGraphAbstract<EntityVertex>) this);

        // Change to NodeStatus.FINISHED
        vertexVisitData.endVertexVisit(index);
    }


    public boolean equivalentVertexes(int thisIndex, EntityVertex thatVertex) {
        return this.vertexMap.get(thisIndex).equivalent(thatVertex);
    }

    public int vertexCount() {
        return this.vertexMap.size();
    }

}
