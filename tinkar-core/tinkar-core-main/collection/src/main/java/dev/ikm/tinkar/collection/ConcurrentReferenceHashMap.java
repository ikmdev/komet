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
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

//~--- classes ----------------------------------------------------------------

/**
 * An advanced hash table supporting configurable garbage collection semantics of keys and values, optional
 * referential-equality, full concurrency of retrievals, and adjustable expected concurrency for updates.
 *
 * This table is designed around specific advanced use-cases. If there is any doubt whether this table is for
 * you, you most likely should be using
 * {@link java.util.concurrent.ConcurrentHashMap} instead.
 *
 * This table supports strong, weak, and soft keys and values. By default keys are weak, and values are
 * strong. Such a configuration offers similar behavior to {@link java.util.WeakHashMap}, entries of this
 * table are periodically removed once their corresponding keys are no longer referenced outside of this
 * table. In other words, this table will not prevent a key from being discarded by the garbage collector.
 * Once a key has been discarded by the collector, the corresponding entry is no longer visible to this table;
 * however, the entry may occupy space until a future table operation decides to reclaim it. For this reason,
 * summary functions such as {@code size} and {@code isEmpty} might return a value greater than the observed
 * number of entries. In order to support a high level of concurrency, stale entries are only reclaimed during
 * blocking (usually mutating) operations.
 *
 * Enabling soft keys allows entries in this table to remain until their space is absolutely needed by the
 * garbage collector. This is unlike weak keys which can be reclaimed as soon as they are no longer referenced
 * by a normal strong reference. The primary use case for soft keys is a cache, which ideally occupies memory
 * that is not in use for as long as possible.
 *
 * By default, values are held using a normal strong reference. This provides the commonly desired guarantee
 * that a value will always have at least the same life-span as it's key. For this reason, care should be
 * taken to ensure that a value never refers, either directly or indirectly, to its key, thereby preventing
 * reclamation. If this is unavoidable, then it is recommended to use the same reference type in use for the
 * key. However, it should be noted that non-strong values may disappear before their corresponding key.
 *
 * While this table does allow the use of both strong keys and values, it is recommended to use {@link java.util.concurrent.ConcurrentHashMap}
 * for such a configuration, since it is optimized for that case.
 *
 * Just like {@link java.util.concurrent.ConcurrentHashMap}, this class obeys the same functional
 * specification as {@link java.util.Hashtable}, and includes versions of methods corresponding to each method
 * of {@code Hashtable}. However, even though all operations are thread-safe, retrieval operations do
 * <em>not</em> entail locking, and there is <em>not</em> any support for locking the entire table in a way
 * that prevents all access. This class is fully interoperable with {@code Hashtable} in programs that rely
 * on its thread safety but not on its synchronization details.
 *
 * <p> Retrieval operations (including {@code get}) generally do not block, so may overlap with update
 * operations (including {@code put} and {@code remove}). Retrievals reflect the results of the most
 * recently <em>completed</em> update operations holding upon their onset. For aggregate operations such as
 * {@code putAll} and {@code clear}, concurrent retrievals may reflect insertion or removal of only some
 * entries. Similarly, Iterators and Enumerations return elements reflecting the state of the hash table at
 * some point at or since the creation of the iterator/enumeration. They do <em>not</em> throw
 * {@link ConcurrentModificationException}. However, iterators are designed to be used by only one thread at a
 * time.
 *
 * <p> The allowed concurrency among update operations is guided by the optional {@code concurrencyLevel}
 * constructor argument (default {@code 16}), which is used as a hint for internal sizing. The table is
 * internally partitioned to try to permit the indicated number of concurrent updates without contention.
 * Because placement in hash tables is essentially random, the actual concurrency will vary. Ideally, you
 * should choose a value to accommodate as many threads as will ever concurrently modify the table. Using a
 * significantly higher value than you need can waste space and time, and a significantly lower value can lead
 * to thread contention. But overestimates and underestimates within an order of magnitude do not usually have
 * much noticeable impact. A value of one is appropriate when it is known that only one thread will modify and
 * all others will only read. Also, resizing this or any other kind of hash table is a relatively slow
 * operation, so, when possible, it is a good idea to provide estimates of expected table sizes in
 * constructors.
 *
 * <p> This class and its views and iterators implement all of the <em>optional</em> methods of the {@link Map}
 * and {@link Iterator} interfaces.
 *
 * <p> Like {@link Hashtable} but unlike {@link HashMap}, this class does <em>not</em> allow {@code null} to
 * be used as a key or value.
 *
 * <p> This class is a member of the <a href="{@docRoot}/../technotes/guides/collections/index.html"> Java
 * Collections Framework</a>.
 *
 * @author Doug Lea
 * @author Jason T. Greene
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @deprecated Replace with Caffeine.newBuilder().weakValues().build();
 */
