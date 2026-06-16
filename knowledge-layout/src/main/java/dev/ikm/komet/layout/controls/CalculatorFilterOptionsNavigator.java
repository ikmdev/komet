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
package dev.ikm.komet.layout.controls;

import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * A {@link FilterOptionsNavigator} backed directly by a {@link ViewCalculator}, with no dependency on
 * the {@code navigator} module. This lets {@code knowledge-layout}'s own windows (the KL-engine /
 * Knowledge-Layout-Editor custom layouts driven by {@code ViewContextMenuButtonArea}) feed the
 * View Options popup a navigation source without pulling in {@code dev.ikm.komet.navigator}
 * (ike-issues#661/#684). It mirrors {@code ViewNavigator}'s calculator delegation.
 *
 * @param viewCalculator the view calculator that resolves navigation and descriptions
 */
public record CalculatorFilterOptionsNavigator(ViewCalculator viewCalculator) implements FilterOptionsNavigator {

    @Override
    public int[] getRootNids() {
        return new int[]{TinkarTerm.ROOT_VERTEX.nid()};
    }

    @Override
    public ImmutableList<Edge> getChildEdges(int parentNid) {
        return viewCalculator.childEdges(parentNid);
    }

    @Override
    public ViewCalculator getViewCalculator() {
        return viewCalculator;
    }
}
