package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableConcept;
import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * Represents a specialized component area designed to handle observable concepts within a JavaFX application.
 *
 * The KlConceptArea interface extends the functional capabilities of {@code KlComponentArea}
 * by focusing specifically on observable concept entities. It provides methods for accessing
 * and working with the observable concept and its associated properties.
 *
 * This interface helps in managing and observing changes to concept-related entities and their
 * versions in a structured manner, enabling efficient updates and interactions in JavaFX-based
 * user interfaces.
 *
 * @param <FX> the type of the JavaFX {@code Pane} represented by this concept area
 * @see KlChronologyArea
 * @see ObservableConcept
 * @see ObservableConceptVersion
 * @see ConceptVersionRecord
 */
public non-sealed interface KlConceptArea<FX extends Pane>
        extends KlChronologyArea<ObservableConcept, ObservableConceptVersion, FX> {
    /**
     * Retrieves the observable concept associated with this pane.
     *
     * This method provides the current value of the observable concept by accessing
     * the JavaFX {@code ObjectProperty} that encapsulates the observable entity.
     *
     * @return the {@code ObservableConcept} associated with this pane
     */
    default ObservableConcept observableConcept() {
        return chronologyProperty().get();
    }

    /**
     * Retrieves the JavaFX {@code ObjectProperty} that encapsulates the
     * {@code ObservableConcept} associated with this pane. This property allows
     * for observing changes to the concept or modifying its value.
     *
     * @return the {@code ObjectProperty} holding the {@code ObservableConcept}.
     */
    default ObjectProperty<ObservableConcept> conceptProperty() {
        return chronologyProperty();
    }

    non-sealed interface Factory<FX extends Pane, KL extends KlConceptArea<FX>>
            extends KlChronologyArea.Factory<FX, ObservableConcept, ObservableConceptVersion, KL> {
    }
}
