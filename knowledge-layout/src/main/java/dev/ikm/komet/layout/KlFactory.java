package dev.ikm.komet.layout;

import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;

import static dev.ikm.komet.layout.KlRestorable.camelCaseToWords;

/**
 * Defines a factory for creating and restoring instances of {@link KlView}.
 * This interface provides methods for retrieving metadata about the factory,
 * such as the names and descriptions of the gadgets it produces, and supports
 * customization for layout tools through palette icons.
 *
 * @param <KL> The type of {@link KlView} produced by this factory.
 * @deprecated use KlView.Factory instead.
 */
@Deprecated
public interface KlFactory<KL extends KlPeerable> {

    /**
     * Creates an instance of type T using the provided KlPreferencesFactory.
     *
     * @param preferencesFactory an instance of KlPreferencesFactory used to provide
     *                           necessary preferences for creating the object.
     * @return an instance of type T created using the given preferencesFactory.
     * @deprecated Use {@code create(KlPreferencesFactory preferencesFactory, GridLayoutForComponentFactory gridLayoutForComponentFactory) }
     */
    @Deprecated
    KL create(KlPreferencesFactory preferencesFactory);


    /**
     * Creates an instance of type KL using the provided KlPreferencesFactory and
     * GridLayoutForComponentFactory. This method delegates the creation process
     * to the {@code create} method with a single KlPreferencesFactory parameter.
     *
     * @param preferencesFactory an instance of KlPreferencesFactory used to provide
     *                           the necessary preferences for creating the object.
     * @param layoutComputer an instance of GridLayoutForComponentFactory,
     *                                       though it is not utilized in this implementation.
     * @return an instance of type KL created using the given KlPreferencesFactory.
     */
    default KL create(KlPreferencesFactory preferencesFactory, LayoutComputer layoutComputer) {
        // Override, and remove default in future revisions. TODO: not sure this method is right.
        return create(preferencesFactory);
    }

    default KL create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaLayoutForArea) {
        KL kl = create(preferencesFactory);
        if (kl instanceof KlArea klArea) {
            klArea.setAreaLayout(areaLayoutForArea);
        }
        return kl;
    }

    /**
     * Restores an instance of type T using the provided preferences.
     *
     * @param preferences an instance of KometPreferences that contains the
     *                    configuration or state required to restore the object.
     * @return an instance of type T restored using the given preferences.
     */
    KL restore(KometPreferences preferences);


    /**
     * Retrieves the name of this factory.
     *
     * @return A string representing the name of the factory.
     */
    default String name() {
        return camelCaseToWords(this.getClass().getSimpleName());
    }
}
