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
package dev.ikm.komet.artifact;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ArtifactExportController is responsible for triggering an export class in Tinkar-Core. This class
 * utilizes the ArtifactExport.fxml file that can be located in resources. Once a user clicks the 'export all'
 * button a outputted protobuf zip will be generated with all database data.
 */

public class ArtifactExportController {

    private static final Logger LOG = LoggerFactory.getLogger(ArtifactExportController.class);

    @FXML
    private Button exportFilteredButton;

    @FXML
    private Button exportAllButton;

    @FXML
    private ComboBox<ConceptEntity<ConceptEntityVersion>> handlePathComboBox;

    @FXML
    private RadioButton filteredRadioButton;

    @FXML
    private RadioButton exportAllRadioButton;

    @FXML
    private ListView<PatternEntity<PatternEntityVersion>> membershipPatternSelectionListView;

    @FXML
    private DatePicker dateRangeFrom;

    @FXML
    private DatePicker dateRangeTo;

    @FXML
    private Spinner<LocalTime> timeRangeFromSpinner;

    @FXML
    private Spinner<LocalTime> timeRangeToSpinner;

    @FXML
    private Label exportLabel;

    @FXML
    private ToggleGroup exportGroup;

    /**
     * This method is responsible for triggering the export from entities to a protobuf zip file.
     * @throws IOException if the export fails to succeed
     */
    @FXML
    public void initialize() {
        setupPathComboBox();

        setupMembershipPatternSelectionListView();

        setupDateRangeSpinner();
    }

    @FXML
    void handleRadioButton(ActionEvent event) throws IOException {

        if (filteredRadioButton.isSelected()) {
            setFilteredRadioAndExportButton(false);
            exportLabel.setText("Exporting filtered selection");

        } else if (exportAllRadioButton.isSelected()) {
            setFilteredRadioAndExportButton(true);
            exportLabel.setText("Exporting all data");
        }
    }

    @FXML
    void handleExport(ActionEvent event) throws IOException {

        //Creating a file chooser so the user can choose a location for the outputted data
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export file name as");

        //Making sure the zip is the only thing that is zipped up
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip"));

        //Date formatter for the desired date template
        String pattern = "yyyyMMdd-HHmm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String initialFileName = "komet-%s.zip".formatted(simpleDateFormat.format(new Date()));
        fileChooser.setInitialFileName(initialFileName);

        //Triggers the file chooser screen (where a user can choose a location)
        File exportFile = fileChooser.showSaveDialog(getExportButton().getScene().getWindow());
        if(exportFile == null){
            //User hits cancel
            return;
        }

        // Proceed to export
        getExportButton().setDisable(false);
        if (exportGroup.getSelectedToggle().equals(exportAllRadioButton)) {
            exportAll(exportFile);
        } else {
            exportFilteredSelection(exportFile);
        }
    }

    private void setupPathComboBox(){

        handlePathComboBox.setConverter(new StringConverter<ConceptEntity<ConceptEntityVersion>>() {
            @Override
            public String toString(ConceptEntity<ConceptEntityVersion> object) {
                return object.description();
            }

            @Override
            public ConceptEntity<ConceptEntityVersion> fromString(String string) {
                return null;
            }
        });


        handlePathComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<ConceptEntity<ConceptEntityVersion>> call(ListView<ConceptEntity<ConceptEntityVersion>> conceptEntityListView) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(ConceptEntity<ConceptEntityVersion> conceptEntityVersionConceptEntity, boolean b) {
                        super.updateItem(conceptEntityVersionConceptEntity, b);
                        if (conceptEntityVersionConceptEntity != null) {
                            setText(conceptEntityVersionConceptEntity.description());
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        });
        handlePathComboBox.getItems().addAll(findAllPaths());
    }

    private void setupMembershipPatternSelectionListView(){

        membershipPatternSelectionListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        membershipPatternSelectionListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<PatternEntity<PatternEntityVersion>> call(ListView<PatternEntity<PatternEntityVersion>> patternEntityListView) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(PatternEntity<PatternEntityVersion> patternEntityVersionPatternEntity, boolean b) {
                        super.updateItem(patternEntityVersionPatternEntity, b);
                        if (patternEntityVersionPatternEntity != null) {
                            setText(patternEntityVersionPatternEntity.description());
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        });
    }

    private void setupDateRangeSpinner(){
        dateRangeFrom.setValue(LocalDate.now().minusDays(1L));
        dateRangeTo.setValue(LocalDate.now());

        ObservableList<LocalTime> observableList = FXCollections.observableList(generateLocalTimes());

        SpinnerValueFactory<LocalTime> spinnerValueFactoryDateRangeFrom = new SpinnerValueFactory.ListSpinnerValueFactory<>(observableList);
        SpinnerValueFactory<LocalTime> spinnerValueFactoryDateRangeTo = new SpinnerValueFactory.ListSpinnerValueFactory<>(observableList);

        timeRangeFromSpinner.setValueFactory(spinnerValueFactoryDateRangeFrom);
        timeRangeToSpinner.setValueFactory(spinnerValueFactoryDateRangeTo);

        timeRangeFromSpinner.getValueFactory().setValue(LocalTime.of(12, 0));
        timeRangeToSpinner.getValueFactory().setValue(LocalTime.of(23,59));
    }

    private void exportAll(File exportFile){
        //Asynchronously starting a thread to run the export to protobuf
        CompletableFuture.supplyAsync( () -> {
            try {
                //Calls a tinkar-core class that is responsible for transforming entities from the database to
                ExportEntitiesToProtobufFile exportEntitiesToProtobufFile = new ExportEntitiesToProtobufFile(exportFile);
                exportEntitiesToProtobufFile.compute();
            } catch(Throwable e) {
                throw new RuntimeException(e);
            } finally {
                //Running a JavaFX method on the application thread using platform.runlater
                Platform.runLater(() -> {
                    getExportButton().setDisable(false);
                });
            }
            return null;
        });
    }

