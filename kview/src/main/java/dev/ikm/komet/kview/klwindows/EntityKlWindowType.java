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

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Interface representing a category of window within the Komet workspace.
 * <p>
 * {@code EntityKlWindowType} implementations allow the workspace to organize, persist, and restore
 * windows consistently across sessions. Each type provides a unique prefix
 * for generating identifiers and associating window instances with their type.
 * <p>
 * Both enum and non-enum implementations are supported, with different registration
 * methods for each case. Enum implementations are registered as a class, while non-enum
 * implementations are registered as individual instances.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Using built-in enum type
 * EntityKlWindowType windowType = EntityKlWindowTypes.CONCEPT;
 * String prefix = windowType.getPrefix();
 *
 * // Looking up a window type
 * EntityKlWindowType foundType = EntityKlWindowType.fromString("CONCEPT");
 * }</pre>
 *
 * @see EntityKlWindowTypes
 */
public interface EntityKlWindowType {
    /**
     * Returns the unique prefix associated with this window type.
     * Used to generate identifiers for windows of this type and to identify
     * window instances when persisting or restoring the workspace.
     *
     * @return non-null prefix string that uniquely identifies this window type
     */
    String getPrefix();

    /**
     * Returns a string representation of this window type.
     * By default, enum constants will return their name, but implementations
     * may override this to provide a more descriptive representation.
     *
     * @return non-null string representation of this window type
     */
    String toString();

    /**
     * Converts a string representation to a {@code EntityKlWindowType} instance.
     * This method is maintained for backward compatibility with older code.
     * <p>
     * The lookup is performed by the {@link Registry#fromString(String)} method,
     * matching against prefix, enum name (if applicable), or custom string
     * representation (case-insensitive).
     *
     * @param value string representation of a {@code EntityKlWindowType}
     * @return matching {@code EntityKlWindowType} instance
     * @throws IllegalArgumentException if value is null or no matching window type is found
     * @see Registry#fromString(String)
     */
    static EntityKlWindowType fromString(String value) {
        return Registry.fromString(value);
    }

    /**
     * Central registry for {@link EntityKlWindowType} implementations.
     * <p>
     * Provides methods to register classes and instances and perform lookups by string value,
     * matching on prefix, enum name (if applicable), or custom {@code toString()}
     * value (case-insensitive).
     * <p>
     * The registry is thread-safe, using {@link CopyOnWriteArrayList} for concurrent access.
     */
    class Registry {

        private static final List<Class<? extends EntityKlWindowType>> REGISTERED_ENUM_CLASSES = new CopyOnWriteArrayList<>();
        private static final List<EntityKlWindowType> REGISTERED_INSTANCES = new CopyOnWriteArrayList<>();

        // Initialize with built-in enum types
        static {
            registerEnumType(EntityKlWindowTypes.class);
        }

        // Prevent instantiation
        private Registry() { }

        /**
         * Registers an enum class implementing {@link EntityKlWindowType} for lookup.
         * All enum constants from this class will be available for lookup through
         * the {@link #fromString(String)} method.
         *
         * @param enumClass the enum class to register (must implement {@code EntityKlWindowType})
         * @throws IllegalArgumentException if enumClass is null or not an enum class
         *         that implements {@code EntityKlWindowType}
         */
        public static void registerEnumType(Class<? extends EntityKlWindowType> enumClass) {
            if (enumClass == null) {
                throw new IllegalArgumentException("Cannot register null class for EntityKlWindowType");
            }
            if (!enumClass.isEnum()) {
                throw new IllegalArgumentException(enumClass.getName()
                        + " must be an enum that implements EntityKlWindowType");
            }
            if (!REGISTERED_ENUM_CLASSES.contains(enumClass)) {
                REGISTERED_ENUM_CLASSES.add(enumClass);
            }
        }

