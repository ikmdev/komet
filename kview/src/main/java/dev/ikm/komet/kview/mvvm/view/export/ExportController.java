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

import static dev.ikm.komet.kview.events.ExportDateTimePopOverEvent.APPLY_POP_OVER;
import static dev.ikm.komet.kview.events.ExportDateTimePopOverEvent.CANCEL_POP_OVER;
import static dev.ikm.komet.kview.events.ExportDateTimePopOverEvent.FROM_DATE;
import static dev.ikm.komet.kview.events.ExportDateTimePopOverEvent.TO_DATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ExportDateTimePopOverEvent;
import dev.ikm.komet.kview.fxutils.ComboBoxHelper;
import dev.ikm.komet.kview.mvvm.viewmodel.ExportViewModel;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.aggregator.TemporalEntityAggregator;
import dev.ikm.tinkar.fhir.transformers.FhirCodeSystemTransform;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ExportController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportController.class);

    protected static final String FHIR_TIME_EXPORT_PICKER_FXML_FILE = "fhir-time-export-picker.fxml";

    private static final String CUSTOM_RANGE = "Custom Range";

    private static final String CURRRENT_DATE = "Current Date";


    private static final String CURRENT_DATE_TIME_RANGE_FROM = "01/01/2022, 12:00 AM";

    @InjectViewModel
    private ExportViewModel exportViewModel;

    @FXML
    private TextField exportName;

    @FXML
    private ComboBox<String> exportOptions;

    @FXML
    private ComboBox<EntityFacade> pathOptions;

    @FXML
    private ComboBox<String> timePeriodComboBox;

    @FXML
    private HBox dateTimePickerHbox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button exportButton;

    @FXML
    private Label dateTimeFromLabel;

    @FXML
    private Label dateTimeToLabel;

    private PopOver fromDateTimePopOver;

    private PopOver toDateTimePopOver;

    private long customFromEpochMillis;

    private long customToEpochMillis;

    private EvtBus exportDatasetEventBus;

    private UUID exportTopic;

    private static final String CHANGE_SET = "Change set";

    private static final String FHIR = "FHIR";

    @FXML
    public void initialize() {
        exportDatasetEventBus = EvtBusFactory.getDefaultEvtBus();
        exportTopic = UUID.randomUUID();

        exportOptions.getItems().addAll(CHANGE_SET, FHIR);
        setupDateTimeExportComboBox();
        setupCustomDateRangeLabel();

        // initially set the export button to disable and
        // set up an invalidation to make sure the form is valid before
        // the user is allowed to click 'export'
        exportButton.setDisable(true);
        InvalidationListener formValid = (obs) -> {
            boolean isFormValid = isFormPopulated();
            exportButton.setDisable(!isFormValid);
        };
        exportOptions.getSelectionModel().selectedItemProperty().addListener(formValid);

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

        // path options are disabled.  in the future, when they are enabled, we can
        // add them
        setUpPathOptions();
    }

    private void setUpPathOptions() {
        pathOptions.setConverter((new StringConverter<EntityFacade>() {
            @Override
            public String toString(EntityFacade conceptEntity) {
                ViewCalculator viewCalculator = getViewProperties().calculator();
                return (conceptEntity != null) ? viewCalculator.getRegularDescriptionText(conceptEntity).get() : "";
            }

            @Override
            public EntityFacade fromString(String s) {
                return null;
            }
        }));
        pathOptions.getItems().addAll(exportViewModel.getPaths());
    }

    private ViewProperties getViewProperties() {
        return exportViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    public void setupDateTimeExportComboBox() {
        dateTimePickerHbox.setVisible(false);
        handleCurrentDateTimeExport();
        ComboBoxHelper.setupComboBoxWithIcon(timePeriodComboBox, String::toString, "check-mark");
        timePeriodComboBox.getSelectionModel().select(CURRRENT_DATE);
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

    public void handleCurrentDateTimeExport() {
        // Responsible for showing/hiding custom date range controls
        timePeriodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            //FIXME
            if (timePeriodComboBox.getValue().equals("Current Date")) {
                // Hide custom date range controls
                dateTimePickerHbox.setVisible(false);
            } else {
                dateTimePickerHbox.setVisible(true);
            }
        });
    }

    @FXML
    private void handleCancelButtonEvent(ActionEvent cancelEvent) {
        cancelEvent.consume();
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleExport(ActionEvent exportEvent) {
        exportEvent.consume();

        String exportOption = exportOptions.getSelectionModel().getSelectedItem();
        FileChooser fileChooser = new FileChooser();
        //Date formatter for the desired date template
        String pattern = "yyyyMMdd-HHmm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);


        // get the from and to dates as millisecond long values
        long fromDate = transformStringInLocalDateTimeToEpochMillis(CURRENT_DATE_TIME_RANGE_FROM);
        long toDate =  System.currentTimeMillis();
        String dateChoice = timePeriodComboBox.getSelectionModel().getSelectedItem();
        if (CUSTOM_RANGE.equals(dateChoice)) {
            fromDate = this.customFromEpochMillis == 0 ? transformStringInLocalDateTimeToEpochMillis(dateTimeFromLabel.getText()) : this.customFromEpochMillis;
            toDate = this.customToEpochMillis == 0 ? transformStringInLocalDateTimeToEpochMillis(dateTimeToLabel.getText()) : this.customToEpochMillis;
        }
        // if the user enters a name then use that name, e.g. test.json or test.zip
        // if the user does not enter a name, then default to komet-yyyyMMdd-HHmm.zip|.json
        String initialFileName = exportName.getText().isBlank()
                ? "komet-%s".formatted(simpleDateFormat.format(new Date()))
                : exportName.getText();
        if (exportOption.equalsIgnoreCase(CHANGE_SET)) {
            initialFileName += ".zip";
            fileChooser.setTitle("Export file name as");
            //Making sure the zip is the only thing that is zipped up
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Zip Files", "*.zip"));
            fileChooser.setInitialFileName(initialFileName);
            performChangeSetExport(fileChooser, fromDate, toDate);
        } else if (exportOption.equalsIgnoreCase(FHIR)) {
            initialFileName += ".json";
            fileChooser.setTitle("Fhir File Exporter");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Fhir Json Files", "*.json"));
            fileChooser.setInitialFileName(initialFileName);
            // FHIR export
            performFhirExport(fileChooser, fromDate, toDate);
        }

    }

    private void performChangeSetExport(FileChooser fileChooser, long fromDate, long toDate) {
        File exportFile = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (exportFile != null) {
            //Calls a tinkar-core class that is responsible for transforming entities from the database to

            CompletableFuture<EntityCountSummary> completableFuture = EntityService.get().temporalExport(exportFile, fromDate, toDate)
                    .whenComplete((entityCountSummary, th) -> {
                        if (th != null) {
                            LOG.error("Export failed to complete", th);
                        }else {
                            LOG.info("Export Completed");
                        }
                    });
            notifyProgressIndicator(completableFuture, "Export all data");
        }
    }

    private void notifyProgressIndicator(CompletableFuture<EntityCountSummary> completableFuture, String title) {
        Task<EntityCountSummary> javafxTask = new Task() {
            @Override
            protected EntityCountSummary call() throws Exception {
                updateTitle(title);
                updateProgress(-1, 1);
                completableFuture.whenComplete((entityCountSummary, th) -> {
                    if (th != null) {
                        updateMessage( "Export failed to complete");
                        updateProgress(0.0, 0.0);
                    }else {
                        updateMessage("Export Completed!");
                        updateProgress(1.0, 1.0);
                    }
                });
                EntityCountSummary entityCountSummary = completableFuture.get();

                return entityCountSummary;
            }
        };
        ProgressHelper.progress(javafxTask, "Cancel Export");
    }

    private void performFhirExport(FileChooser fileChooser, long fromDate, long toDate) {
        //Triggers the file chooser screen (where a user can choose a location)
        File exportFile = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (exportFile != null) {
            Task<Void> exportTask = exportChangeSet(fromDate, toDate, exportFile);
            ProgressHelper.progress(exportTask, "Cancel Export");
        }
    }

    private long transformStringInLocalDateTimeToEpochMillis(String localDateTimeFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy, hh:mm a");
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeFormat, formatter);
        ZoneId zoneId = ZoneId.of("America/New_York");
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
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

    private void saveFhirFormatToDisk(String fhirString, File exportFile) throws IOException {
        try (Writer write = new FileWriter(exportFile)) {
            write.write(fhirString);
            write.flush();
        }
    }

    @FXML
    private void updateFromDateTime(ActionEvent event) {
        fromDateTimePopOver.show((Node) event.getSource());
    }

    @FXML
    private void updateToDateTime(ActionEvent event) {
        toDateTimePopOver.show((Node) event.getSource());
    }

    private String transformLocalDateTimeToStr(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy, hh:mm a");
        ZoneId zoneId = ZoneId.of("America/New_York");
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        return dateTimeFormatter.format(zonedDateTime);
    }

    private LocalDateTime transformEpochMillisToLocalDateTime(long epochMillis) {
        Instant instant = Instant.ofEpochMilli(epochMillis);
        ZoneId zoneId = ZoneId.of("America/New_York");
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        return zonedDateTime.toLocalDateTime();
    }

    private void setupCustomDateRangeLabel() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        String customDateRangeFrom = transformLocalDateTimeToStr(startOfDay);
        dateTimeFromLabel.setText(customDateRangeFrom);
        String customDateRangeTo = transformLocalDateTimeToStr(LocalDateTime.now());
        dateTimeToLabel.setText(customDateRangeTo);
    }

    private boolean isFormPopulated() {
        return (!exportOptions.getSelectionModel().isEmpty())
                && (true);
    }
}
