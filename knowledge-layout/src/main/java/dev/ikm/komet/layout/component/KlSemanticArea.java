package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableSemantic;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * Represents a pane bound to an observable semantic within a JavaFX application.
 *
 * The KlSemanticPane interface is a specialized extension of the KlComponentPane
 * interface, specifically associating its observable component with an {@code ObservableSemantic}.
 * This enables the pane to manage and present an observable semantic while supporting properties
 * and features that facilitate observation and modification of its state.
 *
 * By implementing this interface, one can seamlessly work with observable semantic entities
 * and their corresponding JavaFX {@code Pane}, fostering integration between the UI components
 * and the underlying semantic-related observable data.
 *
 * @param <FX> the type of JavaFX {@code Pane} associated with this semantic pane
 * @see KlComponentArea
 * @see ObservableSemantic
 */
public non-sealed interface KlSemanticArea<FX extends Pane> extends KlComponentArea<ObservableSemantic, FX> {

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
        return componentProperty().get();
    }

    /**
     * Retrieves the JavaFX {@code ObjectProperty} encapsulating the
     * {@code ObservableSemantic} associated with this pane. This property
     * allows for observing changes to the semantic pattern or modifying its value.
     *
     * @return the {@code ObjectProperty} holding the {@code ObservableSemantic}.
     */
    default ObjectProperty<ObservableSemantic> patternSemantic() {
        return componentProperty();
    }

}
