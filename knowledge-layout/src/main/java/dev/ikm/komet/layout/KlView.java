package dev.ikm.komet.layout;

import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.tinkar.coordinate.Coordinates;
import javafx.scene.layout.BorderPane;

/**
 * KlView is an interface that extends KlWidget specifically to provide a node to manage contextual
 * view coordinates. All descendent KlWidget items will inherit the view coordinate from a parent KlView.
 * TODO: Debate between making this interface extend KlGadget or KlWidget. KlGadget have no layout constraints, while KlWidgets are all GridLayout.
 * Maybe Grid is OK if it has a set size of 1 x 1 with proper grow values.
 */
public interface KlView extends KlGadget<BorderPane> {
    /**
     * PreferenceKeys is an enumeration that represents a collection of keys associated
     * with preferences in a specific context. Each key has an associated default value
     * provided during instantiation. This enum implements the PropertyWithDefault interface,
     * enabling it to provide a default value for each preference key.
     *
     * The purpose of these keys is to define and manage preferences with consistent
     * default values that can be overridden as needed. This simplifies preference handling
     * by providing a predefined structure for default configurations.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        /**
         * Represents the default coordinate record used for view configurations.
         * This key is primarily used within the preference management system to define
         * and retrieve the view coordinate. The associated value is provided
         * by the Coordinates.View.DefaultView() method, ensuring a standardized format
         * for view-based coordinate preferences.
         */
        VIEW_COORDINATE(Coordinates.View.DefaultView());

        private final Object defaultValue;

        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object defaultValue() {
            return defaultValue;
        }
    }

}
