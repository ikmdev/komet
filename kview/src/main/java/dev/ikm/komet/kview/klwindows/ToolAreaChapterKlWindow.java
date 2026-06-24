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
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.layout.area.KlToolArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.preferences.KlProfiles;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;

/**
 * A non-entity chapter window that hosts a {@link KlToolArea} (e.g. the Claude Assistant)
 * inside the Journal workspace.
 *
 * <p>The window is a thin host: it instantiates the supplied tool-area factory, hands the
 * area the journal {@link ViewProperties} and a close callback, and exposes the area's
 * {@code fxObject()} as its content pane.
 *
 * <p><b>Restoration uses the Kl framework, not the entity-centric path.</b> Because a tool
 * window has no {@link dev.ikm.tinkar.terms.EntityFacade}, it cannot flow through
 * {@link EntityKlWindowFactory} (whose {@code create}/{@code restore} are typed to
 * {@link AbstractEntityChapterKlWindow}). Instead {@link #save()} records the hosting
 * {@link KlToolArea.Factory} class name, and the static {@link #restore} resolves it across
 * module layers with {@link PluggableService#forName(String)} — the same cross-layer
 * mechanism the Kl framework uses in {@code KlView.restore}. The journal dispatches to it by
 * {@linkplain EntityKlWindowType#getPrefix() window-type prefix}. The area's conversations
 * persist independently (per knowledge base, per user), so a restored area reloads its rail.
 */
public final class ToolAreaChapterKlWindow extends AbstractChapterKlWindow<Pane> {

    private static final Logger LOG = LoggerFactory.getLogger(ToolAreaChapterKlWindow.class);

    /**
     * Non-entity window type shared by all tool-area windows. The prefix is distinct from the
     * entity window types so the journal can dispatch restoration to {@link #restore} and so
     * persisted-workspace scans never mistake a tool window for an entity chapter.
     */
    public static final EntityKlWindowType TOOL_WINDOW_TYPE = new EntityKlWindowType() {
        @Override
        public String getPrefix() {
            return "journal_tool_";
        }

        @Override
        public String toString() {
            return "JOURNAL_TOOL";
        }
    };

    /** Preference key holding the hosting {@link KlToolArea.Factory} class name. */
    public static final String TOOL_AREA_FACTORY_CLASS = "TOOL_AREA_FACTORY_CLASS";

    static {
        // Register the (non-enum) tool window type so EntityKlWindowState.fromPreferences and
        // revert() can round-trip the persisted WINDOW_TYPE string. Idempotent.
        try {
            EntityKlWindowType.Registry.registerInstance(TOOL_WINDOW_TYPE);
        } catch (RuntimeException alreadyRegistered) {
            // already registered (duplicate prefix) — fine
        }
    }

    private final UUID windowTopic;
    private final String areaFactoryClassName;

    /**
     * Creates a tool window hosting the area produced by {@code toolAreaFactory}.
     *
     * @param windowTopic     unique identifier for this window instance
     * @param toolAreaFactory the discovered factory that produces the tool area to host
     * @param viewProperties  the hosting journal's view properties, passed through to the area
     * @param preferences     window preferences node (non-null so the window can be restored)
     */
    public ToolAreaChapterKlWindow(UUID windowTopic,
                                   KlToolArea.Factory<?, ?> toolAreaFactory,
                                   ViewProperties viewProperties,
                                   KometPreferences preferences) {
        super(viewProperties, preferences);
        this.windowTopic = windowTopic;
        this.areaFactoryClassName = toolAreaFactory.getClass().getName();

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

    /**
     * Restores a tool window from its saved preferences using the Kl framework's native,
     * cross-layer factory resolution: it reads the persisted {@link KlToolArea.Factory} class
     * name and loads it with {@link PluggableService#forName(String)} (which spans the plugin
     * module layer), re-creates the area, and re-applies the saved window geometry. No
     * {@code EntityFacade} is involved.
     *
     * @param windowSettings the journal's parent window settings
     * @param preferences    the saved window preferences node
     * @return the restored tool window
     */
    public static ToolAreaChapterKlWindow restore(WindowSettings windowSettings, KometPreferences preferences) {
        final EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);
        final UUID windowTopic = windowState.getWindowId();
        final UUID journalTopic = preferences.getUuid(JOURNAL_TOPIC)
                .orElseThrow(() -> new IllegalStateException("No journal topic for tool window " + windowTopic));
        final ViewProperties viewProperties =
                KlWindowPreferencesUtils.getJournalViewProperties(windowSettings, journalTopic);
        final String factoryClassName = preferences.get(TOOL_AREA_FACTORY_CLASS)
                .orElseThrow(() -> new IllegalStateException("No tool-area factory class for " + windowTopic));
        try {
            final Class<?> factoryClass = PluggableService.forName(factoryClassName);
            final KlToolArea.Factory<?, ?> toolAreaFactory =
                    (KlToolArea.Factory<?, ?>) factoryClass.getDeclaredConstructor().newInstance();
            final ToolAreaChapterKlWindow window =
                    new ToolAreaChapterKlWindow(windowTopic, toolAreaFactory, viewProperties, preferences);
            window.revert();
            LOG.info("Restored tool window {} hosting {}", windowTopic, factoryClassName);
            return window;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to restore tool window " + windowTopic
                    + " from factory " + factoryClassName, e);
        }
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
        // Persist immediately so the journal can restore this window next session
        // (the journal's saveWindows() later re-saves the final placement).
        save();
    }

    // ---- Persistence: window geometry + the hosting tool-area factory class --------------

    @Override
    public void save() {
        super.save();   // base: WINDOW_ID, WINDOW_TYPE, position, size
        if (preferences != null && areaFactoryClassName != null) {
            preferences.put(TOOL_AREA_FACTORY_CLASS, areaFactoryClassName);
            try {
                preferences.flush();
            } catch (Exception e) {
                LOG.error("Failed to persist tool-area factory class for {}", windowTopic, e);
            }
        }
    }

    @Override
    public void revert() {
        super.revert();   // base: re-apply persisted position/size
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
        // The factory-class key is written directly in save(); no extra state object fields.
    }

    @Override
    protected void applyAdditionalState(EntityKlWindowState state) {
        // No additional state to apply.
    }
}
