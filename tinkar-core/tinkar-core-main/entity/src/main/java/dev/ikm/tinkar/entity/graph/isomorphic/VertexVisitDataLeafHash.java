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

import dev.ikm.tinkar.common.util.ArrayUtil;
import dev.ikm.tinkar.entity.graph.VertexVisitData;
import dev.ikm.tinkar.entity.graph.VisitProcessor;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import java.util.BitSet;

public class VertexVisitDataLeafHash extends VertexVisitData {
    /**
     * Initialized values to -1.
     * The VertexAncestorKey algorithm for computing the hashcode will not allow -1, to a value of
     * -1 indicates that no vertex has for that index is yet assigned.
     */
    protected final int[] vertexHashArray;

    protected final BitSet[] leafIndexesAtVertexOrBelowArray;

    protected final ImmutableIntSet[] leafHashesAtVertexOrBelowArray;

    protected final MutableIntObjectMap<BitSet> vertexHashToVertexIndexMap;

    protected final MutableIntObjectMap<MutableIntSet> nidsReferencedAtVertexOrAboveIndexMap;

    public VertexVisitDataLeafHash(int graphSize) {
        this(graphSize, null, null);
    }

    public VertexVisitDataLeafHash(int graphSize, VisitProcessor<? extends VertexVisitDataLeafHash> vertexStartConsumer) {
        this(graphSize, vertexStartConsumer, null);
    }

    public VertexVisitDataLeafHash(int graphSize, VisitProcessor<? extends VertexVisitDataLeafHash> vertexStartConsumer, VisitProcessor<? extends VertexVisitDataLeafHash> vertexEndConsumer) {
        super(graphSize, vertexStartConsumer, vertexEndConsumer);
        this.vertexHashArray = ArrayUtil.createAndFillWithMinusOne(graphSize);
        this.vertexHashToVertexIndexMap = IntObjectMaps.mutable.ofInitialCapacity(graphSize);
        this.nidsReferencedAtVertexOrAboveIndexMap = IntObjectMaps.mutable.ofInitialCapacity(graphSize);
        this.leafIndexesAtVertexOrBelowArray = new BitSet[graphSize];
        this.leafHashesAtVertexOrBelowArray = new ImmutableIntSet[graphSize];
    }
}
