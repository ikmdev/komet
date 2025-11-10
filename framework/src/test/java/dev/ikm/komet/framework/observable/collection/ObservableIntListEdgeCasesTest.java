package dev.ikm.komet.framework.observable.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservableIntList Edge Cases and Boundary Tests")
class ObservableIntListEdgeCasesTest {

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {

        @ParameterizedTest
        @ValueSource(ints = {Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1, 1})
        @DisplayName("List handles boundary integer values")
        void testBoundaryValues(int value) {
            ObservableIntList list = new ObservableIntList();
            list.addInt(value);

            assertEquals(value, list.getInt(0));
            assertTrue(list.contains(value));
        }

        @Test
        @DisplayName("List handles Integer.MIN_VALUE correctly")
        void testMinValue() {
            ObservableIntList list = new ObservableIntList(new int[] {Integer.MIN_VALUE});

            assertEquals(Integer.MIN_VALUE, list.getInt(0));
            assertTrue(list.removeInt(Integer.MIN_VALUE));
            assertEquals(0, list.size());
        }

        @Test
        @DisplayName("Can store and retrieve Integer.MAX_VALUE and Integer.MIN_VALUE")
        void testMaxValue() {
            // Create a list with one element initialized to MAX_VALUE
            ObservableIntList list = new ObservableIntList(new int[]{Integer.MAX_VALUE});

            assertEquals(Integer.MAX_VALUE, list.getInt(0));
            int oldValue = list.doSetInt(0, Integer.MIN_VALUE);
            assertEquals(Integer.MAX_VALUE, oldValue);
            assertEquals(Integer.MIN_VALUE, list.getInt(0));
        
        // Also test that we can add MAX_VALUE to an empty list
        ObservableIntList list2 = new ObservableIntList();
        list2.addInt(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, list2.getInt(0));
    }

        @Test
        @DisplayName("List handles zero correctly")
        void testZeroValue() {
            ObservableIntList list = new ObservableIntList(0, 0, 0);

            assertEquals(3, list.size());
            assertEquals(0, list.getInt(0));
            assertEquals(0, list.getInt(1));
            assertEquals(0, list.getInt(2));
        }
    }

    @Nested
    @DisplayName("Index Boundary Tests")
    class IndexBoundaryTests {

        @Test
        @DisplayName("Accessing empty list throws IndexOutOfBoundsException")
        void testEmptyListAccess() {
            ObservableIntList list = new ObservableIntList();

            assertThrows(IndexOutOfBoundsException.class, () -> list.getInt(0));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100})
        @DisplayName("Negative indices throw IndexOutOfBoundsException")
        void testNegativeIndices(int index) {
            ObservableIntList list = new ObservableIntList(1, 2, 3);

            assertThrows(IndexOutOfBoundsException.class, () -> list.getInt(index));
        }

        @Test
        @DisplayName("Index at size boundary throws IndexOutOfBoundsException")
        void testIndexAtSizeBoundary() {
            ObservableIntList list = new ObservableIntList(1, 2, 3);

            assertThrows(IndexOutOfBoundsException.class, () -> list.getInt(3));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
        }

        @Test
        @DisplayName("doSetInt at boundary indices works correctly")
        void testSetAtBoundaries() {
            ObservableIntList list = new ObservableIntList(10, 20, 30);

            // First index
            assertEquals(10, list.doSetInt(0, 100));
            assertEquals(100, list.getInt(0));

            // Last index
            assertEquals(30, list.doSetInt(2, 300));
            assertEquals(300, list.getInt(2));
        }

