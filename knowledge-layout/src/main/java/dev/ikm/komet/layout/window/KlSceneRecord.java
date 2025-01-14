package dev.ikm.komet.layout.window;

import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * A record representing a KlScene object with a Scene and corresponding preferences.
 * This class implements the KlScene interface and provides access to the scene's root
 * and itself as a KlScene gadget.
 *
 * @param scene the JavaFX Scene associated with this KlSceneRecord
 * @param preferences the KometPreferences providing configuration or customization options
 */
public record KlSceneRecord(Scene scene, KometPreferences preferences) implements KlScene {
    /**
     * Retrieves the root node of the associated Scene.
     * The root node represents the top-most parent within the scene's hierarchy
     * of visual elements.
     *
     * @return The {@link Parent} object serving as the root node of the Scene.
     */
    @Override
    public Parent getRoot() {
        return scene.getRoot();
    }

    /**
     * Retrieves this instance as a KlScene gadget.
     * This method provides a self-referential return, allowing the instance
     * to represent itself within the Knowledge Layout system when a KlScene gadget is required.
     *
     * @return This instance as a {@link KlScene} object.
     */
    @Override
    public KlScene klGadget() {
        return this;
    }

    @Override
    public void classInitialize() {

    }
}
