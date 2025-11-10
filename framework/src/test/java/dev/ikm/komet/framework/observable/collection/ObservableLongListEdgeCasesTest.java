package dev.ikm.komet.framework.observable.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservableLongList Edge Cases and Boundary Tests")
class ObservableLongListEdgeCasesTest {

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {

        @ParameterizedTest
        @ValueSource(longs = {Long.MIN_VALUE, Long.MAX_VALUE, 0L, -1L, 1L, 1234567890123456789L})
        @DisplayName("List handles boundary long values")
        void testBoundaryValues(long value) {
            ObservableLongList list = new ObservableLongList();
            list.addLong(value);

            assertEquals(value, list.getLong(0));
            assertTrue(list.contains(value));
        }

        @Test
        @DisplayName("List handles Long.MIN_VALUE correctly")
        void testMinValue() {
            ObservableLongList list = new ObservableLongList(new long[]{Long.MIN_VALUE});

            assertEquals(Long.MIN_VALUE, list.getLong(0));
            assertTrue(list.removeLong(Long.MIN_VALUE));
            assertEquals(0, list.size());
        }

        @Test
        @DisplayName("Can store and retrieve Long.MAX_VALUE and Long.MIN_VALUE")
        void testMaxValue() {
            // Create a list with one element initialized to MAX_VALUE
            ObservableLongList list = new ObservableLongList(new long[]{Long.MAX_VALUE});

            assertEquals(Long.MAX_VALUE, list.getLong(0));
            long oldValue = list.doSetLong(0, Long.MIN_VALUE);
            assertEquals(Long.MAX_VALUE, oldValue);
            assertEquals(Long.MIN_VALUE, list.getLong(0));

            // Also test that we can add MAX_VALUE to an empty list
            ObservableLongList list2 = new ObservableLongList();
            list2.addLong(Long.MAX_VALUE);
            assertEquals(Long.MAX_VALUE, list2.getLong(0));
        }

        @Test
        @DisplayName("List handles zero correctly")
        void testZeroValue() {
            ObservableLongList list = new ObservableLongList(0L, 0L, 0L);

            assertEquals(3, list.size());
            assertEquals(0L, list.getLong(0));
            assertEquals(0L, list.getLong(1));
            assertEquals(0L, list.getLong(2));
        }

        @Test
        @DisplayName("List handles large positive and negative values")
        void testLargeValues() {
            long largePositive = 9223372036854775806L; // Long.MAX_VALUE - 1
            long largeNegative = -9223372036854775807L; // Long.MIN_VALUE + 1

            ObservableLongList list = new ObservableLongList(largePositive, largeNegative);

            assertEquals(largePositive, list.getLong(0));
            assertEquals(largeNegative, list.getLong(1));
        }
    }

    @Nested
    @DisplayName("Index Boundary Tests")
    class IndexBoundaryTests {

        @Test
        @DisplayName("Accessing empty list throws IndexOutOfBoundsException")
        void testEmptyListAccess() {
            ObservableLongList list = new ObservableLongList();

            assertThrows(IndexOutOfBoundsException.class, () -> list.getLong(0));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100})
        @DisplayName("Negative indices throw IndexOutOfBoundsException")
        void testNegativeIndices(int index) {
            ObservableLongList list = new ObservableLongList(1L, 2L, 3L);

            assertThrows(IndexOutOfBoundsException.class, () -> list.getLong(index));
        }

        @Test
        @DisplayName("Index at size boundary throws IndexOutOfBoundsException")
        void testIndexAtSizeBoundary() {
            ObservableLongList list = new ObservableLongList(1L, 2L, 3L);

            assertThrows(IndexOutOfBoundsException.class, () -> list.getLong(3));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
        }

