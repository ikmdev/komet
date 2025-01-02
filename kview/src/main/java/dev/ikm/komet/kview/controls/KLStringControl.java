package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLStringControlSkin;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * <p>KLStringControl is a custom control that allows displaying and editing
 * String data types</p>
 * <pre><code>
 * KLStringControl stringControl = new KLStringControl();
 * stringControl.setTitle("String Title");
 * stringControl.textProperty().subscribe(t -> System.out.println("String text: " + t));
 * </code></pre>
 * */
@DefaultProperty("text")
public class KLStringControl extends Control {

    /**
     * Creates a KLStringControl
     */
    public KLStringControl() {
        getStyleClass().add("string-control");
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
     * A string property that holds the text of the control, or null if not set
     */
    private final StringProperty textProperty = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() {
       return textProperty;
    }
    public final String getText() {
       return textProperty.get();
    }
    public final void setText(String value) {
        textProperty.set(value);
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
     * are errors editing the string value
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
        return new KLStringControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLStringControl.class.getResource("string-control.css").toExternalForm();
    }
}
