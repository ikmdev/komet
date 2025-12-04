package dev.ikm.tinkar.collection;


import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * An abstract implementation of a set that only supports elements which are instances of an enumeration type.
 * The elements of the set are represented as bit flags for memory efficiency and quick operations.
 * This class ensures that the set is immutable by restricting how elements can be added or removed once created.
 *
 * @param <E> the type of enumeration elements contained in this set. The generic type must extend {@link Enum}.
 */
public abstract class ImmutableEnumSet<E extends Enum<E>>  implements Iterable<E> {
    private long bits = 0;

    /**
     * Constructs an immutable set of enumeration elements using a variable number of arguments.
     * Each element's ordinal value is used to calculate its position within an internal bit field,
     * ensuring efficient storage and quick membership checks.
     *
     * @param elements the enumeration elements to include in the set. These must belong to the same
     *        enumeration type and extend {@link Enum}.
     */
    protected ImmutableEnumSet(E... elements) {
        for (E element : elements) {
            bits |= (1L << element.ordinal());
        }
    }

    /**
     * Constructs an immutable set of enumeration elements using the elements provided in a collection.
     * Each element's ordinal value is used to calculate its position within an internal bit field,
     * optimizing storage and enabling efficient lookups.
     *
     * @param elements the collection of enumeration elements to include in the set.
     *                 These must belong to the same enumeration type and extend {@link Enum}.
     */
    protected ImmutableEnumSet(Collection<? extends E> elements) {
        for (E element : elements) {
            bits |= (1L << element.ordinal());
        }
    }

    /**
     * Checks if the specified enumeration element is present in this immutable enumeration set.
     *
     * @param element the enumeration element to check for membership in the set.
     *                The element must belong to the enumeration type represented by this set.
     * @return {@code true} if the specified element is present in the set, otherwise {@code false}.
     */
    public boolean contains(E element) {
        return (bits & (1L << element.ordinal())) != 0;
    }

    /**
     * Retrieves the class object associated with the enumeration type used in this set.
     * This method provides the concrete {@link Enum} type that defines the elements of the set.
     *
     * @return the {@link Class} representing the enumeration type used in this set.
     */
    protected abstract Class<E> enumClass();

    /**
     * Converts the immutable enumeration set to a modifiable {@link EnumSet}.
     * This method creates a new {@link EnumSet} containing all elements present
     * in the immutable set, ensuring that the result can be safely modified
     * without affecting the original immutable set.
     *
     * @return a modifiable {@link EnumSet} containing all the elements of the
     *         immutable enumeration set.
     */
    public EnumSet<E> toEnumSet() {
        EnumSet<E> result = EnumSet.noneOf(enumClass());
        for (E field : enumClass().getEnumConstants()) {
            if (contains(field)) {
                result.add(field);
            }
        }
        return result;
    }

    @Override
    public Iterator<E> iterator() {
        return toEnumSet().iterator();
    }
}
