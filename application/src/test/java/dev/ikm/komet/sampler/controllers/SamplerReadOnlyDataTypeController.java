package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.HashMap;

public class SamplerReadOnlyDataTypeController {
    @FXML
    private VBox controlContainer;

    @FXML
    private ComboBox<String> dataTypeComboBox;

    @FXML
    private KLReadOnlyDataTypeControl<String> defaultReadOnlyDataTypeControl;

    @FXML
    private CheckBox editModeCheckBox;

    @FXML
    private TextField titleTextField;

    @FXML
    private TextField textTextField;

    @FXML
    private Label samplerDescription;

    private HashMap<String, KLReadOnlyDataTypeControl<?>> classToKLReadOnlyDataTypeControl = new HashMap<>();

    private KLReadOnlyDataTypeControl<?> currentDataTypeControl;

    public void initialize()
    {
        currentDataTypeControl = defaultReadOnlyDataTypeControl;

        samplerDescription.setText("Read only Data Control that's used to display any simple Data types as String, Float, Boolean, Integer.");

        titleTextField.setText(defaultReadOnlyDataTypeControl.getTitle());
        textTextField.setText(defaultReadOnlyDataTypeControl.getValue());
        editModeCheckBox.setSelected(defaultReadOnlyDataTypeControl.isEditMode());

        // Data Type Combobox
        classToKLReadOnlyDataTypeControl.put("String", defaultReadOnlyDataTypeControl);
        classToKLReadOnlyDataTypeControl.put("Integer", new KLReadOnlyDataTypeControl<>(Integer.class));
        classToKLReadOnlyDataTypeControl.put("Float", new KLReadOnlyDataTypeControl<>(Float.class));
        classToKLReadOnlyDataTypeControl.put("Boolean", new KLReadOnlyDataTypeControl<>(Boolean.class));

        for (String key : classToKLReadOnlyDataTypeControl.keySet()) {
            dataTypeComboBox.getItems().add(key);
        }


        dataTypeComboBox.setValue("String");
        dataTypeComboBox.valueProperty().subscribe(newValue -> {
            KLReadOnlyDataTypeControl<?> newControl = null;
            Object value = null;
            switch(dataTypeComboBox.getValue()) {
                case "String" -> {
                    KLReadOnlyDataTypeControl<String> klReadOnlyComponentControl = (KLReadOnlyDataTypeControl<String>)classToKLReadOnlyDataTypeControl.get(newValue);
                    value = "test";
                    newControl = klReadOnlyComponentControl;
                }
                case "Integer" -> {
                    KLReadOnlyDataTypeControl<Integer> klReadOnlyComponentControl = (KLReadOnlyDataTypeControl<Integer>)classToKLReadOnlyDataTypeControl.get(newValue);
                    value = 0;
                    newControl = klReadOnlyComponentControl;
                }
                case "Float" -> {
                    KLReadOnlyDataTypeControl<Float> klReadOnlyComponentControl = (KLReadOnlyDataTypeControl<Float>)classToKLReadOnlyDataTypeControl.get(newValue);
                    value = 0f;
                    newControl = klReadOnlyComponentControl;
                }
                case "Boolean" -> {
                    KLReadOnlyDataTypeControl<Boolean> klReadOnlyComponentControl = (KLReadOnlyDataTypeControl<Boolean>)classToKLReadOnlyDataTypeControl.get(newValue);
                    value = false;
                    newControl = klReadOnlyComponentControl;
                }
            }
            newControl.setTitle(currentDataTypeControl.getTitle());

            controlContainer.getChildren().setAll(newControl);

            textTextField.setText(value.toString());


            currentDataTypeControl = newControl;
        });
    }

    @FXML
    private void onTitleChanged(ActionEvent actionEvent) {
        currentDataTypeControl.setTitle(titleTextField.getText());
    }

    @FXML
    private void onTextChanged(ActionEvent actionEvent) {
        String text = textTextField.getText();
        if (text.isEmpty()) {
            currentDataTypeControl.setValue(null);
            return;
        }

        if (currentDataTypeControl.getClassDataType().equals(String.class)) {
            ((KLReadOnlyDataTypeControl<String>)currentDataTypeControl).setValue(textTextField.getText());
        } else if (currentDataTypeControl.getClassDataType().equals(Integer.class)) {
            ((KLReadOnlyDataTypeControl<Integer>)currentDataTypeControl).setValue(Integer.parseInt(textTextField.getText()));
        } else if (currentDataTypeControl.getClassDataType().equals(Float.class)) {
            ((KLReadOnlyDataTypeControl<Float>)currentDataTypeControl).setValue(Float.parseFloat(textTextField.getText()));
        } else if (currentDataTypeControl.getClassDataType().equals(Boolean.class)) {
            ((KLReadOnlyDataTypeControl<Boolean>)currentDataTypeControl).setValue(Boolean.parseBoolean(textTextField.getText()));
        }
    }

    @FXML
    private void editModeChanged(ActionEvent actionEvent) {
        currentDataTypeControl.setEditMode(editModeCheckBox.isSelected());
    }
}
