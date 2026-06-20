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
import dev.ikm.komet.layout_engine.host.ToolCard;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getWindowPreferences;

/**
 * The thin Journal on-ramp for a {@link ToolCard}: a {@link ChapterKlWindow} that embeds a kview-free,
 * layout-engine {@code ToolCard} (hosting a {@link KlToolArea} such as the Claude Assistant) into the
 * Journal workspace. It is the card-native replacement for the retired {@code ToolAreaChapterKlWindow}.
 *
 * <p>Like {@link DynamicCardKlWindow}, all the realization logic — context binding, chrome, drag,
 * lifecycle, and the per-instance sandboxing of the hosted tool's preferences — lives in the
 * {@code ToolCard}; this window only bridges it to the kview-typed workspace seam
 * ({@code KLWorkspace.getWindows()} holds {@link ChapterKlWindow}) and persists the journal topic plus
 * window geometry. The card carries its own hosted-tool identity (factory class + per-instance node) in
 * its own preferences node, so restore reconstructs the tool with its own conversation rail intact.
 *
 * <p><b>Sandboxing.</b> Two tool-card windows of the same kind — two Claude Assistants in one journal —
 * never share preferences, because each {@code ToolCard} roots its tool's node under the card's own
 * per-instance node rather than a node keyed by the tool-area factory class.
 */
public final class ToolCardKlWindow extends AbstractChapterKlWindow<Pane> {

    private static final Logger LOG = LoggerFactory.getLogger(ToolCardKlWindow.class);

    /**
     * Non-entity window type shared by all tool-card windows. Its prefix is distinct from the entity
     * window types (and from the dynamic-card prefix) so the journal can dispatch restoration to
     * {@link #restore} and so persisted-workspace scans never mistake a tool card for an entity chapter.
     */
    public static final EntityKlWindowType TOOL_CARD_WINDOW_TYPE = new EntityKlWindowType() {
        @Override
        public String getPrefix() {
            return "journal_tool_card_";
        }

        @Override
        public String toString() {
            return "JOURNAL_TOOL_CARD";
        }
    };

    static {
        // Register the (non-enum) tool-card window type so EntityKlWindowState.fromPreferences and
        // revert() can round-trip the persisted WINDOW_TYPE string. Idempotent.
        try {
            EntityKlWindowType.Registry.registerInstance(TOOL_CARD_WINDOW_TYPE);
        } catch (RuntimeException alreadyRegistered) {
            // already registered (duplicate prefix) — fine
        }
    }

    private final UUID windowTopic;
    private UUID journalTopic;
    private final ToolCard card;

    /**
     * Creates a new tool-card window hosting a freshly created {@link ToolCard} for the given tool-area
     * factory.
     *
     * @param journalTopic    the journal topic for event coordination
     * @param toolAreaFactory the discovered tool-area factory to host
     * @param viewProperties  the journal's view properties (the card overrides these)
     * @param preferences     this window's preferences node, or {@code null} to create one
     * @return the created window
     */
    public static ToolCardKlWindow create(UUID journalTopic,
                                          KlToolArea.Factory<?, ?> toolAreaFactory,
                                          ViewProperties viewProperties,
                                          KometPreferences preferences) {
        final KometPreferences windowPreferences = preferences != null
                ? preferences : getWindowPreferences(journalTopic, TOOL_CARD_WINDOW_TYPE);
        // The card discovers its container's coordinate from the scene graph at bind; it isn't handed a
        // view. Its hosted-tool identity is persisted in its own node under this window.
        final KlPreferencesFactory cardPreferencesFactory =
                KlPreferencesFactory.create(windowPreferences, ToolCard.class);
        final ToolCard card = ToolCard.create(cardPreferencesFactory, toolAreaFactory, journalTopic);
        return new ToolCardKlWindow(journalTopic, card, viewProperties, windowPreferences);
    }

