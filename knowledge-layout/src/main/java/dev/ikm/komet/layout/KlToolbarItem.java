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
package dev.ikm.komet.layout;

import dev.ikm.komet.layout.area.AreaGridSettings;
import javafx.scene.Node;

/**
 * A single item contributed to a title-bar / toolbar region — the loosely-coupled, factory-produced unit of
 * card and drawer chrome. Unlike a {@link KlArea}, a toolbar item is a lightweight <em>leaf</em>: it supplies a
 * JavaFX node and a grid {@linkplain #placement() placement}, plus an optional bind/unbind lifecycle for the
 * stateful ones (a coordinate control, a state-reflecting toggle). It deliberately avoids the per-area
 * preferences-node + factory-class scaffolding so a simple control is not over-engineered.
 *
 * <p>Items are produced by a {@link Factory} and placed by their {@link #placement()} {@link AreaGridSettings}
 * into the toolbar's grid — the same mechanism that lays out the card body — so a contributor declares only its
 * own region/column and never needs to know what else is on the bar. A growing spacer item (placement with
 * {@code hGrow = ALWAYS}) right-aligns the items after it, exactly as a spacer column would.
 *
 * @see Factory
 */
public interface KlToolbarItem {

    /**
     * Returns the JavaFX node placed in the toolbar for this item.
     *
     * @return the toolbar node
     */
    Node toolbarNode();

    /**
     * Returns the grid placement (column/row/span, grow, alignment, margin) for this item within the toolbar.
     *
     * @return the placement
     */
    AreaGridSettings placement();

    /**
     * Establishes any bindings or listeners this item needs (for example a toggle observing the state it
     * reflects, or a control observing the card's coordinate). Default does nothing — stateless items need no
     * binding. Paired with {@link #unbind()}.
     */
    default void bind() {
        // Stateless item: nothing to bind.
    }

    /**
     * Releases the bindings or listeners established by {@link #bind()}, so a toolbar can be rebuilt without
     * leaking listeners onto the state the item observed. Default does nothing.
     */
    default void unbind() {
        // Stateless item: nothing to unbind.
    }

    /**
     * Factory contract for producing toolbar items. Concrete factories add typed {@code create(...)} methods
     * for programmatic construction (and may later be discovered for plugin contribution).
     *
     * @param <I> the concrete {@link KlToolbarItem} type this factory produces
     */
    interface Factory<I extends KlToolbarItem> {

        /**
         * The default placement for items produced by this factory, used when a caller does not supply one.
         *
         * @return the default {@link AreaGridSettings} placement
         */
        AreaGridSettings defaultPlacement();
    }
}
