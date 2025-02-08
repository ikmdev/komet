package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlContextSensitiveComponent;
import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Represents a Knowledge Layout (KL) Scene, serving as a part of the broader layout framework.
 * The KlWindowPane interface provides methods to access the scene and its root {@link javafx.scene.Node}, and inherits
 * behavior for managing preferences and acting as a KlGadget.
 *
 * This interface extends KlUniversalPreferences, enabling the retrieval and management of
 * preferences associated with the KlWindowPane, and KlGadget, allowing it to represent itself
 * as a component within the Knowledge Layout system.
 *
 * Methods defined in this interface allow integration with the JavaFX framework, such as
 * retrieving the root node of the scene and accessing the entire JavaFX Scene instance.
 */
public non-sealed interface KlWindowPane extends KlGadget<Parent> {

    /**
     * Enumerates preference keys for managing transformations and properties of a Knowledge Layout (KL) Scene in the Komet application.
     * Each key represents a specific transformation or property applied to visual elements within the scene.
     *
     * This enumeration implements the {@link PropertyWithDefault} interface, allowing each key
     * to provide a predefined default value. The default value serves as a fallback for the associated
     * property when no specific value is explicitly defined.
     *
     * Key definitions:
     * - TRANSLATE_X: Default horizontal translation, measured in units (default: 0.0).
     * - TRANSLATE_Y: Default vertical translation, measured in units (default: 0.0).
     * - TRANSLATE_Z: Default depth translation, measured in units (default: 0.0).
     * - SCALE_X: Default scale factor along the horizontal axis (default: 1.0).
     * - SCALE_Y: Default scale factor along the vertical axis (default: 1.0).
     * - SCALE_Z: Default scale factor along the depth axis (default: 1.0).
     * - ROTATE: Default rotation angle, measured in degrees (default: 0.0).
     *
     * These keys are typically used for managing user preferences related to the layout's transformations,
     * ensuring consistency in visual presentation and interaction across sessions.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        /**
         * Represents the default horizontal translation value for a Knowledge Layout (KL) Scene.
         * The translation is measured in units and defines the initial horizontal offset
         * applied to visual elements in the scene. This preference key is part of the
         * transformation settings for managing the scene's layout and user interaction.
         *
         * Default value: 0.0
         */
        TRANSLATE_X(0.0d),
        /**
         * Represents the default vertical translation value for a Knowledge Layout (KL) Scene.
         * The translation is measured in units and defines the initial vertical offset
         * applied to visual elements in the scene. This preference key is part of the
         * transformation settings for managing the scene's layout and user interaction.
         *
         * Default value: 0.0
         */
        TRANSLATE_Y(0.0d),
        /**
         * Represents the default depth translation value for a Knowledge Layout (KL) Scene.
         * The translation is measured in units and defines the initial depth offset
         * applied to visual elements in the scene. This preference key is part of the
         * transformation settings for managing the scene's layout and user interaction.
         *
         * Default value: 0.0
         */
        TRANSLATE_Z(0.0d),
        /**
         * Represents the horizontal scaling factor for a Komet application window.
         * Defines a default scaling value of 1.0 for the X-axis. This value is typically
         * used to adjust or transform the horizontal dimensions or proportions of a window,
         * scene, or visual element in the application. Scaling may affect the overall
         * appearance and layout of the user interface.
         */
        SCALE_X(1.0d),
        /**
         * Represents the scaling factor along the Y-axis for a window or visual element.
         * The value modifies how elements are visually scaled proportionately in the vertical direction.
         * Default value is set to 1.0, indicating no scaling.
         */
        SCALE_Y(1.0d),
        /**
         * Represents the Z-axis scale factor for 3D transformations or effects.
         *
         * This constant is used to define the default scaling value along the Z-axis,
         * typically in graphical or layout contexts where depth adjustments are required.
         * The default value is 1.0, indicating no scaling along the Z-axis (neutral).
         */
        SCALE_Z(1.0d),
        /**
         * Represents the rotation angle of an object within a user interface or graphical context.
         * This value is typically expressed in degrees, where 0.0 indicates no rotation.
         * It is used to define the rotational transformation applied to a parent coordinate system or object.
         */
        ROTATE(0.0d);

        final Object defaultValue;

        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }
        @Override
        public Object defaultValue() {
            return this.defaultValue;
        }
    }
    /**
     * Retrieves the root node of the whiteboard.
     * The root node serves as the top-most parent of all visual elements in the whiteboard hierarchy.
     *
     * @return The {@link Node} object representing the root node of the scene.
     */
    Parent root();
}
