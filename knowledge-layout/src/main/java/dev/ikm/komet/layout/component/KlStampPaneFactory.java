package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableStamp;

/**
 * A factory interface for creating instances of KlStampPane associated with a specific
 * ObservableStamp. Extends the generic functionality provided by KlComponentPaneFactory
 * by specializing it for handling stamp-related panes.
 *
 * @param <T> the type of KlStampPane created by this factory
 * @param <OE> the type of ObservableStamp associated with the KlStampPane
 */
public non-sealed interface KlStampPaneFactory<T extends KlStampPane, OE extends ObservableStamp>
        extends KlComponentPaneFactory<T, OE> {
}
