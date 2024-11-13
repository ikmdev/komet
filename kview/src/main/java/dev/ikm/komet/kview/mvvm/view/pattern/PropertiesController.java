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


import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.view.descriptionname.DescriptionNameController;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.*;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.DEFINITION_CONFIRMATION;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STATE_MACHINE;
import static dev.ikm.komet.kview.state.PatternDetailsState.NEW_PATTERN_INITIAL;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;

public class PropertiesController {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);

    private static final URL PATTERN_HISTORY_FXML_URL = HistoryController.class.getResource("history.fxml");

    private static final URL CONFIRMATION_FXML_URL = ConfirmationController.class.getResource("confirmation-pane.fxml");

    private static final URL CONTINUE_ADDING_FIELDS_URL = ContinueAddFieldsController.class.getResource("continue-adding-fields.fxml");

    private static final URL PATTERN_DEFINITION_FXML_URL = PatternDefinitionController.class.getResource("pattern-definition.fxml");
    private static final URL PATTERN_DESCRIPTION_FXML_URL = DescriptionNameController.class.getResource("description-name.fxml");

    private static final URL PATTERN_FIELDS_FXML_URL = PatternFieldsController.class.getResource("pattern-fields.fxml");

    private static final String ADD_FQN_TITLE_TEXT = "Add Description: Add Fully Qualified Name";

    private static final String EDIT_FQN_TITLE_TEXT = "Edit Description: Edit Fully Qualified Name";

    private static final String ADD_OTHER_NAME_TITLE_TEXT = "Add Description: Add Other Name";

    private static final String EDIT_OTHER_NAME_TITLE_TEXT = "Edit Description: Edit Other Name";

    private static final String EDIT_FIELD = "Edit Field";
    private static final String ADD_FIELD = "Add Field";

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;

    @InjectViewModel
    private PatternViewModel patternViewModel;

    @FXML
    private SVGPath commentsButton;

    @FXML
    private ToggleButton addEditButton;

    @FXML
    private ToggleButton historyButton;

    @FXML
    private ToggleButton instancesButton;

    @FXML
    private ToggleGroup propertyToggleButtonGroup;

    @FXML
    private BorderPane contentBorderPane;

    private Pane currentEditPane;

    private HistoryController historyController;

    private ConfirmationController confirmationController;

    private ContinueAddFieldsController continueAddFieldsController;

    private PatternDefinitionController patternDefinitionController;

    private PatternFieldsController patternFieldsController;

    private Pane historyPane;

    private Pane confirmationPane;

    private Pane continueAddFieldsPane;

    private Pane patternDefinitionPane;

    private Pane descriptionPane;

    private Pane patternFieldsPane;

    private Subscriber<ShowPatternFormInBumpOutEvent> showPatternFormInBumpOutEventSubscriber;

    private Subscriber<PropertyPanelEvent> showPropertyPanelSubscriber;

    private Subscriber<PatternDescriptionEvent> patternDescriptionEventSubscriber;

    @FXML
    private void initialize() {
        clearView();

        // +-----------------------------------------------------------------------
        // ! history panel selected by default when an existing pattern is summoned
        // +-----------------------------------------------------------------------
        Config historyConfig = new Config(PATTERN_HISTORY_FXML_URL);
        JFXNode<Pane, HistoryController> patternHistoryJFXNode = FXMLMvvmLoader.make(historyConfig);
        historyController = patternHistoryJFXNode.controller();
        historyPane = patternHistoryJFXNode.node();

        // +-----------------------------------------------------------------------
        // ! confirmation panel reused by several forms
        // +-----------------------------------------------------------------------
        Config confirmationPanelConfig = new Config(CONFIRMATION_FXML_URL)
                .addNamedViewModel(new NamedVm("patternPropertiesViewModel", patternPropertiesViewModel));
        JFXNode<Pane, ConfirmationController> confirmationPanelJFXNode = FXMLMvvmLoader.make(confirmationPanelConfig);
        confirmationController = confirmationPanelJFXNode.controller();
        confirmationPane = confirmationPanelJFXNode.node();

        // +-----------------------------------------------------------------------
        // ! continue fields confirmation panel
        // +-----------------------------------------------------------------------
        Config continueAddFieldsConfig = new Config(CONTINUE_ADDING_FIELDS_URL)
                .addNamedViewModel(new NamedVm("patternPropertiesViewModel", patternPropertiesViewModel));
        JFXNode<Pane, ContinueAddFieldsController> continueFieldsJFXNode = FXMLMvvmLoader.make(continueAddFieldsConfig);
        continueAddFieldsController = continueFieldsJFXNode.controller();
        continueAddFieldsPane = continueFieldsJFXNode.node();

        // +-----------------------------------
        // ! Add definition(s) to a Pattern
        // +-----------------------------------
        Config definitionConfig = new Config(PATTERN_DEFINITION_FXML_URL)
                .addNamedViewModel(new NamedVm("patternPropertiesViewModel", patternPropertiesViewModel))
                .updateViewModel("patternDefinitionViewModel", (patternDefinitionViewModel) ->
                        patternDefinitionViewModel.setPropertyValue(PATTERN_TOPIC, patternPropertiesViewModel.getPropertyValue(PATTERN_TOPIC)));
        JFXNode<Pane, PatternDefinitionController> patternDefinitionControllerJFXNode = FXMLMvvmLoader.make(definitionConfig);
        patternDefinitionController = patternDefinitionControllerJFXNode.controller();
        patternDefinitionPane = patternDefinitionControllerJFXNode.node();

        // +-----------------------------------
        // ! Edit Descriptions within a Pattern
        // +-----------------------------------
        Config descrConfig = new Config(PATTERN_DESCRIPTION_FXML_URL);
        descrConfig
                .addNamedViewModel(new NamedVm("patternPropertiesViewModel", patternPropertiesViewModel))
                .updateViewModel("descrNameViewModel", (descrNameViewModel) ->
                        descrNameViewModel.setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                        .setPropertyValue(PATTERN_TOPIC, getPatternTopic())
                );
        JFXNode<Pane, DescriptionNameController> descriptionNameControllerJFXNode = FXMLMvvmLoader.make(descrConfig);
        descriptionPane = descriptionNameControllerJFXNode.node();

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

        // initially a default selected tab and view is shown
        updateDefaultSelectedViews();

        // choose a specific bump out panel either by clicking one of the
        // pencil buttons, or by navigating from the properties toggle and
        // selecting with form chooser buttons
        showPatternFormInBumpOutEventSubscriber = evt -> {
            LOG.info("Show Panel by event type: " + evt.getEventType());
            // TODO swap based on state (edit definition, ).
            if (evt.getEventType() == SHOW_ADD_DEFINITION) {
                currentEditPane = patternDefinitionPane; // must be available.
            } else if (evt.getEventType() == SHOW_EDIT_FIELDS) {
                //Set the field values for edit.
                Optional<ViewModel> viewModel = patternFieldsJFXNode.namedViewModels().stream().filter(namedVm -> namedVm.variableName().equals("patternFieldsViewModel")).map(NamedVm::viewModel).findAny();
                viewModel.ifPresent(model -> {
                    PatternField patternField = evt.getPatternField();
                    model.setPropertyValue(ADD_EDIT_LABEL, EDIT_FIELD);
                    model.setPropertyValue(TOTAL_EXISTING_FIELDS, evt.getTotalFields()-1);
                    model.setPropertyValue(FIELD_ORDER, evt.getFieldOrder());
                    model.setPropertyValue(DISPLAY_NAME, patternField.displayName());
                    model.setPropertyValue(DATA_TYPE, patternField.dataType());
                    model.setPropertyValue(PURPOSE_ENTITY, patternField.purpose());
                    model.setPropertyValue(MEANING_ENTITY, patternField.meaning());
                    model.setPropertyValue(COMMENTS, patternField.comments());
                    model.setPropertyValue(PREVIOUS_PATTERN_FIELD, patternField);
                });
                currentEditPane = patternFieldsPane;
            } else if (evt.getEventType() == SHOW_ADD_FIELDS) {
                patternFieldsJFXNode.updateViewModel("patternFieldsViewModel", (model) -> {
                    PatternField patternField = evt.getPatternField();
                    model.setPropertyValue(ADD_EDIT_LABEL, ADD_FIELD);
                    model.setPropertyValue(TOTAL_EXISTING_FIELDS, evt.getTotalFields());
                    model.setPropertyValue(PREVIOUS_PATTERN_FIELD, patternField);
                });
                currentEditPane = patternFieldsPane;
            } else if (evt.getEventType().getSuperType() == DESCRIPTION_NAME) {
                Optional<DescrNameViewModel> optionalDescrNameViewModel = descriptionNameControllerJFXNode.getViewModel("descrNameViewModel");
                optionalDescrNameViewModel.ifPresent(descrNameViewModel -> setupDescriptionNamePane(evt, descrNameViewModel));
                currentEditPane = descriptionPane;
            } else if (evt.getEventType() == SHOW_CONTINUE_ADD_FIELDS) {
                continueFieldsJFXNode.updateViewModel("patternPropertiesViewModel", (model) ->
                        model.setPropertyValue(TOTAL_EXISTING_FIELDS, evt.getTotalFields())
                );
                currentEditPane = continueAddFieldsPane;
            } else if (evt.getEventType() == SHOW_CONTINUE_EDIT_FIELDS) {
                confirmationController.showContinueEditingFields();
                currentEditPane = confirmationPane;
            }
            this.addEditButton.setSelected(true);
            this.contentBorderPane.setCenter(currentEditPane);
            patternViewModel.save();
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(getPatternTopic(), ShowPatternFormInBumpOutEvent.class, showPatternFormInBumpOutEventSubscriber);

        // ONLY for clicking the properties toggle
        showPropertyPanelSubscriber = evt -> {
            if (evt.getSource() instanceof ToggleButton && evt.getEventType() == OPEN_PANEL) {
                //FIXME why does the window line up off the screen to the left
                propertyPanelCheck();
            }
            if (evt.getEventType() == DEFINITION_CONFIRMATION) {
                currentEditPane = confirmationPane;
                contentBorderPane.setCenter(currentEditPane);
                StateMachine patternSM = getStateMachine();
                patternSM.t("addDefinitions");
                confirmationController.showDefinitionAdded();
            }
            patternViewModel.save();
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(getPatternTopic(), PropertyPanelEvent.class, showPropertyPanelSubscriber);

        patternDescriptionEventSubscriber = evt -> {
            if (evt.getEventType() == PatternDescriptionEvent.PATTERN_ADD_FQN) {
                confirmationController.showFqnAdded();
            } else if (evt.getEventType() == PatternDescriptionEvent.PATTERN_ADD_OTHER_NAME) {
                confirmationController.showOtherNameAdded();
            }
            currentEditPane = confirmationPane;
            contentBorderPane.setCenter(currentEditPane);
            patternViewModel.save();
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(getPatternTopic(), PatternDescriptionEvent.class, patternDescriptionEventSubscriber);

        this.addEditButton.setSelected(true);
    }

    private StateMachine getStateMachine() {
        return patternPropertiesViewModel.getPropertyValue(STATE_MACHINE);
    }

    private void propertyPanelCheck() {
        // figure out if we are a new pattern or existing so that we can determine which
        // panel to open.
        StateMachine stateMachine = patternPropertiesViewModel.getPropertyValue(STATE_MACHINE);
        if (stateMachine.currentState().equals(NEW_PATTERN_INITIAL)) {
            // a brand-new pattern shows the add edit view
            currentEditPane = patternDefinitionPane;
            showAddEditView(new ActionEvent());
        }
        //TODO the state of a completed pattern, ie summon an existing pattern
        // will default to the history pane
    }

    private void setupDescriptionNamePane(ShowPatternFormInBumpOutEvent event, DescrNameViewModel descrNameViewModel) {
        EvtType eventType = event.getEventType();
        if (eventType.getSuperType() != DESCRIPTION_NAME) {
            throw new RuntimeException("Event is not a ShowPatternPanelEvent.DESCRIPTION_NAME");
        }
        descrNameViewModel
            .setPropertyValue(VIEW_PROPERTIES, getViewProperties())
            .setPropertyValue(PATTERN_TOPIC, getPatternTopic());
        if (eventType == SHOW_ADD_FQN) {
            descrNameViewModel.setPropertyValue(MODE, CREATE)
                .setPropertyValue(NAME_TYPE, FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(TITLE_TEXT, ADD_FQN_TITLE_TEXT)
                .setPropertyValue(DESCRIPTION_NAME_TYPE, "Fully Qualified Name")
            ;
        } else if (eventType == SHOW_EDIT_FQN) {
            DescrName descrName = event.getDescrName();
            descrNameViewModel.setPropertyValue(MODE, CREATE) // still creating, pattern not created yet
                .setPropertyValue(NAME_TYPE, FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(TITLE_TEXT, EDIT_FQN_TITLE_TEXT)
                .setPropertyValue(DESCRIPTION_NAME_TYPE, "Fully Qualified Name")
                .setPropertyValue(NAME_TEXT, descrName.getNameText())
                .setPropertyValue(NAME_TYPE, descrName.getNameType())
                .setPropertyValue(CASE_SIGNIFICANCE, descrName.getCaseSignificance())
                .setPropertyValue(STATUS, descrName.getStatus())
                .setPropertyValue(MODULE, descrName.getModule())
                .setPropertyValue(LANGUAGE, descrName.getLanguage())
            ;
        } else if (eventType == SHOW_ADD_OTHER_NAME) {
            descrNameViewModel.setPropertyValue(MODE, CREATE)
                .setPropertyValue(NAME_TYPE, REGULAR_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(TITLE_TEXT, ADD_OTHER_NAME_TITLE_TEXT)
                .setPropertyValue(DESCRIPTION_NAME_TYPE, "Other Name")
            ;
        } else if (eventType == SHOW_EDIT_OTHER_NAME) {
            DescrName descrName = event.getDescrName();
            descrNameViewModel.setPropertyValue(MODE, CREATE) // still creating, pattern not created yet
                .setPropertyValue(NAME_TYPE, REGULAR_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(TITLE_TEXT, EDIT_OTHER_NAME_TITLE_TEXT)
                .setPropertyValue(DESCRIPTION_NAME_TYPE, "Other Name")
                .setPropertyValue(NAME_TEXT, descrName.getNameText())
                .setPropertyValue(NAME_TYPE, descrName.getNameType())
                .setPropertyValue(CASE_SIGNIFICANCE, descrName.getCaseSignificance())
                .setPropertyValue(STATUS, descrName.getStatus())
                .setPropertyValue(MODULE, descrName.getModule())
                .setPropertyValue(LANGUAGE, descrName.getLanguage())
            ;
        }
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
    }

    @FXML
    private void showHistoryView(ActionEvent event) {
        LOG.info("Show Pattern History");
        this.historyButton.setSelected(true);
        contentBorderPane.setCenter(historyPane);
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
