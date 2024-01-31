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

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class JournalCardController {
    private EvtBus journalEventBus;
    @FXML
    Pane cardPane;

    @FXML
    Text journalCardName;

    @FXML
    Label journalTimestampValue;

    @FXML
    Text journalCardConceptCount;
    @FXML
    public void initialize() {
        journalEventBus = EvtBusFactory.getInstance("DefaultEvtBus");
    }

    public void setJournalCardName(String journalCardName) {
        this.journalCardName.setText(journalCardName);
    }

    public void setJournalTimestampValue(String journalTimestampValue) {
        this.journalTimestampValue.setText(journalTimestampValue);
    }

    public void setJournalCardConceptCount(String journalCardConceptCount) {
        this.journalCardConceptCount.setText(journalCardConceptCount);
    }
}
