/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.tinkar.common.id.impl;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdCollection;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PublicIdCollections {
    /**
     * A "salt" value used for randomizing iteration order. This is initialized once
     * and stays constant for the lifetime of the JVM. It need not be truly random, but
     * it needs to vary sufficiently from one run to the next so that iteration order
     * will vary between JVM runs.
     */
    static final int SALT;
    static {
        long nt = System.nanoTime();
        SALT = (int)((nt >>> 32) ^ nt);
    }

    /** No instances. */
    private PublicIdCollections() { }

    /**
     * The reciprocal of load factor. Given a number of elements
     * to store, multiply by this factor to get the table size.
     */
    static final int EXPAND_FACTOR = 2;

    static UnsupportedOperationException uoe() { return new UnsupportedOperationException(); }

    static abstract class AbstractImmutableCollection<E extends PublicId> extends AbstractCollection<E>
    implements PublicIdCollection<E>
    {
        // all mutating methods throw UnsupportedOperationException
        @Override public boolean add(E e) { throw uoe(); }
        @Override public boolean addAll(Collection<? extends E> c) { throw uoe(); }
        @Override public void    clear() { throw uoe(); }
        @Override public boolean remove(Object o) { throw uoe(); }
        @Override public boolean removeAll(Collection<?> c) { throw uoe(); }
        @Override public boolean removeIf(Predicate<? super E> filter) { throw uoe(); }
        @Override public boolean retainAll(Collection<?> c) { throw uoe(); }

        @Override
        public Stream<E> stream() {
            return StreamSupport.stream(spliterator(), false);
        }
    }

    // ---------- List Implementations ----------

    // make a copy, short-circuiting based on implementation class
    @SuppressWarnings("unchecked")
    static <E> List<E> listCopy(Collection<? extends E> coll) {
        if (coll instanceof PublicIdCollections.AbstractImmutableList && coll.getClass() != PublicIdCollections.SubList.class) {
            return (List<E>)coll;
        } else {
            return (List<E>)List.of(coll.toArray());
        }
    }

    @SuppressWarnings("unchecked")
    static <E extends PublicId> PublicIdList<E> emptyList() {
        return (PublicIdList<E>) PublicIdCollections.ListN.EMPTY_LIST;
    }

    static abstract class AbstractImmutableList<E extends PublicId>
            extends PublicIdCollections.AbstractImmutableCollection<E>
            implements PublicIdList<E>, List<E>, RandomAccess {

        // all mutating methods throw UnsupportedOperationException
        @Override public void    add(int index, E element) { throw uoe(); }
        @Override public boolean addAll(int index, Collection<? extends E> c) { throw uoe(); }
        @Override public E       remove(int index) { throw uoe(); }
        @Override public void    replaceAll(UnaryOperator<E> operator) { throw uoe(); }
        @Override public E       set(int index, E element) { throw uoe(); }
        @Override public void    sort(Comparator<? super E> c) { throw uoe(); }

        @Override
        public AbstractImmutableList<E> subList(int fromIndex, int toIndex) {
            int size = size();
            subListRangeCheck(fromIndex, toIndex, size);
            return PublicIdCollections.SubList.fromList(this, fromIndex, toIndex);
        }

        static void subListRangeCheck(int fromIndex, int toIndex, int size) {
            if (fromIndex < 0)
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            if (toIndex > size)
                throw new IndexOutOfBoundsException("toIndex = " + toIndex);
            if (fromIndex > toIndex)
                throw new IllegalArgumentException("fromIndex(" + fromIndex +
                        ") > toIndex(" + toIndex + ")");
        }

        @Override
        public Iterator<E> iterator() {
            return new PublicIdCollections.ListItr<E>(this, size());
        }

        @Override
        public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        @Override
        public ListIterator<E> listIterator(final int index) {
            int size = size();
            if (index < 0 || index > size) {
                throw outOfBounds(index);
            }
            return new PublicIdCollections.ListItr<E>(this, size, index);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof List)) {
                return false;
            }

            Iterator<?> oit = ((List<?>) o).iterator();
            for (int i = 0, s = size(); i < s; i++) {
                if (!oit.hasNext() || !get(i).equals(oit.next())) {
                    return false;
                }
            }
            return !oit.hasNext();
        }

        @Override
        public int indexOf(Object o) {
            Objects.requireNonNull(o);
            for (int i = 0, s = size(); i < s; i++) {
                if (o.equals(get(i))) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            Objects.requireNonNull(o);
            for (int i = size() - 1; i >= 0; i--) {
                if (o.equals(get(i))) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int hashCode() {
            int hash = 1;
            for (int i = 0, s = size(); i < s; i++) {
                hash = 31 * hash + get(i).hashCode();
            }
            return hash;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        IndexOutOfBoundsException outOfBounds(int index) {
            return new IndexOutOfBoundsException("Index: " + index + " Size: " + size());
        }
    }

    static final class ListItr<E> implements ListIterator<E> {

        
        private final List<E> list;

        
        private final int size;

        
        private final boolean isListIterator;

        private int cursor;

        ListItr(List<E> list, int size) {
            this.list = list;
            this.size = size;
            this.cursor = 0;
            isListIterator = false;
        }

        ListItr(List<E> list, int size, int index) {
            this.list = list;
            this.size = size;
            this.cursor = index;
            isListIterator = true;
        }

        public boolean hasNext() {
            return cursor != size;
        }

        public E next() {
            try {
                int i = cursor;
                E next = list.get(i);
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw uoe();
        }

        public boolean hasPrevious() {
            if (!isListIterator) {
                throw uoe();
            }
            return cursor != 0;
        }

        public E previous() {
            if (!isListIterator) {
                throw uoe();
            }
            try {
                int i = cursor - 1;
                E previous = list.get(i);
                cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            if (!isListIterator) {
                throw uoe();
            }
            return cursor;
        }

        public int previousIndex() {
            if (!isListIterator) {
                throw uoe();
            }
            return cursor - 1;
        }

        public void set(E e) {
            throw uoe();
        }

        public void add(E e) {
            throw uoe();
        }
    }

    static final class SubList<E extends PublicId> extends PublicIdCollections.AbstractImmutableList<E>
            implements RandomAccess {


        private final List<E> root;


        private final int offset;


        private final int size;

        private SubList(List<E> root, int offset, int size) {
            this.root = root;
            this.offset = offset;
            this.size = size;
        }

        /**
         * Constructs a sublist of another SubList.
         */
        static <E extends PublicId> PublicIdCollections.SubList<E> fromSubList(PublicIdCollections.SubList<E> parent, int fromIndex, int toIndex) {
            return new PublicIdCollections.SubList<>(parent.root, parent.offset + fromIndex, toIndex - fromIndex);
        }

        /**
         * Constructs a sublist of an arbitrary AbstractImmutableList, which is
         * not a SubList itself.
         */
        static <E extends PublicId> PublicIdCollections.SubList<E> fromList(List<E> list, int fromIndex, int toIndex) {
            return new PublicIdCollections.SubList<>(list, fromIndex, toIndex - fromIndex);
        }

        public E get(int index) {
            Objects.checkIndex(index, size);
            return root.get(offset + index);
        }

        public int size() {
            return size;
        }

        public Iterator<E> iterator() {
            return new PublicIdCollections.ListItr<>(this, size());
        }

        public ListIterator<E> listIterator(int index) {
            rangeCheck(index);
            return new PublicIdCollections.ListItr<>(this, size(), index);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index > size) {
                throw outOfBounds(index);
            }
        }

        @Override
        public void forEach(Consumer<? super E> consumer) {
            iterator().forEachRemaining(consumer);
        }

        @Override
        public PublicId[] toIdArray() {
            return new PublicId[0];
        }

        @Override
        public boolean contains(PublicId value) {
            Iterator<E> itr = iterator();
            while (itr.hasNext()) {
                if (value.equals(itr.next())) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class List12<E extends PublicId> extends PublicIdCollections.AbstractImmutableList<E>
            {

        
        private final E e0;

        
        private final E e1;

        public List12(E e0) {
            this.e0 = Objects.requireNonNull(e0);
            this.e1 = null;
        }

        public List12(E e0, E e1) {
            this.e0 = Objects.requireNonNull(e0);
            this.e1 = Objects.requireNonNull(e1);
        }

        @Override
        public int size() {
            return e1 != null ? 2 : 1;
        }

        @Override
        public E get(int index) {
            if (index == 0) {
                return e0;
            } else if (index == 1 && e1 != null) {
                return e1;
            }
            throw outOfBounds(index);
        }

                @Override
                public void forEach(Consumer<? super E> consumer) {
                    if (e0 != null) {
                        consumer.accept(e0);
                    }
                    if (e1 != null) {
                        consumer.accept(e1);
                    }
                }

                @Override
                public PublicId[] toIdArray() {
                    if (e0 != null) {
                        if (e1 != null) {
                            return new PublicId[] {e0, e1};
                        }
                        return new PublicId[] {e0};
                    }
                    return new PublicId[0];
                }

                @Override
                public boolean contains(PublicId value) {
                    if (e0 != null) {
                        if (value.equals(e0)) {
                            return true;
                        }
                    }
                    if (e1 != null) {
                        return value.equals(e1);
                    }
                    return false;
                }
            }

    public static final class ListN<E extends PublicId> extends PublicIdCollections.AbstractImmutableList<E>
            {

        public static final PublicIdList EMPTY_LIST = new PublicIdCollections.ListN<>();

        
//        private final E[] elements;
        private final List<E> elements;

        @SafeVarargs
        public ListN(E... input) {
            elements = new ArrayList<>(input.length);
            elements.addAll(Arrays.asList(input));
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public E get(int index) {
            return elements.get(index);
        }

                @Override
                public void forEach(Consumer<? super E> consumer) {
                    for (E element: elements) {
                        if (element != null) {
                            consumer.accept(element);
                        }
                    }
                }

                @Override
                public PublicId[] toIdArray() {
                    return elements.toArray(new PublicId[0]);
                }

                @Override
                public boolean contains(PublicId value) {
                    for (E element: elements) {
                        if (value.equals(element)) {
                            return true;
                        }
                    }
                    return false;
                }
    }

    // ---------- Set Implementations ----------

    static abstract class AbstractImmutableSet<E extends PublicId> extends PublicIdCollections.AbstractImmutableCollection<E>
            implements Set<E>, PublicIdSet<E> {

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Set)) {
                return false;
            }

            Collection<?> c = (Collection<?>) o;
            if (c.size() != size()) {
                return false;
            }
            for (Object e : c) {
                if (e == null || !contains(e)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public abstract int hashCode();
    }

    @SuppressWarnings("unchecked")
    public static <E extends PublicId> Set<E> emptySet() {
        return (Set<E>) PublicIdCollections.SetN.EMPTY_SET;
    }

    public static final class Set12<E extends PublicId> extends PublicIdCollections.AbstractImmutableSet<E>
    {

        
        final E e0;
        
        final E e1;

        public Set12(E e0) {
            this.e0 = Objects.requireNonNull(e0);
            this.e1 = null;
        }

        public Set12(E e0, E e1) {
            if (e0.equals(Objects.requireNonNull(e1))) { // implicit nullcheck of e0
                throw new IllegalArgumentException("duplicate element: " + e0);
            }

            this.e0 = e0;
            this.e1 = e1;
        }

        @Override
        public int size() {
            return (e1 == null) ? 1 : 2;
        }

        @Override
        public boolean contains(Object o) {
            return o.equals(e0) || o.equals(e1); // implicit nullcheck of o
        }

        @Override
        public int hashCode() {
            return e0.hashCode() + (e1 == null ? 0 : e1.hashCode());
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {
                private int idx = size();

                @Override
                public boolean hasNext() {
                    return idx > 0;
                }

                @Override
                public E next() {
                    if (idx == 1) {
                        idx = 0;
                        return SALT >= 0 || e1 == null ? e0 : e1;
                    } else if (idx == 2) {
                        idx = 1;
                        return SALT >= 0 ? e1 : e0;
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }
        @Override
        public void forEach(Consumer<? super E> consumer) {
            if (e0 != null) {
                consumer.accept(e0);
            }
            if (e1 != null) {
                consumer.accept(e1);
            }
        }

        @Override
        public PublicId[] toIdArray() {
            if (e0 != null) {
                if (e1 != null) {
                    return new PublicId[] {e0, e1};
                }
                return new PublicId[] {e0};
            }
            return new PublicId[0];
        }

        @Override
        public boolean contains(PublicId value) {
            if (e0 != null) {
                if (value.equals(e0)) {
                    return true;
                }
            }
            if (e1 != null) {
                return value.equals(e1);
            }
            return false;
        }
    }

    /**
     * An array-based Set implementation. The element array must be strictly
     * larger than the size (the number of contained elements) so that at
     * least one null is always present.
     * @param <E> the element type
     */
    public static final class SetN<E extends PublicId> extends PublicIdCollections.AbstractImmutableSet<E>
        {

            public static final Set<?> EMPTY_SET = new PublicIdCollections.SetN<>();

        
        final PublicId[] elements;
        
        final int size;

        @SafeVarargs
        @SuppressWarnings("unchecked")
        public SetN(E... input) {
            size = input.length; // implicit nullcheck of input

            elements = new PublicId[EXPAND_FACTOR * input.length];
            for (int i = 0; i < input.length; i++) {
                E e = input[i];
                int idx = probe(e); // implicit nullcheck of e
                if (idx >= 0) {
                    throw new IllegalArgumentException("duplicate element: " + e);
                } else {
                    elements[-(idx + 1)] = e;
                }
            }
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            Objects.requireNonNull(o);
            return size > 0 && probe(o) >= 0;
        }

        private final class SetNIterator implements Iterator<E> {

            private int remaining;

            private int idx;

            SetNIterator() {
                remaining = size();
                if (remaining > 0) {
                    idx = Math.floorMod(SALT, elements.length);
                }
            }

            @Override
            public boolean hasNext() {
                return remaining > 0;
            }

            private int nextIndex() {
                int idx = this.idx;
                if (SALT >= 0) {
                    if (++idx >= elements.length) {
                        idx = 0;
                    }
                } else {
                    if (--idx < 0) {
                        idx = elements.length - 1;
                    }
                }
                return this.idx = idx;
            }

            @Override
            public E next() {
                if (hasNext()) {
                    E element;
                    // skip null elements
                    while ((element = (E) elements[nextIndex()]) == null) {}
                    remaining--;
                    return element;
                } else {
                    throw new NoSuchElementException();
                }
            }
        }

        @Override
        public Iterator<E> iterator() {
            return new PublicIdCollections.SetN.SetNIterator();
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (PublicId e : elements) {
                if (e != null) {
                    h += e.hashCode();
                }
            }
            return h;
        }

        // returns index at which element is present; or if absent,
        // (-i - 1) where i is location where element should be inserted.
        // Callers are relying on this method to perform an implicit nullcheck
        // of pe
        private int probe(Object pe) {
            int idx = Math.floorMod(pe.hashCode(), elements.length);
            while (true) {
                E ee = (E) elements[idx];
                if (ee == null) {
                    return -idx - 1;
                } else if (pe.equals(ee)) {
                    return idx;
                } else if (++idx == elements.length) {
                    idx = 0;
                }
            }
        }
            @Override
            public void forEach(Consumer<? super E> consumer) {
                for (PublicId element: elements) {
                    if (element != null) {
                        consumer.accept((E) element);
                    }

                }
            }

            @Override
            public PublicId[] toIdArray() {
                return elements;
            }

            @Override
            public boolean contains(PublicId value) {
                for (PublicId element: elements) {
                    if (value.equals(element)) {
                        return true;
                    }
                }
                return false;
            }
        }

}
