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

import dev.ikm.komet.preferences.KometPreferences;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.stream.Stream;

import static dev.ikm.komet.kview.controls.KLWorkspace.DEFAULT_WINDOW_WIDTH;

/**
 * Encapsulates the state of a window associated with a specific entity in the Komet workspace.
 * <p>
 * This class manages both the UI-related properties (position, size) and entity-related
 * metadata for windows within the application. It provides serialization and deserialization
 * capabilities to persist window states between application sessions using the
 * {@link KometPreferences} framework.
 * <p>
 * The class includes:
 * <ul>
 *   <li>Core window properties (position, size, identifier)</li>
 *   <li>Entity reference properties (UUID, NID, type information)</li>
 *   <li>Support for arbitrary additional properties through a key-value mechanism</li>
 *   <li>Builder pattern for convenient instance creation</li>
 *   <li>Preference persistence and restoration facilities</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Create a window state
 * EntityKlWindowState state = EntityKlWindowState.builder()
 *     .windowId(UUID.randomUUID())
 *     .windowType(EntityKlWindowTypes.CONCEPT)
 *     .position(100, 200)
 *     .size(800, 600)
 *     .entityUuid(conceptUuid)
 *     .property("mode", "EDIT")
 *     .build();
 *
 * // Save state to preferences
 * state.saveToPreferences(preferences);
 *
 * // Restore from preferences
 * EntityKlWindowState restored = EntityKlWindowState.fromPreferences(preferences);
 * }</pre>
 *
 * @see EntityKlWindowType
 * @see KometPreferences
 */
public class EntityKlWindowState {

    private static final Logger LOG = LoggerFactory.getLogger(EntityKlWindowState.class);

    /**
     * Preference key for the window's unique identifier.
     */
    public static final String WINDOW_ID = "WINDOW_ID";

    /**
     * Preference key for the window's type classification.
     */
    public static final String WINDOW_TYPE = "WINDOW_TYPE";

    /**
     * Preference key for the window's X-coordinate position.
     */
    public static final String WINDOW_X_POS = "WINDOW_X_POS";

    /**
     * Preference key for the window's Y-coordinate position.
     */
    public static final String WINDOW_Y_POS = "WINDOW_Y_POS";

    /**
     * Preference key for the window's width.
     */
    public static final String WINDOW_WIDTH = "WINDOW_WIDTH";

    /**
     * Preference key for the window's height.
     */
    public static final String WINDOW_HEIGHT = "WINDOW_HEIGHT";

    /**
     * Preference key for the associated entity's UUID.
     */
    public static final String ENTITY_UUID = "ENTITY_UUID";

    /**
     * Preference key for the associated entity's NID (Node Identifier).
     */
    public static final String ENTITY_NID = "ENTITY_NID";

    /**
     * Preference key for the associated entity's NID type classification.
     */
    public static final String ENTITY_NID_TYPE = "ENTITY_NID_TYPE";

    /**
     * Preference key for storing the properties panel open/closed state.
     */
    public static final String PROPERTY_PANEL_OPEN = "PROPERTY_PANEL_OPEN";

    // Core window properties
    private UUID windowId;
    private EntityKlWindowType windowType;
    private double xPos;
    private double yPos;
    private double width;
    private double height;

    // Entity properties
    private UUID entityUuid;
    private int entityNid;
    private String entityNidType;

    // Additional custom properties
    private final MutableMap<String, Object> additionalProperties = Maps.mutable.empty();

    /**
     * Returns the unique identifier for this window.
     * <p>
     * This UUID distinguishes this window instance from all others in the workspace
     * and is used as the primary key when persisting window state.
     *
     * @return the window's unique identifier
     */
    public UUID getWindowId() {
        return windowId;
    }

    /**
     * Sets the unique identifier for this window.
     *
     * @param windowId the UUID to assign as this window's identifier
     */
    public void setWindowId(UUID windowId) {
        this.windowId = windowId;
    }

    /**
     * Returns the type classification of this window.
     * <p>
     * The window type determines the functional category of the window
     * and affects how it behaves and what content it can display.
     *
     * @return the window's type classification
     * @see EntityKlWindowType
     */
    public EntityKlWindowType getWindowType() {
        return windowType;
    }

