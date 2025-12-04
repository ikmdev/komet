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
import dev.ikm.komet.kview.events.AddOtherNameToConceptEvent;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.EditConceptFullyQualifiedNameEvent;
import dev.ikm.komet.kview.events.OpenPropertiesPanelEvent;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicId;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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

    private Subscriber<OpenPropertiesPanelEvent> propsPanelOpen;

    public EditDescriptionsController() {}

    public EditDescriptionsController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    private ObjectProperty<PublicId> fqnPublicId = new SimpleObjectProperty<>();

    private ObjectProperty<PublicId> otherNamePublicId = new SimpleObjectProperty<>();

    private ViewProperties viewProperties;

    @Override
    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();

        // when the user opens the properties panel, the edit fqn and add other name
        // buttons should be able to populate their respective forms by passing the
        // appropriate PublicId through the event bus
        propsPanelOpen = evt -> {
            fqnPublicId.set(evt.getFqnPublicId());
        };
        eventBus.subscribe(conceptTopic, OpenPropertiesPanelEvent.class, propsPanelOpen);

        editFullyQualifiedNameButton.setOnMouseClicked(event ->
                eventBus.publish(conceptTopic, new EditConceptFullyQualifiedNameEvent(event,
                        EditConceptFullyQualifiedNameEvent.EDIT_FQN, fqnPublicId.get())));

        closePropertiesPanelButton.setOnMouseClicked(event ->
            eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(event,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES)));

        addOtherNameButton.setOnMouseClicked(event ->
            eventBus.publish(conceptTopic, new AddOtherNameToConceptEvent(event,
                AddOtherNameToConceptEvent.ADD_DESCRIPTION, otherNamePublicId.get())));
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

    public void updateModel(final ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }
}
