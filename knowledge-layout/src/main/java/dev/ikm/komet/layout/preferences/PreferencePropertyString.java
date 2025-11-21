package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlPeerable;
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
     * @param klPeerable the {@code KlGadget} instance associated with this preference property.
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property.
     */
    protected PreferencePropertyString(KlPeerable klPeerable, ClassConceptBinding binding) {
        super(new SimpleStringProperty(klPeerable, binding.fullyQualifiedNames().getAny(), PreferenceProperty.INITIAL_STRING_VALUE), binding);
    }

    protected PreferencePropertyString(KlPeerable klPeerable, ClassConceptBinding binding, String initialValue) {
        super(new SimpleStringProperty(klPeerable, binding.fullyQualifiedNames().getAny(), initialValue), binding);
    }

    /**
     * Creates and returns a new instance of {@code PreferencePropertyString}.
     *
     * @param klPeerable the {@code KlGadget} instance associated with the preference property.
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property.
     * @return a new instance of {@code PreferencePropertyString}.
     */
    protected static PreferencePropertyString create(KlPeerable klPeerable, ClassConceptBinding binding) {
        return new PreferencePropertyString(klPeerable, binding);
    }

    protected static PreferencePropertyString create(KlPeerable klPeerable, ClassConceptBinding binding, String initialValue) {
        return new PreferencePropertyString(klPeerable, binding, initialValue);
    }

}
