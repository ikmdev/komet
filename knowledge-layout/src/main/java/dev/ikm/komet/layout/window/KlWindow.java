package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.preferences.KlUniversalPreferences;
import javafx.scene.Scene;

/**
 * A top-level window within which a scene is hosted, and with which the user interacts.
 * The window may be a JavaFx Window or Stage, or some other window equivalent widget
 * such as is provided within the journal view. All KlWindows have a single Scene.
 */
public interface KlWindow extends KlUniversalPreferences {

    /**
     * Retrieves the scene associated with this window.
     * Each window has a single scene that acts as the hierarchical root
     * for all visual elements within the window.
     *
     * @return The {@link Scene} object associated with this window.
     * TODO: Should we be using the root {@link javafx.scene.Node} rather than Scene here?
     */
    Scene scene();
}
