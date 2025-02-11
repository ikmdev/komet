package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableConcept;
import javafx.beans.property.ObjectProperty;

/**
 * Represents a single Concept presented in a Pane.
 */
public non-sealed interface KlConceptPane extends KlComponentPane<ObservableConcept> {
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
