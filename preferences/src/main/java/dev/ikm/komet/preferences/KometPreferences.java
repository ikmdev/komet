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
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * 
 */
public interface KometPreferences {

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

    default Optional<UUID> getUuid(Enum key) {
        Optional<String> optionalString = get(key);
        if (optionalString.isPresent()) {
            return Optional.of(UUID.fromString(optionalString.get()));
        }
        return Optional.empty();
    }

    default Optional<String> get(Enum key) {
        return Optional.ofNullable(get(key, null));
    }

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

    default OptionalInt getInt(String key) {
        Optional<String> optionalValue = get(key);
        if (optionalValue.isPresent()) {
            return OptionalInt.of(Integer.parseInt(optionalValue.get()));
        }
        return OptionalInt.empty();
    }

    default Optional<String> get(String key) {
        return Optional.ofNullable(get(key, null));
    }

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

    default OptionalLong getLong(String key) {
        Optional<String> optionalValue = get(key);
        if (optionalValue.isPresent()) {
            return OptionalLong.of(Long.parseLong(optionalValue.get()));
        }
        return OptionalLong.empty();
    }

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

    default Optional<Boolean> getBoolean(Enum key) {
        return getBoolean(enumToGeneralKey(key));
    }

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

    default void putDoubleArray(Enum key, double[] array) {
        putDoubleArray(enumToGeneralKey(key), array);
    }

    default void putDoubleArray(String key, double[] array) {
        List<String> doubleList = new ArrayList<>(array.length);
        for (double value : array) {
            doubleList.add(Double.toString(value));
        }
        putList(key, doubleList);
    }

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

    default double[] getDoubleArray(Enum key, double[] defaultArray) {
        return getDoubleArray(enumToGeneralKey(key), defaultArray);
    }

    default double[] getDoubleArray(String key, double[] defaultArray) {
        Optional<double[]> optionalArray = getDoubleArray(key);
        if (optionalArray.isPresent()) {
            return optionalArray.get();
        }
        return defaultArray;
    }

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

    default Optional<List<String>> getOptionalList(String key) {
        Optional<String> value = get(key);
        if (value.isPresent()) {
            return Optional.of(getList(key));
        }
        return Optional.empty();
    }

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

    default Optional<double[]> getDoubleArray(Enum key) {
        return getDoubleArray(enumToGeneralKey(key));
    }

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

    default <T extends Object> T getObject(UUID key, T defaultValue) {
        return getObject(key.toString(), defaultValue);
    }

    default <T extends Object> T getObject(String key, T defaultValue) {
        Optional<byte[]> optionalBytes = getByteArray(key);
        if (optionalBytes.isPresent()) {
            return Encodable.decode(optionalBytes.get());
        }
        return defaultValue;
    }

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

    default <T extends Object> T getObject(Enum key, T defaultValue) {
        Optional<byte[]> optionalBytes = getByteArray(key);
        if (optionalBytes.isPresent()) {
            return Encodable.decode(optionalBytes.get());
        }
        return defaultValue;
    }

    default Optional<byte[]> getByteArray(Enum key) {
        return getByteArray(enumToGeneralKey(key));
    }

    default <T extends Object> Optional<T> getObject(Enum key) {
        Optional<byte[]> optionalBytes = getByteArray(key);
        if (optionalBytes.isPresent()) {
            return Optional.ofNullable(Encodable.decode(optionalBytes.get()));
        }
        return Optional.empty();
    }

    default <T extends Object> Optional<T> getObject(UUID key) {
        return getObject(key.toString());
    }

    default <T extends Object> Optional<T> getObject(String key) {
        Optional<byte[]> optionalBytes = getByteArray(key);
        if (optionalBytes.isPresent()) {
            return Optional.ofNullable(Encodable.decode(optionalBytes.get()));
        }
        return Optional.empty();
    }

    default void putObject(Enum key, Encodable encodable) {
        putObject(enumToGeneralKey(key), encodable);
    }

    default void putObject(String key, Encodable encodable) {
        putByteArray(key, encodable.toBytes());
    }

    default void putObject(UUID key, Encodable encodable) {
        putObject(key.toString(), encodable);
    }

    default byte[] getByteArray(Enum key, byte[] defaultValue) {
        return getByteArray(enumToGeneralKey(key), defaultValue);
    }

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

    default KometPreferences node(Class<?> c) {
        return node(nodeName(c));
    }

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

    default <T extends Enum<T>> Optional<T> getEnum(Class<T> enumClass) {
        String key = enumClass.getCanonicalName();
        Optional<String> value = get(key);
        if (value.isPresent()) {
            return Optional.of(Enum.valueOf(enumClass, value.get()));
        }
        return Optional.empty();
    }

    default void putEnum(Enum value) {
        String key = value.getClass().getCanonicalName();
        put(key, value.name());
    }

    default boolean hasKey(Enum enumDefault) {
        return get(enumToGeneralKey(enumDefault)).isPresent();
    }

    default boolean hasKey(String key) {
        return get(key).isPresent();
    }

    default void putArray(Enum key, String[] array) {
        putList(key, Arrays.asList(array));
    }

    default void putList(Enum key, List<String> list) {
        putList(enumToGeneralKey(key), list);
    }

    default void putArray(String key, String[] array) {
        putList(key, Arrays.asList(array));
    }

    default String[] getArray(Enum key) {
        return getList(enumToGeneralKey(key)).toArray(new String[2]);
    }

