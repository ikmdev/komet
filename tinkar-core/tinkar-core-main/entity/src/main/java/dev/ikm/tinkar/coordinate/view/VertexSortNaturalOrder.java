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
package dev.ikm.tinkar.coordinate.view;


import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.util.UUID;

public class VertexSortNaturalOrder implements VertexSort, Encodable {
    public static final VertexSortNaturalOrder SINGLETON = new VertexSortNaturalOrder();
    private static final UUID VERTEX_SORT_UUID = UUID.fromString("035a8679-0f77-4a2c-80c2-2495a8e2bf14");

    private VertexSortNaturalOrder() {
    }

    @Decoder
    public static VertexSortNaturalOrder decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                // Using a static method rather than a constructor eliminates the need for
                // a readResolve method, but allows the implementation to decide how
                // to handle special cases. This is the equivalent of readresolve, since it
                // returns an existing object always.
                return SINGLETON;
        }
    }

    @Override
    public UUID getVertexSortUUID() {
        return VERTEX_SORT_UUID;
    }

    @Override
    public String getVertexSortName() {
        return "Natural sort order";
    }

    @Override
    public String getVertexLabel(int vertexConceptNid, LanguageCalculator languageCalculator) {
        return languageCalculator.getDescriptionText(vertexConceptNid).orElse(PrimitiveData.text(vertexConceptNid));
    }

    @Override
    public final int[] sortVertexes(int[] vertexConceptNids, NavigationCalculator navigationCalculator) {
        if (vertexConceptNids.length < 2) {
            // nothing to sort, skip creating the objects for sort.
            return vertexConceptNids;
        }

        return IntLists.immutable.of(vertexConceptNids).primitiveStream().mapToObj(vertexConceptNid ->
                        new VertexItem(vertexConceptNid, navigationCalculator.getDescriptionTextOrNid(vertexConceptNid)))
                .sorted().mapToInt(value -> value.nid).toArray();
    }

    @Override
    public final ImmutableList<Edge> sortEdges(ImmutableCollection<Edge> edges, NavigationCalculator navigationCalculator) {
        if (edges.size() < 2) {
            // nothing to sort, skip creating the objects for sort.
            return Lists.immutable.ofAll(edges);
        }

        MutableList<Edge> edgesToSort = Lists.mutable.ofAll(edges);
        edgesToSort.sort((o1, o2) -> NaturalOrder.compareStrings(o1.comparisonString(), o2.comparisonString()));

        return edgesToSort.toImmutable();
    }

    @Override
    public int hashCode() {
        return this.getClass().getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass().equals(this.getClass());
    }

    @Override
    public String toString() {
        return getVertexSortName();
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        // No fieldValues...
    }

    private static class VertexItem implements Comparable<VertexItem> {
        private final int nid;
        private final String description;

        public VertexItem(int nid, String description) {
            this.nid = nid;
            this.description = description;
        }

        @Override
        public int compareTo(VertexItem o) {
            return NaturalOrder.compareStrings(this.description, o.description);
        }
    }
}
