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
package dev.ikm.komet.amplify.properties;

import dev.ikm.komet.amplify.events.AddDescriptionToConceptEvent;
import dev.ikm.komet.amplify.events.EditDescriptionConceptEvent;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

import static dev.ikm.komet.amplify.commons.CssHelper.defaultStyleSheet;
import static dev.ikm.komet.amplify.commons.CssHelper.genText;
import static dev.ikm.komet.amplify.events.AmplifyTopics.CONCEPT_TOPIC;

/**
 * The properties window providing tabs of Edit, Hierarchy, History, and Comments.
 * This controller is associated with the view file history-change-selection.fxml.
 */
public class PropertiesController implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);
    protected static final String HISTORY_CHANGE_FXML_FILE = "history-change-selection.fxml";
    protected static final String HIERARCHY_VIEW_FXML_FILE = "hierarchy-view.fxml";

    protected static final String EDIT_VIEW_FXML_FILE = "edit-view.fxml";

    protected static final String EDIT_ADD_DESCRIPTION_FXML_FILE = "add-description.fxml";

    protected static final String ADD_AXIOM_FXML_FILE = "edit-description.fxml";

    @FXML
    private SVGPath commentsButton;

    @FXML
    private ToggleButton editButton;

    @FXML
    private ToggleButton historyButton;

    @FXML
    private ToggleButton hierarchyButton;

    @FXML
    private ToggleGroup propertyToggleButtonGroup;

    @FXML
    private BorderPane contentBorderPane;

    private Pane historyTabsBorderPane;
    private HistoryChangeController historyChangeController;

    private Pane hierarchyTabBorderPane;
    private HierarchyController hierarchyController;

    private Pane editBorderPane;

    private Pane addDescriptionPane;

    private Pane addAxiomPane;
    private EditConceptController editConceptController;

    private AddDescriptionController addDescriptionController;

    private EditDescriptionController editDescriptionController;

    private Pane commentsPane = new StackPane(genText("Comments Pane"));
    private ViewProperties viewProperties;
    private EntityFacade entityFacade;

    private EvtBus eventBus;

    private Subscriber<AddDescriptionToConceptEvent> descriptionSubscriber;

    private Subscriber<EditDescriptionConceptEvent> axiomSubscriber;

    /**
     * This is called after dependency injection has occurred to the JavaFX controls above.
     */
    @FXML
    public void initialize() throws IOException {
        clearView();

        eventBus = EvtBusFactory.getInstance(EvtBus.class);

        // Programmatically change CSS Theme
        String styleSheet = defaultStyleSheet();

        // Load History tabs View Panel (FXML & Controller)
        FXMLLoader loader = new FXMLLoader(getClass().getResource(HISTORY_CHANGE_FXML_FILE));
        this.historyTabsBorderPane = loader.load();
        this.historyTabsBorderPane.getStylesheets().add(styleSheet);
        this.historyChangeController = loader.getController();

        // Load Hierarchy tab View Panel (FXML & Controller)
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource(HIERARCHY_VIEW_FXML_FILE));
        this.hierarchyTabBorderPane = loader2.load();
        this.hierarchyTabBorderPane.getStylesheets().add(styleSheet);
        this.hierarchyController = loader2.getController();

        // Load Edit tab
        FXMLLoader loaderEdit = new FXMLLoader(getClass().getResource(EDIT_VIEW_FXML_FILE));
        this.editBorderPane = loaderEdit.load();
        this.editBorderPane.getStylesheets().add(styleSheet);
        this.editConceptController = loaderEdit.getController();

        FXMLLoader loaderEditDescription = new FXMLLoader(getClass().getResource(EDIT_ADD_DESCRIPTION_FXML_FILE));
        this.addDescriptionPane = loaderEditDescription.load();
        this.addDescriptionPane.getStylesheets().add(styleSheet);
        this.addDescriptionController = loaderEditDescription.getController();

        FXMLLoader loaderEditAxiom= new FXMLLoader(getClass().getResource(ADD_AXIOM_FXML_FILE));
        this.addAxiomPane = loaderEditAxiom.load();
        this.addAxiomPane.getStylesheets().add(styleSheet);
        this.editDescriptionController = loaderEditAxiom.getController();

        // initially a default selected tab and view is shown
        updateDefaultSelectedViews();

        // when we receive an event because the user clicked the
        // Add Description button, we want to change the Pane in the
        // Edit Concept bump out to be the Add Description form
        descriptionSubscriber = evt -> contentBorderPane.setCenter(addDescriptionPane);
        eventBus.subscribe(CONCEPT_TOPIC, AddDescriptionToConceptEvent.class, descriptionSubscriber);

        // when we receive an event because the user clicked the
        // Add Axiom button, we want to change the Pane in the
        // Edit Concept bump out to be the Add Axiom form
        axiomSubscriber = evt -> contentBorderPane.setCenter(addAxiomPane);
        eventBus.subscribe(CONCEPT_TOPIC, EditDescriptionConceptEvent.class, axiomSubscriber);
    }

    private void updateDefaultSelectedViews() {
        // default to selected tab (History)
        Toggle tab = propertyToggleButtonGroup.getSelectedToggle();
        if (editButton.equals(tab)) {
            contentBorderPane.setCenter(editBorderPane);
        } else if (hierarchyButton.equals(tab)) {
            contentBorderPane.setCenter(hierarchyTabBorderPane);
        } else if (historyButton.equals(tab)) {
            contentBorderPane.setCenter(historyTabsBorderPane);
        } else if (commentsButton.equals(tab)) {
            contentBorderPane.setCenter(commentsPane);
        }
    }
    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade){
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
        this.historyChangeController.updateModel(viewProperties, entityFacade);
        this.hierarchyController.updateModel(viewProperties, entityFacade);
    }
    public void updateView() {
        this.historyChangeController.updateView();
        this.hierarchyController.updateView();
    }

    @FXML
    private void showEditView(ActionEvent event) {
        event.consume();
        LOG.info("Show Edit View " + event);
        contentBorderPane.setCenter(editBorderPane);
    }
    @FXML
    private void showNavigatorView(ActionEvent event) {
        event.consume();
        LOG.info("Show Navigator View " + event);
        contentBorderPane.setCenter(hierarchyTabBorderPane);
    }
    @FXML
    private void showHistoryView(ActionEvent event) {
        event.consume();
        LOG.info("Show History View " + event);
        contentBorderPane.setCenter(historyTabsBorderPane);
    }
    @FXML
    private void showCommentsView(ActionEvent event) {
        event.consume();
        LOG.info("Show Comments View " + event);
        contentBorderPane.setCenter(commentsPane);

    }

    public HistoryChangeController getHistoryChangeController() {
        return historyChangeController;
    }

    public HierarchyController getHierarchyController() {
        return hierarchyController;
    }

    public void clearView() {
    }
}
