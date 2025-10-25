package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlParent;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.layout.Pane;

/**
 * The {@code KlVersionPane} interface represents a pane that displays a single version of an entity.
 *
 * @param <OV> the type of the ObservableVersion that the pane works with
 *
 * @see ObservableVersion
 */
public sealed interface KlAreaForVersion<OV extends ObservableVersion<?>, FX extends Pane> extends KlArea<FX>, KlParent<FX>
        permits KlAreaForConceptVersion, KlAreaForGenericVersion, KlAreaForPatternVersion, KlAreaForSemanticVersion, KlAreaForStampVersion {
    /**
     * Returns the version of the observable entity associated with this pane.
     *
     * @return the version of the observable entity
     */
    default OV version() {
        return versionProperty().getValue();
    }

    /**
     * Returns the property representing the version of the observable entity associated with this pane.
     *
     * @return the property containing the version of the observable entity
     */
    ReadOnlyProperty<OV> versionProperty();

    sealed interface Factory<FX extends Pane, OV extends ObservableVersion<?>,
            KL extends KlAreaForVersion<OV, FX>>
            extends KlArea.Factory<FX, KL>
            permits KlAreaForConceptVersion.Factory, KlAreaForGenericVersion.Factory, KlAreaForPatternVersion.Factory,
                    KlAreaForSemanticVersion.Factory, KlAreaForStampVersion.Factory {


    }
}
