/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.window.KlJournalWindow;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;

/**
 * Factory interface for creating entity chapter windows within the Journal workspace.
 * <p>
 * Implementations of this interface produce specialized window instances
 * (e.g., concept, pattern, semantic windows) that display details for particular entity types
 * within the workspace. Each factory is associated with a specific {@link EntityKlWindowType}
 * and creates windows specialized for displaying and editing entities of that type.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Get a factory for concept windows
 * EntityKlWindowFactory factory = Registry.getFactory(EntityKlWindowTypes.CONCEPT);
 *
 * // Create a window for a specific concept
 * AbstractEntityChapterKlWindow window = factory.create(
 *     journalId,
 *     conceptEntityFacade,
 *     viewProperties,
 *     preferences
 * );
 * }</pre>
 *
 * @see AbstractEntityChapterKlWindow
 * @see EntityKlWindowType
 * @see KlJournalWindow
 */
public interface EntityKlWindowFactory extends KlFactory<AbstractEntityChapterKlWindow> {

    /**
     * Creates a new chapter window for the specified entity under the given journal topic.
     * <p>
     * This method constructs a specialized window that displays and allows editing
     * of the provided entity. The window is associated with a journal topic for
     * communication with other components in the workspace.
     * <p>
     * The returned window is initialized with the entity data but may load additional
     * information asynchronously. If the entity facade is null, an empty or default
     * view should be created.
     *
     * @param journalTopic   the UUID identifying the journal topic that owns and communicates with this window
     * @param entityFacade   the entity facade providing data for display; may be null to create an empty or default view
     * @param viewProperties properties and calculators for querying and formatting data in the view
     * @param preferences    preferences facilitating read/write of window-specific settings
     * @return an {@link AbstractEntityChapterKlWindow} instance representing the detail view for the entity
     * @throws IllegalArgumentException if journalTopic is null or viewProperties is null
     * @throws RuntimeException         if window creation fails for any reason
     */
    AbstractEntityChapterKlWindow create(UUID journalTopic,
                                         EntityFacade entityFacade,
                                         ViewProperties viewProperties,
                                         KometPreferences preferences);

    /**
     * Returns the unique type identifier for windows created by this factory.
     * <p>
     * This type is used for registry lookup, window identification, and persistence
     * of window state. Each factory implementation must return a unique window type.
     *
     * @return the {@link EntityKlWindowType} associated with this factory
     */
    EntityKlWindowType getWindowType();

    /**
     * Registry for locating and retrieving {@link EntityKlWindowFactory} implementations.
     * <p>
     * This class provides a central registry for window factories, allowing the application
     * to locate and create windows of various types by their {@link EntityKlWindowType}.
     * <p>
     * The registry is automatically populated with factories discovered via Java's
     * {@link ServiceLoader} mechanism at class initialization time. Additional factories
     * can be manually registered using the {@link #registerFactory} method.
     */
    class Registry {
        private static final Logger LOG = LoggerFactory.getLogger(Registry.class);
        private static final Map<EntityKlWindowType, EntityKlWindowFactory> FACTORIES = new HashMap<>();

        static {
            // Discover and register factories via ServiceLoader
            ServiceLoader<EntityKlWindowFactory> loader = ServiceLoader.load(EntityKlWindowFactory.class);
            for (EntityKlWindowFactory factory : loader) {
                registerFactory(factory);
            }
        }

        /**
         * Creates a chapter window for the specified window type using its registered factory.
         * <p>
         * This method synchronously creates a window instance of the specified type
         * and initializes it with the provided entity and settings. The window is fully
         * initialized when this method returns.
         *
         * @param windowType     the type of window to create
         * @param journalTopic   the UUID of the owning journal topic
         * @param entityFacade   the entity facade to display
         * @param viewProperties properties for querying and formatting view data
         * @param preferences    preferences for persisting window settings
         * @return a new {@link ChapterKlWindow} instance
         * @throws IllegalArgumentException if no factory is registered for the given window type
         *                                  or if required parameters are invalid
         */
        public static AbstractEntityChapterKlWindow createWindow(EntityKlWindowType windowType,
                                                                 UUID journalTopic,
                                                                 EntityFacade entityFacade,
                                                                 ViewProperties viewProperties,
                                                                 KometPreferences preferences) {
            EntityKlWindowFactory factory = getFactory(windowType);
            if (factory == null) {
                LOG.warn("No factory registered for window type: {}", windowType);
                throw new IllegalArgumentException("No factory registered for window type: " + windowType);
            }
            return factory.create(journalTopic, entityFacade, viewProperties, preferences);
        }

        /**
         * Restores a previously saved entity chapter window using stored preferences.
         * <p>
         * This method reconstructs a window instance from serialized state information
         * stored in the provided preferences. It first extracts the window type from
         * the preferences, locates the appropriate factory for that type, and then
         * delegates the actual restoration to the factory's restore method.
         * <p>
         * The window is fully initialized with its previous state when this method returns.
         *
         * @param preferences preferences containing the serialized window state
         * @return a restored {@link AbstractEntityChapterKlWindow} instance
         * @throws IllegalArgumentException if no factory is registered for the window type
         *                                  found in the preferences, or if the preferences
         *                                  contain invalid window state information
         */
        public static AbstractEntityChapterKlWindow restoreWindow(KometPreferences preferences) {
            EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);
            EntityKlWindowType windowType = windowState.getWindowType();
            EntityKlWindowFactory factory = getFactory(windowType);
            if (factory == null) {
                LOG.warn("No factory registered for window type: {}", windowType);
                throw new IllegalArgumentException("No factory registered for window type: " + windowType);
            }
            return factory.restore(preferences);
        }

        /**
         * Registers a new {@link EntityKlWindowFactory} with the registry.
         * <p>
         * This method adds a factory to the registry, allowing its window type
         * to be created through the registry's creation methods. If a factory
         * for the same window type already exists, it will be replaced.
         *
         * @param factory the factory implementation to register
         * @throws IllegalArgumentException if factory is null or returns a null window type
         */
        public static void registerFactory(EntityKlWindowFactory factory) {
            FACTORIES.put(factory.getWindowType(), factory);
            LOG.info("Registered window factory for type: {}", factory.getWindowType());
        }

        /**
         * Retrieves the factory registered for the specified window type.
         * <p>
         * This method looks up the factory associated with the given window type
         * in the registry. It returns null if no factory is registered for that type.
         *
         * @param windowType the window type to look up
         * @return the corresponding factory, or {@code null} if none is registered
         */
        public static EntityKlWindowFactory getFactory(EntityKlWindowType windowType) {
            return FACTORIES.get(windowType);
        }

        /**
         * Checks whether a factory is registered for the given window type.
         * <p>
         * This is a convenience method for checking if a factory exists without
         * retrieving it. It's useful for validation before attempting to create
         * a window of a specific type.
         *
         * @param windowType the window type to check
         * @return {@code true} if a factory is registered, {@code false} otherwise
         */
        public static boolean hasFactory(EntityKlWindowType windowType) {
            return FACTORIES.containsKey(windowType);
        }

        /**
         * Returns all window types for which factories have been registered.
         * <p>
         * This method provides a way to discover all available window types
         * in the current application context. It returns an array of all
         * window types that can be created through this registry.
         *
         * @return an array of registered {@link EntityKlWindowType}s
         */
        public static EntityKlWindowType[] getWindowTypes() {
            return FACTORIES.keySet().toArray(new EntityKlWindowType[0]);
        }
    }
}