    private void exportFilteredSelection(File exportFile) {

        // TODO: Need a method from exportEntitiesToProtobufFile that will collect filtered selection

        LocalDate localDateDateRangeFrom = dateRangeFrom.getValue();
        LocalDate localDateDateRangeTo = dateRangeTo.getValue();
        LocalTime localTimeRangeFromSpinner = timeRangeFromSpinner.getValue();
        LocalTime localTimeRangeToSpinner = timeRangeToSpinner.getValue();
        String dateTimeRangeFrom = localDateDateRangeFrom.toString() + " " + localTimeRangeFromSpinner.toString();
        String  dateTimeRangeTo = localDateDateRangeTo.toString() + " " + localTimeRangeToSpinner.toString();
        long epocMilliDateTimeSelectedRangeFrom = timeRangeToQuery(dateTimeRangeFrom);
        long epocMilliDateTimeSelectedRangeTo = timeRangeToQuery(dateTimeRangeTo);


        LOG.info("Path selected: " + handlePathComboBox.getSelectionModel().getSelectedItem().description());
        LOG.info("membership Pattern selected: " + membershipPatternSelectionListView.getSelectionModel()
                .getSelectedItems().stream().map(EntityFacade::description).toList());
        LOG.info("Time range from: " + dateTimeRangeFrom);
        LOG.info("Time range to: "+ dateTimeRangeTo);
        LOG.info("EpocMilli for DateTime selectedRange From: " + epocMilliDateTimeSelectedRangeFrom);
        LOG.info("EpocMilli for DateTime selectedRange To: " + epocMilliDateTimeSelectedRangeTo);
    }

    public Button getExportButton() {
        return exportAllButton;
    }

    public void handleSelectivePathExport(ActionEvent event) {
        //Getting selected item from the combobox
        LOG.info("Path Id: " + handlePathComboBox.getSelectionModel().getSelectedItem().nid());
        LOG.info("Patterns: "+ findAllMembershipPatterns(handlePathComboBox.getSelectionModel().getSelectedItem().nid()));
        membershipPatternSelectionListView.getItems().clear();
        membershipPatternSelectionListView.getItems().addAll(findAllMembershipPatterns(handlePathComboBox.getSelectionModel().getSelectedItem().nid()));
    }

    private List<ConceptEntity<ConceptEntityVersion>> findAllPaths(){
        //List of Concepts that represent available Paths in the data
        List<ConceptEntity<ConceptEntityVersion>> paths = new ArrayList<>();
        //Get all Path semantics from the Paths Pattern
        int[] pathSemanticNids = EntityService.get().semanticNidsOfPattern(TinkarTerm.PATHS_PATTERN.nid());
        //For each Path semantic get the concept that the semantic is referencing
        for (int pathSemanticNid : pathSemanticNids) {
            SemanticEntity<SemanticEntityVersion> semanticEntity = Entity.getFast(pathSemanticNid);
            int pathConceptNid = semanticEntity.referencedComponentNid();
            paths.add(EntityService.get().getEntityFast(pathConceptNid));
        }
        return paths;
    }

    private List<PatternEntity<PatternEntityVersion>> findAllMembershipPatterns(int pathConceptNid) {
        List<PatternEntity<PatternEntityVersion>> membershipPatterns = new ArrayList<>();
        PrimitiveData.get()
                .forEachPatternNid(patternNid -> {
                    /*
                     *  Use the STAMP calculator to get the latest version of the pattern based on the pattern's
                     *  Status == Active, and it's Time relative to the provided path (pathConceptNid)
                     */
                    StampCoordinateRecord stampCoordinateRecord = StampCoordinateRecord.make(StateSet.ACTIVE, pathConceptNid);
                    Latest<PatternEntityVersion> patternEntityVersionLatest = stampCoordinateRecord.stampCalculator().latest(patternNid);

                    /*
                     * Using the latest version of the current pattern, check to see if the purpose of the pattern
                     * is equal to TinkarTerm.MEMBERSHIP_SEMANTIC.
                     */
                    if (patternEntityVersionLatest.isPresent() && patternEntityVersionLatest.get().active()) {
                        PatternEntityVersion patternEntityVersion = patternEntityVersionLatest.get();
                        if (patternEntityVersion.semanticPurposeNid() == TinkarTerm.MEMBERSHIP_SEMANTIC.nid()) {
                            membershipPatterns.add(Entity.getFast(patternNid));
                        }
                    }
                });
        return membershipPatterns;
    }

    public void setFilteredRadioAndExportButton( boolean disable) {
        handlePathComboBox.setDisable(disable);
        membershipPatternSelectionListView.setDisable(disable);
        dateRangeFrom.setDisable(disable);
        timeRangeFromSpinner.setDisable(disable);
        dateRangeTo.setDisable(disable);
        timeRangeToSpinner.setDisable(disable);
    }

    private long timeRangeToQuery(String timeRangeSelected) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime parsedDatetime = LocalDateTime.parse(timeRangeSelected, dateTimeFormatter);
        Instant instant = parsedDatetime.toInstant(ZoneOffset.UTC);
        return instant.toEpochMilli();
    }

    private List<LocalTime> generateLocalTimes(){
        return IntStream.range(0, 24)
                .boxed()
                .flatMap(hour -> IntStream.range(0,60)
                        .mapToObj(minute -> LocalTime.of(hour, minute)))
                .collect(Collectors.toList());
    }

}

