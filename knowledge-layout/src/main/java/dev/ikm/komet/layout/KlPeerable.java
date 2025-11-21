package dev.ikm.komet.layout;

import dev.ikm.komet.layout.context.KlContext;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.stage.Window;
import org.eclipse.collections.api.list.ImmutableList;

import static dev.ikm.komet.layout.KlPeerable.PropertyKeys.KL_PEER;

/**
 * A typed object of programmatic interest to the Knowledge Layout system that
 * uses the KometPreferences for saving and restoring state.
 */
public sealed interface KlPeerable extends KlRestorable permits KlKnowledgeBaseContext, KlView {
    /**
     * Keys for objects that a {@code KlPeerable} object will store in the properties of their associated
     * JavaFX peer. Some of these objects will provide caching and computation
     * functionality (behavior) and may not be strictly data carriers. In cases
     * where the state must be saved and restored, the {@code KlPeerable} class is responsible for
     * populating the properties with objects derived from, and saved to, a corresponding
     * {@code KometPreferencesNode} with a corresponding {@code PropertyKey}.
     */
    enum PropertyKeys {
        /**
         * Represents a property key used within the property system of JavaFX {@code Nodes}
         * and {@code Window} objects to provide a mechanism for associating specific objects or behaviors.
         * The {@code KL_PEER} key is used to link JavaFx objects with their corresponding Knowledge layout peer.
         * It allows for dynamic attachment of additional functionality or metadata
         * to the Nodes, supporting advanced system configurations.
         */
        FX_PEER,
        /**
         * Represents a property key used within the property system of JavaFX {@code Nodes}
         * and {@code Window} objects to associate them with their corresponding Knowledge layout peer.
         * This key is utilized to enable dynamic linking of JavaFX objects to their corresponding
         * knolwedge layout peer,
         * allowing additional functionality, behavior, or metadata to be incorporated
         * into the JavaFX objects as needed.
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


    static KlPeerable getKlPeer(Node node) {
        Node tempNode = node;
        while (tempNode != null) {
            if (tempNode.hasProperties() && tempNode.getProperties().containsKey(KL_PEER)) {
                return (KlPeerable) tempNode.getProperties().get(KL_PEER);
            }
            tempNode = tempNode.getParent();
        }
        throw new IllegalStateException("Can't find KL_PEER in scene graph. Node: " + node);
    }

    static KlPeerable getKlPeer(Window window) {
        while (window.hasProperties() && window.getProperties().containsKey(KL_PEER)) {
            return (KlPeerable) window.getProperties().get(KL_PEER);
        }
        throw new IllegalStateException("Can't find KL_PEER in scene graph. Window: " + window);
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
