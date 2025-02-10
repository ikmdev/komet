package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlEntityType;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.context.KlContextFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;

/**
 * A factory interface for creating instances of {@code KlComponentPane} associated
 * with a specific type of {@code ObservableEntity}. This interface extends
 * {@code KlEntityType} and {@code KlFactory}, providing methods for creating
 * component panes that are associated with a given observable entity and
 * initialized with user preferences. Implementations of this factory specialize
 * in creating specific types of component panes for various observable entities.
 *
 * @param <T>  the specific type of {@code KlComponentPane} created by this factory
 * @param <OE> the type of {@code ObservableEntity} associated with the created component pane
 */
public sealed interface KlComponentPaneFactory<T extends KlComponentPane, OE extends ObservableEntity>
        extends KlEntityType<OE>, KlFactory<T>
        permits KlConceptPaneFactory, KlGenericComponentPaneFactory, KlPatternPaneFactory, KlSemanticPaneFactory, KlStampPaneFactory {

    /**
     * Creates an instance of type T, associates it with the provided {@code ObservableEntity},
     * and initializes it using the specified {@code KlPreferencesFactory}.
     *
     * @param observableEntity the observable entity of type {@code OE} to be associated with the created component pane
     * @param preferencesFactory an instance of {@code KlPreferencesFactory} used to provide
     *                           preferences for initializing the created component pane
     * @return an instance of type {@code T} that is initialized with the given preferences and associated with the provided observable entity
     */
    default T create(OE observableEntity,
                     KlPreferencesFactory preferencesFactory) {
        T componentPane = create(preferencesFactory);
        componentPane.componentProperty().setValue(observableEntity);
        return componentPane;
    }
}
