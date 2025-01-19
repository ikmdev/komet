package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import javafx.scene.Parent;
import javafx.stage.Window;

/**
 * A top-level window within which a scene is hosted, and with which the user interacts.
 * The window may be a JavaFx Window or Stage, or some other window equivalent widget
 * such as is provided within the journal view. All KlWindows have a single Scene.
 */
public interface KlWindow<W extends Window> extends KlGadget<W> {

    /**
     * Enumerates preference keys for managing the properties and default configuration states
     * of a top-level window in the Komet application. Each key corresponds to a specific
     * property of the window, such as its size, position, visibility, or opacity. These keys
     * are used to define and retrieve default values for the associated properties.
     *
     * This enumeration implements the {@link PropertyWithDefault} interface, allowing each key
     * to provide a predefined default value. The default value acts as a fallback when explicit
     * values are not specified or available.
     *
     * Key definitions:
     * - WINDOW_X_LOCATION: The horizontal location of the window on the screen, defaulting to 0.
     * - WINDOW_Y_LOCATION: The vertical location of the window on the screen, defaulting to 0.
     * - WINDOW_WIDTH: The width of the window, defaulting to 800 pixels.
     * - WINDOW_HEIGHT: The height of the window, defaulting to 500 pixels.
     * - VISIBLE: Indicates whether the window is visible, defaulting to false.
     * - OPACITY: The opacity level of the window, defaulting to 1.0 (fully opaque).
     *
     * This enumeration is typically used within the context of managing and restoring window
     * preferences, ensuring a consistent user experience across sessions.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        WINDOW_X_LOCATION(0),
        WINDOW_Y_LOCATION(0),
        WINDOW_WIDTH(800),
        WINDOW_HEIGHT(500),
        VISIBLE(false),
        OPACITY(1.0);

        final Object defaultValue;

        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object defaultValue() {
            return defaultValue;
        }
    }
    /**
     * Retrieves the root node of the scene.
     * The root node serves as the top-most parent of all visual elements in the scene's hierarchy.
     *
     * @return The {@link Parent} object representing the root node of the scene.
     */
    Parent root();

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