        /**
         * Registers a single {@link EntityKlWindowType} instance for lookup.
         * This method is used for non-enum {@code EntityKlWindowType} implementations.
         * <p>
         * The registered instance will be available for lookup through
         * the {@link #fromString(String)} method.
         *
         * @param instance the {@code EntityKlWindowType} instance to register
         * @throws IllegalArgumentException if instance is null or has a prefix that
         *         duplicates one already registered
         */
        public static void registerInstance(EntityKlWindowType instance) {
            if (instance == null) {
                throw new IllegalArgumentException("Cannot register null instance for EntityKlWindowType");
            }

            // Check for duplicate prefix
            String prefix = instance.getPrefix();
            if (isDuplicatePrefix(prefix)) {
                throw new IllegalArgumentException("Window type with prefix '"
                        + prefix + "' is already registered");
            }

            REGISTERED_INSTANCES.add(instance);
        }

        /**
         * Checks if a prefix would be a duplicate across all registered window types.
         * This is used internally to ensure uniqueness of prefixes.
         *
         * @param prefix the prefix to check for duplication
         * @return true if the prefix already exists in any registered window type, false otherwise
         */
        private static boolean isDuplicatePrefix(String prefix) {
            // Check enum constants
            for (Class<? extends EntityKlWindowType> enumClass : REGISTERED_ENUM_CLASSES) {
                for (Object constant : enumClass.getEnumConstants()) {
                    EntityKlWindowType wt = (EntityKlWindowType) constant;
                    if (wt.getPrefix().equals(prefix)) {
                        return true;
                    }
                }
            }

            // Check individual instances
            for (EntityKlWindowType instance : REGISTERED_INSTANCES) {
                if (instance.getPrefix().equals(prefix)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Looks up a {@link EntityKlWindowType} instance by string across all registered sources.
         * Matching is performed case-insensitively against:
         * <ul>
         *   <li>The prefix returned by {@link EntityKlWindowType#getPrefix()}</li>
         *   <li>The enum constant name (for enum-based implementations)</li>
         *   <li>The string representation returned by {@link EntityKlWindowType#toString()}</li>
         * </ul>
         *
         * @param value string to convert to a window type (must not be null)
         * @return matching {@code EntityKlWindowType} instance
         * @throws IllegalArgumentException if value is null or no match is found
         */
        public static EntityKlWindowType fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Window type lookup value cannot be null");
            }
            String key = value.trim().toLowerCase(Locale.ROOT);

            // First check registered enum classes
            for (Class<? extends EntityKlWindowType> enumClass : REGISTERED_ENUM_CLASSES) {
                for (Object constant : enumClass.getEnumConstants()) {
                    EntityKlWindowType wt = (EntityKlWindowType) constant;
                    if (matches(wt, key, constant instanceof Enum<?> ? ((Enum<?>) constant).name() : null)) {
                        return wt;
                    }
                }
            }

            // Then check registered individual instances
            for (EntityKlWindowType instance : REGISTERED_INSTANCES) {
                if (matches(instance, key, null)) {
                    return instance;
                }
            }

            throw new IllegalArgumentException("Unknown window type: " + value);
        }

        /**
         * Helper method to check if a {@code EntityKlWindowType} matches a given key.
         * Compares the lowercase representations of the prefix, toString() value,
         * and enum name (if applicable) against the provided key.
         *
         * @param wt the window type to check
         * @param key the lowercase search key to match against
         * @param enumName the enum constant name, if applicable, or null
         * @return true if the window type matches the key, false otherwise
         */
        private static boolean matches(EntityKlWindowType wt, String key, String enumName) {
            final String prefix = wt.getPrefix().toLowerCase(Locale.ROOT);
            final String repr = wt.toString().toLowerCase(Locale.ROOT);

            return prefix.equals(key) || repr.equals(key) ||
                    (enumName != null && enumName.toLowerCase(Locale.ROOT).equals(key));
        }

        /**
         * Returns an unmodifiable list of all registered {@code EntityKlWindowType} instances.
         * This includes all enum constants from registered enum classes
         * and all individually registered instances.
         *
         * @return an unmodifiable list of all available {@code EntityKlWindowType} instances
         */
        public static List<EntityKlWindowType> getAllTypes() {
            List<EntityKlWindowType> allTypes = new ArrayList<>();

            // Add all enum constants
            for (Class<? extends EntityKlWindowType> enumClass : REGISTERED_ENUM_CLASSES) {
                Collections.addAll(allTypes, enumClass.getEnumConstants());
            }

            // Add all individually registered instances
            allTypes.addAll(REGISTERED_INSTANCES);

            return Collections.unmodifiableList(allTypes);
        }
    }
}