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


import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import static dev.ikm.tinkar.common.service.CachingService.LOG;

public class ArtifactImportController {
    @FXML

    private Label choosenFileLabel;
    @FXML


    private Button importButton;
    @FXML


    private Button cancelButton;
    @FXML


    private ProgressBar importProgressBar;
    @FXML


    private FileChooser fileChooser;

    @FXML
    public void setChoosenFileLabel(Label choosenFileLabel) {
        this.choosenFileLabel = choosenFileLabel;
    }

    @FXML
    public void setImportButton(Button importButton) {
        this.importButton = importButton;
    }

    @FXML
    public void setCancelButton(Button cancelButton) {
        this.cancelButton = cancelButton;
    }

    @FXML
    public void setImportProgressBar(ProgressBar importProgressBar) {
        this.importProgressBar = importProgressBar;
    }

    @FXML
    public void setFileChooser(FileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    @FXML
    void handleChooseFile(ActionEvent event) throws IOException {
        getImportProgressBar().setVisible(false);
        Stage stage = (Stage)choosenFileLabel.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if(selectedFile != null){
            choosenFileLabel.setText(selectedFile.getAbsoluteFile().toString());
            importButton.setDisable(false);
            cancelButton.setDisable(false);
        } else {
            choosenFileLabel.setText("No file selected.");
            importButton.setDisable(true);
            cancelButton.setDisable(true);
            importProgressBar.setProgress(0);
        }
    }

    @FXML
    void initiateProtobufTransform(ActionEvent event) {
        importProgressBar.setVisible(true);
        importButton.setDisable(true);
        File selectedFile = new File(choosenFileLabel.getText());
        Task<Boolean> importTask = createWorker(selectedFile);

        importProgressBar.progressProperty().unbind();
        importProgressBar.progressProperty().bind(importTask.progressProperty());

        Thread thread = new Thread(importTask);
        thread.start();
    }

    @FXML
    void cancelImport(ActionEvent event) {
        importProgressBar.setProgress(0);
    }

    Task<Boolean> createWorker(File selectedFile) {
        return new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                try{
                    LoadEntitiesFromProtobufFile loadEntities = new LoadEntitiesFromProtobufFile(selectedFile);
                    loadEntities.compute();
                }
                catch(Exception e){
                    LOG.error("Exception has been thrown, see below:", e);
                }
                updateProgress(100, 100);
                importButton.setDisable(false);
                cancelButton.setDisable(true);
                return true;
            }
        };
    }

    protected Label getChoosenFileLabel() {
        return choosenFileLabel;
    }

    protected Button getImportButton() {
        return importButton;
    }

    protected Button getCancelButton() {
        return cancelButton;
    }

    protected ProgressBar getImportProgressBar() {
        return importProgressBar;
    }

    protected FileChooser getFileChooser() {
        return fileChooser;
    }
}
