package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLImageControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.io.File;

public class KLImageControl extends Control {
    public KLImageControl() {
        getStyleClass().add("image-control");
    }

    // -- title
    private StringProperty title = new SimpleStringProperty("Image");
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- prompt text
    private StringProperty promptText = new SimpleStringProperty("Add image");
    public String getPromptText() { return promptText.get(); }
    public StringProperty promptTextProperty() { return promptText; }
    public void setPromptText(String text) { this.promptText.set(text); }

    // -- image file
    private final ObjectProperty<File> imageFile = new SimpleObjectProperty<>();
    public File getImageFile() { return imageFile.get();}
    public ObjectProperty<File> imageFileProperty() { return imageFile; }
    public void setImageFile(File imageFile) { this.imageFile.set(imageFile); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLImageControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("image-control.css").toExternalForm();
    }
}