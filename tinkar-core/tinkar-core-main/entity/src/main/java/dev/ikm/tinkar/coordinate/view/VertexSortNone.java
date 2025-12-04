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
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.UUID;

public class VertexSortNone implements VertexSort, Encodable {

    public static final VertexSortNone SINGLETON = new VertexSortNone();
    private static final UUID VERTEX_SORT_UUID = UUID.fromString("9e21329f-da07-4a15-8664-7a08ebdad987");

    private VertexSortNone() {
    }

    @Decoder
    public static VertexSortNone decode(DecoderInput in) {
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
        return "No sort order";
    }

    @Override
    public String getVertexLabel(int vertexConceptNid, LanguageCalculator languageCalculator) {
        return languageCalculator.getDescriptionText(vertexConceptNid).orElse(PrimitiveData.text(vertexConceptNid));
    }

    @Override
    public int[] sortVertexes(int[] vertexConceptNids, NavigationCalculator navigationCalculator) {
        return vertexConceptNids;
    }

    @Override
    public ImmutableList<Edge> sortEdges(ImmutableCollection<Edge> edges, NavigationCalculator navigationCalculator) {
        return Lists.immutable.ofAll(edges);
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        // No fieldValues...
    }
}
