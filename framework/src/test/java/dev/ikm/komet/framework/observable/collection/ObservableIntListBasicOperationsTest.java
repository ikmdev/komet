package dev.ikm.komet.framework.observable.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservableIntList Basic Operations Tests")
class ObservableIntListBasicOperationsTest {

    @Nested
    @DisplayName("Get Operations")
    class GetOperations {

        private ObservableIntList list;

        @BeforeEach
        void setUp() {
            list = new ObservableIntList(10, 20, 30, 40, 50);
        }

        @Test
        @DisplayName("getInt returns correct primitive value")
        void testGetInt() {
            assertEquals(10, list.getInt(0));
            assertEquals(30, list.getInt(2));
            assertEquals(50, list.getInt(4));
        }

        @Test
        @DisplayName("get returns correct boxed value")
        void testGet() {
            assertEquals(Integer.valueOf(20), list.get(1));
            assertEquals(Integer.valueOf(40), list.get(3));
        }

        @Test
        @DisplayName("getInt throws IndexOutOfBoundsException for negative index")
        void testGetIntNegativeIndex() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.getInt(-1));
        }

        @Test
        @DisplayName("getInt throws IndexOutOfBoundsException for index >= size")
        void testGetIntIndexTooLarge() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.getInt(5));
        }
    }

    @Nested
    @DisplayName("Add Operations")
    class AddOperations {

        private ObservableIntList list;

        @BeforeEach
        void setUp() {
            list = new ObservableIntList();
        }

        @Test
        @DisplayName("addInt adds element to end of list")
        void testAddInt() {
            assertTrue(list.addInt(100));
            assertEquals(1, list.size());
            assertEquals(100, list.getInt(0));

            assertTrue(list.addInt(200));
            assertEquals(2, list.size());
            assertEquals(200, list.getInt(1));
        }

        @Test
        @DisplayName("add (boxed) adds element to end of list")
        void testAddBoxed() {
            assertTrue(list.add(300));
            assertEquals(1, list.size());
            assertEquals(300, list.getInt(0));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, -1, 100, Integer.MAX_VALUE, Integer.MIN_VALUE})
        @DisplayName("addInt handles various integer values")
        void testAddIntVariousValues(int value) {
            list.addInt(value);
            assertEquals(value, list.getInt(0));
        }

        @Test
        @DisplayName("doAddInt inserts element at specified index")
        void testDoAddInt() {
            list.addInt(10);
            list.addInt(30);
            list.doAddInt(1, 20);

            assertEquals(3, list.size());
            assertEquals(10, list.getInt(0));
            assertEquals(20, list.getInt(1));
            assertEquals(30, list.getInt(2));
        }
    }

    @Nested
    @DisplayName("Set Operations")
    class SetOperations {

        private ObservableIntList list;

        @BeforeEach
        void setUp() {
            list = new ObservableIntList(10, 20, 30, 40, 50);
        }

        @Test
        @DisplayName("doSetInt replaces value and returns old value")
        void testDoSetInt() {
            int oldValue = list.doSetInt(2, 300);

            assertEquals(30, oldValue, "Should return old value");
            assertEquals(300, list.getInt(2), "Should set new value");
            assertEquals(5, list.size(), "Size should remain unchanged");
        }

        @Test
        @DisplayName("set (boxed) replaces value and returns old value")
        void testSetBoxed() {
            Integer oldValue = list.set(1, 200);

            assertEquals(Integer.valueOf(20), oldValue);
            assertEquals(200, list.getInt(1));
        }

        @Test
        @DisplayName("doSetInt at first index works correctly")
        void testDoSetIntFirstIndex() {
            list.doSetInt(0, 100);
            assertEquals(100, list.getInt(0));
        }

        @Test
        @DisplayName("doSetInt at last index works correctly")
        void testDoSetIntLastIndex() {
            list.doSetInt(4, 500);
            assertEquals(500, list.getInt(4));
        }
    }

    @Nested
    @DisplayName("Remove Operations")
    class RemoveOperations {

        private ObservableIntList list;

        @BeforeEach
        void setUp() {
            list = new ObservableIntList(10, 20, 30, 40, 50);
        }

        @Test
        @DisplayName("doRemoveInt removes by index and returns value")
        void testDoRemoveInt() {
            int removed = list.doRemoveInt(2);

            assertEquals(30, removed, "Should return removed value");
            assertEquals(4, list.size(), "Size should decrease");
            assertEquals(40, list.getInt(2), "Elements after removed index should shift");
        }

        @Test
        @DisplayName("removeInt removes first occurrence by value")
        void testRemoveInt() {
            assertTrue(list.removeInt(30), "Should return true when value found");
            assertEquals(4, list.size());
            assertEquals(40, list.getInt(2));
        }

        @Test
        @DisplayName("removeInt returns false when value not found")
        void testRemoveIntNotFound() {
            assertFalse(list.removeInt(999), "Should return false when value not found");
            assertEquals(5, list.size(), "Size should remain unchanged");
        }

        @Test
        @DisplayName("remove (by index) removes element")
        void testRemoveByIndex() {
            Integer removed = list.remove(0);

            assertEquals(Integer.valueOf(10), removed);
            assertEquals(4, list.size());
            assertEquals(20, list.getInt(0));
        }

        @Test
        @DisplayName("removeInt removes first occurrence when duplicates exist")
        void testRemoveIntWithDuplicates() {
            list.addInt(20); // Add duplicate
            assertTrue(list.removeInt(20));
            assertEquals(5, list.size());
            assertEquals(20, list.getInt(4), "Second occurrence should remain");
        }
    }

    @Nested
    @DisplayName("Size and Empty Operations")
    class SizeOperations {

        @Test
        @DisplayName("size returns correct count")
        void testSize() {
            ObservableIntList list = new ObservableIntList();
            assertEquals(0, list.size());

            list.addInt(1);
            assertEquals(1, list.size());

            list.addInt(2);
            list.addInt(3);
            assertEquals(3, list.size());
        }

        @Test
        @DisplayName("isEmpty returns true for empty list")
        void testIsEmpty() {
            ObservableIntList list = new ObservableIntList();
            assertTrue(list.isEmpty());

            list.addInt(1);
            assertFalse(list.isEmpty());

            list.remove(0);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("clear removes all elements")
        void testClear() {
            ObservableIntList list = new ObservableIntList(1, 2, 3, 4, 5);
            list.clear();

            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }
    }

    @Nested
    @DisplayName("Contains Operations")
    class ContainsOperations {

        private ObservableIntList list;

        @BeforeEach
        void setUp() {
            list = new ObservableIntList(10, 20, 30, 40, 50);
        }

        @Test
        @DisplayName("contains returns true for existing boxed value")
        void testContains() {
            assertTrue(list.contains(30));
            assertTrue(list.contains(10));
            assertTrue(list.contains(50));
        }

        @Test
        @DisplayName("contains returns false for non-existing value")
        void testContainsNonExisting() {
            assertFalse(list.contains(999));
            assertFalse(list.contains(0));
        }
    }
}
