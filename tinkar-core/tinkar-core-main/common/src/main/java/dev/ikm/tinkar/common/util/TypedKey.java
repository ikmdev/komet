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
package dev.ikm.tinkar.common.util;

import java.util.Map;
import java.util.Objects;

/**
 * A type-safe key for storing and retrieving values from map-like structures.
 * <p>
 * Provides compile-time type safety by encoding the value type in the key itself,
 * eliminating the need for unsafe casts when retrieving values from generic maps.
 * This is a general-purpose utility that can be used across the entire codebase.
 * <p>
 * <b>Can be implemented by enums</b> to gain singleton, iteration, and exhaustiveness benefits.
 *
 * <h2>Why TypedKey Exists: The Problem It Solves</h2>
 * <p>
 * Traditional {@code Map<String, Object>} usage suffers from type safety issues:
 * <pre>{@code
 * // Traditional approach - unsafe and error-prone:
 * Map<String, Object> config = new HashMap<>();
 * config.put("port", 8080);
 * config.put("host", "localhost");
 *
 * Integer port = (Integer) config.get("port");  // ⚠️ Unsafe cast
 * String host = (String) config.get("hsot");    // ⚠️ Typo not caught
 * Integer wrong = (Integer) config.get("host"); // ⚠️ ClassCastException at runtime!
 * }</pre>
 *
 * <p>
 * {@code TypedKey} solves these problems by making the type system work for you:
 * <ul>
 *   <li><b>Type mismatches caught at compile time</b> - not at runtime</li>
 *   <li><b>No unsafe casts</b> - compiler enforces type correctness</li>
 *   <li><b>IDE autocomplete</b> - shows correct return types</li>
 *   <li><b>Refactoring support</b> - type changes propagate automatically</li>
 *   <li><b>Typo prevention</b> - key constants prevent string typos</li>
 * </ul>
 *
 * <h2>Core Concept: Phantom Types + Interface Default Methods</h2>
 * <p>
 * {@code TypedKey<T>} uses the "phantom type" pattern - the type parameter {@code T}
 * exists only at compile time to provide type safety. This interface provides all
 * implementation via default methods, requiring only two abstract methods from implementors:
 * <ul>
 *   <li>{@link #getKey()} - Returns the string identifier</li>
 *   <li>{@link #getType()} - Returns the expected value type</li>
 * </ul>
 * <p>
 * This design enables both standalone instances (via {@link #of(String, Class)}) and
 * enum implementations for singleton + iteration benefits.
 *
 * <h2>Benefits</h2>
 * <ul>
 *   <li><b>Compile-time safety:</b> Type errors caught by compiler, not at runtime</li>
 *   <li><b>No unsafe casts:</b> Eliminates {@code (SomeType)} style casts</li>
 *   <li><b>Self-documenting:</b> Key declaration shows expected value type</li>
 *   <li><b>IDE support:</b> Full autocomplete with correct types</li>
 *   <li><b>Refactoring-safe:</b> Type changes propagate through codebase</li>
 *   <li><b>Framework agnostic:</b> Works with any Map-based storage</li>
 *   <li><b>Enum compatible:</b> Can be implemented by enums for additional benefits</li>
 * </ul>
 *
 * <h2>Usage Pattern 1: Standalone Constants</h2>
 * <pre>{@code
 * // Define typed keys
 * public static final TypedKey<String> USERNAME = TypedKey.of("username", String.class);
 * public static final TypedKey<Integer> AGE = TypedKey.of("age", Integer.class);
 * public static final TypedKey<List<String>> ROLES = TypedKey.of("roles", List.class);
 *
 * // Use with any Map
 * Map<String, Object> data = new HashMap<>();
 *
 * // Type-safe put - compiler enforces types!
 * USERNAME.put(data, "john.doe");
 * AGE.put(data, 42);
 *
 * // Type-safe get - no casting needed!
 * String username = USERNAME.get(data);  // Type-safe!
 * Integer age = AGE.get(data);           // Type-safe!
 *
 * // Compile errors for wrong types:
 * // USERNAME.put(data, 123);  // ✗ Won't compile!
 * }</pre>
 *
 * <h2>Usage Pattern 2: Enum Implementation (Recommended for Closed Sets)</h2>
 * <pre>{@code
 * // Enum provides singleton + iteration + exhaustiveness
 * public final class ConfigKeys {
 *     enum Keys {
 *         PORT(Integer.class),
 *         HOST(String.class),
 *         TIMEOUT(Long.class);
 *
 *         private final TypedKey<?> key;
 *
 *         Keys(Class<?> type) {
 *             this.key = TypedKey.of(this.name(), type);
 *         }
 *
 *         public TypedKey<?> key() { return key; }
 *     }
 *
 *     // Type-safe static constants
 *     public static final TypedKey<Integer> PORT = (TypedKey<Integer>) Keys.PORT.key();
 *     public static final TypedKey<String> HOST = (TypedKey<String>) Keys.HOST.key();
 *
 *     // Iteration support
 *     public static Keys[] allKeys() { return Keys.values(); }
 * }
 *
 * // Type-safe access
 * Integer port = ConfigKeys.PORT.get(map);
 *
 * // Iteration when needed
 * for (ConfigKeys.Keys key : ConfigKeys.allKeys()) {
 *     if (key.key().containsIn(map)) {
 *         System.out.println(key + " is set");
 *     }
 * }
 *
 * // Exhaustive switching
 * switch (key) {
 *     case PORT -> handlePort();
 *     case HOST -> handleHost();
 *     case TIMEOUT -> handleTimeout();
 *     // Compiler ensures all cases covered!
 * }
 * }</pre>
 *
 * <h2>Common Use Cases</h2>
 * <ul>
 *   <li>Configuration maps with heterogeneous value types</li>
 *   <li>Property bags in ViewModels or data transfer objects</li>
 *   <li>Cache keys with type information</li>
 *   <li>Context maps in frameworks</li>
 *   <li>Session/request attributes</li>
 * </ul>
 *
 * <h2>When to Use TypedKey vs Alternatives</h2>
 * <table border="1" cellpadding="5">
 * <caption>TypedKey vs Alternative Approaches</caption>
 * <tr>
 *   <th>Situation</th>
 *   <th>Use This</th>
 *   <th>Rationale</th>
 * </tr>
 * <tr>
 *   <td>Map with mixed value types</td>
 *   <td>{@code TypedKey<T>}</td>
 *   <td>Provides compile-time safety without wrapper objects</td>
 * </tr>
 * <tr>
 *   <td>Map with single value type</td>
 *   <td>{@code Map<String, MyType>}</td>
 *   <td>Built-in type safety is sufficient</td>
 * </tr>
 * <tr>
 *   <td>Fixed set of properties</td>
 *   <td>Regular class with fields</td>
 *   <td>More efficient and clearer than dynamic properties</td>
 * </tr>
 * <tr>
 *   <td>ViewModel properties</td>
 *   <td>{@code dev.ikm.komet.framework.property.TypedProperty}</td>
 *   <td>Specialized for Cognitive framework integration</td>
 * </tr>
 * <tr>
 *   <td>Closed set needing iteration</td>
 *   <td>Enum implementing TypedKey</td>
 *   <td>Singleton + iteration + exhaustiveness benefits</td>
 * </tr>
 * <tr>
 *   <td>Configuration/context passing</td>
 *   <td>{@code TypedKey<T>}</td>
 *   <td>Flexible and type-safe for dynamic settings</td>
 * </tr>
 * </table>
 *
 * <h2>Architectural Context</h2>
 * <p>
 * {@code TypedKey} is the foundation of a layered type-safe property system:
 * <ul>
 *   <li><b>tinkar-common:</b> {@code TypedKey} - Generic map-based type safety (this class)</li>
 *   <li><b>komet-framework:</b> {@code TypedProperty} - ViewModel-specific specialization</li>
 *   <li><b>Application layer:</b> Domain-specific property constants (e.g., {@code StampProperties})</li>
 * </ul>
 * <p>
 * Use {@code TypedKey} when working with general-purpose maps, caches, or configuration.
 * Use {@code TypedProperty} when working with Cognitive ViewModels in the UI layer.
 *
 * <h2>Design Decisions</h2>
 * <ul>
 *   <li><b>Interface with defaults:</b> Enables enum implementation without boilerplate</li>
 *   <li><b>Immutable:</b> Keys are immutable to enable safe use as map keys</li>
 *   <li><b>String-based:</b> Uses string identifiers for serialization compatibility</li>
 *   <li><b>Lightweight:</b> Minimal runtime overhead (just string comparison)</li>
 *   <li><b>Generic:</b> Works with any {@link Map} implementation</li>
 *   <li><b>Two factory methods:</b> {@code of(key, class)} for instances</li>
 * </ul>
 *
 * <h2>Best Practices</h2>
 * <ul>
 *   <li><b>Define keys as constants:</b> {@code public static final TypedKey<...>}</li>
 *   <li><b>Use enums for closed sets:</b> Enables iteration and exhaustiveness checking</li>
 *   <li><b>Group related keys:</b> Use dedicated classes for key collections</li>
 *   <li><b>Use descriptive names:</b> Key constants should clearly indicate purpose</li>
 *   <li><b>Document key ownership:</b> Clarify which component manages each key</li>
 * </ul>
 *
 * @param <T> the type of value associated with this key
 * See also: {@code dev.ikm.komet.framework.property.TypedProperty} for ViewModel-specific properties
 */
