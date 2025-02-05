package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class SampleReadOnlyImageController {
    @FXML
    private KLReadOnlyImageControl readOnlyImageControl;

    @FXML
    private CheckBox editModeCheckBox;

    @FXML
    private TextField titleTextField;

    @FXML
    private Label samplerDescription;

    public void initialize()
    {
        samplerDescription.setText("Read-Only Image Control used to display images.");

        titleTextField.textProperty().bindBidirectional(readOnlyImageControl.titleProperty());
        editModeCheckBox.setSelected(readOnlyImageControl.isEditMode());
    }

    @FXML
    private void editModeChanged(ActionEvent actionEvent) {
        readOnlyImageControl.setEditMode(editModeCheckBox.isSelected());
    }

    @FXML
    private void chooseImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        // Set file chooser title
        fileChooser.setTitle("Open Image File");

        // Set extension filters to only show image files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(editModeCheckBox.getScene().getWindow());
        if (selectedFile != null) {
            System.out.println("Image selected: " + selectedFile.getName());
        } else {
            System.out.println("No file selected.");
        }

        readOnlyImageControl.setImageFile(selectedFile);
    }
}