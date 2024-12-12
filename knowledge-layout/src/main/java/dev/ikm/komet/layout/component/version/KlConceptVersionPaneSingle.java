package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableConceptVersion;

/**
 * The {@code KlConceptVersionPaneSingle} interface represents a pane that displays a single version
 * of a concept.
 *
 * <p>
 * This interface is a specialization of {@link KlVersionPaneSingle} for handling
 * {@link ObservableConceptVersion} types.
 * </p>
 *
 * @param <T> the type of the version to be handled by this pane
 *
 * @see KlVersionPaneSingle
 * @see ObservableConceptVersion
 */
public interface KlConceptVersionPaneSingle extends KlVersionPaneSingle<ObservableConceptVersion> {

}
