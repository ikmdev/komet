package dev.ikm.komet.layout.preferences;

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
     * Creates a new instance of PreferencePropertyBoolean with a SimpleBooleanProperty initialized
     * to the default INITIAL_BOOLEAN_VALUE and the specified ClassConceptBinding.
     *
     * @param binding the ClassConceptBinding instance to associate with the created PreferencePropertyBoolean
     * @return a new instance of PreferencePropertyBoolean
     */
    protected static PreferencePropertyBoolean create(ClassConceptBinding binding) {
        return new PreferencePropertyBoolean(new SimpleBooleanProperty(PreferenceProperty.INITIAL_BOOLEAN_VALUE), binding);
    }
}
