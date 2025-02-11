package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservablePattern;

/**
 * A specialized factory interface for creating instances of `KlPatternPane` associated with
 * an `ObservablePattern`. This factory is responsible for providing tailored creation
 * methods, ensuring that the resulting panes are properly configured and associated
 * with the specified observable pattern entities.
 *
 * @param <T>  the type of `KlPatternPane` produced by the factory
 * @param <OE> the type of `ObservablePattern` associated with the `KlPatternPane`
 */
public non-sealed interface KlPatternPaneFactory<T extends KlPatternPane, OE extends ObservablePattern>
        extends KlComponentPaneFactory<T, OE> {
}