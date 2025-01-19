package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * {@code PreferencePropertyDouble} is a specialized implementation of {@code PreferenceProperty},
 * used for managing preferences with {@code double} values in a JavaFX property context.
 * It bridges a {@code SimpleDoubleProperty} to the preference management system,
 * enabling synchronization between application state and user-configurable settings
 * in the form of {@code double} values.
 */
public class PreferencePropertyDouble extends PreferenceProperty<Number, SimpleDoubleProperty> {

    /**
     * Constructs an instance of {@code PreferencePropertyDouble}.
     *
     * @param implInstance the {@code SimpleDoubleProperty} instance that serves as the underlying implementation
     * @param binding the {@code ClassConceptBinding} providing the class context and interaction configuration for the property
     */
    protected PreferencePropertyDouble(SimpleDoubleProperty implInstance, ClassConceptBinding binding) {
        super(implInstance, binding);
    }

    /**
     * Creates a new instance of {@code PreferencePropertyDouble} with the specified gadget and binding.
     *
     * @param gadget the {@code KlGadget} instance associated with the property
     * @param binding the {@code ClassConceptBinding} providing class-related context and property interaction details
     * @return a new {@code PreferencePropertyDouble} initialized with the provided gadget and binding
     */
    protected static PreferencePropertyDouble create(KlGadget gadget, ClassConceptBinding binding) {
        return new PreferencePropertyDouble(new SimpleDoubleProperty(gadget, binding.fullyQualifiedNames().getAny(),
                PreferenceProperty.INITIAL_DOUBLE_VALUE), binding);
    }

    /**
     * Retrieves the current value of the underlying {@code SimpleDoubleProperty}.
     *
     * @return the current value as a {@code double}.
     */
    public double get() {
        return implInstance.get();
    }

    /**
     * Retrieves the primitive {@code double} value of the underlying {@code SimpleDoubleProperty}.
     *
     * @return the current value as a {@code double}.
     */
    public double doubleValue() {
        return implInstance.doubleValue();
    }

    /**
     * Retrieves the current value of the underlying property as a {@code Double} object.
     *
     * @return the current value of the property, or {@code null} if the value is not set.
     */
    public Double getValue() {
        return implInstance.getValue();
    }
}
