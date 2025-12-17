package dev.ikm.komet.framework.observable.collection;

import javafx.collections.ModifiableObservableListBase;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;

/**
 * An observable list implementation that stores primitive int values using Eclipse Collections' 
 * {@link MutableIntList} as the backing collection, while providing JavaFX observable capabilities
 * through the {@link ModifiableObservableListBase} abstraction.
 * 
 * <h2>Memory and Performance Benefits</h2>
 * 
 * <p>This implementation provides significant advantages over using {@code ObservableList<Integer>} 
 * with boxed {@code Integer} objects from standard Java Collections:</p>
 * 
 * <p><b>Memory Efficiency</b>
 * <ul>
 *   <li><strong>Object Overhead Elimination:</strong> Each boxed {@code Integer} object in a standard 
 *       {@code List<Integer>} requires approximately 16-24 bytes of memory (12-16 bytes for object 
 *       header + 4 bytes for the int value + padding). In contrast, a primitive int array stores 
 *       values directly as 4 bytes each, resulting in <strong>4-6x memory savings</strong>.</li>
 *   
 *   <li><strong>Pointer Indirection Removal:</strong> Standard collections store references (8 bytes 
 *       on 64-bit JVMs) to Integer objects, which themselves contain the int value. This implementation 
 *       stores primitives directly in contiguous memory, eliminating pointer indirection and improving 
 *       CPU cache locality.</li>
 *   
 *   <li><strong>Example:</strong> A list of 1,000,000 integers:
 *       <ul>
 *         <li>{@code List<Integer>}: ~28-32 MB (24 bytes per object + 8 bytes per reference)</li>
 *         <li>{@code ObservableIntList}: ~4 MB (4 bytes per primitive)</li>
 *         <li><strong>Memory Savings: 85-87%</strong></li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <p><b>Garbage Collection Benefits</b>
 * <ul>
 *   <li><strong>Reduced GC Pressure:</strong> Boxing operations create temporary {@code Integer} objects 
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
 * <p><b>Performance Characteristics</b>
 * <ul>
 *   <li><strong>CPU Cache Efficiency:</strong> Primitive arrays have excellent spatial locality, allowing 
 *       CPUs to prefetch and cache data more effectively compared to pointer-chasing through boxed objects.</li>
 *   
 *   <li><strong>Autoboxing Cost Elimination:</strong> Every get/set operation on {@code List<Integer>} 
 *       incurs boxing/unboxing overhead. This implementation avoids that entirely.</li>
 *   
 *   <li><strong>Bulk Operations:</strong> Eclipse Collections provides highly optimized primitive-aware 
 *       bulk operations (forEach, select, collect, etc.) that operate directly on primitive arrays.</li>
 * </ul>
 * 
 * <p><b>Use Cases</b>
 * <p>This implementation is particularly beneficial for:</p>
 * <ul>
 *   <li>Large collections of numeric identifiers or keys</li>
 *   <li>Performance-critical code with frequent list access or modification</li>
 *   <li>Memory-constrained environments or applications managing many collections</li>
 *   <li>Real-time applications sensitive to GC pauses</li>
 *   <li>Data processing pipelines with high allocation rates</li>
 * </ul>
 * 
 * @see ModifiableObservableListBase
 * @see MutableIntList
 * @see <a href="https://eclipse.dev/collections/">Eclipse Collections Documentation</a>
 * @see <a href="https://openjfx.io/javadoc/17/javafx.base/javafx/collections/ModifiableObservableListBase.html">JavaFX ModifiableObservableListBase</a>
 */
public class ObservableIntList extends ModifiableObservableListBase<Integer> {
    /**
     * Maximum array size to prevent OutOfMemoryError and VM limit errors.
     * Some VMs reserve header words in arrays, so we use Integer.MAX_VALUE - 8
     * as a safe maximum, similar to ArrayList.
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private final MutableIntList backingList;
    
    /**
     * Creates an empty ObservableIntList.
     */
    public ObservableIntList() {
        this.backingList = IntLists.mutable.empty();
    }
    
