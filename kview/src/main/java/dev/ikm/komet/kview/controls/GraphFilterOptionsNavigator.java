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
package dev.ikm.komet.kview.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.komet.layout.controls.FilterOptionsNavigator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * A {@link FilterOptionsNavigator} backed by a graph {@link Navigator}. Lets a kview host feed a
 * {@code Navigator} to the relocated View Options popup without the popup depending on the
 * {@code navigator} module (ike-issues#684). This adapter stays in kview, where the {@code navigator}
 * dependency already exists.
 *
 * @param navigator the backing graph navigator
 */
public record GraphFilterOptionsNavigator(Navigator navigator) implements FilterOptionsNavigator {

    @Override
    public int[] getRootNids() {
        return navigator.getRootNids();
    }

    @Override
    public ImmutableList<Edge> getChildEdges(int parentNid) {
        return navigator.getChildEdges(parentNid);
    }

    @Override
    public ViewCalculator getViewCalculator() {
        return navigator.getViewCalculator();
    }
}
