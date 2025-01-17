package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlGadget;
import javafx.scene.Parent;

/**
 * Represents a Knowledge Layout (KL) Scene, serving as a part of the broader layout framework.
 * The KlWhiteBoard interface provides methods to access the scene and its root {@link javafx.scene.Node}, and inherits
 * behavior for managing preferences and acting as a KlGadget.
 *
 * This interface extends KlUniversalPreferences, enabling the retrieval and management of
 * preferences associated with the KlWhiteBoard, and KlGadget, allowing it to represent itself
 * as a component within the Knowledge Layout system.
 *
 * Methods defined in this interface allow integration with the JavaFX framework, such as
 * retrieving the root node of the scene and accessing the entire JavaFX Scene instance.
 */
public interface KlWhiteBoard extends KlGadget<KlWhiteBoard> {
    /**
     * Retrieves the root node of the whiteboard.
     * The root node serves as the top-most parent of all visual elements in the whiteboard hierarchy.
     *
     * @return The {@link Parent} object representing the root node of the scene.
     */
    Parent getRoot();
}
