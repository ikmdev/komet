package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLReadOnlyStringControl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SamplerReadOnlyStringController {

    @FXML
    private KLReadOnlyStringControl readOnlyStringControl;

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

        titleTextField.setText(readOnlyStringControl.getTitle());
        textTextField.setText(readOnlyStringControl.getText());
        editModeCheckBox.setSelected(readOnlyStringControl.isEditMode());
    }

    @FXML
    private void onTitleChanged(ActionEvent actionEvent) {
        readOnlyStringControl.setTitle(titleTextField.getText());
    }

    @FXML
    private void onTextChanged(ActionEvent actionEvent) {
        readOnlyStringControl.setText(textTextField.getText());
    }

    @FXML
    private void editModeChanged(ActionEvent actionEvent) {
        readOnlyStringControl.setEditMode(editModeCheckBox.isSelected());
    }
}
