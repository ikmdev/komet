package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyImageControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import java.io.File;

public class KLReadOnlyImageControl extends KLReadOnlyBaseControl {
    public KLReadOnlyImageControl() {
        getStyleClass().add("read-only-image-control");
    }

    // -- image file
    private final ObjectProperty<File> imageFile = new SimpleObjectProperty<>();
    public File getImageFile() { return imageFile.get();}
    public ObjectProperty<File> imageFileProperty() { return imageFile; }
    public void setImageFile(File imageFile) { this.imageFile.set(imageFile); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyImageControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("read-only-image-control.css").toExternalForm();
    }
}