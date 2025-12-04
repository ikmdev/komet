package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableConcept;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * Represents a pane bound to an observable concept within a JavaFX application.
 *
 * KlConceptPane is a specialized interface that extends the {@code KlComponentPane} interface,
 * specifically associating its observable component with an {@code ObservableConcept}.
 * This allows the pane to manage and present an observable concept while supporting properties
 * and features that enable observation and modification of its state.
 *
 * The {@code P} generic parameter defines the type of JavaFX {@code Pane} associated
 * with this interface.
 *
 * @param <P> the type of JavaFX {@code Pane} for this component.
 * @see KlComponentArea
 * @see ObservableConcept
 */
public non-sealed interface KlConceptArea<P extends Pane> extends KlComponentArea<ObservableConcept, P> {
    /**
     * Retrieves the observable concept associated with this pane.
     *
     * This method provides the current value of the observable concept by accessing
     * the JavaFX {@code ObjectProperty} that encapsulates the observable entity.
     *
     * @return the {@code ObservableConcept} associated with this pane
     */
    default ObservableConcept observableConcept() {
        return componentProperty().get();
    }

    /**
     * Retrieves the JavaFX {@code ObjectProperty} that encapsulates the
     * {@code ObservableConcept} associated with this pane. This property allows
     * for observing changes to the concept or modifying its value.
     *
     * @return the {@code ObjectProperty} holding the {@code ObservableConcept}.
     */
    default ObjectProperty<ObservableConcept> conceptProperty() {
        return componentProperty();
    }

}
