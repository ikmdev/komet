package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import javafx.scene.Node;

/**
 * Represents a single pane component in the layout system that deals with
 * a specific observable pattern.
 *
 * @param <N> the type of the JavaFX node that this pane uses.
 */
public interface KlPatternPaneSingle<N extends Node> extends KlComponentPaneSingle<ObservablePatternVersion, PatternVersionRecord> {
    ObservablePattern observableEntity();

}
