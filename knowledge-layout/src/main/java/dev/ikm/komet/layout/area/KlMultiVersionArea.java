package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.KlAreaForListOfVersions.VersionsAndSelection;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

/**
 * The {@code KlMultiVersionPane} interface provides a contract for components handling
 * multiple versions of the same entity.
 *
 * @param <OV> the type of the ObservableVersion that the pane works with
 *
 * @see KlArea
 * @see ObservableVersion
 */
public non-sealed interface KlMultiVersionArea<OV extends ObservableVersion<EntityVersion>, FX extends Pane>
        extends KlArea<FX> {

    /**
     * Configures the component with a read-only property that encapsulates information
     * about the versions and the selected versions of an entity.
     *
     * @param versionsAndSelectionProperty a {@code ReadOnlyObjectProperty} that contains
     *                                      a {@code VersionsAndSelection}
     *                                      object, representing the list of observable versions
     *                                      and the subset of selected versions.
     */
    void setVersionAndSelectionProperty(ReadOnlyObjectProperty<VersionsAndSelection> versionsAndSelectionProperty);


    /**
     * Retrieves the list of single version panes associated with this multi-version pane.
     *
     * @return an ObservableList of KlVersionPane<V> objects, representing the individual version panes
     *         that handle and display single versions of the entity managed by this multi-version pane.
     */
    ObservableList<KlAreaForVersion<OV, FX>> klVersionAreas();

    non-sealed interface Factory<FX extends Pane, OV extends ObservableVersion<EntityVersion>, KL extends KlMultiVersionArea<OV, FX>>
            extends KlArea.Factory<FX, KL> {
    }

}
