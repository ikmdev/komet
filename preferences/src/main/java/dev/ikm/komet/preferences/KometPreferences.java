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
package dev.ikm.komet.preferences;

import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.terms.*;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * 
 */
public interface KometPreferences {

    /**
     * Copies the current subtree to the specified subtreeCopyParent node in the preferences tree.
     *
     * @param newParent the Preferences node to which the current subtree will be copied
     * @param overwrite a boolean flag indicating whether to overwrite existing preferences
     *                  at the destination node if they conflict with this subtree
     * @return true if the operation was successful, false otherwise
     * @throws BackingStoreException if an error occurs while trying to access or modify the backing store
     */
    default boolean copyThisSubtreeTo(Preferences newParent, boolean overwrite) throws BackingStoreException {
        return copyThisSubtreeTo(this, newParent, overwrite);
    }

    static boolean copyThisSubtreeTo(KometPreferences oldNodeToCopyFrom, Preferences newParent, boolean overwrite) throws BackingStoreException {
        List<String> childrenNames = List.of(newParent.childrenNames());
        boolean childAlreadyExists = childrenNames.contains(oldNodeToCopyFrom.name());
        if (!overwrite && childAlreadyExists) {
            return false;
        }
        if (childAlreadyExists) {
            newParent.node(oldNodeToCopyFrom.name()).removeNode();
            newParent.flush();
        }
        for (String childName : newParent.childrenNames()) {
            KometPreferences existingChild = oldNodeToCopyFrom.node(childName);
            Preferences newChild = newParent.node(childName);
            recursiveAdd(existingChild, newChild);
        }
        newParent.flush();
        return true;
    }
    static void recursiveAdd(KometPreferences oldNodeToCopyFrom, Preferences newParentToAddTo) throws BackingStoreException {
        for (String childName : oldNodeToCopyFrom.childrenNames()) {
            KometPreferences existingChild = oldNodeToCopyFrom.node(childName);
            Preferences newChild = newParentToAddTo.node(childName);
            for (String key: existingChild.keys()) {
                newChild.put(key, existingChild.get(key, null));
            }
            newChild.flush();
            recursiveAdd(existingChild, newChild);
        }
    }


    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   an enumeration used to reference the key
     * @param value the value to be associated with the specified key
     */
    default void put(Enum key, String value) {
        put(enumToGeneralKey(key), value);
    }

    /**
     * Associates the specified value with the specified key in this preference
     * node.
     *
     * @param key   key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @throws NullPointerException     if key or value is <tt>null</tt>.
     * @throws IllegalArgumentException if <tt>key.length()</tt> exceeds
     *                                  <tt>MAX_KEY_LENGTH</tt> or if <tt>value.length</tt> exceeds
     *                                  <tt>MAX_VALUE_LENGTH</tt>.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     */
    void put(String key, String value);

    /**
     * Converts an Enum key into a generalized string representation suitable for use as a unique key.
     * The generated string format includes parts of the package name, class name, enum name, and a UUID suffix.
     * Ensures the resulting key does not exceed the maximum allowed length.
     *
     * @param key the Enum key to be converted into a generalized string representation
     * @return a generated string representing the Enum key, which satisfies the maximum length constraint
     * @throws IllegalStateException if the generated key exceeds the maximum allowed length
     */
    default String enumToGeneralKey(Enum key) {
        UUID uuidSuffix = UUID.nameUUIDFromBytes(key.getDeclaringClass().getName().getBytes());
        StringBuilder sb = new StringBuilder();
        String nameToSplit;
        if (key.getDeclaringClass().getEnclosingClass() != null) {
            nameToSplit = key.getDeclaringClass().getEnclosingClass().getCanonicalName();
        } else {
            nameToSplit = key.getDeclaringClass().getPackageName();
        }
        String[] classParts = nameToSplit.split("\\.");
        for (String part : classParts) {
            sb.append(part.charAt(0));
        }
        sb.append(".").append(key.getDeclaringClass().getSimpleName());
        sb.append(".").append(key.name());

        String prefix = sb.toString();
        if (prefix.length() > Preferences.MAX_KEY_LENGTH) {
            throw new IllegalStateException("MAX_KEY_LENGTH exceeded by " + prefix);
        }

        String stringKey = prefix + "_" + uuidSuffix;

        if (stringKey.length() > Preferences.MAX_KEY_LENGTH) {
            int sizeToRemove = stringKey.length() - Preferences.MAX_KEY_LENGTH;
            stringKey = stringKey.substring(sizeToRemove);
        }
        return stringKey;
    }

    /**
     * Associates the specified UUID value with the specified key in this preference
     * node.
     *
     * @param key   key with which the specified value is to be associated.
     * @param value UUID value to be associated with the specified key.
     * @throws NullPointerException     if key or value is <tt>null</tt>.
     * @throws IllegalArgumentException if <tt>key.length()</tt> exceeds
     *                                  <tt>MAX_KEY_LENGTH</tt> or if <tt>value.length</tt> exceeds
     *                                  <tt>MAX_VALUE_LENGTH</tt>.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     */
    default void putUuid(Enum key, UUID value) {
        put(enumToGeneralKey(key), value.toString());
    }

    /**
     * Returns the UUID value associated with the specified key in this preference
     * node. Returns the specified default if there is no value associated with
     * the key, or the backing store is inaccessible.
     *
     * @param key
     * @param defaultValue
     * @return the value associated with <tt>key</tt>, or <tt>defaultValue</tt>
     * if no value is associated with <tt>key</tt>, or the backing store is
     * inaccessible.
     * @throws NullPointerException if key or defaultValue is <tt>null</tt>.
     */
    default UUID getUuid(Enum key, UUID defaultValue) {
        if (defaultValue != null) {
            String uuidStr = get(key, defaultValue.toString());
            return UUID.fromString(uuidStr);
        }
        throw new NullPointerException("Default value cannot be null");
    }

    /**
     * Retrieves the value associated with the specified enum key. If no value is found, returns the provided default value.
     *
     * @param key the enum key whose associated value is to be retrieved
     * @param defaultValue the value to return if no value is associated with the specified key
     * @return the value associated with the specified key, or the default value if no value is found
     */
    default String get(Enum key, String defaultValue) {
        return get(enumToGeneralKey(key), defaultValue);
    }

    /**
     * Returns the value associated with the specified key in this preference
     * node. Returns the specified default if there is no value associated with
     * the key, or the backing store is inaccessible.
     *
     * <p>
     * Some implementations may store default values in their backing stores. If
     * there is no value associated with the specified key but there is such a
     * <i>stored default</i>, it is returned in preference to the specified
     * default.
     *
     * @param key          key whose associated value is to be returned.
     * @param defaultValue the value to be returned in the event that this
     *                     preference node has no value associated with <tt>key</tt>.
     * @return the value associated with <tt>key</tt>, or <tt>defaultValue</tt>
     * if no value is associated with <tt>key</tt>, or the backing store is
     * inaccessible.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @throws NullPointerException  if <tt>key</tt> or <tt>defaultValue</tt> is
     *                               <tt>null</tt>.
     */
    String get(String key, String defaultValue);

