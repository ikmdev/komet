package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.PublicIDControlSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/// Control for the Public ID UUID.
/// This control only has a single property, which is rendered in the default skin PublicIDControlSkin.
public class PublicIDControl extends Control {

     /// The public ID (UUID) property
    private SimpleStringProperty publicId = new SimpleStringProperty(this, "publicId");
    /// The showLabel property which controls if the ID label is displayed
    private SimpleBooleanProperty showLabel = new SimpleBooleanProperty(this, "showLabel", true);

    public PublicIDControl() {
         super();
    }

    public PublicIDControl(boolean showLabel, String publicId) {
        super();
        setPublicId(publicId);
        setShowLabel(showLabel);
    }

    public SimpleStringProperty publicIdProperty() {
        return publicId;
    }

    public SimpleBooleanProperty showLabelProperty() {
        return showLabel;
    }

    public String getPublicId() {
        return publicIdProperty().get();
    }

    public void setPublicId(String publicId) {
        publicIdProperty().set(publicId);
    }

    public boolean getShowLabel() {
        return showLabelProperty().get();
    }

    public void setShowLabel(boolean showLabel) {
        showLabelProperty().set(showLabel);
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
