package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableConcept;

/**
 * A factory interface for creating instances of {@code KlConceptPane}, which
 * represent concepts in a pane. This interface specializes the generic
 * {@code KlComponentPaneFactory} by focusing on panes associated with
 * {@code ObservableConcept} entities.
 *
 * @param <T>  the specific type of {@code KlConceptPane} created by this factory
 * @param <OE> the type of {@code ObservableConcept} associated with the created concept pane
 */
public non-sealed interface KlConceptPaneFactory<T extends KlConceptPane, OE extends ObservableConcept>
        extends KlComponentPaneFactory<T, OE> {
}