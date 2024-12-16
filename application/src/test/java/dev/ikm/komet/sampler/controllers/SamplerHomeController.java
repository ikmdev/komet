package dev.ikm.komet.sampler.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SamplerHomeController {
    @FXML
    private Label samplerDescription;

    public void initialize()
    {
        samplerDescription.setText("This is Komet Sampler. This Sampler is meant to showcase and test all controls that Komet provides." +
                "\nIt's also like a playground for these controls.");

    }
}