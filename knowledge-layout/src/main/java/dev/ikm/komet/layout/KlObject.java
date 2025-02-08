package dev.ikm.komet.layout;

import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.preferences.PreferenceProperty;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.collections.ObservableMap;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * A typed object of programmatic interest to the Knowledge Layout system that
 * uses the KometPreferences for saving and restoring state.
 */
public sealed interface KlObject permits KlGadget, KlKnowledgeBaseContext{
    /**
     * Keys for objects that KlGadgets will store in the properties of their associated
     * JavaFx {@code Node}s. Some of these objects will provide caching and computation
     * functionality (behavior), and may not be strictly data carriers. In those cases,
     * where the state must be saved and restored, the gadget class is responsible for
     * populating the properties with objects derived from, and saved to, a corresponding
     * KometPreferencesNode with a corresponding PropertyKey.
     */
    enum PropertyKeys {
        /**
         * Represents a property key used within the property system of JavaFX {@code Nodes}
         * and {@code Window} objects to provide a mechanism for associating specific objects or behaviors.
         * The {@code KL_PEER} key is used to link JavaFx objects with their corresponding Knowledge layout peer.
         * It allows for dynamic attachment of additional functionality or metadata
         * to the Nodes, supporting advanced system configurations.
         */
        KL_PEER,

        /**
         * Represents a property key used to associate a {@code KlContextProvider}
         * with a JavaFX {@code Node}. This key helps in providing and managing
         * context-specific behaviors and data within the JavaFX scene graph. The
         * {@code KlContextProvider} is expected to supply {@code KlContext} information,
         * which encapsulates details like view coordinates and context-sensitive
         * properties. The implementation is responsible for lifecycle management
         * and persistence of associated contextual data if needed.
         */
        KL_CONTEXT,
    }


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
        INITIALIZED(Boolean.FALSE),

        /**
         * Fully qualified name of the factory class. Used to restore the {@link KlGadget}
         * from preferences.
         */
        FACTORY_CLASS(PreferenceProperty.INITIAL_STRING_VALUE),

        /**
         * Represents the name of the specific implementation of a {@link KlGadget}
         * that can be restored from preferences. This key is used to identify
         * and manage restoration of the window's state during application initialization
         * or when reloading user preferences.
         */
        NAME_FOR_RESTORE("");

        /**
         * Represents the default value associated with a preference key.
         * This value provides an initial or fallback configuration used
         * when no other value has been explicitly set or retrieved.
         */
        Object defaultValue;

        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object defaultValue() {
            return defaultValue;
        }
    }

    /**
     * Retrieves the {@code KlContext} associated with the current {@code KlObject}.
     * The context represents a layout or configuration context,
     * providing information for organizing and managing the associated structure or behavior.
     *
     * @return the {@code KlContext} instance linked to the current {@code KlObject}.
     */
    KlContext context();

    /**
     * Retrieves the list of {@code KlContext} instances that are enclosing or
     * surrounding the current context in a hierarchical or structural manner.
     * The list is ordered by distance from this context (first element is the closest).
     *
     * @return an immutable list of {@code KlContext} objects representing the
     *         enclosing contexts of this instance
     */
    ImmutableList<KlContext> contexts();

    /**
     * Retrieves the {@code KometPreferences} associated with the current {@code KlObject} instance.
     * These preferences provide configuration and customization options for the gadget's behavior.
     *
     * @return the {@code KometPreferences} associated with the current {@code KlGadget}.
     */
    KometPreferences preferences();

    /**
     * Retrieves an observable map of properties associated with the current {@code KlObject}.
     * These properties provide a flexible mechanism for storing additional state or configuration data
     * for the object, allowing dynamic updates and interaction with observers.
     *
     * @return an {@code ObservableMap} containing property keys of type {@code Object} and their associated values of type {@code Object}.
     */
    ObservableMap<Object,Object> properties();

    /**
     * Determines whether the current {@code KlObject} has any properties defined in its associated
     * properties map.
     *
     * @return {@code true} if there are one or more properties defined in the properties map,
     *         {@code false} otherwise.
     */
    boolean hasProperties();

    /**
     * Determines whether the current {@code KlObject} has a property associated with the specified key.
     *
     * @param key the key for which to check the existence of an associated property.
     * @return {@code true} if a property associated with the given key exists in the properties map,
     *         {@code false} otherwise.
     */
    boolean hasProperty(Object key);
}
