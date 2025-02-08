package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlContextSensitiveComponent;
import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.KlStateCommands;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Parent;
import javafx.stage.Window;

/**
 * A top-level window within which a {@code Scene} is provided, and with which the user interacts.
 * The window may be a JavaFx Window or Stage. All KlWindows have a single {@code Scene}.
 * The {@code Scene} will not have an associated {@code PropertyKeys.KL_PEER}.
 * The {@code Scene.getRoot()} will have an associated {@code PropertyKeys.KL_PEER}.
 * The {@code KlView} peer is the root {@code Node} of the stage.
 *
 */
public non-sealed interface KlFxWindow extends KlGadget<Window>, KlStateCommands, KlContextProvider {

    /**
     * Enumerates preference keys for managing the properties and default configuration states
     * of a top-level window in the Komet application. Each key corresponds to a specific
     * property of the window, such as its size, position, visibility, or opacity. These keys
     * are used to define and retrieve default values for the associated properties.
     * <p>
     * This enumeration implements the {@link PropertyWithDefault} interface, allowing each key
     * to provide a predefined default value. The default value acts as a fallback when explicit
     * values are not specified or available.
     * <p>
     * Key definitions:
     * - WINDOW_X_LOCATION: The horizontal location of the window on the screen, defaulting to 0.
     * - WINDOW_Y_LOCATION: The vertical location of the window on the screen, defaulting to 0.
     * - WINDOW_WIDTH: The width of the window, defaulting to 800 pixels.
     * - WINDOW_HEIGHT: The height of the window, defaulting to 500 pixels.
     * - VISIBLE: Indicates whether the window is visible, defaulting to false.
     * - OPACITY: The opacity level of the window, defaulting to 1.0 (fully opaque).
     * <p>
     * This enumeration is typically used within the context of managing and restoring window
     * preferences, ensuring a consistent user experience across sessions.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        /**
         * Represents the horizontal location of the window on the screen.
         * This preference key is used to specify the default X-coordinate position
         * of a top-level window within the application. The value determines the
         * initial horizontal offset from the left edge of the screen where the window
         * will be displayed.
         *
         * Default value: 0.0
         */
        WINDOW_X_LOCATION(0d),
        /**
         * Represents the vertical location of the window on the screen.
         * This preference key is used to specify the default Y-coordinate position
         * of a top-level window within the application. The value determines the
         * initial vertical offset from the top edge of the screen where the window
         * will be displayed.
         *
         * Default value: 0.0
         */
        WINDOW_Y_LOCATION(0d),
        /**
         * Represents the default width of a top-level window in the application.
         * This preference key is used to specify the default horizontal dimension
         * of the window in pixels. The value determines the initial width when
         * the window is created or restored to its default state.
         *
         * Default value: 800.0
         */
        WINDOW_WIDTH(800d),
        /**
         * Represents the default height of a window in the Komet application's layout system.
         *
         * This variable defines the initial height for a window, measured in units, which is
         * typically used during the creation or rendering of a UI window. It provides a
         * pre-set default value to maintain consistency across windows and ensure proper layout
         * proportions within the application.
         */
        WINDOW_HEIGHT(500d),
        /**
         * A constant that determines the visibility state of a certain component or element within the application.
         * It is primarily used to toggle the visibility of the associated component between visible (true)
         * and hidden (false) states.
         *
         * Initial value: false, meaning the component is hidden by default.
         */
        WINDOW_VISIBLE(false),

        /**
         * Represents the opacity level for a component or layout in the context of the Komet application's windowing framework.
         *
         * The {@code OPACITY} variable defines the transparency level using a double value ranging from 0.0 to 1.0.
         * A value of 0.0 represents complete transparency, while a value of 1.0 corresponds to full opacity.
         *
         * Default value: 1.0
         */
        WINDOW_OPACITY(1.0d),

        /**
         * Represents the title of the window in the Komet layout system.
         * <p>
         * The {@code WINDOW_TITLE} variable holds the default title to be displayed for
         * a window. This title may be used as a placeholder until it is explicitly
         * set by the end-user or the application logic. By default, the value is "Untitled".
         *
         */
        WINDOW_TITLE("Untitled");

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
    /**
     * Saves the current state of the window or layout with the specified name.
     * This method is typically used to persist the configuration, positioning, and other
     * state-related data of the layout under a user-defined name.
     *
     * @param layoutName The name under which the layout state will be saved.
     *                   This name can be used to identify and retrieve the saved layout in the future.
     */
    void saveAsLayout(String layoutName);

    /**
     * Retrieves the current context associated with this instance.
     * The context provides access to layout orchestration and configuration utilities,
     * including view coordinates, graphical elements, and hierarchical associations.
     *
     * @return the {@link KlContext} instance representing the current context
     */
    default KlContext context() {
        return KlGadget.super.context();
    }
}
