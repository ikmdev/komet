package dev.ikm.komet.framework.observable.collection;

import javafx.collections.ModifiableObservableListBase;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;

/**
 * An observable list implementation that stores primitive long values using Eclipse Collections'
 * {@link MutableLongList} as the backing collection, while providing JavaFX observable capabilities
 * through the {@link ModifiableObservableListBase} abstraction.
 *
 * <h2>Memory and Performance Benefits</h2>
 *
 * <p>This implementation provides significant advantages over using {@code ObservableList<Long>}
 * with boxed {@code Long} objects from standard Java Collections:</p>
 *
 * <h3>Memory Efficiency</h3>
 * <ul>
 *   <li><strong>Object Overhead Elimination:</strong> Each boxed {@code Long} object in a standard
 *       {@code List<Long>} requires approximately 24-32 bytes of memory (12-16 bytes for object
 *       header + 8 bytes for the long value + padding). In contrast, a primitive long array stores
 *       values directly as 8 bytes each, resulting in <strong>3-4x memory savings</strong>.</li>
 *
 *   <li><strong>Pointer Indirection Removal:</strong> Standard collections store references (8 bytes
 *       on 64-bit JVMs) to Long objects, which themselves contain the long value. This implementation
 *       stores primitives directly in contiguous memory, eliminating pointer indirection and improving
 *       CPU cache locality.</li>
 *
 *   <li><strong>Example:</strong> A list of 1,000,000 longs:
 *       <ul>
 *         <li>{@code List<Long>}: ~40 MB (32 bytes per object + 8 bytes per reference)</li>
 *         <li>{@code ObservableLongList}: ~8 MB (8 bytes per primitive)</li>
 *         <li><strong>Memory Savings: 80%</strong></li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h3>Garbage Collection Benefits</h3>
 * <ul>
 *   <li><strong>Reduced GC Pressure:</strong> Boxing operations create temporary {@code Long} objects
 *       that become garbage immediately after use. For frequently accessed or modified lists, this creates
 *       significant garbage collection overhead. Primitive collections eliminate this entirely.</li>
 *
 *   <li><strong>Fewer GC Pauses:</strong> With fewer objects to track and collect, GC cycles are shorter
 *       and less frequent, leading to more predictable application performance.</li>
 *
 *   <li><strong>Allocation Rate Reduction:</strong> Operations like adding elements, iterating, or
 *       transforming the list don't allocate wrapper objects, drastically reducing the heap allocation
 *       rate and young generation pressure.</li>
 * </ul>
 *
 * <h3>Performance Characteristics</h3>
 * <ul>
 *   <li><strong>CPU Cache Efficiency:</strong> Primitive arrays have excellent spatial locality, allowing
 *       CPUs to prefetch and cache data more effectively compared to pointer-chasing through boxed objects.</li>
 *
 *   <li><strong>Autoboxing Cost Elimination:</strong> Every get/set operation on {@code List<Long>}
 *       incurs boxing/unboxing overhead. This implementation avoids that entirely.</li>
 *
 *   <li><strong>Bulk Operations:</strong> Eclipse Collections provides highly optimized primitive-aware
 *       bulk operations (forEach, select, collect, etc.) that operate directly on primitive arrays.</li>
 * </ul>
 *
 * <h3>Use Cases</h3>
 * <p>This implementation is particularly beneficial for:</p>
 * <ul>
 *   <li>Large collections of numeric identifiers, timestamps, or 64-bit keys</li>
 *   <li>Performance-critical code with frequent list access or modification</li>
 *   <li>Memory-constrained environments or applications managing many collections</li>
 *   <li>Real-time applications sensitive to GC pauses</li>
 *   <li>Data processing pipelines with high allocation rates</li>
 *   <li>Storing native identifiers (NIDs) and database keys in Tinkar/Komet applications</li>
 * </ul>
 *
 * @see ModifiableObservableListBase
 * @see MutableLongList
 * @see <a href="https://eclipse.dev/collections/">Eclipse Collections Documentation</a>
 * @see <a href="https://openjfx.io/javadoc/17/javafx.base/javafx/collections/ModifiableObservableListBase.html">JavaFX ModifiableObservableListBase</a>
 */
public class ObservableLongList extends ModifiableObservableListBase<Long> {
    /**
     * Maximum array size to prevent OutOfMemoryError and VM limit errors.
     * Some VMs reserve header words in arrays, so we use Integer.MAX_VALUE - 8
     * as a safe maximum, similar to ArrayList.
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private final MutableLongList backingList;

    /**
     * Creates an empty ObservableLongList.
     */
    public ObservableLongList() {
        this.backingList = LongLists.mutable.empty();
    }

