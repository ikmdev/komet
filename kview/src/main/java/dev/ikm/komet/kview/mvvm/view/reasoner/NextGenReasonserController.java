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

import static dev.ikm.komet.kview.fxutils.TitledPaneHelper.putArrowOnRight;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NextGenReasonserController {

    @FXML
    private TitledPane classificationDateTitledPane;

    @FXML
    private VBox classificationDateVBox;

    private static final Logger LOG = LoggerFactory.getLogger(NextGenReasonserController.class);

    private static final String REASONER_RESULT_FXML = "reasoner-entry.fxml";


    public NextGenReasonserController() {

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
        JFXNode<Pane, NextGenReasonerResultController> reasonerResultJFXNode = FXMLMvvmLoader
                .make(NextGenReasonerResultController.class.getResource(REASONER_RESULT_FXML));
        NextGenReasonerResultController controller = reasonerResultJFXNode.controller();

        classificationDateVBox.getChildren().add(reasonerResultJFXNode.node());
    }

}
