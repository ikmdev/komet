package dev.ikm.komet.layout.component.version.field.example.klstring;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlStringField;
import javafx.scene.control.TextField;

/**
 * Represents a concrete implementation of {@link StringFieldAbstract} and {@link KlStringField} that provides
 * a UI component for displaying and binding string values using a {@link TextField}. This component is specifically
 * designed to work with an {@link ObservableField} of type {@link String} and an {@link ObservableView}.
 *
 * The control binds to the observable field's value property to synchronize the displayed value with the underlying
 * data model. Changes in the text field are automatically reflected in the observable field's value.
 *
 * The purpose of the class is to provide a reusable and customizable user interface for editing and displaying
 * string values while leveraging observable properties and the associated view context.
 */
public class StringFieldText extends StringFieldAbstract implements KlStringField {
    // Would it be better to set the string control in a local field in a constructor prolog (see https://openjdk.org/jeps/492)?
    private StringFieldText(ObservableField<String> observableStringField, ObservableView observableView) {
        super(observableStringField, new TextField("The string"), observableView);
        getValueControl().setText(observableStringField.value());
        getValueControl().textProperty().bind(observableStringField.valueProperty());
    }

    /**
     * Retrieves the {@link TextField} control used to display and edit the value of the string field.
     * The control is bound to the associated {@link ObservableField<String>}, reflecting
     * the current value and updating automatically when the value changes.
     *
     * @return the {@link TextField} control displaying and editing the string field's value.
     */
    private TextField getValueControl() {
        return (TextField) this.stringControl;
    }

    /**
     * Creates a new instance of {@link StringFieldText} using the given {@link ObservableField} and {@link ObservableView}.
     * The created instance provides a UI component for displaying and binding string values.
     *
     * @param observableStringField the {@link ObservableField} representing the string value to be displayed and bound
     * @param observableView the {@link ObservableView} providing contextual coordinate information
     * @return a new instance of {@link StringFieldText} bound to the given observable field and view
     */
    public static StringFieldText create(ObservableField<String> observableStringField, ObservableView observableView) {
        return new StringFieldText(observableStringField, observableView);
    }
}
