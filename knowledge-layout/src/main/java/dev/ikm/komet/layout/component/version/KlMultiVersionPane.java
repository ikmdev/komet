package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.KometPlugin;
import javafx.collections.ObservableList;

/**
 * The {@code KlMultiVersionPane} interface provides a contract for components handling
 * multiple versions of the same entity.
 *
 * @param <V> the type of the ObservableVersion that the pane works with
 *
 * @see KometPlugin
 * @see ObservableVersion
 */
public interface KlMultiVersionPane<V extends ObservableVersion> extends KometPlugin {
    ObservableList<ObservableVersion<V>> entityVersions();
    ObservableList<KlVersionPaneSingle<V>> klVersions();
}
