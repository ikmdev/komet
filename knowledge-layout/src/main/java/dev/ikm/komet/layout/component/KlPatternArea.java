package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * Represents a specialized component area in a JavaFX application associated
 * with observable patterns and their versions.
 *
 * The KlPatternArea interface extends the KlComponentArea interface, focusing
 * on managing observable patterns and their related version records within a
 * JavaFX pane. It provides methods to access the underlying observable pattern
 * and its associated JavaFX property for observing or modifying its state.
 *
 * This interface acts as a key abstraction for UI components specifically
 * designed to interact with observable patterns and their lifecycle.
 *
 * @param <FX> the type of the JavaFX {@code Pane} used for this pattern area
 * @see KlChronologyArea
 * @see ObservablePattern
 * @see ObservablePatternVersion
 * @see PatternVersionRecord
 */
public non-sealed interface KlPatternArea<FX extends Pane>
        extends KlChronologyArea<ObservablePattern, ObservablePatternVersion, FX> {

    /**
     * Retrieves the observable pattern associated with this pane.
     *
     * This method provides the current value of the observable pattern by accessing
     * the JavaFX {@code ObjectProperty} that encapsulates the observable entity.
     *
     * @return the {@code ObservablePattern} associated with this pane
     */
    default ObservablePattern observablePattern() {
        return chronologyProperty().get();
    }

    /**
     * Retrieves the JavaFX {@code ObjectProperty} that encapsulates the
     * {@code ObservablePattern} associated with this pane. This property allows
     * for observing changes to the pattern or modifying its value.
     *
     * @return the {@code ObjectProperty} holding the {@code ObservablePattern}.
     */
    default ObjectProperty<ObservablePattern> patternProperty() {
        return chronologyProperty();
    }

    non-sealed interface Factory<FX extends Pane, KL extends KlPatternArea<FX>>
            extends KlChronologyArea.Factory<FX, ObservablePattern, ObservablePatternVersion, KL> {
    }
}
