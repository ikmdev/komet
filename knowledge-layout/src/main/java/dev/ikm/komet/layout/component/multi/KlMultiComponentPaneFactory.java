package dev.ikm.komet.layout.component.multi;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlEntityType;
import dev.ikm.komet.layout.window.KlWindowPaneFactory;
import dev.ikm.komet.preferences.KometPreferences;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * The KlMultiComponentPaneFactory interface is responsible for creating instances of KlMultiComponentPane
 * tailored to a specified list of observable entities, a given observable view, and user preferences.
 * This factory provides a mechanism to define a concrete implementation of the multi-component pane
 * built from observable entities.
 *
 * @param <OE> The type of ObservableEntity handled by the factory. ObservableEntities are entities
 *             whose versions and properties can be observed and tracked for changes.
 */
public interface KlMultiComponentPaneFactory<OE extends ObservableEntity> extends KlWindowPaneFactory, KlEntityType<OE> {

    /**
     * Creates an instance of KlMultiComponentPane tailored to the specified observable entities, observable view,
     * and user preferences. This method constructs a multi-component pane that facilitates layout and comparison
     * of multiple observable components of the same type.
     *
     * @param observableEntities a list of observable entities whose properties and versions are to be managed
     *                           and rendered within the created pane
     * @param observableView an observable view that provides coordinate and navigation settings for the pane
     * @param preferences user preferences that influence the configuration and behavior of the created pane
     * @return an instance of KlMultiComponentPane configured with the specified entities, view, and preferences
     */
    KlMultiComponentPane<OE> create(ImmutableList<OE> observableEntities,
                           ObservableView observableView,
                           KometPreferences preferences);
}