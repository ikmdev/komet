package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLIntegerControl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SamplerIntegerController {

    @FXML
    private KLIntegerControl integerControl;

    @FXML
    private Label integerControlText;

    @FXML
    private Label samplerDescription;

    public void initialize()
    {
        samplerDescription.setText("Integer Control that's used to edit KLIntegerField");

        integerControlText.textProperty().bind(integerControl.valueProperty().asString());
    }
}