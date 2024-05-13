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
package dev.ikm.komet.amplify.lidr.details;

import dev.ikm.komet.amplify.commons.AbstractBasicController;
import dev.ikm.komet.amplify.lidr.om.ResultConformanceRecord;
import dev.ikm.komet.amplify.mvvm.SimpleViewModel;
import dev.ikm.komet.amplify.mvvm.loader.InjectViewModel;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.amplify.lidr.viewmodels.ViewModelHelper.findDescrNameText;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.CONCEPT_TOPIC;

public class ResultConformanceDetailsController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(ResultConformanceDetailsController.class);
    public static final String RESULT_CONFORMANCE_DETAIL_FXML = "result-conformance-detail.fxml"; // grid pane
    public static final String RESULT_CONFORMANCE_RECORD = "resultsConformanceRecord";

    @FXML
    private Text commentsValueText;

    @FXML
    private Text dateResultTypeValueText;

    @FXML
    private VBox allowableResultsVBox;

    @FXML
    private Text nameValueText;

    @FXML
    private Text scaleTypeValueText;

    @InjectViewModel
    SimpleViewModel resultDetailsViewModel;

    /////////////////////////// other private variables ///////////////////////
    private EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
    private ResultConformanceRecord resultsConformanceRecord;

    @Override
    @FXML
    public void initialize() {
        clearView();
        resultsConformanceRecord = resultDetailsViewModel.getPropertyValue(RESULT_CONFORMANCE_RECORD);


    }

    private UUID getConceptTopic() {
        return resultDetailsViewModel.getPropertyValue(CONCEPT_TOPIC);
    }

    @Override
    public void updateView() {
        commentsValueText.setText("");
        nameValueText.setText(findDescrNameText(resultsConformanceRecord.resultConformanceId()));
        scaleTypeValueText.setText(findDescrNameText(resultsConformanceRecord.scaleId()));
        // Todo: populate allowable results
    }

    @Override
    public void clearView() {
        commentsValueText.setText("");
        allowableResultsVBox.getChildren().clear();
        nameValueText.setText("");
        scaleTypeValueText.setText("");
    }
    private TextFlow createResultsTextFlow(String resultText) {
        Text text = new Text(resultText);
        text.getStyleClass().add("result-field-value");
        return new TextFlow(text);
    }
    @Override
    public void cleanup() {

    }

    @Override
    public SimpleViewModel getViewModel() {
        return resultDetailsViewModel;
    }
}
