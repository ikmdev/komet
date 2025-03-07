package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableSemanticVersion;

/**
 * The {@code KlSemanticVersionPaneFactory} interface is a specialized factory interface for creating instances of
 * {@link KlSemanticVersionArea} configured with {@link ObservableSemanticVersion} objects within the
 * Komet Knowledge Layout framework.
 *
 * This factory serves as an extension of {@link KlVersionAreaFactory}, providing a contract specifically
 * for generating UI components that represent semantic versions in a structured and consistent manner.
 *
 * @param <KL> the type of {@link KlSemanticVersionArea} that this factory creates
 * @param <OV> the type of {@link ObservableSemanticVersion} associated with the pane
 *
 * @see KlSemanticVersionArea
 * @see ObservableSemanticVersion
 * @see KlVersionAreaFactory
 */
public non-sealed interface KlSemanticVersionAreaFactory<KL extends KlSemanticVersionArea, OV extends ObservableSemanticVersion>
        extends KlVersionAreaFactory<KL, OV> {
}