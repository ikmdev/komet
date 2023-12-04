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

import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static dev.ikm.tinkar.common.service.CachingService.LOG;

/**
 * ArtifactExportController is responsible for triggering an export class in Tinkar-Core. This class
 * utilizes the ArtifactExport.fxml file that can be located in resources. Once a user clicks the 'export all'
 * button a outputted protobuf zip will be generated with all database data.
 */
public class ArtifactExportController {
    @FXML
    private Button exportButton;

    /**
     * This method is responsible for triggering the export from entities to a protobuf zip file.
     * @param event is the action of a user clicking the 'export button'
     * @throws IOException if the export fails to succeed
     */
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
        getExportButton().setDisable(true);

        //Asynchronously starting a thread to run the export to protobuf
        CompletableFuture.supplyAsync( () -> {
            try {
                //Calls a tinkar-core class that is responsible for transforming entities from the database to protobuf
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

    public Button getExportButton() {
        return exportButton;
    }
}

