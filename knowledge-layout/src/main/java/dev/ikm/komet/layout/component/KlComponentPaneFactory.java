package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlEntityType;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.preferences.KometPreferences;

/**
 * A factory interface for creating instances of KlComponentPane associated with a specific
 * type of ObservableEntity. Provides methods to integrate user preferences and view-related
 * configurations into the creation of component panes.
 *
 * @param <OE> the type of ObservableEntity that this factory works with
 */
public interface KlComponentPaneFactory<OE extends ObservableEntity> extends KlEntityType<OE>, KlFactory<KlWidget> {

    /**
     * Creates a KlComponentPane instance for the given observable entity, observable view,
     * and user preferences.
     *
     * @param observableEntity the observable entity of type OE to associate with the component pane
     * @param observableView the observable view that provides the view-related coordinate settings
     * @param preferences the user preferences to configure the component pane
     * @return an instance of KlComponentPane configured with the provided parameters
     */
    KlComponentPane<OE> create(OE observableEntity,
                           ObservableView observableView,
                           KometPreferences preferences);
}
