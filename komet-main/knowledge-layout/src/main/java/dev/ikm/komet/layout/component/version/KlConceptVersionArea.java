package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * The {@code KlConceptVersionPane} interface represents a pane that displays a single version
 * of a concept.
 *
 * <p>
 * This interface is a specialization of {@link KlVersionArea} for handling
 * {@link ObservableConceptVersion} types.
 * </p>
 *
 *
 * @see KlVersionArea
 * @see ObservableConceptVersion
 */
public non-sealed interface KlConceptVersionArea<P extends Pane> extends KlVersionArea<ObservableConceptVersion, P> {
    /**
     * Returns the observable concept version associated with this pane.
     * This method provides a convenience wrapper for getting the version
     * from the {@code KlVersionPane} interface.
     *
     * @return the observable concept version associated with this pane
     */
    default ObservableConceptVersion conceptVersion() {
        return KlVersionArea.super.version();
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
