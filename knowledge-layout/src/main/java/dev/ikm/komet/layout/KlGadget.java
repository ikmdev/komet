package dev.ikm.komet.layout;

import dev.ikm.komet.layout.preferences.PreferenceProperty;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;

/**
 * Highest level Knowledge Layout Component. Some components, such as {@code Window} do
 * not descend from {@code Node}, and this interface enables inclusion of those components
 * in the Knowledge Layout paradigm (i.e. consistent use of factories and preferences, and
 * an ability to serialize, share, and restore a layout.)
 *
 */
public interface KlGadget<T> {
    /**
     * Enum representing the keys used to manage and access user preferences
     * related to gadgets within the application. This enum defines constants
     * that are essential for storing and retrieving configuration or state
     * information for restoring windows or initializing preferences.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        /**
         * Boolean string representing if the preferences have been initialized.
         */
        INITIALIZED( Boolean.FALSE),

        /**
         * Fully qualified name of the factory class. Used to restore the KlWindow
         * from preferences.
         */
        FACTORY_CLASS(PreferenceProperty.INITIAL_STRING_VALUE),

        /**
         * Represents the name of the specific implementation of a {@link KlWindow}
         * that can be restored from preferences. This key is used to identify
         * and manage restoration of the window's state during application initialization
         * or when reloading user preferences.
         */
        NAME_FOR_RESTORE("");

        Object defaultValue;

        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Object defaultValue() {
            return defaultValue;
        }
    }
    /**
     * Provides an instance of the generic type T associated with the knowledge layout component.
     *
     * @return an instance of type T, representing a specific knowledge layout gadget.
     */
    T klGadget();
}
