package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableStampVersion;

/**
 * The {@code KlStampVersionPaneFactory} interface is a factory for creating instances of
 * {@link KlStampVersionPane}, specialized for handling {@link ObservableStampVersion} types.
 *
 * This interface extends {@link KlVersionPaneFactory}, inheriting its general-purpose
 * methods for constructing version-related panes, and narrows the scope specifically
 * to {@link KlStampVersionPane}.
 *
 * @param <T>  the type of {@link KlStampVersionPane} that the factory will create
 * @param <OV> the type of {@link ObservableStampVersion} that will be associated with the pane
 *
 * @see KlVersionPaneFactory
 * @see ObservableStampVersion
 * @see KlStampVersionPane
 */
public non-sealed interface KlStampVersionPaneFactory<T extends KlStampVersionPane, OV extends ObservableStampVersion>
        extends KlVersionPaneFactory<T, OV> {
}