    /**
     * Sets the type classification for this window.
     *
     * @param windowType the type to assign to this window
     * @see EntityKlWindowType
     */
    public void setWindowType(EntityKlWindowType windowType) {
        this.windowType = windowType;
    }

    /**
     * Returns the X-coordinate position of this window.
     * <p>
     * This coordinate represents the horizontal position of the window's
     * top-left corner within the application workspace.
     *
     * @return the X-coordinate position
     */
    public double getXPos() {
        return xPos;
    }

    /**
     * Sets the X-coordinate position of this window.
     *
     * @param xPos the X-coordinate to set
     */
    public void setXPos(double xPos) {
        this.xPos = xPos;
    }

    /**
     * Returns the Y-coordinate position of this window.
     * <p>
     * This coordinate represents the vertical position of the window's
     * top-left corner within the application workspace.
     *
     * @return the Y-coordinate position
     */
    public double getYPos() {
        return yPos;
    }

    /**
     * Sets the Y-coordinate position of this window.
     *
     * @param yPos the Y-coordinate to set
     */
    public void setYPos(double yPos) {
        this.yPos = yPos;
    }

    /**
     * Returns the width of this window.
     *
     * @return the window width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the width of this window.
     *
     * @param width the width to set
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Returns the height of this window.
     *
     * @return the window height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the height of this window.
     *
     * @param height the height to set
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Returns the UUID of the entity associated with this window.
     * <p>
     * This UUID identifies the primary data entity (such as a concept or pattern)
     * that the window is displaying or editing.
     *
     * @return the associated entity's UUID, or null if no entity is associated
     */
    public UUID getEntityUuid() {
        return entityUuid;
    }

    /**
     * Sets the UUID of the entity associated with this window.
     *
     * @param entityUuid the UUID of the entity to associate with this window
     */
    public void setEntityUuid(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }

    /**
     * Returns the NID (Node Identifier) of the entity associated with this window.
     * <p>
     * The NID is an internal identifier used to reference entities within the
     * knowledge model system.
     *
     * @return the associated entity's NID, or 0 if no entity is associated
     */
    public int getEntityNid() {
        return entityNid;
    }

    /**
     * Sets the NID (Node Identifier) of the entity associated with this window.
     *
     * @param entityNid the NID of the entity to associate with this window
     */
    public void setEntityNid(int entityNid) {
        this.entityNid = entityNid;
    }

    /**
     * Returns the type classification of the associated entity's NID.
     * <p>
     * This provides additional metadata about the kind of entity
     * referenced by the NID.
     * <p>
     * If the entityNidType field is null, this method attempts to retrieve
     * the value from the additional properties map.
     *
     * @return the entity NID type, or null if not set
     */
    public String getEntityNidType() {
        if (entityNidType != null) {
            return entityNidType;
        }

        // Check if it's stored in properties
        return getStringProperty(ENTITY_NID_TYPE, null);
    }

    /**
     * Sets the type classification of the associated entity's NID.
     * <p>
     * This method also adds the value to the additional properties map for
     * redundancy and backward compatibility.
     *
     * @param nidType the entity NID type to set
     */
    public void setEntityNidType(String nidType) {
        this.entityNidType = nidType;
        if (nidType != null) {
            addProperty(ENTITY_NID_TYPE, nidType);
        }
    }

    /**
     * Sets both X and Y position coordinates in one call.
     * <p>
     * This is a convenience method for setting the window position.
     *
     * @param x the X-coordinate position
     * @param y the Y-coordinate position
     */
    public void setPosition(double x, double y) {
        this.xPos = x;
        this.yPos = y;
    }

