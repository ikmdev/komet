package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Scene;

/**
 * Interface representing a factory for creating Scene instances.
 * Extends the KlFactory interface, inheriting its generic methods and behaviors.
 */
public interface KlSceneFactory extends KlFactory {
    /**
     * Creates a new Scene instance using the specified preferences.
     * If the preferences are uninitialized, defaults are used for initialization, and are then written
     * to the preferences.
     *
     * @param preferences the KometPreferences instance that provides the configuration settings for the Scene
     * @return a new Scene object configured with the given preferences
     */
    Scene create(KometPreferences preferences);
}
