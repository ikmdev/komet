package dev.ikm.komet.kview.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class KLReadOnlyBaseSingleValueControl<T> extends KLReadOnlyBaseControl {
    // -- value
    private ObjectProperty<T> value = new SimpleObjectProperty<>();
    public T getValue() { return value.get(); }
    public ObjectProperty<T> valueProperty() { return value; }
    public void setValue(T value) { this.value.set(value); }

    // -- on remove action
    private ObjectProperty<Runnable> onRemoveAction = new SimpleObjectProperty<>();
    public Runnable getOnRemoveAction() { return onRemoveAction.get(); }
    public ObjectProperty<Runnable> onRemoveActionProperty() { return onRemoveAction; }
    public void setOnRemoveAction(Runnable onEditAction) { this.onRemoveAction.set(onEditAction); }
}