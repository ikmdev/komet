package dev.ikm.komet.app.credential;

import javafx.beans.property.StringProperty;
import javafx.scene.control.PasswordField;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;

/**
 * PasswordEditor is a class that extends AbstractPropertyEditor and is used for editing password properties.
 */
public class PasswordEditor extends AbstractPropertyEditor<String, PasswordField> {
    /**
     * Constructs a PasswordEditor object for editing password properties.
     *
     * @param property the property to be edited
     */
    public PasswordEditor(PropertySheet.Item property) {
        super(property, new PasswordField());
    }

    /**
     * The PasswordEditor class is used for editing password properties. It extends the AbstractPropertyEditor class.
     */
    public PasswordEditor(PropertySheet.Item property, PasswordField control, boolean readonly) {
        super(property, new PasswordField(), readonly);
    }

    /**
     * Returns the observable value of the editor's text property.
     *
     * @return The observable value of the editor's text property.
     */
    @Override
    protected StringProperty getObservableValue() {
        return getEditor().textProperty();
    }

    /**
     * Sets the value of the editor.
     *
     * @param value the value to be set
     */
    @Override
    public void setValue(String value) {
        getEditor().setText(String.valueOf(value));
    }

    /**
     * Sets the value of the editor. This method specifically handles
     * the char[] value type, and handles explicit conversion to string,
     * to eliminate type cast errors.
     *
     * @param value the value to be set
     */
    public void setValue(char[] value) {
        getEditor().setText(String.valueOf(value));
    }
}
