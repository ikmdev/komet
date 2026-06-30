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
import dev.ikm.komet.kview.mvvm.view.journal.JournalKlContext;
import dev.ikm.komet.layout_engine.host.AbstractHostCard;
import dev.ikm.komet.layout_engine.host.KlCardProvider;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getWindowPreferences;

/**
 * The generic Journal on-ramp for a plugin-contributed {@link AbstractHostCard}: a {@link ChapterKlWindow}
 * that embeds any card produced by a {@link KlCardProvider} into the Journal workspace. It is the card-tier
 * generalization of {@link DynamicCardKlWindow} / {@link ToolCardKlWindow} — the card supplies its own chrome,
 * coordinate context, lifecycle, and sandboxed prefs-node storage; this window only bridges it to the
 * kview-typed workspace seam ({@code KLWorkspace.getWindows()} holds {@link ChapterKlWindow}) and persists the
 * journal topic, window geometry, and the provider class name.
 *
 * <p><b>Restoration uses the Kl framework, not the entity-factory registry.</b> The window persists the
 * {@link KlCardProvider} class name and, on restore, resolves it across module layers with
 * {@link PluggableService#forName(String)} (spanning the plugin layer) and hands the saved node back to the
 * provider, which reconstructs the card from its own preferences. The journal dispatches here by
 * {@linkplain EntityKlWindowType#getPrefix() window-type prefix}.
 */
public final class CardKlWindow extends AbstractChapterKlWindow<Pane> {

    private static final Logger LOG = LoggerFactory.getLogger(CardKlWindow.class);

    /**
     * Non-entity window type shared by all provider-contributed card windows. Its prefix is distinct from the
     * entity window types (and the dynamic-card / tool-card prefixes) so the journal can dispatch restoration
     * to {@link #restore} and persisted-workspace scans never mistake a card for an entity chapter.
     */
    public static final EntityKlWindowType CARD_WINDOW_TYPE = new EntityKlWindowType() {
        @Override
        public String getPrefix() {
            return "journal_card_";
        }

        @Override
        public String toString() {
            return "JOURNAL_CARD";
        }
    };

    /** Preference key holding the contributing {@link KlCardProvider} class name. */
    public static final String CARD_PROVIDER_CLASS = "CARD_PROVIDER_CLASS";

    static {
        // Register the (non-enum) card window type so EntityKlWindowState.fromPreferences and revert() can
        // round-trip the persisted WINDOW_TYPE string. Idempotent.
        try {
            EntityKlWindowType.Registry.registerInstance(CARD_WINDOW_TYPE);
        } catch (RuntimeException alreadyRegistered) {
            // already registered (duplicate prefix) — fine
        }
    }

    private final UUID windowTopic;
    private UUID journalTopic;
    private final String providerClassName;
    private final AbstractHostCard card;

    /**
     * Creates a new card window hosting a freshly created card from {@code provider}.
     *
     * @param journalTopic   the journal topic for event coordination
     * @param provider       the discovered card provider
     * @param viewProperties the journal's view properties (the card overrides these)
     * @param preferences    this window's preferences node, or {@code null} to create one
     * @return the created window
     */
    public static CardKlWindow create(UUID journalTopic,
                                      KlCardProvider provider,
                                      ViewProperties viewProperties,
                                      KometPreferences preferences) {
        final KometPreferences windowPreferences = preferences != null
                ? preferences : getWindowPreferences(journalTopic, CARD_WINDOW_TYPE);
        final AbstractHostCard card = provider.createCard(windowPreferences, journalTopic);
        return new CardKlWindow(journalTopic, provider.getClass().getName(), card, viewProperties, windowPreferences);
    }