        @Test
        @DisplayName("doSetLong at boundary indices works correctly")
        void testSetAtBoundaries() {
            ObservableLongList list = new ObservableLongList(10L, 20L, 30L);

            // First index
            assertEquals(10L, list.doSetLong(0, 100L));
            assertEquals(100L, list.getLong(0));

            // Last index
            assertEquals(30L, list.doSetLong(2, 300L));
            assertEquals(300L, list.getLong(2));
        }

        @Test
        @DisplayName("doRemoveLong at boundary indices works correctly")
        void testRemoveAtBoundaries() {
            ObservableLongList list = new ObservableLongList(10L, 20L, 30L);

            // Remove last
            assertEquals(30L, list.doRemoveLong(2));
            assertEquals(2, list.size());

            // Remove first
            assertEquals(10L, list.doRemoveLong(0));
            assertEquals(1, list.size());
            assertEquals(20L, list.getLong(0));
        }
    }

    @Nested
    @DisplayName("Duplicate Value Tests")
    class DuplicateValueTests {

        @Test
        @DisplayName("List allows duplicate values")
        void testDuplicatesAllowed() {
            ObservableLongList list = new ObservableLongList(5L, 5L, 5L, 5L);

            assertEquals(4, list.size());
            assertEquals(5L, list.getLong(0));
            assertEquals(5L, list.getLong(3));
        }

        @Test
        @DisplayName("removeLong removes only first occurrence of duplicate")
        void testRemoveFirstDuplicate() {
            ObservableLongList list = new ObservableLongList(1L, 2L, 2L, 2L, 3L);

            assertTrue(list.removeLong(2L));
            assertEquals(4, list.size());
            assertEquals(2L, list.getLong(1), "Second occurrence should still exist");
            assertEquals(2L, list.getLong(2), "Third occurrence should still exist");
        }

        @Test
        @DisplayName("contains returns true when duplicate values exist")
        void testContainsDuplicates() {
            ObservableLongList list = new ObservableLongList(7L, 7L, 7L);

            assertTrue(list.contains(7L));
        }
    }

    @Nested
    @DisplayName("Empty List Operations")
    class EmptyListTests {

        @Test
        @DisplayName("removeLong on empty list returns false")
        void testRemoveFromEmptyList() {
            ObservableLongList list = new ObservableLongList();

            assertFalse(list.removeLong(42L));
            assertEquals(0, list.size());
        }

        @Test
        @DisplayName("contains on empty list returns false")
        void testContainsOnEmptyList() {
            ObservableLongList list = new ObservableLongList();

            assertFalse(list.contains(0L));
            assertFalse(list.contains(100L));
        }

        @Test
        @DisplayName("clear on empty list does nothing")
        void testClearEmptyList() {
            ObservableLongList list = new ObservableLongList();
            list.clear();

            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("isEmpty returns true for newly created and cleared list")
        void testIsEmpty() {
            ObservableLongList list = new ObservableLongList();
            assertTrue(list.isEmpty());

            list.addLong(1L);
            assertFalse(list.isEmpty());

            list.clear();
            assertTrue(list.isEmpty());
        }
    }

    @Nested
    @DisplayName("Single Element List Tests")
    class SingleElementTests {

        @Test
        @DisplayName("Single element list operations work correctly")
        void testSingleElement() {
            ObservableLongList list = new ObservableLongList();
            list.addLong(42L);

            assertEquals(1, list.size());
            assertEquals(42L, list.getLong(0));
            assertTrue(list.contains(42L));

            list.doSetLong(0, 100L);
            assertEquals(100L, list.getLong(0));

            long removed = list.doRemoveLong(0);
            assertEquals(100L, removed);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("Removing single element makes list empty")
        void testRemoveSingleElement() {
            ObservableLongList list = new ObservableLongList();
            list.addLong(99L);

            assertTrue(list.removeLong(99L));
            assertTrue(list.isEmpty());
            assertEquals(0, list.size());
        }

        @Test
        @DisplayName("Single element list with boundary value")
        void testSingleElementBoundaryValue() {
            // Use array form to avoid confusion with capacity constructor
            ObservableLongList list = new ObservableLongList(new long[]{Long.MAX_VALUE});

            assertEquals(1, list.size());
            assertEquals(Long.MAX_VALUE, list.getLong(0));
            assertTrue(list.removeLong(Long.MAX_VALUE));
            assertTrue(list.isEmpty());
        }
    }

    @Nested
    @DisplayName("Large List Tests")
    class LargeListTests {

        @Test
        @DisplayName("List handles large number of elements")
        void testLargeList() {
            ObservableLongList list = new ObservableLongList();

            // Add 10,000 elements
            for (long i = 0; i < 10_000; i++) {
                list.addLong(i);
            }

            assertEquals(10_000, list.size());
            assertEquals(0L, list.getLong(0));
            assertEquals(9_999L, list.getLong(9_999));
        }

        @Test
        @DisplayName("List handles operations on large dataset")
        void testOperationsOnLargeList() {
            ObservableLongList list = new ObservableLongList();

            for (long i = 0; i < 1_000; i++) {
                list.addLong(i);
            }

            // Remove some elements
            for (long i = 0; i < 100; i++) {
                list.removeLong(i);
            }

            assertEquals(900, list.size());
        }

        @Test
        @DisplayName("List handles large long values in large dataset")
        void testLargeListWithLargeValues() {
            ObservableLongList list = new ObservableLongList();

            // Add values starting from a large base
            long base = 1000000000000000000L;
            for (int i = 0; i < 1000; i++) {
                list.addLong(base + i);
            }

            assertEquals(1000, list.size());
            assertEquals(base, list.getLong(0));
            assertEquals(base + 999, list.getLong(999));
        }
    }

    @Nested
    @DisplayName("Null and Type Tests")
    class NullAndTypeTests {

        @Test
        @DisplayName("contains(null) returns false")
        void testContainsNull() {
            ObservableLongList list = new ObservableLongList(1L, 2L, 3L);

            assertFalse(list.contains(null));
        }

        @Test
        @DisplayName("remove(null) returns false")
        void testRemoveNull() {
            ObservableLongList list = new ObservableLongList(1L, 2L, 3L);

            assertFalse(list.remove(null));
            assertEquals(3, list.size());
        }
    }

    @Nested
    @DisplayName("Sequential Operations Tests")
    class SequentialOperationsTests {

        @ParameterizedTest
        @CsvSource({
            "0, 10, 100",
            "1, 20, 200",
            "2, 30, 300"
        })
        @DisplayName("Multiple set operations work correctly")
        void testMultipleSets(int index, long value1, long value2) {
            ObservableLongList list = new ObservableLongList(10L, 20L, 30L);

            list.doSetLong(index, value1);
            assertEquals(value1, list.getLong(index));

            list.doSetLong(index, value2);
            assertEquals(value2, list.getLong(index));
        }

        @Test
        @DisplayName("Alternating add and remove maintains consistency")
        void testAlternatingAddRemove() {
            ObservableLongList list = new ObservableLongList();

            for (long i = 0; i < 100; i++) {
                list.addLong(i);
                if (i > 0) {
                    list.removeLong(i - 1);
                }
            }

            assertEquals(1, list.size());
            assertEquals(99L, list.getLong(0));
        }
    }

    @Nested
    @DisplayName("Backing List Access Tests")
    class BackingListTests {

        @Test
        @DisplayName("getBackingList returns non-null")
        void testGetBackingList() {
            ObservableLongList list = new ObservableLongList(1L, 2L, 3L);

            assertNotNull(list.getBackingList());
            assertEquals(3, list.getBackingList().size());
        }

        @Test
        @DisplayName("Backing list contains correct values")
        void testBackingListValues() {
            ObservableLongList list = new ObservableLongList(10L, 20L, 30L);

            assertEquals(10L, list.getBackingList().get(0));
            assertEquals(20L, list.getBackingList().get(1));
            assertEquals(30L, list.getBackingList().get(2));
        }
    }
}