    /**
     * Sets both width and height in one call.
     * <p>
     * This is a convenience method for setting the window size.
     *
     * @param width  the window width
     * @param height the window height
     */
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Adds a custom property to the window state.
     * <p>
     * This method allows storing arbitrary additional data associated with
     * the window beyond the predefined fields. These properties are persisted
     * alongside the standard properties when saving to preferences.
     * <p>
     * Supported property value types include String, Integer, Double, Boolean,
     * and UUID. Other types will be stored using their string representation.
     *
     * @param key   the property key (cannot be null or empty)
     * @param value the property value to store
     * @throws IllegalArgumentException if the key is null or empty (logged as warning)
     */
    public void addProperty(String key, Object value) {
        if (key == null || key.isEmpty()) {
            LOG.warn("Attempted to add property with null or empty key");
            return;
        }
        additionalProperties.put(key, value);
    }

    /**
     * Retrieves a custom property value by key.
     * <p>
     * This method returns the raw Object value stored for the given key.
     * For type-safe access, use the typed getter methods instead.
     *
     * @param key the property key to look up
     * @return the property value, or null if the key doesn't exist
     * @see #getStringProperty(String, String)
     * @see #getIntProperty(String, int)
     * @see #getDoubleProperty(String, double)
     * @see #getBooleanProperty(String, boolean)
     */
    public Object getProperty(String key) {
        return additionalProperties.get(key);
    }

    /**
     * Retrieves a custom property value wrapped in an Optional.
     * <p>
     * This method provides a null-safe way to access property values.
     *
     * @param key the property key to look up
     * @return an Optional containing the property value, or empty if the key doesn't exist
     */
    public Optional<Object> getPropertyOptional(String key) {
        return Optional.ofNullable(additionalProperties.get(key));
    }

    /**
     * Retrieves a string property with a default value fallback.
     * <p>
     * This method attempts to retrieve a property value as a String.
     * If the key doesn't exist or the value is not a String, the default value is returned.
     *
     * @param key          the property key to look up
     * @param defaultValue the default value to return if the property is missing or not a string
     * @return the property value as a String, or the default value
     */
    public String getStringProperty(String key, String defaultValue) {
        Object value = additionalProperties.get(key);
        return (value instanceof String) ? (String) value : defaultValue;
    }

    /**
     * Retrieves an integer property with a default value fallback.
     * <p>
     * This method attempts to retrieve a property value as an Integer.
     * If the key doesn't exist or the value is not an Integer, the default value is returned.
     *
     * @param key          the property key to look up
     * @param defaultValue the default value to return if the property is missing or not an integer
     * @return the property value as an integer, or the default value
     */
    public int getIntProperty(String key, int defaultValue) {
        Object value = additionalProperties.get(key);
        return (value instanceof Integer) ? (Integer) value : defaultValue;
    }

    /**
     * Retrieves a double property with a default value fallback.
     * <p>
     * This method attempts to retrieve a property value as a Double.
     * If the key doesn't exist or the value is not a Double, the default value is returned.
     *
     * @param key          the property key to look up
     * @param defaultValue the default value to return if the property is missing or not a double
     * @return the property value as a double, or the default value
     */
    public double getDoubleProperty(String key, double defaultValue) {
        Object value = additionalProperties.get(key);
        return (value instanceof Double) ? (Double) value : defaultValue;
    }

    /**
     * Retrieves a boolean property with a default value fallback.
     * <p>
     * This method attempts to retrieve a property value as a Boolean.
     * If the key doesn't exist or the value is not a Boolean, the default value is returned.
     *
     * @param key          the property key to look up
     * @param defaultValue the default value to return if the property is missing or not a boolean
     * @return the property value as a boolean, or the default value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        Object value = additionalProperties.get(key);
        return (value instanceof Boolean) ? (Boolean) value : defaultValue;
    }

    /**
     * Creates a new builder for constructing instances of this class.
     * <p>
     * The builder pattern provides a fluent API for creating window state instances
     * with a readable, chained syntax.
     *
     * @return a new builder instance
     * @see Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating {@link EntityKlWindowState} instances.
     * <p>
     * This class provides a fluent API for constructing window state objects
     * with various combinations of properties in a readable, chained syntax.
     * <p>
     * Example usage:
     * <pre>{@code
     * EntityKlWindowState state = EntityKlWindowState.builder()
     *     .windowId(UUID.randomUUID())
     *     .windowType(EntityKlWindowTypes.CONCEPT)
     *     .position(100, 200)
     *     .size(800, 600)
     *     .build();
     * }</pre>
     */
    public static class Builder {
        private final EntityKlWindowState state = new EntityKlWindowState();

