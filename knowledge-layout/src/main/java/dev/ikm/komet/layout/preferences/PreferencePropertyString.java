package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlObject;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.property.SimpleStringProperty;

/**
 * The {@code PreferencePropertyString} class is a concrete implementation of
 * {@code PreferenceProperty} for handling string properties.
 * It is used to bind and manage preferences represented as strings.
 */
public class PreferencePropertyString extends PreferenceProperty<String, SimpleStringProperty> {

    /**
     * Constructs a new {@code PreferencePropertyString} instance.
     *
     * @param klObject the {@code KlGadget} instance associated with this preference property.
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property.
     */
    protected PreferencePropertyString(KlObject klObject, ClassConceptBinding binding) {
        super(new SimpleStringProperty(klObject, binding.fullyQualifiedNames().getAny(), PreferenceProperty.INITIAL_STRING_VALUE), binding);
    }

    /**
     * Creates and returns a new instance of {@code PreferencePropertyString}.
     *
     * @param klObject the {@code KlGadget} instance associated with the preference property.
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property.
     * @return a new instance of {@code PreferencePropertyString}.
     */
    protected static PreferencePropertyString create(KlObject klObject, ClassConceptBinding binding) {
        return new PreferencePropertyString(klObject, binding);
    }

}
