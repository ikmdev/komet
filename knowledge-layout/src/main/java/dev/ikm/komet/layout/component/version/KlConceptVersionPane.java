package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import javafx.beans.property.ObjectProperty;

/**
 * The {@code KlConceptVersionPane} interface represents a pane that displays a single version
 * of a concept.
 *
 * <p>
 * This interface is a specialization of {@link KlVersionPane} for handling
 * {@link ObservableConceptVersion} types.
 * </p>
 *
 *
 * @see KlVersionPane
 * @see ObservableConceptVersion
 */
public non-sealed interface KlConceptVersionPane extends KlVersionPane<ObservableConceptVersion> {
    /**
     * Returns the observable concept version associated with this pane.
     * This method provides a convenience wrapper for getting the version
     * from the {@code KlVersionPane} interface.
     *
     * @return the observable concept version associated with this pane
     */
    default ObservableConceptVersion conceptVersion() {
        return KlVersionPane.super.version();
    }

    /**
     * Returns the property representing the observable concept version associated with this pane.
     *
     * @return the property containing the observable concept version
     */
    default ObjectProperty<ObservableConceptVersion> conceptVersionProperty() {
        return versionProperty();
    }
}
