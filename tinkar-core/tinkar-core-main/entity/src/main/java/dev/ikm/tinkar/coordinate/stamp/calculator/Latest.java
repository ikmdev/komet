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
package dev.ikm.tinkar.coordinate.stamp.calculator;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 *
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 *
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */


//~--- JDK imports ------------------------------------------------------------

import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LatestVersion.
 *
 * @param <V> the value type
 *                                                                                                                                                                                                                                                                                                                                TODO [KEC] search for all get() methods to make sure test for isPresent() is completed.
 * 
 */
public final class Latest<V> {

    private static final Latest<?> EMPTY = new Latest<>();
    /**
     * The value.
     */
    V value;

    /**
     * The contradictions.
     */
    ImmutableSet<V> contradictions;

    //~--- constructors --------------------------------------------------------
    public Latest() {
    }

    /**
     * Instantiates a new latest version.
     *
     * @param versionType the version type
     */
    public Latest(Class<V> versionType) {
    }

    /**
     * Instantiates a new latest version.
     *
     * @param versions the versions
     */
    public Latest(List<V> versions) {
        this.value = Objects.requireNonNull(versions.get(0), "latest version cannot be null");

        if (versions.size() < 2) {
            this.contradictions = null;
        } else {
            this.contradictions = Sets.immutable.ofAll(versions.subList(1, versions.size()));
        }
    }

    /**
     * Instantiates a new latest version.
     *
     * @param latest the latest
     */
    public Latest(V latest) {
        this.value = Objects.requireNonNull(latest, "latest version cannot be null");
        this.contradictions = null;
    }

    /**
     * Instantiates a new latest version.
     *
     * @param latest         the latest
     * @param contradictions the contradictions
     */
    public Latest(V latest, Collection<V> contradictions) {
        this.value = latest;

        if (contradictions == null) {
            this.contradictions = null;
        } else {
            this.contradictions = Sets.immutable.ofAll(contradictions);
        }
    }

    //~--- methods -------------------------------------------------------------

    public static <T> Latest<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    public static <T> Latest<T> empty() {
        @SuppressWarnings("unchecked")
        Latest<T> t = (Latest<T>) EMPTY;
        return t;
    }

    public static <T> Latest<T> of(T value) {
        return new Latest<>(value);
    }

    /**
     * Adds the latest.
     *
     * @param value the value
     */
    public void addLatest(V value) {
        if (this.value == null) {
            this.value = value;
        } else {
            if (this.contradictions == null) {
                this.contradictions = Sets.immutable.of(value);
            } else {
                MutableSet<V> tempContradictions = Sets.mutable.ofAll(this.contradictions);
                tempContradictions.add(value);
                this.contradictions = tempContradictions.toImmutable();
            }
        }
    }

    /**
     * @param consumer the consumer to process the value if it is present.
     * @return the latest version unmodified for use in a fluent API manner.
     */
    public Latest<V> ifPresent(Consumer<? super V> consumer) {
        if (value != null) {
            consumer.accept(this.value);
        }
        return this;
    }

