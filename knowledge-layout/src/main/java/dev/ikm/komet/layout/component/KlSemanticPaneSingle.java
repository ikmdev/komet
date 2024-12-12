package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.tinkar.entity.SemanticVersionRecord;

/**
 * Represents a single pane component in the layout system that deals with
 * a specific observable semantic and its version.
 */
public interface KlSemanticPaneSingle extends KlComponentPaneSingle<ObservableSemanticVersion, SemanticVersionRecord> {
    ObservableSemantic observableEntity();
}
