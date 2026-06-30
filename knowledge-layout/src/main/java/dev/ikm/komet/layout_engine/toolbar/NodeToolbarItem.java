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

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.ToolbarItemBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * A {@link dev.ikm.komet.layout.KlToolbarItem} that wraps an arbitrary, already-built JavaFX {@link Node} (a
 * coordinate control, a Publish button, a close button) as a placed toolbar area. It is the cheap adapter that
 * lets existing card chrome become first-class toolbar items without rewriting the control's behavior: the card
 * builds the node and its logic as before, then wraps it here for uniform placement and lifecycle. The wrapped
 * control owns its own state, so this item's {@code subArea*} hooks are empty.
 */
public final class NodeToolbarItem extends ToolbarItemBlueprint<HBox> {

    private NodeToolbarItem(KometPreferences preferences) {
        super(preferences, new HBox());
        fxObject().setAlignment(Pos.CENTER_LEFT);
    }

    private NodeToolbarItem(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new HBox());
        fxObject().setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Installs the node this item presents, replacing any previous content.
     *
     * @param node the node to wrap
     */
    public void setNode(Node node) {
        fxObject().getChildren().setAll(node);
    }

    @Override
    protected void subAreaSave() {
        // The wrapped control owns its state.
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        // The wrapped control owns its state.
    }

    @Override
    protected void subAreaRevert() {
        // The wrapped control owns its state.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

    @Override
    public void knowledgeLayoutUnbind() {
        // Nothing to unbind; the wrapped control manages its own listeners.
    }

    /**
     * Returns a factory for {@code NodeToolbarItem} instances.
     *
     * @return a new {@link Factory}
     */
    public static Factory factory() {
        return new Factory();
    }

    /**
     * Restores a {@code NodeToolbarItem} from previously stored preferences.
     *
     * @param preferences the preferences node backing the item
     * @return the restored item
     */
    public static NodeToolbarItem restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    /**
     * Factory that produces and restores {@link NodeToolbarItem} instances.
     */
    public static final class Factory implements ToolbarItemBlueprint.Factory<HBox, NodeToolbarItem> {

        @Override
        public NodeToolbarItem restore(KometPreferences preferences) {
            return new NodeToolbarItem(preferences);
        }

        @Override
        public NodeToolbarItem create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            NodeToolbarItem item = new NodeToolbarItem(preferencesFactory, this);
            item.setAreaLayout(areaGridSettings.with(this.getClass()));
            return item;
        }
    }
}
