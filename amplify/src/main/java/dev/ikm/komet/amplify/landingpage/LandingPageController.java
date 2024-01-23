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
package dev.ikm.komet.amplify.landingpage;

import static dev.ikm.komet.amplify.events.AmplifyTopics.JOURNAL_TOPIC;

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.events.CreateJournalEvent;
import dev.ikm.komet.framework.events.EvtBus;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

public class LandingPageController implements BasicController {
    @FXML
    private ScrollPane journalProjectCardScrollPane;

    @FXML
    private FlowPane gridViewFlowPane;

    @FXML
    ToggleButton settingsToggleButton;

    @FXML
    Button newProjectJournalButton;

    private EvtBus journalEventBus;

    public ToggleButton getSettingsToggleButton() {
        return settingsToggleButton;
    }

    public void setSettingsToggleButton(ToggleButton settingsToggleButton) {
        this.settingsToggleButton = settingsToggleButton;
    }

    @FXML
    @Override
    public void initialize() {
        clearView();
        journalEventBus = EvtBus.getInstance("AmplifyEvtBus");

        journalProjectCardScrollPane.viewportBoundsProperty().addListener((ov, oldBounds, bounds) -> {
            gridViewFlowPane.setPrefWidth(bounds.getWidth());
            gridViewFlowPane.setPrefHeight(bounds.getHeight());
        });

        newProjectJournalButton.setOnAction(event ->  {
            // publish the event that the new journal button was pressed
            journalEventBus.publish(JOURNAL_TOPIC,
                    new CreateJournalEvent(newProjectJournalButton, CreateJournalEvent.CREATE_JOURNAL));
        });
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
