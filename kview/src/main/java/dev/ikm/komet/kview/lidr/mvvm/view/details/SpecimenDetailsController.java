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
package dev.ikm.komet.kview.lidr.mvvm.view.details;

import dev.ikm.komet.kview.lidr.mvvm.model.SpecimenRecord;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.ViewModelHelper;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.ViewModelHelper.findDescrNameText;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CONCEPT_TOPIC;

public class SpecimenDetailsController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(SpecimenDetailsController.class);
    public static final String SPECIMEN_DETAIL_FXML = "specimen-detail.fxml"; // grid pane
    public static final String SPECIMEN_RECORD = "resultsConformanceRecord";

    @FXML
    private Text systemValueText;

    @FXML
    private Text nameValueText;

    @FXML
    private Text specimentCollectionMethodValueText;

    @InjectViewModel
    SimpleViewModel specimenDetailsViewModel;

    /////////////////////////// other private variables ///////////////////////
    private EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
    private SpecimenRecord specimenRecord;

    @Override
    @FXML
    public void initialize() {
        clearView();
        specimenRecord = specimenDetailsViewModel.getPropertyValue(SPECIMEN_RECORD);
        updateView();
    }

    private UUID getConceptTopic() {
        return specimenDetailsViewModel.getPropertyValue(CONCEPT_TOPIC);
    }

    @Override
    public void updateView() {
        systemValueText.setText(ViewModelHelper.findDescrNameText(specimenRecord.systemId()));
        specimentCollectionMethodValueText.setText(ViewModelHelper.findDescrNameText(specimenRecord.methodTypeId()));
        // Name of specimen
        nameValueText.setText(ViewModelHelper.findDescrNameText(specimenRecord.specimenId()));

    }

    @Override
    public void clearView() {
        systemValueText.setText("");
        nameValueText.setText("");
        specimentCollectionMethodValueText.setText("");
    }

    @Override
    public void cleanup() {

    }

    @Override
    public SimpleViewModel getViewModel() {
        return specimenDetailsViewModel;
    }
}
