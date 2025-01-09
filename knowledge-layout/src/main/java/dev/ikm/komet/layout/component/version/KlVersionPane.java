package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.component.KlPane;

/**
 * The {@code KlVersionPane} interface represents a pane that displays a single version of an entity.
 *
 * @param <OV> the type of the ObservableVersion that the pane works with
 *
 * @see ObservableVersion
 */
public interface KlVersionPane <OV extends ObservableVersion> extends KlPane {
    /**
     * Returns the version of the observable entity associated with this pane.
     *
     * @return the version of the observable entity
     */
    OV version();
}
