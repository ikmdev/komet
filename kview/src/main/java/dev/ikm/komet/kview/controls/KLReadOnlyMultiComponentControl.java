package dev.ikm.komet.kview.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;

import java.util.function.Consumer;

public abstract class KLReadOnlyMultiComponentControl extends KLReadOnlyBaseControl {
    public static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");

    // -- on remove action
    private ObjectProperty<Consumer<ComponentItem>> onRemoveAction = new SimpleObjectProperty<>();
    public Consumer<ComponentItem> getOnRemoveAction() { return onRemoveAction.get(); }
    public ObjectProperty<Consumer<ComponentItem>> onRemoveActionProperty() { return onRemoveAction; }
    public void setOnRemoveAction(Consumer<ComponentItem> onEditAction) { this.onRemoveAction.set(onEditAction); }
}