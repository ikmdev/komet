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
package dev.ikm.komet.kview.mvvm.view.export;

import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ExportDateTimePopOverEvent;
import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.aggregator.TemporalEntityAggregator;
import dev.ikm.tinkar.fhir.transformers.FhirCodeSystemTransform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.events.ExportDateTimePopOverEvent.*;

public class ExportDatasetController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportDatasetController.class);
    private static final String CURRENT_DATE = "Current Date";
    private static final String CUSTOM_RANGE = "Custom Range";

    private EvtBus exportDatasetEventBus;

    private UUID exportTopic;

    @FXML
    private ComboBox<ConceptEntity<ConceptEntityVersion>> snomedCTComboBox;

    @FXML
    private ComboBox<ConceptEntity<ConceptEntityVersion>> snomedUKComboBox;

    @FXML
    private ComboBox<ConceptEntity<ConceptEntityVersion>> loincComboBox;

    @FXML
    private ComboBox<ConceptEntity<ConceptEntityVersion>> rxNormComboBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button exportButton;

    @FXML
    private ComboBox<String> timePeriodComboBox;

    @FXML
    private FlowPane tagsFlowpane;

    @FXML
    private TextField commentTextField;

    @FXML
    private VBox modulesVBox;

    @FXML
    private HBox snomedCTHbox;

    @FXML
    private HBox snomedUKHbox;

    @FXML
    private HBox loinCHbox;

    @FXML
    private HBox rxNormHbox;

    @FXML
    private HBox dateTimePickerHbox;

    @FXML
    private Label dateTimeFromLabel;
    @FXML
    private Label dateTimeToLabel;

    @FXML
    ProgressBar exportProgressBar;
    @FXML
    Label exportStatusMessage;

    //////////////////////// private variables /////////////////////////////
    private ViewProperties viewProperties;

    private DatePicker datePicker;

    private Subscriber<ExportDateTimePopOverEvent> fromDatePopOverSubscriber;

    private PopOver fromDateTimePopOver;
    private PopOver toDateTimePopOver;

    private long customFromEpochMillis;
    private long customToEpochMillis;

    private Stage addTagStage = null;

    protected static final String FHIR_TIME_EXPORT_PICKER_FXML_FILE = "fhir-time-export-picker.fxml";

    private static final String CURRENT_DATE_TIME_RANGE_FROM = "01/01/2022, 12:00 AM";


    public ExportDatasetController() {
    }

    @Override
    @FXML
    public void initialize() {
        exportDatasetEventBus = EvtBusFactory.getDefaultEvtBus();
        clearView();
        setupModulesTags();
        setupDateTimeExportComboBox();
        setupCustomDateRangeLabel();

        exportTopic = UUID.randomUUID();

        // Create PopOver for From Date
        fromDateTimePopOver = createPopover(exportTopic, FROM_DATE, (epochTime) -> {
            this.customFromEpochMillis = epochTime;
            dateTimeFromLabel.setText(transformLocalDateTimeToStr(transformEpochMillisToLocalDateTime(epochTime)));
        });

        // Create PopOver for To Date
        toDateTimePopOver = createPopover(exportTopic, TO_DATE, (epochTime) -> {
            this.customToEpochMillis = epochTime;
            dateTimeToLabel.setText(transformLocalDateTimeToStr(transformEpochMillisToLocalDateTime(epochTime)));
        });

        tagsFlowpane.setDisable(true);
        commentTextField.setDisable(true);

    }

    private PopOver createPopover(UUID exportTopic, final int rangeType, Consumer<Long> dateTimeConsumer) {

        JFXNode<BorderPane, TimeAndDatePickerController> pickerNodeController = FXMLMvvmLoader
                .make(getClass().getResource(FHIR_TIME_EXPORT_PICKER_FXML_FILE),
                        new TimeAndDatePickerController(exportTopic, rangeType));

        BorderPane bp = pickerNodeController.node();
        PopOver dateTimePopOver = new PopOver(bp);
        DatePicker datePicker = new DatePicker();
        DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
        final Node popupContent = datePickerSkin.getPopupContent();
        popupContent.setId("calendar-node");
        bp.setLeft(popupContent);
        datePicker.valueProperty().addListener((observableValue, oldDate, newDate) -> {
            popupContent.setUserData(newDate);
        });
        popupContent.setUserData(LocalDate.now());

        Subscriber<ExportDateTimePopOverEvent> datePopOverSubscriber = evt -> {
            if (evt.getRangeType() != rangeType) return;

            if (evt.getEventType() == CANCEL_POP_OVER) {
                dateTimePopOver.hide();
                return;
            }
            if (evt.getEventType() == APPLY_POP_OVER) {
                // Update the main from date.
                dateTimeConsumer.accept(evt.getEpochMillis());
                dateTimePopOver.hide();
            }
        };
        exportDatasetEventBus.subscribe(exportTopic, ExportDateTimePopOverEvent.class, datePopOverSubscriber);
        return dateTimePopOver;

    }

    public void setupCustomDateRangeLabel() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        String customDateRangeFrom = transformLocalDateTimeToStr(startOfDay);
        dateTimeFromLabel.setText(customDateRangeFrom);
        String customDateRangeTo = transformLocalDateTimeToStr(LocalDateTime.now());
        dateTimeToLabel.setText(customDateRangeTo);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        modulesVBox.getChildren().clear();
    }

    @Override
    public void cleanup() {

    }

    @FXML
    private void handleCancelButtonEvent(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        tagsFlowpane.getChildren().clear();
    }

    @FXML
    void handleExport(ActionEvent event){

        //Creating a file chooser so the user can choose a location for the outputted data
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Fhir File Exporter");

        //Making sure the zip is the only thing that is zipped up
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fhir Json Files", "*.json"));

        //Date formatter for the desired date template
        String pattern = "yyyy-MM-dd-HHmm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String initialFileName = "fhir-%s.json".formatted(simpleDateFormat.format(new Date()));
        fileChooser.setInitialFileName(initialFileName);

        //Triggers the file chooser screen (where a user can choose a location)
        File exportFile = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (exportFile != null) {
            exportButton.setDisable(true);
            exportProgressBar.setVisible(true);
            exportStatusMessage.setVisible(false);

            long fromDate = transformStringInLocalDateTimeToEpochMillis(CURRENT_DATE_TIME_RANGE_FROM);
            long toDate =  System.currentTimeMillis();
            String dateChoice = timePeriodComboBox.getSelectionModel().getSelectedItem();
            if (CUSTOM_RANGE.equals(dateChoice)) {
                fromDate = this.customFromEpochMillis == 0 ? transformStringInLocalDateTimeToEpochMillis(dateTimeFromLabel.getText()) : this.customFromEpochMillis;
                toDate = this.customToEpochMillis == 0 ? transformStringInLocalDateTimeToEpochMillis(dateTimeToLabel.getText()) : this.customToEpochMillis;
            }
            Task<Void> exportTask = exportChangeSet(fromDate, toDate, exportFile);
            exportProgressBar.progressProperty().unbind();
            exportProgressBar.progressProperty().bind(exportTask.progressProperty());
            ProgressHelper.progress(exportTask, "Cancel Export");
        }
    }

    private Task<Void> exportChangeSet(long fromDate, long toDate, File exportFile) {
        TrackingCallable<Void> trackingCallable = new TrackingCallable<>(){
            @Override
            protected Void compute() throws Exception {
                updateTitle("FHIR JSON changeset to file " + exportFile.getName());
                updateProgress(0,3);
                updateMessage("Retrieving changeset data in progress.");
                try{
                    Set<ConceptEntity<? extends ConceptEntityVersion>> conceptEntities = retrieveDataTask(fromDate, toDate).call();
                    updateProgress(1,3);
                    updateMessage("Retrieved " + conceptEntities.size()+" concepts to export complete.");
                    StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
                    FhirCodeSystemTransform fhirCodeSystemTransform = new FhirCodeSystemTransform(fromDate, toDate, stampCalculator, conceptEntities.stream(), (fhirString) -> {
                        try {
                        updateProgress(2,3);
                        saveFhirFormatToDisk(fhirString, exportFile);
                        updateProgress(3,3);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    fhirCodeSystemTransform.compute();
                }catch(Exception e){
                    LOG.error("Error Saving FHIR export file", e);
                    updateMessage("Error Saving FHIR export file");
                    return null;
                }
                exportButton.setDisable(false);
                return null;
            }
        };
        return TaskWrapper.make(trackingCallable);
    }

    @FXML
    void handleAddTags(ActionEvent event) {
        if (addTagStage != null) {
            addTagStage.show();
            return;
        }
        addTagStage = new Stage();
        FXMLLoader addTagLoader = ExportDatasetViewFactory.createFXMLLoaderForAddTagExportDataset();
        try {
            Pane addTagBorderPane = addTagLoader.load();
            AddTagExportDatasetController addTagExportDatasetController = addTagLoader.getController();
            Scene sourceScene = new Scene(addTagBorderPane, 480, 450);
            addTagStage.setScene(sourceScene);
            addTagStage.setTitle("Add Tag(s)");
            addTagStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setupModulesTags() {
        modulesVBox.getChildren().addAll(snomedCTHbox, snomedUKHbox, loinCHbox, rxNormHbox);
        modulesVBox.setDisable(true);
    }

    public void setupDateTimeExportComboBox() {
        timePeriodComboBox.setValue(CURRENT_DATE);
        timePeriodComboBox.getItems().addAll(CURRENT_DATE, CUSTOM_RANGE);
        dateTimePickerHbox.setVisible(false);
        handleCurrentDateTimeExport();
    }

    public void handleCurrentDateTimeExport() {
        // Responsible for showing/hiding custom date range controls
        timePeriodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timePeriodComboBox.getValue().equals("Current Date")) {
                // Hide custom date range controls
                dateTimePickerHbox.setVisible(false);
            } else {
                dateTimePickerHbox.setVisible(true);
            }
        });
    }

    @FXML
    private void updateFromDateTime(ActionEvent event) {
        fromDateTimePopOver.show((Node) event.getSource());
    }

    @FXML
    private void updateToDateTime(ActionEvent event) {
        toDateTimePopOver.show((Node) event.getSource());
    }

    private TrackingCallable<Set<ConceptEntity<? extends ConceptEntityVersion>>> retrieveDataTask(long fromTimeStamp, long toTimeStamp) {
        return new TrackingCallable<>(){
            @Override
            protected Set<ConceptEntity<? extends ConceptEntityVersion>> compute() {
                Set<ConceptEntity<? extends ConceptEntityVersion>> concepts = new HashSet<>();
                TemporalEntityAggregator temporalEntityAggregator = new TemporalEntityAggregator(fromTimeStamp, toTimeStamp);
                temporalEntityAggregator.aggregate(nid -> {
                    Entity<EntityVersion> entity = EntityService.get().getEntityFast(nid);
                    if (entity instanceof ConceptEntity conceptEntity) {
                        concepts.add(conceptEntity);
                    } else if (entity instanceof SemanticEntity semanticEntity) {
                        Entity<EntityVersion> referencedConcept = semanticEntity.referencedComponent();
                        if (referencedConcept instanceof ConceptEntity concept) {
                            concepts.add(concept);
                        }
                    }
                });
                return concepts;
            }
        };
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    private String transformLocalDateTimeToStr(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy, hh:mm a");
        ZoneId zoneId = ZoneId.of("America/New_York");
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        return dateTimeFormatter.format(zonedDateTime);
    }

    private void saveFhirFormatToDisk(String fhirString, File exportFile) throws IOException {
        try (Writer write = new FileWriter(exportFile)) {
            write.write(fhirString);
            write.flush();
        }
    }

    private LocalDateTime transformEpochMillisToLocalDateTime(long epochMillis) {
        Instant instant = Instant.ofEpochMilli(epochMillis);
        ZoneId zoneId = ZoneId.of("America/New_York");
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        return zonedDateTime.toLocalDateTime();
    }

    private long transformStringInLocalDateTimeToEpochMillis(String localDateTimeFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy, hh:mm a");
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeFormat, formatter);
        ZoneId zoneId = ZoneId.of("America/New_York");
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

}
