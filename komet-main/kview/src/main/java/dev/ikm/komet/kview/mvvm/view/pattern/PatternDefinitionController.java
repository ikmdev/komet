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

import static dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent.PATTERN_DEFINITION;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.DEFINITION_CONFIRMATION;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_INVALID;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.MEANING_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PURPOSE_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STATE_MACHINE;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.model.PatternDefinition;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternDefinitionViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternDefinitionController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternDefinitionController.class);

    @InjectViewModel
    private PatternDefinitionViewModel patternDefinitionViewModel;

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;

    @FXML
    private VBox purposeAndMeaningContainer;

    @FXML
    private Button doneButton;

    @FXML
    private void clearView() {
        patternDefinitionViewModel.setPropertyValue(PURPOSE_ENTITY, null);
        patternDefinitionViewModel.setPropertyValue(MEANING_ENTITY, null);
        patternDefinitionViewModel.save(true);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        LOG.info("cancel");
    }

    @FXML
    private void initialize() {
        ChangeListener fieldsValidationListener = (obs, oldValue, newValue) -> {
            patternDefinitionViewModel.validate();
            patternDefinitionViewModel.setPropertyValue(IS_INVALID, patternDefinitionViewModel.hasErrorMsgs());
        };

        KLComponentControl purposeComponentControl = KLComponentControlFactory.createTypeAheadComponentControl(patternPropertiesViewModel.getViewProperties().calculator());
        KLComponentControl meaningComponentControl = KLComponentControlFactory.createTypeAheadComponentControl(patternPropertiesViewModel.getViewProperties().calculator());

        purposeComponentControl.setTitle("Purpose");
        meaningComponentControl.setTitle("Meaning");

        doneButton.disableProperty().bind(patternDefinitionViewModel.getProperty(IS_INVALID));

        ObjectProperty<EntityProxy> purposeProp = patternDefinitionViewModel.getProperty(PURPOSE_ENTITY);
        ObjectProperty<EntityProxy> meaningProp = patternDefinitionViewModel.getProperty(MEANING_ENTITY);

        purposeProp.addListener(fieldsValidationListener);
        meaningProp.addListener(fieldsValidationListener);

        purposeComponentControl.entityProperty().bindBidirectional(purposeProp);
        meaningComponentControl.entityProperty().bindBidirectional(meaningProp);

        purposeAndMeaningContainer.getChildren().addAll(purposeComponentControl, meaningComponentControl);
    }

    /**
     * cancel editing, close the panel
     * @param actionEvent
     */
    @FXML
    private void onCancel(ActionEvent actionEvent) {
        //publish close env
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        clearView();
    }

    /**
     * completing the action of adding a pattern definition
     * firing an event so that values will be saved in the viewModel
     * @param actionEvent
     */
    @FXML
    public void onDone(ActionEvent actionEvent) {
        // save calls validate
        patternDefinitionViewModel.save();

        PatternDefinition patternDefinition = new PatternDefinition(
                patternDefinitionViewModel.getPropertyValue(PURPOSE_ENTITY),
                patternDefinitionViewModel.getPropertyValue(MEANING_ENTITY),
                null);

        StateMachine patternSM = patternPropertiesViewModel.getPropertyValue(STATE_MACHINE);
        patternSM.t("definitionsDone");

        // publish and event so that we can go to the definition confirmation screen
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC),
                new PropertyPanelEvent(actionEvent.getSource(), DEFINITION_CONFIRMATION));

        // publish form submission data
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC),
                new PatternDefinitionEvent(actionEvent.getSource(), PATTERN_DEFINITION, patternDefinition));
    }
}
