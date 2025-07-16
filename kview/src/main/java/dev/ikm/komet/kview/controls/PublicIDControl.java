package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.PublicIDSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class PublicIDControl  extends Control {

    private SimpleStringProperty publicId = new SimpleStringProperty(this, "publicId");

    public SimpleStringProperty publicIdProperty() {
        return publicId;
    }

    private PublicIDSkin publicIdSkin;

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        publicIdSkin = new PublicIDSkin(this);
        return publicIdSkin;
    }

}
