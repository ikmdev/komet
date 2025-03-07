package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.layout.KlEntityType;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import javafx.scene.layout.Pane;

/**
 * A sealed factory interface for creating instances of {@code KlComponentPane}
 * that are associated with specific {@code ObservableEntity} types. This interface
 * provides a template for constructing and initializing UI components that are dynamically
 * bound to observable JavaFX entities. The core functionality includes creating panes
 * with specified observable entities and applying user preferences.
 *
 * The {@code KlComponentPaneFactory} interface integrates with the type system to restrict
 * implementers to a predefined set of specific pane factories, ensuring the implementation
 * of agreed-upon behavior across different types of observable data components.
 *
 * This interface extends the {@code KlEntityType} interface for obtaining runtime information
 * about the observable entity type and the {@code KlFactory} interface for creating specific UI components.
 *
 * @param <FX> the type of JavaFX pane created by this factory
 * @param <KL> the type of {@code KlComponentPane} created, which is tied to both {@code FX}
 *             and {@code OE}
 * @param <OE> the type of {@code ObservableEntity} associated with the created {@code KlComponentPane}
 * @see KlEntityType
 * @see KlFactory
 */
public sealed interface KlComponentAreaFactory<FX extends Pane, KL extends KlComponentArea<OE, FX>, OE extends ObservableEntity>
        extends KlEntityType<OE>, KlFactory<KL>
        permits KlConceptAreaFactory, KlGenericComponentAreaFactory, KlPatternAreaFactory, KlSemanticAreaFactory, KlStampAreaFactory {
    /**
     * Creates an instance of {@code KL} associated with a specified {@code OE observableEntity},
     * and initializes it using the provided {@code preferencesFactory}. The {@code observableEntity}
     * is set on the created component pane's {@code componentProperty}.
     *
     * @param observableEntity the observable entity of type {@code OE} to associate with the created component pane
     * @param preferencesFactory an instance of {@code KlPreferencesFactory} used to initialize the created component pane
     * @return an instance of {@code KL} initialized with the provided {@code preferencesFactory} and set with the given {@code observableEntity}
     */
    default KL create(OE observableEntity,
                      KlPreferencesFactory preferencesFactory) {
        KL componentPane = create(preferencesFactory);
        componentPane.componentProperty().setValue(observableEntity);
        return componentPane;
    }
}
