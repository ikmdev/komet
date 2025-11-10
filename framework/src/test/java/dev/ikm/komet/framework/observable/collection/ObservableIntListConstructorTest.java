package dev.ikm.komet.framework.observable.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservableIntList Constructor Tests")
class ObservableIntListConstructorTest {

    @Test
    @DisplayName("Default constructor creates empty list")
    void testDefaultConstructor() {
        ObservableIntList list = new ObservableIntList();

        assertEquals(0, list.size(), "New list should be empty");
        assertTrue(list.isEmpty(), "New list should be empty");
    }

    @Test
    @DisplayName("Constructor with initial capacity creates empty list")
    void testConstructorWithInitialCapacity() {
        ObservableIntList list = new ObservableIntList(100);

        assertEquals(0, list.size(), "List with initial capacity should be empty");
        assertTrue(list.isEmpty(), "List with initial capacity should be empty");
    }

    @Test
    @DisplayName("Constructor with varargs creates list with specified values")
    void testConstructorWithVarargs() {
        ObservableIntList list = new ObservableIntList(1, 2, 3, 4, 5);

        assertEquals(5, list.size(), "List should contain 5 elements");
        assertEquals(1, list.getInt(0), "First element should be 1");
        assertEquals(2, list.getInt(1), "Second element should be 2");
        assertEquals(3, list.getInt(2), "Third element should be 3");
        assertEquals(4, list.getInt(3), "Fourth element should be 4");
        assertEquals(5, list.getInt(4), "Fifth element should be 5");
    }

    @Test
    @DisplayName("Constructor with empty varargs creates empty list")
    void testConstructorWithEmptyVarargs() {
        ObservableIntList list = new ObservableIntList(new int[]{});

        assertEquals(0, list.size(), "List should be empty");
        assertTrue(list.isEmpty(), "List should be empty");
    }

    @Test
    @DisplayName("Constructor with single value creates list with one element")
    void testConstructorWithSingleValue() {
        // Note: Single int argument is interpreted as initial capacity, not value
        // Must use array form to create list with single value
        ObservableIntList list = new ObservableIntList(new int[]{42});

        assertEquals(1, list.size(), "List should contain 1 element");
        assertEquals(42, list.getInt(0), "Element should be 42");
    }

    @Test
    @DisplayName("Constructor with negative values works correctly")
    void testConstructorWithNegativeValues() {
        ObservableIntList list = new ObservableIntList(-1, -2, -3);

        assertEquals(3, list.size(), "List should contain 3 elements");
        assertEquals(-1, list.getInt(0), "First element should be -1");
        assertEquals(-2, list.getInt(1), "Second element should be -2");
        assertEquals(-3, list.getInt(2), "Third element should be -3");
    }

    @Test
    @DisplayName("Constructor with zero initial capacity creates empty list")
    void testConstructorWithZeroCapacity() {
        ObservableIntList list = new ObservableIntList(0);

        assertEquals(0, list.size(), "List should be empty");
        assertTrue(list.isEmpty(), "List should be empty");
    }
}
