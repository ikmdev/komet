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
package dev.ikm.komet.kview.mvvm.view.pattern;

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ShowPatternPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.UUID;

import static dev.ikm.komet.kview.events.ShowPatternPanelEvent.SHOW_ADD_DEFINITION;
import static dev.ikm.komet.kview.events.ShowPatternPanelEvent.SHOW_EDIT_FIELDS;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

public class PropertiesController {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);

    private static final URL PATTERN_DEFINITION_FXML_URL = PatternDefinitionController.class.getResource("pattern-definition.fxml");

    private static final URL PATTERN_FIELDS_FXML_URL = PatternFieldsController.class.getResource("pattern-fields.fxml");

    private EvtBus eventBus;

    @InjectViewModel
    private PatternViewModel patternViewModel;

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

    private Pane currentEditPane;

    private PatternDefinitionController patternDefinitionController;

    private PatternFieldsController patternFieldsController;

    private Pane patternDefinitionPane;

    private Pane patternFieldsPane;

    private Subscriber<ShowPatternPanelEvent> showPatternPanelEventSubscriber;

    @FXML
    private void initialize() {
        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // +-----------------------------------
        // ! Add definition(s) to a Pattern
        // +------------------------------------
        Config definitionConfig = new Config(PATTERN_DEFINITION_FXML_URL);

        JFXNode<Pane, PatternDefinitionController> patternDefinitionControllerJFXNode = FXMLMvvmLoader.make(definitionConfig);
        patternDefinitionController = patternDefinitionControllerJFXNode.controller();
        patternDefinitionPane = patternDefinitionControllerJFXNode.node();


        // +-----------------------------------
        // ! Edit field(s) within a Pattern
        // +-----------------------------------
        Config fieldsConfig = new Config(PATTERN_FIELDS_FXML_URL);
        fieldsConfig.updateViewModel("propertiesViewModel", (propertiesViewModel) ->
                propertiesViewModel.setPropertyValue(VIEW_PROPERTIES, getViewProperties()));

        JFXNode<Pane, PatternFieldsController> patternFieldsJFXNode = FXMLMvvmLoader.make(fieldsConfig);
        patternFieldsController = patternFieldsJFXNode.controller();
        patternFieldsPane = patternFieldsJFXNode.node();
        patternFieldsController.setViewProperties(getViewProperties());

        // initially a default selected tab and view is shown
        updateDefaultSelectedViews();

        showPatternPanelEventSubscriber = evt -> {
            LOG.info("Show Edit View " + evt.getEventType());
            this.editButton.setSelected(true);

            // TODO swap based on state (edit definition, ).
            if (evt.getEventType() == SHOW_ADD_DEFINITION) {
                currentEditPane = patternDefinitionPane; // must be available.
            } else if (evt.getEventType() == SHOW_EDIT_FIELDS) {
                currentEditPane = patternFieldsPane;
            }
            updateEditPane();
        };
        eventBus.subscribe(getConceptTopic(), ShowPatternPanelEvent.class, showPatternPanelEventSubscriber);
    }

    private void updateDefaultSelectedViews() {
        // default to selected tab (History)
        Toggle tab = propertyToggleButtonGroup.getSelectedToggle();
        if (editButton.equals(tab)) {
            contentBorderPane.setCenter(null);
        }
    }

    @FXML
    private void showEditView(ActionEvent event) {
        event.consume();
        this.editButton.setSelected(true);
        LOG.info("Show Edit View " + event);
        contentBorderPane.setCenter(patternDefinitionPane);
    }

    private void updateEditPane() {
        contentBorderPane.setCenter(currentEditPane);
    }

    public UUID getConceptTopic() {
        //TODO will we have a pattern topic?
        return patternViewModel.getPropertyValue(CONCEPT_TOPIC);
    }

    public ViewProperties getViewProperties() {
        return patternViewModel.getPropertyValue(VIEW_PROPERTIES);
    }
    public void clearView() {
    }
}
