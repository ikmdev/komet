package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyStringControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

public class KLReadOnlyDataTypeControl<T> extends KLReadOnlyBaseControl {

    public enum DataType {
        INTEGER,
        FLOAT,
        STRING,
        BOOLEAN,
        UUID,
        INSTANT,
        BYTE_ARRAY
    }

    public KLReadOnlyDataTypeControl() {
        getStyleClass().add("read-only-string-control");
    }

    // -- data type
    private ObjectProperty<DataType> type = new SimpleObjectProperty<>(DataType.STRING);
    public DataType getType() { return type.get(); }
    public ObjectProperty<DataType> typeProperty() { return type; }
    public void setType(DataType type) { this.type.set(type); }

    // -- value
    private ObjectProperty<T> value = new SimpleObjectProperty<>();
    public T getValue() { return value.get(); }
    public ObjectProperty<T> valueProperty() { return value; }
    public void setValue(T value) { this.value.set(value); }

    // -- on add units of measure action
    private ObjectProperty<Runnable> onAddUnitsOfMeasureAction = new SimpleObjectProperty<>();
    public Runnable getOnAddUnitsOfMeasureAction() { return onAddUnitsOfMeasureAction.get(); }
    public ObjectProperty<Runnable> onAddUnitsOfMeasureActionProperty() { return onAddUnitsOfMeasureAction; }
    public void setOnAddUnitsOfMeasureAction(Runnable onAddUnitsOfMeasureAction) { this.onAddUnitsOfMeasureAction.set(onAddUnitsOfMeasureAction); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyStringControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("read-only-string-control.css").toExternalForm();
    }
}