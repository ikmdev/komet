package dev.ikm.komet.kview.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;

import java.util.function.Consumer;

public abstract class KLReadOnlyMultiComponentControl extends Control {
    public static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");

    // -- title
    private StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- prompt text
    private StringProperty promptText = new SimpleStringProperty("[Placeholder]");
    public String getPromptText() { return promptText.get(); }
    public StringProperty promptTextProperty() { return promptText; }
    public void setPromptText(String text) { this.promptText.set(text); }

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
    private ObjectProperty<Consumer<ComponentItem>> onRemoveAction = new SimpleObjectProperty<>();
    public Consumer<ComponentItem> getOnRemoveAction() { return onRemoveAction.get(); }
    public ObjectProperty<Consumer<ComponentItem>> onRemoveActionProperty() { return onRemoveAction; }
    public void setOnRemoveAction(Consumer<ComponentItem> onEditAction) { this.onRemoveAction.set(onEditAction); }
}