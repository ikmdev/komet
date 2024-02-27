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

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.events.AddOtherNameToConceptEvent;
import dev.ikm.komet.amplify.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.amplify.events.EditConceptFullyQualifiedNameEvent;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditDescriptionsController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(EditDescriptionsController.class);

    @FXML
    private Button editFullyQualifiedNameButton;

    @FXML
    private Button closePropertiesPanelButton;

    @FXML
    private Button addOtherNameButton;

    private EvtBus eventBus;

    private UUID conceptTopic;

    public EditDescriptionsController() {}

    public EditDescriptionsController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @Override
    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();

        editFullyQualifiedNameButton.setOnMouseClicked(event ->
                eventBus.publish(conceptTopic, new EditConceptFullyQualifiedNameEvent(event,
                        EditConceptFullyQualifiedNameEvent.EDIT_FQN)));

        closePropertiesPanelButton.setOnMouseClicked(event ->
            eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(event,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES)));

        addOtherNameButton.setOnMouseClicked(event ->
            eventBus.publish(conceptTopic, new AddOtherNameToConceptEvent(event,
                AddOtherNameToConceptEvent.ADD_DESCRIPTION)));
    }


    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {

    }

    @Override
    public void cleanup() {

    }
}
