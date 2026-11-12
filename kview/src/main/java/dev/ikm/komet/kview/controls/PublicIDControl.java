package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.PublicIDControlSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/// Control for the Public ID UUID.
/// This control only has a single property, which is rendered in the default skin PublicIDControlSkin.
public class PublicIDControl extends Control {

     /// The public ID (UUID) property
    private StringProperty publicId = new SimpleStringProperty(this, "publicId");

    /// Creates the control. Its stylesheet goes on the scene through [ControlSceneStylesheets],
    /// which adds it at most once to a scene.
    public PublicIDControl() {
        sceneProperty().subscribe(scene -> ControlSceneStylesheets.ensure(scene, getUserAgentStylesheet()));
    }

    public PublicIDControl(String publicId) {
        this();
        setPublicId(publicId);
    }

    public StringProperty publicIdProperty() {
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

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return PublicIDControl.class.getResource("public-id-control.css").toExternalForm();
    }

}