    /**
     * Creates an ObservableIntList with the specified initial capacity.
     * <p>
     * <b>Important:</b> This creates an <i>empty</i> list with pre-allocated storage capacity.
     * To create a list with a single value, use {@code new ObservableIntList(new int[]{value})}
     * or {@code new ObservableIntList(value1, value2, ...)} for multiple values.
     * 
     * @param initialCapacity the initial capacity of the backing list
     * @throws IllegalArgumentException if initialCapacity is negative or exceeds maximum array size
     */
    public ObservableIntList(int initialCapacity) {
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
        this.backingList = IntLists.mutable.withInitialCapacity(initialCapacity);
    }
    
    /**
     * Creates an ObservableIntList containing the specified values.
     * <p>
     * <b>Examples:</b>
     * <ul>
     *   <li>{@code new ObservableIntList()} - empty list</li>
     *   <li>{@code new ObservableIntList(42)} - <b>CREATES EMPTY LIST WITH CAPACITY 42</b></li>
     *   <li>{@code new ObservableIntList(new int[]{42})} - list with single value 42</li>
     *   <li>{@code new ObservableIntList(1, 2, 3)} - list with values [1, 2, 3]</li>
     * </ul>
     * <p>
     * <b>⚠️ Important:</b> Due to Java's overload resolution rules, passing a single
     * int value calls the capacity constructor, not this varargs constructor. To create
     * a list with a single value, wrap it in an array: {@code new ObservableIntList(new int[]{value})}
     * 
     * @param values the initial values to populate the list
     */
    public ObservableIntList(int... values) {
        this.backingList = IntLists.mutable.with(values);
    }

