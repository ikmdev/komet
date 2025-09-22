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
package dev.ikm.komet.kview.mvvm.view.concept;

import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.view.timeline.TimelineController;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.flow.FlowSubscriber;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ConceptNode extends ExplorationNodeAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptNode.class);

    protected final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    protected FlowSubscriber<Integer> invalidationSubscriber;
    protected ChangeListener<EntityFacade> entityFocusChangeListener;

    protected static final String STYLE_ID = "kview-details-node";
    protected static final String TITLE = "Concept Details";

    /////// Properties slide out //////////////////////////////

    private ConceptPropertiesController propertiesViewController;

    ////// Timeline (Time travel) control //////////////////////
    protected static final String CONCEPT_TIMELINE_VIEW_FXML_FILE = "timeline.fxml";
    private BorderPane timelineViewBorderPane;
    private TimelineController timelineViewController;


    public ConceptNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        this(viewProperties, nodePreferences, false);
    }
    public ConceptNode(ViewProperties viewProperties, KometPreferences nodePreferences, boolean displayOnJournalView) {
        super(viewProperties, nodePreferences);
        init(displayOnJournalView);
        //registerListeners(viewProperties);
        revertPreferences();
    }

    /**
     * Initialization view panel(fxml) and it's view.
     */
    private void init(boolean displayOnJournalView) {
        // Let's grab what's inside the properties. Should be the journal window's event topic.
        // This details concept window can message events to the current journal window. E.g. progress popup window.
        UUID journalWindowTopic = nodePreferences().getUuid(PreferenceKey.CURRENT_JOURNAL_WINDOW_TOPIC).get();
    }



    protected void revertDetailsPreferences() {

    }

    public ConceptPropertiesController getPropertiesViewController() {
        return propertiesViewController;
    }

    public TimelineController getTimelineViewController() {
        return timelineViewController;
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        LOG.info("handle activiy called ....");
        if (entities.isEmpty()) {
            entityFocusProperty.set(null);
        } else {
            EntityFacade entityFacade = entities.get(0);
            // Only display Concept Details.
            if (entityFacade instanceof ConceptFacade) {
                LOG.info( "GOT OUR FACADE THROUGH handle ACTIVITY ??");
                entityFocusProperty.set(entityFacade);
            } else {
                entityFocusProperty.set(null);
            }
        }
    }

    @Override
    public final void revertAdditionalPreferences() {
        if (nodePreferences.hasKey(DetailNodeKey.ENTITY_FOCUS)) {
            LOG.info("reverting our entity back through OLD preference system");
            nodePreferences.getEntity(DetailNodeKey.ENTITY_FOCUS).ifPresentOrElse(entityFacade -> entityFocusProperty.set(entityFacade),
                    () -> entityFocusProperty.set(null));
        }
        revertDetailsPreferences();
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {
        LOG.info("Save current entity through old preference system");
        if (entityFocusProperty.get() != null) {
            nodePreferences.putEntity(DetailNodeKey.ENTITY_FOCUS, entityFocusProperty.get());
        } else {
            nodePreferences.remove(DetailNodeKey.ENTITY_FOCUS);
        }
        nodePreferences.putBoolean(DetailNodeKey.REQUEST_FOCUS_ON_ACTIVITY, false);
        saveDetailsPreferences();
    }

    protected void saveDetailsPreferences() {

    }


    @Override
    public Node getNode() {
        return new Pane();
    }

    @Override
    public void close() {
        if (entityFocusProperty.isNotNull().get()) {
            LOG.info("Closing DetailsNode Concept nid: " + this.entityFocusProperty.get().nid());
        }
        this.entityFocusProperty.removeListener(this.entityFocusChangeListener);
        Entity.provider().removeSubscriber(this.invalidationSubscriber);
    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class<ConceptNodeFactory> factoryClass() {
        return ConceptNodeFactory.class;
    }
    public enum DetailNodeKey {
        ENTITY_FOCUS,
        REQUEST_FOCUS_ON_ACTIVITY
    }

}
