package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.KometPlugin;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.collections.ObservableList;

/**
 * Represents a pane that can contain multiple observable components, each with an associated version.
 * This pane will allow layout for multiple components of the same type, such as might be used to compare
 * semantics side by side.
 *
 * @param <O> the type of the observable version
 * @param <V> the type of the entity version
 */
public interface KlMultiComponentPane<O extends ObservableVersion<V>, V extends EntityVersion> extends KometPlugin {
    /**
     * Retrieves the list of observable entities associated with this pane.
     *
     * @return an ObservableList of ObservableEntity objects, representing the entities and their versions contained within this pane.
     */
    ObservableList<ObservableEntity<O, V>> entities();
    /**
     * Retrieves the list of single pane components associated with this multi-component pane.
     *
     * @return an ObservableList of KlComponentPaneSingle objects, representing the individual component panes contained within this multi-component pane.
     */
    ObservableList<KlComponentPaneSingle<O,V>> klComponents();
}
