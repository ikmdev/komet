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
package dev.ikm.komet.kview.lidr.mvvm.view.properties;

import dev.ikm.komet.kview.lidr.mvvm.view.analyte.AnalyteGroupController;
import dev.ikm.komet.kview.lidr.mvvm.view.device.DeviceController;
import dev.ikm.komet.kview.lidr.mvvm.view.results.ResultsController;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.AnalyteGroupViewModel;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.DeviceViewModel;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel;
import dev.ikm.komet.kview.lidr.events.ShowPanelEvent;
import dev.ikm.komet.kview.mvvm.view.properties.HierarchyController;
import dev.ikm.komet.kview.mvvm.view.properties.HistoryChangeController;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.carlfx.cognitive.loader.*;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import static dev.ikm.komet.kview.fxutils.CssHelper.genText;
import static dev.ikm.komet.kview.lidr.events.ShowPanelEvent.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.*;

/**
 * The properties window providing tabs of Edit, Hierarchy, History, and Comments.
 * This view is associated with the view file history-change-selection.fxml.
 */
public class PropertiesController {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);
    protected static final String HISTORY_CHANGE_FXML_FILE = "history-change-selection.fxml";
    protected static final String HIERARCHY_VIEW_FXML_FILE = "hierarchy-view.fxml";
    protected static final URL DEVICE_FXML_URL = DeviceController.class.getResource("device-summary.fxml");
    protected static final URL ANALYTE_GROUP_FXML_URL = AnalyteGroupController.class.getResource("analyte-group.fxml");
    protected static final URL MANUAL_ENTRY_RESULTS_FXML_URL = ResultsController.class.getResource("manual-entry-results.fxml");

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

    @FXML
    private FlowPane propertiesTabsPane;

    @InjectViewModel
    private SimpleViewModel propertiesViewModel;

    private Pane historyTabsBorderPane;
    private HistoryChangeController historyChangeController;

    private Pane hierarchyTabBorderPane;
    private HierarchyController hierarchyController;

    private Pane addDevicePane;

    private DeviceController addDeviceController;

    private Pane analyteGroupPane;

    private AnalyteGroupController analyteGroupController;

    private Pane manualResultsPane;

    private ResultsController manualResultsController;

    private Pane currentAddEditPane;
    private Pane commentsPane = new StackPane(genText("Comments Pane"));
    private EntityFacade entityFacade;

    private EvtBus eventBus;


    public PropertiesController() {
    }

    public UUID getConceptTopic() {
        return propertiesViewModel.getPropertyValue(CONCEPT_TOPIC);
    }
    /**
     * This is called after dependency injection has occurred to the JavaFX controls above.
     */
    @FXML
    public void initialize() throws IOException {
        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // Load History tabs View Panel (FXML & Controller)
        FXMLLoader loader = new FXMLLoader(HistoryChangeController.class.getResource(HISTORY_CHANGE_FXML_FILE));
        historyTabsBorderPane = loader.load();
        historyChangeController = loader.getController();

        // Load Hierarchy tab View Panel (FXML & Controller)
        FXMLLoader loader2 = new FXMLLoader(HierarchyController.class.getResource(HIERARCHY_VIEW_FXML_FILE));
        hierarchyTabBorderPane = loader2.load();
        hierarchyController = loader2.getController();

        // updates both history and hierarchy controllers. If create Mode the device is null.
        updateModel(getViewProperties(), propertiesViewModel.getPropertyValue(LidrViewModel.DEVICE_ENTITY));
        updateView();

        // +-----------------------------------
        // ! Add a Device and MFG
        // +------------------------------------
        ValidationViewModel deviceViewModel = new DeviceViewModel()
                .setPropertyValue(MODE, CREATE)
                .setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                .setPropertyValue(CONCEPT_TOPIC, getConceptTopic());

        Config deviceConfig = new Config(DEVICE_FXML_URL)
                .addNamedViewModel(new NamedVm("deviceViewModel", deviceViewModel));

        JFXNode<Pane, DeviceController> deviceControllerJFXNode = FXMLMvvmLoader.make(deviceConfig);
        addDeviceController = deviceControllerJFXNode.controller();
        addDevicePane = deviceControllerJFXNode.node();

        // +-----------------------------------
        // ! Analyte Group
        // +------------------------------------
        ValidationViewModel analyteGroupViewModel = new AnalyteGroupViewModel()
                .setPropertyValue(MODE, CREATE)
                .setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                .setPropertyValue(CONCEPT_TOPIC, getConceptTopic());

        Config analyteGroupConfig = new Config(ANALYTE_GROUP_FXML_URL)
                .addNamedViewModel(new NamedVm("analyteGroupViewModel", analyteGroupViewModel));

        JFXNode<Pane, AnalyteGroupController> analyteControllerJFXNode = FXMLMvvmLoader.make(analyteGroupConfig);
        analyteGroupController = analyteControllerJFXNode.controller();
        analyteGroupPane = analyteControllerJFXNode.node();
        // +-----------------------------------
        // ! Results Manual Entry
        // +------------------------------------
        Config resultsConfig = new Config(MANUAL_ENTRY_RESULTS_FXML_URL)
                .updateViewModel("resultsViewModel", (resultsViewModel) ->
                        resultsViewModel
                                .setPropertyValue(MODE, CREATE)
                                .setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                                .setPropertyValue(CONCEPT_TOPIC, getConceptTopic()));

        JFXNode<Pane, ResultsController> resultsControllerJFXNode = FXMLMvvmLoader.make(resultsConfig);
        manualResultsController = resultsControllerJFXNode.controller();
        manualResultsPane = resultsControllerJFXNode.node();

        // initially a default selected tab and view is shown
        updateDefaultSelectedViews();

        // Responsible for showing a panel based on the user clicking on pencil (device, anaylte group)
        Subscriber<ShowPanelEvent> showPanelEventSubscriber = (evt -> {
            LOG.info("Show Edit View " + evt.getEventType());
            this.editButton.setSelected(true);

            // TODO swap based on state (addDevice, addAnalyteGroup).
            if (evt.getEventType() == SHOW_ADD_DEVICE) {
                deviceViewModel.setPropertyValue(DeviceViewModel.IS_INVALID, true);
                currentAddEditPane = addDevicePane; // must be available.
            } else if (evt.getEventType() == SHOW_ADD_ANALYTE_GROUP) {
                analyteGroupViewModel.setPropertyValue(AnalyteGroupViewModel.IS_INVALID, true);
                currentAddEditPane = analyteGroupPane;
            } else if (evt.getEventType() == SHOW_MANUAL_ADD_RESULTS) {
                currentAddEditPane = manualResultsPane;
            }
            updateAddEditPane();
        });
        eventBus.subscribe(getConceptTopic(), ShowPanelEvent.class, showPanelEventSubscriber);
    }

    /**
     * Obtain a view model based on variable name.
     * @param jFXNode a JFXNode object contains JavaFX Node and Controller pair.
     * @param viewModelVariableName The variable name of the ViewModel instance inside of the controller class.
     * @return A view model
     */
    public static <T extends Node, U, R extends ViewModel> R getViewModel(JFXNode<T, U> jFXNode, String viewModelVariableName) {
        ViewModel viewModel = jFXNode
                .namedViewModels()
                .stream()
                .filter(namedVm -> namedVm.variableName().equals(viewModelVariableName))
                .map(NamedVm::viewModel)
                .findAny().get();
        return (R) viewModel;
    }

    /**
     *
     */
    private void updateAddEditPane() {
        contentBorderPane.setCenter(currentAddEditPane);
    }
    public ViewProperties getViewProperties() {
        return propertiesViewModel.getPropertyValue(VIEW_PROPERTIES);
    }


    private void updateDefaultSelectedViews() {
        // default to selected tab (History)
        Toggle tab = propertyToggleButtonGroup.getSelectedToggle();
        if (editButton.equals(tab)) {
            contentBorderPane.setCenter(null);
        } else if (hierarchyButton.equals(tab)) {
            contentBorderPane.setCenter(hierarchyTabBorderPane);
        } else if (historyButton.equals(tab)) {
            contentBorderPane.setCenter(historyTabsBorderPane);
        } else if (commentsButton.equals(tab)) {
            contentBorderPane.setCenter(commentsPane);
        }
    }

    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade){
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
        this.editButton.setSelected(true);
        LOG.info("Show Edit View " + event);
        // TODO swap based on state (addDevice, addAnalyteGroup).
        contentBorderPane.setCenter(addDevicePane);
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
}
