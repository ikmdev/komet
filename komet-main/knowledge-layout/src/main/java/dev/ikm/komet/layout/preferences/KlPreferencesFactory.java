package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.preferences.KometPreferences;
import java.util.function.Supplier;

/**
 * The KlPreferencesFactory interface provides a method for creating instances of
 * KometPreferences.
 */
public interface KlPreferencesFactory extends Supplier<KometPreferences> {

    /**
     * Provides a new unique instance of {@code KometPreferences} associated with the specified
     * implementation class within the parent preferences node. Multiple calls will create multiple
     * unique instances of {@code KometPreferences}, all with the same parent.
     *
     * @return a {@code KometPreferences} instance uniquely associated with the implementation class
     *         and created within the parent preferences node
     */
    @Override
    KometPreferences get();


    /**
     * Creates a new {@code KlPreferencesFactory} instance that encapsulates a parent preferences node
     * and an implementation class, enabling hierarchical and context-specific preference management.
     *
     * @param parentPreferences the parent {@code KometPreferences} node in which the new preferences factory
     *                          will create preference nodes
     * @param implementationClass the specific class for which the preferences factory is being created
     * @return a new {@code KlPreferencesFactory} instance for managing preferences associated with the specified
     *         implementation class
     */
    static KlPreferencesFactory create(KometPreferences parentPreferences, Class implementationClass) {
        return new KlPreferenceFactoryProvider.PreferenceFactoryWithParentPreferences(parentPreferences, implementationClass);
    }

    /**
     * Creates a new {@code KlPreferencesFactory} instance that encapsulates a parent factory
     * and an implementation class, enabling hierarchical preference management
     * with unique preference nodes for the specified implementation class.
     *
     * @param preferencesFactory the parent {@code KlPreferencesFactory} instance that provides context for
     *                           creating hierarchical preferences
     * @param implementationClass the class for which the preferences factory is being created
     * @return a new {@code KlPreferencesFactory} instance for managing preferences
     *         associated with the specified implementation class
     */
    static KlPreferencesFactory create(KlPreferencesFactory preferencesFactory, Class implementationClass) {
        return new KlPreferenceFactoryProvider.PreferenceFactoryWithParentFactory(preferencesFactory, implementationClass);
    }

}