        @Test
        @DisplayName("doRemoveInt at boundary indices works correctly")
        void testRemoveAtBoundaries() {
            ObservableIntList list = new ObservableIntList(10, 20, 30);

            // Remove last
            assertEquals(30, list.doRemoveInt(2));
            assertEquals(2, list.size());

            // Remove first
            assertEquals(10, list.doRemoveInt(0));
            assertEquals(1, list.size());
            assertEquals(20, list.getInt(0));
        }
    }

    @Nested
    @DisplayName("Duplicate Value Tests")
    class DuplicateValueTests {

        @Test
        @DisplayName("List allows duplicate values")
        void testDuplicatesAllowed() {
            ObservableIntList list = new ObservableIntList(5, 5, 5, 5);

            assertEquals(4, list.size());
            assertEquals(5, list.getInt(0));
            assertEquals(5, list.getInt(3));
        }

        @Test
        @DisplayName("removeInt removes only first occurrence of duplicate")
        void testRemoveFirstDuplicate() {
            ObservableIntList list = new ObservableIntList(1, 2, 2, 2, 3);

            assertTrue(list.removeInt(2));
            assertEquals(4, list.size());
            assertEquals(2, list.getInt(1), "Second occurrence should still exist");
            assertEquals(2, list.getInt(2), "Third occurrence should still exist");
        }

        @Test
        @DisplayName("contains returns true when duplicate values exist")
        void testContainsDuplicates() {
            ObservableIntList list = new ObservableIntList(7, 7, 7);

            assertTrue(list.contains(7));
        }
    }

    @Nested
    @DisplayName("Empty List Operations")
    class EmptyListTests {

        @Test
        @DisplayName("removeInt on empty list returns false")
        void testRemoveFromEmptyList() {
            ObservableIntList list = new ObservableIntList();

            assertFalse(list.removeInt(42));
            assertEquals(0, list.size());
        }

        @Test
        @DisplayName("contains on empty list returns false")
        void testContainsOnEmptyList() {
            ObservableIntList list = new ObservableIntList();

            assertFalse(list.contains(0));
            assertFalse(list.contains(100));
        }

        @Test
        @DisplayName("clear on empty list does nothing")
        void testClearEmptyList() {
            ObservableIntList list = new ObservableIntList();
            list.clear();

            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("isEmpty returns true for newly created and cleared list")
        void testIsEmpty() {
            ObservableIntList list = new ObservableIntList();
            assertTrue(list.isEmpty());

            list.addInt(1);
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
            ObservableIntList list = new ObservableIntList();
            list.addInt(42);

            assertEquals(1, list.size());
            assertEquals(42, list.getInt(0));
            assertTrue(list.contains(42));

            list.doSetInt(0, 100);
            assertEquals(100, list.getInt(0));

            int removed = list.doRemoveInt(0);
            assertEquals(100, removed);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("Removing single element makes list empty")
        void testRemoveSingleElement() {
            ObservableIntList list = new ObservableIntList();
            list.addInt(99);

            assertTrue(list.removeInt(99));
            assertTrue(list.isEmpty());
            assertEquals(0, list.size());
        }
    }

    @Nested
    @DisplayName("Large List Tests")
    class LargeListTests {

        @Test
        @DisplayName("List handles large number of elements")
        void testLargeList() {
            ObservableIntList list = new ObservableIntList();

            // Add 10,000 elements
            for (int i = 0; i < 10_000; i++) {
                list.addInt(i);
            }

            assertEquals(10_000, list.size());
            assertEquals(0, list.getInt(0));
            assertEquals(9_999, list.getInt(9_999));
        }

        @Test
        @DisplayName("List handles operations on large dataset")
        void testOperationsOnLargeList() {
            ObservableIntList list = new ObservableIntList();

            for (int i = 0; i < 1_000; i++) {
                list.addInt(i);
            }

            // Remove some elements
            for (int i = 0; i < 100; i++) {
                list.removeInt(i);
            }

            assertEquals(900, list.size());
        }
    }

    @Nested
    @DisplayName("Null and Type Tests")
    class NullAndTypeTests {

        @Test
        @DisplayName("contains(null) returns false")
        void testContainsNull() {
            ObservableIntList list = new ObservableIntList(1, 2, 3);

            assertFalse(list.contains(null));
        }

        @Test
        @DisplayName("remove(null) returns false")
        void testRemoveNull() {
            ObservableIntList list = new ObservableIntList(1, 2, 3);

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
        void testMultipleSets(int index, int value1, int value2) {
            ObservableIntList list = new ObservableIntList(10, 20, 30);

            list.doSetInt(index, value1);
            assertEquals(value1, list.getInt(index));

            list.doSetInt(index, value2);
            assertEquals(value2, list.getInt(index));
        }

        @Test
        @DisplayName("Alternating add and remove maintains consistency")
        void testAlternatingAddRemove() {
            ObservableIntList list = new ObservableIntList();

            for (int i = 0; i < 100; i++) {
                list.addInt(i);
                if (i > 0) {
                    list.removeInt(i - 1);
                }
            }

            assertEquals(1, list.size());
            assertEquals(99, list.getInt(0));
        }
    }

    @Nested
    @DisplayName("Backing List Access Tests")
    class BackingListTests {

        @Test
        @DisplayName("getBackingList returns non-null")
        void testGetBackingList() {
            ObservableIntList list = new ObservableIntList(1, 2, 3);

            assertNotNull(list.getBackingList());
            assertEquals(3, list.getBackingList().size());
        }

        @Test
        @DisplayName("Backing list contains correct values")
        void testBackingListValues() {
            ObservableIntList list = new ObservableIntList(10, 20, 30);

            assertEquals(10, list.getBackingList().get(0));
            assertEquals(20, list.getBackingList().get(1));
            assertEquals(30, list.getBackingList().get(2));
        }
    }
}
