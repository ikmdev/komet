package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlVersionType;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;

/**
 * The {@code KlVersionPaneFactory} interface is a generic factory for creating instances of {@link KlVersionArea}
 * associated with specific {@link ObservableVersion} types.
 * <p>
 * This interface serves as a foundational contract within the Komet Knowledge Layout framework for constructing
 * user interface components that represent different types of versioned entities. It is designed to provide a
 * uniform approach to instantiate components tailored to specific observable versions.
 * <p>
 * The interface is sealed, permitting only the following subtypes:
 * - {@code KlConceptVersionPaneFactory}
 * - {@code KlPatternVersionPaneFactory}
 * - {@code KlSemanticVersionPaneFactory}
 * - {@code KlStampVersionPaneFactory}
 *
 * Type Parameters:
 * - {@code T}: Represents the specific type of {@link KlVersionArea} created by the factory.
 * - {@code OV}: Denotes the type of {@link ObservableVersion} associated with the version pane.
 * <p>
 * Responsibilities:
 * - Define the general contract for creating version panes.
 * - Ensure that panes created by the factory are tailored to the correct type of observable version.
 * <p>
 * Method Details:
 * - {@link #create}:
 *   Creates an instance of {@link KlVersionArea} bound to the given observable version and configured using
 *   the provided preferences factory.
 * <p>
 * Integration Notes:
 * - This interface is primarily intended for supporting the creation and management of versioned visualization
 *   components within the Komet Knowledge Layout framework.
 * <p>
 * See Also:
 * - {@link KlFactory}
 * - {@link KlVersionType}
 * - {@link KlConceptVersionAreaFactory}
 * - {@link KlPatternVersionAreaFactory}
 * - {@link KlSemanticVersionAreaFactory}
 * - {@link KlStampVersionAreaFactory}
 */
public sealed interface KlVersionAreaFactory<P extends KlVersionArea, OV extends ObservableVersion>
        extends KlFactory<P>, KlVersionType<OV>
        permits KlConceptVersionAreaFactory, KlGenericVersionAreaFactory, KlPatternVersionAreaFactory, KlSemanticVersionAreaFactory, KlStampVersionAreaFactory {

    /**
     * Creates an instance of type {@code T} that represents a version pane for the specified
     * {@code ObservableVersion} and configured using the provided preferences factory.
     *
     * @param observableVersion the observable version data to be displayed and managed by the created version pane
     * @param preferencesFactory the preferences factory used to configure settings for the version pane
     * @return a newly created instance of type {@code T} representing the version pane tailored to the specified version type and preferences
     */
    default P create(OV observableVersion,
                     KlPreferencesFactory preferencesFactory) {
        P versionPane = create(preferencesFactory);
        versionPane.versionProperty().setValue(observableVersion);
        return versionPane;
    }
}
