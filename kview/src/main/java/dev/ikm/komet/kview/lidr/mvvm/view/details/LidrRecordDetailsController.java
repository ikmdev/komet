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

import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.lidr.mvvm.model.*;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.AnalyteGroupViewModel;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.ViewModelHelper;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.*;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.lidr.mvvm.view.details.ResultConformanceDetailsController.RESULT_CONFORMANCE_DETAIL_FXML;
import static dev.ikm.komet.kview.lidr.mvvm.view.details.ResultConformanceDetailsController.RESULT_CONFORMANCE_RECORD;
import static dev.ikm.komet.kview.lidr.mvvm.view.details.SpecimenDetailsController.SPECIMEN_DETAIL_FXML;
import static dev.ikm.komet.kview.lidr.mvvm.view.details.SpecimenDetailsController.SPECIMEN_RECORD;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

public class LidrRecordDetailsController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(LidrRecordDetailsController.class);
    public static final String LIDR_RECORD_FXML = "one-lidr-record-detail.fxml";
    public static final String NOT_APPLICABLE = "N/A";

    @FXML
    private TitledPane lidrRecordTitledPane;

    @FXML
    private TitledPane analyteTitledPane;

    @FXML
    private GridPane analyteDetailsGridPane;

    @FXML
    private Text componentValueText;

    @FXML
    private Text dataResultsTypeValueText;

    @FXML
    private VBox dataResultsTypesVBox;

    @FXML
    private Text methodValueText;

    @FXML
    private VBox resultsConformanceVBox;

    @FXML
    private TitledPane resultsTitledPane;
    @FXML
    private TitledPane specimenTitledPane;

    @FXML
    private VBox specimentsVBox;

    @FXML
    private Text targetsValueText;

    @FXML
    private Text timeAspectValueText;

    @FXML
    private RowConstraints referenceRangesGridRow;
    @FXML
    private RowConstraints detectionLimitRowConstraints;

    @FXML
    private RowConstraints exampleUnitsRowConstraints;

    @InjectViewModel
    AnalyteGroupViewModel analyteGroupViewModel;

    /////////////////////////// other private variables ///////////////////////
    private EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
    private LidrRecord lidrRecord;

    private void closeAllTitledPanes() {
        lidrRecordTitledPane.setExpanded(false);
        analyteTitledPane.setExpanded(false);
        resultsTitledPane.setExpanded(false);
        specimenTitledPane.setExpanded(false);
    }

    @Override
    @FXML
    public void initialize() {
        clearView();

        closeAllTitledPanes();

        lidrRecord = analyteGroupViewModel.getPropertyValue(AnalyteGroupViewModel.LIDR_RECORD);
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
        AnalyteRecord analyteRecord = DataModelHelper.makeAnalyteRecord(lidrRecord.analyte().analyteId());
        lidrRecordTitledPane.setText(ViewModelHelper.findDescrNameText(analyteRecord.componentId(), "Unknown Result Interpretation") + " Result Interpretation");
        componentValueText.setText(ViewModelHelper.findDescrNameText(analyteRecord.componentId(), NOT_APPLICABLE));
        dataResultsTypeValueText.setText(ViewModelHelper.findDescrNameText(lidrRecord.dataResultsTypeId()));
        methodValueText.setText(ViewModelHelper.findDescrNameText(analyteRecord.methodTypeId(), NOT_APPLICABLE));
        if (lidrRecord.targets().size() > 0) {
            String targetsValue = lidrRecord.targets().stream().map(targetRecord -> ViewModelHelper.findDescrNameText(targetRecord.targetId())).collect(Collectors.joining(", "));
            targetsValueText.setText(targetsValue);
        } else {
            targetsValueText.setText(NOT_APPLICABLE);
        }
        timeAspectValueText.setText(ViewModelHelper.findDescrNameText(analyteRecord.timeAspectId(), NOT_APPLICABLE));

    }

    @Override
    public void clearView() {
        specimentsVBox.getChildren().clear();
        resultsConformanceVBox.getChildren().clear();
        componentValueText.setText("");
        dataResultsTypeValueText.setText("");
        methodValueText.setText("");
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
