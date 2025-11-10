package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.layout.Pane;

/**
 * The {@code KlConceptVersionPane} interface represents a pane that displays a single version
 * of a concept.
 *
 * <p>
 * This interface is a specialization of {@link KlAreaForVersion} for handling
 * {@link ObservableConceptVersion} types.
 * </p>
 *
 *
 * @see KlAreaForVersion
 * @see ObservableConceptVersion
 */
public non-sealed interface KlAreaForConceptVersion<FX extends Pane> extends KlAreaForVersion<ObservableConceptVersion, FX> {
    /**
     * Returns the observable concept version associated with this pane.
     * This method provides a convenience wrapper for getting the version
     * from the {@code KlVersionPane} interface.
     *
     * @return the observable concept version associated with this pane
     */
    default ObservableConceptVersion conceptVersion() {
        return KlAreaForVersion.super.version();
    }

    /**
     * Returns the property representing the observable concept version associated with this pane.
     *
     * @return the property containing the observable concept version
     */
    default ReadOnlyProperty<ObservableConceptVersion> conceptVersionProperty() {
        return versionProperty();
    }

    non-sealed interface Factory<FX extends Pane, KL extends KlAreaForConceptVersion<FX>>
            extends KlAreaForVersion.Factory<FX, ObservableConceptVersion, KL> {

    }
}
