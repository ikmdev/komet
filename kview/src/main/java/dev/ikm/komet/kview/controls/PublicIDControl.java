package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.PublicIDControlSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/// Control for the Public ID UUID.
/// This control only has a single property, which is rendered in the default skin PublicIDSkin.
public class PublicIDControl extends Control {

     /// The public ID (UUID)
    private SimpleStringProperty publicId = new SimpleStringProperty(this, "publicId");

    public SimpleStringProperty publicIdProperty() {
        return publicId;
    }

    public String getPublicId() {
        return publicIdProperty().get();
    }

    public void setPublicId(String publicId) {
        publicIdProperty().set(publicId);
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new PublicIDControlSkin(this);
    }

}
