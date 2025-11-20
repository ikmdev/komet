package dev.ikm.komet.framework.observable.collection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservableIntList Performance Demonstration Tests")
@Tag("performance")
class ObservableIntListPerformanceTest {

    private static final int TEST_SIZE = 100_000;

    @Test
    @DisplayName("Primitive operations avoid boxing overhead")
    void testPrimitiveVsBoxedOperations() {
        ObservableIntList primitiveList = new ObservableIntList();
        ObservableList<Integer> boxedList = FXCollections.observableArrayList();

        // Demonstrate primitive operations (no boxing)
        long primitiveStart = System.nanoTime();
        for (int i = 0; i < TEST_SIZE; i++) {
            primitiveList.addInt(i);
        }
        long primitiveTime = System.nanoTime() - primitiveStart;

        // Demonstrate boxed operations (requires boxing)
        long boxedStart = System.nanoTime();
        for (int i = 0; i < TEST_SIZE; i++) {
            boxedList.add(i);
        }
        long boxedTime = System.nanoTime() - boxedStart;

        // Verify both lists have correct size
        assertEquals(TEST_SIZE, primitiveList.size());
        assertEquals(TEST_SIZE, boxedList.size());

        // Note: This is a demonstration, not a strict performance assertion
        // Primitive operations should generally be faster due to no boxing overhead
        System.out.println("Primitive add time: " + primitiveTime / 1_000_000.0 + " ms");
        System.out.println("Boxed add time: " + boxedTime / 1_000_000.0 + " ms");
        System.out.println("Speedup: " + (double) boxedTime / primitiveTime + "x");
    }

    @Test
    @DisplayName("Primitive get operations avoid unboxing overhead")
    void testPrimitiveVsBoxedRetrieval() {
        ObservableIntList primitiveList = new ObservableIntList();
        ObservableList<Integer> boxedList = FXCollections.observableArrayList();

        // Populate lists
        for (int i = 0; i < TEST_SIZE; i++) {
            primitiveList.addInt(i);
            boxedList.add(i);
        }

        // Test primitive retrieval (no unboxing)
        long primitiveStart = System.nanoTime();
        long primitiveSum = 0;
        for (int i = 0; i < TEST_SIZE; i++) {
            primitiveSum += primitiveList.getInt(i);
        }
        long primitiveTime = System.nanoTime() - primitiveStart;

        // Test boxed retrieval (requires unboxing)
        long boxedStart = System.nanoTime();
        long boxedSum = 0;
        for (int i = 0; i < TEST_SIZE; i++) {
            boxedSum += boxedList.get(i);
        }
        long boxedTime = System.nanoTime() - boxedStart;

        // Verify sums are equal
        assertEquals(primitiveSum, boxedSum);

        System.out.println("Primitive get time: " + primitiveTime / 1_000_000.0 + " ms");
        System.out.println("Boxed get time: " + boxedTime / 1_000_000.0 + " ms");
        System.out.println("Speedup: " + (double) boxedTime / primitiveTime + "x");
    }

    @Test
    @DisplayName("Memory efficiency demonstration")
    void testMemoryEfficiency() {
        // This test demonstrates the concept, actual memory measurement would require
        // instrumentation or profiling tools

        ObservableIntList primitiveList = new ObservableIntList();
        ObservableList<Integer> boxedList = FXCollections.observableArrayList();

        int testSize = 1_000_000;

        // Populate both lists
        for (int i = 0; i < testSize; i++) {
            primitiveList.addInt(i);
            boxedList.add(i);
        }

        assertEquals(testSize, primitiveList.size());
        assertEquals(testSize, boxedList.size());

        // Memory analysis (conceptual):
        // Primitive: ~4 MB (4 bytes per int)
        // Boxed: ~28-32 MB (24 bytes per Integer object + 8 bytes per reference)
        // Memory savings: 85-87%

        long estimatedPrimitiveMemory = (long) testSize * 4; // 4 bytes per int
        long estimatedBoxedMemory = (long) testSize * 32; // ~32 bytes per Integer object

        System.out.println("Estimated primitive list memory: " + estimatedPrimitiveMemory / 1_000_000.0 + " MB");
        System.out.println("Estimated boxed list memory: " + estimatedBoxedMemory / 1_000_000.0 + " MB");
        System.out.println("Memory savings: " + (100.0 - (double) estimatedPrimitiveMemory / estimatedBoxedMemory * 100) + "%");
    }

