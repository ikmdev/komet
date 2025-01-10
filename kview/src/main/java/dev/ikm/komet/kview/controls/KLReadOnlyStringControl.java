package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyStringControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;

public class KLReadOnlyStringControl extends KLReadOnlyBaseControl {

    public enum StringDataType {
        INTEGER,
        FLOAT,
        STRING,
        BOOLEAN,
        UUID,
        INSTANT,
        BYTE_ARRAY
    }

    public KLReadOnlyStringControl() {
        getStyleClass().add("read-only-string-control");
    }

    // -- data type
    private ObjectProperty<StringDataType> dataType = new SimpleObjectProperty<>(StringDataType.STRING);
    public StringDataType getDataType() { return dataType.get(); }
    public ObjectProperty<StringDataType> dataTypeProperty() { return dataType; }
    public void setDataType(StringDataType stringDataType) { this.dataType.set(stringDataType); }

    // -- text
    private StringProperty text = new SimpleStringProperty();
    public String getText() { return text.get(); }
    public StringProperty textProperty() { return text; }
    public void setText(String text) { this.text.set(text); }

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