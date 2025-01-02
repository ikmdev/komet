package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLIntegerControlSkin;
import javafx.scene.control.Skin;

public class KLIntegerControl extends KLStringControl {

    public KLIntegerControl() {
        getStyleClass().add("integer-control");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLIntegerControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLIntegerControl.class.getResource("integer-control.css").toExternalForm();
    }
}