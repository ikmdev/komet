package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.ObservableEntity;

public interface KlEntityType<OE extends ObservableEntity> {
    /**
     * Retrieves the class type of the observable entity class that this factory creates components for.
     * Enables runtime access to the generic entity class that would otherwise be erased.
     *
     * @return A {@link Class} object representing the type of the observable entity (OE).
     */
    Class<OE> entityType();
}