    default String[] getArray(String key) {
        return getList(key).toArray(new String[2]);
    }

    default void putConceptList(Enum key, List<? extends ConceptFacade> list) {
        putConceptList(enumToGeneralKey(key), list);
    }

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

    default void putComponentList(Enum key, Collection<? extends EntityFacade> list) {
        putComponentList(enumToGeneralKey(key), list);
    }

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

    default <T extends EntityFacade> List<T> getEntityList(Enum key, T[] defaultList) {
        return getEntityList(enumToGeneralKey(key), defaultList);
    }

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

    default List<EntityFacade> getEntityList(Enum key) {
        return getEntityList(enumToGeneralKey(key));
    }

    default List<EntityFacade> getEntityList(String key) {
        List<String> list = getList(key);
        List<EntityFacade> proxyList = new ArrayList<>(list.size());
        for (String proxyString : list) {
            ProxyFactory.fromXmlFragmentOptional(proxyString).ifPresent(entityFacade -> proxyList.add(entityFacade));
        }
        return proxyList;
    }

    default List<String> getList(Enum key) {
        return getList(enumToGeneralKey(key));
    }

    default List<String> getList(Enum key, List<String> defaultList) {
        List<String> list = getList(enumToGeneralKey(key));
        if (list.isEmpty()) {
            return defaultList;
        }
        return list;
    }

    default List<EntityProxy.Concept> getConceptList(Enum key) {
        return getConceptList(enumToGeneralKey(key));
    }

    default List<EntityProxy.Concept> getConceptList(String key) {
        List<String> list = getList(key);
        List<EntityProxy.Concept> proxyList = new ArrayList<>(list.size());
        for (String proxyString : list) {
            ProxyFactory.fromXmlFragmentOptional(proxyString).ifPresent(entityFacade -> proxyList.add((EntityProxy.Concept) entityFacade));
        }
        return proxyList;
    }

    default List<PatternFacade> getPatternList(Enum key) {
        return getPatternList(key, new ArrayList<>());
    }

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

    default List<PatternFacade> getPatternList(Enum key, PatternFacade[] defaultList) {
        return getPatternList(key, Arrays.asList(defaultList));
    }

    default List<ConceptFacade> getConceptList(Enum key, ConceptFacade[] defaultList) {
        return getConceptList(key, Arrays.asList(defaultList));
    }

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

    default List<String> getList(String key, List<String> defaultList) {
        List<String> list = getList(key);
        if (list.isEmpty()) {
            return defaultList;
        }
        return list;
    }

    default void putPassword(Enum key, char[] password) {
        putPassword(enumToGeneralKey(key), password);
    }

    default void putPassword(String key, char[] password) {
        try {
            String encryptedPassword = PasswordHasher.encrypt("obfuscate-komet".toCharArray(), password);
            put(key, encryptedPassword);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    default Optional<char[]> getPassword(Enum key) {
        return getPassword(enumToGeneralKey(key));
    }

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

    default char[] getPassword(Enum key, char[] defaultPassword) {
        Optional<char[]> savedPassword = getPassword(enumToGeneralKey(key));
        if (savedPassword.isPresent()) {
            return savedPassword.get();
        }
        return defaultPassword;
    }

    default char[] getPassword(String key, char[] defaultPassword) {
        Optional<char[]> savedPassword = getPassword(key);
        if (savedPassword.isPresent()) {
            return savedPassword.get();
        }
        return defaultPassword;
    }

    default Optional<EntityProxy.Concept> getConceptProxy(Enum key) {
        return getConceptProxy(enumToGeneralKey(key));
    }

    default Optional<EntityProxy.Concept> getConceptProxy(String key) {
        Optional<String> spec = get(key);

        if (spec.isPresent()) {
            return ProxyFactory.fromXmlFragmentOptional(spec.get());
        }
        return Optional.empty();
    }

    default EntityProxy.Concept getConceptProxy(Enum key, EntityProxy.Concept defaultValue) {
        return getConceptProxy(enumToGeneralKey(key), defaultValue);
    }

    default EntityProxy.Concept getConceptProxy(String key, EntityProxy.Concept defaultValue) {
        Optional<String> spec = get(key);
        if (spec.isPresent()) {
            return ProxyFactory.fromXmlFragment(spec.get());
        }
        return defaultValue;
    }

    default void putConceptProxy(String key, EntityProxy.Concept conceptProxy) {
        put(key, conceptProxy.toXmlFragment());
    }

    default void putConceptProxy(Enum key, EntityProxy.Concept conceptProxy) {
        put(enumToGeneralKey(key), conceptProxy.toXmlFragment());
    }

    default void putEntity(Enum key, EntityFacade entityFacade) {
        putEntity(enumToGeneralKey(key), entityFacade);
    }

    default void putEntity(String key, EntityFacade entityFacade) {
        put(key, entityFacade.toXmlFragment());
    }

    default Optional<EntityFacade> getEntity(Enum key) {
        return getEntity(enumToGeneralKey(key));
    }

    default Optional<EntityFacade> getEntity(String key) {
        Optional<String> optionalXml = get(key);
        if (optionalXml.isPresent()) {
            return Optional.ofNullable(ProxyFactory.fromXmlFragment(optionalXml.get()));
        }
        return Optional.empty();
    }


    default EntityFacade getEntity(Enum key, EntityFacade defaultValue) {
        return getEntity(enumToGeneralKey(key), defaultValue);
    }

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
