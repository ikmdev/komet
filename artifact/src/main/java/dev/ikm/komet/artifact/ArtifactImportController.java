package dev.ikm.komet.artifact;


import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ArtifactImportController {
    @FXML
    private Label choosenFileLabel;

    @FXML
    private Text fileFormatId;

    @FXML
    private Button importButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ProgressBar importProgressBar;

    @FXML
    void handleChooseFile(ActionEvent event) throws IOException {
        importProgressBar.setVisible(false);
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

    private Task<Boolean> createWorker(File selectedFile) {
        return new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                LoadEntitiesFromProtobufFile loadEntities = new LoadEntitiesFromProtobufFile(selectedFile);
                loadEntities.compute();
                updateProgress(100, 100);
                importButton.setDisable(false);
                cancelButton.setDisable(true);
                return true;
            }
        };
    }
}
