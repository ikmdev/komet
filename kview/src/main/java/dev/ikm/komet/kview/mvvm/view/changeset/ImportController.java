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
package dev.ikm.komet.kview.mvvm.view.changeset;

import static dev.ikm.tinkar.events.FrameworkTopics.CALCULATOR_CACHE_TOPIC;
import static dev.ikm.tinkar.events.FrameworkTopics.PROGRESS_TOPIC;
import static dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent.GLOBAL_REFRESH;
import static dev.ikm.komet.kview.mvvm.viewmodel.ImportViewModel.ImportField.DESTINATION_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ImportViewModel.ImportField.SELECTED_FILE;
import com.jpro.webapi.WebAPI;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.FrameworkTopics;
import dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.kview.events.pattern.PatternSavedEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.ImportViewModel;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import one.jpro.platform.file.ExtensionFilter;
import one.jpro.platform.file.FileSource;
import one.jpro.platform.file.dropper.FileDropper;
import one.jpro.platform.file.picker.FileOpenPicker;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code ImportController} class manages the user interface interactions
 * for importing datasets within the application. It handles file selection via
 * drag-and-drop or browsing, validates the selected file, and initiates the import process.
 *
 * <p>This controller is associated with the FXML view and interacts with the
 * {@link ImportViewModel} to maintain the state and validation of the import form.</p>
 */
public class ImportController {

    private static final Logger LOG = LoggerFactory.getLogger(ImportController.class);

    /**
     * Pseudo-class applied to the drop area when files are being dragged over it.
     */
    private static final PseudoClass FILES_DRAG_OVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("files-drag-over");

    /**
     * Extension filter for ZIP files used in file selection dialogs.
     */
    private static final ExtensionFilter ZIP_EXTENSION_FILTER = ExtensionFilter.of("Zip Files", ".zip");

    /**
     * The area where users can drag and drop dataset files for import.
     */
    @FXML
    private VBox dropDatasetArea;

    /**
     * The label displaying the name of the selected dataset file.
     */
    @FXML
    private Label datasetLabel;

    /**
     * The pane containing the progress indicator for the import operation.
     */
    @FXML
    public StackPane uploadProgressPane;

    /**
     * The button that opens a file chooser dialog for browsing dataset files.
     */
    @FXML
    private Button browseButton;

    /**
     * The button to cancel the import operation and close the dialog.
     */
    @FXML
    private Button cancelButton;

    /**
     * The button to initiate the import process of the selected dataset file.
     */
    @FXML
    private Button importButton;

    /**
     * The ViewModel managing the import form's state and validation logic.
     */
    @InjectViewModel
    private ImportViewModel importViewModel;

    /**
     * The progress indicator for the upload operation.
     */
    private ProgressIndicator uploadProgressIndicator;

    @FXML
    public void initialize() {
        // Create a progress indicator for the upload operation
        uploadProgressIndicator = new ProgressIndicator();

        // Setup file dropper for the drop area
        setupFileDropperHandler(dropDatasetArea);
        setupBrowseHandler(browseButton);

        // Initially validate the view model
        importViewModel.validate();
        importButton.disableProperty().bind(importViewModel.invalidProperty());
    }

    /**
     * Configures the file dropper handler for the specified node. This enables
     * drag-and-drop functionality for importing dataset files.
     *
     * @param dropNode the UI node that will act as the drop area
     */
    private void setupFileDropperHandler(Node dropNode) {
        FileDropper fileDropper = FileDropper.create(dropNode);
        fileDropper.setExtensionFilter(ZIP_EXTENSION_FILTER);
        fileDropper.setOnDragEntered(fileDragEvent ->
                dropNode.pseudoClassStateChanged(FILES_DRAG_OVER_PSEUDO_CLASS, true));
        fileDropper.setOnDragExited(fileDragEvent ->
                dropNode.pseudoClassStateChanged(FILES_DRAG_OVER_PSEUDO_CLASS, false));
        fileDropper.setOnFilesSelected(this::handleSelectedFile);
    }

