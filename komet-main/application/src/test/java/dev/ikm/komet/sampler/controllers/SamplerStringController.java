package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLStringControl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SamplerStringController {

    @FXML
    private KLStringControl stringControl;

    @FXML
    private Label stringControlText;

    @FXML
    private Label samplerDescription;

    public void initialize()
    {
        samplerDescription.setText("String Control that's used to edit KLStringField");

        stringControlText.textProperty().bind(stringControl.textProperty());
    }
}