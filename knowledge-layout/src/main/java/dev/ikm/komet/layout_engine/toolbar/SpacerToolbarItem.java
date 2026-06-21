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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * A {@link dev.ikm.komet.layout.KlToolbarItem} that contributes a growing spacer — an empty region with
 * {@code hGrow = ALWAYS} — so the items placed after it are pushed to the trailing (right) edge. It is the
 * toolbar's right-alignment lever, expressed as a placed area rather than a hand-coded gap. It is a leaf area
 * with no content state of its own, so its {@code subArea*} hooks are empty.
 */
public final class SpacerToolbarItem extends ToolbarItemBlueprint<Region> {

    private SpacerToolbarItem(KometPreferences preferences) {
        super(preferences, new Region());
        HBox.setHgrow(fxObject(), Priority.ALWAYS);
        GridPane.setHgrow(fxObject(), Priority.ALWAYS);
    }

    private SpacerToolbarItem(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new Region());
        HBox.setHgrow(fxObject(), Priority.ALWAYS);
        GridPane.setHgrow(fxObject(), Priority.ALWAYS);
    }

    @Override
    protected void subAreaSave() {
        // A spacer has no content state.
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        // A spacer has no content state.
    }

    @Override
    protected void subAreaRevert() {
        // A spacer has no content state.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

    @Override
    public void knowledgeLayoutUnbind() {
        // Nothing to unbind.
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
     * Restores a {@code SpacerToolbarItem} from previously stored preferences.
     *
     * @param preferences the preferences node backing the item
     * @return the restored item
     */
    public static SpacerToolbarItem restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    /**
     * Factory that produces and restores {@link SpacerToolbarItem} instances.
     */
    public static final class Factory implements ToolbarItemBlueprint.Factory<Region, SpacerToolbarItem> {

        @Override
        public SpacerToolbarItem restore(KometPreferences preferences) {
            return new SpacerToolbarItem(preferences);
        }

        @Override
        public SpacerToolbarItem create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            SpacerToolbarItem item = new SpacerToolbarItem(preferencesFactory, this);
            item.setAreaLayout(areaGridSettings.with(this.getClass()));
            return item;
        }
    }
}
