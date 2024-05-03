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
package dev.ikm.komet.amplify.lidr.properties;

import dev.ikm.komet.amplify.lidr.analyte.AnalyteGroupController;
import dev.ikm.komet.amplify.lidr.device.DeviceController;
import dev.ikm.komet.amplify.lidr.events.ShowPanelEvent;
import dev.ikm.komet.amplify.lidr.viewmodels.AnalyteGroupViewModel;
import dev.ikm.komet.amplify.mvvm.ValidationViewModel;
import dev.ikm.komet.amplify.mvvm.loader.Config;
import dev.ikm.komet.amplify.mvvm.loader.FXMLMvvmLoader;
import dev.ikm.komet.amplify.mvvm.loader.JFXNode;
import dev.ikm.komet.amplify.mvvm.loader.NamedVm;
import dev.ikm.komet.amplify.properties.HierarchyController;
import dev.ikm.komet.amplify.properties.HistoryChangeController;
import dev.ikm.komet.amplify.lidr.results.ResultsController;
import dev.ikm.komet.amplify.lidr.viewmodels.DeviceViewModel;
import dev.ikm.komet.amplify.lidr.viewmodels.ResultsViewModel;
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
import java.util.UUID;

import static dev.ikm.komet.amplify.commons.CssHelper.genText;
import static dev.ikm.komet.amplify.lidr.events.ShowPanelEvent.*;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.*;

/**
 * The properties window providing tabs of Edit, Hierarchy, History, and Comments.
 * This controller is associated with the view file history-change-selection.fxml.
 */
public class PropertiesController {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);
    protected static final String HISTORY_CHANGE_FXML_FILE = "history-change-selection.fxml";
    protected static final String HIERARCHY_VIEW_FXML_FILE = "hierarchy-view.fxml";
    protected static final String DEVICE_FXML_FILE = "device-summary.fxml";
    protected static final String ANALYTE_GROUP_FXML_FILE = "analyte-group.fxml";
    protected static final String MANUAL_ENTRY_RESULTS_FXML_FILE = "manual-entry-results.fxml";



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

    private Pane addDevicePane;

    private DeviceController addDeviceController;

    private Pane analyteGroupPane;

    private AnalyteGroupController analyteGroupController;

    private Pane manualResultsPane;

    private ResultsController manualResultsController;

    private Pane currentAddEditPane;
    private Pane commentsPane = new StackPane(genText("Comments Pane"));
    private ViewProperties viewProperties;
    private EntityFacade entityFacade;

    private EvtBus eventBus;

    private UUID conceptTopic;


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
        FXMLLoader loader = new FXMLLoader(HistoryChangeController.class.getResource(HISTORY_CHANGE_FXML_FILE));
        historyTabsBorderPane = loader.load();
        historyChangeController = loader.getController();

        // Load Hierarchy tab View Panel (FXML & Controller)
        FXMLLoader loader2 = new FXMLLoader(HierarchyController.class.getResource(HIERARCHY_VIEW_FXML_FILE));
        hierarchyTabBorderPane = loader2.load();
        hierarchyController = loader2.getController();
        // updateModel() TODO we may avoid this call
        updateModel(getViewProperties(), null);

        // +-----------------------------------
        // ! Add a Device and MFG
        // +------------------------------------
        ValidationViewModel deviceViewModel = new DeviceViewModel().setPropertyValue(MODE, CREATE)
                .setPropertyValue(VIEW_PROPERTIES, viewProperties)
                .setPropertyValue(CONCEPT_TOPIC, conceptTopic)
                .save(true);

        Config deviceConfig = new Config(DeviceController.class.getResource(DEVICE_FXML_FILE));
        deviceConfig.namedViewModels(new NamedVm("deviceViewModel", deviceViewModel));
        JFXNode<Pane, DeviceController> deviceControllerJFXNode = FXMLMvvmLoader.make(deviceConfig);
        addDeviceController = deviceControllerJFXNode.controller();
        addDevicePane = deviceControllerJFXNode.node();

        // +-----------------------------------
        // ! Analyte Group
        // +------------------------------------
        ValidationViewModel analyteGroupViewModel = new AnalyteGroupViewModel()
                .setPropertyValue(MODE, CREATE)
                .setPropertyValue(VIEW_PROPERTIES, viewProperties)
                .setPropertyValue(CONCEPT_TOPIC, conceptTopic)
                .save(true);

        Config analyteGroupConfig = new Config(AnalyteGroupController.class.getResource(ANALYTE_GROUP_FXML_FILE));
        analyteGroupConfig.namedViewModels(new NamedVm("analyteGroupViewModel", analyteGroupViewModel));
        JFXNode<Pane, AnalyteGroupController> analyteControllerJFXNode = FXMLMvvmLoader.make(analyteGroupConfig);
        analyteGroupController = analyteControllerJFXNode.controller();
        analyteGroupPane = analyteControllerJFXNode.node();

        // +-----------------------------------
        // ! Results Manual
        // +------------------------------------
        ValidationViewModel resultsViewModel = new ResultsViewModel()
                .setPropertyValue(MODE, CREATE)
                .setPropertyValue(VIEW_PROPERTIES, viewProperties)
                .setPropertyValue(CONCEPT_TOPIC, conceptTopic)
                .save(true);
        Config resultsConfig = new Config(ResultsController.class.getResource(MANUAL_ENTRY_RESULTS_FXML_FILE));
        resultsConfig.namedViewModels(new NamedVm("resultsViewModel", resultsViewModel));
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
                currentAddEditPane = addDevicePane; // must be available.
            } else if (evt.getEventType() == SHOW_ADD_ANALYTE_GROUP) {
                currentAddEditPane = analyteGroupPane;
            } else if (evt.getEventType() == SHOW_MANUAL_ADD_RESULTS) {
                currentAddEditPane = manualResultsPane;
            }
            updateAddEditPane();
        });
        eventBus.subscribe(conceptTopic, ShowPanelEvent.class, showPanelEventSubscriber);
    }

    /**
     *
     */
    private void updateAddEditPane() {
        contentBorderPane.setCenter(currentAddEditPane);
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


}
