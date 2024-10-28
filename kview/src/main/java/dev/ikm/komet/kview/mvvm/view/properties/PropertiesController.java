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
package dev.ikm.komet.kview.mvvm.view.properties;


import static dev.ikm.komet.kview.fxutils.CssHelper.genText;
import static dev.ikm.komet.kview.mvvm.view.descriptionname.DescriptionNameController.ADD_FQN_TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.view.descriptionname.DescriptionNameController.ADD_OTHER_NAME_TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.view.descriptionname.DescriptionNameController.EDIT_FQN_TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.view.descriptionname.DescriptionNameController.EDIT_OTHER_NAME_TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.CONCEPT_STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CONCEPT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.DESCRIPTION_NAME_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.PARENT_PROCESS;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.PARENT_PUBLIC_ID;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.EDIT;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.AddFullyQualifiedNameEvent;
import dev.ikm.komet.kview.events.AddOtherNameToConceptEvent;
import dev.ikm.komet.kview.events.EditConceptFullyQualifiedNameEvent;
import dev.ikm.komet.kview.events.EditOtherNameConceptEvent;
import dev.ikm.komet.kview.events.OpenPropertiesPanelEvent;
import dev.ikm.komet.kview.events.ShowEditDescriptionPanelEvent;
import dev.ikm.komet.kview.mvvm.view.descriptionname.DescriptionNameController;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

/**
 * The properties window providing tabs of Edit, Hierarchy, History, and Comments.
 * This view is associated with the view file history-change-selection.fxml.
 */
