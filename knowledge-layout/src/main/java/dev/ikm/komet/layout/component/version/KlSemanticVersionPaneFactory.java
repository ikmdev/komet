package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableSemanticVersion;

/**
 * The {@code KlSemanticVersionPaneFactory} interface is a specialized factory interface for creating instances of
 * {@link KlSemanticVersionPane} configured with {@link ObservableSemanticVersion} objects within the
 * Komet Knowledge Layout framework.
 *
 * This factory serves as an extension of {@link KlVersionPaneFactory}, providing a contract specifically
 * for generating UI components that represent semantic versions in a structured and consistent manner.
 *
 * @param <T> the type of {@link KlSemanticVersionPane} that this factory creates
 * @param <OV> the type of {@link ObservableSemanticVersion} associated with the pane
 *
 * @see KlSemanticVersionPane
 * @see ObservableSemanticVersion
 * @see KlVersionPaneFactory
 */
public non-sealed interface KlSemanticVersionPaneFactory<T extends KlSemanticVersionPane, OV extends ObservableSemanticVersion>
        extends KlVersionPaneFactory<T, OV> {
}