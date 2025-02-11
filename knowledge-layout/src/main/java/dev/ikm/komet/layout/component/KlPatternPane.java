package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservablePattern;
import javafx.beans.property.ObjectProperty;

/**
 * Represents a single Pattern presented in a Pane.
 */
public non-sealed interface KlPatternPane extends KlComponentPane<ObservablePattern> {

    /**
     * Retrieves the observable pattern associated with this pane.
     *
     * This method provides the current value of the observable pattern by accessing
     * the JavaFX {@code ObjectProperty} that encapsulates the observable entity.
     *
     * @return the {@code ObservablePattern} associated with this pane
     */
    default ObservablePattern observablePattern() {
        return componentProperty().get();
    }

    /**
     * Retrieves the JavaFX {@code ObjectProperty} that encapsulates the
     * {@code ObservablePattern} associated with this pane. This property allows
     * for observing changes to the pattern or modifying its value.
     *
     * @return the {@code ObjectProperty} holding the {@code ObservablePattern}.
     */
    default ObjectProperty<ObservablePattern> patternProperty() {
        return componentProperty();
    }

}