public interface TypedKey<T> {

    /**
     * Returns the string key identifier.
     * <p>
     * For enums, typically implemented as {@code return this.name();}.
     * For standalone instances, returns the key provided at construction.
     *
     * @return the key string (never null)
     */
    String getKey();

    /**
     * Returns the expected type of values for this key.
     * <p>
     * Used for runtime type checking and casting. For enums, typically stored
     * in a field and returned with appropriate casting.
     *
     * @return the value type (never null)
     */
    Class<T> getType();

    /**
     * Gets the typed value from the specified map.
     * <p>
     * Provides compile-time type safety through generics. Performs runtime type
     * checking and throws ClassCastException if stored value doesn't match expected type.
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * String username = USERNAME.get(map);  // Type-safe!
     * }</pre>
     *
     * @param map the map to retrieve the value from
     * @return the typed property value, or null if not present
     * @throws ClassCastException if stored value doesn't match expected type
     */
    default T get(Map<String, ?> map) {
        Object value = map.get(getKey());
        if (value != null && !getType().isInstance(value)) {
            throw new ClassCastException(
                "Key '" + getKey() + "' expected " + getType().getSimpleName() +
                " but found " + value.getClass().getSimpleName()
            );
        }
        return getType().cast(value);
    }

    /**
     * Puts the typed value into the specified map.
     * <p>
     * Provides compile-time type safety through generics. Performs runtime
     * validation to ensure type correctness before storing.
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * USERNAME.put(map, "john.doe");  // Type-safe!
     * }</pre>
     *
     * @param map the map to store the value in
     * @param value the value to store (must match expected type)
     * @throws ClassCastException if value doesn't match expected type
     */
    default void put(Map<String, Object> map, T value) {
        if (value != null && !getType().isInstance(value)) {
            throw new ClassCastException(
                "Key '" + getKey() + "' expects " + getType().getSimpleName() +
                " but received " + value.getClass().getSimpleName()
            );
        }
        map.put(getKey(), value);
    }

