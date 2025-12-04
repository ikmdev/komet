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
package dev.ikm.tinkar.collection;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.ObjIntConsumer;

/**
 * An interface to allow different intObject map implementations
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * @param <E>
 */
public interface IntObjectMap<E>
{

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for the key, the old value
	 * is replaced by the specified value. (A map <tt>m</tt> is said to contain
	 * a mapping for a key <tt>k</tt> if and only if
	 * {@link #containsKey(int) m.containsKey(k)} would return <tt>true</tt>.)
	 *
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
         * @return <tt>true</tt> if the receiver did not already contain such a key; <tt>false</tt> if the receiver did
   *         already contain such a key - the new value has now replaced the formerly associated value.
	 * @throws NullPointerException if the specified value is null
	 *             and this map does not permit null keys or values
	 */
	public boolean put(int key, E value);

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for
	 * the key, the old value is replaced by the specified value and returned. (A map
	 * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
	 * if {@link #containsKey(int) m.containsKey(k)} would return <tt>true</tt>.)
	 *
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @return The old value, or null if no value present.
	 * @throws NullPointerException if the specified value is null
	 *             and this map does not permit null keys or values
	 */
	public E getAndSet(int key, E value);

	/**
	 * Returns the value to which the specified key is mapped,
	 * or {@code null} if this map contains no mapping for the key.
	 *
	 * <p>
	 * More formally, if this map contains a mapping from a key
	 * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise
	 * it returns {@code null}. (There can be at most one such mapping.)
	 *
	 * <p>
	 * If this map permits null values, then a return value of
	 * {@code null} does not <i>necessarily</i> indicate that the map
	 * contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to {@code null}. The {@link #containsKey
	 * containsKey} operation may be used to distinguish these two cases.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or
	 *         {@code null} if this map contains no mapping for the key
	 */
	public E get(int key);

	/**
	 * Returns the value to which the specified key is mapped,
	 * or an empty {@code Optional} if this map contains no mapping for the key.
	 *
	 * <p>
	 * More formally, if this map contains a mapping from a key
	 * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise
	 * it returns {@code null}. (There can be at most one such mapping.)
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or
	 *         {@code null} if this map contains no mapping for the key
	 */
	public Optional<E> getOptional(int key);

	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the specified
	 * key. More formally, returns <tt>true</tt> if and only if
	 * this map contains a mapping for a key <tt>k</tt> such that
	 * <tt>(key==null ? k==null : key.equals(k))</tt>. (There can be
	 * at most one such mapping.)
	 *
	 * @param key key whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map contains a mapping for the specified key
	 */
	public boolean containsKey(int key);

	/**
	 * Returns the number of key-value mappings in this map.
	 *
	 * @return the number of key-value mappings in this map
	 */
	public int size();

	/**
	 * Removes all of the mappings from this map.
	 * The map will be empty after this call returns.
	 */
	public void clear();

	/**
	 * Apply an operation to every key/value pair in the map
	 * 
	 * @param consumer the operation to perform
	 */
	public void forEach(ObjIntConsumer<E> consumer);

	/**
	 * Atomically updates the element at index {@code index} with the
	 * results of applying the given function to the current and
	 * given values, returning the updated value. The function should
	 * be side-effect-free, since it may be re-applied when attempted
	 * updates fail due to contention among threads. The function is
	 * applied with the current value at index {@code index} as its first
	 * argument, and the given update as the second argument.
	 *
	 * @param index the index
	 * @param newValue the update value
	 * @param accumulatorFunction a side-effect-free function of two arguments
	 * @return the updated value
	 */
	public E accumulateAndGet(int index, E newValue, BinaryOperator<E> accumulatorFunction);
}
