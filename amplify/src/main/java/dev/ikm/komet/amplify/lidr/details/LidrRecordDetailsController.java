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
import dev.ikm.komet.amplify.lidr.om.AnalyteRecord;
import dev.ikm.komet.amplify.lidr.om.LidrRecord;
import dev.ikm.komet.amplify.lidr.om.ResultConformanceRecord;
import dev.ikm.komet.amplify.lidr.om.SpecimenRecord;
import dev.ikm.komet.amplify.lidr.viewmodels.AnalyteGroupViewModel;
import dev.ikm.komet.amplify.mvvm.SimpleViewModel;
import dev.ikm.komet.amplify.mvvm.loader.*;
import dev.ikm.komet.amplify.viewmodels.FormViewModel;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.amplify.lidr.details.ResultConformanceDetailsController.*;
import static dev.ikm.komet.amplify.lidr.details.SpecimenDetailsController.SPECIMEN_DETAIL_FXML;
import static dev.ikm.komet.amplify.lidr.details.SpecimenDetailsController.SPECIMEN_RECORD;
import static dev.ikm.komet.amplify.lidr.viewmodels.AnalyteGroupViewModel.LIDR_RECORD;
import static dev.ikm.komet.amplify.lidr.viewmodels.ViewModelHelper.findDescrNameText;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.VIEW_PROPERTIES;

public class LidrRecordDetailsController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(LidrRecordDetailsController.class);
    public static final String LIDR_RECORD_FXML = "one-lidr-record-detail.fxml";
    public static final String NOT_APPLICABLE = "N/A";

    @FXML
    private TitledPane analyteTitledPane;

    @FXML
    private Text componentValueText;

    @FXML
    private Text dataResultTypeValueText;

    @FXML
    private Text dataResultsTypeValueText;

    @FXML
    private VBox dataResultsTypesVBox;

    @FXML
    private Text detectionLimitValueText;

    @FXML
    private Text exampleUnitsValueText;

    @FXML
    private Text methodValueText;

    @FXML
    private Text referenceRangesValueText;

    @FXML
    private VBox resultsConformanceVBox;

    @FXML
    private TitledPane resultsTitledPane;

    @FXML
    private VBox specimentsVBox;

    @FXML
    private Text targetsValueText;

    @FXML
    private Text timeAspectValueText;

    @InjectViewModel
    AnalyteGroupViewModel analyteGroupViewModel;

    /////////////////////////// other private variables ///////////////////////
    private EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
    private LidrRecord lidrRecord;
    @Override
    @FXML
    public void initialize() {
        clearView();
        lidrRecord = analyteGroupViewModel.getPropertyValue(LIDR_RECORD);
        if (lidrRecord != null) {
            // populate analyte fields
            updateView();

            // for each results build entry
            lidrRecord.resultConformances().forEach(resultConformanceRecord -> {
                // add one into view model
                SimpleViewModel resultDetailsViewModel = createResultDetailViewModel(resultConformanceRecord);
                Config config = new Config(ResultConformanceDetailsController.class.getResource(RESULT_CONFORMANCE_DETAIL_FXML))
                        .addNamedViewModel(new NamedVm("resultDetailsViewModel", resultDetailsViewModel));
                JFXNode<Pane, ResultConformanceDetailsController> resultNodeController = FXMLMvvmLoader.make(config);
                resultNodeController.controller().updateView();
                resultsConformanceVBox.getChildren().add(resultNodeController.node());
            });

            // For each specimen build entry
            lidrRecord.specimens().forEach(specimenRecord -> {
                // add one into view model
                SimpleViewModel specimenDetailsViewModel = createSpecimenDetailViewModel(specimenRecord);
                JFXNode<Pane, SpecimenDetailsController> specimenNodeController = FXMLMvvmLoader.make(SpecimenDetailsController.class.getResource(SPECIMEN_DETAIL_FXML),
                        new NamedVm("specimenDetailsViewModel", specimenDetailsViewModel));
                specimenNodeController.controller().updateView();
                specimentsVBox.getChildren().add(specimenNodeController.node());
            });

        }
    }
    private SimpleViewModel createResultDetailViewModel(ResultConformanceRecord resultConformanceRecord) {
        SimpleViewModel resultDetailsViewModel = new SimpleViewModel()
                .addProperty(CONCEPT_TOPIC, getConceptTopic())
                .addProperty(VIEW_PROPERTIES, getViewProperties())
                .addProperty(RESULT_CONFORMANCE_RECORD, resultConformanceRecord);
        return resultDetailsViewModel;

    }

    private SimpleViewModel createSpecimenDetailViewModel(SpecimenRecord specimenRecord) {
        SimpleViewModel specimenDetailsViewModel = new SimpleViewModel()
                .addProperty(CONCEPT_TOPIC, getConceptTopic())
                .addProperty(VIEW_PROPERTIES, getViewProperties())
                .addProperty(SPECIMEN_RECORD, specimenRecord);
        return specimenDetailsViewModel;

    }

    public ViewProperties getViewProperties() {
        return analyteGroupViewModel.getPropertyValue(VIEW_PROPERTIES);
    }
    private UUID getConceptTopic() {
        return analyteGroupViewModel.getPropertyValue(CONCEPT_TOPIC);
    }
    private String getDisplayText(ConceptEntity conceptEntity) {
        Optional<String> stringOptional = getViewProperties().calculator().getRegularDescriptionText(conceptEntity.nid());
        return stringOptional.orElse("");
    }

    @Override
    public void updateView() {
        AnalyteRecord analyteRecord = AnalyteRecord.make(lidrRecord.analyte().analyteId());
        componentValueText.setText(findDescrNameText(analyteRecord.componentId(), NOT_APPLICABLE));
        dataResultTypeValueText.setText(NOT_APPLICABLE);
        dataResultsTypeValueText.setText(NOT_APPLICABLE);
        detectionLimitValueText.setText(NOT_APPLICABLE);
        exampleUnitsValueText.setText(NOT_APPLICABLE);
        methodValueText.setText(findDescrNameText(analyteRecord.methodTypeId(), NOT_APPLICABLE));
        referenceRangesValueText.setText(NOT_APPLICABLE);
        targetsValueText.setText(NOT_APPLICABLE);
        timeAspectValueText.setText(findDescrNameText(analyteRecord.timeAspectId(), NOT_APPLICABLE));

    }

    @Override
    public void clearView() {
        componentValueText.setText("");
        dataResultTypeValueText.setText("");
        dataResultsTypeValueText.setText("");
        detectionLimitValueText.setText("");
        exampleUnitsValueText.setText("");
        methodValueText.setText("");
        referenceRangesValueText.setText("");
        targetsValueText.setText("");
        timeAspectValueText.setText("");
    }

    @Override
    public void cleanup() {

    }

    @Override
    public FormViewModel getViewModel() {
        return analyteGroupViewModel;
    }
}
