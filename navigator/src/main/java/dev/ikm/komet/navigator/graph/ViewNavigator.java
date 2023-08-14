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
package dev.ikm.komet.navigator.graph;

import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.terms.TinkarTerm;

public class ViewNavigator implements Navigator {

    private final ViewCalculator viewCalculator;

    public ViewNavigator(ViewCoordinate viewCoordinate) {
        viewCalculator = ViewCalculatorWithCache.getCalculator(viewCoordinate.toViewCoordinateRecord());
    }

    @Override
    public int[] getParentNids(int childNid) {
        return viewCalculator.sortedParentsOf(childNid).toArray();
    }

    @Override
    public int[] getChildNids(int parentNid) {
        return viewCalculator.sortedChildrenOf(parentNid).toArray();
    }

    @Override
    public ImmutableList<Edge> getParentEdges(int childNid) {
        return viewCalculator.parentEdges(childNid);
    }

    @Override
    public ImmutableList<Edge> getChildEdges(int parentNid) {
        return viewCalculator.childEdges(parentNid);
    }

    @Override
    public boolean isLeaf(int conceptNid) {
        return viewCalculator.unsortedChildrenOf(conceptNid).isEmpty();
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        return viewCalculator.unsortedChildrenOf(parentNid).contains(childNid);
    }

    @Override
    public boolean isDescendentOf(int descendantNid, int ancestorNid) {
        return viewCalculator.descendentsOf(ancestorNid).contains(descendantNid);
    }

    @Override
    public int[] getRootNids() {
        return new int[]{TinkarTerm.ROOT_VERTEX.nid()};
    }

    @Override
    public ViewCalculator getViewCalculator() {
        return viewCalculator;
    }
}
