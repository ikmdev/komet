package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLBooleanControlSkin;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Skin;

public class KLBooleanControl extends RadioButton {

    public KLBooleanControl() {
        getStyleClass().add("boolean-control");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLBooleanControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLBooleanControl.class.getResource("boolean-control.css").toExternalForm();
    }
}
