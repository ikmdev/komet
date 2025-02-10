package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLBooleanControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * <p>KLBooleanControl is a "editable" control typically used to edit boolean data types, although it is general purpose
 * control not coupled with any class (like KLBooleanField).
 *
 * It has a title and a control to edit the boolean value property.
 */
public class KLBooleanControl extends Control {

    public KLBooleanControl() {
        getStyleClass().add("boolean-control");
    }

    // -- title
    /**
     * A string property that sets the title of the control
     */
    private final StringProperty title = new SimpleStringProperty();
    public final StringProperty titleProperty() { return title; }
    public final String getTitle() { return title.get(); }
    public final void setTitle(String value) { title.set(value); }

    // -- value
    private final ObjectProperty<Boolean> value = new SimpleObjectProperty<>();
    public final ObjectProperty<Boolean> valueProperty() { return value; }
    public boolean isValue() { return value.get(); }
    public void setValue(boolean value) { this.value.set(value); }

    // -- prompt text
    private final StringProperty promptText = new SimpleStringProperty(this, "Choose Selection");
    public final StringProperty promptTextProperty() { return promptText; }
    public String getPromptText() { return promptText.get(); }
    public void setPromptText(String value) { this.promptText.set(value); }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLBooleanControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLBooleanControl.class.getResource("boolean-control.css").toExternalForm();
    }
}
