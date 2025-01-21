package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Represents a specialized preference property object for handling instances of {@code Encodable}
 * within a JavaFX property structure. This class is intended to provide integration with
 * {@code KlGadget} and {@code ClassConceptBinding} for managing and binding preference-based
 * property values.
 */
public class PreferencesPropertyObject<E extends Encodable> extends PreferenceProperty<E, SimpleObjectProperty<E>> {

    /**
     * Constructs an instance of {@code PreferencesPropertyObject} with the specified gadget and binding.
     *
     * @param gadget  the {@code KlGadget} instance associated with the preference property.
     * @param binding the {@code ClassConceptBinding} used to define bindings and initialize the property.
     */
    protected PreferencesPropertyObject(KlGadget gadget, ClassConceptBinding binding) {
        super(new SimpleObjectProperty<E>(gadget, binding.fullyQualifiedNames().getAny(),
                (E) PreferenceProperty.INITIAL_ENCODABLE_VALUE), binding);
    }

    /**
     * Creates and returns a new instance of {@code PreferencesPropertyObject} using the specified gadget and binding.
     *
     * @param gadget  the {@code KlGadget} instance associated with the preference property.
     * @param binding the {@code ClassConceptBinding} used to define bindings and initialize the property.
     * @return a newly created {@code PreferencesPropertyObject} instance with the provided gadget and binding.
     */
    protected static PreferencesPropertyObject create(KlGadget gadget, ClassConceptBinding binding) {
        return new PreferencesPropertyObject(gadget, binding);
    }

}
