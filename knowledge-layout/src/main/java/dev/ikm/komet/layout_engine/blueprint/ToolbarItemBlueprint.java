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
package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlToolbarItem;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.layout.Region;

/**
 * Blueprint base for {@link KlToolbarItem} implementations — the engine-side plumbing for a <em>leaf</em> area
 * placed on a toolbar. It supplies the full {@link StateAndContextBlueprint} machinery (preferences-backed
 * state, view context, lifecycle, save/revert/restore) and the {@link AreaBlueprint} grid placement, but —
 * unlike {@link SupplementalAreaBlueprint} or {@link CardBlueprint} — it is <em>not</em> a {@code KlParent}: a
 * toolbar item hosts no child areas, so there is no children grid. The concrete item supplies its own root
 * {@code Region} (a switch, a button, a spacer) and persists its own content through the standard hooks.
 *
 * <p>It is a non-sealed member of the sealed {@link AreaBlueprint} family, so concrete items extend it the same
 * way concrete cards extend {@link CardBlueprint}.
 *
 * @param <FX> the type of JavaFX {@code Region} that serves as the item's root node
 */
public non-sealed abstract class ToolbarItemBlueprint<FX extends Region> extends AreaBlueprint<FX>
        implements KlToolbarItem<FX> {

    /**
     * Restores a toolbar-item blueprint from previously stored preferences.
     *
     * @param preferences the preferences node backing this item
     * @param fxObject     the item's root node
     */
    public ToolbarItemBlueprint(KometPreferences preferences, FX fxObject) {
        super(preferences, fxObject);
    }

    /**
     * Creates a new toolbar-item blueprint from a preferences factory and the producing area factory.
     *
     * @param preferencesFactory the factory that provisions this item's preferences node
     * @param areaFactory        the {@link KlToolbarItem.Factory} (as a {@link KlArea.Factory}) producing this item
     * @param fxObject           the item's root node
     */
    public ToolbarItemBlueprint(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory, FX fxObject) {
        super(preferencesFactory, areaFactory, fxObject);
    }

    /**
     * The factory contract for concrete toolbar items, mirroring {@link SupplementalAreaBlueprint.Factory}.
     *
     * @param <FX> the type of JavaFX {@code Region} that serves as the produced item's root node
     * @param <KL> the concrete {@link KlToolbarItem} type produced
     */
    public interface Factory<FX extends Region, KL extends KlToolbarItem<FX>>
            extends KlToolbarItem.Factory<FX, KL> {
    }
}
