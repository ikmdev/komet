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
package dev.ikm.komet.layout_engine.toolbar;

import dev.ikm.komet.layout.KlToolbarItem;
import dev.ikm.komet.layout.area.AreaGridSettings;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * A {@link KlToolbarItem} that contributes a growing spacer — an empty region with {@code hGrow = ALWAYS} — so
 * the items placed after it are pushed to the trailing (right) edge. It is the toolbar's right-alignment lever,
 * expressed as a placed item rather than a hand-coded gap; in a grid toolbar its column absorbs the slack, and
 * in a row ({@code HBox}) the node's own {@code hgrow} does the same.
 */
public final class SpacerToolbarItem implements KlToolbarItem {

    private final Region node = new Region();
    private final AreaGridSettings placement;

    private SpacerToolbarItem(AreaGridSettings placement) {
        this.placement = placement;
        HBox.setHgrow(node, Priority.ALWAYS);
    }

    @Override
    public Node toolbarNode() {
        return node;
    }

    @Override
    public AreaGridSettings placement() {
        return placement;
    }

    /**
     * Returns a factory for {@code SpacerToolbarItem} instances.
     *
     * @return a new {@link Factory}
     */
    public static Factory factory() {
        return new Factory();
    }

    /**
     * Factory that produces {@link SpacerToolbarItem} instances.
     */
    public static final class Factory implements KlToolbarItem.Factory<SpacerToolbarItem> {

        @Override
        public AreaGridSettings defaultPlacement() {
            return AreaGridSettings.DEFAULT.withHGrow(Priority.ALWAYS);
        }

        /**
         * Creates a spacer item with the default (growing) placement.
         *
         * @return the created item
         */
        public SpacerToolbarItem create() {
            return create(defaultPlacement());
        }

        /**
         * Creates a spacer item with the given placement.
         *
         * @param placement the grid placement within the toolbar
         * @return the created item
         */
        public SpacerToolbarItem create(AreaGridSettings placement) {
            return new SpacerToolbarItem(placement);
        }
    }
}
