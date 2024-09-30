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
package dev.ikm.komet.kview.mvvm.view.reasoner;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.reasoner.CloseReasonerPanelEvent.CLOSE;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.reasoner.CloseReasonerPanelEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NextGenReasonerController {

    @FXML
    private TitledPane classificationDateTitledPane;

    @FXML
    private TitledPane conceptSetSizeTitlePane;

    @FXML
    private TitledPane equivalenciesTitlePane;


    @FXML
    private VBox inferredChangesVBox;

    @FXML
    private TitledPane inferredChangesTitlePane;

    private static final Logger LOG = LoggerFactory.getLogger(NextGenReasonerController.class);

    private static final String REASONER_INFERRED_CHANGE_FXML = "reasoner-inferred-change.fxml";


    public NextGenReasonerController() {

    }

    @FXML
    private void initialize() {
        showResults();
    }

    /**
     * populate the reasoner results
     */
    public void showResults() {
        //TODO this is a mockup, future effort will populate will real results
        int mockInferredChangesCount = 5;
        setInferredChangesCount(mockInferredChangesCount);
        inferredChangesVBox.getChildren().addAll(
                IntStream.range(0, mockInferredChangesCount).mapToObj(i -> FXMLMvvmLoader
                        .make(NextGenReasonerInferredChangeController.class.getResource(REASONER_INFERRED_CHANGE_FXML)).node()
                ).collect(Collectors.toList()));
        setEquivalenciesCount(455);
        setConceptSetSizeCount(20020);
    }

    @FXML
    private void closeReasoner(ActionEvent actionEvent) {
        EvtBusFactory.getDefaultEvtBus().publish(JOURNAL_TOPIC, new CloseReasonerPanelEvent(actionEvent.getSource(), CLOSE));
    }

    private void setInferredChangesCount(int inferredChangesCount) {
        inferredChangesTitlePane.setText("Inferred changes: %,d".formatted(inferredChangesCount));
    }

    private void setEquivalenciesCount(int equivalenciesCount) {
        equivalenciesTitlePane.setText("Equivalencies: %,d".formatted(equivalenciesCount));
    }

    private void setConceptSetSizeCount(int conceptSetSizeCount) {
        conceptSetSizeTitlePane.setText("Concept Set Size: %,d".formatted(conceptSetSizeCount));
    }
}
