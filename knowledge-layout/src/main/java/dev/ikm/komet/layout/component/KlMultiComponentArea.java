package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.layout.KlArea;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * The {@code KlMultiComponentPane} interface provides a contract for presenting
 * multiple observable entities within a pane.
 *
 * @param <OE> the type parameter that extends {@link ObservableEntity}, representing
 * the specific observable entity type managed by this pane.
 *
 * @see KlArea
 * @see ObservableEntity
 * @see KlChronologyArea
 */
public non-sealed interface KlMultiComponentArea<OE extends ObservableEntity, FX extends Pane> extends KlArea<FX> {
    /**
     * Retrieves the list of observable entities associated with this pane.
     *
     * @return an ObservableList of ObservableEntity objects, representing the entities and their versions contained within this pane.
     */
    ObservableList<OE> observableEntities();
    /**
     * Retrieves the list of single pane components associated with this multi-component pane.
     *
     * @return an ObservableList of KlComponentPane objects, representing the individual component panes contained within this multi-component pane.
     */
    ObservableList<KlChronologyArea> klComponentAreas();


    non-sealed interface Factory<FX extends Region, KL extends KlArea<FX>> extends KlArea.Factory<FX, KL> {

    }

}