        /**
         * Sets the window identifier.
         *
         * @param windowId the UUID to use as the window identifier
         * @return this builder instance for method chaining
         */
        public Builder windowId(UUID windowId) {
            state.setWindowId(windowId);
            return this;
        }

        /**
         * Sets the window type.
         *
         * @param windowType the window type to assign
         * @return this builder instance for method chaining
         */
        public Builder windowType(EntityKlWindowType windowType) {
            state.setWindowType(windowType);
            return this;
        }

        /**
         * Sets the window position.
         *
         * @param x the X-coordinate position
         * @param y the Y-coordinate position
         * @return this builder instance for method chaining
         */
        public Builder position(double x, double y) {
            state.setPosition(x, y);
            return this;
        }

        /**
         * Sets the window size.
         *
         * @param width  the window width
         * @param height the window height
         * @return this builder instance for method chaining
         */
        public Builder size(double width, double height) {
            state.setSize(width, height);
            return this;
        }

        /**
         * Sets the associated entity UUID.
         *
         * @param uuid the entity UUID to associate with the window
         * @return this builder instance for method chaining
         */
        public Builder entityUuid(UUID uuid) {
            state.setEntityUuid(uuid);
            return this;
        }

        /**
         * Sets the associated entity NID.
         *
         * @param nid the entity NID to associate with the window
         * @return this builder instance for method chaining
         */
        public Builder entityNid(int nid) {
            state.setEntityNid(nid);
            return this;
        }

        /**
         * Sets the associated entity NID type.
         *
         * @param nidType the entity NID type classification
         * @return this builder instance for method chaining
         */
        public Builder entityNidType(String nidType) {
            state.setEntityNidType(nidType);
            return this;
        }

        /**
         * Adds a custom property to the window state.
         *
         * @param key   the property key
         * @param value the property value
         * @return this builder instance for method chaining
         */
        public Builder property(String key, Object value) {
            state.addProperty(key, value);
            return this;
        }

        /**
         * Builds and returns the configured window state instance.
         *
         * @return the fully configured {@link EntityKlWindowState} instance
         */
        public EntityKlWindowState build() {
            return state;
        }
    }

    /**
     * Saves the window state to the specified preferences node.
     * <p>
     * This method serializes all window properties, including core properties,
     * entity properties, and any additional custom properties, to the given
     * preferences node. The properties are stored using standardized key names
     * defined by the class constants.
     * <p>
     * Additional properties are automatically prefixed with "PROP_" when stored
     * to distinguish them from core properties.
     *
     * @param preferences the preferences node to save to
     * @return true if the save operation completed successfully, false if an error occurred
     * @throws IllegalArgumentException if preferences is null (logged as error)
     */
    public boolean saveToPreferences(KometPreferences preferences) {
        if (preferences == null) {
            LOG.error("Cannot save window state: preferences is null");
            return false;
        }

        try {
            // Save core window properties
            preferences.put(WINDOW_ID, windowId.toString());
            preferences.put(WINDOW_TYPE, windowType.toString());
            preferences.putDouble(WINDOW_X_POS, xPos);
            preferences.putDouble(WINDOW_Y_POS, yPos);
            preferences.putDouble(WINDOW_WIDTH, width);
            preferences.putDouble(WINDOW_HEIGHT, height);

            // Save entity properties
            if (entityUuid != null) {
                preferences.put(ENTITY_UUID, entityUuid.toString());
            }

            if (entityNid != 0) {
                preferences.putInt(ENTITY_NID, entityNid);
            }

            if (entityNidType != null) {
                preferences.put(ENTITY_NID_TYPE, entityNidType);
            }

            // Save additional properties
            saveAdditionalProperties(preferences);

            return true;
        } catch (Exception e) {
            LOG.error("Error saving window state to preferences", e);
            return false;
        }
    }