    /**
     * Sets up the action handler for the browse button. When clicked, it opens a
     * file chooser dialog allowing users to select dataset files for import.
     *
     * @param button the browse button to which the handler is attached
     */
    private void setupBrowseHandler(Button button) {
        button.setOnAction(actionEvent -> {
            FileOpenPicker fileOpenPicker = FileOpenPicker.create(button);
            fileOpenPicker.setTitle("Open Resource File");
            fileOpenPicker.getExtensionFilters().addAll(ZIP_EXTENSION_FILTER, ExtensionFilter.ANY);
            fileOpenPicker.setOnFilesSelected(this::handleSelectedFile);
        });
    }

    /**
     * Handles the event when files are selected either via drag-and-drop or browsing.
     * It processes the first selected file, updates the ViewModel, and reflects the
     * selection in the UI.
     *
     * @param fileSources the list of selected file sources
     */
    private void handleSelectedFile(List<? extends FileSource> fileSources) {
        if (!fileSources.isEmpty()) {
            FileSource selectedFileSource = fileSources.getFirst();
            if (WebAPI.isBrowser()) {
                uploadProgressIndicator.progressProperty().unbind();
                uploadProgressIndicator.progressProperty().bind(selectedFileSource.progressProperty());
                uploadProgressPane.getChildren().setAll(uploadProgressIndicator);
            }
            selectedFileSource.uploadFileAsync().thenAccept(file -> Platform.runLater(() -> {
                datasetLabel.setText(file.getName());
                importViewModel.setPropertyValue(SELECTED_FILE, file);
                importViewModel.save();
                LOG.info("Selected file for import: {}", file);
            })).exceptionally(throwable -> {
                LOG.error("Error uploading file: ", throwable);
                AlertStreams.dispatchToRoot(new RuntimeException("Error uploading file: " + throwable.getMessage()));
                return null;
            });
        }
    }

    /**
     * Handles the cancel button event by closing the import dialog without performing any action.
     *
     * @param event the action event triggered by clicking the cancel button
     */
    @FXML
    void handleCancelButtonEvent(ActionEvent event) {
        closeDialog();
    }

    /**
     * Handles the import button event by initiating the import process if the ViewModel is valid.
     * It loads entities from the selected Protobuf file and shows a progress indicator.
     *
     * @param event the action event triggered by clicking the import button
     */
    @FXML
    void handleImportButtonEvent(ActionEvent event) {

        if (importViewModel.validProperty().get()) {
            File selectedFile = importViewModel.getPropertyValue(SELECTED_FILE);
            LoadEntitiesFromProtobufFile importTask = new LoadEntitiesFromProtobufFile(selectedFile);
            FrameworkTopics destTopic = importViewModel.getPropertyValue(DESTINATION_TOPIC);
            CompletableFuture<EntityCountSummary> future = ProgressHelper.progress(destTopic, importTask, "Cancel Import");
            future.whenComplete((entityCountSummary, throwable) -> {
                if (throwable != null) {
                    importTask.updateMessage("Import Failed: "+throwable.getMessage());
                    throwable.printStackTrace();
                } else {
                    if (entityCountSummary != null) {
                        EntityCountSummary ecs = entityCountSummary;
                        String completeMsg = importTask.getMessage();
                        importTask.updateMessage("%s - total: %d, C: %d, Sem: %d, P: %d, Stamps: %d".formatted(
                                completeMsg,
                                ecs.getTotalCount(),
                                ecs.conceptsCount(),
                                ecs.semanticsCount(),
                                ecs.patternsCount(),
                                ecs.stampsCount())
                        );
                    }
                    // Refresh the Pattern Navigation
                    EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC,new PatternSavedEvent(event.getSource(), PatternSavedEvent.PATTERN_UPDATE_EVENT));

                    EvtBusFactory.getDefaultEvtBus().publish(CALCULATOR_CACHE_TOPIC, new RefreshCalculatorCacheEvent(event.getSource(), GLOBAL_REFRESH));

                }

            });

            closeDialog();
            LOG.info("Importing dataset from file: {}", selectedFile);
        }
    }

    /**
     * Closes the import dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
