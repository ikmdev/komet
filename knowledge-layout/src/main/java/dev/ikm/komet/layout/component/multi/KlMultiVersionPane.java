package dev.ikm.komet.layout.component.multi;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.component.version.KlVersionPane;
import javafx.collections.ObservableList;

/**
 * The {@code KlMultiVersionPane} interface provides a contract for components handling
 * multiple versions of the same entity.
 *
 * @param <V> the type of the ObservableVersion that the pane works with
 *
 * @see KlWidget
 * @see ObservableVersion
 */
public interface KlMultiVersionPane<V extends ObservableVersion> extends KlWidget {
    /**
     * Retrieves the list of observable versions associated with an entity in this multi-version pane.
     *
     * @return an ObservableList of ObservableVersion<V> objects, representing the multiple versions of the entity managed by this pane.
     */
    ObservableList<ObservableVersion<V>> entityVersions();
    /**
     * Retrieves the list of single version panes associated with this multi-version pane.
     *
     * @return an ObservableList of KlVersionPane<V> objects, representing the individual version panes
     *         that handle and display single versions of the entity managed by this multi-version pane.
     */
    ObservableList<KlVersionPane<V>> klVersions();
}
