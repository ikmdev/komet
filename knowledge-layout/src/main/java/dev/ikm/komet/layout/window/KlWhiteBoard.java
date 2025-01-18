package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

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
        TRANSLATE_X(0.0),
        TRANSLATE_Y(0.0),
        TRANSLATE_Z(0.0),
        SCALE_X(1.0),
        SCALE_Y(1.0),
        SCALE_Z(1.0),
        ROTATE(0.0);

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
     * @return The {@link BorderPane} object representing the root node of the scene.
     */
    BorderPane getRoot();
}