    /**
     * Helper method to save additional custom properties to preferences.
     * <p>
     * This method iterates through all entries in the additionalProperties map
     * and stores them in the preferences node with an appropriate type-specific method.
     * Property keys are prefixed with "PROP_" to distinguish them from core properties.
     *
     * @param preferences the preferences node to save to
     */
    private void saveAdditionalProperties(KometPreferences preferences) {
        additionalProperties.forEach((key, value) -> {
            final String propKey = "PROP_" + key;

            switch (value) {
                case Integer intValue -> preferences.putInt(propKey, intValue);
                case Long longValue -> preferences.putLong(propKey, longValue);
                case Double doubleValue -> preferences.putDouble(propKey, doubleValue);
                case Boolean booleanValue -> preferences.putBoolean(propKey, booleanValue);
                case UUID uuidValue -> preferences.put(propKey, uuidValue.toString());
                case String strValue -> preferences.put(propKey, strValue);
                case null -> { /* Skip null values */ }
                default -> preferences.put(propKey, value.toString());
            }
        });
    }

    /**
     * Restores and creates a window state instance from the specified preferences node.
     * <p>
     * This method deserializes all window properties from the given preferences node,
     * recreating a complete window state object. It loads core properties, entity
     * properties, and any additional custom properties that were previously stored.
     * <p>
     * Required core properties (window ID and type) must be present in the preferences;
     * an exception is thrown if they are missing. Other properties will use defaults
     * if not found.
     *
     * @param preferences the preferences node to load from
     * @return a new window state instance populated with values from preferences
     * @throws IllegalArgumentException if preferences is null or required properties are missing
     */
    public static EntityKlWindowState fromPreferences(KometPreferences preferences) {
        if (preferences == null) {
            LOG.error("Cannot load window state: preferences is null");
            throw new IllegalArgumentException("Cannot load window state: preferences is null");
        }

        try {
            EntityKlWindowState windowState = new EntityKlWindowState();

            // Load core window properties
            windowState.windowId = UUID.fromString(preferences.get(WINDOW_ID).orElseThrow());
            windowState.windowType = EntityKlWindowType.fromString(preferences.get(WINDOW_TYPE).orElseThrow());
            windowState.xPos = preferences.getDouble(WINDOW_X_POS, 0);
            windowState.yPos = preferences.getDouble(WINDOW_Y_POS, 0);
            windowState.width = preferences.getDouble(WINDOW_WIDTH, DEFAULT_WINDOW_WIDTH);
            windowState.height = preferences.getDouble(WINDOW_HEIGHT, -1); // Compute height based on content

            // Load entity properties
            loadEntityProperties(preferences, windowState);

            // Load additional properties
            loadAdditionalProperties(preferences, windowState);

            return windowState;
        } catch (Exception e) {
            LOG.error("Error loading window state from preferences", e);
            throw new IllegalArgumentException("Error loading window state from preferences", e);
        }
    }

    /**
     * Helper method to load entity properties from preferences.
     * <p>
     * This method extracts entity-related properties (UUID, NID, and NID type)
     * from the given preferences node and populates the window state object.
     *
     * @param preferences the preferences node to load from
     * @param state       the window state instance to populate
     */
    private static void loadEntityProperties(KometPreferences preferences, EntityKlWindowState state) {
        preferences.get(ENTITY_UUID).ifPresent(uuid -> {
            try {
                state.entityUuid = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                LOG.error("Invalid UUID format: {}", uuid, e);
            }
        });

        preferences.getInt(ENTITY_NID).ifPresent(nid -> state.entityNid = nid);
        preferences.get(ENTITY_NID_TYPE).ifPresent(nidType -> state.entityNidType = nidType);
    }

    /**
     * Helper method to load additional custom properties from preferences.
     * <p>
     * This method looks for properties with keys prefixed with "PROP_" in the
     * preferences node and adds them to the window state's additionalProperties map.
     * It attempts to load each property with its appropriate type.
     *
     * @param preferences the preferences node to load from
     * @param state       the window state instance to populate
     */
    private static void loadAdditionalProperties(KometPreferences preferences, EntityKlWindowState state)
            throws BackingStoreException {
        Arrays.stream(preferences.keys())
                .filter(key -> key.startsWith("PROP_"))
                .forEach(key -> {
                    final String propertyName = key.substring(5);

                    Stream.<Optional<?>>of(tryLoadBoolean(preferences, key),
                                    tryLoadInteger(preferences, key),
                                    tryLoadLong(preferences, key),
                                    tryLoadDouble(preferences, key),
                                    tryLoadString(preferences, key))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .ifPresent(value -> state.additionalProperties.put(propertyName, value));
                });
    }

