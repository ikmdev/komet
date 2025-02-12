package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableSemantic;
import javafx.beans.property.ObjectProperty;

/**
 * Represents a single Pattern presented in a Pane.
 */
public non-sealed interface KlSemanticPane extends KlComponentPane<ObservableSemantic> {

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
