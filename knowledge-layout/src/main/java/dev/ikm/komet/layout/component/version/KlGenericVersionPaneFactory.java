package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableVersion;

/**
 * The {@code KlGenericVersionPaneFactory} interface is a specialized subtype of {@code KlVersionPaneFactory},
 * responsible for creating instances of {@link KlVersionPane} associated with the generic {@link ObservableVersion}.
 *
 * This interface supports the construction of user interface components that can represent a wide range
 * of versioned entities, without being limited to a specific observable version type. It provides a
 * general-purpose factory mechanism for creating version panes within the Komet Knowledge Layout framework.
 *
 * Key Characteristics:
 * - Non-sealed, allowing for further extension by implementations beyond the sealed hierarchy of {@code KlVersionPaneFactory}.
 * - Facilitates the association between a generic version pane and its observable version data.
 *
 * Responsibilities:
 * - Create and configure generic version panes for use with observable entities.
 * - Provide a flexible factory interface for handling observable versions of various types.
 *
 * See Also:
 * - {@link KlVersionPaneFactory}
 * - {@link KlVersionPane}
 * - {@link ObservableVersion}
 */
public non-sealed interface KlGenericVersionPaneFactory extends KlVersionPaneFactory<KlVersionPane, ObservableVersion>{
}
