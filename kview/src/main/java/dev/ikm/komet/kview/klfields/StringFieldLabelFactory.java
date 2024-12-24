package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.FieldFactory;
import dev.ikm.komet.layout.component.version.field.KlField;
import jdk.jfr.Description;
import jdk.jfr.Name;

/**
 * A factory implementation for creating read-only string fields represented by a {@link StringFieldLabel}.
 * This implementation of {@link FieldFactory} generates a field component where the string value is displayed
 * with a label, and editing functionality is disabled, as labels are non-editable controls.
 *
 * This class uses the factory pattern to encapsulate the creation logic for {@link StringFieldLabel}.
 * The {@link StringFieldLabel} binds to an {@link ObservableField<String>} to automatically update its
 * displayed value when the underlying observable string field changes. The factory also ensures that
 * the created field is linked to an {@link ObservableView}, providing the necessary context for the field
 * interactions and coordination within the application.
 *
 * The {@link StringFieldLabelFactory} is designed for use in scenarios where a non-editable string field
 * is required, such as displaying static or read-only data.
 *
 * Methods:
 * 1. {@code create(ObservableField<String>, ObservableView)}: Initializes and returns a new {@link StringFieldLabel}
 *    instance using the provided observable field and view.
 * 2. {@code getFieldInterface()}: Returns the interface type that this factory produces, i.e., {@link StringFieldLabel}.
 */
public class StringFieldLabelFactory implements FieldFactory<String> {
    public StringFieldLabelFactory() {
    }

    /**
     * Creates a new {@link KlField} instance with a label representation for displaying string values.
     * The created {@link KlField} is initialized and bound to the provided {@link ObservableField} to allow
     * automatic updates based on changes to the underlying observable field. The {@link ObservableView} provides
     * the necessary context for interaction and coordination within the application.
     *
     * @param observableField The observable string field to be linked to the created field. Changes to this field
     *                        will automatically update the displayed value in the label representation.
     * @param observableView  The observable view context that provides application-specific settings and interactions
     *                        for the created field.
     * @return A new {@link KlField} instance configured as a read-only string field, represented using a label.
     */
    @Override
    public KlField<String> create(ObservableField<String> observableField, ObservableView observableView) {
        return StringFieldLabel.create(observableField, observableView);
    }

    @Override
    public Class<StringFieldLabel> getFieldInterface() {
        return StringFieldLabel.class;
    }

    @Override
    public String getName() {
        return "Read-only String Field Factory";
    }

    @Override
    public String getDescription() {
        return "A String field that uses a Label to present the field. " +
                "The field is not editable because the Label does not support editing.";
    }
}