    public void ifPresentOrElse(Consumer<? super V> action, Runnable emptyAction) {
        if (value != null) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    public <R extends Object> R ifAbsentOrFunction(Supplier<R> ifAbsent, Function<V, R> ifPresent) {
        if (isPresent()) {
            return ifPresent.apply(value);
        }
        return ifAbsent.get();
    }

    /**
     * Return true if there is a value present, otherwise false.
     *
     * @return true if there is a value present, otherwise false.
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Return false if there is no value present, otherwise, passes the value into the supplied customCheck, and returns that response.
     *
     * @param customCheck The test to run, if the value is present.
     * @return true if present and customCheck returns true, otherwise, false.
     */
    public boolean isPresentAnd(Predicate<V> customCheck) {
        if (value == null) {
            return false;
        } else {
            return customCheck.test(value);
        }
    }

    /**
     * Return true if there is a value absent, otherwise false.
     *
     * @return true if the value absent, otherwise false.
     */
    public boolean isAbsent() {
        return value == null;
    }

    /**
     * Return the value if present, otherwise return other.
     *
     * @param other
     * @return the value if present, otherwise return other.
     */
    public V orElse(V other) {
        if (this.value != null) {
            return this.value;
        }
        return other;
    }

    /**
     * Return the value if present, otherwise invoke other and return the result of that invocation.
     *
     * @param other
     * @return the value if present, otherwise invoke other and return the result of that invocation.
     */
    public V orElseGet(Supplier<? extends V> other) {
        if (this.value != null) {
            return this.value;
        }
        return other.get();
    }

    /**
     * Execute the runnable to execute if the value is present.
     *
     * @param runnable the runnable to execute if the value is present
     * @return the latest version unmodified for use in a fluent API manner.
     */
    public Latest<V> ifAbsent(Runnable runnable) {
        if (value == null) {
            runnable.run();
        }
        return this;
    }

    /**
     * Return the contained value, if present, otherwise throw an exception to be created by the provided supplier.
     *
     * @param <X>               Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to be thrown
     * @return the present value
     * @throws X if there is no value present
     */
    public <X extends Throwable> V orElseThrow(Supplier<? extends X> exceptionSupplier)
            throws X {
        if (this.value != null) {
            return this.value;
        }
        throw exceptionSupplier.get();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "LatestVersion«" + this.value + ", contradictions: " + contradictions() + '»';
    }

    /**
     * Read-only set of contradictions.
     *
     * @return the contradictions
     */
    public ImmutableSet<V> contradictions() {
        if (this.contradictions == null) {
            return Sets.immutable.empty();
        }
        return this.contradictions;
    }

    public String toStringOr(Function<V, String> toStringFunction, String ifMissing) {
        return isPresent() ? toStringFunction.apply(value) : ifMissing;
    }

    /**
     * The latest version value
     *
     * @return the latest version
     * @throws NoSuchElementException - if there is no value present
     * @see this.isPresent()
     */
    public V get() {
        if (this.value == null) {
            throw new NoSuchElementException();
        }
        return this.value;
    }

    /**
     * @return all latest versions, including the contradictions.
     */
    public ImmutableList<V> getWithContradictions() {
        if (this.value == null) {
            throw new NoSuchElementException();
        }
        if (contradictions != null) {
            MutableList tempList = Lists.mutable.withAll(contradictions);
            tempList.add(this.value);
            return tempList.toImmutable();
        }
        return Lists.immutable.with(this.value);
    }

    /**
     * If a value is present, and the value matches the given predicate, return an Optional describing the value, otherwise return an empty Optional.
     *
     * @param predicate a predicate to apply to the value, if present
     * @return an Optional describing the value of this Optional if a value is present and the value matches the given predicate, otherwise an empty Optional
     */
    public Latest<V> filter(Predicate<Latest<V>> predicate) {
        if (predicate.test(this)) {
            return this;
        }
        return new Latest<>();
    }

    /**
     * If a value is present, apply the provided mapping function to it, and if the result is non-null,
     * return an Optional describing the result. Otherwise return an empty Optional.
     *
     * @param <U>    The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return an Optional describing the result of applying a mapping function to the value of this Optional, if a value is present, otherwise an empty Optional
     */
    public <U> Latest<U> map(Function<? super Latest<V>, ? extends Latest<U>> mapper) {
        return mapper.apply(this);
    }

    /**
     * Stream of the latest values (if more that one latest value is computed, then
     * all are included in this stream), including all contradictions.
     *
     * @return the stream
     */
    public Stream<V> versionStream() {
        final Stream.Builder<V> builder = Stream.builder();

        if (this.value == null) {
            return Stream.<V>builder()
                    .build();
        }

        builder.accept(this.value);

        if (this.contradictions != null) {
            this.contradictions.forEach((contradiction) -> {
                builder.add(contradiction);
            });
        }

        return builder.build();
    }

    public ImmutableList<V> versionList() {
        if (this.value == null) {
            return Lists.immutable.empty();
        }
        if (this.contradictions == null) {
            return Lists.immutable.of(this.value);
        }
        MutableList versions = Lists.mutable.ofInitialCapacity(this.contradictions.size() + 1);
        versions.add(value);
        versions.addAll(this.contradictions.castToSet());
        return versions.toImmutable();
    }

    public boolean isContradicted() {
        if (this.contradictions == null) {
            return false;
        }
        return !this.contradictions.isEmpty();
    }

    public void sortByState() {
        if (value != null && value instanceof EntityVersion entityVersion) {
            if (entityVersion.stamp().state() != State.ACTIVE) {
                //See if we have an active one to swap it with.
                if (contradictions != null) {
                    MutableSet<V> mutableContradictions = Sets.mutable.ofAll(contradictions);
                    for (V c : contradictions) {
                        if (entityVersion.stamp().state() == State.ACTIVE) {
                            mutableContradictions.remove(c);
                            mutableContradictions.add(value);
                            value = c;
                            break;
                        }
                    }
                    this.contradictions = mutableContradictions.toImmutable();
                }
            }
        }
    }

    public IntIdSet stampNids() {
        if (contradictions == null) {
            if (value instanceof EntityVersion EntityVersion) {
                return IntIds.set.of(EntityVersion.stampNid());
            }
            throw new IllegalStateException("value not instanceof EntityVersion: " + value);
        }
        MutableIntList intList = IntLists.mutable.withInitialCapacity(contradictions.size() + 1);
        if (value instanceof EntityVersion entityVersion) {
            intList.add(entityVersion.stampNid());
        } else {
            throw new IllegalStateException("value not instanceof EntityVersion: " + value);
        }
        for (V version : contradictions) {
            if (version instanceof EntityVersion contradictionVersion) {
                intList.add(contradictionVersion.stampNid());
            } else {
                throw new IllegalStateException("value not instanceof EntityVersion: " + version);
            }
        }
        return IntIds.set.of(intList.toArray());
    }
}

