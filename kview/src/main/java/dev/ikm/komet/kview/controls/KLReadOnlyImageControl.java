package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyImageControlSkin;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

public class KLReadOnlyImageControl extends KLReadOnlyBaseSingleValueControl<Image> {
    public KLReadOnlyImageControl() {
        getStyleClass().add("read-only-image-control");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyImageControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("read-only-image-control.css").toExternalForm();
    }
}