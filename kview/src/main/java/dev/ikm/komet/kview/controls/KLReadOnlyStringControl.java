package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyStringControlSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class KLReadOnlyStringControl extends Control {

    public KLReadOnlyStringControl() {
        getStyleClass().add("read-only-string-control");
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

    // -- edit mode
    private BooleanProperty editMode = new SimpleBooleanProperty();
    public boolean isEditMode() { return editMode.get(); }
    public BooleanProperty editModeProperty() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode.set(editMode); }

    // -- on edit action
    private ObjectProperty<Runnable> onEditAction = new SimpleObjectProperty<>();
    public Runnable getOnEditAction() { return onEditAction.get(); }
    public ObjectProperty<Runnable> onEditActionProperty() { return onEditAction; }
    public void setOnEditAction(Runnable onEditAction) { this.onEditAction.set(onEditAction); }

    // -- on remove action
    private ObjectProperty<Runnable> onRemoveAction = new SimpleObjectProperty<>();
    public Runnable getOnRemoveAction() { return onRemoveAction.get(); }
    public ObjectProperty<Runnable> onRemoveActionProperty() { return onRemoveAction; }
    public void setOnRemoveAction(Runnable onEditAction) { this.onRemoveAction.set(onEditAction); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyStringControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("read-only-string-control.css").toExternalForm();
    }
}