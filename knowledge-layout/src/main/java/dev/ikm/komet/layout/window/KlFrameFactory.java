package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;

/**
 * Defines a factory interface for creating instances of {@link KlRenderView}.
 * This factory provides mechanisms for creating and restoring {@link KlRenderView}
 * objects and supports customization through preferences and context configurations.
 *
 * This interface inherits from {@link KlFactory}, leveraging its generic
 * methods to handle creation, restoration, and related functionalities
 * specific to {@link KlRenderView}.
 */
public interface KlFrameFactory extends KlFactory<KlRenderView> {

}
