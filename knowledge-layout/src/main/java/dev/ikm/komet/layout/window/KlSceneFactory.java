package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.function.Supplier;

/**
 * Interface representing a factory for creating Scene instances.
 * Extends the KlFactory interface, inheriting its generic methods and behaviors.
 *
 * TODO: Should we be focusing on a root {@link Node} rather than a {@link Scene}?
 */
public interface KlSceneFactory extends KlFactory<KlScene> {

    /**
     * Creates a KlScene instance using the provided widget and preference supplier.
     * This method is responsible for initializing the scene with required configurations
     * and layout information supplied by the given KlWidget and KometPreferences supplier.
     *
     * @param widget the KlWidget instance used for configuring and rendering UI components
     * @param preferenceSupplier a Supplier that provides KometPreferences, offering configuration
     *                             settings for the scene
     * @return a KlScene instance initialized with the specified widget and preference supplier
     */
    KlScene create(KlWidget widget, Supplier<KometPreferences> preferenceSupplier);

    /**
     * Restores a previously saved Scene instance using the specified preferences.
     * The provided KometPreferences object is used to retrieve configuration
     * settings for recreating the Scene.
     *
     * @param preferences the KometPreferences instance containing saved configuration
     *                    settings for the Scene
     * @return the restored Scene object configured with the specified preferences
     */
    KlScene restore(KometPreferences preferences);

}
