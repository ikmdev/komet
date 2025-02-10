package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.kview.controls.KLImageControl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SampleImageController {

    @FXML
    private KLImageControl imageControl;

    @FXML
    private Label samplerDescription;

    public void initialize() {
        samplerDescription.setText("Image Control used to edit images.");
    }
}