@Deprecated
public class ConcurrentReferenceHashMap<K, V>
        extends AbstractMap<K, V>
        implements java.util.concurrent.ConcurrentMap<K, V>, Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7249069246763182397L;

    /** The Constant DEFAULT_KEY_TYPE. */
    static final ReferenceType DEFAULT_KEY_TYPE = ReferenceType.WEAK;

    /** The Constant DEFAULT_VALUE_TYPE. */
    static final ReferenceType DEFAULT_VALUE_TYPE = ReferenceType.STRONG;

    /**
     * The default initial capacity for this table, used when not otherwise specified in a constructor.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The default load factor for this table, used when not otherwise specified in a constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The default concurrency level for this table, used when not otherwise specified in a constructor.
     */
    static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the constructors with
     * arguments. MUST be a power of two <= 1<<30 to ensure that entries are indexable using ints.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The maximum number of segments to allow; used to bound constructor arguments.
     */
    static final int MAX_SEGMENTS = 1 << 16;  // slightly conservative

    /**
     * Number of unsynchronized retries in size and containsValue methods before resorting to locking. This is
     * used to avoid unbounded retries if tables undergo continuous modification which would make it
     * impossible to obtain an accurate result.
     */
    static final int RETRIES_BEFORE_LOCK = 2;

    //~--- fields --------------------------------------------------------------

    /**
     * Mask value for indexing into segments. The upper bits of a key's hash code are used to choose the
     * segment.
     */
    final int segmentMask;

    /**
     * Shift value for indexing within segments.
     */
    final int segmentShift;

    /** The segments, each of which is a specialized hash table. */
    final Segment<K, V>[] segments;

    /** The identity comparisons. */
    boolean identityComparisons;

    /** The key set. */
    transient Set<K> keySet;

    /** The entry set. */
    transient Set<Map.Entry<K, V>> entrySet;

    /** The values. */
    transient Collection<V> values;

    //~--- constant enums ------------------------------------------------------

    /**
     * The Enum Option.
     */
    public static enum Option {
        /**
         * Indicates that referential-equality (== instead of .equals()) should be used when locating keys.
         * This offers similar behavior to {@link IdentityHashMap}
         */
        IDENTITY_COMPARISONS
    }

    /*
     * The basic strategy is to subdivide the table among Segments, each of which itself is a concurrently
     * readable hash table.
     */

    /**
     * An uri specifying which Java reference type should be used to refer to a key and/or value.
     */
    public static enum ReferenceType {
        /** Indicates a normal Java strong reference should be used. */
        STRONG,

        /** Indicates a {@link WeakReference} should be used. */
        WEAK,

        /** Indicates a {@link SoftReference} should be used. */
        SOFT
    }

    ;
    ;

    //~--- constructors --------------------------------------------------------

    /**
     * Creates a new, empty map with a default initial capacity (16), reference types (weak keys, strong
     * values), default load factor (0.75) and concurrencyLevel (16).
     */
    public ConcurrentReferenceHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    /**
     * Creates a new, empty map with the specified initial capacity, and with default reference types (weak
     * keys, strong values), load factor (0.75) and concurrencyLevel (16).
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to accommodate
     * this many elements.
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    public ConcurrentReferenceHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    /**
     * Creates a new map with the same mappings as the given map. The map is created with a capacity of 1.5
     * times the number of mappings in the given map or 16 (whichever is greater), and a default load factor
     * (0.75) and concurrencyLevel (16).
     *
     * @param m the map
     */
    public ConcurrentReferenceHashMap(Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR,
                DEFAULT_CONCURRENCY_LEVEL);
        putAll(m);
    }

    /**
     * Creates a new, empty map with the specified initial capacity and load factor and with the default
     * reference types (weak keys, strong values), and concurrencyLevel (16).
     *
     * @param initialCapacity The implementation performs internal sizing to accommodate this many elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed when
     * the average number of elements per bin exceeds this threshold.
     * @throws IllegalArgumentException if the initial capacity of elements is negative or the load factor is
     * nonpositive
     *
     * @since 1.6
     */
    public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
    }

    /**
     * Creates a new, empty reference map with the specified key and value reference types.
     *
     * @param keyType the reference type to use for keys
     * @param valueType the reference type to use for values
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    public ConcurrentReferenceHashMap(ReferenceType keyType, ReferenceType valueType) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, keyType, valueType, null);
    }

    /**
     * Creates a new, empty map with the specified initial capacity, load factor and concurrency level.
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to accommodate
     * this many elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed when
     * the average number of elements per bin exceeds this threshold.
     * @param concurrencyLevel the estimated number of concurrently updating threads. The implementation
     * performs internal sizing to try to accommodate this many threads.
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor or
     * concurrencyLevel are nonpositive.
     */
    public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this(initialCapacity, loadFactor, concurrencyLevel, DEFAULT_KEY_TYPE, DEFAULT_VALUE_TYPE, null);
    }

    /**
     * Creates a new, empty map with the specified initial capacity, reference types and with default load
     * factor (0.75) and concurrencyLevel (16).
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to accommodate
     * this many elements.
     * @param keyType the reference type to use for keys
     * @param valueType the reference type to use for values
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    public ConcurrentReferenceHashMap(int initialCapacity, ReferenceType keyType, ReferenceType valueType) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, keyType, valueType, null);
    }

    /**
     * Creates a new, empty reference map with the specified reference types and behavioral options.
     *
     * @param keyType the reference type to use for keys
     * @param valueType the reference type to use for values
     * @param options the options
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    public ConcurrentReferenceHashMap(ReferenceType keyType, ReferenceType valueType, EnumSet<Option> options) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, keyType, valueType, options);
    }

    /*
     * ---------------- Public operations --------------
     */

    /**
     * Creates a new, empty map with the specified initial capacity, reference types, load factor and
     * concurrency level.
     *
     * Behavioral changing options such as {@link Option#IDENTITY_COMPARISONS} can also be specified.
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to accommodate
     * this many elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed when
     * the average number of elements per bin exceeds this threshold.
     * @param concurrencyLevel the estimated number of concurrently updating threads. The implementation
     * performs internal sizing to try to accommodate this many threads.
     * @param keyType the reference type to use for keys
     * @param valueType the reference type to use for values
     * @param options the behavioral options
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor or
     * concurrencyLevel are nonpositive.
     */
    public ConcurrentReferenceHashMap(int initialCapacity,
                                      float loadFactor,
                                      int concurrencyLevel,
                                      ReferenceType keyType,
                                      ReferenceType valueType,
                                      EnumSet<Option> options) {
        if (!(loadFactor > 0) || (initialCapacity < 0) || (concurrencyLevel <= 0)) {
            throw new IllegalArgumentException();
        }

        if (concurrencyLevel > MAX_SEGMENTS) {
            concurrencyLevel = MAX_SEGMENTS;
        }

        // Find power-of-two sizes best matching arguments
        int sshift = 0;
        int ssize  = 1;

        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }

        this.segmentShift = 32 - sshift;
        this.segmentMask  = ssize - 1;
        this.segments     = Segment.newArray(ssize);

        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }

        int c = initialCapacity / ssize;

        if (c * ssize < initialCapacity) {
            ++c;
        }

        int cap = 1;

        while (cap < c) {
            cap <<= 1;
        }

        this.identityComparisons = (options != null) && options.contains(Option.IDENTITY_COMPARISONS);

        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i] = new Segment<>(cap, loadFactor, keyType, valueType, this.identityComparisons);
        }
    }

    //~--- methods -------------------------------------------------------------

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i].clear();
        }
    }

    /**
     * Legacy method testing if some key maps into the specified value in this table. This method is identical
     * in functionality to
     * {@link #containsValue}, and exists solely to ensure full compatibility with class {@link java.util.Hashtable},
     * which supported this method prior to introduction of the Java Collections framework.
     *
     * @param value a value to search for
     * @return {@code true} if and only if some key maps to the {@code value} argument in this table as
     * determined by the {@code equals} method; {@code false} otherwise
     * @throws NullPointerException if the specified value is null
     */
    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * Tests if the specified object is a key in this table.
     *
     * @param key possible key
     * @return {@code true} if and only if the specified object is a key in this table, as determined by the
     * {@code equals} method; {@code false} otherwise.
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean containsKey(Object key) {
        final int hash = hashOf(key);

        return segmentFor(hash).containsKey(key, hash);
    }

    /**
     * Returns {@code true} if this map maps one or more keys to the specified value. Note: This method
     * requires a full internal traversal of the hash table, and so is much slower than method
     * {@code containsKey}.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if this map maps one or more keys to the specified value
     * @throws NullPointerException if the specified value is null
     */
    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

        // See explanation of modCount use above
        final Segment<K, V>[] segments = this.segments;
        final int[]           mc       = new int[segments.length];

        // Try a few times without locking
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            int mcsum = 0;

            for (int i = 0; i < segments.length; ++i) {
                mcsum += mc[i] = segments[i].modCount;

                if (segments[i].containsValue(value)) {
                    return true;
                }
            }

            boolean cleanSweep = true;

            if (mcsum != 0) {
                for (int i = 0; i < segments.length; ++i) {
                    if (mc[i] != segments[i].modCount) {
                        cleanSweep = false;
                        break;
                    }
                }
            }

            if (cleanSweep) {
                return false;
            }
        }

        // Resort to locking all segments
        for (int i = 0; i < segments.length; ++i) {
            segments[i].lock();
        }

        boolean found = false;

        try {
            for (int i = 0; i < segments.length; ++i) {
                if (segments[i].containsValue(value)) {
                    found = true;
                    break;
                }
            }
        } finally {
            for (int i = 0; i < segments.length; ++i) {
                segments[i].unlock();
            }
        }

        return found;
    }

    /**
     * Returns an enumeration of the values in this table.
     *
     * @return an enumeration of the values in this table
     * @see #values()
     */
    public Enumeration<V> elements() {
        return new ValueIterator();
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set is backed by the map, so
     * changes to the map are reflected in the set, and vice-versa. The set supports element removal, which
     * removes the corresponding mapping from the map, via the {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear} operations. It does not support the
     * {@code add} or {@code addAll} operations.
     *
     * <p>The view's {@code iterator} is a "weakly consistent" iterator that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon construction of the iterator, and may (but is
     * not guaranteed to) reflect any modifications subsequent to construction.
     *
     * @return the set
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final Set<Map.Entry<K, V>> es = this.entrySet;

        return (es != null) ? es
                : (this.entrySet = new EntrySet());
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is backed by the map, so changes
     * to the map are reflected in the set, and vice-versa. The set supports element removal, which removes
     * the corresponding mapping from this map, via the {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear} operations. It does not support the
     * {@code add} or {@code addAll} operations.
     *
     * <p>The view's {@code iterator} is a "weakly consistent" iterator that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon construction of the iterator, and may (but is
     * not guaranteed to) reflect any modifications subsequent to construction.
     *
     * @return the set
     */
    @Override
    public Set<K> keySet() {
        final Set<K> ks = this.keySet;

        return (ks != null) ? ks
                : (this.keySet = new KeySet());
    }

    /**
     * Returns an enumeration of the keys in this table.
     *
     * @return an enumeration of the keys in this table
     * @see #keySet()
     */
    public Enumeration<K> keys() {
        return new KeyIterator();
    }

    /**
     * Removes any stale entries whose keys have been finalized. Use of this method is normally not necessary
     * since stale entries are automatically removed lazily, when blocking operations are required. However,
     * there are some cases where this operation should be performed eagerly, such as cleaning up old
     * references to a ClassLoader in a multi-classloader environment.
     *
     * Note: this method will acquire locks, one at a time, across all segments of this table, so if it is to
     * be used, it should be used sparingly.
     */
    public void purgeStaleEntries() {
        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i].removeStale();
        }
    }

    /**
     * Maps the specified key to the specified value in this table. Neither the key nor the value can be null.
     *
     * <p> The value can be retrieved by calling the {@code get} method with a key that is equal to the
     * original key.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or {@code null} if there was no mapping for
     * {@code key}
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }

        final int hash = hashOf(key);

        return segmentFor(hash).put(key, hash, value, false);
    }

    /**
     * Copies all of the mappings from the specified map to this one. These mappings replace any mappings that
     * this map had for any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> e: m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key, or {@code null} if there was no mapping
     * for the key
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V putIfAbsent(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }

        final int hash = hashOf(key);

        return segmentFor(hash).put(key, hash, value, true);
    }

    public V putIfAbsentReturnCurrentValue(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }

        final int hash = hashOf(key);

        V existingValue = segmentFor(hash).put(key, hash, value, true);
        if (existingValue != null) {
            return existingValue;
        }
        return value;
    }

    /**
     * Removes the key (and its corresponding value) from this map. This method does nothing if the key is not
     * in the map.
     *
     * @param key the key that needs to be removed
     * @return the previous value associated with {@code key}, or {@code null} if there was no mapping for
     * {@code key}
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V remove(Object key) {
        final int hash = hashOf(key);

        return segmentFor(hash).remove(key, hash, null, false);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean remove(Object key, Object value) {
        final int hash = hashOf(key);

        if (value == null) {
            return false;
        }

        return segmentFor(hash).remove(key, hash, value, false) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key, or {@code null} if there was no mapping
     * for the key
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V replace(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }

        final int hash = hashOf(key);

        return segmentFor(hash).replace(key, hash, value);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if any of the arguments are null
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if ((oldValue == null) || (newValue == null)) {
            throw new NullPointerException();
        }

        final int hash = hashOf(key);

        return segmentFor(hash).replace(key, hash, oldValue, newValue);
    }

    /**
     * Returns the number of key-value mappings in this map. If the map contains more than
     * {@code Integer.MAX_VALUE} elements, returns {@code Integer.MAX_VALUE}.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        final Segment<K, V>[] segments = this.segments;
        long                  sum      = 0;
        long                  check    = 0;
        final int[]           mc       = new int[segments.length];

        // Try a few times to get accurate count. On failure due to
        // continuous async changes in table, resort to locking.
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            check = 0;
            sum   = 0;

            int mcsum = 0;

            for (int i = 0; i < segments.length; ++i) {
                sum   += segments[i].count;
                mcsum += mc[i] = segments[i].modCount;
            }

            if (mcsum != 0) {
                for (int i = 0; i < segments.length; ++i) {
                    check += segments[i].count;

                    if (mc[i] != segments[i].modCount) {
                        check = -1;  // force retry
                        break;
                    }
                }
            }

            if (check == sum) {
                break;
            }
        }

        if (check != sum) {  // Resort to locking all segments
            sum = 0;

            for (int i = 0; i < segments.length; ++i) {
                segments[i].lock();
            }

            for (int i = 0; i < segments.length; ++i) {
                sum += segments[i].count;
            }

            for (int i = 0; i < segments.length; ++i) {
                segments[i].unlock();
            }
        }

        if (sum > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) sum;
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map. The collection is backed by the
     * map, so changes to the map are reflected in the collection, and vice-versa. The collection supports
     * element removal, which removes the corresponding mapping from this map, via the
     * {@code Iterator.remove}, {@code Collection.remove}, {@code removeAll}, {@code retainAll}, and
     * {@code clear} operations. It does not support the {@code add} or {@code addAll} operations.
     *
     * <p>The view's {@code iterator} is a "weakly consistent" iterator that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon construction of the iterator, and may (but is
     * not guaranteed to) reflect any modifications subsequent to construction.
     *
     * @return the collection
     */
    @Override
    public Collection<V> values() {
        final Collection<V> vs = this.values;

        return (vs != null) ? vs
                : (this.values = new Values());
    }

    /**
     * Returns the segment that should be used for key with given hash.
     *
     * @param hash the hash code for the key
     * @return the segment
     */
    final Segment<K, V> segmentFor(int hash) {
        return this.segments[(hash >>> this.segmentShift) & this.segmentMask];
    }

    /*
     * ---------------- Small Utilities --------------
     */

    /**
     * Applies a supplemental hash function to a given hashCode, which defends against poor quality hash
     * functions. This is critical because ConcurrentReferenceHashMap uses power-of-two length hash tables,
     * that otherwise encounter collisions for hashCodes that do not differ in lower or upper bits.
     *
     * @param h the h
     * @return the int
     */
    private static int hash(int h) {
        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h << 15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 6);
        h += (h << 2) + (h << 14);
        return h ^ (h >>> 16);
    }

    /**
     * Hash of.
     *
     * @param key the key
     * @return the int
     */
    private int hashOf(Object key) {
        return hash(this.identityComparisons ? System.identityHashCode(key)
                : key.hashCode());
    }

    /**
     * Reconstitute the {@code ConcurrentReferenceHashMap} instance from a stream (i.e., deserialize it).
     *
     * @param s the stream
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        // Initialize each segment to be minimally sized, and let grow.
        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i].setTable(new HashEntry[1]);
        }

        // Read the keys and values, and put the mappings in the table
        for (;;) {
            final K key   = (K) s.readObject();
            final V value = (V) s.readObject();

            if (key == null) {
                break;
            }

            put(key, value);
        }
    }

    /*
     * ---------------- Serialization Support --------------
     */

    /**
     * Save the state of the {@code ConcurrentReferenceHashMap} instance to a stream (i.e., serialize it).
     *
     * @param s the stream
     * @throws IOException Signals that an I/O exception has occurred.
     * @serialData the key (Object) and value (Object) for each key-value mapping, followed by a null pair.
     * The key-value mappings are emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        s.defaultWriteObject();

        for (int k = 0; k < this.segments.length; ++k) {
            final Segment<K, V> seg = this.segments[k];

            seg.lock();

            try {
                final HashEntry<K, V>[] tab = seg.table;

                for (int i = 0; i < tab.length; ++i) {
                    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                        final K key = e.key();

                        if (key == null)  // Skip GC'd keys
                        {
                            continue;
                        }

                        s.writeObject(key);
                        s.writeObject(e.value());
                    }
                }
            } finally {
                seg.unlock();
            }
        }

        s.writeObject(null);
        s.writeObject(null);
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        final Segment<K, V>[] segments = this.segments;

        /*
         * We keep track of per-segment modCounts to avoid ABA problems in which an element in one segment was
         * added and in another removed during traversal, in which case the table was never actually empty at
         * any point. Note the similar use of modCounts in the size() and containsValue() methods, which are
         * the only other methods also susceptible to ABA problems.
         */
        final int[] mc    = new int[segments.length];
        int         mcsum = 0;

        for (int i = 0; i < segments.length; ++i) {
            if (segments[i].count != 0) {
                return false;
            } else {
                mcsum += mc[i] = segments[i].modCount;
            }
        }

        // If mcsum happens to be zero, then we know we got a snapshot
        // before any modifications at all were made.  This is
        // probably common enough to bother tracking.
        if (mcsum != 0) {
            for (int i = 0; i < segments.length; ++i) {
                if ((segments[i].count != 0) || (mc[i] != segments[i].modCount)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping
     * for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code key.equals(k)}, then this method returns {@code v};
     * otherwise it returns
     * {@code null}. (There can be at most one such mapping.)
     *
     * @param key the key
     * @return the v
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V get(Object key) {
        final int hash = hashOf(key);

        return segmentFor(hash).get(key, hash);
    }

    //~--- inner interfaces ----------------------------------------------------

    /**
     * The Interface KeyReference.
     */

    /*
     * ---------------- Inner Classes --------------
     */
    static interface KeyReference {
        /**
         * Key hash.
         *
         * @return the int
         */
        int keyHash();

        /**
         * Key ref.
         *
         * @return the object
         */
        Object keyRef();
    }


    //~--- inner classes -------------------------------------------------------

    /**
     * The Class EntryIterator.
     */
    final class EntryIterator
            extends HashIterator
            implements Iterator<Entry<K, V>> {
        /**
         * Next.
         *
         * @return the map. entry
         */
        @Override
        public Map.Entry<K, V> next() {
            final HashEntry<K, V> e = super.nextEntry();

            return new WriteThroughEntry(e.key(), e.value());
        }
    }


    /**
     * The Class EntrySet.
     */
    final class EntrySet
            extends AbstractSet<Map.Entry<K, V>> {
        /**
         * Clear.
         */
        @Override
        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }

        /**
         * Contains.
         *
         * @param o the o
         * @return true, if successful
         */
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof final Entry<?, ?> e)) {
                return false;
            }

            final V               v = ConcurrentReferenceHashMap.this.get(e.getKey());

            return (v != null) && v.equals(e.getValue());
        }

        /**
         * Iterator.
         *
         * @return the iterator
         */
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        /**
         * Removes the.
         *
         * @param o the o
         * @return true, if successful
         */
        @Override
        public boolean remove(Object o) {
            if (!(o instanceof final Entry<?, ?> e)) {
                return false;
            }

            return ConcurrentReferenceHashMap.this.remove(e.getKey(), e.getValue());
        }

        /**
         * Size.
         *
         * @return the int
         */
        @Override
        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        //~--- get methods ------------------------------------------------------

        /**
         * Checks if empty.
         *
         * @return true, if empty
         */
        @Override
        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }
    }


    /**
     * ConcurrentReferenceHashMap list entry. Note that this is never exported out as a user-visible
     * Map.Entry.
     *
     * Because the value field is volatile, not final, it is legal wrt the Java Memory Model for an
     * unsynchronized reader to see null instead of initial value when read via a data race. Although a
     * reordering leading to this is not likely to ever actually occur, the Segment.readValueUnderLock method
     * is used as a backup in case a null (pre-initialized) value is ever seen in an unsynchronized access
     * method.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    static final class HashEntry<K, V> {
        /** The key ref. */
        final Object keyRef;

        /** The hash. */
        final int hash;

        /** The value ref. */
        volatile Object valueRef;

        /** The next. */
        final HashEntry<K, V> next;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new hash entry.
         *
         * @param key the key
         * @param hash the hash
         * @param next the next
         * @param value the value
         * @param keyType the key type
         * @param valueType the value type
         * @param refQueue the ref queue
         */
        HashEntry(K key,
                  int hash,
                  HashEntry<K, V> next,
                  V value,
                  ReferenceType keyType,
                  ReferenceType valueType,
                  ReferenceQueue<Object> refQueue) {
            this.hash     = hash;
            this.next     = next;
            this.keyRef   = newKeyReference(key, keyType, refQueue);
            this.valueRef = newValueReference(value, valueType, refQueue);
        }

        //~--- methods ----------------------------------------------------------

        /**
         * Dereference value.
         *
         * @param value the value
         * @return the v
         */
        @SuppressWarnings("unchecked")
        final V dereferenceValue(Object value) {
            if (value instanceof KeyReference) {
                return ((Reference<V>) value).get();
            }

            return (V) value;
        }

        /**
         * Key.
         *
         * @return the k
         */
        @SuppressWarnings("unchecked")
        final K key() {
            if (this.keyRef instanceof KeyReference) {
                return ((Reference<K>) this.keyRef).get();
            }

            return (K) this.keyRef;
        }

        /**
         * New array.
         *
         * @param <K> the key type
         * @param <V> the value type
         * @param i the i
         * @return the hash entry[]
         */
        @SuppressWarnings("unchecked")
        static final <K, V> HashEntry<K, V>[] newArray(int i) {
            return new HashEntry[i];
        }

        /**
         * New key reference.
         *
         * @param key the key
         * @param keyType the key type
         * @param refQueue the ref queue
         * @return the object
         */
        final Object newKeyReference(K key, ReferenceType keyType, ReferenceQueue<Object> refQueue) {
            if (keyType == ReferenceType.WEAK) {
                return new WeakKeyReference<>(key, this.hash, refQueue);
            }

            if (keyType == ReferenceType.SOFT) {
                return new SoftKeyReference<>(key, this.hash, refQueue);
            }

            return key;
        }

        /**
         * New value reference.
         *
         * @param value the value
         * @param valueType the value type
         * @param refQueue the ref queue
         * @return the object
         */
        final Object newValueReference(V value, ReferenceType valueType, ReferenceQueue<Object> refQueue) {
            if (valueType == ReferenceType.WEAK) {
                return new WeakValueReference<>(value, this.keyRef, this.hash, refQueue);
            }

            if (valueType == ReferenceType.SOFT) {
                return new SoftValueReference<>(value, this.keyRef, this.hash, refQueue);
            }

            return value;
        }

        /**
         * Value.
         *
         * @return the v
         */
        final V value() {
            return dereferenceValue(this.valueRef);
        }

        //~--- set methods ------------------------------------------------------

        /**
         * Set value.
         *
         * @param value the value
         * @param valueType the value type
         * @param refQueue the ref queue
         */
        final void setValue(V value, ReferenceType valueType, ReferenceQueue<Object> refQueue) {
            this.valueRef = newValueReference(value, valueType, refQueue);
        }
    }


    /**
     * The Class HashIterator.
     */

    /*
     * ---------------- Iterator Support --------------
     */
    abstract class HashIterator {
        /** The next segment index. */
        int nextSegmentIndex;

        /** The next table index. */
        int nextTableIndex;

        /** The current table. */
        HashEntry<K, V>[] currentTable;

        /** The next entry. */
        HashEntry<K, V> nextEntry;

        /** The last returned. */
        HashEntry<K, V> lastReturned;

        /** The current key. */
        K currentKey;  // Strong reference to weak key (prevents gc)

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new hash iterator.
         */
        HashIterator() {
            this.nextSegmentIndex = ConcurrentReferenceHashMap.this.segments.length - 1;
            this.nextTableIndex   = -1;
            advance();
        }

        //~--- methods ----------------------------------------------------------

        /**
         * Removes the.
         */
        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            }

            ConcurrentReferenceHashMap.this.remove(this.currentKey);
            this.lastReturned = null;
        }

        /**
         * Advance.
         */
        final void advance() {
            if ((this.nextEntry != null) && (this.nextEntry = this.nextEntry.next) != null) {
                return;
            }

            while (this.nextTableIndex >= 0) {
                if ((this.nextEntry = this.currentTable[this.nextTableIndex--]) != null) {
                    return;
                }
            }

            while (this.nextSegmentIndex >= 0) {
                final Segment<K, V> seg = ConcurrentReferenceHashMap.this.segments[this.nextSegmentIndex--];

                if (seg.count != 0) {
                    this.currentTable = seg.table;

                    for (int j = this.currentTable.length - 1; j >= 0; --j) {
                        if ((this.nextEntry = this.currentTable[j]) != null) {
                            this.nextTableIndex = j - 1;
                            return;
                        }
                    }
                }
            }
        }

        /**
         * Next entry.
         *
         * @return the hash entry
         */
        HashEntry<K, V> nextEntry() {
            do {
                if (this.nextEntry == null) {
                    throw new NoSuchElementException();
                }

                this.lastReturned = this.nextEntry;
                this.currentKey   = this.lastReturned.key();
                advance();
            } while (this.currentKey == null);  // Skip GC'd keys

            return this.lastReturned;
        }

        //~--- get methods ------------------------------------------------------

        /**
         * Checks for more elements.
         *
         * @return true, if successful
         */
        public boolean hasMoreElements() {
            return hasNext();
        }

        /**
         * Checks for next.
         *
         * @return true, if successful
         */
        public boolean hasNext() {
            while (this.nextEntry != null) {
                if (this.nextEntry.key() != null) {
                    return true;
                }

                advance();
            }

            return false;
        }
    }


    /**
     * The Class KeyIterator.
     */
    final class KeyIterator
            extends HashIterator
            implements Iterator<K>, Enumeration<K> {
        /**
         * Next.
         *
         * @return the k
         */
        @Override
        public K next() {
            return super.nextEntry()
                    .key();
        }

        /**
         * Next element.
         *
         * @return the k
         */
        @Override
        public K nextElement() {
            return super.nextEntry()
                    .key();
        }
    }


    /**
     * The Class KeySet.
     */
    final class KeySet
            extends AbstractSet<K> {
        /**
         * Clear.
         */
        @Override
        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }

        /**
         * Contains.
         *
         * @param o the o
         * @return true, if successful
         */
        @Override
        public boolean contains(Object o) {
            return ConcurrentReferenceHashMap.this.containsKey(o);
        }

        /**
         * Iterator.
         *
         * @return the iterator
         */
        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        /**
         * Removes the.
         *
         * @param o the o
         * @return true, if successful
         */
        @Override
        public boolean remove(Object o) {
            return ConcurrentReferenceHashMap.this.remove(o) != null;
        }

        /**
         * Size.
         *
         * @return the int
         */
        @Override
        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        //~--- get methods ------------------------------------------------------

        /**
         * Checks if empty.
         *
         * @return true, if empty
         */
        @Override
        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }
    }


    /**
     * Segments are specialized versions of hash tables. This subclasses from ReentrantLock opportunistically,
     * just to simplify some locking and avoid separate construction.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    static final class Segment<K, V>
            extends ReentrantLock
            implements Serializable {
        /** The Constant serialVersionUID. */

        /*
         * Segments maintain a table of entry lists that are ALWAYS kept in a consistent state, so can be read
         * without locking. Next fields of nodes are immutable (final). All list additions are performed at
         * the front of each bin. This makes it easy to check changes, and also fast to traverse. When nodes
         * would otherwise be changed, new nodes are created to replace them. This works well for hash tables
         * since the bin lists tend to be short. (The average length is less than two for the default load
         * factor threshold.)
         *
         * Read operations can thus proceed without locking, but rely on selected uses of volatiles to ensure
         * that completed write operations performed by other threads are noticed. For most purposes, the
         * "count" field, tracking the number of elements, serves as that volatile variable ensuring
         * visibility. This is convenient because this field needs to be read in many read operations anyway:
         *
         * - All (unsynchronized) read operations must first read the "count" field, and should not look at
         * table entries if it is 0.
         *
         * - All (synchronized) write operations should write to the "count" field after structurally changing
         * any bin. The operations must not take any action that could even momentarily cause a concurrent
         * read operation to see inconsistent data. This is made easier by the nature of the read operations
         * in Map. For example, no operation can reveal that the table has grown but the threshold has not yet
         * been updated, so there are no atomicity requirements for this with respect to reads.
         *
         * As a guide, all critical volatile reads and writes to the count field are marked in code comments.
         */
        private static final long serialVersionUID = 2249069246763182397L;

        //~--- fields -----------------------------------------------------------

        /**
         * The number of elements in this segment's region.
         */
        transient volatile int count;

        /**
         * Number of updates that alter the size of the table. This is used during bulk-read methods to make
         * sure they see a consistent snapshot: If modCounts change during a traversal of segments computing
         * size or checking containsValue, then we might have an inconsistent view of state so (usually) must
         * retry.
         */
        transient int modCount;

        /**
         * The table is rehashed when its size exceeds this threshold. (The value of this field is always
         * {@code (int)(capacity * loadFactor)}.)
         */
        transient int threshold;

        /**
         * The per-segment table.
         */
        transient volatile HashEntry<K, V>[] table;

        /**
         * The load factor for the hash table. Even though this value is same for all segments, it is
         * replicated to avoid needing links to outer object.
         *
         * @serial
         */
        final float loadFactor;

        /**
         * The collected weak-key reference queue for this segment. This should be (re)initialized whenever
         * table is assigned,
         */
        transient volatile ReferenceQueue<Object> refQueue;

        /** The key type. */
        final ReferenceType keyType;

        /** The value type. */
        final ReferenceType valueType;

        /** The identity comparisons. */
        final boolean identityComparisons;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new segment.
         *
         * @param initialCapacity the initial capacity
         * @param lf the lf
         * @param keyType the key type
         * @param valueType the value type
         * @param identityComparisons the identity comparisons
         */
        Segment(int initialCapacity,
                float lf,
                ReferenceType keyType,
                ReferenceType valueType,
                boolean identityComparisons) {
            this.loadFactor          = lf;
            this.keyType             = keyType;
            this.valueType           = valueType;
            this.identityComparisons = identityComparisons;
            setTable(HashEntry.<K, V>newArray(initialCapacity));
        }

        //~--- methods ----------------------------------------------------------

        /**
         * Clear.
         */
        void clear() {
            if (this.count != 0) {
                lock();

                try {
                    final HashEntry<K, V>[] tab = this.table;

                    for (int i = 0; i < tab.length; i++) {
                        tab[i] = null;
                    }

                    ++this.modCount;

                    // replace the reference queue to avoid unnecessary stale cleanups
                    this.refQueue = new ReferenceQueue<>();
                    this.count    = 0;  // write-volatile
                } finally {
                    unlock();
                }
            }
        }

        /**
         * Contains key.
         *
         * @param key the key
         * @param hash the hash
         * @return true, if successful
         */
        boolean containsKey(Object key, int hash) {
            if (this.count != 0) {  // read-volatile
                HashEntry<K, V> e = getFirst(hash);

                while (e != null) {
                    if ((e.hash == hash) && keyEq(key, e.key())) {
                        return true;
                    }

                    e = e.next;
                }
            }

            return false;
        }

        /**
         * Contains value.
         *
         * @param value the value
         * @return true, if successful
         */
        boolean containsValue(Object value) {
            if (this.count != 0) {                  // read-volatile
                final HashEntry<K, V>[] tab = this.table;
                final int               len = tab.length;

                for (int i = 0; i < len; i++) {
                    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                        final Object opaque = e.valueRef;
                        V            v;

                        if (opaque == null) {
                            v = readValueUnderLock(e);  // recheck
                        } else {
                            v = e.dereferenceValue(opaque);
                        }

                        if (value.equals(v)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        /**
         * New array.
         *
         * @param <K> the key type
         * @param <V> the value type
         * @param i the i
         * @return the segment[]
         */
        @SuppressWarnings("unchecked")
        static final <K, V> Segment<K, V>[] newArray(int i) {
            return new Segment[i];
        }

        /**
         * New hash entry.
         *
         * @param key the key
         * @param hash the hash
         * @param next the next
         * @param value the value
         * @return the hash entry
         */
        HashEntry<K, V> newHashEntry(K key, int hash, HashEntry<K, V> next, V value) {
            return new HashEntry<>(key, hash, next, value, this.keyType, this.valueType, this.refQueue);
        }

        /**
         * Put.
         *
         * @param key the key
         * @param hash the hash
         * @param value the value
         * @param onlyIfAbsent the only if absent
         * @return the v
         */
        V put(K key, int hash, V value, boolean onlyIfAbsent) {
            lock();

            try {
                removeStale();

                int c = this.count;

                if (c++ > this.threshold) {             // ensure capacity
                    final int reduced = rehash();

                    if (reduced > 0)                     // adjust from possible weak cleanups
                    {
                        this.count = (c -= reduced) - 1;  // write-volatile
                    }
                }

                final HashEntry<K, V>[] tab   = this.table;
                final int               index = hash & (tab.length - 1);
                final HashEntry<K, V>   first = tab[index];
                HashEntry<K, V>         e     = first;

                while ((e != null) && ((e.hash != hash) ||!keyEq(key, e.key()))) {
                    e = e.next;
                }

                V oldValue;

                if (e != null) {
                    oldValue = e.value();

                    if (!onlyIfAbsent) {
                        e.setValue(value, this.valueType, this.refQueue);
                    }
                } else {
                    oldValue = null;
                    ++this.modCount;
                    tab[index] = newHashEntry(key, hash, first, value);
                    this.count = c;                      // write-volatile
                }

                return oldValue;
            } finally {
                unlock();
            }
        }

        /**
         * Reads value field of an entry under lock. Called if value field ever appears to be null. This is
         * possible only if a compiler happens to reorder a HashEntry initialization with its table
         * assignment, which is legal under memory model but is not known to ever occur.
         *
         * @param e the e
         * @return the v
         */
        V readValueUnderLock(HashEntry<K, V> e) {
            lock();

            try {
                removeStale();
                return e.value();
            } finally {
                unlock();
            }
        }

        /**
         * Rehash.
         *
         * @return the int
         */
        int rehash() {
            final HashEntry<K, V>[] oldTable    = this.table;
            final int               oldCapacity = oldTable.length;

            if (oldCapacity >= MAXIMUM_CAPACITY) {
                return 0;
            }

            /*
             * Reclassify nodes in each list to new Map. Because we are using power-of-two expansion, the
             * elements from each bin must either stay at same index, or move with a power of two offset. We
             * eliminate unnecessary node creation by catching cases where old nodes can be reused because
             * their next fields won't change. Statistically, at the default threshold, only about one-sixth
             * of them need cloning when a table doubles. The nodes they replace will be garbage collectable
             * as soon as they are no longer referenced by any reader thread that may be in the midst of
             * traversing table right now.
             */
            final HashEntry<K, V>[] newTable = HashEntry.newArray(oldCapacity << 1);

            this.threshold = (int) (newTable.length * this.loadFactor);

            final int sizeMask = newTable.length - 1;
            int       reduce   = 0;

            for (int i = 0; i < oldCapacity; i++) {
                // We need to guarantee that any existing reads of old Map can
                // proceed. So we cannot yet null out each bin.
                final HashEntry<K, V> e = oldTable[i];

                if (e != null) {
                    final HashEntry<K, V> next = e.next;
                    final int             idx  = e.hash & sizeMask;

                    // Single node on list
                    if (next == null) {
                        newTable[idx] = e;
                    } else {
                        // Reuse trailing consecutive sequence at same slot
                        HashEntry<K, V> lastRun = e;
                        int             lastIdx = idx;

                        for (HashEntry<K, V> last = next; last != null; last = last.next) {
                            final int k = last.hash & sizeMask;

                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }

                        newTable[lastIdx] = lastRun;

                        // Clone all remaining nodes
                        for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
                            // Skip GC'd weak refs
                            final K key = p.key();

                            if (key == null) {
                                reduce++;
                                continue;
                            }

                            final int             k = p.hash & sizeMask;
                            final HashEntry<K, V> n = newTable[k];

                            newTable[k] = newHashEntry(key, p.hash, n, p.value());
                        }
                    }
                }
            }

            this.table = newTable;
            return reduce;
        }

        /**
         * Remove; match on key only if value null, else match both.
         *
         * @param key the key
         * @param hash the hash
         * @param value the value
         * @param refRemove the ref remove
         * @return the v
         */
        V remove(Object key, int hash, Object value, boolean refRemove) {
            lock();

            try {
                if (!refRemove) {
                    removeStale();
                }

                int                     c     = this.count - 1;
                final HashEntry<K, V>[] tab   = this.table;
                final int               index = hash & (tab.length - 1);
                final HashEntry<K, V>   first = tab[index];
                HashEntry<K, V>         e     = first;

                // a ref remove operation compares the Reference instance
                while ((e != null) && (key != e.keyRef) && (refRemove || (hash != e.hash) ||!keyEq(key, e.key()))) {
                    e = e.next;
                }

                V oldValue = null;

                if (e != null) {
                    final V v = e.value();

                    if ((value == null) || value.equals(v)) {
                        oldValue = v;

                        // All entries following removed node can stay
                        // in list, but all preceding ones need to be
                        // cloned.
                        ++this.modCount;

                        HashEntry<K, V> newFirst = e.next;

                        for (HashEntry<K, V> p = first; p != e; p = p.next) {
                            final K pKey = p.key();

                            if (pKey == null) {  // Skip GC'd keys
                                c--;
                                continue;
                            }

                            newFirst = newHashEntry(pKey, p.hash, newFirst, p.value());
                        }

                        tab[index] = newFirst;
                        this.count = c;         // write-volatile
                    }
                }

                return oldValue;
            } finally {
                unlock();
            }
        }

        /**
         * Removes the stale.
         */
        final void removeStale() {
            KeyReference ref;

            while ((ref = (KeyReference) this.refQueue.poll()) != null) {
                remove(ref.keyRef(), ref.keyHash(), null, true);
            }
        }

        /**
         * Replace.
         *
         * @param key the key
         * @param hash the hash
         * @param newValue the new value
         * @return the v
         */
        V replace(K key, int hash, V newValue) {
            lock();

            try {
                removeStale();

                HashEntry<K, V> e = getFirst(hash);

                while ((e != null) && ((e.hash != hash) ||!keyEq(key, e.key()))) {
                    e = e.next;
                }

                V oldValue = null;

                if (e != null) {
                    oldValue = e.value();
                    e.setValue(newValue, this.valueType, this.refQueue);
                }

                return oldValue;
            } finally {
                unlock();
            }
        }

        /**
         * Replace.
         *
         * @param key the key
         * @param hash the hash
         * @param oldValue the old value
         * @param newValue the new value
         * @return true, if successful
         */
        boolean replace(K key, int hash, V oldValue, V newValue) {
            lock();

            try {
                removeStale();

                HashEntry<K, V> e = getFirst(hash);

                while ((e != null) && ((e.hash != hash) ||!keyEq(key, e.key()))) {
                    e = e.next;
                }

                boolean replaced = false;

                if ((e != null) && oldValue.equals(e.value())) {
                    replaced = true;
                    e.setValue(newValue, this.valueType, this.refQueue);
                }

                return replaced;
            } finally {
                unlock();
            }
        }

        /**
         * Key eq.
         *
         * @param src the src
         * @param dest the dest
         * @return true, if successful
         */
        private boolean keyEq(Object src, Object dest) {
            return this.identityComparisons ? src == dest
                    : src.equals(dest);
        }

        //~--- get methods ------------------------------------------------------

        /**
         * Returns properly casted first entry of bin for given hash.
         *
         * @param hash the hash
         * @return the first
         */
        HashEntry<K, V> getFirst(int hash) {
            final HashEntry<K, V>[] tab = this.table;

            return tab[hash & (tab.length - 1)];
        }

        /**
         * Gets the.
         *
         * @param key the key
         * @param hash the hash
         * @return the v
         */

        /*
         * Specialized implementations of map methods
         */
        V get(Object key, int hash) {
            if (this.count != 0) {                  // read-volatile
                HashEntry<K, V> e = getFirst(hash);

                while (e != null) {
                    if ((e.hash == hash) && keyEq(key, e.key())) {
                        final Object opaque = e.valueRef;

                        if (opaque != null) {
                            return e.dereferenceValue(opaque);
                        }

                        return readValueUnderLock(e);  // recheck
                    }

                    e = e.next;
                }
            }

            return null;
        }

        //~--- set methods ------------------------------------------------------

        /**
         * Sets table to new HashEntry array. Call only while holding lock or in constructor.
         *
         * @param newTable the new per-segment table
         */
        void setTable(HashEntry<K, V>[] newTable) {
            this.threshold = (int) (newTable.length * this.loadFactor);
            this.table     = newTable;
            this.refQueue  = new ReferenceQueue<>();
        }
    }


    /**
     * The Class SimpleEntry.
     *
     * @param <K> the key type
     * @param <V> the value type
     */

    /*
     * This class is needed for JDK5 compatibility.
     */
    static class SimpleEntry<K, V>
            implements Entry<K, V>, java.io.Serializable {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -8499721149061103585L;

        //~--- fields -----------------------------------------------------------

        /** The key. */
        private final K key;

        /** The value. */
        private V value;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new simple entry.
         *
         * @param entry the entry
         */
        public SimpleEntry(Entry<? extends K, ? extends V> entry) {
            this.key   = entry.getKey();
            this.value = entry.getValue();
        }

        /**
         * Instantiates a new simple entry.
         *
         * @param key the key
         * @param value the value
         */
        public SimpleEntry(K key, V value) {
            this.key   = key;
            this.value = value;
        }

        //~--- methods ----------------------------------------------------------

        /**
         * Equals.
         *
         * @param o the o
         * @return true, if successful
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof final Entry<?, ?> e)) {
                return false;
            }

            return eq(this.key, e.getKey()) && eq(this.value, e.getValue());
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return ((this.key == null) ? 0
                    : this.key.hashCode()) ^ ((this.value == null) ? 0
                    : this.value.hashCode());
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return this.key + "=" + this.value;
        }

        /**
         * Eq.
         *
         * @param o1 the o 1
         * @param o2 the o 2
         * @return true, if successful
         */
        private static boolean eq(Object o1, Object o2) {
            return (o1 == null) ? o2 == null
                    : o1.equals(o2);
        }

        //~--- get methods ------------------------------------------------------

        /**
         * Gets the key.
         *
         * @return the key
         */
        @Override
        public K getKey() {
            return this.key;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        @Override
        public V getValue() {
            return this.value;
        }

        //~--- set methods ------------------------------------------------------

        /**
         * Set value.
         *
         * @param value the value
         * @return the v
         */
        @Override
        public V setValue(V value) {
            final V oldValue = this.value;

            this.value = value;
            return oldValue;
        }
    }


    /**
     * A soft-key reference which stores the key hash needed for reclamation.
     *
     * @param <K> the key type
     */
    static final class SoftKeyReference<K>
            extends SoftReference<K>
            implements KeyReference {
        /** The hash. */
        final int hash;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new soft key reference.
         *
         * @param key the key
         * @param hash the hash
         * @param refQueue the ref queue
         */
        SoftKeyReference(K key, int hash, ReferenceQueue<Object> refQueue) {
            super(key, refQueue);
            this.hash = hash;
        }

        //~--- methods ----------------------------------------------------------

        /**
         * Key hash.
         *
         * @return the int
         */
        @Override
        public final int keyHash() {
            return this.hash;
        }

        /**
         * Key ref.
         *
         * @return the object
         */
        @Override
        public final Object keyRef() {
            return this;
        }
    }


    /**
     * The Class SoftValueReference.
     *
     * @param <V> the value type
     */
    static final class SoftValueReference<V>
            extends SoftReference<V>
            implements KeyReference {
        /** The key ref. */
        final Object keyRef;

        /** The hash. */
        final int hash;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new soft value reference.
         *
         * @param value the value
         * @param keyRef the key ref
         * @param hash the hash
         * @param refQueue the ref queue
         */
        SoftValueReference(V value, Object keyRef, int hash, ReferenceQueue<Object> refQueue) {
            super(value, refQueue);
            this.keyRef = keyRef;
            this.hash   = hash;
        }

        //~--- methods ----------------------------------------------------------

        /**
         * Key hash.
         *
         * @return the int
         */
        @Override
        public final int keyHash() {
            return this.hash;
        }

        /**
         * Key ref.
         *
         * @return the object
         */
        @Override
        public final Object keyRef() {
            return this.keyRef;
        }
    }


    /**
     * The Class ValueIterator.
     */
    final class ValueIterator
            extends HashIterator
            implements Iterator<V>, Enumeration<V> {
        /**
         * Next.
         *
         * @return the v
         */
        @Override
        public V next() {
            return super.nextEntry()
                    .value();
        }

        /**
         * Next element.
         *
         * @return the v
         */
        @Override
        public V nextElement() {
            return super.nextEntry()
                    .value();
        }
    }


    /**
     * The Class Values.
     */
    final class Values
            extends AbstractCollection<V> {
        /**
         * Clear.
         */
        @Override
        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }

        /**
         * Contains.
         *
         * @param o the o
         * @return true, if successful
         */
        @Override
        public boolean contains(Object o) {
            return ConcurrentReferenceHashMap.this.containsValue(o);
        }

        /**
         * Iterator.
         *
         * @return the iterator
         */
        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        /**
         * Size.
         *
         * @return the int
         */
        @Override
        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        //~--- get methods ------------------------------------------------------

        /**
         * Checks if empty.
         *
         * @return true, if empty
         */
        @Override
        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }
    }


    /**
     * A weak-key reference which stores the key hash needed for reclamation.
     *
     * @param <K> the key type
     */
    static final class WeakKeyReference<K>
            extends WeakReference<K>
            implements KeyReference {
        /** The hash. */
        final int hash;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new weak key reference.
         *
         * @param key the key
         * @param hash the hash
         * @param refQueue the ref queue
         */
        WeakKeyReference(K key, int hash, ReferenceQueue<Object> refQueue) {
            super(key, refQueue);
            this.hash = hash;
        }

        //~--- methods ----------------------------------------------------------

        /**
         * Key hash.
         *
         * @return the int
         */
        @Override
        public final int keyHash() {
            return this.hash;
        }

        /**
         * Key ref.
         *
         * @return the object
         */
        @Override
        public final Object keyRef() {
            return this;
        }
    }


    /**
     * The Class WeakValueReference.
     *
     * @param <V> the value type
     */
    static final class WeakValueReference<V>
            extends WeakReference<V>
            implements KeyReference {
        /** The key ref. */
        final Object keyRef;

        /** The hash. */
        final int hash;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new weak value reference.
         *
         * @param value the value
         * @param keyRef the key ref
         * @param hash the hash
         * @param refQueue the ref queue
         */
        WeakValueReference(V value, Object keyRef, int hash, ReferenceQueue<Object> refQueue) {
            super(value, refQueue);
            this.keyRef = keyRef;
            this.hash   = hash;
        }

        //~--- methods ----------------------------------------------------------

        /**
         * Key hash.
         *
         * @return the int
         */
        @Override
        public final int keyHash() {
            return this.hash;
        }

        /**
         * Key ref.
         *
         * @return the object
         */
        @Override
        public final Object keyRef() {
            return this.keyRef;
        }
    }


    /**
     * Custom Entry class used by EntryIterator.next(), that relays setValue changes to the underlying map.
     */
    final class WriteThroughEntry
            extends SimpleEntry<K, V> {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -7900634345345313646L;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new write through entry.
         *
         * @param k the k
         * @param v the v
         */
        WriteThroughEntry(K k, V v) {
            super(k, v);
        }

        //~--- set methods ------------------------------------------------------

        /**
         * Set our entry's value and write through to the map. The value to return is somewhat arbitrary here.
         * Since a WriteThroughEntry does not necessarily track asynchronous changes, the most recent
         * "previous" value could be different from what we return (or could even have been removed in which
         * case the put will re-establish). We do not and cannot guarantee more.
         *
         * @param value the value
         * @return the v
         */
        @Override
        public V setValue(V value) {
            if (value == null) {
                throw new NullPointerException();
            }

            final V v = super.setValue(value);

            ConcurrentReferenceHashMap.this.put(getKey(), value);
            return v;
        }
    }
}

