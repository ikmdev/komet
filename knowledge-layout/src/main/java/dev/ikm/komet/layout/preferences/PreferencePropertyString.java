package dev.ikm.komet.layout.preferences;

import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.property.SimpleStringProperty;

/**
 * The {@code PreferencePropertyString} class is a concrete implementation of
 * {@code PreferenceProperty} for handling string properties.
 * It is used to bind and manage preferences represented as strings.
 */
public class PreferencePropertyString extends PreferenceProperty<String, SimpleStringProperty> {

    /**
     * Constructs a new {@code PreferencePropertyString} with the given {@code ClassConceptBinding}.
     * This initializes the property with a default string value.
     *
     * @param binding the {@code ClassConceptBinding} used to bind the property
     */
    protected PreferencePropertyString(ClassConceptBinding binding) {
        super(new SimpleStringProperty(PreferenceProperty.INITIAL_STRING_VALUE), binding);
    }

    /**
     * Creates a new instance of {@code PreferencePropertyString} using the given {@code ClassConceptBinding}.
     *
     * @param binding the {@code ClassConceptBinding} used to initialize the instance
     * @return a new instance of {@code PreferencePropertyString}
     */
    protected static PreferencePropertyString create(ClassConceptBinding binding) {
        return new PreferencePropertyString(binding);
    }

}
