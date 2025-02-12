package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlObject;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The PreferencePropertyInteger class extends PreferenceProperty to manage integer preferences
 * in the form of a SimpleIntegerProperty. It enables storing, retrieving, and binding integer values
 * as properties within a system that uses ClassConceptBinding to associate semantic meanings.
 */
public class PreferencePropertyInteger extends PreferenceProperty<Number, SimpleIntegerProperty> {
    /**
     * Constructs a PreferencePropertyInteger instance with the given SimpleIntegerProperty
     * implementation instance and associated ClassConceptBinding.
     *
     * @param implInstance the underlying SimpleIntegerProperty instance used for this property
     * @param binding the ClassConceptBinding associated with this PreferencePropertyInteger instance
     */
    private PreferencePropertyInteger(SimpleIntegerProperty implInstance, ClassConceptBinding binding) {
        super(implInstance, binding);
    }

    /**
     * Creates a new instance of {@code PreferencePropertyInteger} associated with the given
     * {@code KlObject} and {@code ClassConceptBinding}.
     *
     * @param klObject the {@code KlObject} instance to associate with this property
     * @param binding  the {@code ClassConceptBinding} providing the fully qualified names
     *                 for associating semantic meanings
     * @return a new {@code PreferencePropertyInteger} instance
     */
    protected static PreferencePropertyInteger create(KlObject klObject, ClassConceptBinding binding) {
        return new PreferencePropertyInteger(new SimpleIntegerProperty(klObject, binding.fullyQualifiedNames().getAny(),
                PreferenceProperty.INITIAL_INTEGER_VALUE), binding);
    }

    /**
     * Retrieves the current integer value held by the underlying {@code SimpleIntegerProperty} implementation.
     *
     * @return the current integer value of the property
     */
    public int get() {
        return implInstance.get();
    }

    /**
     * Sets a new integer value for the underlying SimpleIntegerProperty implementation.
     *
     * @param newValue the new integer value to be set
     */
    public void set(int newValue) {
        implInstance.set(newValue);
    }
}
