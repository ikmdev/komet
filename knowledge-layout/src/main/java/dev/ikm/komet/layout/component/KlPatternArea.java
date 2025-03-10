package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservablePattern;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * Represents a pane bound to an observable pattern within a JavaFX application.
 *
 * KlPatternPane is a specialized interface that extends the {@code KlComponentPane} interface,
 * specifically associating its observable component with an {@code ObservablePattern}.
 * This allows the pane to manage and present an observable pattern while supporting properties
 * and features that enable observation and modification of its state.
 *
 * The {@code FX} generic parameter defines the type of JavaFX {@code Pane} associated
 * with this interface.
 *
 * @param <FX> the type of JavaFX {@code Pane} for this component pane.
 * @see KlComponentArea
 * @see ObservablePattern
 */
public non-sealed interface KlPatternArea<FX extends Pane> extends KlComponentArea<ObservablePattern, FX> {

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
