package dev.ikm.komet.layout.preferences;

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
     * Creates a new instance of PreferencePropertyInteger with an initial integer value
     * and a specified ClassConceptBinding.
     *
     * @param binding the ClassConceptBinding associated with this PreferencePropertyInteger instance
     * @return a new PreferencePropertyInteger instance
     */
    protected static PreferencePropertyInteger create(ClassConceptBinding binding) {
        return new PreferencePropertyInteger(new SimpleIntegerProperty(PreferenceProperty.INITIAL_INTEGER_VALUE), binding);
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
