package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyComponentControlSkin;
import javafx.scene.control.Skin;

public class KLReadOnlyComponentControl extends KLReadOnlyBaseSingleValueControl<ComponentItem> {

    public KLReadOnlyComponentControl() {
        getStyleClass().add("read-only-component-control");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyComponentControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLReadOnlyComponentControl.class.getResource("read-only-component-control.css").toExternalForm();
    }
}