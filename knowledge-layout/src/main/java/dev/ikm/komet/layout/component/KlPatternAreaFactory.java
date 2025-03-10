package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservablePattern;
import javafx.scene.layout.Pane;

/**
 * An interface for creating instances of {@link KlPatternArea} associated with an {@link ObservablePattern}.
 * It extends the {@link KlComponentAreaFactory} interface, specializing in creating component panes
 * that interact with observable patterns in JavaFX applications.
 *
 * This factory allows for the dynamic binding of {@link ObservablePattern} entities to JavaFX panes,
 * facilitating the creation and initialization of UI components representing pattern-related data.
 * It ensures a consistent interface for managing and presenting pattern-related observable entities
 * within the Komet framework.
 *
 * @param <FX> the type of JavaFX {@code Pane} created by this factory.
 * @see KlPatternArea
 * @see ObservablePattern
 * @see KlComponentAreaFactory
 */
public non-sealed interface KlPatternAreaFactory<FX extends Pane>
        extends KlComponentAreaFactory<FX, KlPatternArea<FX>, ObservablePattern> {
}