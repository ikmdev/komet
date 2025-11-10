package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * Represents a semantic-specific component area within a JavaFX application that manages
 * observable semantic entities.
 *
 * The KlSemanticArea interface extends the functionality of KlComponentArea, providing
 * additional methods specific to managing and interacting with observable semantics and
 * their corresponding patterns. This interface facilitates the integration of semantic
 * logic into JavaFX-based user interface components.
 *
 * @param <FX> the type of the JavaFX {@code Pane} representing this semantic area
 * @see KlChronologyArea
 */
public non-sealed interface KlSemanticArea<FX extends Pane>
        extends KlChronologyArea<ObservableSemantic, ObservableSemanticVersion, FX> {

    /**
     * Retrieves the observable semantic pattern associated with this pane.
     *
     * This method provides access to the current value of the observable semantic
     * pattern by getting the value from the JavaFX {@code ObjectProperty} that
     * encapsulates the observable entity.
     *
     * @return the {@code ObservableSemantic} associated with this pane
     */
    default ObservableSemantic observablePattern() {
        return chronologyProperty().get();
    }

    /**
     * Retrieves the JavaFX {@code ObjectProperty} encapsulating the
     * {@code ObservableSemantic} associated with this pane. This property
     * allows for observing changes to the semantic pattern or modifying its value.
     *
     * @return the {@code ObjectProperty} holding the {@code ObservableSemantic}.
     */
    default ObjectProperty<ObservableSemantic> patternSemantic() {
        return chronologyProperty();
    }

    non-sealed interface Factory<FX extends Pane, KL extends KlSemanticArea<FX>>
            extends KlChronologyArea.Factory<FX, ObservableSemantic, ObservableSemanticVersion, KL> {
    }
}