public class PropertiesController implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);
    protected static final String HISTORY_CHANGE_FXML_FILE = "history-change-selection.fxml";
    protected static final String HIERARCHY_VIEW_FXML_FILE = "hierarchy-view.fxml";

    protected static final String EDIT_VIEW_FXML_FILE = "edit-view.fxml";

    protected static final String EDIT_DESCRIPTIONS_FXML_FILE = "edit-descriptions.fxml";

    private static final URL DESCRIPTION_FXML_URL = DescriptionNameController.class.getResource("description-name.fxml");

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

    private Pane currentEditPane;

    private Pane editDescriptionsPane;


    private EditConceptController editConceptController;


    private DescriptionNameController descriptionNameController;


    private EditDescriptionsController editDescriptionsController;

    private Pane commentsPane = new StackPane(genText("Comments Pane"));
    private ViewProperties viewProperties;
    private EntityFacade entityFacade;

    private EvtBus eventBus;

    private Subscriber<AddOtherNameToConceptEvent> addOtherNameSubscriber;

    private Subscriber<EditOtherNameConceptEvent> editOtherNameConceptEventSubscriber;

    private Subscriber<EditConceptFullyQualifiedNameEvent> editConceptFullyQualifiedNameEventSubscriber;

    private Subscriber<AddFullyQualifiedNameEvent> addFqnSubscriber;

    private Subscriber<ShowEditDescriptionPanelEvent> editDescriptionPaneSubscriber;

    private Subscriber<OpenPropertiesPanelEvent> propsPanelOpen;

    private UUID conceptTopic;

    @InjectViewModel
    private ConceptViewModel conceptViewModel;


    public PropertiesController() {
    }

    public PropertiesController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    /**
     * This is called after dependency injection has occurred to the JavaFX controls above.
     */
    @FXML
    public void initialize() throws IOException {
        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // Load History tabs View Panel (FXML & Controller)
        FXMLLoader loader = new FXMLLoader(getClass().getResource(HISTORY_CHANGE_FXML_FILE));
        historyTabsBorderPane = loader.load();
        historyChangeController = loader.getController();

        // Load Hierarchy tab View Panel (FXML & Controller)
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource(HIERARCHY_VIEW_FXML_FILE));
        hierarchyTabBorderPane = loader2.load();
        hierarchyController = loader2.getController();

        // Load Edit tab
        FXMLLoader loaderEdit = new FXMLLoader(getClass().getResource(EDIT_VIEW_FXML_FILE));
        loaderEdit.setController(new EditConceptController(conceptTopic));
        editBorderPane = loaderEdit.load();
        editConceptController = loaderEdit.getController();

        // Edit Descriptions panel inside the Edit tab (under Properties bump out on Concept window)
        FXMLLoader loaderEditDescriptions = new FXMLLoader(getClass().getResource(EDIT_DESCRIPTIONS_FXML_FILE));
        loaderEditDescriptions.setController(new EditDescriptionsController(conceptTopic));
        editDescriptionsPane = loaderEditDescriptions.load();
        editDescriptionsController = loaderEditDescriptions.getController();

        // when we receive an event because the user clicked the
        // Edit Descriptions Button
        // we then load the panel with the choice of buttons to edit
        editDescriptionPaneSubscriber = evt -> contentBorderPane.setCenter(editDescriptionsPane);
        eventBus.subscribe(conceptTopic, ShowEditDescriptionPanelEvent.class, editDescriptionPaneSubscriber);

        Config descrConfig = new Config(DESCRIPTION_FXML_URL);
        descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) ->
            descrNameViewModel
                    .setPropertyValue(VIEW_PROPERTIES, viewProperties)
                    .setPropertyValue(TOPIC, conceptTopic)
                    .setPropertyValue(PARENT_PROCESS, CONCEPT)
                    .setPropertyValue(STAMP_VIEW_MODEL, conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL))
        );


        // when we receive an event because the user clicked the
        // Add Other Name button from the Properties > Edit bump out, we want to change the Pane in the
        // Edit Concept bump out to be the Add Other Name form
        addOtherNameSubscriber = evt -> {
            descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                descrNameViewModel.setPropertyValue(MODE, CREATE)
                        .setPropertyValue(NAME_TYPE, REGULAR_NAME_DESCRIPTION_TYPE)
                        .setPropertyValue(TITLE_TEXT, ADD_OTHER_NAME_TITLE_TEXT)
                        .setPropertyValue(DESCRIPTION_NAME_TYPE, "Other Name")
                        .setPropertyValue(STAMP_VIEW_MODEL, conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL))
                ;
            });
            JFXNode<Pane, DescriptionNameController> descriptionNameControllerJFXNode = FXMLMvvmLoader.make(descrConfig);
            descriptionNameController = descriptionNameControllerJFXNode.controller();
            currentEditPane = descriptionNameControllerJFXNode.node();
            contentBorderPane.setCenter(currentEditPane);
            editButton.setSelected(true);
        };
        eventBus.subscribe(conceptTopic, AddOtherNameToConceptEvent.class, addOtherNameSubscriber);



        // when we receive an event because the user clicked the
        // Add Axiom button, we want to change the Pane in the
        // Edit Concept bump out to be the Add Axiom form

        editOtherNameConceptEventSubscriber = evt -> {

            descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                descrNameViewModel.setPropertyValue(MODE, CREATE) // still creating, pattern not created yet
                        .setPropertyValue(NAME_TYPE, REGULAR_NAME_DESCRIPTION_TYPE)
                        .setPropertyValue(TITLE_TEXT, EDIT_OTHER_NAME_TITLE_TEXT)
                        .setPropertyValue(DESCRIPTION_NAME_TYPE, "Other Name")
                        .setPropertyValue(PARENT_PUBLIC_ID, evt.getPublicId())
                        .setPropertyValue(STAMP_VIEW_MODEL, conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL))
                ;
            });
            JFXNode<Pane, DescriptionNameController> descriptionNameControllerJFXNode = FXMLMvvmLoader.make(descrConfig);
            descriptionNameController = descriptionNameControllerJFXNode.controller();
            currentEditPane = descriptionNameControllerJFXNode.node();
            contentBorderPane.setCenter(currentEditPane);
            if (evt.getPublicId() != null) {
                descriptionNameController.setConceptAndPopulateForm(evt.getPublicId());
            }else {
                descriptionNameController.setConceptAndPopulateForm(evt.getDescrName());
            }
            editButton.setSelected(true);
        };
        eventBus.subscribe(conceptTopic, EditOtherNameConceptEvent.class, editOtherNameConceptEventSubscriber);

        // when we receive an event because the user clicked the
        // Fully Qualified Name in the Concept, we want to change the Pane in the
        // Edit Concept bump out to be the Edit Fully Qualified Name form
        editConceptFullyQualifiedNameEventSubscriber = evt -> {
            descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                descrNameViewModel.setPropertyValue(MODE, EDIT)
                        .setPropertyValue(NAME_TYPE, FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                        .setPropertyValue(TITLE_TEXT, EDIT_FQN_TITLE_TEXT)
                        .setPropertyValue(DESCRIPTION_NAME_TYPE, "Fully Qualified Name")
                        .setPropertyValue(PARENT_PUBLIC_ID, evt.getPublicId())
                        .setPropertyValue(STAMP_VIEW_MODEL, conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL))
                ;
            });
            JFXNode<Pane, DescriptionNameController> descriptionNameControllerJFXNode = FXMLMvvmLoader.make(descrConfig);
            descriptionNameController = descriptionNameControllerJFXNode.controller();
            currentEditPane = descriptionNameControllerJFXNode.node();
            contentBorderPane.setCenter(currentEditPane);

            if (evt.getPublicId() != null) {
                descriptionNameController.setConceptAndPopulateForm(evt.getPublicId());
            }else {
                descriptionNameController.setConceptAndPopulateForm(evt.getDescrName());
            }
            editButton.setSelected(true);
        };
        eventBus.subscribe(conceptTopic, EditConceptFullyQualifiedNameEvent.class, editConceptFullyQualifiedNameEventSubscriber);

        // this event happens on during the creation of a new concept
        // a new concept will not have a fully qualified name and will need one
        addFqnSubscriber = evt -> {
            descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                descrNameViewModel.setPropertyValue(MODE, CREATE)
                        .setPropertyValue(NAME_TYPE, FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                        .setPropertyValue(TITLE_TEXT, ADD_FQN_TITLE_TEXT)
                        .setPropertyValue(DESCRIPTION_NAME_TYPE, "Fully Qualified Name")
                        .setPropertyValue(STAMP_VIEW_MODEL, conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL))
                ;
            });
            JFXNode<Pane, DescriptionNameController> descriptionNameControllerJFXNode = FXMLMvvmLoader.make(descrConfig);
            descriptionNameController = descriptionNameControllerJFXNode.controller();
            currentEditPane = descriptionNameControllerJFXNode.node();
            contentBorderPane.setCenter(currentEditPane);
            editButton.setSelected(true);
        };
        eventBus.subscribe(conceptTopic, AddFullyQualifiedNameEvent.class, addFqnSubscriber);

        // when opening the properties panel the default toggle to view is the history tab
        propsPanelOpen = evt -> {
            historyButton.setSelected(true);
            contentBorderPane.setCenter(historyTabsBorderPane);
        };
        eventBus.subscribe(conceptTopic, OpenPropertiesPanelEvent.class, propsPanelOpen);

    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setConceptTopic(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade){
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
        this.historyChangeController.updateModel(viewProperties, entityFacade);
        this.hierarchyController.updateModel(viewProperties, entityFacade);

        this.editDescriptionsController.updateModel(viewProperties);
    }

    public void updateView() {
        this.historyChangeController.updateView();
        this.hierarchyController.updateView();
    }

    @FXML
    private void showEditView(ActionEvent event) {
        event.consume();
        this.editButton.setSelected(true);
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
