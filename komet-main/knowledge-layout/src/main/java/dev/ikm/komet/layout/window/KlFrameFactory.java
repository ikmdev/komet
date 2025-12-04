package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;

/**
 * Defines a factory interface for creating instances of {@link KlFrame}.
 * This factory provides mechanisms for creating and restoring {@link KlFrame}
 * objects and supports customization through preferences and context configurations.
 *
 * This interface inherits from {@link KlFactory}, leveraging its generic
 * methods to handle creation, restoration, and related functionalities
 * specific to {@link KlFrame}.
 */
public interface KlFrameFactory extends KlFactory<KlFrame> {

}
