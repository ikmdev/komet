package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableSemanticVersion;

/**
 * The {@code KlSemanticVersionPaneSingle} interface represents a pane that displays a single version
 * of a semantic entity.
 *
 * This interface is a specialization of {@link KlVersionPaneSingle} for handling
 * {@link ObservableSemanticVersion} types.
 *
 * @see KlVersionPaneSingle
 * @see ObservableSemanticVersion
 */
public interface KlSemanticVersionPaneSingle extends KlVersionPaneSingle<ObservableSemanticVersion> {
}
