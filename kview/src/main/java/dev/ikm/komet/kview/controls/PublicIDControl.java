package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.PublicIDControlSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/// Control for the Public ID UUID.
/// This control only has a single property, which is rendered in the default skin PublicIDControlSkin.
public class PublicIDControl extends Control {

     /// The public ID (UUID) property
    private StringProperty publicId = new SimpleStringProperty(this, "publicId");
    /// The showLabel property which controls if the ID label is displayed
    private BooleanProperty showLabel = new SimpleBooleanProperty(this, "showLabel", true);
    /// The trimIdentifier tells the PublicIDControlSkin to trim the identifier value from the colon in the prefix
    private BooleanProperty trimIdentifier = new SimpleBooleanProperty(this, "trimIdentifier", false);

    public PublicIDControl() {
         super();
    }

    public PublicIDControl(boolean showLabel, String publicId, boolean trimIdentifier) {
        super();
        setPublicId(publicId);
        setShowLabel(showLabel);
        setTrimIdentifier(trimIdentifier);
    }

    public StringProperty publicIdProperty() {
        return publicId;
    }

    public BooleanProperty showLabelProperty() {
        return showLabel;
    }

    public BooleanProperty trimIdentifierProperty() {
        return trimIdentifier;
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

    public boolean getTrimIdentifier() {
        return trimIdentifierProperty().get();
    }

    public void setTrimIdentifier(boolean trimIdentifier) {
        trimIdentifierProperty().set(trimIdentifier);
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
