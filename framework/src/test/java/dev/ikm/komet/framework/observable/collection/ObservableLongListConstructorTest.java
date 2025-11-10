package dev.ikm.komet.framework.observable.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservableLongList Constructor Tests")
class ObservableLongListConstructorTest {

    @Test
    @DisplayName("Default constructor creates empty list")
    void testDefaultConstructor() {
        ObservableLongList list = new ObservableLongList();

        assertEquals(0, list.size(), "New list should be empty");
        assertTrue(list.isEmpty(), "New list should be empty");
    }

    @Test
    @DisplayName("Constructor with initial capacity creates empty list")
    void testConstructorWithInitialCapacity() {
        ObservableLongList list = new ObservableLongList(100);

        assertEquals(0, list.size(), "List with initial capacity should be empty");
        assertTrue(list.isEmpty(), "List with initial capacity should be empty");
    }

    @Test
    @DisplayName("Constructor with varargs creates list with specified values")
    void testConstructorWithVarargs() {
        ObservableLongList list = new ObservableLongList(1L, 2L, 3L, 4L, 5L);

        assertEquals(5, list.size(), "List should contain 5 elements");
        assertEquals(1L, list.getLong(0), "First element should be 1");
        assertEquals(2L, list.getLong(1), "Second element should be 2");
        assertEquals(3L, list.getLong(2), "Third element should be 3");
        assertEquals(4L, list.getLong(3), "Fourth element should be 4");
        assertEquals(5L, list.getLong(4), "Fifth element should be 5");
    }

    @Test
    @DisplayName("Constructor with empty varargs creates empty list")
    void testConstructorWithEmptyVarargs() {
        ObservableLongList list = new ObservableLongList(new long[]{});

        assertEquals(0, list.size(), "List should be empty");
        assertTrue(list.isEmpty(), "List should be empty");
    }

    @Test
    @DisplayName("Constructor with single value creates list with one element")
    void testConstructorWithSingleValue() {
        // Note: Single int argument is interpreted as initial capacity, not value
        // Must use array form to create list with single value
        ObservableLongList list = new ObservableLongList(new long[]{42L});

        assertEquals(1, list.size(), "List should contain 1 element");
        assertEquals(42L, list.getLong(0), "Element should be 42");
    }

    @Test
    @DisplayName("Constructor with negative values works correctly")
    void testConstructorWithNegativeValues() {
        ObservableLongList list = new ObservableLongList(-1L, -2L, -3L);

        assertEquals(3, list.size(), "List should contain 3 elements");
        assertEquals(-1L, list.getLong(0), "First element should be -1");
        assertEquals(-2L, list.getLong(1), "Second element should be -2");
        assertEquals(-3L, list.getLong(2), "Third element should be -3");
    }

    @Test
    @DisplayName("Constructor with zero initial capacity creates empty list")
    void testConstructorWithZeroCapacity() {
        ObservableLongList list = new ObservableLongList(0);

        assertEquals(0, list.size(), "List should be empty");
        assertTrue(list.isEmpty(), "List should be empty");
    }

    @Test
    @DisplayName("Constructor with large values works correctly")
    void testConstructorWithLargeValues() {
        ObservableLongList list = new ObservableLongList(
            Long.MAX_VALUE,
            Long.MIN_VALUE,
            1234567890123456789L
        );

        assertEquals(3, list.size(), "List should contain 3 elements");
        assertEquals(Long.MAX_VALUE, list.getLong(0), "First element should be Long.MAX_VALUE");
        assertEquals(Long.MIN_VALUE, list.getLong(1), "Second element should be Long.MIN_VALUE");
        assertEquals(1234567890123456789L, list.getLong(2), "Third element should be 1234567890123456789L");
    }
}
