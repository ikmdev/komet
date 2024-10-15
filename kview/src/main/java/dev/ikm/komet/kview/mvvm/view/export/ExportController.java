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

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ExportDateTimePopOverEvent;
import dev.ikm.komet.kview.fxutils.ComboBoxHelper;
import dev.ikm.komet.kview.mvvm.viewmodel.ExportViewModel;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.InvalidationListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
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
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.events.ExportDateTimePopOverEvent.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

public class ExportController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportController.class);

    protected static final String TIME_EXPORT_PICKER_FXML_FILE = "time-export-picker.fxml";

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

    @FXML
    public void initialize() {
        exportDatasetEventBus = EvtBusFactory.getDefaultEvtBus();
        exportTopic = UUID.randomUUID();

        exportOptions.getItems().addAll(CHANGE_SET);
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
        exportOptions.setValue(CHANGE_SET);
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
                .make(getClass().getResource(TIME_EXPORT_PICKER_FXML_FILE),
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
            String progressTitle = CUSTOM_RANGE.equals(dateChoice) ?
                    "Export Date Range: %s to %s".formatted(dateTimeFromLabel.getText(), dateTimeToLabel.getText())  : "Export All Data";
            performChangeSetExport(fileChooser, fromDate, toDate, progressTitle);
        } else {
            AlertStreams.dispatchToRoot(new UnsupportedOperationException("Export Type not supported"));
        }
    }

    private void performChangeSetExport(FileChooser fileChooser, long fromDate, long toDate, String progressTitle) {
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
            notifyProgressIndicator(completableFuture, progressTitle);
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

    private long transformStringInLocalDateTimeToEpochMillis(String localDateTimeFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy, hh:mm a");
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeFormat, formatter);
        ZoneId zoneId = ZoneId.of("America/New_York");
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
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
