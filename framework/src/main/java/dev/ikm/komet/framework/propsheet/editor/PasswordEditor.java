package dev.ikm.komet.framework.propsheet.editor;

import javafx.beans.property.StringProperty;
import javafx.scene.control.PasswordField;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;

public class PasswordEditor extends AbstractPropertyEditor<String, PasswordField> {
    public PasswordEditor(PropertySheet.Item property) {
        super(property, new PasswordField());
    }

    public PasswordEditor(PropertySheet.Item property, PasswordField control, boolean readonly) {
        super(property, new PasswordField(), readonly);
    }

    @Override
    protected StringProperty getObservableValue() {
        return getEditor().textProperty();
    }

    @Override
    public void setValue(String value) {
        getEditor().setText(value);
    }

}