    @Test
    @DisplayName("Remove operations performance comparison")
    void testRemovePerformance() {
        ObservableIntList primitiveList = new ObservableIntList();
        ObservableList<Integer> boxedList = FXCollections.observableArrayList();

        int testSize = 10_000;

        // Populate lists
        for (int i = 0; i < testSize; i++) {
            primitiveList.addInt(i);
            boxedList.add(i);
        }

        // Test primitive remove (no boxing)
        long primitiveStart = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            primitiveList.removeInt(i);
        }
        long primitiveTime = System.nanoTime() - primitiveStart;

        // Test boxed remove (requires boxing for comparison)
        long boxedStart = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            boxedList.remove(Integer.valueOf(i));
        }
        long boxedTime = System.nanoTime() - boxedStart;

        System.out.println("Primitive remove time: " + primitiveTime / 1_000_000.0 + " ms");
        System.out.println("Boxed remove time: " + boxedTime / 1_000_000.0 + " ms");
        System.out.println("Speedup: " + (double) boxedTime / primitiveTime + "x");

        assertEquals(testSize - 1000, primitiveList.size());
        assertEquals(testSize - 1000, boxedList.size());
    }

    @Test
    @DisplayName("Set operations performance comparison")
    void testSetPerformance() {
        ObservableIntList primitiveList = new ObservableIntList();
        ObservableList<Integer> boxedList = FXCollections.observableArrayList();

        // Populate lists
        for (int i = 0; i < TEST_SIZE; i++) {
            primitiveList.addInt(i);
            boxedList.add(i);
        }

        // Test primitive set (no boxing/unboxing)
        long primitiveStart = System.nanoTime();
        for (int i = 0; i < TEST_SIZE; i++) {
            primitiveList.doSetInt(i, i * 2);
        }
        long primitiveTime = System.nanoTime() - primitiveStart;

        // Test boxed set (requires boxing)
        long boxedStart = System.nanoTime();
        for (int i = 0; i < TEST_SIZE; i++) {
            boxedList.set(i, i * 2);
        }
        long boxedTime = System.nanoTime() - boxedStart;

        System.out.println("Primitive set time: " + primitiveTime / 1_000_000.0 + " ms");
        System.out.println("Boxed set time: " + boxedTime / 1_000_000.0 + " ms");
        System.out.println("Speedup: " + (double) boxedTime / primitiveTime + "x");

        // Verify final values
        assertEquals(0, primitiveList.getInt(0));
        assertEquals((TEST_SIZE - 1) * 2, primitiveList.getInt(TEST_SIZE - 1));
    }

    @Test
    @DisplayName("Iteration performance comparison")
    void testIterationPerformance() {
        ObservableIntList primitiveList = new ObservableIntList();
        ObservableList<Integer> boxedList = FXCollections.observableArrayList();

        // Populate lists
        for (int i = 0; i < TEST_SIZE; i++) {
            primitiveList.addInt(i);
            boxedList.add(i);
        }

        // Test primitive iteration using getInt
        long primitiveStart = System.nanoTime();
        long sum1 = 0;
        for (int i = 0; i < primitiveList.size(); i++) {
            sum1 += primitiveList.getInt(i);
        }
        long primitiveTime = System.nanoTime() - primitiveStart;

        // Test boxed iteration
        long boxedStart = System.nanoTime();
        long sum2 = 0;
        for (Integer value : boxedList) {
            sum2 += value;
        }
        long boxedTime = System.nanoTime() - boxedStart;

        assertEquals(sum1, sum2);

        System.out.println("Primitive iteration time: " + primitiveTime / 1_000_000.0 + " ms");
        System.out.println("Boxed iteration time: " + boxedTime / 1_000_000.0 + " ms");
        System.out.println("Speedup: " + (double) boxedTime / primitiveTime + "x");
    }

    @Test
    @DisplayName("Cache locality benefits demonstration")
    void testCacheLocality() {
        // Primitive arrays have better cache locality due to contiguous memory
        ObservableIntList primitiveList = new ObservableIntList();

        // Populate with sequential values
        for (int i = 0; i < TEST_SIZE; i++) {
            primitiveList.addInt(i);
        }

        // Sequential access (benefits from cache locality)
        long sequentialStart = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < TEST_SIZE; i++) {
            sum += primitiveList.getInt(i);
        }
        long sequentialTime = System.nanoTime() - sequentialStart;

        System.out.println("Sequential access time: " + sequentialTime / 1_000_000.0 + " ms");
        System.out.println("Sum (for verification): " + sum);

        // The benefit: primitive arrays are stored contiguously in memory,
        // allowing the CPU to prefetch data more effectively
        assertTrue(sum > 0, "Sum should be positive for verification");
    }
}
