package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlPeerable;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Represents a specialized preference property object for handling instances of {@code Encodable}
 * within a JavaFX property structure. This class is intended to provide integration with
 * {@code KlGadget} and {@code ClassConceptBinding} for managing and binding preference-based
 * property values.
 */
public class PreferencePropertyObject<E extends Encodable> extends PreferenceProperty<E, SimpleObjectProperty<E>> {

    /**
     * Constructs an instance of {@code PreferencePropertyObject} with the specified gadget and binding.
     *
     * @param klPeerable the {@code KlObject} instance associated with the preference property.
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property.
     */
    protected PreferencePropertyObject(KlPeerable klPeerable, ClassConceptBinding binding) {
        super(new SimpleObjectProperty<E>(klPeerable, binding.fullyQualifiedNames().getAny(),
                null), binding);
    }

    protected PreferencePropertyObject(KlPeerable klPeerable, ClassConceptBinding binding, E initialValue) {
        super(new SimpleObjectProperty<E>(klPeerable, binding.fullyQualifiedNames().getAny(),
                initialValue), binding);
    }

    /**
     * Creates and returns a new instance of {@code PreferencePropertyObject} using the specified gadget and binding.
     *
     * @param klPeerable the {@code KlObject} instance associated with the preference property.
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property.
     * @return a newly created {@code PreferencePropertyObject} instance with the provided klObject and binding.
     */
    protected static PreferencePropertyObject create(KlPeerable klPeerable, ClassConceptBinding binding) {
        return new PreferencePropertyObject(klPeerable, binding);
    }

    protected static PreferencePropertyObject create(KlPeerable klPeerable, ClassConceptBinding binding, Encodable initialValue) {
        return new PreferencePropertyObject(klPeerable, binding, initialValue);
    }

}