    /**
     * Checks if the map contains this key.
     *
     * @param map the map to check
     * @return true if the key exists in the map
     */
    default boolean containsIn(Map<String, ?> map) {
        return map.containsKey(getKey());
    }

    /**
     * Removes this key from the map and returns the previous value.
     *
     * @param map the map to remove the key from
     * @return the previous typed value, or null if not present
     */
    default T remove(Map<String, ?> map) {
        Object value = map.remove(getKey());
        return value != null ? getType().cast(value) : null;
    }

    /**
     * Factory method for creating standalone TypedKey instances.
     * <p>
     * Use this for ad-hoc keys or when you don't need enum benefits.
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * TypedKey<String> USERNAME = TypedKey.of("username", String.class);
     * }</pre>
     *
     * @param <T> the type of value this key holds
     * @param key the string identifier (must not be null or blank)
     * @param type the expected value type (must not be null)
     * @return a new TypedKey instance
     * @throws IllegalArgumentException if key is null/blank or type is null
     */
    static <T> TypedKey<T> of(String key, Class<T> type) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        return new TypedKeyImpl<>(key, type);
    }

    /**
     * Simple record implementation for standalone (non-enum) TypedKey instances.
     * <p>
     * This is an internal implementation detail used by the {@link #of(String, Class)}
     * factory method. Users should not instantiate this directly.
     */
    record TypedKeyImpl<T>(String key, Class<T> type) implements TypedKey<T> {
        /**
         * Compact constructor with validation.
         */
        public TypedKeyImpl {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Key cannot be null or blank");
            }
            if (type == null) {
                throw new IllegalArgumentException("Type cannot be null");
            }
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Class<T> getType() {
            return type;
        }

        @Override
        public String toString() {
            return "TypedKey[" + key + ": " + type.getSimpleName() + "]";
        }
    }
}
