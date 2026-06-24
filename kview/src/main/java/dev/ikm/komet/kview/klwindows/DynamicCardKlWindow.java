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
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.host.DynamicCard;
import dev.ikm.komet.layout_engine.host.DynamicComponentCard;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getWindowPreferences;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;

/**
 * The thin Journal on-ramp for a {@link DynamicCard}: a {@link ChapterKlWindow} that embeds a
 * kview-free, layout-engine {@code DynamicCard} into the Journal workspace.
 *
 * <p>It is the component-layout sibling of {@link ToolCardKlWindow}: a host that instantiates
 * the engine object, hands it the journal {@link ViewProperties} (which the card overrides into its
 * own per-card coordinate), and exposes the card's {@code fxObject()} as its content pane. All the
 * realization logic — sections, patterns, supplemental areas, context binding, lifecycle — lives in
 * the {@code DynamicCard}; this window only bridges it to the kview-typed workspace seam
 * ({@code KLWorkspace.getWindows()} holds {@link ChapterKlWindow}).
 *
 * <p><b>Restoration uses the Kl framework, not the entity-factory registry.</b> A dynamic card is
 * not tied to an entity <em>type</em> (it realizes any designed layout for any reference component),
 * so it does not flow through {@link EntityKlWindowFactory}. Instead it carries its own
 * {@link #DYNAMIC_CARD_WINDOW_TYPE}; {@link #save()} records the editor layout title and reference
 * component, and the static {@link #restore} rebuilds the card from those. The journal dispatches to
 * it by {@linkplain EntityKlWindowType#getPrefix() window-type prefix}, exactly as it does for tool
 * windows.
 */
