package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * Interface representing a factory for creating Scene instances.
 * Extends the KlFactory interface, inheriting its generic methods and behaviors.
 *
 * TODO: Should we be focusing on a root {@link Node} rather than a {@link Scene}?
 */
public interface KlSceneFactory extends KlFactory {
    /**
     * @deprecated Use {@link #restore(KometPreferences)}
     */
    @Deprecated
    Scene create(KometPreferences preferences);

    /**
     * Restores a previously saved Scene instance using the specified preferences.
     * The provided KometPreferences object is used to retrieve configuration
     * settings for recreating the Scene.
     *
     * @param preferences the KometPreferences instance containing saved configuration
     *                    settings for the Scene
     * @return the restored Scene object configured with the specified preferences
     */
    Scene restore(KometPreferences preferences);

    /**
     * Creates a new Scene instance using the specified preferences factory.
     * The preferences factory is used to generate the necessary configuration settings
     * for initializing the Scene object.
     *
     * @param preferencesFactory the KlPreferencesFactory instance that provides the means
     *                           to generate configuration preferences for the Scene
     * @return a newly created Scene object configured using the preferences provided by
     *         the preferences factory
     */
    Scene create(KlPreferencesFactory preferencesFactory);

}
