package dev.ikm.komet.layout.preferences;

import dev.ikm.tinkar.common.bind.ClassConceptBinding;

/**
 * Represents a property that is capable of providing a default value. Classes implementing this
 * interface define properties with an associated default value that can be used when no specific
 * value is provided or configured.
 */
public interface PropertyWithDefault extends ClassConceptBinding {
    /**
     * Retrieves the default value associated with a property.
     *
     * @return the default value of the property, or {@code null} if no default value is defined
     */
    Object defaultValue();
}