public final class DynamicCardKlWindow extends AbstractChapterKlWindow<Pane> {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicCardKlWindow.class);

    /**
     * Non-entity window type shared by all dynamic-card windows. Its prefix is distinct from the
     * entity window types so the journal can dispatch restoration to {@link #restore} and so
     * persisted-workspace scans never mistake a card for an entity chapter.
     */
    public static final EntityKlWindowType DYNAMIC_CARD_WINDOW_TYPE = new EntityKlWindowType() {
        @Override
        public String getPrefix() {
            return "journal_dynamic_card_";
        }

        @Override
        public String toString() {
            return "JOURNAL_DYNAMIC_CARD";
        }
    };

    /** Preference key holding the title of the editor-designed layout this card realizes. */
    public static final String DYNAMIC_CARD_LAYOUT_TITLE = "DYNAMIC_CARD_LAYOUT_TITLE";

    /** Preference key: whether this card is the component-focused specialization ({@link DynamicComponentCard}). */
    public static final String DYNAMIC_CARD_COMPONENT_FOCUSED = "DYNAMIC_CARD_COMPONENT_FOCUSED";

    static {
        // Register the (non-enum) dynamic-card window type so EntityKlWindowState.fromPreferences and
        // revert() can round-trip the persisted WINDOW_TYPE string. Idempotent.
        try {
            EntityKlWindowType.Registry.registerInstance(DYNAMIC_CARD_WINDOW_TYPE);
        } catch (RuntimeException alreadyRegistered) {
            // already registered (duplicate prefix) — fine
        }
    }

    private final UUID windowTopic;
    private UUID journalTopic;
    private EntityFacade entityFacade;
    private final String editorLayoutTitle;
    private final boolean componentFocused;
    private final DynamicCard card;

    /**
     * Creates a new dynamic-card window hosting a freshly created {@link DynamicCard} (or, when
     * {@code componentFocused}, a {@link DynamicComponentCard}) that realizes the named editor-designed
     * layout for the given reference component.
     *
     * @param journalTopic            the journal topic for event coordination
     * @param entityFacade            the reference component the layout is about, or {@code null}
     * @param editorLayoutTitle       the title of the editor-designed layout to realize
     * @param editorWindowPreferences the preferences node of that designed layout
     * @param componentFocused        {@code true} to host a component-focused {@link DynamicComponentCard}
     * @param viewProperties          the journal's view properties (the card overrides these)
     * @param preferences             this window's preferences node, or {@code null} to create one
     * @return the created window
     */
    public static DynamicCardKlWindow create(UUID journalTopic,
                                             EntityFacade entityFacade,
                                             String editorLayoutTitle,
                                             KometPreferences editorWindowPreferences,
                                             boolean componentFocused,
                                             ViewProperties viewProperties,
                                             KometPreferences preferences) {
        final KometPreferences windowPreferences = preferences != null
                ? preferences : getWindowPreferences(journalTopic, DYNAMIC_CARD_WINDOW_TYPE);
        // The card discovers its container's coordinate from the scene graph at bind; it isn't handed a
        // view. Its content (component + designed layout) is persisted in its own node under this window.
        final KlPreferencesFactory cardPreferencesFactory =
                KlPreferencesFactory.create(windowPreferences, DynamicCard.class);
        final DynamicCard card = componentFocused
                ? DynamicComponentCard.create(cardPreferencesFactory, editorWindowPreferences, entityFacade, journalTopic)
                : DynamicCard.create(cardPreferencesFactory, editorWindowPreferences, entityFacade, journalTopic);
        return new DynamicCardKlWindow(journalTopic, entityFacade, editorLayoutTitle, componentFocused,
                card, viewProperties, windowPreferences);
    }

    /**
     * Wraps an already-built {@link DynamicCard} (freshly created via {@link #create} or restored via
     * {@link #restore}) in the chapter-window adapter, wiring the close + component-focus callbacks.
     *
     * @param journalTopic      the journal topic for event coordination
     * @param entityFacade      the card's current reference component, or {@code null}
     * @param editorLayoutTitle the title of the realized layout (for logging / window-state backstop)
     * @param componentFocused  {@code true} when the card is a {@link DynamicComponentCard}
     * @param card              the card to host
     * @param viewProperties    the journal's view properties
     * @param windowPreferences this window's already-resolved preferences node
     */
    private DynamicCardKlWindow(UUID journalTopic,
                                EntityFacade entityFacade,
                                String editorLayoutTitle,
                                boolean componentFocused,
                                DynamicCard card,
                                ViewProperties viewProperties,
                                KometPreferences windowPreferences) {
        super(viewProperties, windowPreferences);
        this.journalTopic = journalTopic;
        this.entityFacade = entityFacade;
        this.editorLayoutTitle = editorLayoutTitle;
        this.componentFocused = componentFocused;

        final EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(this.preferences);
        this.windowTopic = windowState.getWindowId();
        setWindowState(windowState);

        this.card = card;
        // The chrome's close control delegates to the window's close action (wired by the journal's
        // setupWorkspaceWindow), so closing a card removes it from the workspace.
        this.card.setOnCloseRequest(() -> getOnClose().ifPresent(Runnable::run));
        // Re-focusing the card on a dropped component updates and persists this window's reference.
        this.card.setOnComponentFocused(component -> {
            this.entityFacade = component;
            save();
        });
        this.paneWindow = card.fxObject();

        LOG.info("Dynamic-card window {} ({}) realizing layout '{}'",
                windowTopic, componentFocused ? "component" : "plain", editorLayoutTitle);
    }

    /**
     * Restores a dynamic-card window by reconstructing its {@link DynamicCard} from the card's OWN
     * preferences node via the framework restore ({@code UNRESTORED → revert()}) — the card carries its
     * own component + designed layout. A window saved before the card persisted its content has an empty
     * card node, so a migration fallback seeds the card from the legacy window-state copy.
     *
     * <p>The card is handed the journal's <em>live</em> {@link ViewProperties} (the instance the journal's
     * View popup edits), so a restored card tracks journal-coordinate changes like a newly created one.
     *
     * @param preferences           the saved window preferences node
     * @param journalViewProperties the journal's live view properties (the card overrides these)
     * @return the restored window
     */
    public static DynamicCardKlWindow restore(KometPreferences preferences, ViewProperties journalViewProperties) {
        final EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);
        final UUID windowTopic = windowState.getWindowId();
        final UUID journalTopic = preferences.getUuid(JOURNAL_TOPIC)
                .orElseThrow(() -> new IllegalStateException("No journal topic for dynamic-card window " + windowTopic));
        final boolean componentFocused = preferences.get(DYNAMIC_CARD_COMPONENT_FOCUSED)
                .map(Boolean::parseBoolean).orElse(false);

        // Reconstruct the card from its OWN node (named for DynamicCard — deterministic) via the framework
        // restore, then revert() to load its persisted content (no-clobber). Address the node directly:
        // the factory supplier would allocate a fresh sequential node instead of reusing the saved one.
        final KometPreferences cardNode = preferences.node(DynamicCard.class.getSimpleName());
        final DynamicCard card = componentFocused
                ? DynamicComponentCard.restore(cardNode) : DynamicCard.restore(cardNode);
        card.revert();

        // Migration fallback: a window saved before the card persisted its own content has an empty card
        // node, so revert() loaded no layout — seed the card from the legacy window-state copy.
        if (!card.isContentRestored()) {
            final int entityNid = windowState.getEntityNid();
            card.setReferenceComponent(entityNid != 0 ? Entity.getFast(entityNid) : null);
            preferences.get(DYNAMIC_CARD_LAYOUT_TITLE).ifPresent(title ->
                    card.setEditorWindowPreferences(KometPreferencesImpl.getConfigurationRootPreferences()
                            .node(KL_EDITOR_APP).node(title)));
            card.setJournalTopic(journalTopic);
        }

        final EntityFacade entityFacade = card.getReferenceComponent();
        final String editorLayoutTitle = preferences.get(DYNAMIC_CARD_LAYOUT_TITLE).orElse(null);

        final DynamicCardKlWindow window = new DynamicCardKlWindow(journalTopic, entityFacade, editorLayoutTitle,
                componentFocused, card, journalViewProperties, preferences);
        window.revert();   // restore the window geometry (AbstractChapterKlWindow state)
        LOG.info("Restored dynamic-card window {} from the card's own node", windowTopic);
        return window;
    }

    @Override
    public UUID getWindowTopic() {
        return windowTopic;
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return DYNAMIC_CARD_WINDOW_TYPE;
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
        // window's preferences are removed — the explicit cleanup kview's chapter windows lack.
        card.knowledgeLayoutUnbind();
        super.delete();
    }

    // ---- Persistence: window geometry + reference component + the realized layout title ----------

    @Override
    public void save() {
        super.save();   // base: WINDOW_ID, WINDOW_TYPE, position, size, plus captureAdditionalState below
        // Persist the card's content (component + designed layout) to its OWN node via the framework
        // save path (subAreaSave). The window-state copy below stays for now as a backstop until the
        // restore path is switched to read the card's node (next 2a increment).
        card.save();
        if (preferences != null) {
            if (editorLayoutTitle != null) {
                preferences.put(DYNAMIC_CARD_LAYOUT_TITLE, editorLayoutTitle);
            }
            preferences.put(DYNAMIC_CARD_COMPONENT_FOCUSED, Boolean.toString(componentFocused));
            try {
                preferences.flush();
            } catch (Exception e) {
                LOG.error("Failed to persist layout title for dynamic-card window {}", windowTopic, e);
            }
        }
    }

    @Override
    protected void captureAdditionalState(EntityKlWindowState state) {
        if (entityFacade != null) {
            state.setEntityNid(entityFacade.nid());
            if (entityFacade.publicId() != null) {
                UUID[] uuids = entityFacade.publicId().asUuidArray();
                if (uuids.length > 0) {
                    state.setEntityUuid(uuids[0]);
                }
            }
        }
        if (journalTopic != null) {
            state.addProperty(JOURNAL_TOPIC.name(), journalTopic.toString());
        }
        if (editorLayoutTitle != null) {
            state.addProperty(DYNAMIC_CARD_LAYOUT_TITLE, editorLayoutTitle);
        }
    }

    @Override
    protected void applyAdditionalState(EntityKlWindowState state) {
        final String journalTopicString = state.getStringProperty(JOURNAL_TOPIC.name(), null);
        if (journalTopicString != null) {
            this.journalTopic = UUID.fromString(journalTopicString);
        }
    }

    // ---- AbstractChapterKlWindow contract (no property panel for dynamic-card windows) -----------

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
