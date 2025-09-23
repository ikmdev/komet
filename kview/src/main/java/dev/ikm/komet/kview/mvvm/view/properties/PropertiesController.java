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

import static dev.ikm.komet.kview.events.StampEvent.ADD_STAMP;
import static dev.ikm.komet.kview.events.StampEvent.CREATE_STAMP;
import static dev.ikm.komet.kview.fxutils.CssHelper.genText;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.PARENT_PUBLIC_ID;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.DESCRIPTION_LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.HAS_OTHER_NAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Type.CONCEPT;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.view.common.StampFormController;
import dev.ikm.komet.kview.events.AddFullyQualifiedNameEvent;
import dev.ikm.komet.kview.events.AddOtherNameToConceptEvent;
import dev.ikm.komet.kview.events.EditConceptFullyQualifiedNameEvent;
import dev.ikm.komet.kview.events.EditOtherNameConceptEvent;
import dev.ikm.komet.kview.events.OpenPropertiesPanelEvent;
import dev.ikm.komet.kview.events.ShowEditDescriptionPanelEvent;
import dev.ikm.komet.kview.events.StampEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampAddSubmitFormViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampCreateFormViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
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

    protected static final String ADD_OTHER_NAME_FXML_FILE = "add-other-name.fxml";

    protected static final String EDIT_OTHER_NAME_FXML_FILE = "edit-other-name-form.fxml";

    protected static final String EDIT_FQN_FXML_FILE = "edit-fully-qualified-name.fxml";

    protected static final String ADD_FQN_FXML_FILE = "add-fully-qualified-name.fxml";

    @FXML
    private ToggleButton commentsButton;

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

    @FXML
    private FlowPane propertiesTabsPane;

    private JFXNode<Pane, StampFormController> stampJFXNode;
    private Pane historyTabsBorderPane;
    private HistoryChangeController historyChangeController;

    private Pane hierarchyTabBorderPane;
    private HierarchyController hierarchyController;

    private Pane editBorderPane;

    private Pane addOtherNamePane;

    private Pane editFqnPane;

    private Pane addFqnPane;

    private Pane editDescriptionsPane;

    private Pane editOtherNamePane;

    private EditConceptController editConceptController;

    private AddOtherNameController addOtherNameController;

    private EditDescriptionFormController editDescriptionFormController;

    private EditDescriptionsController editDescriptionsController;

    private EditFullyQualifiedNameController editFullyQualifiedNameController;

    private AddFullyQualifiedNameController addFullyQualifiedNameController;

    private Pane commentsPane = new StackPane(genText("Comments Pane"));
    private ViewProperties viewProperties;
    private EntityFacade entityFacade;

    private PublicId fqnPublicId;

    private PublicId otherNamePublicId; // latest other name

    private EvtBus eventBus;

    private Subscriber<AddOtherNameToConceptEvent> addOtherNameSubscriber;

    private Subscriber<EditOtherNameConceptEvent> editOtherNameConceptEventSubscriber;

    private Subscriber<EditConceptFullyQualifiedNameEvent> editConceptFullyQualifiedNameEventSubscriber;

    private Subscriber<AddFullyQualifiedNameEvent> addFqnSubscriber;

    private Subscriber<ShowEditDescriptionPanelEvent> editDescriptionPaneSubscriber;

    private Subscriber<StampEvent> addStampSubscriber;

    private Subscriber<StampEvent> createStampSubscriber;

    private Subscriber<OpenPropertiesPanelEvent> propsPanelOpen;


    private UUID conceptTopic;

    private boolean hasOtherNames = false;

    private StampAddSubmitFormViewModel stampAddSubmitFormViewModel;

    private StampCreateFormViewModel stampCreateFormViewModel;

    private boolean editMode;

    public PropertiesController() {
    }

    public PropertiesController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
        this.stampAddSubmitFormViewModel = new StampAddSubmitFormViewModel(CONCEPT);
        this.stampCreateFormViewModel = new StampCreateFormViewModel(CONCEPT);
    }

    /**
     * This is called after dependency injection has occurred to the JavaFX controls above.
     */
    @FXML
    public void initialize() throws IOException {
        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // Load Stamp add View Panel (FXML & Controller)
        Config stampConfig = new Config(StampFormController.class.getResource(StampFormController.STAMP_FORM_FXML_FILE));
        stampJFXNode = FXMLMvvmLoader.make(stampConfig);

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

        // NOTE: New way of using injected View Models inside of Controllers.
        JFXNode<Pane, AddOtherNameController> addOtherNameControllerNode = FXMLMvvmLoader.make(
                getClass().getResource(ADD_OTHER_NAME_FXML_FILE),
                new AddOtherNameController(conceptTopic));

        addOtherNamePane = addOtherNameControllerNode.node();
        addOtherNameController = addOtherNameControllerNode.controller();

        JFXNode<Pane, EditDescriptionFormController> editDescriptionFormControllerNode = FXMLMvvmLoader.make(
                getClass().getResource(EDIT_OTHER_NAME_FXML_FILE),
                new EditDescriptionFormController(conceptTopic));
        editOtherNamePane = editDescriptionFormControllerNode.node();
        editDescriptionFormController = editDescriptionFormControllerNode.controller();

        //TODO for future there will be an edit axiom form

        JFXNode<Pane, EditFullyQualifiedNameController> editFqnControllerNode = FXMLMvvmLoader.make(
                getClass().getResource(EDIT_FQN_FXML_FILE),
                new EditFullyQualifiedNameController(conceptTopic)
        );
        editFqnPane = editFqnControllerNode.node();
        editFullyQualifiedNameController = editFqnControllerNode.controller();

        // NOTE: New way of using injected View Models inside of Controllers.
        JFXNode<Pane, AddFullyQualifiedNameController> addFqnControllerNode = FXMLMvvmLoader.make(
                getClass().getResource(ADD_FQN_FXML_FILE),
                new AddFullyQualifiedNameController(conceptTopic));
        addFqnPane = addFqnControllerNode.node();
        addFullyQualifiedNameController = addFqnControllerNode.controller();

        // initially a default selected tab and view is shown
        updateDefaultSelectedViews();

        // when we receive an event because the user clicked the
        // Edit Descriptions Button
        // we then load the panel with the choice of buttons to edit
        editDescriptionPaneSubscriber = evt -> contentBorderPane.setCenter(editDescriptionsPane);
        eventBus.subscribe(conceptTopic, ShowEditDescriptionPanelEvent.class, editDescriptionPaneSubscriber);


        // when we receive an event because the user clicked the
        // Add Other Name button from the Properties > Edit bump out, we want to change the Pane in the
        // Edit Concept bump out to be the Add Other Name form
        addOtherNameSubscriber = evt -> {
            contentBorderPane.setCenter(addOtherNamePane);
            editButton.setSelected(true);
            editButton.setText("ADD");

            // Create a new View Model for this form
            addOtherNameController.updateModel(getViewProperties(), (viewModel, controller) -> {
                // Clear view model. Please see addOtherNameController's initialize() method.
                viewModel.setValue(NAME_TEXT, "")
                        .setValue(CASE_SIGNIFICANCE, null)
                        .setValue(MODULE, null)
                        .setValue(LANGUAGE, null)
                        .setValue(STATUS, TinkarTerm.ACTIVE_STATE)
                        .setValue(IS_SUBMITTED, false)

                        .setValue(DESCRIPTION_CASE_SIGNIFICANCE, addFullyQualifiedNameController.getViewModel().getValue(CASE_SIGNIFICANCE))
                        .setValue(DESCRIPTION_LANGUAGE, addFullyQualifiedNameController.getViewModel().getValue(LANGUAGE))

                        .setValue(HAS_OTHER_NAME, hasOtherNames);

                // if in edit mode and navigating from the properties > edit > add other name
                // then the public id will be set
                if (evt.getPublicId() != null) {
                    viewModel.setValue(PARENT_PUBLIC_ID, evt.getPublicId());
                }
                viewModel.reset(); // copy model values into property values
                // update the UI form
                controller.clearView();
                controller.updateView();
            });
        };
        eventBus.subscribe(conceptTopic, AddOtherNameToConceptEvent.class, addOtherNameSubscriber);

        // when we receive an event because the user clicked the
        // Add Axiom button, we want to change the Pane in the
        // Edit Concept bump out to be the Add Axiom form

        editOtherNameConceptEventSubscriber = evt -> {
            contentBorderPane.setCenter(editOtherNamePane);
            editButton.setSelected(true);
            editButton.setText("EDIT");
            if (evt.getPublicId() != null) {
                editDescriptionFormController.setConceptAndPopulateForm(evt.getPublicId());
            }else {
                editDescriptionFormController.setConceptAndPopulateForm(evt.getDescrName());
            }
        };
        eventBus.subscribe(conceptTopic, EditOtherNameConceptEvent.class, editOtherNameConceptEventSubscriber);


        // when we receive an event because the user clicked the
        // Fully Qualified Name in the Concept, we want to change the Pane in the
        // Edit Concept bump out to be the Edit Fully Qualified Name form
        editConceptFullyQualifiedNameEventSubscriber = evt -> {
            // don't go into edit mode if there is no FQN yet
            if (evt.getPublicId() == null && evt.getDescrName() == null) {
                // default to adding an FQN is there isn't one
                eventBus.publish(conceptTopic, new AddFullyQualifiedNameEvent(evt,
                        AddFullyQualifiedNameEvent.ADD_FQN, getViewProperties()));
                return;
            }
            // check if the center pane is already showing, we don't want duplicate entries in the dropdowns
            if (!contentBorderPane.getCenter().equals(editFqnPane)) {
                editFullyQualifiedNameController.updateModel(getViewProperties(), null);
                contentBorderPane.setCenter(editFqnPane);
            }
            editButton.setSelected(true);
            editButton.setText("EDIT");
            if (evt.getPublicId() != null) {
                editFullyQualifiedNameController.setConceptAndPopulateForm(evt.getPublicId());
            }else {
                editFullyQualifiedNameController.setConceptAndPopulateForm(evt.getDescrName());
            }
        };
        eventBus.subscribe(conceptTopic, EditConceptFullyQualifiedNameEvent.class, editConceptFullyQualifiedNameEventSubscriber);

        // this event happens on during the creation of a new concept
        // a new concept will not have a fully qualified name and will need one
        addFqnSubscriber = evt -> {
            // check if the center pane is already showing, we don't want duplicate entries in the dropdowns
            if (!contentBorderPane.getCenter().equals(addFqnPane)) {
                contentBorderPane.setCenter(addFqnPane);
                editButton.setSelected(true);
                editButton.setText("ADD");

                // Clear existing ViewModel in form.
                addFullyQualifiedNameController.updateModel(evt.getViewProperties(), (viewModel, controller) -> {
                    viewModel.setPropertyValue(NAME_TEXT, "")
                            .setPropertyValue(CASE_SIGNIFICANCE, null)
                            .setPropertyValue(MODULE, null)
                            .setPropertyValue(LANGUAGE, null)
                            .setPropertyValue(STATUS, TinkarTerm.ACTIVE_STATE)
                            .setPropertyValue(IS_SUBMITTED, false);
                    controller.clearView();
                    controller.updateView();
                });
            }
        };
        eventBus.subscribe(conceptTopic, AddFullyQualifiedNameEvent.class, addFqnSubscriber);

        // when opening the properties panel the default toggle to view is the history tab
        propsPanelOpen = evt -> {
            if (contentBorderPane.getCenter() == null) {
                historyButton.setSelected(true);
                contentBorderPane.setCenter(historyTabsBorderPane);
            }
        };
        eventBus.subscribe(conceptTopic, OpenPropertiesPanelEvent.class, propsPanelOpen);

        // -- add stamp
        addStampSubscriber = evt -> {
            if (evt.getEventType() == ADD_STAMP) {
                stampJFXNode.controller().init(stampAddSubmitFormViewModel);
                this.stampAddSubmitFormViewModel.update(entityFacade, conceptTopic, viewProperties);

                contentBorderPane.setCenter(stampJFXNode.node());
                editButton.setSelected(true);
            }
        };

        eventBus.subscribe(conceptTopic, StampEvent.class, addStampSubscriber);

        // -- create stamp
        createStampSubscriber = evt -> {
            if (evt.getEventType() == CREATE_STAMP) {
                stampJFXNode.controller().init(stampCreateFormViewModel);
                this.stampCreateFormViewModel.update(entityFacade, conceptTopic, viewProperties);

                contentBorderPane.setCenter(stampJFXNode.node());
                editButton.setSelected(true);
            }
        };

        eventBus.subscribe(conceptTopic, StampEvent.class, createStampSubscriber);
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setConceptTopic(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
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

    public String selectedView() {
        Toggle tab = propertyToggleButtonGroup.getSelectedToggle();
        if (editButton.equals(tab)) {
            return "EDIT";
        } else if (hierarchyButton.equals(tab)) {
            return "HIERARCHY";
        } else if (historyButton.equals(tab)) {
            return "HISTORY";
        } else if (commentsButton.equals(tab)) {
            return "COMMENTS";
        } else {
            return "NONE";
        }
    };

    public void restoreSelectedView(String selectedView) {
        LOG.info("restore selected concept view with " + selectedView);
        switch (selectedView) {
            case "EDIT" -> {
                editButton.setSelected(true);
                contentBorderPane.setCenter(editBorderPane);
            }
            case "HIERARCHY" -> {
                hierarchyButton.setSelected(true);
                contentBorderPane.setCenter(hierarchyTabBorderPane);
            }
            case "HISTORY" -> {
                historyButton.setSelected(true);
                contentBorderPane.setCenter(historyTabsBorderPane);
            }
            case "COMMENTS" -> {
                commentsButton.setSelected(true);
                contentBorderPane.setCenter(commentsPane);
            }
        }
    }

    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade){
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
        this.historyChangeController.updateModel(viewProperties, entityFacade);
        this.hierarchyController.updateModel(viewProperties, entityFacade);

        this.editDescriptionsController.updateModel(viewProperties);

        this.editDescriptionFormController.updateModel(viewProperties, entityFacade);
        this.editFullyQualifiedNameController.updateModel(viewProperties, entityFacade);

        // Create a new DescrNameViewModel for the otherNameViewModel.
        this.addOtherNameController.updateModel(viewProperties);

        // Create a new DescrNameViewModel for the addfqncontroller.
        this.addFullyQualifiedNameController.updateModel(viewProperties);

        if (editMode && stampAddSubmitFormViewModel != null) {
            this.stampAddSubmitFormViewModel.update(entityFacade, conceptTopic, viewProperties);
        } else if (!editMode && stampCreateFormViewModel != null) {
            this.stampCreateFormViewModel.update(entityFacade, conceptTopic, viewProperties);
        }
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

    /**
     * Returns the propertiesTabsPane to be used as a draggable region.
     * @return The FlowPane containing the property tabs
     */
    public FlowPane getPropertiesTabsPane() {
        return propertiesTabsPane;
    }

    public void setHasOtherName(boolean value) {
        hasOtherNames = value;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public StampFormViewModelBase getStampFormViewModel() {
        if (editMode) {
            return stampAddSubmitFormViewModel;
        } else {
            return stampCreateFormViewModel;
        }
    }
}
