package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLReadOnlyStringListControl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SampleReadOnlyStringListController {
    @FXML
    private KLReadOnlyStringListControl readOnlyStringListControl;

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
        samplerDescription.setText("Read-Only String List Control that's used to display KLComponentSetField");

        titleTextField.setText(readOnlyStringListControl.getTitle());
        editModeCheckBox.setSelected(readOnlyStringListControl.isEditMode());
    }

    @FXML
    private void onTitleChanged(ActionEvent actionEvent) {
        readOnlyStringListControl.setTitle(titleTextField.getText());
    }

    @FXML
    private void onAddText(ActionEvent actionEvent) {
        readOnlyStringListControl.getTexts().add(textTextField.getText());
    }

    @FXML
    private void editModeChanged(ActionEvent actionEvent) {
        readOnlyStringListControl.setEditMode(editModeCheckBox.isSelected());
    }

    @FXML
    private void onRemoveText(ActionEvent actionEvent) {
        if (!readOnlyStringListControl.getTexts().isEmpty()) {
            readOnlyStringListControl.getTexts().removeLast();
        }
    }
}