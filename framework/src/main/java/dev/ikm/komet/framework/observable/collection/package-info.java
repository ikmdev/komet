/**
 * Observable primitive collections combining Eclipse Collections' efficient primitive data structures
 * with JavaFX's observable framework for reactive UI programming.
 *
 * <h2>Overview</h2>
 *
 * <p>This package provides JavaFX observable wrappers around Eclipse Collections' primitive-optimized
 * data structures. These implementations bridge the gap between memory-efficient primitive storage
 * and reactive JavaFX UI bindings, enabling developers to build responsive applications without
 * sacrificing memory efficiency or performance.</p>
 *
 * <h2>Rationale: Why Observable Native Data Structures?</h2>
 *
 * <p>Standard JavaFX collections ({@code ObservableList<T>}) work exclusively with object references,
 * forcing the use of wrapper classes ({@code Integer}, {@code Long}, etc.) when storing primitive values.
 * This design, while flexible, introduces significant overhead in memory-intensive or performance-critical
 * applications:</p>
 *
 * <p><b>1. Memory Overhead Problem</b>
 *
 * <p>Every boxed primitive object carries substantial memory overhead beyond its actual value:</p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse;">
 *   <caption><b>Memory Comparison: Boxed vs. Primitive Storage</b></caption>
 *   <thead>
 *     <tr>
 *       <th>Type</th>
 *       <th>Primitive Size</th>
 *       <th>Boxed Object Size</th>
 *       <th>Reference Size (64-bit JVM)</th>
 *       <th>Total per Element</th>
 *       <th>Overhead Factor</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@code int}</td>
 *       <td>4 bytes</td>
 *       <td>16 bytes (header + value + padding)</td>
 *       <td>8 bytes</td>
 *       <td>24 bytes</td>
 *       <td><b>6x</b></td>
 *     </tr>
 *     <tr>
 *       <td>{@code long}</td>
 *       <td>8 bytes</td>
 *       <td>24 bytes (header + value + padding)</td>
 *       <td>8 bytes</td>
 *       <td>32 bytes</td>
 *       <td><b>4x</b></td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <p><b>Real-World Impact:</b> A JavaFX application tracking 1 million integer identifiers:</p>
 * <ul>
 *   <li>{@code ObservableList<Integer>}: <b>~24-32 MB</b> of heap space</li>
 *   <li>{@code ObservableIntList}: <b>~4 MB</b> of heap space</li>
 *   <li><b>Memory savings: 83-87%</b></li>
 * </ul>
 *
 * <p><b>2. Garbage Collection Pressure</b>
 *
 * <p>Boxing creates temporary objects that quickly become garbage, especially in tight loops
 * or during bulk operations. Consider a simple operation like filtering a list:</p>
 *
 * <pre>{@code
 * // Using ObservableList<Integer> - creates millions of temporary Integer objects
 * ObservableList<Integer> ids = ...;
 * for (int i = 0; i < ids.size(); i++) {
 *     Integer value = ids.get(i);  // Boxes to Integer
 *     if (value > 1000) {           // Unboxes for comparison
 *         process(value);           // Re-boxes for method call
 *     }
 * }  // All boxed Integers become garbage
 *
 * // Using ObservableIntList - zero allocations
 * ObservableIntList ids = ...;
 * for (int i = 0; i < ids.size(); i++) {
 *     int value = ids.getInt(i);   // Direct primitive access
 *     if (value > 1000) {           // Primitive comparison
 *         process(value);           // Primitive parameter
 *     }
 * }  // Zero garbage created
 * }</pre>
 *
 * <p><b>Impact:</b> Reduced GC frequency, shorter GC pauses, and more predictable application performance,
 * especially critical for real-time or interactive applications.</p>
 *
 * <p><b>3. CPU Cache Performance</b>
 *
 * <p>Modern CPUs rely heavily on cache hierarchies (L1/L2/L3). Primitive arrays offer superior
 * cache performance due to memory layout:</p>
 *
 * <ul>
 *   <li><b>Primitive Arrays:</b> Values stored contiguously in memory. Accessing one element
 *       loads adjacent elements into cache, enabling efficient prefetching and sequential access.</li>
 *   <li><b>Object Collections:</b> References stored contiguously, but actual objects scattered
 *       across heap. Each access requires pointer indirection, causing cache misses and memory latency.</li>
 * </ul>
 *
 * <pre>
 * Primitive Array Layout (excellent cache locality):
 * [4][4][4][4][4][4][4][4]... (contiguous int values)
 *
 * Object Collection Layout (poor cache locality):
 * [ptr][ptr][ptr][ptr]... → scattered Integer objects across heap
 * </pre>
 *
 * <p><b>Result:</b> Primitive collections can be 2-10x faster for sequential operations like iteration,
 * searching, and filtering, depending on data size and access patterns.</p>
 *
 * <p><b>4. Autoboxing Hidden Costs</b>
 *
 * <p>Java's autoboxing feature makes wrapper classes convenient but hides performance costs:</p>
 *
 * <pre>{@code
 * ObservableList<Integer> list = ...;
 * int sum = 0;
 * for (Integer value : list) {  // Each iteration:
 *     sum += value;              // 1. Unboxes Integer to int
 * }                              // 2. Performs addition
 *                                // 3. Result stays primitive (no re-boxing)
 * // But millions of unboxing operations still occurred
 *
 * ObservableIntList list = ...;
 * int sum = 0;
 * list.getBackingList().forEach(value -> {  // Direct primitive iteration
 *     sum += value;  // No boxing/unboxing overhead
 * });
 * }</pre>
 *
 * <p><b>5. Integration with Eclipse Collections</b>
 *
 * <p>By using Eclipse Collections as the backing store, these observable collections gain access to
 * highly optimized primitive-aware operations:</p>
 *
 * <pre>{@code
 * ObservableIntList ids = ...;
 *
 * // Rich functional API without boxing
 * MutableIntList filtered = ids.getBackingList()
 *     .select(id -> id > 1000)           // Primitive predicate
 *     .collect(id -> id * 2)             // Primitive transformation
 *     .sortThis();                        // In-place sorting
 *
 * // Statistical operations
 * long sum = ids.getBackingList().sum();
 * int max = ids.getBackingList().max();
 * double average = ids.getBackingList().average();
 *
 * // All operations work directly on primitives with zero boxing
 * }</pre>
 *
 * <h2>When to Use Observable Primitive Collections</h2>
 *
 * <p><b>Use these collections when:</b></p>
 * <ul>
 *   <li>Storing large quantities of numeric identifiers, timestamps, or numeric keys</li>
 *   <li>Building performance-critical UI components with frequent data updates</li>
 *   <li>Working with real-time data streams or high-frequency updates</li>
 *   <li>Memory constraints are a concern (embedded systems, mobile, cloud cost optimization)</li>
 *   <li>Application profiling reveals GC pressure from boxed primitives</li>
 *   <li>Needing reactive UI bindings with Tinkar NIDs or database identifiers</li>
 * </ul>
 *
 * <p><b>Standard {@code ObservableList<T>} is sufficient when:</b></p>
 * <ul>
 *   <li>Collections are small (hundreds of elements, not millions)</li>
 *   <li>Elements are already objects (strings, domain objects, etc.)</li>
 *   <li>Updates are infrequent and performance is not critical</li>
 *   <li>Type safety and generics compatibility are more important than performance</li>
 * </ul>
 *
 * <h2>Architecture: Bridging Two Worlds</h2>
 *
 * <p>These observable collections solve a unique challenge: providing reactive JavaFX bindings
 * while maintaining primitive storage efficiency. The architecture consists of three layers:</p>
 *
 * <pre>
 * ┌─────────────────────────────────────────┐
 * │  JavaFX Observable Framework            │
 * │  (ModifiableObservableListBase)         │  ← Change notifications, listeners
 * ├─────────────────────────────────────────┤
 * │  ObservableIntList / ObservableLongList │  ← Bridge layer, dual APIs
 * ├─────────────────────────────────────────┤
 * │  Eclipse Collections Primitive Lists    │  ← Efficient storage, bulk operations
 * │  (MutableIntList / MutableLongList)     │
 * └─────────────────────────────────────────┘
 * </pre>
 *
 * <p><b>Layer Responsibilities:</b></p>
 * <ol>
 *   <li><b>JavaFX Layer:</b> Handles change notifications, invalidation listeners, and UI bindings</li>
 *   <li><b>Bridge Layer:</b> Provides both boxed (for compatibility) and unboxed (for performance) APIs</li>
 *   <li><b>Storage Layer:</b> Efficient primitive arrays with functional operations</li>
 * </ol>
 *
 * <h2>Usage Patterns</h2>
 *
 * <p><b>Basic Operations</b>
 * <pre>{@code
 * // Creating observable primitive lists
 * ObservableIntList ids = new ObservableIntList();
 * ids.addInt(1);      // Primitive add (no boxing)
 * ids.add(2);         // Boxed add (for compatibility)
 *
 * int value = ids.getInt(0);    // Primitive get (no unboxing)
 * Integer boxed = ids.get(0);   // Boxed get (standard ObservableList API)
 *
 * // Binding to JavaFX UI (automatic boxing for compatibility)
 * ListView<Integer> listView = new ListView<>(ids);
 * }</pre>
 *
 * <p><b>Performance-Critical Paths</b>
 * <pre>{@code
 * // Use primitive methods for performance-critical code
 * ObservableIntList largeDataset = new ObservableIntList(1_000_000);
 *
 * // Fast primitive iteration
 * largeDataset.getBackingList().forEach(value -> {
 *     // Process primitive value without boxing
 *     processId(value);
 * });
 *
 * // Bulk operations via Eclipse Collections
 * MutableIntList filtered = largeDataset.getBackingList()
 *     .select(id -> id > threshold)
 *     .collectInt(id -> id * 2);
 * }</pre>
 *
 * <p><b>JavaFX Integration</b>
 * <pre>{@code
 * // Observable collections work seamlessly with JavaFX bindings
 * ObservableLongList timestamps = new ObservableLongList();
 *
 * // Listen for changes
 * timestamps.addListener((ListChangeListener.Change<? extends Long> change) -> {
 *     while (change.next()) {
 *         if (change.wasAdded()) {
 *             updateUI(change.getAddedSubList());
 *         }
 *     }
 * });
 *
 * // Bind to properties
 * IntegerProperty count = new SimpleIntegerProperty();
 * count.bind(Bindings.size(timestamps));
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li><b>Prefer unboxed methods:</b> Use {@code addInt()}, {@code getLong()}, etc. in
 *       performance-critical code paths to avoid boxing overhead.</li>
 *
 *   <li><b>Batch updates:</b> Wrap multiple modifications in {@code beginChange()}/{@code endChange()}
 *       to fire a single change notification instead of one per modification.</li>
 *
 *   <li><b>Direct backing access:</b> For bulk operations, use {@code getBackingList()} to leverage
 *       Eclipse Collections' optimized primitive operations, but remember to manually notify listeners
 *       if you modify the list directly.</li>
 *
 *   <li><b>Initial capacity:</b> Pre-allocate capacity when the approximate size is known to avoid
 *       repeated array resizing during population.</li>
 *
 *   <li><b>Measurement:</b> Profile memory and GC behavior before and after switching to primitive
 *       collections to quantify benefits for your specific use case.</li>
 * </ul>
 *
 * <h2>Available Implementations</h2>
 *
 * <ul>
 *   <li>{@link dev.ikm.komet.framework.observable.collection.ObservableIntList} -
 *       Observable list of primitive {@code int} values</li>
 *   <li>{@link dev.ikm.komet.framework.observable.collection.ObservableLongList} -
 *       Observable list of primitive {@code long} values</li>
 * </ul>
 *
 * <h2>References</h2>
 *
 * <ul>
 *   <li><a href="https://eclipse.dev/collections/">Eclipse Collections</a> - High-performance primitive collections library</li>
 *   <li><a href="https://openjfx.io/javadoc/17/javafx.base/javafx/collections/package-summary.html">JavaFX Observable Collections</a> -
 *       Reactive collection framework</li>
 *   <li><a href="https://shipilev.net/jvm/anatomy-quarks/">JVM Anatomy Quarks</a> - Deep dives into JVM object layout and performance</li>
 * </ul>
 *
 * @since 1.0
 */
package dev.ikm.komet.framework.observable.collection;
