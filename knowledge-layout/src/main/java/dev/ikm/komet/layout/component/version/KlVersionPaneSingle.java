package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.KometPlugin;

/**
 * The {@code KlVersionPaneSingle} interface represents a pane that displays a single version of an entity.
 *
 * @param <V> the type of the ObservableVersion that the pane works with
 *
 * @see KometPlugin
 * @see ObservableVersion
 */
public interface KlVersionPaneSingle<V extends ObservableVersion> extends KometPlugin {
    /**
     * Returns the version of the observable entity associated with this pane.
     *
     * @return the version of the observable entity
     */
    V version();
}
