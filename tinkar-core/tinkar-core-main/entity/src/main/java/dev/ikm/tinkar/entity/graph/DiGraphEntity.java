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
import dev.ikm.tinkar.component.graph.Vertex;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class DiGraphEntity<V extends EntityVertex> extends DiGraphAbstract<V> implements DiGraph<V> {
    private static final Logger LOG = LoggerFactory.getLogger(DiGraphEntity.class);
    final ImmutableList<V> roots;
    final ImmutableIntObjectMap<ImmutableIntList> predecessorMap;

    public DiGraphEntity(ImmutableList<V> roots, ImmutableList<V> vertexMap,
                         ImmutableIntObjectMap<ImmutableIntList> successorMap, ImmutableIntObjectMap<ImmutableIntList> predecessorMap) {
        super(vertexMap, successorMap);
        this.roots = roots;
        this.predecessorMap = predecessorMap;
    }

    public static DiGraphEntity make(DiGraph<Vertex> tree) {
        ImmutableList<EntityVertex> vertexMap = getVertexEntities(tree);
        MutableList<EntityVertex> rootList = Lists.mutable.ofInitialCapacity(tree.roots().size());
        for (Vertex vertex : tree.roots()) {
            rootList.add(EntityVertex.make(vertex));
        }
        return new DiGraphEntity(rootList.toImmutable(), vertexMap, tree.successorMap(), tree.predecessorMap());
    }

    public static DiGraphEntity make(ByteBuf readBuf, byte entityFormatVersion) {
        if (entityFormatVersion != ENTITY_FORMAT_VERSION) {
            throw new IllegalStateException("Unsupported entity format version: " + entityFormatVersion);
        }
        ImmutableList<EntityVertex> vertexMap = readVertexEntities(readBuf, entityFormatVersion);
        ImmutableIntObjectMap<ImmutableIntList> successorMap = readIntIntListMap(readBuf);
        ImmutableIntObjectMap<ImmutableIntList> predecessorMap = readIntIntListMap(readBuf);

        int rootCount = readBuf.readInt();
        MutableList<EntityVertex> roots = Lists.mutable.ofInitialCapacity(rootCount);
        for (int i = 0; i < rootCount; i++) {
            roots.add(vertexMap.get(i));
        }

        return new DiGraphEntity(roots.toImmutable(), vertexMap.toImmutable(),
                successorMap, predecessorMap);

    }

    public static <V extends EntityVertex> Builder<V> builder() {
        return new Builder<>();
    }

    @Override
    public ImmutableList<V> roots() {
        return roots;
    }

    @Override
    public ImmutableList<V> predecessors(V vertex) {
        ImmutableIntList predecessorlist = predecessorMap.get(vertex.vertexIndex());
        MutableList<V> predecessors = Lists.mutable.ofInitialCapacity(predecessorlist.size());
        predecessorlist.forEach(successorIndex -> {
            predecessors.add(vertex(successorIndex));
        });
        return predecessors.toImmutable();
    }

    @Override
    public ImmutableIntObjectMap<ImmutableIntList> predecessorMap() {
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
                writeIntIntListMap(byteBuf, predecessorMap());

                byteBuf.writeInt(roots.size());
                roots.forEach(root -> byteBuf.writeInt(root.vertexIndex()));
                return byteBuf.asArray();
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.info("Tree: " + e.getMessage());
                byteBufRef.get().recycle();
                bufSize = bufSize + defaultSize;
                byteBufRef.set(ByteBufPool.allocate(bufSize));
            }
        }
    }

    public static class Builder<V extends EntityVertex> {
        private final MutableList<V> vertexMap = Lists.mutable.empty();
        private final MutableIntObjectMap<MutableIntList> successorMap = IntObjectMaps.mutable.empty();
        private final MutableIntObjectMap<MutableIntList> predecessorMap = IntObjectMaps.mutable.empty();
        ;
        private final MutableList<V> roots = Lists.mutable.empty();

        protected Builder() {
        }

        public Builder addRoot(V root) {
            addVertex(root);
            roots.add(root);
            return this;
        }

        public Builder addVertex(V vertex) {
            if (vertex.vertexIndex() > 0 &&
                    vertex.vertexIndex() < vertexMap.size() &&
                    vertexMap.get(vertex.vertexIndex()) == vertex) {
                // already in the list.
            } else {
                vertex.setVertexIndex(vertexMap.size());
                vertexMap.add(vertex.vertexIndex(), vertex);
            }
            return this;
        }

        public Builder addEdge(V child, V parent) {
            addVertex(child);
            addVertex(parent);
            successorMap.getIfAbsentPut(parent.vertexIndex(), IntLists.mutable.empty()).add(child.vertexIndex());
            predecessorMap.getIfAbsentPut(child.vertexIndex(), IntLists.mutable.empty()).add(parent.vertexIndex());
            return this;
        }

        public DiGraphEntity<V> build() {

            MutableIntObjectMap<ImmutableIntList> intermediateSuccessorMap = IntObjectMaps.mutable.ofInitialCapacity(successorMap.size());
            successorMap.forEachKeyValue((vertex, successorList) -> intermediateSuccessorMap.put(vertex, successorList.toImmutable()));

            MutableIntObjectMap<ImmutableIntList> intermediatePredecessorMap = IntObjectMaps.mutable.ofInitialCapacity(predecessorMap.size());
            predecessorMap.forEachKeyValue((vertex, predecessorList) -> intermediatePredecessorMap.put(vertex, predecessorList.toImmutable()));

            return new DiGraphEntity(roots.toImmutable(),
                    vertexMap.toImmutable(),
                    intermediateSuccessorMap.toImmutable(),
                    intermediatePredecessorMap.toImmutable());
        }
    }

}
