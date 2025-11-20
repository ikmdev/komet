package dev.ikm.komet.framework.observable.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservableLongList Basic Operations Tests")
class ObservableLongListBasicOperationsTest {

    @Nested
    @DisplayName("Get Operations")
    class GetOperations {

        private ObservableLongList list;

        @BeforeEach
        void setUp() {
            list = new ObservableLongList(10L, 20L, 30L, 40L, 50L);
        }

        @Test
        @DisplayName("getLong returns correct primitive value")
        void testGetLong() {
            assertEquals(10L, list.getLong(0));
            assertEquals(30L, list.getLong(2));
            assertEquals(50L, list.getLong(4));
        }

        @Test
        @DisplayName("get returns correct boxed value")
        void testGet() {
            assertEquals(Long.valueOf(20L), list.get(1));
            assertEquals(Long.valueOf(40L), list.get(3));
        }

        @Test
        @DisplayName("getLong throws IndexOutOfBoundsException for negative index")
        void testGetLongNegativeIndex() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.getLong(-1));
        }

        @Test
        @DisplayName("getLong throws IndexOutOfBoundsException for index >= size")
        void testGetLongIndexTooLarge() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.getLong(5));
        }
    }

    @Nested
    @DisplayName("Add Operations")
    class AddOperations {

        private ObservableLongList list;

        @BeforeEach
        void setUp() {
            list = new ObservableLongList();
        }

        @Test
        @DisplayName("addLong adds element to end of list")
        void testAddLong() {
            assertTrue(list.addLong(100L));
            assertEquals(1, list.size());
            assertEquals(100L, list.getLong(0));

            assertTrue(list.addLong(200L));
            assertEquals(2, list.size());
            assertEquals(200L, list.getLong(1));
        }

        @Test
        @DisplayName("add (boxed) adds element to end of list")
        void testAddBoxed() {
            assertTrue(list.add(300L));
            assertEquals(1, list.size());
            assertEquals(300L, list.getLong(0));
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, 1L, -1L, 100L, Long.MAX_VALUE, Long.MIN_VALUE, 1234567890123456789L})
        @DisplayName("addLong handles various long values")
        void testAddLongVariousValues(long value) {
            list.addLong(value);
            assertEquals(value, list.getLong(0));
        }

        @Test
        @DisplayName("doAddLong inserts element at specified index")
        void testDoAddLong() {
            list.addLong(10L);
            list.addLong(30L);
            list.doAddLong(1, 20L);

            assertEquals(3, list.size());
            assertEquals(10L, list.getLong(0));
            assertEquals(20L, list.getLong(1));
            assertEquals(30L, list.getLong(2));
        }
    }

    @Nested
    @DisplayName("Set Operations")
    class SetOperations {

        private ObservableLongList list;

        @BeforeEach
        void setUp() {
            list = new ObservableLongList(10L, 20L, 30L, 40L, 50L);
        }

        @Test
        @DisplayName("doSetLong replaces value and returns old value")
        void testDoSetLong() {
            long oldValue = list.doSetLong(2, 300L);

            assertEquals(30L, oldValue, "Should return old value");
            assertEquals(300L, list.getLong(2), "Should set new value");
            assertEquals(5, list.size(), "Size should remain unchanged");
        }

        @Test
        @DisplayName("set (boxed) replaces value and returns old value")
        void testSetBoxed() {
            Long oldValue = list.set(1, 200L);

            assertEquals(Long.valueOf(20L), oldValue);
            assertEquals(200L, list.getLong(1));
        }

        @Test
        @DisplayName("doSetLong at first index works correctly")
        void testDoSetLongFirstIndex() {
            list.doSetLong(0, 100L);
            assertEquals(100L, list.getLong(0));
        }

        @Test
        @DisplayName("doSetLong at last index works correctly")
        void testDoSetLongLastIndex() {
            list.doSetLong(4, 500L);
            assertEquals(500L, list.getLong(4));
        }
    }

    @Nested
    @DisplayName("Remove Operations")
    class RemoveOperations {

        private ObservableLongList list;

        @BeforeEach
        void setUp() {
            list = new ObservableLongList(10L, 20L, 30L, 40L, 50L);
        }

        @Test
        @DisplayName("doRemoveLong removes by index and returns value")
        void testDoRemoveLong() {
            long removed = list.doRemoveLong(2);

            assertEquals(30L, removed, "Should return removed value");
            assertEquals(4, list.size(), "Size should decrease");
            assertEquals(40L, list.getLong(2), "Elements after removed index should shift");
        }

        @Test
        @DisplayName("removeLong removes first occurrence by value")
        void testRemoveLong() {
            assertTrue(list.removeLong(30L), "Should return true when value found");
            assertEquals(4, list.size());
            assertEquals(40L, list.getLong(2));
        }

        @Test
        @DisplayName("removeLong returns false when value not found")
        void testRemoveLongNotFound() {
            assertFalse(list.removeLong(999L), "Should return false when value not found");
            assertEquals(5, list.size(), "Size should remain unchanged");
        }

        @Test
        @DisplayName("remove (by index) removes element")
        void testRemoveByIndex() {
            Long removed = list.remove(0);

            assertEquals(Long.valueOf(10L), removed);
            assertEquals(4, list.size());
            assertEquals(20L, list.getLong(0));
        }

        @Test
        @DisplayName("removeLong removes first occurrence when duplicates exist")
        void testRemoveLongWithDuplicates() {
            list.addLong(20L); // Add duplicate
            assertTrue(list.removeLong(20L));
            assertEquals(5, list.size());
            assertEquals(20L, list.getLong(4), "Second occurrence should remain");
        }
    }

    @Nested
    @DisplayName("Size and Empty Operations")
    class SizeOperations {

        @Test
        @DisplayName("size returns correct count")
        void testSize() {
            ObservableLongList list = new ObservableLongList();
            assertEquals(0, list.size());

            list.addLong(1L);
            assertEquals(1, list.size());

            list.addLong(2L);
            list.addLong(3L);
            assertEquals(3, list.size());
        }

        @Test
        @DisplayName("isEmpty returns true for empty list")
        void testIsEmpty() {
            ObservableLongList list = new ObservableLongList();
            assertTrue(list.isEmpty());

            list.addLong(1L);
            assertFalse(list.isEmpty());

            list.remove(0);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("clear removes all elements")
        void testClear() {
            ObservableLongList list = new ObservableLongList(1L, 2L, 3L, 4L, 5L);
            list.clear();

            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }
    }

    @Nested
    @DisplayName("Contains Operations")
    class ContainsOperations {

        private ObservableLongList list;

        @BeforeEach
        void setUp() {
            list = new ObservableLongList(10L, 20L, 30L, 40L, 50L);
        }

        @Test
        @DisplayName("contains returns true for existing boxed value")
        void testContains() {
            assertTrue(list.contains(30L));
            assertTrue(list.contains(10L));
            assertTrue(list.contains(50L));
        }

        @Test
        @DisplayName("contains returns false for non-existing value")
        void testContainsNonExisting() {
            assertFalse(list.contains(999L));
            assertFalse(list.contains(0L));
        }
    }
}
