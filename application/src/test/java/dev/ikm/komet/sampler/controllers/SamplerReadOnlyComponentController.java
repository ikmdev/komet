package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SamplerReadOnlyComponentController {

    @FXML
    private KLReadOnlyComponentControl readOnlyComponentControl;

    @FXML
    private CheckBox editModeCheckBox;

    @FXML
    private TextField titleTextField;

    @FXML
    private TextField textTextField;

    @FXML
    private Label samplerDescription;

    public void initialize()
    {
        samplerDescription.setText("Read only String Control that's used to display KLStringField");

        titleTextField.setText(readOnlyComponentControl.getTitle());
        textTextField.setText(readOnlyComponentControl.getText());
        editModeCheckBox.setSelected(readOnlyComponentControl.isEditMode());
    }

    @FXML
    private void onTitleChanged(ActionEvent actionEvent) {
        readOnlyComponentControl.setTitle(titleTextField.getText());
    }

    @FXML
    private void onTextChanged(ActionEvent actionEvent) {
        readOnlyComponentControl.setText(textTextField.getText());
    }

    @FXML
    private void editModeChanged(ActionEvent actionEvent) {
        readOnlyComponentControl.setEditMode(editModeCheckBox.isSelected());
    }
}