    /**
     * Wraps an already-built {@link ToolCard} (freshly created via {@link #create} or restored via
     * {@link #restore}) in the chapter-window adapter, wiring the close callback.
     *
     * @param journalTopic      the journal topic for event coordination
     * @param card              the card to host
     * @param viewProperties    the journal's view properties
     * @param windowPreferences this window's already-resolved preferences node
     */
    private ToolCardKlWindow(UUID journalTopic,
                             ToolCard card,
                             ViewProperties viewProperties,
                             KometPreferences windowPreferences) {
        super(viewProperties, windowPreferences);
        this.journalTopic = journalTopic;

        final EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(this.preferences);
        this.windowTopic = windowState.getWindowId();
        setWindowState(windowState);

        this.card = card;
        // The chrome's close control delegates to the window's close action (wired by the journal's
        // setupWorkspaceWindow), so closing a tool card removes it from the workspace.
        this.card.setOnCloseRequest(() -> getOnClose().ifPresent(Runnable::run));
        this.paneWindow = card.fxObject();

        LOG.info("Tool-card window {} hosting a tool card", windowTopic);
    }

    /**
     * Restores a tool-card window by reconstructing its {@link ToolCard} from the card's OWN preferences
     * node via the framework restore ({@code UNRESTORED → revert()}) — the card carries its own hosted-tool
     * identity and re-instantiates the tool from its per-instance node. The card is handed the journal's
     * <em>live</em> {@link ViewProperties} so a restored card tracks journal-coordinate changes like a
     * newly created one.
     *
     * @param preferences           the saved window preferences node
     * @param journalViewProperties the journal's live view properties (the card overrides these)
     * @return the restored window
     */
    public static ToolCardKlWindow restore(KometPreferences preferences, ViewProperties journalViewProperties) {
        final EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);
        final UUID windowTopic = windowState.getWindowId();
        final UUID journalTopic = preferences.getUuid(JOURNAL_TOPIC)
                .orElseThrow(() -> new IllegalStateException("No journal topic for tool-card window " + windowTopic));

        // Reconstruct the card from its OWN node (named for ToolCard — deterministic) via the framework
        // restore, then revert() to load its persisted hosted-tool identity. Address the node directly:
        // the factory supplier would allocate a fresh sequential node instead of reusing the saved one.
        final KometPreferences cardNode = preferences.node(ToolCard.class.getSimpleName());
        final ToolCard card = ToolCard.restore(cardNode);
        card.revert();
        if (!card.isContentRestored()) {
            LOG.warn("Tool-card window {} restored with no hosted-tool identity; it will be empty", windowTopic);
        }

        final ToolCardKlWindow window =
                new ToolCardKlWindow(journalTopic, card, journalViewProperties, preferences);
        window.revert();   // restore the window geometry (AbstractChapterKlWindow state)
        LOG.info("Restored tool-card window {} from the card's own node", windowTopic);
        return window;
    }

    @Override
    public UUID getWindowTopic() {
        return windowTopic;
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return TOOL_CARD_WINDOW_TYPE;
    }

    @Override
    public void onShown() {
        // Bind the card's knowledge-layout lifecycle now that it is on the workspace, then persist
        // so the journal can restore this window next session.
        card.knowledgeLayoutBind();
        save();
    }

    @Override
    public void delete() {
        // Tear down the card's per-card view context (removes its coordinate listener) before the
        // window's preferences are removed.
        card.knowledgeLayoutUnbind();
        super.delete();
    }

    // ---- Persistence: window geometry + the hosted-tool identity (in the card's own node) --------

    @Override
    public void save() {
        super.save();   // base: WINDOW_ID, WINDOW_TYPE, position, size, plus captureAdditionalState below
        // Persist the card's hosted-tool identity to its OWN node via the framework save path (subAreaSave).
        card.save();
        if (preferences != null) {
            try {
                preferences.flush();
            } catch (Exception e) {
                LOG.error("Failed to persist tool-card window {}", windowTopic, e);
            }
        }
    }

    @Override
    protected void captureAdditionalState(EntityKlWindowState state) {
        if (journalTopic != null) {
            state.addProperty(JOURNAL_TOPIC.name(), journalTopic.toString());
        }
    }

    @Override
    protected void applyAdditionalState(EntityKlWindowState state) {
        final String journalTopicString = state.getStringProperty(JOURNAL_TOPIC.name(), null);
        if (journalTopicString != null) {
            this.journalTopic = UUID.fromString(journalTopicString);
        }
    }

    // ---- AbstractChapterKlWindow contract (no property panel for tool-card windows) --------------

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
}
