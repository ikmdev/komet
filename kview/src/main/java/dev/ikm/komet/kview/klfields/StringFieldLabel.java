package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import javafx.scene.control.Label;

/**
 * Concrete implementation of a string field component that binds to an observable string field.
 * This class extends {@link StringFieldAbstract} to provide a specialized UI representation
 * of a string field where the value is displayed using a {@link Label} control.
 *
 * The {@link Label} control reflects the current value of the {@link ObservableField<String>}
 * and is automatically updated whenever the field's value changes. Additionally,
 * the label's content is bound bi-directionally to the observable field.
 */
public class StringFieldLabel extends StringFieldAbstract {

    /**
     * Constructs a StringFieldLabel instance by binding an observable string field to a {@code Label} control.
     * The label's text property is initialized with the current value of the observable field and is further
     * bound to the field's value such that it updates automatically when the value changes.
     *
     * @param observableStringField The observable field representing a string value. The field's current value
     *                              is used to initialize and bind the {@code Label}'s text property.
     * @param observableView        The observable view providing overall context and coordination for the field
     *                              and its corresponding UI components.
     */
    private StringFieldLabel(ObservableField<String> observableStringField, ObservableView observableView) {
        // Would it be better to set the string control in a local field in a constructor prolog (see https://openjdk.org/jeps/492)?
        super(observableStringField, new Label("The value"), observableView);
        getValueControl().setText(observableStringField.value());
        getValueControl().textProperty().bind(observableStringField.valueProperty());
    }

    /**
     * Retrieves the {@link Label} control used to display the value of the string field.
     * The control reflects the current value of the associated {@link ObservableField}
     * and updates automatically when the field's value changes.
     *
     * @return the {@link Label} control displaying the string field's value.
     */
    private Label getValueControl() {
        return (Label) this.stringControl;
    }

    /**
     * Factory method for creating a new instance of {@link StringFieldLabel}.
     * This method initializes a {@link StringFieldLabel} with the given observable field and view.
     *
     * @param observableStringField The observable field representing a string value. The field's value
     *                              will be bound to the {@code Label} control in the created instance.
     * @param observableView        The observable view providing context and coordination for the
     *                              {@link StringFieldLabel} and its associated UI components.
     * @return A new instance of {@link StringFieldLabel} bound to the provided observable field and view.
     */
    public static StringFieldLabel create(ObservableField<String> observableStringField, ObservableView observableView) {
        return new StringFieldLabel(observableStringField, observableView);
    }

}

