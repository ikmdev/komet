package dev.ikm.komet.layout.coordinate;

import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.layout.KometPlugin;
import dev.ikm.tinkar.coordinate.ImmutableCoordinate;

/**
 * Marker interface for all coordinate plugin panes. This interface designates that a plugin pane
 * is related to coordinates within the system, and serves as a common ancestor for more
 * specific coordinate-related interfaces.
 */
public interface KlCoordinate<C extends ObservableCoordinate<IC>, IC extends ImmutableCoordinate> extends KometPlugin {
    /**
     * Retrieves the observable coordinate instance related to this plugin pane.
     *
     * @return the coordinate instance of type C.
     */
    C coordinate();
}
