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
package dev.ikm.komet.amplify.properties;

import static dev.ikm.komet.amplify.events.AmplifyTopics.CONCEPT_TOPIC;

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.events.AddDescriptionToConceptEvent;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditConceptController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(EditConceptController.class);

    private static final String ADD_DESCRIPTION_BUTTON_TEXT = "ADD DESCRIPTION";

    private static final String ADD_AXIOM_BUTTON_TEXT = "ADD AXIOM";

    @FXML
    private Label conceptTitleLabel;

    @FXML
    private Button addDescriptionsButton;

    @FXML
    private Button addAxiomsButton;

    private EvtBus eventBus;

    @FXML
    @Override
    public void initialize() {
        clearView();

        eventBus = EvtBusFactory.getInstance(EvtBus.class);

        // FIXME: for the mock-up, enter a dummy value of the concept title
        //  for the future, the concept title will populate this Label text
        //setConceptTitleLabel("Edit: Tetrology of Fallout");

        setAddDescriptionsButton(ADD_DESCRIPTION_BUTTON_TEXT);
        setAddAxiomsButton(ADD_AXIOM_BUTTON_TEXT);

        // when the user clicks the ADD DESCRIPTION button
        // on the Journal > Concept > Properties bump out > Edit tab
        // publish an event so that the concept detail window can
        // listen for it and swap the pane in the bump out to be the
        // add description form
        addDescriptionsButton.setOnMouseClicked(event -> {
            eventBus.publish(CONCEPT_TOPIC,
                    new AddDescriptionToConceptEvent(this,
                            AddDescriptionToConceptEvent.ADD_DESCRIPTION));
        });

        // the addAxiomsButton doesn't do anything at this time since it
        // is part of a mock-up for future functionality
    }

    public void setConceptTitleLabel(String conceptTitleText) {
        this.conceptTitleLabel.setText(conceptTitleText);
    }

    public void setAddDescriptionsButton(String addDescriptionsButtonText) {
        this.addDescriptionsButton.setText(addDescriptionsButtonText);
    }

    public void setAddAxiomsButton(String addAxiomsButtonText) {
        this.addAxiomsButton.setText(addAxiomsButtonText);
    }

    @Override
    public void updateView() { }

    @Override
    public void clearView() { }

    @Override
    public void cleanup() { }
}
