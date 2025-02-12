package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableConceptVersion;

/**
 * The {@code KlConceptVersionPaneFactory} interface provides a contract for creating
 * instances of {@link KlConceptVersionPane} that are associated with {@link ObservableConceptVersion}.
 *
 * This interface extends from {@link KlVersionPaneFactory}, inheriting its generic contract for
 * creating version panes with specific observable versions, while specializing in the context
 * of concept versions.
 *
 * Type Parameters:
 * - {@code T}: A concrete implementation of the {@link KlConceptVersionPane} interface.
 * - {@code OV}: The specific type of {@link ObservableConceptVersion} that the factory
 *   works with and is associated with the created panes.
 *
 * Responsibilities:
 * - Define the creation process for concept version panes.
 * - Ensure the created panes are tailored to handle {@link ObservableConceptVersion}.
 *
 * Use Cases:
 * - Intended for creating and managing layout components that display and interact with
 *   concept version data in a user interface.
 *
 * Integration Notes:
 * - Designed as part of the Komet Knowledge Layout framework to support concept version management.
 * - Works seamlessly with other components within the versioning and layout system.
 *
 * @param <T> the type of the {@link KlConceptVersionPane}.
 * @param <OV> the type of the {@link ObservableConceptVersion}.
 *
 * @see KlConceptVersionPane
 * @see ObservableConceptVersion
 * @see KlVersionPaneFactory
 */
public non-sealed interface KlConceptVersionPaneFactory<T extends KlConceptVersionPane, OV extends ObservableConceptVersion>
        extends KlVersionPaneFactory<T, OV> {
}
