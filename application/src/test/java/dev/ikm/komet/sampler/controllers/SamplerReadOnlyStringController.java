package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SamplerReadOnlyStringController {
    @FXML
    private ComboBox dataTypeComboBox;

    @FXML
    private KLReadOnlyDataTypeControl<String> readOnlyStringControl;

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
        samplerDescription.setText("Read only String Control that's used to display KLStringField, " +
                "KlIntegerField, KlFloatField, KlUuidField, KlInstantField");

        titleTextField.setText(readOnlyStringControl.getTitle());
        textTextField.setText(readOnlyStringControl.getValue());
        editModeCheckBox.setSelected(readOnlyStringControl.isEditMode());

        // Data Type Combobox
//        for (DataType dataType : DataType.values()) {
//            dataTypeComboBox.getItems().add(dataType);
//        }
//        dataTypeComboBox.setValue(readOnlyStringControl.getType());
//        dataTypeComboBox.valueProperty().addListener(observable -> {
//            readOnlyStringControl.setType(dataTypeComboBox.getValue());
//        });
    }

    @FXML
    private void onTitleChanged(ActionEvent actionEvent) {
        readOnlyStringControl.setTitle(titleTextField.getText());
    }

    @FXML
    private void onTextChanged(ActionEvent actionEvent) {
        readOnlyStringControl.setValue(textTextField.getText());
    }

    @FXML
    private void editModeChanged(ActionEvent actionEvent) {
        readOnlyStringControl.setEditMode(editModeCheckBox.isSelected());
    }
}
