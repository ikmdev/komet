package dev.ikm.komet.layout.component.multi;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.component.KlComponentPane;
import javafx.collections.ObservableList;

/**
 * The {@code KlMultiComponentPane} interface provides a contract for presenting
 * multiple observable entities within a pane.
 *
 * @param <OE> the type parameter that extends {@link ObservableEntity}, representing
 * the specific observable entity type managed by this pane.
 *
 * @see KlWidget
 * @see ObservableEntity
 * @see KlComponentPane
 */
public interface KlMultiComponentPane<OE extends ObservableEntity> extends KlWidget {
    /**
     * Retrieves the list of observable entities associated with this pane.
     *
     * @return an ObservableList of ObservableEntity objects, representing the entities and their versions contained within this pane.
     */
    ObservableList<OE> entities();
    /**
     * Retrieves the list of single pane components associated with this multi-component pane.
     *
     * @return an ObservableList of KlComponentPaneSingle objects, representing the individual component panes contained within this multi-component pane.
     */
    ObservableList<KlComponentPane<OE>> klComponents();
}
