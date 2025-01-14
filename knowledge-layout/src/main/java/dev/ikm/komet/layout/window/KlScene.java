package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.preferences.KlUniversalPreferences;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Represents a Knowledge Layout (KL) Scene, serving as a part of the broader layout framework.
 * The KlScene interface provides methods to access the scene and its root {@link javafx.scene.Node}, and inherits
 * behavior for managing preferences and acting as a KlGadget.
 *
 * This interface extends KlUniversalPreferences, enabling the retrieval and management of
 * preferences associated with the KlScene, and KlGadget, allowing it to represent itself
 * as a component within the Knowledge Layout system.
 *
 * Methods defined in this interface allow integration with the JavaFX framework, such as
 * retrieving the root node of the scene and accessing the entire JavaFX Scene instance.
 */
public interface KlScene extends KlUniversalPreferences, KlGadget<KlScene> {
    /**
     * Retrieves the root node of the scene.
     * The root node serves as the top-most parent of all visual elements in the scene's hierarchy.
     *
     * @return The {@link Parent} object representing the root node of the scene.
     */
    Parent getRoot();

    /**
     * Retrieves the JavaFX Scene associated with this KlScene instance.
     * The Scene acts as the primary container for all visual elements
     * within the UI hierarchy of this KlScene.
     *
     * @return The {@link Scene} object associated with this KlScene.
     */
    Scene scene();
}
