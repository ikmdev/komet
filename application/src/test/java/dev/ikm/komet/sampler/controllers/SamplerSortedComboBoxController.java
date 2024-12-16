package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.SortedComboBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SamplerSortedComboBoxController {
    @FXML
    private TextField textField;

    @FXML
    private Label samplerDescription;

    @FXML
    private SortedComboBox sortedComboBox;

    public void initialize()
    {
        samplerDescription.setText("The SortedComboBox is a control thats just a regular ComboBox the different is that its values " +
                "when the SortedComboBox is 'open' are always sorted. Sorting relies on the 'NaturalOrder.getObjectComparator()' which" +
                "by default sorts in ascending order \n(but also allows for other types of sorting order)");

    }

    public void onAddPressed(ActionEvent actionEvent) {
        sortedComboBox.getItems().add(textField.getText());
    }
}