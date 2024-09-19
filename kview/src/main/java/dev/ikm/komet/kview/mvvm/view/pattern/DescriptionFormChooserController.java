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

import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_FQN;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_OTHER_NAME;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_FQN;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_OTHER_NAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel.DISPLAY_FQN_EDIT_MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel.DISPLAY_OTHER_NAME_EDIT_MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

import java.util.UUID;

public class DescriptionFormChooserController {

    @FXML
    private Button addEditFqnButton;

    @FXML
    private Button addOtherNameButton;

    @FXML
    private Button closePropertiesButton;

    @FXML
    private Label continueEditingLabel;

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;

    @FXML
    public void initialize() {
        patternPropertiesViewModel.getProperty(DISPLAY_FQN_EDIT_MODE).addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && (boolean) newValue) {
                addEditFqnButton.setText("EDIT FULLY QUALIFIED NAME");
                continueEditingLabel.setText("Continue Editing Descriptions?");
            } else {
                addEditFqnButton.setText("ADD FULLY QUALIFIED NAME");
                continueEditingLabel.setText("");
            }
        }));
        // default to blank when we first start adding descriptions
        continueEditingLabel.setText("");
    }

    @FXML
    private void showFqnForm(ActionEvent actionEvent) {
        EvtType showFqnEvt = SHOW_ADD_FQN;
        if (actionEvent.getSource() instanceof Button button && button.getText().startsWith("EDIT")) {
            showFqnEvt = SHOW_EDIT_FQN;
        }
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), showFqnEvt));
    }

    public UUID getPatternTopic() {
        return patternPropertiesViewModel.getPropertyValue(PATTERN_TOPIC);
    }
    @FXML
    private void showOtherNameForm(ActionEvent actionEvent) {
        EvtType showOtherNameEvt = SHOW_ADD_OTHER_NAME;
        if (actionEvent.getSource() instanceof Button button && button.getText().startsWith("EDIT")) {
            showOtherNameEvt = SHOW_EDIT_OTHER_NAME;
        }
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), showOtherNameEvt));
    }

    @FXML
    private void closePropertiesPanel(ActionEvent actionEvent) {
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
    }
}
