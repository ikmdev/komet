package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlPeerable;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * A specialized implementation of {@link PreferenceProperty} for Boolean values using {@link SimpleBooleanProperty}
 * as the underlying property type. This class provides predefined functionality for handling Boolean preference
 * properties in an application.
 */
public class PreferencePropertyBoolean extends PreferenceProperty<Boolean, SimpleBooleanProperty> {
    /**
     * Constructor for PreferencePropertyBoolean.
     *
     * @param implInstance the SimpleBooleanProperty instance representing the underlying implementation property
     * @param binding the ClassConceptBinding instance to associate with the property
     */
    private PreferencePropertyBoolean(SimpleBooleanProperty implInstance, ClassConceptBinding binding) {
        super(implInstance, binding);
    }

    /**
     * Creates a new instance of {@link PreferencePropertyBoolean} using the provided {@link KlPeerable}
     * and {@link ClassConceptBinding}.
     *
     * @param klPeerable the {@link KlPeerable} instance associated with this preference property
     * @param binding  the {@link ClassConceptBinding} instance providing binding details for the property
     * @return a new {@link PreferencePropertyBoolean} instance with the specified {@link KlPeerable} and binding
     */
    protected static PreferencePropertyBoolean create(KlPeerable klPeerable, ClassConceptBinding binding) {
        return new PreferencePropertyBoolean(new SimpleBooleanProperty(klPeerable, binding.fullyQualifiedNames().getAny(),
                PreferenceProperty.INITIAL_BOOLEAN_VALUE), binding);
    }
    protected static PreferencePropertyBoolean create(KlPeerable klPeerable, ClassConceptBinding binding, boolean initialValue) {
        return new PreferencePropertyBoolean(new SimpleBooleanProperty(klPeerable, binding.fullyQualifiedNames().getAny(),
                initialValue), binding);
    }
}