    /**
     * Attempts to load a boolean value from preferences for the given key.
     * <p>
     * This method tries to retrieve a boolean value from the preferences node.
     * If the retrieval fails (e.g., the value is not a boolean), an empty Optional is returned.
     *
     * @param preferences the preferences node to load from
     * @param key         the preference key to retrieve
     * @return an Optional containing the boolean value if successful, empty otherwise
     */
    private static Optional<Boolean> tryLoadBoolean(KometPreferences preferences, String key) {
        try {
            return preferences.getBoolean(key);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to load an integer value from preferences for the given key.
     * <p>
     * This method tries to retrieve an integer value from the preferences node.
     * If the retrieval fails (e.g., the value is not an integer), an empty Optional is returned.
     *
     * @param preferences the preferences node to load from
     * @param key         the preference key to retrieve
     * @return an Optional containing the integer value if successful, empty otherwise
     */
    private static Optional<Integer> tryLoadInteger(KometPreferences preferences, String key) {
        try {
            return preferences.getInt(key).stream().boxed().findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to load a long value from preferences for the given key.
     * <p>
     * This method tries to retrieve a long value from the preferences node.
     * If the retrieval fails (e.g., the value is not a long), an empty Optional is returned.
     *
     * @param preferences the preferences node to load from
     * @param key         the preference key to retrieve
     * @return an Optional containing the long value if successful, empty otherwise
     */
    private static Optional<Long> tryLoadLong(KometPreferences preferences, String key) {
        try {
            return preferences.getLong(key).stream().boxed().findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to load a double value from preferences for the given key.
     * <p>
     * This method tries to retrieve a double value from the preferences node.
     * If the retrieval fails (e.g., the value is not a double), an empty Optional is returned.
     *
     * @param preferences the preferences node to load from
     * @param key         the preference key to retrieve
     * @return an Optional containing the double value if successful, empty otherwise
     */
    private static Optional<Double> tryLoadDouble(KometPreferences preferences, String key) {
        try {
            return preferences.getDouble(key).stream().boxed().findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to load a string value from preferences for the given key.
     * <p>
     * This method tries to retrieve a string value from the preferences node.
     * It also checks if the string represents a UUID and validates it.
     * If the retrieval fails, an empty Optional is returned.
     *
     * @param preferences the preferences node to load from
     * @param key         the preference key to retrieve
     * @return an Optional containing the string value if successful, empty otherwise
     */
    private static Optional<String> tryLoadString(KometPreferences preferences, String key) {
        Optional<String> stringValue = preferences.get(key);
        if (stringValue.isPresent()) {
            // Check if it's a UUID string
            String value = stringValue.get();
            try {
                final UUID uuid = UUID.fromString(value);
                return Optional.of(uuid.toString());
            } catch (IllegalArgumentException ex) {
                return Optional.of(value);
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns a string representation of this window state.
     * <p>
     * The string includes key information about the window, including its
     * identifier, type, position, size, associated entity details, and
     * the number of additional properties.
     *
     * @return a string representation of the window state
     */
    @Override
    public String toString() {
        return "{" +
                "windowType='" + windowType + '\'' +
                ", windowId='" + windowId + '\'' +
                ", position=(" + xPos + "," + yPos + ")" +
                ", size=(" + width + "x" + height + ")" +
                ", entityUuid=" + entityUuid +
                ", entityNid=" + entityNid +
                ", entityNidType=" + entityNidType +
                ", additionalProperties={" + additionalProperties.entrySet().stream().map(
                entry -> entry.getKey() + "=" + entry.getValue()
        ).reduce((a, b) -> a + ", " + b).orElse("") + "}" +
                '}';
    }
}