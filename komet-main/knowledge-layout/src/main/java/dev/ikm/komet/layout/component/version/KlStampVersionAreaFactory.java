package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableStampVersion;

/**
 * The {@code KlStampVersionPaneFactory} interface is a factory for creating instances of
 * {@link KlStampVersionArea}, specialized for handling {@link ObservableStampVersion} types.
 *
 * This interface extends {@link KlVersionAreaFactory}, inheriting its general-purpose
 * methods for constructing version-related panes, and narrows the scope specifically
 * to {@link KlStampVersionArea}.
 *
 * @param <KL>  the type of {@link KlStampVersionArea} that the factory will create
 * @param <OV> the type of {@link ObservableStampVersion} that will be associated with the pane
 *
 * @see KlVersionAreaFactory
 * @see ObservableStampVersion
 * @see KlStampVersionArea
 */
public non-sealed interface KlStampVersionAreaFactory<KL extends KlStampVersionArea, OV extends ObservableStampVersion>
        extends KlVersionAreaFactory<KL, OV> {
}