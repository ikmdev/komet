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
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_FIELDS;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.UUID;

public class DescriptionFormChooserController {

    @FXML
    private Button addEditFqnButton;

    @FXML
    private Button addOtherNameButton;

    @FXML
    private Button closePropertiesButton;

    private UUID patternTopic;

    public DescriptionFormChooserController(UUID patternTopic) {
        this.patternTopic = patternTopic;
    }

    @FXML
    private void showFqnForm(ActionEvent actionEvent) {
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(patternTopic, new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_ADD_FQN));
    }

    @FXML
    private void showOtherNameForm(ActionEvent actionEvent) {
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(patternTopic, new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_ADD_OTHER_NAME));
    }

    @FXML
    private void closePropertiesPanel(ActionEvent actionEvent) {
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(patternTopic, new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
    }
}
