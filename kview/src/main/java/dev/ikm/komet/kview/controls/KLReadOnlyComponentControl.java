package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyComponentControlSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

public class KLReadOnlyComponentControl extends Control {

    public KLReadOnlyComponentControl() {
        getStyleClass().add("read-only-component-control");
    }

    // -- title
    private StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- text
    private StringProperty text = new SimpleStringProperty();
    public String getText() { return text.get(); }
    public StringProperty textProperty() { return text; }
    public void setText(String text) { this.text.set(text); }

    // -- icon
    private ObjectProperty<Image> icon = new SimpleObjectProperty<>();
    public Image getIcon() { return icon.get(); }
    public ObjectProperty<Image> iconProperty() { return icon; }
    public void setIcon(Image icon) { this.icon.set(icon); }

    // -- edit mode
    private BooleanProperty editMode = new SimpleBooleanProperty();
    public boolean isEditMode() { return editMode.get(); }
    public BooleanProperty editModeProperty() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode.set(editMode); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyComponentControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLReadOnlyComponentControl.class.getResource("read-only-component-control.css").toExternalForm();
    }
}