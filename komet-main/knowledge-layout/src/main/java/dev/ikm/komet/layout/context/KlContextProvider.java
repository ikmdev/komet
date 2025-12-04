package dev.ikm.komet.layout.context;

import dev.ikm.komet.layout.KlObject;

/**
 * Provides an interface for obtaining a {@link KlContext}, which represents
 * contextual information for layout orchestration and user interface configuration.
 * Implementing classes define the mechanism to supply the associated context.
 */
public interface KlContextProvider {
    /**
     * Retrieves the KlContext associated with the implementing KlContextProvider.
     *
     * @return the KlContext instance representing the contextual information for
     *         layout orchestration and user interface configuration
     */
    KlContext context();

    /**
     * Retrieves the {@code KlObject} instance associated with the implementing KlContextProvider.
     *
     * @return the {@code KlObject} instance representing a gadget configuration or functionality
     *         within the broader knowledge layout orchestration context
     */
    default KlObject klObject() {
        return (KlObject) this;
    }

}
