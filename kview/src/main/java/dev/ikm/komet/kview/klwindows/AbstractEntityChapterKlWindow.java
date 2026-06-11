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

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.PROPERTY_PANEL_OPEN;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.SELECTED_PROPERTY_PANEL;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getWindowPreferences;

/**
 * An abstract implementation of {@link AbstractChapterKlWindow} specifically designed for windows
 * that display or edit knowledge management entities in the Komet system.
 * <p>This class extends the basic window functionality to provide entity-specific capabilities,
 * including:
 * <ul>
 *   <li>Management of the entity being displayed or edited through {@link EntityFacade}</li>
 *   <li>Connection to a journal topic for event communication between windows</li>
 *   <li>Type-specific handling of different knowledge entities (concepts, patterns, etc.)</li>
 *   <li>Persistence of entity references in window state</li>
 * </ul>
 * <p>Entity chapter windows serve as specialized containers for different types of knowledge entities
 * within the system and provide the foundation for type-specific windows like concept editors,
 * pattern browsers, or LIDR viewers.
 *
 * @see AbstractChapterKlWindow
 * @see EntityFacade
 */
public abstract class AbstractEntityChapterKlWindow extends AbstractChapterKlWindow<Pane> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEntityChapterKlWindow.class);
    /**
     * The UUID uniquely identifying this window within the application.
     * This ID is used for tracking the window, persisting its state, and
     * addressing it in the event system.
     */
    private final UUID windowTopic;

    /**
     * The UUID identifying the journal topic that owns this window.
     * <p>     * The journal topic is used by the owning Journal Window to communicate events
     * related to this window, allowing for coordination between multiple windows
     * that may be displaying related content.
     */
    private UUID journalTopic;

    /**
     * The entity being displayed or edited in this window.
     * <p>     * The entity facade provides access to the underlying knowledge entity (concept,
     * pattern, semantic, etc.) that this window is responsible for presenting or
     * allowing the user to modify.
     */
    private EntityFacade entityFacade;

    /**
     * Constructs a new entity-focused chapter window with references to the journal topic,
     * entity facade, view properties, and user preferences.
     * <p>     * This constructor initializes the window with its core components and either creates
     * a new window state or loads an existing one from preferences. If preferences are not
     * provided, a new UUID is generated for the window and appropriate preferences are created.
     *
     * @param journalTopic The UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade The entity facade representing the knowledge entity to display or edit.
     * @param viewProperties The view properties providing access to view calculators for querying data.
     * @param preferences The komet preferences for storing and retrieving user configuration.
     *                   If null, new preferences will be created based on the journalTopic and window type.
     */
    public AbstractEntityChapterKlWindow(UUID journalTopic,
                                         EntityFacade entityFacade,
                                         ViewProperties viewProperties,
                                         KometPreferences preferences) {
        // Authority-first coordinate unification (ike-issues#660): the window owns a single coordinate
        // of record, seeded from the journal-provided coordinate. getViewProperties() returns a
        // ViewProperties *derived* from that source view (a legacy shim for the FXML body); subclasses
        // call establishViewContext() once their paneWindow exists to wrap the same source view as the
        // KL ViewContext for KL areas.
        super(deriveSourceCoordinate(viewProperties), preferences);
        this.journalTopic = Objects.requireNonNull(journalTopic, "journalTopic cannot be null");
        this.entityFacade = entityFacade;

        if (preferences == null) {
            // Use UUID-based window directory name for the preferences path
            this.preferences = getWindowPreferences(journalTopic, getWindowType());
        }

        final EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(this.preferences);
        windowTopic = windowState.getWindowId();
        setWindowState(windowState);
    }

    /**
     * Seeds an independent per-window coordinate source from the journal-provided coordinate and
     * returns a {@link ViewProperties} derived from it. The returned view is what the FXML body reads;
     * its {@code parentView()} is the source view {@link #establishViewContext()} wraps, so the View
     * menu, the derived view, and KL areas all read one coordinate (ike-issues#660).
     *
     * @param journalView the coordinate handed in by the journal when opening the window
     * @return a {@code ViewProperties} derived from the window-owned source view
     */
    protected static ViewProperties deriveSourceCoordinate(ViewProperties journalView) {
        ObservableViewNoOverride sourceView =
                new ObservableViewNoOverride(journalView.parentView().toViewCoordinateRecord(), "Chapter view");
        return sourceView.makeOverridableViewProperties("Chapter view");
    }

    /**
     * Establishes the window's coordinate as a KL {@link KlContext} on its root pane, wrapping the same
     * source view the derived {@link #getViewProperties()} is built over. KL areas placed in this window
     * then resolve it via {@code viewForContext()} and re-render on {@code contextChanged()}. Subclasses
     * call this once {@code paneWindow} is assigned (ike-issues#660).
     */
    protected void establishViewContext() {
        KlContext.overView(this, getViewProperties().parentView(), getClass().getSimpleName());
    }

    @Override
    public UUID getWindowTopic() {
        return windowTopic;
    }

    /**
     * Returns the entity currently being displayed or edited in this window.
     *
     * @return The current {@link EntityFacade} being displayed or edited, or null if none is set.
     */
    public EntityFacade getEntityFacade() {
        return entityFacade;
    }

    /**
     * Updates the entity being displayed or edited in this window.
     *
     * @param entityFacade The new {@link EntityFacade} to display or edit.
     */
    protected void setEntityFacade(EntityFacade entityFacade) {
        this.entityFacade = entityFacade;
    }

    /**
     * Returns the journal topic UUID associated with this window.
     *
     * @return The UUID for the journal topic used by the owning Journal Window.
     */
    public UUID getJournalTopic() {
        return journalTopic;
    }

    /**
     * Updates the journal topic UUID associated with this window.
     *
     * @param journalTopic The updated UUID representing the journal topic.
     */
    protected void setJournalTopic(UUID journalTopic) {
        this.journalTopic = journalTopic;
    }

    @Override
    protected void captureAdditionalState(EntityKlWindowState state) {
        // Save entity information if available
        if (entityFacade != null) {
            state.setEntityNid(entityFacade.nid());
            if (entityFacade.publicId() != null) {
                UUID[] uuids = entityFacade.publicId().asUuidArray();
                if (uuids.length > 0) {
                    state.setEntityUuid(uuids[0]);
                }
            }
        }

        // Save journal topic
        if (journalTopic != null) {
            state.addProperty(JOURNAL_TOPIC.name(), journalTopic.toString());
        }
    }

    @Override
    protected void applyAdditionalState(EntityKlWindowState state) {
        // Restore journal topic
        String journalTopicString = state.getStringProperty(JOURNAL_TOPIC.name(), null);
        if (journalTopicString != null) {
            this.journalTopic = UUID.fromString(journalTopicString);
        }

        // Restore properties panel state
        setPropertyPanelOpen(state.getBooleanProperty(PROPERTY_PANEL_OPEN, false));

        setSelectedPropertyPanel(state.getStringProperty(SELECTED_PROPERTY_PANEL, "EDIT"));
    }

    @Override
    public void onShown() {
        // Default implementation does nothing
    }
}