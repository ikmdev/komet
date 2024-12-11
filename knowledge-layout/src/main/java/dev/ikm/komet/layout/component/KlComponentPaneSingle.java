package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.layout.KometPlugin;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.komet.framework.observable.ObservableVersion;

import java.util.UUID;

/**
 * Represents a single pane component in the layout system that deals with
 * a specific observable entity and its version.
 *
 * @param <O> the type of the observable version
 * @param <V> the type of the entity version
 */
public interface KlComponentPaneSingle<O extends ObservableVersion<V>, V extends EntityVersion>
        extends KometPlugin {
    /**
     * Retrieves the observable entity associated with this pane.
     *
     * @return the observable entity of type ObservableEntity.
     */
    ObservableEntity<O, V> observableEntity();
    /**
     * Retrieves the unique identifier for this KlComponent. Note that the UUID for the
     * KlComponent is independent of whatever entity it may contain at a particular instant. And the
     * UUID will not change across the life of this Knowledge Layout Component.
     *
     * @return the UUID representing the unique identifier of the KlComponent.
     */
    UUID klComponentId();
}
