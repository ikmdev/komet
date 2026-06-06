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
package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.area.KlToolArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.preferences.KlProfiles;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * A non-entity chapter window that hosts a {@link KlToolArea} (e.g. the Claude Assistant)
 * inside the Journal workspace.
 *
 * <p>The window is a thin host: it instantiates the supplied tool-area factory, hands the
 * area the journal {@link ViewProperties} and a close callback, and exposes the area's
 * {@code fxObject()} as its content pane. Tool windows are ephemeral — their conversation
 * state is not persisted or restored across sessions — so {@link #save()} and
 * {@link #revert()} are intentionally no-ops.
 */
public final class ToolAreaChapterKlWindow extends AbstractChapterKlWindow<Pane> {

    private static final Logger LOG = LoggerFactory.getLogger(ToolAreaChapterKlWindow.class);

    /**
     * Ephemeral, non-entity window type shared by all tool-area windows. The prefix is
     * distinct from the entity window types so persisted-workspace scans never mistake a
     * tool window for an entity chapter.
     */
    private static final EntityKlWindowType TOOL_WINDOW_TYPE = new EntityKlWindowType() {
        @Override
        public String getPrefix() {
            return "journal_tool_";
        }

        @Override
        public String toString() {
            return "JOURNAL_TOOL";
        }
    };

    private final UUID windowTopic;

    /**
     * Creates a tool window hosting the area produced by {@code toolAreaFactory}.
     *
     * @param windowTopic     unique identifier for this window instance
     * @param toolAreaFactory the discovered factory that produces the tool area to host
     * @param viewProperties  the hosting journal's view properties, passed through to the area
     * @param preferences     window preferences, or {@code null} for an ephemeral window
     */
    public ToolAreaChapterKlWindow(UUID windowTopic,
                                   KlToolArea.Factory<?, ?> toolAreaFactory,
                                   ViewProperties viewProperties,
                                   KometPreferences preferences) {
        super(viewProperties, preferences);
        this.windowTopic = windowTopic;

        final KlPreferencesFactory areaPreferencesFactory =
                KlProfiles.sharedWindowPreferenceFactory(toolAreaFactory.getClass());
        final KlToolArea<?> toolArea = toolAreaFactory.create(areaPreferencesFactory);
        toolArea.setToolViewProperties(viewProperties);
        toolArea.setOnCloseRequest(() -> getOnClose().ifPresent(Runnable::run));

        final Region areaNode = toolArea.fxObject();
        final BorderPane root = new BorderPane(areaNode);
        this.paneWindow = root;

        LOG.info("Created tool window {} hosting {}", windowTopic, toolAreaFactory.toolName());
    }

    @Override
    public UUID getWindowTopic() {
        return windowTopic;
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return TOOL_WINDOW_TYPE;
    }

    @Override
    public void onShown() {
        // No deferred initialization required; the area builds its UI on construction.
    }

    // ---- Ephemeral: conversation state is not persisted across sessions ------------------

    @Override
    public void save() {
        // Intentionally no-op: tool windows are not restored across sessions.
    }

    @Override
    public void revert() {
        // Intentionally no-op: nothing to restore.
    }

    // ---- AbstractChapterKlWindow contract (no property panel for tool windows) -----------

    @Override
    protected boolean isPropertyPanelOpen() {
        return false;
    }

    @Override
    protected void setPropertyPanelOpen(boolean isOpen) {
        // No property panel.
    }

    @Override
    protected String selectedPropertyPanel() {
        return null;
    }

    @Override
    protected void setSelectedPropertyPanel(String selectedPanel) {
        // No property panel.
    }

    @Override
    protected void captureAdditionalState(EntityKlWindowState state) {
        // Ephemeral window: no additional state to capture.
    }

    @Override
    protected void applyAdditionalState(EntityKlWindowState state) {
        // Ephemeral window: no additional state to apply.
    }
}
