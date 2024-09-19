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


import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.DESCRIPTION_NAME;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_DEFINITION;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_FQN;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_OTHER_NAME;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_FIELDS;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_FQN;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_OTHER_NAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.DESCRIPTION_NAME_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel.DISPLAY_DEFINITION_EDIT_MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel.DISPLAY_FQN_EDIT_MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel.DISPLAY_OTHER_NAME_EDIT_MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent;
import dev.ikm.komet.kview.mvvm.view.descriptionname.DescriptionNameController;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
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
import org.carlfx.cognitive.loader.NamedVm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.UUID;

public class PropertiesController {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);

    private static final URL PATTERN_DEFINITION_FXML_URL = PatternDefinitionController.class.getResource("pattern-definition.fxml");
    private static final URL PATTERN_DESCRIPTION_FXML_URL = DescriptionNameController.class.getResource("description-name.fxml");

    private static final URL PATTERN_FIELDS_FXML_URL = PatternFieldsController.class.getResource("pattern-fields.fxml");

    private static final URL PATTERN_FORM_CHOOSER_FXML_URL = PatternFormChooserController.class.getResource("pattern-form-chooser.fxml");

    private static final URL DESCRIPTION_FORM_CHOOSER_FXML_URL = DescriptionFormChooserController.class.getResource("pattern-description-chooser.fxml");

    private static final String ADD_FQN_TITLE_TEXT = "Add Description: Add Fully Qualified Name";

    private static final String EDIT_FQN_TITLE_TEXT = "Edit Description: Edit Fully Qualified Name";

    private static final String ADD_OTHER_NAME_TITLE_TEXT = "Add Description: Add Other Name";

    private static final String EDIT_OTHER_NAME_TITLE_TEXT = "Edit Description: Edit Other Name";

    private EvtBus eventBus;

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;


    @FXML
    private SVGPath commentsButton;

    @FXML
    private ToggleButton addEditButton;

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

    private PatternFormChooserController patternFormChooserController;

    private Pane patternFormChooserPane;

    private DescriptionFormChooserController descriptionFormChooserController;

    private Pane descriptionNameChooserPane;

    private Subscriber<ShowPatternFormInBumpOutEvent> showPatternPanelEventSubscriber;

    private Subscriber<PropertyPanelEvent> showPropertyPanelSubscriber;

    private Subscriber<PatternDefinitionEvent> patternDefinitionEventSubscriber;

    @FXML
    private void initialize() {
        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // +-----------------------------------
        // ! Add definition(s) to a Pattern
        // +------------------------------------
        Config definitionConfig = new Config(PATTERN_DEFINITION_FXML_URL)
                .addNamedViewModel(new NamedVm("patternPropertiesViewModel", patternPropertiesViewModel))
                .updateViewModel("patternDefinitionViewModel", (patternDefinitionViewModel) ->
                        patternDefinitionViewModel.setPropertyValue(PATTERN_TOPIC, patternPropertiesViewModel.getPropertyValue(PATTERN_TOPIC)));
        JFXNode<Pane, PatternDefinitionController> patternDefinitionControllerJFXNode = FXMLMvvmLoader.make(definitionConfig);
        patternDefinitionController = patternDefinitionControllerJFXNode.controller();
        patternDefinitionPane = patternDefinitionControllerJFXNode.node();


        // +-----------------------------------
        // ! Edit field(s) within a Pattern
        // +-----------------------------------
        Config fieldsConfig = new Config(PATTERN_FIELDS_FXML_URL)
                .updateViewModel("patternFieldsViewModel", (patternFieldsViewModel) ->
                        patternFieldsViewModel
                                .setPropertyValue(PATTERN_TOPIC, patternPropertiesViewModel.getPropertyValue(PATTERN_TOPIC))
                                .setPropertyValue(VIEW_PROPERTIES, getViewProperties()));

        JFXNode<Pane, PatternFieldsController> patternFieldsJFXNode = FXMLMvvmLoader.make(fieldsConfig);
        patternFieldsController = patternFieldsJFXNode.controller();
        patternFieldsPane = patternFieldsJFXNode.node();
        patternFieldsController.setViewProperties(getViewProperties());

        JFXNode<Pane, PatternFormChooserController> patternFormJFXNode = FXMLMvvmLoader.make(PATTERN_FORM_CHOOSER_FXML_URL,
                new PatternFormChooserController(patternPropertiesViewModel.getPropertyValue(PATTERN_TOPIC)));
        patternFormChooserController = patternFormJFXNode.controller();
        patternFormChooserPane = patternFormJFXNode.node();

        Config descrFormconfig = new Config(DESCRIPTION_FORM_CHOOSER_FXML_URL)
                .addNamedViewModel(new NamedVm("patternPropertiesViewModel", patternPropertiesViewModel));
        JFXNode<Pane, DescriptionFormChooserController> descriptionFormJFXNode = FXMLMvvmLoader.make(descrFormconfig);
        descriptionFormChooserController = descriptionFormJFXNode.controller();
        descriptionNameChooserPane = descriptionFormJFXNode.node();

        // initially a default selected tab and view is shown
        updateDefaultSelectedViews();

        // choose a specific bump out panel either by clicking one of the
        // pencil buttons, or by navigating from the properties toggle and
        // selecting with form chooser buttons
        showPatternPanelEventSubscriber = evt -> {
            LOG.info("Show Add/Edit View " + evt.getEventType());

            // TODO swap based on state (edit definition, ).
            if (evt.getEventType() == SHOW_ADD_DEFINITION) {
                currentEditPane = patternDefinitionPane; // must be available.
            } else if (evt.getEventType() == SHOW_EDIT_FIELDS) {
                currentEditPane = patternFieldsPane;
            } else if (evt.getEventType().getSuperType() == DESCRIPTION_NAME) {
                setupDescriptionNamePane(evt.getEventType());
            } else if (evt.getEventType() == DESCRIPTION_NAME) {
                currentEditPane = descriptionNameChooserPane;
            }
            this.addEditButton.setSelected(true);
            updateEditPane();
        };
        eventBus.subscribe(getPatternTopic(), ShowPatternFormInBumpOutEvent.class, showPatternPanelEventSubscriber);

        // ONLY for clicking the properties toggle
        showPropertyPanelSubscriber = evt -> {
            if (evt.getSource() instanceof ToggleButton && evt.getEventType() == OPEN_PANEL) {
                // if they hit the properties button, then give them the form chooser panel
                // e.g. buttons with choices of Add|Edit Definition, Add|Edit Description, Add|Edit Fields
                setupBumpOut();
                updateEditPane();
            }
        };
        eventBus.subscribe(getPatternTopic(), PropertyPanelEvent.class, showPropertyPanelSubscriber);

        patternDefinitionEventSubscriber = evt -> {
            boolean isInEditMode = patternPropertiesViewModel.getPropertyValue(DISPLAY_DEFINITION_EDIT_MODE);
            this.addEditButton.setText(isInEditMode ? "EDIT" : "ADD");
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(getPatternTopic(), PatternDefinitionEvent.class, patternDefinitionEventSubscriber);
        this.addEditButton.setSelected(true);
    }

    private void setupBumpOut() {
        if (patternPropertiesViewModel.shouldShowFormChooser()) {
            currentEditPane = patternFormChooserPane;
        } else if (patternPropertiesViewModel.shouldShowDescriptionChooser()) {
            currentEditPane = descriptionNameChooserPane;
        } else if (patternPropertiesViewModel.shouldShowFields()) {
            currentEditPane = patternFieldsPane;
        }
    }

    private void setupDescriptionNamePane(EvtType eventType) {
        if (eventType.getSuperType() != DESCRIPTION_NAME) {
            throw new RuntimeException("Event is not a ShowPatternPanelEvent.DESCRIPTION_NAME");
        }

        Config descrConfig = new Config(PATTERN_DESCRIPTION_FXML_URL);
        descrConfig
                .addNamedViewModel(new NamedVm("patternPropertiesViewModel", patternPropertiesViewModel))
                .updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                    descrNameViewModel
                        .setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                        .setPropertyValue(PATTERN_TOPIC, getPatternTopic());
        });
        if (eventType == SHOW_ADD_FQN) {
            descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                descrNameViewModel.setPropertyValue(MODE, CREATE)
                        .setPropertyValue(NAME_TYPE, FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                        .setPropertyValue(TITLE_TEXT, ADD_FQN_TITLE_TEXT)
                        .setPropertyValue(DESCRIPTION_NAME_TYPE, "Fully Qualified Name")
                ;
            });
        } else if (eventType == SHOW_EDIT_FQN) {
            descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                descrNameViewModel.setPropertyValue(MODE, CREATE) // still creating, pattern not created yet
                        .setPropertyValue(NAME_TYPE, FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                        .setPropertyValue(TITLE_TEXT, EDIT_FQN_TITLE_TEXT)
                        .setPropertyValue(DESCRIPTION_NAME_TYPE, "Fully Qualified Name")
                ;
            });
        } else if (eventType == SHOW_ADD_OTHER_NAME) {
            descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                descrNameViewModel.setPropertyValue(MODE, CREATE)
                        .setPropertyValue(NAME_TYPE, REGULAR_NAME_DESCRIPTION_TYPE)
                        .setPropertyValue(TITLE_TEXT, ADD_OTHER_NAME_TITLE_TEXT)
                        .setPropertyValue(DESCRIPTION_NAME_TYPE, "Other Name")
                ;
            });
        } else if (eventType == SHOW_EDIT_OTHER_NAME) {
            descrConfig.updateViewModel("descrNameViewModel", (descrNameViewModel) -> {
                descrNameViewModel.setPropertyValue(MODE, CREATE) // still creating, pattern not created yet
                        .setPropertyValue(NAME_TYPE, REGULAR_NAME_DESCRIPTION_TYPE)
                        .setPropertyValue(TITLE_TEXT, EDIT_OTHER_NAME_TITLE_TEXT)
                        .setPropertyValue(DESCRIPTION_NAME_TYPE, "Other Name")
                ;
            });
        }
        JFXNode<Pane, DescriptionNameController> descriptionNameControllerJFXNode = FXMLMvvmLoader.make(descrConfig);
        currentEditPane = descriptionNameControllerJFXNode.node();
    }

    private void updateDefaultSelectedViews() {
        // default to selected tab (History)
        Toggle tab = propertyToggleButtonGroup.getSelectedToggle();
        if (addEditButton.equals(tab)) {
            contentBorderPane.setCenter(null);
        }
    }

    @FXML
    private void showAddEditView(ActionEvent event) {
        LOG.info("Show Add/Edit View " + event);
        event.consume();
        this.addEditButton.setSelected(true);
        contentBorderPane.setCenter(currentEditPane);
        if (currentEditPane == descriptionNameChooserPane) {
            // if we will display EDIT for either FQN or Other Name, the display EDIT, otherwise ADD
            if ((boolean) patternPropertiesViewModel.getPropertyValue(DISPLAY_FQN_EDIT_MODE)
                    || (boolean) patternPropertiesViewModel.getPropertyValue(DISPLAY_OTHER_NAME_EDIT_MODE)) {
                this.addEditButton.setText("EDIT");
            } else {
                this.addEditButton.setText("ADD");
            }
        }
    }

    private void updateEditPane() {
        contentBorderPane.setCenter(currentEditPane);
    }

    public UUID getPatternTopic() {
        return patternPropertiesViewModel.getPropertyValue(PATTERN_TOPIC);
    }

    public ViewProperties getViewProperties() {
        return patternPropertiesViewModel.getPropertyValue(VIEW_PROPERTIES);
    }
    public void clearView() {
    }
}
