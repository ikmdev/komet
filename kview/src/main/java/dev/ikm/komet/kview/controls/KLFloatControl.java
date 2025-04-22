package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLFloatControlSkin;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Objects;

/**
 * <p>KLFloatControl is a custom control that allows displaying and editing
 * Float data types</p>
 * <pre><code>
 * KLFloatControl floatControl = new KLFloatControl();
 * floatControl.setTitle("Float Title");
 * floatControl.valueProperty().subscribe(v -> System.out.println("Float value: " + v));
 * </code></pre>
 * */
@DefaultProperty("value")
public class KLFloatControl extends Control {

    /**
     * Creates a KLFloatControl
     */
    public KLFloatControl() {
        getStyleClass().add("float-control");
        getStylesheets().add(getUserAgentStylesheet());
    }

    /**
     * A string property that sets the title of the control, if any
     */
    private final StringProperty titleProperty = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() {
        return titleProperty;
    }
    public final String getTitle() {
        return titleProperty.get();
    }
    public final void setTitle(String value) {
        titleProperty.set(value);
    }

    /**
     * A Float property that holds a float value, between
     * {@link Float#MIN_VALUE} and {@link Float#MAX_VALUE},
     * or null if no value is set.
     */
    private final ObjectProperty<Float> valueProperty = new SimpleObjectProperty<>(this, "value");
    public final ObjectProperty<Float> valueProperty() {
        return valueProperty;
    }
    public final Float getValue() {
        return valueProperty.get();
    }
    public final void setValue(Float value) {
        if(value != null && !Objects.equals(valueProperty.get(), value)){
            valueProperty.set(value);
        }
    }

    /**
     * A string property that sets the unit of measure of the value, if any
     */
    private final StringProperty unitOfMeasureProperty = new SimpleStringProperty(this, "unitOfMeasure");
    public final StringProperty unitOfMeasureProperty() {
        return unitOfMeasureProperty;
    }
    public final String getUnitOfMeasure() {
        return unitOfMeasureProperty.get();
    }
    public final void setUnitOfMeasure(String value) {
        unitOfMeasureProperty.set(value);
    }

    /**
     * A string property that sets the prompt of the control
     */
    private final StringProperty promptTextProperty = new SimpleStringProperty(this, "promptText");
    public final StringProperty promptTextProperty() {
        return promptTextProperty;
    }
    public final String getPromptText() {
        return promptTextProperty.get();
    }
    public final void setPromptText(String value) {
        promptTextProperty.set(value);
    }

    /**
     * Boolean property that enables displaying a label with an error message when there
     * are errors editing the float value
     */
    private final BooleanProperty showErrorProperty = new SimpleBooleanProperty(this, "showError", false);
    public final BooleanProperty showErrorProperty() {
        return showErrorProperty;
    }
    public final boolean isShowError() {
        return showErrorProperty.get();
    }
    public final void setShowError(boolean value) {
        showErrorProperty.set(value);
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLFloatControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLFloatControl.class.getResource("float-control.css").toExternalForm();
    }
}
