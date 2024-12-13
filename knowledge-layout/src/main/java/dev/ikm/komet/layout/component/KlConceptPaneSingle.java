package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableConcept;
import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import javafx.scene.Node;

/**
 * Represents a single pane component in the layout system that deals with
 * a specific observable concept and its version.
 *
 * @param <N> the type of the JavaFX node that this pane uses.
 */
public interface KlConceptPaneSingle<N extends Node> extends KlComponentPaneSingle<ObservableConceptVersion, ConceptVersionRecord> {
    ObservableConcept observableEntity();

}