    private CardKlWindow(UUID journalTopic,
                         String providerClassName,
                         AbstractHostCard card,
                         ViewProperties viewProperties,
                         KometPreferences windowPreferences) {
        super(viewProperties, windowPreferences);
        this.journalTopic = journalTopic;
        this.providerClassName = providerClassName;

        final EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(this.preferences);
        this.windowTopic = windowState.getWindowId();
        setWindowState(windowState);

        this.card = card;
        // Seed the card's coordinate-context parent with the journal's live view, so it inherits the
        // data-bearing journal coordinate instead of falling back to the default knowledge-base view when its
        // pane is not yet a workspace scene-graph descendant at the deferred bind pulse. ToolCard does the
        // equivalent via setToolViewProperties; this is the card-window analogue, and fixes both the create
        // and restore paths because both run this constructor.
        this.card.setSeedParentContext(new JournalKlContext(viewProperties.nodeView()));
        // The chrome's close control delegates to the window's close action (wired by the journal's
        // setupWorkspaceWindow), so closing a card removes it from the workspace.
        this.card.setOnCloseRequest(() -> getOnClose().ifPresent(Runnable::run));
        this.paneWindow = card.fxObject();

        LOG.info("Card window {} hosting card from {}", windowTopic, providerClassName);
    }

    /**
     * Restores a card window by resolving its {@link KlCardProvider} (persisted class name) across module
     * layers and letting the provider reconstruct the card from its own node. The card is handed the journal's
     * <em>live</em> {@link ViewProperties} so a restored card tracks journal-coordinate changes like a new one.
     *
     * @param preferences           the saved window preferences node
     * @param journalViewProperties the journal's live view properties (the card overrides these)
     * @return the restored window
     */
    public static CardKlWindow restore(KometPreferences preferences, ViewProperties journalViewProperties) {
        final EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);
        final UUID windowTopic = windowState.getWindowId();
        final UUID journalTopic = preferences.getUuid(JOURNAL_TOPIC)
                .orElseThrow(() -> new IllegalStateException("No journal topic for card window " + windowTopic));
        final String providerClassName = preferences.get(CARD_PROVIDER_CLASS)
                .orElseThrow(() -> new IllegalStateException("No card provider class for " + windowTopic));

        final KlCardProvider provider = resolveProvider(providerClassName);
        final AbstractHostCard card = provider.restoreCard(preferences);

        final CardKlWindow window =
                new CardKlWindow(journalTopic, providerClassName, card, journalViewProperties, preferences);
        window.revert();   // restore the window geometry (AbstractChapterKlWindow state)
        LOG.info("Restored card window {} from {}", windowTopic, providerClassName);
        return window;
    }

    /** Resolves a {@link KlCardProvider} by class name across module layers (spans the plugin layer). */
    private static KlCardProvider resolveProvider(String providerClassName) {
        try {
            final Class<?> providerClass = PluggableService.forName(providerClassName);
            return (KlCardProvider) providerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to resolve card provider " + providerClassName, e);
        }
    }

    @Override
    public UUID getWindowTopic() {
        return windowTopic;
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return CARD_WINDOW_TYPE;
    }

    @Override
    public void onShown() {
        // Bind the card's knowledge-layout lifecycle now that it is on the workspace, then persist so the
        // journal can restore this window next session.
        card.knowledgeLayoutBind();
        save();
    }

    @Override
    public void delete() {
        // Tear down the card's per-card view context (removes its coordinate listener) before the window's
        // preferences are removed — which deletes the card's prefs-node directory and its sandboxed files.
        card.knowledgeLayoutUnbind();
        super.delete();
    }

    // ---- Persistence: window geometry + the card's own node + the provider class -----------------

    @Override
    public void save() {
        super.save();   // base: WINDOW_ID, WINDOW_TYPE, position, size, plus captureAdditionalState below
        card.save();    // the card persists its own content to its own node (framework save)
        if (preferences != null) {
            preferences.put(CARD_PROVIDER_CLASS, providerClassName);
            try {
                preferences.flush();
            } catch (Exception e) {
                LOG.error("Failed to persist card window {}", windowTopic, e);
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

    // ---- AbstractChapterKlWindow contract (no property panel for card windows) -------------------

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