    /**
     * Retrieves an Optional UUID from the given key.
     *
     * @param key the enumeration key used to retrieve the associated value
     * @return an Optional containing the UUID if present and successfully parsed; otherwise, an empty Optional
     */
    default Optional<UUID> getUuid(Enum key) {
        Optional<String> optionalString = get(key);
        if (optionalString.isPresent()) {
            return Optional.of(UUID.fromString(optionalString.get()));
        }
        return Optional.empty();
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the enum key used to retrieve the associated value
     * @return an Optional containing the value if found, or an empty Optional if no value is associated with the key
     */
    default Optional<String> get(Enum key) {
        return Optional.ofNullable(get(key, null));
    }

    /**
     * Removes the entry associated with the specified key.
     *
     * @param key the enumeration key whose associated entry is to be removed
     */
    default void remove(Enum key) {
        remove(enumToGeneralKey(key));
    }

    /**
     * Removes the value associated with the specified key in this preference
     * node, if any.
     *
     * <p>
     * If this implementation supports <i>stored defaults</i>, and there is such
     * a default for the specified preference, the stored default will be
     * "exposed" by this call, in the sense that it will be returned by a
     * succeeding call to <tt>get</tt>.
     *
     * @param key key whose mapping is to be removed from the preference node.
     * @throws NullPointerException  if <tt>key</tt> is <tt>null</tt>.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     */
    void remove(String key);

    /**
     * Removes all of the preferences (key-value associations) in this
     * preference node. This call has no effect on any descendants of this node.
     *
     * <p>
     * If this implementation supports <i>stored defaults</i>, and this node in
     * the preferences hierarchy contains any such defaults, the stored defaults
     * will be "exposed" by this call, in the sense that they will be returned
     * by succeeding calls to <tt>get</tt>.
     *
     * @throws BackingStoreException if this operation cannot be completed due
     *                               to a failure in the backing store, or inability to communicate with it.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @see #removeNode()
     */
    void clear() throws BackingStoreException;

    /**
     * Stores the given integer value associated with the specified key.
     *
     * @param key   the enumeration key to be used as the identifier for storing the value
     * @param value the integer value to be stored
     */
    default void putInt(Enum key, int value) {
        putInt(enumToGeneralKey(key), value);
    }

    /**
     * Associates a string representing the specified int value with the
     * specified key in this preference node. The associated string is the one
     * that would be returned if the int value were passed to
     * {@link Integer#toString(int)}. This method is intended for use in
     * conjunction with {@link #getInt}.
     *
     * @param key   key with which the string form of value is to be associated.
     * @param value value whose string form is to be associated with key.
     * @throws NullPointerException     if <tt>key</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException if <tt>key.length()</tt> exceeds
     *                                  <tt>MAX_KEY_LENGTH</tt>.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     * @see #getInt(String, int)
     */
    void putInt(String key, int value);

    /**
     * Retrieves an integer value associated with the specified Enum key,
     * returning a default value if no matching data is found.
     *
     * @param key the Enum key to retrieve the value for
     * @param value the default integer value to return if no data is found
     * @return the integer value associated with the Enum key or the default value if no data is found
     */
    default int getInt(Enum key, int value) {
        return getInt(enumToGeneralKey(key), value);
    }

    /**
     * Returns the int value represented by the string associated with the
     * specified key in this preference node. The string is converted to an
     * integer as by {@link Integer#parseInt(String)}. Returns the specified
     * default if there is no value associated with the key, the backing store
     * is inaccessible, or if
     * <tt>Integer.parseInt(String)</tt> would throw a {@link
     * NumberFormatException} if the associated value were passed. This method
     * is intended for use in conjunction with {@link #putInt}.
     *
     * <p>
     * If the implementation supports <i>stored defaults</i> and such a default
     * exists, is accessible, and could be converted to an int with
     * <tt>Integer.parseInt</tt>, this int is returned in preference to the
     * specified default.
     *
     * @param key          key whose associated value is to be returned as an int.
     * @param defaultValue the value to be returned in the event that this
     *                     preference node has no value associated with <tt>key</tt>
     *                     or the associated value cannot be interpreted as an int, or the backing
     *                     store is inaccessible.
     * @return the int value represented by the string associated with
     * <tt>key</tt> in this preference node, or <tt>defaultValue</tt> if the
     * associated value does not exist or cannot be interpreted as an int.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @throws NullPointerException  if <tt>key</tt> is <tt>null</tt>.
     * @see #putInt(String, int)
     * @see #get(String, String)
     */
    int getInt(String key, int defaultValue);

    /**
     * Retrieves an integer value associated with the specified key.
     * If the key exists and its value can be parsed as an integer, it returns an OptionalInt containing the integer value.
     * Otherwise, it returns an empty OptionalInt.
     *
     * @param key the key whose associated integer value is to be returned
     * @return an OptionalInt containing the integer value if present and parseable, or an empty OptionalInt if not
     */
    default OptionalInt getInt(String key) {
        Optional<String> optionalValue = get(key);
        if (optionalValue.isPresent()) {
            return OptionalInt.of(Integer.parseInt(optionalValue.get()));
        }
        return OptionalInt.empty();
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key used to retrieve the associated value
     * @return an {@link Optional} containing the value associated with the key,
     *         or an empty {@link Optional} if no value is found
     */
    default Optional<String> get(String key) {
        return Optional.ofNullable(get(key, null));
    }

    /**
     * Stores a long value associated with the specified Enum key.
     *
     * @param key the Enum key to be associated with the long value
     * @param value the long value to be stored
     */
    default void putLong(Enum key, long value) {
        putLong(enumToGeneralKey(key), value);
    }

    /**
     * Associates a string representing the specified long value with the
     * specified key in this preference node. The associated string is the one
     * that would be returned if the long value were passed to
     * {@link Long#toString(long)}. This method is intended for use in
     * conjunction with {@link #getLong}.
     *
     * @param key   key with which the string form of value is to be associated.
     * @param value value whose string form is to be associated with key.
     * @throws NullPointerException     if <tt>key</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException if <tt>key.length()</tt> exceeds
     *                                  <tt>MAX_KEY_LENGTH</tt>.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     * @see #getLong(String, long)
     */
    void putLong(String key, long value);

    /**
     * Returns the long value represented by the string associated with the
     * specified key in this preference node. The string is converted to a long
     * as by {@link Long#parseLong(String)}. Returns the specified default if
     * there is no value associated with the key, the backing store is
     * inaccessible, or if
     * <tt>Long.parseLong(String)</tt> would throw a {@link
     * NumberFormatException} if the associated value were passed. This method
     * is intended for use in conjunction with {@link #putLong}.
     *
     * <p>
     * If the implementation supports <i>stored defaults</i> and such a default
     * exists, is accessible, and could be converted to a long with
     * <tt>Long.parseLong</tt>, this long is returned in preference to the
     * specified default.
     *
     * @param key          key whose associated value is to be returned as a long.
     * @param defaultValue the value to be returned in the event that this
     *                     preference node has no value associated with <tt>key</tt>
     *                     or the associated value cannot be interpreted as a long, or the backing
     *                     store is inaccessible.
     * @return the long value represented by the string associated with
     * <tt>key</tt> in this preference node, or <tt>defaultValue</tt> if the
     * associated value does not exist or cannot be interpreted as a long.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @throws NullPointerException  if <tt>key</tt> is <tt>null</tt>.
     * @see #putLong(String, long)
     * @see #get(String, String)
     */
    long getLong(String key, long defaultValue);

    /**
     * Retrieves the value associated with the given key, parses it into a long, and returns it
     * wrapped in an {@code OptionalLong} if present.
     *
     * @param key the key whose associated value is to be retrieved
     * @return an {@code OptionalLong} containing the parsed long value if the key exists
     *         and the value can be successfully parsed; otherwise, an empty {@code OptionalLong}
     */
    default OptionalLong getLong(String key) {
        Optional<String> optionalValue = get(key);
        if (optionalValue.isPresent()) {
            return OptionalLong.of(Long.parseLong(optionalValue.get()));
        }
        return OptionalLong.empty();
    }

    /**
     * Stores a boolean value associated with the specified enum key.
     *
     * @param key the enum key to associate with the boolean value
     * @param value the boolean value to be stored
     */
    default void putBoolean(Enum key, boolean value) {
        putBoolean(enumToGeneralKey(key), value);
    }

    /**
     * Associates a string representing the specified boolean value with the
     * specified key in this preference node. The associated string is
     * <tt>"true"</tt> if the value is true, and <tt>"false"</tt> if it is
     * false. This method is intended for use in conjunction with
     * {@link #getBoolean}.
     *
     * @param key   key with which the string form of value is to be associated.
     * @param value value whose string form is to be associated with key.
     * @throws NullPointerException     if <tt>key</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException if <tt>key.length()</tt> exceeds
     *                                  <tt>MAX_KEY_LENGTH</tt>.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     * @see #getBoolean(String, boolean)
     * @see #get(String, String)
     */
    void putBoolean(String key, boolean value);

    /**
     * Retrieves a boolean value associated with the given enumeration key.
     * If the value cannot be found, the provided default value is returned.
     *
     * @param key the enumeration key used to identify the boolean value
     * @param defaultValue the value to return if no associated value is found
     * @return the boolean value associated with the key, or the default value if none is found
     */
    default boolean getBoolean(Enum key, boolean defaultValue) {
        return getBoolean(enumToGeneralKey(key), defaultValue);
    }

    /**
     * Returns the boolean value represented by the string associated with the
     * specified key in this preference node. Valid strings are <tt>"true"</tt>,
     * which represents true, and <tt>"false"</tt>, which represents false. Case
     * is ignored, so, for example, <tt>"TRUE"</tt>
     * and <tt>"False"</tt> are also valid. This method is intended for use in
     * conjunction with {@link #putBoolean}.
     *
     * <p>
     * Returns the specified default if there is no value associated with the
     * key, the backing store is inaccessible, or if the associated value is
     * something other than <tt>"true"</tt> or
     * <tt>"false"</tt>, ignoring case.
     *
     * <p>
     * If the implementation supports <i>stored defaults</i> and such a default
     * exists and is accessible, it is used in preference to the specified
     * default, unless the stored default is something other than
     * <tt>"true"</tt> or <tt>"false"</tt>, ignoring case, in which case the
     * specified default is used.
     *
     * @param key          key whose associated value is to be returned as a boolean.
     * @param defaultValue the value to be returned in the event that this
     *                     preference node has no value associated with <tt>key</tt>
     *                     or the associated value cannot be interpreted as a boolean, or the
     *                     backing store is inaccessible.
     * @return the boolean value represented by the string associated with
     * <tt>key</tt> in this preference node, or <tt>defaultValue</tt> if the
     * associated value does not exist or cannot be interpreted as a boolean.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @throws NullPointerException  if <tt>key</tt> is <tt>null</tt>.
     * @see #get(String, String)
     * @see #putBoolean(String, boolean)
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Retrieves an optional boolean value associated with the given enum key.
     *
     * @param key the enum key for which the boolean value is to be retrieved
     * @return an {@code Optional} containing the boolean value if present, or an empty {@code Optional} otherwise
     */
    default Optional<Boolean> getBoolean(Enum key) {
        return getBoolean(enumToGeneralKey(key));
    }

    /**
     * Retrieves the boolean value associated with the given key.
     * The method checks if the key exists, and if so, attempts
     * to parse its value as a boolean.
     *
     * @param key the key for which the boolean value is to be retrieved
     * @return an Optional containing the parsed boolean value if the key exists and is parsable, or an empty Optional if the key does not exist
     */
    default Optional<Boolean> getBoolean(String key) {
        Optional<String> optionalValue = get(key);
        if (optionalValue.isPresent()) {
            if (Boolean.parseBoolean(optionalValue.get())) {
                return Optional.of(true);
            }
            return Optional.of(false);
        }
        return Optional.empty();
    }

    /**
     * Stores a BigDecimal value associated with the specified Enum key.
     *
     * @param key   the Enum key used to associate with the BigDecimal value
     * @param value the BigDecimal value to store
     */
    default void putBigDecimal(Enum key, BigDecimal value) {
        putBigDecimal(enumToGeneralKey(key), value);
    }
    /**
     * Stores a BigDecimal value as a string representation associated with the specified key.
     *
     * @param key the key with which the specified BigDecimal value is to be associated
     * @param value the BigDecimal value to be stored, which will be converted to its string representation
     */
    default void putBigDecimal(String key, BigDecimal value) {
        put(key, value.toString());
    }
    /**
     * Retrieves a BigDecimal value associated with a given Enum key. If the key does not have
     * an associated value, returns the provided default BigDecimal value.
     *
     * @param key the Enum key used to identify the value
     * @param bigDecimal the default BigDecimal value to return if no value is found for the key
     * @return the BigDecimal value associated with the key, or the provided default value if none is found
     */
    default BigDecimal getBigDecimal(Enum key, BigDecimal bigDecimal) {
        return getBigDecimal(enumToGeneralKey(key), bigDecimal);
    }
    /**
     * Retrieves the BigDecimal value associated with the specified key. If the value
     * is not present, the provided default value is returned.
     *
     * @param key the key to retrieve the BigDecimal value associated with it
     * @param defaultValue the default BigDecimal value to return if the key is not present
     * @return the BigDecimal value associated with the key, or the default value if not present
     */
    default BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        Optional<BigDecimal> optionalValue = getBigDecimal(key);
        if (optionalValue.isPresent()) {
            return optionalValue.get();
        }
        return defaultValue;
    }

    /**
     * Retrieves the BigDecimal value associated with the key.
     *
     * @param key the key whose associated value is to be retrieved
     * @return an Optional containing the BigDecimal representation of the value if present,
     *         or an empty Optional if the value is absent
     */
    default Optional<BigDecimal> getBigDecimal(String key) {
        Optional<String> optionalString = get(key);
        if (optionalString.isPresent()) {
            return Optional.of(new BigDecimal(optionalString.get()));
        }
        return Optional.empty();
    }

    /**
     * Retrieves an Optional containing a BigDecimal value associated with the specified Enum key.
     *
     * @param key the Enum key used to identify and retrieve the associated BigDecimal value
     * @return an Optional containing the BigDecimal value if present, or an empty Optional if no value is found
     */
    default Optional<BigDecimal> getBigDecimal(Enum key) {
        return getBigDecimal(enumToGeneralKey(key));
    }

    /**
     * Associates the specified double value with the specified key in the underlying storage.
     *
     * @param key   the enumeration key to be used for storing the value
     * @param value the double value to be stored
     */
    default void putDouble(Enum key, double value) {
        putDouble(enumToGeneralKey(key), value);
    }

    /**
     * Associates a string representing the specified double value with the
     * specified key in this preference node. The associated string is the one
     * that would be returned if the double value were passed to
     * {@link Double#toString(double)}. This method is intended for use in
     * conjunction with {@link #getDouble}.
     *
     * @param key   key with which the string form of value is to be associated.
     * @param value value whose string form is to be associated with key.
     * @throws NullPointerException     if <tt>key</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException if <tt>key.length()</tt> exceeds
     *                                  <tt>MAX_KEY_LENGTH</tt>.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     * @see #getDouble(String, double)
     */
    void putDouble(String key, double value);

    /**
     * Retrieves a double value associated with the given Enum key. If no value is found, the specified default value is returned.
     *
     * @param key the Enum key used to retrieve the value
     * @param defaultValue the default value to return if no value is associated with the key
     * @return the double value associated with the key, or the defaultValue if no value is found
     */
    default double getDouble(Enum key, double defaultValue) {
        return getDouble(enumToGeneralKey(key), defaultValue);
    }

    /**
     * Returns the double value represented by the string associated with the
     * specified key in this preference node. The string is converted to an
     * integer as by {@link Double#parseDouble(String)}. Returns the specified
     * default if there is no value associated with the key, the backing store
     * is inaccessible, or if <tt>Double.parseDouble(String)</tt> would throw a
     * {@link NumberFormatException} if the associated value were passed. This
     * method is intended for use in conjunction with {@link #putDouble}.
     *
     * <p>
     * If the implementation supports <i>stored defaults</i> and such a default
     * exists, is accessible, and could be converted to a double with
     * <tt>Double.parseDouble</tt>, this double is returned in preference to the
     * specified default.
     *
     * @param key          key whose associated value is to be returned as a double.
     * @param defaultValue the value to be returned in the event that this
     *                     preference node has no value associated with <tt>key</tt>
     *                     or the associated value cannot be interpreted as a double, or the backing
     *                     store is inaccessible.
     * @return the double value represented by the string associated with
     * <tt>key</tt> in this preference node, or <tt>defaultValue</tt> if the
     * associated value does not exist or cannot be interpreted as a double.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @throws NullPointerException  if <tt>key</tt> is <tt>null</tt>.
     * @see #putDouble(String, double)
     * @see #get(String, String)
     */
    double getDouble(String key, double defaultValue);

    default OptionalDouble getDouble(String key) {
        Optional<String> optionalValue = get(key);
        if (optionalValue.isPresent()) {
            return OptionalDouble.of(Double.parseDouble(optionalValue.get()));
        }
        return OptionalDouble.empty();
    }

    /**
     * Stores a double array associated with the specified key.
     *
     * @param key the enumeration key used to identify the double array
     * @param array the double array to be stored
     */
    default void putDoubleArray(Enum key, double[] array) {
        putDoubleArray(enumToGeneralKey(key), array);
    }

    /**
     * Stores a double array in the underlying key-value store by converting
     * it to a list of string representations of the double values.
     *
     * @param key the key associated with the double array
     * @param array the array of double values to be stored
     */
    default void putDoubleArray(String key, double[] array) {
        List<String> doubleList = new ArrayList<>(array.length);
        for (double value : array) {
            doubleList.add(Double.toString(value));
        }
        putList(key, doubleList);
    }

    /**
     * Stores a list of strings associated with a given key, concatenating list elements
     * into a single string separated by the delimiter "|!%|".
     *
     * @param key the key to associate with the concatenated string
     * @param list the list of strings to be concatenated and stored
     */
    default void putList(String key, List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1) {
                builder.append("|!%|");
            }
        }
        put(key, builder.toString());
    }

    /**
     * Retrieves a double array associated with the specified Enum key.
     * If no value is associated with the key, returns the provided default array.
     *
     * @param key the Enum key used to retrieve the associated double array
     * @param defaultArray the default double array to return if no value is associated with the key
     * @return the double array associated with the specified key, or the provided default array if no value is found
     */
    default double[] getDoubleArray(Enum key, double[] defaultArray) {
        return getDoubleArray(enumToGeneralKey(key), defaultArray);
    }

    /**
     * Retrieves a double array associated with the specified key. If no such array exists,
     * the provided default array is returned.
     *
     * @param key the key for which the double array is to be retrieved
     * @param defaultArray the default double array to return if no value is found for the key
     * @return the double array associated with the key, or the default array if the key is not present
     */
    default double[] getDoubleArray(String key, double[] defaultArray) {
        Optional<double[]> optionalArray = getDoubleArray(key);
        if (optionalArray.isPresent()) {
            return optionalArray.get();
        }
        return defaultArray;
    }

    /**
     * Retrieves an optional array of doubles corresponding to the specified key.
     * The method attempts to fetch a list of strings associated with the key,
     * convert those strings to doubles, and return them as an array.
     *
     * @param key the key used to retrieve the list of strings
     * @return an {@code Optional} containing a double array if the list exists and
     *         conversion is successful, or an empty {@code Optional} if the list is not found
     *         or cannot be converted
     */
    default Optional<double[]> getDoubleArray(String key) {
        Optional<List<String>> optionalValue = getOptionalList(key);
        if (optionalValue.isPresent()) {
            List<String> listValue = optionalValue.get();
            double[] doubleArray = new double[listValue.size()];
            for (int i = 0; i < doubleArray.length; i++) {
                doubleArray[i] = Double.parseDouble(listValue.get(i));
            }
            return Optional.of(doubleArray);
        }
        return Optional.empty();
    }

    /**
     * Retrieves an optional list of strings based on the provided key.
     * If the value associated with the key is present, it returns an Optional containing the list.
     * Otherwise, it returns an empty Optional.
     *
     * @param key the key to retrieve the value and corresponding list
     * @return an Optional containing the list of strings if the value is present, otherwise an empty Optional
     */
    default Optional<List<String>> getOptionalList(String key) {
        Optional<String> value = get(key);
        if (value.isPresent()) {
            return Optional.of(getList(key));
        }
        return Optional.empty();
    }

    /**
     * Retrieves a list of strings by splitting the value associated with the specified key.
     * If no value is present for the key or the value is empty, an empty list is returned.
     *
     * @param key the key whose associated value is to be retrieved and processed
     * @return a list of strings obtained by splitting the value associated with the key,
     *         or an empty list if the key has no associated value or the value is empty
     */
    default List<String> getList(String key) {
        Optional<String> value = get(key);
        if (value.isPresent()) {
            String strValue = value.get();
            if (strValue.equals("")) {
                // nothing to do.
            } else {
                String[] elements = strValue.split("\\|!%\\|");
                return new ArrayList(Arrays.asList(elements));
            }
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves an optional double array associated with the specified enum key.
     *
     * @param key the enum key for which the double array is to be retrieved
     * @return an Optional containing the double array if it exists, or an empty Optional if it does not
     */
    default Optional<double[]> getDoubleArray(Enum key) {
        return getDoubleArray(enumToGeneralKey(key));
    }

    /**
     * Stores a byte array associated with the specified enum key.
     *
     * @param key the enumeration key to associate with the byte array
     * @param value the byte array to be stored
     */
    default void putByteArray(Enum key, byte[] value) {
        putByteArray(enumToGeneralKey(key), value);
    }

    /**
     * Associates a string representing the specified byte array with the
     * specified key in this preference node. The associated string is the
     * <i>Base64</i> encoding of the byte array, as defined in <a
     * href=http://www.ietf.org/rfc/rfc2045.txt>RFC 2045</a>, Section 6.8, with
     * one minor change: the string will consist solely of characters from the
     * <i>Base64 Alphabet</i>; it will not contain any newline characters. Note
     * that the maximum length of the byte array is limited to three quarters of
     * <tt>MAX_VALUE_LENGTH</tt> so that the length of the Base64 encoded String
     * does not exceed <tt>MAX_VALUE_LENGTH</tt>. This method is intended for
     * use in conjunction with {@link #getByteArray}.
     *
     * @param key   key with which the string form of value is to be associated.
     * @param value value whose string form is to be associated with key.
     * @throws NullPointerException     if key or value is <tt>null</tt>.
     * @throws IllegalArgumentException if key.length() exceeds MAX_KEY_LENGTH
     *                                  or if value.length exceeds MAX_VALUE_LENGTH*3/4.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     * @see #getByteArray(String, byte[])
     * @see #get(String, String)
     */
    void putByteArray(String key, byte[] value);

    /**
     * Retrieves an object associated with the specified key. If the key does not
     * exist or cannot be resolved, the provided default value will be returned.
     *
     * @param <T>          the type of the object to be retrieved
     * @param key          the unique identifier for the object
     * @param defaultValue the default object to return if the key is not present
     *                      or the associated value cannot be resolved
     * @return the object corresponding to the key if found, otherwise the
     *         provided default value
     */
    default <T extends Object> T getObject(UUID key, T defaultValue) {
        return getObject(key.toString(), defaultValue);
    }

    /**
     * Retrieves an object associated with the given key. If the key does not
     * exist or if decoding fails, the provided default value is returned.
     *
     * @param <T> the type of the object to be retrieved
     * @param key the key used to locate the object in the storage
     * @param defaultValue the value to return if the key is not found or if the object cannot be decoded
     * @return the retrieved object associated with the key, or the default value if the object is not found or decoding fails
     */
    default <T extends Object> T getObject(String key, T defaultValue) {
        Optional<byte[]> optionalBytes = getByteArray(key);
        if (optionalBytes.isPresent()) {
            return Encodable.decode(optionalBytes.get());
        }
        return defaultValue;
    }

    /**
     * Retrieves a byte array associated with the given key.
     *
     * @param key the key used to retrieve the associated byte array
     * @return an Optional containing the byte array if present, otherwise an empty Optional
     */
    default Optional<byte[]> getByteArray(String key) {
        Optional<String> optionalValue = get(key);
        if (optionalValue.isPresent()) {
            return Optional.of(getByteArray(key, new byte[0]));
        }
        return Optional.empty();
    }

    /**
     * Returns the byte array value represented by the string associated with
     * the specified key in this preference node. Valid strings are
     * <i>Base64</i> encoded binary data, as defined in <a
     * href=http://www.ietf.org/rfc/rfc2045.txt>RFC 2045</a>, Section 6.8, with
     * one minor change: the string must consist solely of characters from the
     * <i>Base64 Alphabet</i>; no newline characters or extraneous characters
     * are permitted. This method is intended for use in conjunction with
     * {@link #putByteArray}.
     *
     * <p>
     * Returns the specified default if there is no value associated with the
     * key, the backing store is inaccessible, or if the associated value is not
     * a valid Base64 encoded byte array (as defined above).
     *
     * <p>
     * If the implementation supports <i>stored defaults</i> and such a default
     * exists and is accessible, it is used in preference to the specified
     * default, unless the stored default is not a valid Base64 encoded byte
     * array (as defined above), in which case the specified default is used.
     *
     * @param key          key whose associated value is to be returned as a byte array.
     * @param defaultValue the value to be returned in the event that this
     *                     preference node has no value associated with <tt>key</tt>
     *                     or the associated value cannot be interpreted as a byte array, or the
     *                     backing store is inaccessible.
     * @return the byte array value represented by the string associated with
     * <tt>key</tt> in this preference node, or <tt>defaultValue</tt> if the
     * associated value does not exist or cannot be interpreted as a byte array.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @throws NullPointerException  if <tt>key</tt> or <tt>defaultValue</tt> is
     *                               <tt>null</tt>.
     * @see #get(String, String)
     * @see #putByteArray(String, byte[])
     */
    byte[] getByteArray(String key, byte[] defaultValue);

    /**
     * Retrieves an object associated with the specified key. If no object is found,
     * returns the provided default value.
     *
     * @param <T>          the type of the object to be retrieved
     * @param key          the enumeration key used to retrieve the object
     * @param defaultValue the default value to be returned if an object is not found
     * @return the object associated with the specified key, or the default value if none exists
     */
    default <T extends Object> T getObject(Enum key, T defaultValue) {
        Optional<byte[]> optionalBytes = getByteArray(key);
        if (optionalBytes.isPresent()) {
            return Encodable.decode(optionalBytes.get());
        }
        return defaultValue;
    }

    /**
     * Retrieves a byte array associated with the specified Enum key.
     *
     * @param key the Enum key used to access the corresponding byte array
     * @return an Optional containing the byte array if present, otherwise an empty Optional
     */
    default Optional<byte[]> getByteArray(Enum key) {
        return getByteArray(enumToGeneralKey(key));
    }

    /**
     * Retrieves an object by decoding a byte array associated with the specified key.
     *
     * @param <T> the type of the decoded object
     * @param key the key used for lookup to retrieve and decode the byte array
     * @return an Optional containing the decoded object, or an empty Optional if the byte array is not present or decoding fails
     */
    default <T extends Object> Optional<T> getObject(Enum key) {
        Optional<byte[]> optionalBytes = getByteArray(key);
        if (optionalBytes.isPresent()) {
            return Optional.ofNullable(Encodable.decode(optionalBytes.get()));
        }
        return Optional.empty();
    }

    /**
     * Retrieves an object of a specified type associated with the given key.
     *
     * @param <T> the type of the object to be retrieved
     * @param key the UUID associated with the object
     * @return an Optional containing the object if found, or an empty Optional if not found
     */
    default <T extends Object> Optional<T> getObject(UUID key) {
        return getObject(key.toString());
    }

    /**
     * Retrieves an object associated with a given key.
     *
     * This method attempts to retrieve a serialized object in the form of a byte array
     * using the specified key. If the byte array is present, it decodes the byte array
     * into an object of the specified type. If no associated data is found, an empty
     * Optional is returned.
     *
     * @param <T> the type of the object to be retrieved
     * @param key the identifier associated with the object
     * @return an Optional containing the decoded object if present, otherwise an empty Optional
     */
    default <T extends Object> Optional<T> getObject(String key) {
        Optional<byte[]> optionalBytes = getByteArray(key);
        if (optionalBytes.isPresent()) {
            return Optional.ofNullable(Encodable.decode(optionalBytes.get()));
        }
        return Optional.empty();
    }

    /**
     * Stores the given Encodable object associated with the specified Enum key.
     *
     * @param key the Enum key used to associate the Encodable object
     * @param encodable the Encodable object to be stored
     */
    default void putObject(Enum key, Encodable encodable) {
        putObject(enumToGeneralKey(key), encodable);
    }

    /**
     * Stores an object represented by an Encodable instance into a data structure using the specified key.
     *
     * @param key The unique string key associated with the object.
     * @param encodable The Encodable instance representing the object to be stored, which will be converted to a byte array.
     */
    default void putObject(String key, Encodable encodable) {
        putByteArray(key, encodable.toBytes());
    }

    /**
     * Puts an object into a storage system using the provided UUID as the key.
     *
     * @param key the unique identifier of the object to be stored
     * @param encodable the object to be stored, which must implement the Encodable interface
     */
    default void putObject(UUID key, Encodable encodable) {
        putObject(key.toString(), encodable);
    }

    /**
     * Retrieves a byte array associated with the given key. If no value is associated
     * with the key, returns the provided default value.
     *
     * @param key the Enum key to identify the byte array
     * @param defaultValue the default byte array to return if no value is associated with the key
     * @return the byte array associated with the given key, or the default value if no value is found
     */
    default byte[] getByteArray(Enum key, byte[] defaultValue) {
        return getByteArray(enumToGeneralKey(key), defaultValue);
    }

    /**
     * Retrieves all child nodes of the current preferences node as an array of KometPreferences.
     *
     * @return an array of KometPreferences representing the child nodes of the current preferences node.
     * @throws BackingStoreException if there is a failure accessing the backing store for the preferences.
     */
    default KometPreferences[] children() throws BackingStoreException {
        String[] childrenNames = childrenNames();
        KometPreferences[] children = new KometPreferences[childrenNames.length];
        for (int i = 0; i < childrenNames.length; i++) {
            children[i] = node(childrenNames[i]);
        }
        return children;
    }

    /**
     * Returns the names of the children of this preference node, relative to
     * this node. (The returned array will be of size zero if this node has no
     * children.)
     *
     * @return the names of the children of this preference node.
     * @throws BackingStoreException if this operation cannot be completed due
     *                               to a failure in the backing store, or inability to communicate with it.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     */
    String[] childrenNames() throws BackingStoreException;

    /**
     * Returns the named preference node in the same tree as this node, creating
     * it and any of its ancestors if they do not already exist. Accepts a
     * relative or absolute path name. Relative path names (which do not begin
     * with the slash character <tt>('/')</tt>) are interpreted relative to this
     * preference node.
     *
     * <p>
     * If the returned node did not exist prior to this call, this node and any
     * ancestors that were created by this call are not guaranteed to become
     * permanent until the <tt>flush</tt> method is called on the returned node
     * (or one of its ancestors or descendants).
     *
     * @param pathName the path name of the preference node to return.
     * @return the specified preference node.
     * @throws IllegalArgumentException if the path name is invalid (i.e., it
     *                                  contains multiple consecutive slash characters, or ends with a slash
     *                                  character and is more than one character long).
     * @throws NullPointerException     if path name is <tt>null</tt>.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     * @see #flush()
     */
    KometPreferences node(String pathName);

    /**
     * Checks if the current node contains any child nodes.
     *
     * @return true if the current node has one or more child nodes, false otherwise
     * @throws BackingStoreException if a failure occurs while attempting to retrieve child nodes
     */
    default boolean hasChildren() throws BackingStoreException {
        return childrenNames().length != 0;
    }

    /**
     * Returns the parent of this preference node, or <tt>null</tt> if this is
     * the root.
     *
     * @return the parent of this preference node.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     */
    KometPreferences parent();

    /**
     * Retrieves the preferences node associated with the specified class.
     *
     * @param c the class for which to retrieve the preferences node
     * @return the preferences node associated with the specified class
     */
    default KometPreferences node(Class<?> c) {
        return node(nodeName(c));
    }

    /**
     * Generates a preferences node name based on the package name of the specified class.
     * If the class belongs to an array, an {@link IllegalArgumentException} is thrown.
     * The node name is constructed by replacing dots in the package name with slashes,
     * prefixed by a slash. If the specified class does not belong to any package,
     * returns "/<unnamed>" as the node name.
     *
     * @param c the class to generate the preferences node name for
     * @return the preferences node name based on the class's package name
     * @throws IllegalArgumentException if the specified class is an array
     */
    static String nodeName(Class<?> c) {
        if (c.isArray()) {
            throw new IllegalArgumentException(
                    "Arrays have no associated preferences node.");
        }
        String className = c.getName();
        int pkgEndIndex = className.lastIndexOf('.');
        if (pkgEndIndex < 0) {
            return "/<unnamed>";
        }
        String packageName = className.substring(0, pkgEndIndex);
        return "/" + packageName.replace('.', '/');
    }

    /**
     * Checks if a preferences node associated with the specified class exists.
     *
     * @param c the class for which the preferences node is to be checked
     * @return true if the preferences node exists, false otherwise
     * @throws BackingStoreException if an error occurs while attempting to check
     *         the existence of the preferences node
     */
    default boolean nodeExists(Class<?> c) throws BackingStoreException {
        return nodeExists(nodeName(c));
    }

    /**
     * Returns true if the named preference node exists in the same tree as this
     * node. Relative path names (which do not begin with the slash character
     * <tt>('/')</tt>) are interpreted relative to this preference node.
     *
     * <p>
     * If this node (or an ancestor) has already been removed with the
     * {@link #removeNode()} method, it <i>is</i> legal to invoke this method,
     * but only with the path name <tt>""</tt>; the invocation will return
     * <tt>false</tt>. Thus, the idiom <tt>p.nodeExists("")</tt> may be used to
     * test whether <tt>p</tt> has been removed.
     *
     * @param pathName the path name of the node whose existence is to be
     *                 checked.
     * @return true if the specified node exists.
     * @throws BackingStoreException    if this operation cannot be completed due
     *                                  to a failure in the backing store, or inability to communicate with it.
     * @throws IllegalArgumentException if the path name is invalid (i.e., it
     *                                  contains multiple consecutive slash characters, or ends with a slash
     *                                  character and is more than one character long).
     * @throws NullPointerException     if path name is <tt>null</tt>.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method and
     *                                  <tt>pathName</tt> is not the empty string (<tt>""</tt>).
     */
    boolean nodeExists(String pathName)
            throws BackingStoreException;

    /**
     * Removes this preference node and all of its descendants, invalidating any
     * preferences contained in the removed nodes. Once a node has been removed,
     * attempting any method other than {@link #name()},
     * {@link #absolutePath()}, {@link #flush()} or
     * {@link #node(String) nodeExists("")} on the corresponding
     * <tt>Preferences</tt> instance will fail with an
     * <tt>IllegalStateException</tt>. (The methods defined on {@link Object}
     * can still be invoked on a node after it has been removed; they will not
     * throw <tt>IllegalStateException</tt>.)
     *
     * <p>
     * The removal is not guaranteed to be persistent until the
     * <tt>flush</tt> method is called on this node (or an ancestor).
     *
     * <p>
     * If this implementation supports <i>stored defaults</i>, removing a node
     * exposes any stored defaults at or below this node. Thus, a subsequent
     * call to <tt>nodeExists</tt> on this node's path name may return
     * <tt>true</tt>, and a subsequent call to <tt>node</tt> on this path name
     * may return a (different) <tt>Preferences</tt> instance representing a
     * non-empty collection of preferences and/or children.
     *
     * @throws BackingStoreException         if this operation cannot be completed due
     *                                       to a failure in the backing store, or inability to communicate with it.
     * @throws IllegalStateException         if this node (or an ancestor) has already
     *                                       been removed with the {@link #removeNode()} method.
     * @throws UnsupportedOperationException if this method is invoked on the
     *                                       root node.
     * @see #flush()
     */
    void removeNode() throws BackingStoreException;

    /**
     * Returns this preference node's name, relative to its parent.
     *
     * @return this preference node's name, relative to its parent.
     */
    String name();

    /**
     * Returns this preference node's absolute path name.
     *
     * @return this preference node's absolute path name.
     */
    String absolutePath();

    /**
     * @return the preference node type. CONFIGURATION, USER, or SYSTEM.
     */
    PreferenceNodeType getNodeType();

    /**
     * Forces any changes in the contents of this preference node and its
     * descendants to the persistent store. Once this method returns
     * successfully, it is safe to assume that all changes made in the subtree
     * rooted at this node prior to the method invocation have become permanent.
     *
     * <p>
     * Implementations are free to flush changes into the persistent store at
     * any time. They do not need to wait for this method to be called.
     *
     * <p>
     * When a flush occurs on a newly created node, it is made persistent, as
     * are any ancestors (and descendants) that have yet to be made persistent.
     * Note however that any preference value changes in ancestors are
     * <i>not</i> guaranteed to be made persistent.
     *
     * <p>
     * If this method is invoked on a node that has been removed with the
     * {@link #removeNode()} method, flushSpi() is invoked on this node, but not
     * on others.
     *
     * @throws BackingStoreException if this operation cannot be completed due
     *                               to a failure in the backing store, or inability to communicate with it.
     * @see #sync()
     */
    void flush() throws BackingStoreException;

    /**
     * Ensures that future reads from this preference node and its descendants
     * reflect any changes that were committed to the persistent store (from any
     * VM) prior to the <tt>sync</tt> invocation. As a side-effect, forces any
     * changes in the contents of this preference node and its descendants to
     * the persistent store, as if the <tt>flush</tt>
     * method had been invoked on this node.
     *
     * @throws BackingStoreException if this operation cannot be completed due
     *                               to a failure in the backing store, or inability to communicate with it.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @see #flush()
     */
    void sync() throws BackingStoreException;

    /**
     * Registers the specified listener to receive <i>preference change
     * events</i> for this preference node. A preference change event is
     * generated when a preference is added to this node, removed from this
     * node, or when the value associated with a preference is changed.
     * (Preference change events are <i>not</i> generated by the {@link
     * #removeNode()} method, which generates a <i>node change event</i>.
     * Preference change events <i>are</i> generated by the <tt>clear</tt>
     * method.)
     *
     * <p>
     * Events are only guaranteed for changes made within the same JVM as the
     * registered listener, though some implementations may generate events for
     * changes made outside this JVM. Events may be generated before the changes
     * have been made persistent. Events are not generated when preferences are
     * modified in descendants of this node; a caller desiring such events must
     * register with each descendant.
     *
     * @param pcl The preference change listener to add.
     * @throws NullPointerException  if <tt>pcl</tt> is null.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @see #removePreferenceChangeListener(PreferenceChangeListener)
     * @see #addNodeChangeListener(NodeChangeListener)
     */
    void addPreferenceChangeListener(
            PreferenceChangeListener pcl);

    /**
     * Removes the specified preference change listener, so it no longer
     * receives preference change events.
     *
     * @param pcl The preference change listener to remove.
     * @throws IllegalArgumentException if <tt>pcl</tt> was not a registered
     *                                  preference change listener on this node.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     * @see #addPreferenceChangeListener(PreferenceChangeListener)
     */
    void removePreferenceChangeListener(
            PreferenceChangeListener pcl);

    /**
     * Registers the specified listener to receive <i>node change events</i>
     * for this node. A node change event is generated when a child node is
     * added to or removed from this node. (A single {@link #removeNode()}
     * invocation results in multiple <i>node change events</i>, one for every
     * node in the subtree rooted at the removed node.)
     *
     * <p>
     * Events are only guaranteed for changes made within the same JVM as the
     * registered listener, though some implementations may generate events for
     * changes made outside this JVM. Events may be generated before the changes
     * have become permanent. Events are not generated when indirect descendants
     * of this node are added or removed; a caller desiring such events must
     * register with each descendant.
     *
     * <p>
     * Few guarantees can be made regarding node creation. Because nodes are
     * created implicitly upon access, it may not be feasible for an
     * implementation to determine whether a child node existed in the backing
     * store prior to access (for example, because the backing store is
     * unreachable or cached information is out of date). Under these
     * circumstances, implementations are neither required to generate node
     * change events nor prohibited from doing so.
     *
     * @param ncl The <tt>NodeChangeListener</tt> to add.
     * @throws NullPointerException  if <tt>ncl</tt> is null.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @see #removeNodeChangeListener(NodeChangeListener)
     * @see #addPreferenceChangeListener(PreferenceChangeListener)
     */
    void addNodeChangeListener(NodeChangeListener ncl);

    /**
     * Removes the specified <tt>NodeChangeListener</tt>, so it no longer
     * receives change events.
     *
     * @param ncl The <tt>NodeChangeListener</tt> to remove.
     * @throws IllegalArgumentException if <tt>ncl</tt> was not a registered
     *                                  <tt>NodeChangeListener</tt> on this node.
     * @throws IllegalStateException    if this node (or an ancestor) has been
     *                                  removed with the {@link #removeNode()} method.
     * @see #addNodeChangeListener(NodeChangeListener)
     */
    void removeNodeChangeListener(NodeChangeListener ncl);

    /**
     * Emits on the specified output stream an XML document representing all of
     * the preferences contained in this node (but not its descendants). This
     * XML document is, in effect, an offline backup of the node.
     *
     * <p>
     * The XML document will have the following DOCTYPE declaration:
     * <pre>{@code
     * <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
     * }</pre> The UTF-8 character encoding will be used.
     *
     * <p>
     * This method is an exception to the general rule that the results of
     * concurrently executing multiple methods in this class yields results
     * equivalent to some serial execution. If the preferences at this node are
     * modified concurrently with an invocation of this method, the exported
     * preferences comprise a "fuzzy snapshot" of the preferences contained in
     * the node; some of the concurrent modifications may be reflected in the
     * exported data while others may not.
     *
     * @param os the output stream on which to emit the XML document.
     * @throws IOException           if writing to the specified output stream results in
     *                               an <tt>IOException</tt>.
     * @throws BackingStoreException if preference data cannot be read from
     *                               backing store.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     */
    void exportNode(OutputStream os)
            throws IOException, BackingStoreException;

    /**
     * Emits an XML document representing all of the preferences contained in
     * this node and all of its descendants. This XML document is, in effect, an
     * offline backup of the subtree rooted at the node.
     *
     * <p>
     * The XML document will have the following DOCTYPE declaration:
     * <pre>{@code
     * <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
     * }</pre> The UTF-8 character encoding will be used.
     *
     * <p>
     * This method is an exception to the general rule that the results of
     * concurrently executing multiple methods in this class yields results
     * equivalent to some serial execution. If the preferences or nodes in the
     * subtree rooted at this node are modified concurrently with an invocation
     * of this method, the exported preferences comprise a "fuzzy snapshot" of
     * the subtree; some of the concurrent modifications may be reflected in the
     * exported data while others may not.
     *
     * @param os the output stream on which to emit the XML document.
     * @throws IOException           if writing to the specified output stream results in
     *                               an <tt>IOException</tt>.
     * @throws BackingStoreException if preference data cannot be read from
     *                               backing store.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     * @see #exportNode(OutputStream)
     */
    void exportSubtree(OutputStream os)
            throws IOException, BackingStoreException;

    /**
     * For single value enumerated preferences... i.e. the enumeration would be
     * used only once, for one type of preferences. In such cases, the class
     * name of the enumeration itself can serve as the key for the preference
     * value associated with the preference.
     *
     * @param <T>
     * @param defaultValue
     * @return the enumerated preference value.
     */
    default <T extends Enum<T>> T getEnum(T defaultValue) {
        String key = defaultValue.getClass().getCanonicalName();
        String value = get(key, defaultValue.name());
        return (T) Enum.valueOf(defaultValue.getClass(), value);
    }

    /**
     * Retrieves an enum constant of the specified type from a storage based on its canonical name.
     *
     * @param <T>       the type of the enum.
     * @param enumClass the class object of the enum type.
     * @return an {@code Optional} containing the enum constant if present, otherwise an empty {@code Optional}.
     */
    default <T extends Enum<T>> Optional<T> getEnum(Class<T> enumClass) {
        String key = enumClass.getCanonicalName();
        Optional<String> value = get(key);
        if (value.isPresent()) {
            return Optional.of(Enum.valueOf(enumClass, value.get()));
        }
        return Optional.empty();
    }

    /**
     * Stores the provided enum value in a key-value pair format. The key is generated using the canonical name
     * of the enum's class, and the value is the name of the enum constant.
     *
     * @param value the enum value to be stored; must not be null
     */
    default void putEnum(Enum value) {
        String key = value.getClass().getCanonicalName();
        put(key, value.name());
    }

    /**
     * Checks if a specific key, represented by the given enum, is present.
     *
     * @param enumDefault the enum representing the key to check
     * @return true if the key is present, false otherwise
     */
    default boolean hasKey(Enum enumDefault) {
        return get(enumToGeneralKey(enumDefault)).isPresent();
    }

    /**
     * Checks if the specified key exists in the underlying structure.
     *
     * @param key the key to be checked for presence
     * @return true if the key exists, false otherwise
     */
    default boolean hasKey(String key) {
        return get(key).isPresent();
    }

    /**
     * Associates the specified array of strings with the specified key in the form of a list.
     *
     * @param key the key with which the specified array is to be associated
     * @param array the array of strings to be associated with the specified key
     */
    default void putArray(Enum key, String[] array) {
        putList(key, Arrays.asList(array));
    }

    /**
     * Adds a list of strings to a storage mechanism identified by the given key.
     *
     * @param key  the enumeration key used to identify where the list will be stored
     * @param list the list of strings to be stored
     */
    default void putList(Enum key, List<String> list) {
        putList(enumToGeneralKey(key), list);
    }

    /**
     * Stores an array of strings associated with a specific key by converting the array into a list.
     *
     * @param key The unique identifier to associate with the array.
     * @param array The array of strings to be stored.
     */
    default void putArray(String key, String[] array) {
        putList(key, Arrays.asList(array));
    }

    /**
     * Retrieves an array of Strings derived from a specified Enum key.
     *
     * @param key the Enum key used to retrieve the associated data
     * @return an array of Strings corresponding to the given Enum key
     */
    default String[] getArray(Enum key) {
        return getList(enumToGeneralKey(key)).toArray(new String[2]);
    }

    /**
     * Retrieves an array of strings corresponding to the specified key.
     *
     * @param key the key used to retrieve the associated list of strings
     * @return an array of strings derived from the list associated with the specified key
     */
    default String[] getArray(String key) {
        return getList(key).toArray(new String[2]);
    }

    /**
     * Adds a list of ConceptFacade objects associated with the specified key to the underlying data structure.
     *
     * @param key the enumeration key used to identify the list of ConceptFacade objects
     * @param list the list of ConceptFacade objects to be added
     */
    default void putConceptList(Enum key, List<? extends ConceptFacade> list) {
        putConceptList(enumToGeneralKey(key), list);
    }

    /**
     * Stores a list of ConceptFacade objects in a delimited string format associated with a specified key.
     *
     * @param key the key used to associate the delimited string representation of the concept list
     *            in the underlying storage mechanism.
     * @param list the list of ConceptFacade objects to be stored. The objects are serialized into
     *             XML fragments and concatenated using a predefined delimiter.
     */
    default void putConceptList(String key, List<? extends ConceptFacade> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i).toXmlFragment());
            if (i < list.size() - 1) {
                builder.append("|!%|");
            }
        }
        put(key, builder.toString());
    }

    /**
     * Associates a collection of EntityFacade components with a specified key in the underlying storage.
     *
     * @param key an Enum representing the key used for storing the component list
     * @param list a collection of EntityFacade objects to be stored under the specified key
     */
    default void putComponentList(Enum key, Collection<? extends EntityFacade> list) {
        putComponentList(enumToGeneralKey(key), list);
    }

    /**
     * Stores a list of components into a specified key in a serialized format. Each component in the
     * list is converted to its XML fragment representation and joined with a specific delimiter.
     *
     * @param key The key under which the serialized list of components will be stored.
     * @param list A collection of EntityFacade objects to be serialized and stored.
     */
    default void putComponentList(String key, Collection<? extends EntityFacade> list) {
        StringBuilder builder = new StringBuilder();
        Iterator<? extends EntityFacade> itr = list.iterator();
        while (itr.hasNext()) {
            EntityFacade entityFacade = itr.next();
            builder.append(entityFacade.toXmlFragment());
            if (itr.hasNext()) {
                builder.append("|!%|");
            }
        }
        put(key, builder.toString());
    }

    /**
     * Retrieves a list of entities based on the provided key or returns the default list if no entities are found.
     *
     * @param <T>          the type parameter extending EntityFacade representing the entity type
     * @param key          the key used to identify the desired entity list
     * @param defaultList  the default list to return if no entities are found for the given key
     * @return a list of entities of the specified type
     */
    default <T extends EntityFacade> List<T> getEntityList(Enum key, T[] defaultList) {
        return getEntityList(enumToGeneralKey(key), defaultList);
    }

    /**
     * Retrieves a list of entity facades based on the provided key. If a list is available for the key,
     * it converts the items in the list from XML fragments to entity facades. If no list is available,
     * it returns the default list provided.
     *
     * @param <T> the type of entity facade
     * @param key the key used to retrieve the optional list
     * @param defaultList the default list of entity facades to return if no list is found for the key
     * @return a list of entity facades, either retrieved and converted based on the key or the default list
     */
    default <T extends EntityFacade> List<T> getEntityList(String key, T[] defaultList) {
        Optional<List<String>> optionalList = getOptionalList(key);
        List<T> componentList = new ArrayList<>(defaultList.length);
        optionalList.ifPresentOrElse(stringList -> {
            for (String entityFacadeXml : stringList) {
                componentList.add((T) ProxyFactory.fromXmlFragment(entityFacadeXml));
            }
        }, () -> {
            for (EntityFacade entityFacade : defaultList) {
                componentList.add((T) entityFacade);
            }
        });
        return componentList;
    }

    /**
     * Retrieves a list of EntityFacade objects based on the provided Enum key.
     *
     * @param key the enumeration key used to determine which entities to retrieve
     * @return a list of EntityFacade objects corresponding to the specified key
     */
    default List<EntityFacade> getEntityList(Enum key) {
        return getEntityList(enumToGeneralKey(key));
    }

    /**
     * Retrieves a list of EntityFacade objects associated with the given key.
     *
     * @param key the key used to fetch the list of associated entity XML fragments
     * @return a list of EntityFacade objects parsed and constructed from the XML fragments
     */
    default List<EntityFacade> getEntityList(String key) {
        List<String> list = getList(key);
        List<EntityFacade> proxyList = new ArrayList<>(list.size());
        for (String proxyString : list) {
            ProxyFactory.fromXmlFragmentOptional(proxyString).ifPresent(entityFacade -> proxyList.add(entityFacade));
        }
        return proxyList;
    }

    /**
     * Retrieves a list of strings associated with a specified enumeration key.
     *
     * @param key the enumeration key used to retrieve the corresponding list
     * @return a list of strings mapped to the given enumeration key
     */
    default List<String> getList(Enum key) {
        return getList(enumToGeneralKey(key));
    }

    /**
     * Retrieves a list of strings associated with the specified key. If the list is empty,
     * the provided default list is returned instead.
     *
     * @param key the enumeration key used to retrieve the associated list
     * @param defaultList the default list to return if the retrieved list is empty
     * @return the list of strings corresponding to the specified key, or the default list if the retrieved list is empty
     */
    default List<String> getList(Enum key, List<String> defaultList) {
        List<String> list = getList(enumToGeneralKey(key));
        if (list.isEmpty()) {
            return defaultList;
        }
        return list;
    }

    /**
     * Retrieves a list of concept entities associated with a specified enum key.
     *
     * @param key the enum key used to retrieve the associated list of concept entities
     * @return a list of EntityProxy.Concept objects corresponding to the provided key
     */
    default List<EntityProxy.Concept> getConceptList(Enum key) {
        return getConceptList(enumToGeneralKey(key));
    }

    /**
     * Retrieves a list of concept proxies associated with the specified key.
     *
     * @param key the key used to retrieve the associated list of concept proxies
     * @return a list of EntityProxy.Concept objects corresponding to the given key
     */
    default List<EntityProxy.Concept> getConceptList(String key) {
        List<String> list = getList(key);
        List<EntityProxy.Concept> proxyList = new ArrayList<>(list.size());
        for (String proxyString : list) {
            ProxyFactory.fromXmlFragmentOptional(proxyString).ifPresent(entityFacade -> proxyList.add((EntityProxy.Concept) entityFacade));
        }
        return proxyList;
    }

    /**
     * Retrieves a list of PatternFacade objects associated with the given key.
     * If no patterns are found, an empty list is returned.
     *
     * @param key an Enum representing the key used to retrieve the patterns
     * @return a list of PatternFacade objects
     */
    default List<PatternFacade> getPatternList(Enum key) {
        return getPatternList(key, new ArrayList<>());
    }

    /**
     * Retrieves a list of PatternFacade objects associated with the specified key.
     * If no such list is present, returns a copy of the provided default list.
     *
     * @param key the enum key used to retrieve the list
     * @param defaultList the default list of PatternFacade objects to return if no list is found
     * @return a list of PatternFacade objects, either from the stored data or a copy of the provided default list
     */
    default List<PatternFacade> getPatternList(Enum key, List<PatternFacade> defaultList) {
        Optional<List<PatternFacade>> optionalList = getOptionalPatternList(enumToGeneralKey(key));
        if (optionalList.isEmpty()) {
            List<PatternFacade> proxyList = new ArrayList<>(defaultList.size());
            for (PatternFacade patternFacade : defaultList) {
                proxyList.add(patternFacade);
            }
            return proxyList;
        }
        return optionalList.get();
    }

    /**
     * Retrieves an optional list of {@link PatternFacade} objects based on the provided key.
     * The method converts a list of string representations into a list of {@link PatternFacade} objects
     * using the {@link ProxyFactory#fromXmlFragmentOptional(String)} method. If the key does not map to
     * any list or if the list is empty, an empty {@link Optional} is returned.
     *
     * @param key the key used to retrieve the optional list of string representations
     *            from an underlying data source
     * @return an {@link Optional} containing a list of {@link PatternFacade} objects if present,
     *         or an empty {@link Optional} if no data is available or conversion fails
     */
    default Optional<List<PatternFacade>> getOptionalPatternList(String key) {
        Optional<List<String>> optionalList = getOptionalList(key);
        if (optionalList.isPresent()) {
            List<String> list = optionalList.get();
            List<PatternFacade> proxyList = new ArrayList<>(list.size());
            for (String proxyString : list) {
                ProxyFactory.fromXmlFragmentOptional(proxyString).ifPresent(entityFacade -> proxyList.add((EntityProxy.Pattern) entityFacade));
            }
            return Optional.of(proxyList);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a list of PatternFacade objects based on the specified key.
     *
     * @param key the enumeration key used to identify the list of patterns.
     * @param defaultList an array of PatternFacade objects to be used as the default list.
     * @return a List of PatternFacade objects associated with the specified key, or the provided default list if no specific list is found.
     */
    default List<PatternFacade> getPatternList(Enum key, PatternFacade[] defaultList) {
        return getPatternList(key, Arrays.asList(defaultList));
    }

    /**
     * Retrieves a list of ConceptFacade objects associated with the specified key. If no
     * associated list exists for the given key, the provided default list is returned.
     *
     * @param key the key used to retrieve the associated list of ConceptFacade objects
     * @param defaultList the default array of ConceptFacade objects to be returned if no
     *                    list is associated with the provided key
     * @return a list of ConceptFacade objects associated with the key, or the default list
     *         if no such list is found
     */
    default List<ConceptFacade> getConceptList(Enum key, ConceptFacade[] defaultList) {
        return getConceptList(key, Arrays.asList(defaultList));
    }

    /**
     * Retrieves a list of ConceptFacade objects associated with the specified key.
     * If no list is found, the provided default list is returned as a copy.
     *
     * @param key the enumeration key used to retrieve the concept list
     * @param defaultList the default list of ConceptFacade objects to be used if no associated list is found
     * @return a list of ConceptFacade objects. If no associated list is found, a copy of the default list is returned
     */
    default List<ConceptFacade> getConceptList(Enum key, List<ConceptFacade> defaultList) {
        Optional<List<ConceptFacade>> optionalList = getOptionalConceptList(enumToGeneralKey(key));
        if (optionalList.isEmpty()) {
            List<ConceptFacade> proxyList = new ArrayList<>(defaultList.size());
            for (ConceptFacade conceptProxy : defaultList) {
                proxyList.add(conceptProxy);
            }
            return proxyList;
        }
        return optionalList.get();
    }

    /**
     * Retrieves an optional list of concept facades associated with the provided key.
     * If the key corresponds to an optional list of strings, each string is converted
     * to a ConceptFacade using the ProxyFactory. If no such list exists, or if the
     * conversion fails, an empty optional is returned.
     *
     * @param key the key used to retrieve the optional list of strings representing concept facades
     * @return an optional list of ConceptFacade objects; returns an empty optional if no list exists
     *         or if the conversion fails
     */
    default Optional<List<ConceptFacade>> getOptionalConceptList(String key) {
        Optional<List<String>> optionalList = getOptionalList(key);
        if (optionalList.isPresent()) {
            List<String> list = optionalList.get();
            List<ConceptFacade> proxyList = new ArrayList<>(list.size());
            for (String proxyString : list) {
                ProxyFactory.fromXmlFragmentOptional(proxyString).ifPresent(entityFacade -> proxyList.add((EntityProxy.Concept) entityFacade));
            }
            return Optional.of(proxyList);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a list associated with the given key. If the retrieved list is empty,
     * the provided default list is returned.
     *
     * @param key the key used to retrieve the associated list
     * @param defaultList the default list to return if the retrieved list is empty
     * @return the list associated with the key or the default list if the retrieved list is empty
     */
    default List<String> getList(String key, List<String> defaultList) {
        List<String> list = getList(key);
        if (list.isEmpty()) {
            return defaultList;
        }
        return list;
    }

    /**
     * Associates the specified password with the provided key. The key is
     * converted to a general key representation before storing the password.
     *
     * @param key the enumeration key to be converted and used for storing the password
     * @param password the password to be associated with the given key
     */
    default void putPassword(Enum key, char[] password) {
        putPassword(enumToGeneralKey(key), password);
    }

    /**
     * Stores an encrypted version of the given password associated with the specified key.
     *
     * @param key the key to associate with the encrypted password
     * @param password the password to be encrypted and stored
     */
    default void putPassword(String key, char[] password) {
        try {
            String encryptedPassword = PasswordHasher.encrypt("obfuscate-komet".toCharArray(), password);
            put(key, encryptedPassword);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrieves the password associated with the given key.
     *
     * @param key the enumeration key for which the password is to be retrieved
     * @return an {@code Optional} containing the password as a character array, or an empty {@code Optional}
     *         if no password is found for the given key
     */
    default Optional<char[]> getPassword(Enum key) {
        return getPassword(enumToGeneralKey(key));
    }

    /**
     * Retrieves and decrypts the password associated with the specified key.
     *
     * @param key the identifier used to retrieve the encrypted password
     * @return an Optional containing the decrypted password as a char array if present and valid,
     *         or an empty Optional if no valid password is associated with the given key
     * @throws RuntimeException if an error occurs during password retrieval or decryption
     */
    default Optional<char[]> getPassword(String key) {
        try {
            Optional<String> encryptedPassword = get(key);
            if (encryptedPassword.isPresent() && !encryptedPassword.get().isEmpty()) {
                return Optional.of(PasswordHasher.decryptToChars("obfuscate-komet".toCharArray(),
                        encryptedPassword.get()));
            }
            return Optional.empty();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrieves a password associated with the given key. If no password is found,
     * the default password is returned.
     *
     * @param key the enum key used to locate the password
     * @param defaultPassword the default password to return if no saved password is found
     * @return the saved password if present, otherwise the default password
     */
    default char[] getPassword(Enum key, char[] defaultPassword) {
        Optional<char[]> savedPassword = getPassword(enumToGeneralKey(key));
        if (savedPassword.isPresent()) {
            return savedPassword.get();
        }
        return defaultPassword;
    }

    /**
     * Retrieves the password associated with the specified key. If no password is found,
     * the provided default password will be returned.
     *
     * @param key the identifier used to retrieve the saved password
     * @param defaultPassword the default password to return if no saved password is found
     * @return the password associated with the specified key, or the default password if none is found
     */
    default char[] getPassword(String key, char[] defaultPassword) {
        Optional<char[]> savedPassword = getPassword(key);
        if (savedPassword.isPresent()) {
            return savedPassword.get();
        }
        return defaultPassword;
    }

    /**
     * Retrieves a concept proxy associated with the given enum key.
     *
     * @param key the enum key for which the concept proxy is to be retrieved
     * @return an optional containing the associated concept proxy if found, or an empty optional if none exists
     */
    default Optional<EntityProxy.Concept> getConceptProxy(Enum key) {
        return getConceptProxy(enumToGeneralKey(key));
    }

    /**
     * Retrieves a concept proxy based on the provided key.
     *
     * @param key the key used to retrieve the concept specification
     * @return an optional containing the concept proxy if the key corresponds to a valid specification,
     *         or an empty optional if the specification is not present or invalid
     */
    default Optional<EntityProxy.Concept> getConceptProxy(String key) {
        Optional<String> spec = get(key);

        if (spec.isPresent()) {
            return ProxyFactory.fromXmlFragmentOptional(spec.get());
        }
        return Optional.empty();
    }

    /**
     * Retrieves the concept proxy associated with the specified enum key, or returns the provided default value
     * if the association cannot be found.
     *
     * @param key the enum used to lookup the concept proxy
     * @param defaultValue the default concept proxy to be returned if no matching association is found
     * @return the concept proxy associated with the specified enum key, or the specified default value
     */
    default EntityProxy.Concept getConceptProxy(Enum key, EntityProxy.Concept defaultValue) {
        return getConceptProxy(enumToGeneralKey(key), defaultValue);
    }

    /**
     * Retrieves a concept proxy associated with the given key.
     * If the key is not found, returns the provided default value.
     *
     * @param key the key to lookup in the underlying data source
     * @param defaultValue the default concept proxy to return if the key is not found
     * @return the concept proxy corresponding to the key, or the default value if the key is not found
     */
    default EntityProxy.Concept getConceptProxy(String key, EntityProxy.Concept defaultValue) {
        Optional<String> spec = get(key);
        if (spec.isPresent()) {
            return ProxyFactory.fromXmlFragment(spec.get());
        }
        return defaultValue;
    }

    /**
     * Stores a concept proxy associated with the specified key.
     * Converts the concept proxy into its XML fragment representation before storing.
     *
     * @param key the key with which the specified concept proxy is to be associated
     * @param conceptProxy the concept proxy to be stored
     */
    default void putConceptProxy(String key, EntityProxy.Concept conceptProxy) {
        put(key, conceptProxy.toXmlFragment());
    }

    /**
     * Associates the specified concept proxy with the specified key in the underlying data structure.
     * This method uses the provided enumeration key to generate a general key and maps it to the XML
     * fragment representation of the given concept proxy.
     *
     * @param key the enumeration key used to generate the general key for mapping
     * @param conceptProxy the concept proxy to be stored, whose XML fragment will be used
     */
    default void putConceptProxy(Enum key, EntityProxy.Concept conceptProxy) {
        put(enumToGeneralKey(key), conceptProxy.toXmlFragment());
    }

    /**
     * Associates the specified entity with the key derived from the given Enum.
     *
     * @param key the enumeration value to be converted into a general key
     * @param entityFacade the entity to be associated with the derived key
     */
    default void putEntity(Enum key, EntityFacade entityFacade) {
        putEntity(enumToGeneralKey(key), entityFacade);
    }

    /**
     * Stores an entity in a specific format by converting it to an XML fragment and associating it with a given key.
     *
     * @param key the unique identifier used to store the entity
     * @param entityFacade the facade object representing the entity to be stored
     */
    default void putEntity(String key, EntityFacade entityFacade) {
        put(key, entityFacade.toXmlFragment());
    }

    /**
     * Retrieves an EntityFacade instance associated with the specified enumeration key.
     * This method converts the provided Enum key into a general key and retrieves
     * the corresponding EntityFacade instance, if it exists.
     *
     * @param key the enumeration key used to identify the EntityFacade
     * @return an Optional containing the EntityFacade if found, or an empty Optional if no match is found
     */
    default Optional<EntityFacade> getEntity(Enum key) {
        return getEntity(enumToGeneralKey(key));
    }

    /**
     * Retrieves an entity represented as an {@link EntityFacade} object based on the given key.
     * The entity is constructed from an XML fragment if the key is associated with a non-empty value.
     *
     * @param key the identifier used to look up the associated XML data
     * @return an {@link Optional} containing the {@link EntityFacade} object if the key exists and XML data is valid,
     *         otherwise an empty {@link Optional}
     */
    default Optional<EntityFacade> getEntity(String key) {
        Optional<String> optionalXml = get(key);
        if (optionalXml.isPresent()) {
            return Optional.ofNullable(ProxyFactory.fromXmlFragment(optionalXml.get()));
        }
        return Optional.empty();
    }


    /**
     * Retrieves an entity based on the provided key. If the entity is not found, a default value is returned.
     *
     * @param key the enumeration key used to locate the entity
     * @param defaultValue the default value to return if the entity is not found
     * @return the entity associated with the provided key, or the default value if the entity is not found
     */
    default EntityFacade getEntity(Enum key, EntityFacade defaultValue) {
        return getEntity(enumToGeneralKey(key), defaultValue);
    }

    /**
     * Retrieves an EntityFacade object associated with the specified key.
     * If the key does not exist or cannot be mapped to a valid EntityFacade,
     * the provided default value is returned.
     *
     * @param key the unique identifier used to locate the desired EntityFacade
     * @param defaultValue the default EntityFacade to return if the key is not found or invalid
     * @return the EntityFacade object associated with the key, or the defaultValue if no mapping exists
     */
    default EntityFacade getEntity(String key, EntityFacade defaultValue) {
        Optional<String> optionalXml = get(key);
        if (optionalXml.isPresent()) {
            return ProxyFactory.fromXmlFragment(optionalXml.get());
        }
        return defaultValue;
    }

    /**
     * @return Map of the preferences at this node level.
     * @throws java.util.prefs.BackingStoreException
     */
    default Map<String, String> getMap() throws BackingStoreException {
        HashMap<String, String> map = new HashMap<>();
        for (String key : keys()) {
            map.put(key, get(key, ""));
        }
        return map;
    }

    /**
     * Returns all of the keys that have an associated value in this preference
     * node. (The returned array will be of size zero if this node has no
     * preferences.)
     *
     * <p>
     * If the implementation supports <i>stored defaults</i> and there are any
     * such defaults at this node that have not been overridden, by explicit
     * preferences, the defaults are returned in the array in addition to any
     * explicit preferences.
     *
     * @return an array of the keys that have an associated value in this
     * preference node.
     * @throws BackingStoreException if this operation cannot be completed due
     *                               to a failure in the backing store, or inability to communicate with it.
     * @throws IllegalStateException if this node (or an ancestor) has been
     *                               removed with the {@link #removeNode()} method.
     */
    String[] keys() throws BackingStoreException;
}
