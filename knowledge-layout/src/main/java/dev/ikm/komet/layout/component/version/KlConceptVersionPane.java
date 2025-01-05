package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableConceptVersion;
/**
 * The {@code KlVersionPane} interface represents a pane that displays a single version
 * of a concept.
 *
 * <p>
 * This interface is a specialization of {@link KlVersionPane} for handling
 * {@link ObservableConceptVersion} types.
 * </p>
 *
 *
 * @see KlVersionPane
 * @see ObservableConceptVersion
 */
public non-sealed interface KlConceptVersionPane extends KlVersionPane<ObservableConceptVersion> {

}
