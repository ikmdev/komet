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
package dev.ikm.komet.amplify.details;

import dev.ikm.komet.amplify.properties.PropertiesController;
import dev.ikm.komet.amplify.timeline.TimelineController;
import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.TopPanelFactory;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.view.ViewProperties;
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
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static dev.ikm.komet.amplify.commons.CssHelper.defaultStyleSheet;
import static dev.ikm.komet.framework.activity.ActivityStreamOption.PUBLISH;
import static dev.ikm.komet.framework.activity.ActivityStreamOption.SYNCHRONIZE;

public class DetailsNode extends ExplorationNodeAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(DetailsNode.class);

    protected final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    protected FlowSubscriber<Integer> invalidationSubscriber;
    protected ChangeListener<EntityFacade> entityFocusChangeListener;
    protected static final String CONCEPT_DETAILS_VIEW_FXML_FILE = "amplify-details.fxml";

    protected static final String STYLE_ID = "amplify-details-node";
    protected static final String TITLE = "Amplify Details";
    private BorderPane detailsViewBorderPane;
    private DetailsController detailsViewController;

    /////// Properties slide out //////////////////////////////
    protected static final String CONCEPT_PROPERTIES_VIEW_FXML_FILE = "amplify-properties.fxml";
    private BorderPane propertiesViewBorderPane;
    private PropertiesController propertiesViewController;

    ////// Timeline (Time travel) control //////////////////////
    protected static final String CONCEPT_TIMELINE_VIEW_FXML_FILE = "amplify-timeline.fxml";
    private BorderPane timelineViewBorderPane;
    private TimelineController timelineViewController;


    public DetailsNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        this(viewProperties, nodePreferences, false);
    }
    public DetailsNode(ViewProperties viewProperties, KometPreferences nodePreferences, boolean displayOnJournalView) {
        super(viewProperties, nodePreferences);
        init(displayOnJournalView);
        registerListeners(viewProperties);
        revertPreferences();
    }

    /**
     * Initialization view panel(fxml) and it's controller.
     */
    private void init(boolean displayOnJournalView) {
        try {
            // create a unique topic for each concept detail instance
            UUID conceptTopic = UUID.randomUUID();

            // Load Concept Details View Panel (FXML & Controller)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CONCEPT_DETAILS_VIEW_FXML_FILE));
            loader.setController(new DetailsController(conceptTopic));
            this.detailsViewBorderPane = loader.load();
            this.detailsViewController = loader.getController();

            // Programmatically change CSS Theme
            this.detailsViewBorderPane.getStylesheets().clear();
            String styleSheet = defaultStyleSheet();
            this.detailsViewBorderPane.getStylesheets().add(styleSheet);


            if (!displayOnJournalView) {
                // Add the menu drop down for coordinates & activity stream options with Blue Title of concept
                Node topPanel = TopPanelFactory.make(
                        viewProperties,
                        entityFocusProperty,
                        activityStreamKeyProperty,
                        optionForActivityStreamKeyProperty,
                        false);
                this.detailsViewBorderPane.setTop(topPanel);
            }

            // Load Concept Properties View Panel (FXML & Controller)
            FXMLLoader propsFXMLLoader = new FXMLLoader(PropertiesController.class.getResource(CONCEPT_PROPERTIES_VIEW_FXML_FILE));
            propsFXMLLoader.setController(new PropertiesController(conceptTopic));
            this.propertiesViewBorderPane = propsFXMLLoader.load();
            this.propertiesViewController = propsFXMLLoader.getController();
            // style the same as the details view
            this.propertiesViewBorderPane.getStylesheets().add(styleSheet);

            // setup view and controller into details controller
            detailsViewController.attachPropertiesViewSlideoutTray(this.propertiesViewBorderPane);

            // Load Timeline View Panel (FXML & Controller)
            FXMLLoader timelineFXMLLoader = new FXMLLoader(TimelineController.class.getResource(CONCEPT_TIMELINE_VIEW_FXML_FILE));
            this.timelineViewBorderPane = timelineFXMLLoader.load();
            this.timelineViewController = timelineFXMLLoader.getController();

            // This will highlight with green around the pane when the user selects a date point in the timeline.
            timelineViewController.onDatePointSelected((changeCoordinate) ->{
                propertiesViewController.getHistoryChangeController().highlightListItemByChangeCoordinate(changeCoordinate);
            });
            // When Date points are in range (range slider)
            timelineViewController.onDatePointInRange((rangeToggleOn, changeCoordinates) -> {
                if (rangeToggleOn) {
                    propertiesViewController.getHistoryChangeController().filterByRange(changeCoordinates);
                    propertiesViewController.getHierarchyController().diffNavigationGraph(changeCoordinates);
                } else {
                    propertiesViewController.getHistoryChangeController().unfilterByRange();
                    propertiesViewController.getHierarchyController().diffNavigationGraph(Set.of());
                }
            });

            // style the same as the details view
            this.timelineViewBorderPane.getStylesheets().add(styleSheet);

            // setup view and controller into details controller
            detailsViewController.attachTimelineViewSlideoutTray(this.timelineViewBorderPane);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wireup listeners(handler code) that will respond on change. E.g. The entityFocusProperty changes when a user selects a concept (in a Navigator tree view).
     * @param viewProperties
     */
    private void registerListeners(ViewProperties viewProperties) {
        // remove later when closing
        this.entityFocusChangeListener = (observable, oldEntityFacade, newEntityFacade) -> {
            if (newEntityFacade != null) {
                titleProperty.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(newEntityFacade));
                toolTipTextProperty.set(viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(newEntityFacade));

                // Also publish(dispatch) to any subscribers of this view.
                if (PUBLISH.keyForOption().equals(super.optionForActivityStreamKeyProperty.get()) ||
                        SYNCHRONIZE.keyForOption().equals(super.optionForActivityStreamKeyProperty.get())) {
                    getActivityStream().dispatch(newEntityFacade);
                }

                // Populate Detail View
                if (getDetailsViewController() != null) {
                    getDetailsViewController().updateModel(viewProperties, newEntityFacade);
                    getDetailsViewController().updateView();
                }

                // Populate Properties View
                if (getPropertiesViewController() != null) {
                    getPropertiesViewController().updateModel(viewProperties, newEntityFacade);
                    getPropertiesViewController().updateView();
                }

                // Populate Timeline View
                if (getTimelineViewController() != null) {
                    getTimelineViewController().resetConfigPathAndModules();
                    getTimelineViewController().updateModel(viewProperties, newEntityFacade);
                    getTimelineViewController().updateView();
                }

            } else {
                // Show a blank view (nothing selected)
                titleProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                toolTipTextProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                getDetailsViewController().clearView();
                getPropertiesViewController().clearView();
            }
        };

        // When a new entity is selected populate the view. An entity has been selected upstream (activity stream)
        this.entityFocusProperty.addListener(this.entityFocusChangeListener);

        // If database updates the underlying entity, this will do a force update of the UI.
        this.invalidationSubscriber = new FlowSubscriber<>(nid -> {
            if (entityFocusProperty.get() != null && entityFocusProperty.get().nid() == nid) {
                // component has changed, need to update.
                Platform.runLater(() -> entityFocusProperty.set(null));
                Platform.runLater(() -> entityFocusProperty.set(Entity.provider().getEntityFast(nid)));
            }
        });

        // Register to the Entity Service
        Entity.provider().addSubscriberWithWeakReference(this.invalidationSubscriber);
    }

    protected void revertDetailsPreferences() {

    }

    /**
     * Returns the associated controller to update the UI.
     * @return DetailsController The attached controller to the Details view (fxml)
     */
    public DetailsController getDetailsViewController() {
        return detailsViewController;
    }

    public PropertiesController getPropertiesViewController() {
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
        if (entities.isEmpty()) {
            entityFocusProperty.set(null);
        } else {
            EntityFacade entityFacade = entities.get(0);
            // Only display Concept Details.
            if (entityFacade instanceof ConceptFacade) {
                entityFocusProperty.set(entityFacade);
            } else {
                entityFocusProperty.set(null);
            }
        }
    }

    @Override
    public final void revertAdditionalPreferences() {
        if (nodePreferences.hasKey(DetailNodeKey.ENTITY_FOCUS)) {
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
        return this.detailsViewBorderPane;
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
    public Class<DetailsNodeFactory> factoryClass() {
        return DetailsNodeFactory.class;
    }
    public enum DetailNodeKey {
        ENTITY_FOCUS,
        REQUEST_FOCUS_ON_ACTIVITY
    }

}
