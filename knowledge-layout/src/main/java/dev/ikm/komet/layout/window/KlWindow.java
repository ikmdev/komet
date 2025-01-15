package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.preferences.KlUniversalPreferences;
import javafx.scene.Parent;
import javafx.stage.Window;

/**
 * A top-level window within which a scene is hosted, and with which the user interacts.
 * The window may be a JavaFx Window or Stage, or some other window equivalent widget
 * such as is provided within the journal view. All KlWindows have a single Scene.
 */
public interface KlWindow extends KlUniversalPreferences, KlGadget<Window> {
    /**
     * Retrieves the root node of the scene.
     * The root node serves as the top-most parent of all visual elements in the scene's hierarchy.
     *
     * @return The {@link Parent} object representing the root node of the scene.
     */
    Parent getRoot();

    /**
     * Displays the top-level window, making it visible to the user.
     * If the window is currently hidden or not rendered, invoking this method
     * will ensure it is rendered and brought into view.
     */
    void show();

    /**
     * Hides the top-level window, making it invisible to the user.
     * If the window is currently visible or displayed, invoking this method ensures
     * it is removed from the user's view but not destroyed.
     *
     * This method does not permanently dispose of the window or its resources, allowing
     * it to be shown again later through appropriate operations.
     */
    void hide();
}
