package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyImageControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

public class KLReadOnlyImageControl extends KLReadOnlyBaseControl {
    public KLReadOnlyImageControl() {
        getStyleClass().add("read-only-image-control");
    }

    // -- image file
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
    public Image getImage() { return image.get();}
    public ObjectProperty<Image> imageProperty() { return image; }
    public void setImage(Image image) { this.image.set(image); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyImageControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("read-only-image-control.css").toExternalForm();
    }
}