    @Override
    public Integer get(int index) {
        return backingList.get(index);
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    protected void doAdd(int index, Integer element) {
        doAddInt(index, element);
    }

    @Override
    protected Integer doSet(int index, Integer element) {
        return doSetInt(index, element);
    }

    @Override
    protected Integer doRemove(int index) {
        return doRemoveInt(index);
    }
    
    /**
     * Adds a primitive int value at the specified index without boxing.
     * <p>
     * This is the unboxed version of {@link #doAdd(int, Integer)}, avoiding the creation
     * of temporary Integer wrapper objects. Use this method when working with primitive values
     * for maximum performance.
     * 
     * @param index the index at which to insert the value
     * @param value the primitive int value to add
     */
    public void doAddInt(int index, int value) {
        backingList.addAtIndex(index, value);
    }
    
    /**
     * Sets the primitive int value at the specified index without boxing, returning the old value.
     * <p>
     * This is the unboxed version of {@link #doSet(int, Integer)}, avoiding the creation
     * of temporary Integer wrapper objects. Use this method when working with primitive values
     * for maximum performance.
     * 
     * @param index the index at which to set the value
     * @param value the primitive int value to set
     * @return the previous primitive int value at the specified index
     */
    public int doSetInt(int index, int value) {
        int oldValue = backingList.get(index);
        backingList.set(index, value);
        return oldValue;
    }
    
    /**
     * Removes and returns the primitive int value at the specified index without boxing.
     * <p>
     * This is the unboxed version of {@link #doRemove(int)}, avoiding the creation
     * of temporary Integer wrapper objects. Use this method when working with primitive values
     * for maximum performance.
     * 
     * @param index the index of the element to remove
     * @return the primitive int value that was removed
     */
    public int doRemoveInt(int index) {
        int removed = backingList.get(index);
        backingList.removeAtIndex(index);
        return removed;
    }
    
    /**
     * Provides direct access to the backing Eclipse Collections MutableIntList
     * for primitive operations without boxing overhead.
     * 
     * <p><strong>Warning:</strong> Direct modifications to the backing list will
     * not trigger change notifications to JavaFX listeners. Use this method only
     * for read-only operations or when you can manually call {@link #beginChange()}
     * and {@link #endChange()} to notify listeners.</p>
     * 
     * @return the underlying MutableIntList
     */
    public MutableIntList getBackingList() {
        return backingList;
    }
    
    /**
     * Adds a primitive int value to the end of the list, avoiding boxing.
     * 
     * @param value the primitive int value to add
     * @return true (as specified by Collection.add)
     */
    public boolean addInt(int value) {
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
     * Gets a primitive int value at the specified index, avoiding unboxing.
     * 
     * @param index the index of the element to retrieve
     * @return the primitive int value at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public int getInt(int index) {
        return backingList.get(index);
    }
    
    /**
     * Removes the first occurrence of the specified primitive int value from this list, if present.
     * <p>
     * This method operates directly on primitive values without boxing, providing better performance
     * and eliminating garbage collection pressure compared to {@code remove(Integer.valueOf(value))}.
     * 
     * <p><b>Performance Benefits</b>
     * <ul>
     *   <li><strong>No Boxing:</strong> Avoids creating a temporary {@code Integer} object for comparison</li>
     *   <li><strong>Direct Comparison:</strong> Uses primitive {@code ==} comparison instead of {@code equals()}</li>
     *   <li><strong>Zero GC Pressure:</strong> No wrapper objects are allocated or become garbage</li>
     * </ul>
     * 
     * @param value the primitive int value to remove
     * @return true if the value was found and removed, false otherwise
     */
    public boolean removeInt(int value) {
        int index = backingList.indexOf(value);
        if (index >= 0) {
            remove(index);
            return true;
        }
        return false;
    }

    /**
     * Replaces the entire contents of this list with the specified primitive int array.
     * <p>
     * This is a high-performance bulk operation that:
     * <ul>
     *   <li>Clears the existing list</li>
     *   <li>Adds all new values in one operation</li>
     *   <li>Fires a single change notification to listeners</li>
     * </ul>
     *
     * <p><b>Performance Benefits for Large Datasets:</b>
     * <ul>
     *   <li><strong>Single Change Notification:</strong> Fires only one change event instead of N events</li>
     *   <li><strong>No Boxing:</strong> Works directly with primitive int array</li>
     *   <li><strong>Optimized Memory Copy:</strong> Eclipse Collections can use System.arraycopy internally</li>
     * </ul>
     *
     * @param values the new primitive int values to replace the list contents
     */
    public void setAll(int... values) {
        beginChange();
        try {
            int oldSize = backingList.size();
            // Capture old values before clearing
            MutableIntList oldValues = IntLists.mutable.withAll(backingList);
            
            backingList.clear();
            backingList.addAll(values);

            // Notify listeners of the change
            if (oldValues.notEmpty()) {
                // Convert to java.util.List<Integer>
                java.util.List<Integer> removedList = new java.util.ArrayList<>(oldValues.size());
                oldValues.forEach(removedList::add);
                nextRemove(0, removedList);
            }
            if (values.length > 0) {
                nextAdd(0, values.length);
            }
        } finally {
            endChange();
        }
    }

    /**
     * Replaces the entire contents of this list with values from a MutableIntList.
     * <p>
     * This variant is useful when you have an Eclipse Collections int list that you want
     * to use to replace the observable list's contents.
     *
     * @param values the MutableIntList containing the new values
     */
    public void setAll(ImmutableIntList values) {
        beginChange();
        try {
            // Capture old values before clearing
            MutableIntList oldValues = IntLists.mutable.withAll(backingList);

            backingList.clear();
            backingList.addAll(values);

            // Notify listeners of the change
            if (oldValues.notEmpty()) {
                // Convert to java.util.List<Integer>
                java.util.List<Integer> removedList = new java.util.ArrayList<>(oldValues.size());
                oldValues.forEach(removedList::add);
                nextRemove(0, removedList);
            }
            if (values.notEmpty()) {
                nextAdd(0, values.size());
            }
        } finally {
            endChange();
        }
    }
}

