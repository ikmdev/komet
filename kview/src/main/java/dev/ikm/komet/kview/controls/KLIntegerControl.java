package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLIntegerControlSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * <p>KLIntegerControl is a custom control that allows displaying and editing
 * Integer data types</p>
 * <pre><code>
 * KLIntegerControl integerControl = new KLIntegerControl();
 * integerControl.setTitle("Integer Title");
 * integerControl.valueProperty().subscribe(v -> System.out.println("Integer value: " + v));
 * </code></pre>
 * */
public class KLIntegerControl extends Control {

    /**
     * Creates a KLIntegerControl
     */
    public KLIntegerControl() {
        getStyleClass().add("integer-control");
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
     * An integer property that holds an integer value, between
     * {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE},
     * or null if no value is set.
     */
    private final ObjectProperty<Integer> valueProperty = new SimpleObjectProperty<>(this, "value");
    public final ObjectProperty<Integer> valueProperty() {
        return valueProperty;
    }
    public final Integer getValue() {
        return valueProperty.get();
    }
    public final void setValue(Integer value) {
        valueProperty.set(value);
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
     * are errors editing the integer value
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
        return new KLIntegerControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLIntegerControl.class.getResource("integer-control.css").toExternalForm();
    }
}
