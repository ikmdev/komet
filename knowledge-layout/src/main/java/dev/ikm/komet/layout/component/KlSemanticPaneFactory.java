package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableSemantic;

/**
 * A factory interface for creating instances of {@code KlSemanticPane} associated with a specific
 * type of {@code ObservableSemantic}. This interface extends {@code KlComponentPaneFactory},
 * inheriting its capabilities while focusing specifically on handling {@code KlSemanticPane}
 * and {@code ObservableSemantic} types.
 *
 * @param <T>  the type of {@code KlSemanticPane} being created by this factory
 * @param <OE> the type of {@code ObservableSemantic} that this factory works with
 */
public non-sealed interface KlSemanticPaneFactory<T extends KlSemanticPane, OE extends ObservableSemantic>
        extends KlComponentPaneFactory<T, OE> {
}
