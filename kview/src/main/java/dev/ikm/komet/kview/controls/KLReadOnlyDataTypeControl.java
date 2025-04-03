package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyDataTypeControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

public class KLReadOnlyDataTypeControl<T> extends KLReadOnlyBaseControl<T> {

    public KLReadOnlyDataTypeControl() {
        getStyleClass().add("read-only-data-control");
    }

    public KLReadOnlyDataTypeControl(Class<T> type) {
        this();
        this.classDataType = type;
    }

    // -- on add units of measure action
    private ObjectProperty<Runnable> onAddUnitsOfMeasureAction = new SimpleObjectProperty<>();
    public Runnable getOnAddUnitsOfMeasureAction() { return onAddUnitsOfMeasureAction.get(); }
    public ObjectProperty<Runnable> onAddUnitsOfMeasureActionProperty() { return onAddUnitsOfMeasureAction; }
    public void setOnAddUnitsOfMeasureAction(Runnable onAddUnitsOfMeasureAction) { this.onAddUnitsOfMeasureAction.set(onAddUnitsOfMeasureAction); }

    // -- class data type
    private Class<T> classDataType;

    /**
     * The type of the data being stored, which is the type of the value property.
     * This is also used to display different contextmenus depending on the type of the
     * value property.
     *
     * @return The type of the data being stored, which is the type of the value property.
     */
    public Class<T> getClassDataType() { return classDataType; }

    // -- data type
    private String dataType;
    public String getDataType() { return dataType; }
    /**
     * This is another property and another way of specifying the data being stored, which is the
     * type of the value property.
     * This should be mostly used in fxml as in fxml you can't specify generic information or
     * set Class types.
     */
    public void setDataType(String value) throws ClassNotFoundException {
        classDataType = (Class<T>) Class.forName(value);
        this.dataType = value;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyDataTypeControlSkin<T>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("read-only-data-control.css").toExternalForm();
    }
}