    /**
     * Creates an ObservableLongList with the specified initial capacity.
     * <p>
     * <b>Important:</b> This creates an <i>empty</i> list with pre-allocated storage capacity.
     * To create a list with a single value, use {@code new ObservableLongList(new long[]{value})}
     * or {@code new ObservableLongList(value1, value2, ...)} for multiple values.
     *
     * @param initialCapacity the initial capacity of the backing list
     * @throws IllegalArgumentException if initialCapacity is negative or exceeds maximum array size
     */
    public ObservableLongList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException(
                "Illegal initial capacity: " + initialCapacity + " (must be >= 0)");
        }
        if (initialCapacity > MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(
                "Requested initial capacity " + initialCapacity +
                " exceeds maximum array size " + MAX_ARRAY_SIZE +
                ". Use a smaller capacity or consider alternative data structures.");
        }
        this.backingList = LongLists.mutable.withInitialCapacity(initialCapacity);
    }

    /**
     * Creates an ObservableLongList containing the specified values.
     * <p>
     * <b>Examples:</b>
     * <ul>
     *   <li>{@code new ObservableLongList()} - empty list</li>
     *   <li>{@code new ObservableLongList(42)} - <b>CREATES EMPTY LIST WITH CAPACITY 42</b></li>
     *   <li>{@code new ObservableLongList(new long[]{42L})} - list with single value 42</li>
     *   <li>{@code new ObservableLongList(1L, 2L, 3L)} - list with values [1, 2, 3]</li>
     * </ul>
     * <p>
     * <b>⚠️ Important:</b> Due to Java's overload resolution rules, passing a single
     * int value calls the capacity constructor, not this varargs constructor. To create
     * a list with a single value, wrap it in an array: {@code new ObservableLongList(new long[]{value})}
     *
     * @param values the initial values to populate the list
     */
    public ObservableLongList(long... values) {
        this.backingList = LongLists.mutable.with(values);
    }

    @Override
    public Long get(int index) {
        return backingList.get(index);
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    protected void doAdd(int index, Long element) {
        doAddLong(index, element);
    }

    @Override
    protected Long doSet(int index, Long element) {
        return doSetLong(index, element);
    }

    @Override
    protected Long doRemove(int index) {
        return doRemoveLong(index);
    }

    /**
     * Adds a primitive long value at the specified index without boxing.
     * <p>
     * This is the unboxed version of {@link #doAdd(int, Long)}, avoiding the creation
     * of temporary Long wrapper objects. Use this method when working with primitive values
     * for maximum performance.
     *
     * @param index the index at which to insert the value
     * @param value the primitive long value to add
     */
    public void doAddLong(int index, long value) {
        backingList.addAtIndex(index, value);
    }

    /**
     * Sets the primitive long value at the specified index without boxing, returning the old value.
     * <p>
     * This is the unboxed version of {@link #doSet(int, Long)}, avoiding the creation
     * of temporary Long wrapper objects. Use this method when working with primitive values
     * for maximum performance.
     *
     * @param index the index at which to set the value
     * @param value the primitive long value to set
     * @return the previous primitive long value at the specified index
     */
    public long doSetLong(int index, long value) {
        long oldValue = backingList.get(index);
        backingList.set(index, value);
        return oldValue;
    }

    /**
     * Removes and returns the primitive long value at the specified index without boxing.
     * <p>
     * This is the unboxed version of {@link #doRemove(int)}, avoiding the creation
     * of temporary Long wrapper objects. Use this method when working with primitive values
     * for maximum performance.
     *
     * @param index the index of the element to remove
     * @return the primitive long value that was removed
     */
    public long doRemoveLong(int index) {
        long removed = backingList.get(index);
        backingList.removeAtIndex(index);
        return removed;
    }

    /**
     * Provides direct access to the backing Eclipse Collections MutableLongList
     * for primitive operations without boxing overhead.
     *
     * <p><strong>Warning:</strong> Direct modifications to the backing list will
     * not trigger change notifications to JavaFX listeners. Use this method only
     * for read-only operations or when you can manually call {@link #beginChange()}
     * and {@link #endChange()} to notify listeners.</p>
     *
     * @return the underlying MutableLongList
     */
    public MutableLongList getBackingList() {
        return backingList;
    }

    /**
     * Adds a primitive long value to the end of the list, avoiding boxing.
     *
     * @param value the primitive long value to add
     * @return true (as specified by Collection.add)
     */
    public boolean addLong(long value) {
        beginChange();
        try {
            backingList.add(value);
            nextAdd(size() - 1, size());
            return true;
        } finally {
            endChange();
        }
    }

    /**
     * Gets a primitive long value at the specified index, avoiding unboxing.
     *
     * @param index the index of the element to retrieve
     * @return the primitive long value at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public long getLong(int index) {
        return backingList.get(index);
    }

    /**
     * Removes the first occurrence of the specified primitive long value from this list, if present.
     * <p>
     * This method operates directly on primitive values without boxing, providing better performance
     * and eliminating garbage collection pressure compared to {@code remove(Long.valueOf(value))}.
     *
     * <h3>Performance Benefits</h3>
     * <ul>
     *   <li><strong>No Boxing:</strong> Avoids creating a temporary {@code Long} object for comparison</li>
     *   <li><strong>Direct Comparison:</strong> Uses primitive {@code ==} comparison instead of {@code equals()}</li>
     *   <li><strong>Zero GC Pressure:</strong> No wrapper objects are allocated or become garbage</li>
     * </ul>
     *
     * @param value the primitive long value to remove
     * @return true if the value was found and removed, false otherwise
     */
    public boolean removeLong(long value) {
        int index = backingList.indexOf(value);
        if (index >= 0) {
            remove(index);
            return true;
        }
        return false;
    }
}
