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

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.events.OpenPropertiesPanelEvent;
import dev.ikm.komet.kview.events.ShowEditDescriptionPanelEvent;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class EditConceptController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(EditConceptController.class);


    private static final String EDIT_LABEL_PREFIX = "Edit: ";

    @FXML
    private Label conceptTitleLabel;

    @FXML
    private Button editDescriptionsButton;

    @FXML
    private Button editAxiomsButton;

    private Subscriber<OpenPropertiesPanelEvent> propsPanelOpen;

    private EvtBus eventBus;

    private UUID conceptTopic;


    public EditConceptController() { }

    public EditConceptController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    @Override
    public void initialize() {
        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        propsPanelOpen = evt -> {
            setConceptTitleLabel(evt.getConceptFQName());
        };
        eventBus.subscribe(conceptTopic, OpenPropertiesPanelEvent.class, propsPanelOpen);

        // when the user clicks the ADD DESCRIPTION button
        // on the Journal > Concept > Properties bump out > Edit tab
        // publish an event so that the concept detail window can
        // listen for it and swap the pane in the bump out to be the
        // add description form
        editDescriptionsButton.setOnMouseClicked(event -> {
            eventBus.publish(conceptTopic,
                    new ShowEditDescriptionPanelEvent(this,
                            ShowEditDescriptionPanelEvent.SHOW_EDIT_DESCRIPTION));
        });

        // the addAxiomsButton doesn't do anything at this time since it
        // is part of a mock-up for future functionality
    }

    private void setConceptTitleLabel(String conceptTitleLabel) {
        this.conceptTitleLabel.setText(EDIT_LABEL_PREFIX + conceptTitleLabel);
    }

    @Override
    public void updateView() { }

    @Override
    public void clearView() { }

    @Override
    public void cleanup() { }
}
