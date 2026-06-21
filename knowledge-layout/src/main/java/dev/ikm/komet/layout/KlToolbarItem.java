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

import javafx.scene.layout.Region;

/**
 * A single item contributed to a title-bar / toolbar region — a first-class, factory-produced unit of card and
 * drawer chrome. It is a {@code non-sealed} member of the sealed {@link KlArea} family (added the same way
 * {@link KlCard} was), so a toolbar item is a real area: it has a preferences node, saves and restores its
 * state, participates in the {@code knowledgeLayoutBind}/{@code Unbind} lifecycle, and is placed by its
 * {@link dev.ikm.komet.layout.area.AreaGridSettings} into the toolbar's grid — exactly like every other area.
 *
 * <p>Unlike {@link KlCard} (which is also a {@link KlParent}), a toolbar item is a <em>leaf</em>: it hosts no
 * child areas. Its {@link #fxObject() root node} is the node placed on the bar. Because placement, persistence,
 * and lifecycle all come from the area framework, a contributor declares only its own region/column and never
 * needs to know what else is on the bar; a growing-spacer item ({@code hGrow = ALWAYS}) right-aligns the items
 * after it.
 *
 * @param <FX> the type of JavaFX {@code Region} that serves as this item's root node
 * @see KlCard
 */
public non-sealed interface KlToolbarItem<FX extends Region> extends KlArea<FX> {

    /**
     * The factory contract for producing and restoring {@link KlToolbarItem} instances. It is a non-sealed
     * member of the sealed {@link KlArea.Factory} family, mirroring {@link KlCard.Factory}.
     *
     * @param <FX> the type of JavaFX {@code Region} that serves as the produced item's root node
     * @param <KL> the concrete {@link KlToolbarItem} type this factory produces
     */
    non-sealed interface Factory<FX extends Region, KL extends KlToolbarItem<FX>>
            extends KlArea.Factory<FX, KL> {
    }
}
