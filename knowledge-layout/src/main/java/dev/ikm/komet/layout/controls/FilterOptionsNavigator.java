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
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Minimal navigation source the View Options popup needs to populate available coordinate options.
 *
 * <p>It deliberately exposes only the three navigation operations the popup actually uses, decoupling
 * the popup controls from the kview {@code dev.ikm.komet.navigator.graph.Navigator} so they can live in
 * the lower {@code knowledge-layout} module without pulling in the {@code navigator} module
 * (ike-issues#684). Hosts that have a graph {@code Navigator} supply one via
 * {@link GraphFilterOptionsNavigator}.
 */
public interface FilterOptionsNavigator {

    /**
     * Returns the navigation root concept nids.
     *
     * @return the root nids, never {@code null} (may be empty)
     */
    int[] getRootNids();

    /**
     * Returns the child edges of the given parent concept in navigation order.
     *
     * @param parentNid the parent concept nid
     * @return the child edges of {@code parentNid}
     */
    ImmutableList<Edge> getChildEdges(int parentNid);

    /**
     * Returns the view calculator backing this navigation source.
     *
     * @return the backing view calculator
     */
    ViewCalculator getViewCalculator();
}
