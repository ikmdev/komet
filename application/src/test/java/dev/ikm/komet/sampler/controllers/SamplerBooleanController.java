package dev.ikm.komet.sampler.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SamplerBooleanController {
    @FXML
    private Label samplerDescription;

    public void initialize()
    {
        samplerDescription.setText("The KLBooleanControl is a control to be used with Boolean data types. It acts like a " +
                "Radio Button. The user selected between two radio buttons where one corresponds to a 'false' value and the " +
                "other one will correspont to a 'true' value.");

    }
}