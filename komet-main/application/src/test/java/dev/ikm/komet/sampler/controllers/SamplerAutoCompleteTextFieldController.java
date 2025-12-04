package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.AutoCompleteTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class SamplerAutoCompleteTextFieldController {

    @FXML
    private AutoCompleteTextField<String> autoCompleteTextField;

    @FXML
    private TextField onActionTextField;

    @FXML
    private Label samplerDescription;

    public void initialize() {
        samplerDescription.setText("The AutoCompleteTextfield is a general purpose auto complete textfield control. When the user " +
                "\ntypes text in the textfield suggestions are shown below in a popup.");

        autoCompleteTextField.setCompleter(this::onCompleter);
        autoCompleteTextField.setOnAction(actionEvent -> onActionTextField.setText(autoCompleteTextField.getText()));
    }

    private List<String> onCompleter(String inputText) {
        List<String> completerResult = List.of("aa", "aaa", "aab", "aabb", "aabc", "aabcd", "aabcde", "aabcdef");
        return completerResult;
    }
}