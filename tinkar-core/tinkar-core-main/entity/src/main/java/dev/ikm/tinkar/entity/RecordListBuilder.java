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
package dev.ikm.tinkar.entity;

import org.eclipse.collections.api.BooleanIterable;
import org.eclipse.collections.api.ByteIterable;
import org.eclipse.collections.api.CharIterable;
import org.eclipse.collections.api.DoubleIterable;
import org.eclipse.collections.api.FloatIterable;
import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.LongIterable;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.ShortIterable;
import org.eclipse.collections.api.annotation.Beta;
import org.eclipse.collections.api.bag.ImmutableBag;
import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.api.bag.MutableBagIterable;
import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.api.block.HashingStrategy;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.function.Function0;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.function.primitive.BooleanFunction;
import org.eclipse.collections.api.block.function.primitive.ByteFunction;
import org.eclipse.collections.api.block.function.primitive.CharFunction;
import org.eclipse.collections.api.block.function.primitive.DoubleFunction;
import org.eclipse.collections.api.block.function.primitive.DoubleObjectToDoubleFunction;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.eclipse.collections.api.block.function.primitive.FloatObjectToFloatFunction;
import org.eclipse.collections.api.block.function.primitive.IntFunction;
import org.eclipse.collections.api.block.function.primitive.IntObjectToIntFunction;
import org.eclipse.collections.api.block.function.primitive.LongFunction;
import org.eclipse.collections.api.block.function.primitive.LongObjectToLongFunction;
import org.eclipse.collections.api.block.function.primitive.ObjectIntToObjectFunction;
import org.eclipse.collections.api.block.function.primitive.ShortFunction;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.predicate.Predicate2;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.block.procedure.Procedure2;
import org.eclipse.collections.api.block.procedure.primitive.ObjectIntProcedure;
import org.eclipse.collections.api.collection.primitive.MutableBooleanCollection;
import org.eclipse.collections.api.collection.primitive.MutableByteCollection;
import org.eclipse.collections.api.collection.primitive.MutableCharCollection;
import org.eclipse.collections.api.collection.primitive.MutableDoubleCollection;
import org.eclipse.collections.api.collection.primitive.MutableFloatCollection;
import org.eclipse.collections.api.collection.primitive.MutableIntCollection;
import org.eclipse.collections.api.collection.primitive.MutableLongCollection;
import org.eclipse.collections.api.collection.primitive.MutableShortCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.ParallelListIterable;
import org.eclipse.collections.api.list.primitive.ImmutableBooleanList;
import org.eclipse.collections.api.list.primitive.ImmutableByteList;
import org.eclipse.collections.api.list.primitive.ImmutableCharList;
import org.eclipse.collections.api.list.primitive.ImmutableDoubleList;
import org.eclipse.collections.api.list.primitive.ImmutableFloatList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.list.primitive.ImmutableShortList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.MutableMapIterable;
import org.eclipse.collections.api.map.primitive.ImmutableObjectDoubleMap;
import org.eclipse.collections.api.map.primitive.ImmutableObjectLongMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.api.multimap.list.ImmutableListMultimap;
import org.eclipse.collections.api.ordered.OrderedIterable;
import org.eclipse.collections.api.partition.list.PartitionImmutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.api.stack.MutableStack;
import org.eclipse.collections.api.tuple.Pair;

import java.util.Collection;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class RecordListBuilder<T> implements ImmutableList<T> {
    private volatile ImmutableList<T> immutableList;
    private volatile MutableList<T> mutableList = Lists.mutable.withInitialCapacity(2);

    public static RecordListBuilder make() {
        return new RecordListBuilder();
    }

    public RecordListBuilder<T> removeIf(Predicate<T> condition) {
        Iterator<T> i = mutableList.iterator();
        while (i.hasNext()) {
            if (condition.test(i.next())) {
                i.remove();
            }
        }
        return this;
    }

    public RecordListBuilder<T> add(T element) {
        if (mutableList != null) {
            mutableList.add(element);
        } else {
            throw new IllegalStateException("Cannot add to list. Immutable list has already been built. ");
        }
        return this;
    }

    public ImmutableList<T> addAndBuild(T element) {
        if (mutableList != null) {
            mutableList.add(element);
            build();
            return this;
        } else {
            throw new IllegalStateException("Cannot add to list. Immutable list has already been built. ");
        }
    }

    public ImmutableList<T> build() {
        if (mutableList != null) {
            immutableList = mutableList.toImmutable();
            mutableList = null;
        }
        return this;
    }

    public RecordListBuilder<T> with(T element) {
        if (mutableList != null) {
            mutableList.add(element);
        } else {
            throw new IllegalStateException("Cannot add to list. Immutable list has already been built. ");
        }
        return this;
    }

    @Override
    public ImmutableList<T> newWith(T t) {
        if (immutableList == null) {
            build();
        }
        return immutableList.newWith(t);
    }

    @Override
    public ImmutableList<T> newWithout(T t) {
        if (immutableList == null) {
            return mutableList.toImmutable().newWithout(t);
        }
        return immutableList.newWithout(t);
    }


    @Override
    public ImmutableList<T> newWithAll(Iterable<? extends T> iterable) {
        if (immutableList == null) {
            return mutableList.toImmutable().newWithAll(iterable);
        }
        return immutableList.newWithAll(iterable);
    }

    @Override
    public ImmutableList<T> newWithoutAll(Iterable<? extends T> iterable) {
        if (immutableList == null) {
            return mutableList.toImmutable().newWithoutAll(iterable);
        }
        return immutableList.newWithoutAll(iterable);
    }

    @Override
    public ImmutableList<T> tap(Procedure<? super T> procedure) {
        if (immutableList == null) {
            return mutableList.toImmutable().tap(procedure);
        };
        return immutableList.tap(procedure);
    }

    @Override
    public ImmutableList<T> select(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().select(predicate);
        }
        return immutableList.select(predicate);
    }

    @Override
    public <P> ImmutableList<T> selectWith(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().selectWith(predicate2, p);
        }
        return immutableList.selectWith(predicate2, p);
    }

    @Override
    public ImmutableList<T> reject(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().reject(predicate);
        }
        return immutableList.reject(predicate);
    }

    @Override
    public <P> ImmutableList<T> rejectWith(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().rejectWith(predicate2, p);
        }
        return immutableList.rejectWith(predicate2, p);
    }

    @Override
    public PartitionImmutableList<T> partition(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().partition(predicate);
        }
        return immutableList.partition(predicate);
    }

    @Override
    public <P> PartitionImmutableList<T> partitionWith(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().partitionWith(predicate2, p);
        }
        return immutableList.partitionWith(predicate2, p);
    }

    @Override
    public <S> ImmutableList<S> selectInstancesOf(Class<S> aClass) {
        if (immutableList == null) {
            return mutableList.toImmutable().selectInstancesOf(aClass);
        }
        return immutableList.selectInstancesOf(aClass);
    }

    @Override
    public <V> ImmutableList<V> collect(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().collect(function);
        }
        return immutableList.collect(function);
    }

    @Override
    public <V> ImmutableList<V> collectWithIndex(ObjectIntToObjectFunction<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectWithIndex(function);
        }
        return immutableList.collectWithIndex(function);
    }

    @Override
    public ImmutableBooleanList collectBoolean(BooleanFunction<? super T> booleanFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectBoolean(booleanFunction);
        }
        ;
        return immutableList.collectBoolean(booleanFunction);
    }

    @Override
    public ImmutableByteList collectByte(ByteFunction<? super T> byteFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectByte(byteFunction);
        }
        return immutableList.collectByte(byteFunction);
    }

    @Override
    public ImmutableCharList collectChar(CharFunction<? super T> charFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectChar(charFunction);
        }
        return immutableList.collectChar(charFunction);
    }

    @Override
    public ImmutableDoubleList collectDouble(DoubleFunction<? super T> doubleFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectDouble(doubleFunction);
        }
        return immutableList.collectDouble(doubleFunction);
    }

    @Override
    public ImmutableFloatList collectFloat(FloatFunction<? super T> floatFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectFloat(floatFunction);
        }

        return immutableList.collectFloat(floatFunction);
    }

    @Override
    public ImmutableIntList collectInt(IntFunction<? super T> intFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectInt(intFunction);
        }
        return immutableList.collectInt(intFunction);
    }

    @Override
    public ImmutableLongList collectLong(LongFunction<? super T> longFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectLong(longFunction);
        }
        return immutableList.collectLong(longFunction);
    }

    @Override
    public ImmutableShortList collectShort(ShortFunction<? super T> shortFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectShort(shortFunction);
        }
        return immutableList.collectShort(shortFunction);
    }

    @Override
    public <P, V> ImmutableList<V> collectWith(Function2<? super T, ? super P, ? extends V> function2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectWith(function2, p);
        }
        return immutableList.collectWith(function2, p);
    }

    @Override
    public <V> ImmutableList<V> collectIf(Predicate<? super T> predicate, Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectIf(predicate, function);
        }

        return immutableList.collectIf(predicate, function);
    }

    @Override
    public <V> ImmutableList<V> flatCollect(Function<? super T, ? extends Iterable<V>> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollect(function);
        }

        return immutableList.flatCollect(function);
    }

    @Override
    public <P, V> ImmutableList<V> flatCollectWith(Function2<? super T, ? super P, ? extends Iterable<V>> function, P parameter) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectWith(function, parameter);
        }

        return immutableList.flatCollectWith(function, parameter);
    }

    @Override
    public <V> ImmutableListMultimap<V, T> groupBy(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().groupBy(function);
        }
        return immutableList.groupBy(function);
    }

    @Override
    public <V> ImmutableListMultimap<V, T> groupByEach(Function<? super T, ? extends Iterable<V>> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().groupByEach(function);
        }
        return immutableList.groupByEach(function);
    }

    @Override
    public ImmutableList<T> distinct() {
        if (immutableList == null) {
            return mutableList.toImmutable().distinct();
        }
        return immutableList.distinct();
    }

    @Override
    public ImmutableList<T> distinct(HashingStrategy<? super T> hashingStrategy) {
        if (immutableList == null) {
            return mutableList.toImmutable().distinct(hashingStrategy);
        }
        return immutableList.distinct(hashingStrategy);
    }

    @Override
    public <V> ImmutableList<T> distinctBy(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().distinctBy(function);
        }
        return immutableList.distinctBy(function);
    }

    @Override
    public <S> ImmutableList<Pair<T, S>> zip(Iterable<S> iterable) {
        if (immutableList == null) {
            return mutableList.toImmutable().zip(iterable);
        }
        return immutableList.zip(iterable);
    }

    @Override
    public ImmutableList<Pair<T, Integer>> zipWithIndex() {
        if (immutableList == null) {
            return mutableList.toImmutable().zipWithIndex();
        }
        return immutableList.zipWithIndex();
    }

    @Override
    public ImmutableList<T> take(int i) {
        if (immutableList == null) {
            return mutableList.toImmutable().take(i);
        }
        return immutableList.take(i);
    }

    @Override
    public ImmutableList<T> takeWhile(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().takeWhile(predicate);
        }
        return immutableList.takeWhile(predicate);
    }

    @Override
    public ImmutableList<T> drop(int i) {
        if (immutableList == null) {
            return mutableList.toImmutable().drop(i);
        }
        return immutableList.drop(i);
    }

    @Override
    public ImmutableList<T> dropWhile(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().dropWhile(predicate);
        }
        return immutableList.dropWhile(predicate);
    }

    @Override
    public PartitionImmutableList<T> partitionWhile(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().partitionWhile(predicate);
        }
        return immutableList.partitionWhile(predicate);
    }

    @Override
    public List<T> castToList() {
        if (immutableList == null) {
            return mutableList.toImmutable().castToList();
        }

        return immutableList.castToList();
    }

    @Override
    public ImmutableList<T> subList(int i, int i1) {
        if (immutableList == null) {
            return mutableList.toImmutable().subList(i, i1);
        }

        return immutableList.subList(i, i1);
    }

    @Override
    public ImmutableList<T> toReversed() {
        if (immutableList == null) {
            return mutableList.toImmutable().toReversed();
        }

        return immutableList.toReversed();
    }

    @Override
    public <V> ImmutableObjectLongMap<V> sumByInt(Function<? super T, ? extends V> function, IntFunction<? super T> intFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().sumByInt(function, intFunction);
        }

        return immutableList.sumByInt(function, intFunction);
    }

    @Override
    public <V> ImmutableObjectDoubleMap<V> sumByFloat(Function<? super T, ? extends V> function, FloatFunction<? super T> floatFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().sumByFloat(function, floatFunction);
        }

        return immutableList.sumByFloat(function, floatFunction);
    }

    @Override
    public <V> ImmutableObjectLongMap<V> sumByLong(Function<? super T, ? extends V> function, LongFunction<? super T> longFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().sumByLong(function, longFunction);
        }

        return immutableList.sumByLong(function, longFunction);
    }

    @Override
    public <V> ImmutableObjectDoubleMap<V> sumByDouble(Function<? super T, ? extends V> function, DoubleFunction<? super T> doubleFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().sumByDouble(function, doubleFunction);
        }

        return immutableList.sumByDouble(function, doubleFunction);
    }

    @Override
    public <V> ImmutableBag<V> countBy(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().countBy(function);
        }

        return immutableList.countBy(function);
    }

    @Override
    public <V, P> ImmutableBag<V> countByWith(Function2<? super T, ? super P, ? extends V> function, P parameter) {
        if (immutableList == null) {
            return mutableList.toImmutable().countByWith(function, parameter);
        }

        return immutableList.countByWith(function, parameter);
    }

    @Override
    public <V> ImmutableBag<V> countByEach(Function<? super T, ? extends Iterable<V>> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().countByEach(function);
        }

        return immutableList.countByEach(function);
    }

    @Override
    public <V> ImmutableMap<V, T> groupByUniqueKey(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().groupByUniqueKey(function);
        }

        return immutableList.groupByUniqueKey(function);
    }

    @Override
    public <K, V> ImmutableMap<K, V> aggregateInPlaceBy(Function<? super T, ? extends K> groupBy, Function0<? extends V> zeroValueFactory, Procedure2<? super V, ? super T> mutatingAggregator) {
        if (immutableList == null) {
            return mutableList.toImmutable().aggregateInPlaceBy(groupBy, zeroValueFactory, mutatingAggregator);
        }

        return immutableList.aggregateInPlaceBy(groupBy, zeroValueFactory, mutatingAggregator);
    }

    @Override
    public <K, V> ImmutableMap<K, V> aggregateBy(Function<? super T, ? extends K> groupBy, Function0<? extends V> zeroValueFactory, Function2<? super V, ? super T, ? extends V> nonMutatingAggregator) {
        if (immutableList == null) {
            return mutableList.toImmutable().aggregateBy(groupBy, zeroValueFactory, nonMutatingAggregator);
        }

        return immutableList.aggregateBy(groupBy, zeroValueFactory, nonMutatingAggregator);
    }

    @Override
    public Stream<T> stream() {
        if (immutableList == null) {
            return mutableList.toImmutable().stream();
        }

        return immutableList.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        if (immutableList == null) {
            return mutableList.toImmutable().parallelStream();
        }

        return immutableList.parallelStream();
    }

    @Override
    public Spliterator<T> spliterator() {
        if (immutableList == null) {
            return mutableList.toImmutable().spliterator();
        }

        return immutableList.spliterator();
    }

    @Override
    public Collection<T> castToCollection() {
        if (immutableList == null) {
            return mutableList.toImmutable().castToCollection();
        }

        return immutableList.castToCollection();
    }

    @Override
    public void forEach(Procedure<? super T> procedure) {
        if (immutableList == null) {
            mutableList.toImmutable().forEach(procedure);
        } else {
            immutableList.forEach(procedure);
        }
    }

    @Override
    public int size() {
        if (immutableList == null) {
            return mutableList.toImmutable().size();
        }

        return immutableList.size();
    }

    @Override
    public boolean isEmpty() {
        if (immutableList == null) {
            return mutableList.toImmutable().isEmpty();
        }

        return immutableList.isEmpty();
    }

    @Override
    public boolean notEmpty() {
        if (immutableList == null) {
            return mutableList.toImmutable().notEmpty();
        }

        return immutableList.notEmpty();
    }

    @Override
    public T getAny() {
        if (immutableList == null) {
            return mutableList.toImmutable().getAny();
        }

        return immutableList.getAny();
    }

    @Override
    @Deprecated
    public T getFirst() {
        if (immutableList == null) {
            return mutableList.toImmutable().getFirst();
        }

        return immutableList.getFirst();
    }

    @Override
    @Deprecated
    public T getLast() {
        if (immutableList == null) {
            return mutableList.toImmutable().getLast();
        }

        return immutableList.getLast();
    }

    @Override
    public T getOnly() {
        if (immutableList == null) {
            return mutableList.toImmutable().getOnly();
        }

        return immutableList.getOnly();
    }

    @Override
    public boolean contains(Object o) {
        if (immutableList == null) {
            return mutableList.toImmutable().contains(o);
        }

        return immutableList.contains(o);
    }

    @Override
    public <V> boolean containsBy(Function<? super T, ? extends V> function, V value) {
        if (immutableList == null) {
            return mutableList.toImmutable().containsBy(function, value);
        }

        return immutableList.containsBy(function, value);
    }

    @Override
    public boolean containsAllIterable(Iterable<?> iterable) {
        if (immutableList == null) {
            return mutableList.toImmutable().containsAllIterable(iterable);
        }

        return immutableList.containsAllIterable(iterable);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        if (immutableList == null) {
            return mutableList.toImmutable().containsAll(collection);
        }

        return immutableList.containsAll(collection);
    }

    @Override
    public boolean containsAllArguments(Object... objects) {
        if (immutableList == null) {
            return mutableList.toImmutable().containsAllArguments(objects);
        }

        return immutableList.containsAllArguments(objects);
    }

    @Override
    public void each(Procedure<? super T> procedure) {
        if (immutableList == null) {
            mutableList.toImmutable().each(procedure);
        } else {
            immutableList.each(procedure);
        }
    }

    @Override
    public <R extends Collection<T>> R select(Predicate<? super T> predicate, R ts) {
        if (immutableList == null) {
            return mutableList.toImmutable().select(predicate, ts);
        }

        return immutableList.select(predicate, ts);
    }

    @Override
    public <P, R extends Collection<T>> R selectWith(Predicate2<? super T, ? super P> predicate2, P p, R ts) {
        if (immutableList == null) {
            return mutableList.toImmutable().selectWith(predicate2, p, ts);
        }

        return immutableList.selectWith(predicate2, p, ts);
    }

    @Override
    public <R extends Collection<T>> R reject(Predicate<? super T> predicate, R ts) {
        if (immutableList == null) {
            return mutableList.toImmutable().reject(predicate, ts);
        }

        return immutableList.reject(predicate, ts);
    }

    @Override
    public <P, R extends Collection<T>> R rejectWith(Predicate2<? super T, ? super P> predicate2, P p, R ts) {
        if (immutableList == null) {
            return mutableList.toImmutable().rejectWith(predicate2, p, ts);
        }

        return immutableList.rejectWith(predicate2, p, ts);
    }

    @Override
    public <V, R extends Collection<V>> R collect(Function<? super T, ? extends V> function, R vs) {
        if (immutableList == null) {
            return mutableList.toImmutable().collect(function, vs);
        }

        return immutableList.collect(function, vs);
    }

    @Override
    public <R extends MutableBooleanCollection> R collectBoolean(BooleanFunction<? super T> booleanFunction, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectBoolean(booleanFunction, r);
        }

        return immutableList.collectBoolean(booleanFunction, r);
    }

    @Override
    public <R extends MutableByteCollection> R collectByte(ByteFunction<? super T> byteFunction, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectByte(byteFunction, r);
        }

        return immutableList.collectByte(byteFunction, r);
    }

    @Override
    public <R extends MutableCharCollection> R collectChar(CharFunction<? super T> charFunction, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectChar(charFunction, r);
        }

        return immutableList.collectChar(charFunction, r);
    }

    @Override
    public <R extends MutableDoubleCollection> R collectDouble(DoubleFunction<? super T> doubleFunction, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectDouble(doubleFunction, r);
        }

        return immutableList.collectDouble(doubleFunction, r);
    }

    @Override
    public <R extends MutableFloatCollection> R collectFloat(FloatFunction<? super T> floatFunction, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectFloat(floatFunction, r);
        }

        return immutableList.collectFloat(floatFunction, r);
    }

    @Override
    public <R extends MutableIntCollection> R collectInt(IntFunction<? super T> intFunction, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectInt(intFunction, r);
        }

        return immutableList.collectInt(intFunction, r);
    }

    @Override
    public <R extends MutableLongCollection> R collectLong(LongFunction<? super T> longFunction, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectLong(longFunction, r);
        }

        return immutableList.collectLong(longFunction, r);
    }

    @Override
    public <R extends MutableShortCollection> R collectShort(ShortFunction<? super T> shortFunction, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectShort(shortFunction, r);
        }

        return immutableList.collectShort(shortFunction, r);
    }

    @Override
    public <P, V, R extends Collection<V>> R collectWith(Function2<? super T, ? super P, ? extends V> function2, P p, R vs) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectWith(function2, p, vs);
        }

        return immutableList.collectWith(function2, p, vs);
    }

    @Override
    public <V, R extends Collection<V>> R collectIf(Predicate<? super T> predicate, Function<? super T, ? extends V> function, R vs) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectIf(predicate, function, vs);
        }

        return immutableList.collectIf(predicate, function, vs);
    }

    @Override
    public <R extends MutableByteCollection> R flatCollectByte(Function<? super T, ? extends ByteIterable> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectByte(function, target);
        }

        return immutableList.flatCollectByte(function, target);
    }

    @Override
    public <R extends MutableCharCollection> R flatCollectChar(Function<? super T, ? extends CharIterable> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectChar(function, target);
        }

        return immutableList.flatCollectChar(function, target);
    }

    @Override
    public <R extends MutableIntCollection> R flatCollectInt(Function<? super T, ? extends IntIterable> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectInt(function, target);
        }

        return immutableList.flatCollectInt(function, target);
    }

    @Override
    public <R extends MutableShortCollection> R flatCollectShort(Function<? super T, ? extends ShortIterable> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectShort(function, target);
        }

        return immutableList.flatCollectShort(function, target);
    }

    @Override
    public <R extends MutableDoubleCollection> R flatCollectDouble(Function<? super T, ? extends DoubleIterable> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectDouble(function, target);
        }

        return immutableList.flatCollectDouble(function, target);
    }

    @Override
    public <R extends MutableFloatCollection> R flatCollectFloat(Function<? super T, ? extends FloatIterable> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectFloat(function, target);
        }

        return immutableList.flatCollectFloat(function, target);
    }

    @Override
    public <R extends MutableLongCollection> R flatCollectLong(Function<? super T, ? extends LongIterable> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectLong(function, target);
        }

        return immutableList.flatCollectLong(function, target);
    }

    @Override
    public <R extends MutableBooleanCollection> R flatCollectBoolean(Function<? super T, ? extends BooleanIterable> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectBoolean(function, target);
        }

        return immutableList.flatCollectBoolean(function, target);
    }

    @Override
    public <V, R extends Collection<V>> R flatCollect(Function<? super T, ? extends Iterable<V>> function, R vs) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollect(function, vs);
        }

        return immutableList.flatCollect(function, vs);
    }

    @Override
    public <P, V, R extends Collection<V>> R flatCollectWith(Function2<? super T, ? super P, ? extends Iterable<V>> function, P parameter, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().flatCollectWith(function, parameter, target);
        }

        return immutableList.flatCollectWith(function, parameter, target);
    }

    @Override
    public T detect(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().detect(predicate);
        }

        return immutableList.detect(predicate);
    }

    @Override
    public <P> T detectWith(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().detectWith(predicate2, p);
        }

        return immutableList.detectWith(predicate2, p);
    }

    @Override
    public Optional<T> detectOptional(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().detectOptional(predicate);
        }

        return immutableList.detectOptional(predicate);
    }

    @Override
    public <P> Optional<T> detectWithOptional(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().detectWithOptional(predicate2, p);
        }

        return immutableList.detectWithOptional(predicate2, p);
    }

    @Override
    public T detectIfNone(Predicate<? super T> predicate, Function0<? extends T> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().detectIfNone(predicate, function);
        }

        return immutableList.detectIfNone(predicate, function);
    }

    @Override
    public <P> T detectWithIfNone(Predicate2<? super T, ? super P> predicate2, P p, Function0<? extends T> function0) {
        if (immutableList == null) {
            return mutableList.toImmutable().detectWithIfNone(predicate2, p, function0);
        }

        return immutableList.detectWithIfNone(predicate2, p, function0);
    }

    @Override
    public int count(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().count(predicate);
        }

        return immutableList.count(predicate);
    }

    @Override
    public <P> int countWith(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().countWith(predicate2, p);
        }

        return immutableList.countWith(predicate2, p);
    }

    @Override
    public boolean anySatisfy(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().anySatisfy(predicate);
        }

        return immutableList.anySatisfy(predicate);
    }

    @Override
    public <P> boolean anySatisfyWith(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().anySatisfyWith(predicate2, p);
        }

        return immutableList.anySatisfyWith(predicate2, p);
    }

    @Override
    public boolean allSatisfy(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().allSatisfy(predicate);
        }

        return immutableList.allSatisfy(predicate);
    }

    @Override
    public <P> boolean allSatisfyWith(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().allSatisfyWith(predicate2, p);
        }

        return immutableList.allSatisfyWith(predicate2, p);
    }

    @Override
    public boolean noneSatisfy(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().noneSatisfy(predicate);
        }

        return immutableList.noneSatisfy(predicate);
    }

    @Override
    public <P> boolean noneSatisfyWith(Predicate2<? super T, ? super P> predicate2, P p) {
        if (immutableList == null) {
            return mutableList.toImmutable().noneSatisfyWith(predicate2, p);
        }

        return immutableList.noneSatisfyWith(predicate2, p);
    }

    @Override
    public <IV> IV injectInto(IV iv, Function2<? super IV, ? super T, ? extends IV> function2) {
        if (immutableList == null) {
            return mutableList.toImmutable().injectInto(iv, function2);
        }

        return immutableList.injectInto(iv, function2);
    }

    @Override
    public int injectInto(int i, IntObjectToIntFunction<? super T> intObjectToIntFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().injectInto(i, intObjectToIntFunction);
        }

        return immutableList.injectInto(i, intObjectToIntFunction);
    }

    @Override
    public long injectInto(long l, LongObjectToLongFunction<? super T> longObjectToLongFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().injectInto(l, longObjectToLongFunction);
        }

        return immutableList.injectInto(l, longObjectToLongFunction);
    }

    @Override
    public float injectInto(float v, FloatObjectToFloatFunction<? super T> floatObjectToFloatFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().injectInto(v, floatObjectToFloatFunction);
        }

        return immutableList.injectInto(v, floatObjectToFloatFunction);
    }

    @Override
    public double injectInto(double v, DoubleObjectToDoubleFunction<? super T> doubleObjectToDoubleFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().injectInto(v, doubleObjectToDoubleFunction);
        }

        return immutableList.injectInto(v, doubleObjectToDoubleFunction);
    }

    @Override
    public <R extends Collection<T>> R into(R ts) {
        if (immutableList == null) {
            return mutableList.toImmutable().into(ts);
        }

        return immutableList.into(ts);
    }

    @Override
    public MutableList<T> toList() {
        if (immutableList == null) {
            return mutableList.toImmutable().toList();
        }

        return immutableList.toList();
    }

    @Override
    public MutableList<T> toSortedList() {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedList();
        }

        return immutableList.toSortedList();
    }

    @Override
    public MutableList<T> toSortedList(Comparator<? super T> comparator) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedList(comparator);
        }

        return immutableList.toSortedList(comparator);
    }

    @Override
    public <V extends Comparable<? super V>> MutableList<T> toSortedListBy(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedListBy(function);
        }

        return immutableList.toSortedListBy(function);
    }

    @Override
    public MutableSet<T> toSet() {
        if (immutableList == null) {
            return mutableList.toImmutable().toSet();
        }

        return immutableList.toSet();
    }

    @Override
    public MutableSortedSet<T> toSortedSet() {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedSet();
        }

        return immutableList.toSortedSet();
    }

    @Override
    public MutableSortedSet<T> toSortedSet(Comparator<? super T> comparator) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedSet(comparator);
        }

        return immutableList.toSortedSet(comparator);
    }

    @Override
    public <V extends Comparable<? super V>> MutableSortedSet<T> toSortedSetBy(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedSetBy(function);
        }

        return immutableList.toSortedSetBy(function);
    }

    @Override
    public MutableBag<T> toBag() {
        if (immutableList == null) {
            return mutableList.toImmutable().toBag();
        }

        return immutableList.toBag();
    }

    @Override
    public MutableSortedBag<T> toSortedBag() {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedBag();
        }

        return immutableList.toSortedBag();
    }

    @Override
    public MutableSortedBag<T> toSortedBag(Comparator<? super T> comparator) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedBag(comparator);
        }

        return immutableList.toSortedBag(comparator);
    }

    @Override
    public <V extends Comparable<? super V>> MutableSortedBag<T> toSortedBagBy(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedBagBy(function);
        }

        return immutableList.toSortedBagBy(function);
    }

    @Override
    public <NK, NV> MutableMap<NK, NV> toMap(Function<? super T, ? extends NK> function, Function<? super T, ? extends NV> function1) {
        if (immutableList == null) {
            return mutableList.toImmutable().toMap(function, function1);
        }

        return immutableList.toMap(function, function1);
    }

    @Override
    public <NK, NV, R extends Map<NK, NV>> R toMap(Function<? super T, ? extends NK> keyFunction, Function<? super T, ? extends NV> valueFunction, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().toMap(keyFunction, valueFunction, target);
        }

        return immutableList.toMap(keyFunction, valueFunction, target);
    }

    @Override
    public <NK, NV> MutableSortedMap<NK, NV> toSortedMap(Function<? super T, ? extends NK> function, Function<? super T, ? extends NV> function1) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedMap(function, function1);
        }

        return immutableList.toSortedMap(function, function1);
    }

    @Override
    public <NK, NV> MutableSortedMap<NK, NV> toSortedMap(Comparator<? super NK> comparator, Function<? super T, ? extends NK> function, Function<? super T, ? extends NV> function1) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedMap(comparator, function, function1);
        }

        return immutableList.toSortedMap(comparator, function, function1);
    }

    @Override
    public <KK extends Comparable<? super KK>, NK, NV> MutableSortedMap<NK, NV> toSortedMapBy(Function<? super NK, KK> sortBy, Function<? super T, ? extends NK> keyFunction, Function<? super T, ? extends NV> valueFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().toSortedMapBy(sortBy, keyFunction, valueFunction);
        }

        return immutableList.toSortedMapBy(sortBy, keyFunction, valueFunction);
    }

    @Override
    public <NK, NV> MutableBiMap<NK, NV> toBiMap(Function<? super T, ? extends NK> function, Function<? super T, ? extends NV> function1) {
        if (immutableList == null) {
            return mutableList.toImmutable().toBiMap(function, function1);
        }

        return immutableList.toBiMap(function, function1);
    }

    @Override
    public LazyIterable<T> asLazy() {
        if (immutableList == null) {
            return mutableList.toImmutable().asLazy();
        }

        return immutableList.asLazy();
    }

    @Override
    public Object[] toArray() {
        if (immutableList == null) {
            return mutableList.toImmutable().toArray();
        }

        return immutableList.toArray();
    }

    @Override
    public <E> E[] toArray(E[] es) {
        if (immutableList == null) {
            return mutableList.toImmutable().toArray(es);
        }

        return immutableList.toArray(es);
    }

    @Override
    public T min(Comparator<? super T> comparator) {
        if (immutableList == null) {
            return mutableList.toImmutable().min(comparator);
        }

        return immutableList.min(comparator);
    }

    @Override
    public T max(Comparator<? super T> comparator) {
        if (immutableList == null) {
            return mutableList.toImmutable().max(comparator);
        }

        return immutableList.max(comparator);
    }

    @Override
    public Optional<T> minOptional(Comparator<? super T> comparator) {
        if (immutableList == null) {
            return mutableList.toImmutable().minOptional(comparator);
        }

        return immutableList.minOptional(comparator);
    }

    @Override
    public Optional<T> maxOptional(Comparator<? super T> comparator) {
        if (immutableList == null) {
            return mutableList.toImmutable().maxOptional(comparator);
        }

        return immutableList.maxOptional(comparator);
    }

    @Override
    public T min() {
        if (immutableList == null) {
            return mutableList.toImmutable().min();
        }

        return immutableList.min();
    }

    @Override
    public T max() {
        if (immutableList == null) {
            return mutableList.toImmutable().max();
        }

        return immutableList.max();
    }

    @Override
    public Optional<T> minOptional() {
        if (immutableList == null) {
            return mutableList.toImmutable().minOptional();
        }

        return immutableList.minOptional();
    }

    @Override
    public Optional<T> maxOptional() {
        if (immutableList == null) {
            return mutableList.toImmutable().maxOptional();
        }

        return immutableList.maxOptional();
    }

    @Override
    public <V extends Comparable<? super V>> T minBy(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().minBy(function);
        }

        return immutableList.minBy(function);
    }

    @Override
    public <V extends Comparable<? super V>> T maxBy(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().maxBy(function);
        }

        return immutableList.maxBy(function);
    }

    @Override
    public <V extends Comparable<? super V>> Optional<T> minByOptional(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().minByOptional(function);
        }

        return immutableList.minByOptional(function);
    }

    @Override
    public <V extends Comparable<? super V>> Optional<T> maxByOptional(Function<? super T, ? extends V> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().maxByOptional(function);
        }

        return immutableList.maxByOptional(function);
    }

    @Override
    public long sumOfInt(IntFunction<? super T> intFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().sumOfInt(intFunction);
        }

        return immutableList.sumOfInt(intFunction);
    }

    @Override
    public double sumOfFloat(FloatFunction<? super T> floatFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().sumOfFloat(floatFunction);
        }

        return immutableList.sumOfFloat(floatFunction);
    }

    @Override
    public long sumOfLong(LongFunction<? super T> longFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().sumOfLong(longFunction);
        }

        return immutableList.sumOfLong(longFunction);
    }

    @Override
    public double sumOfDouble(DoubleFunction<? super T> doubleFunction) {
        if (immutableList == null) {
            return mutableList.toImmutable().sumOfDouble(doubleFunction);
        }

        return immutableList.sumOfDouble(doubleFunction);
    }

    @Override
    public IntSummaryStatistics summarizeInt(IntFunction<? super T> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().summarizeInt(function);
        }

        return immutableList.summarizeInt(function);
    }

    @Override
    public DoubleSummaryStatistics summarizeFloat(FloatFunction<? super T> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().summarizeFloat(function);
        }

        return immutableList.summarizeFloat(function);
    }

    @Override
    public LongSummaryStatistics summarizeLong(LongFunction<? super T> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().summarizeLong(function);
        }

        return immutableList.summarizeLong(function);
    }

    @Override
    public DoubleSummaryStatistics summarizeDouble(DoubleFunction<? super T> function) {
        if (immutableList == null) {
            return mutableList.toImmutable().summarizeDouble(function);
        }

        return immutableList.summarizeDouble(function);
    }

    @Override
    public <R, A> R reduceInPlace(Collector<? super T, A, R> collector) {
        if (immutableList == null) {
            return mutableList.toImmutable().reduceInPlace(collector);
        }

        return immutableList.reduceInPlace(collector);
    }

    @Override
    public <R> R reduceInPlace(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator) {
        if (immutableList == null) {
            return mutableList.toImmutable().reduceInPlace(supplier, accumulator);
        }

        return immutableList.reduceInPlace(supplier, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        if (immutableList == null) {
            return mutableList.toImmutable().reduce(accumulator);
        }

        return immutableList.reduce(accumulator);
    }

    @Override
    public String makeString() {
        if (immutableList == null) {
            return mutableList.toImmutable().makeString();
        }

        return immutableList.makeString();
    }

    @Override
    public String makeString(String separator) {
        if (immutableList == null) {
            return mutableList.toImmutable().makeString(separator);
        }

        return immutableList.makeString(separator);
    }

    @Override
    public String makeString(String start, String separator, String end) {
        if (immutableList == null) {
            return mutableList.toImmutable().makeString(start, separator, end);
        }

        return immutableList.makeString(start, separator, end);
    }

    @Override
    public void appendString(Appendable appendable) {
        if (immutableList == null) {
            mutableList.toImmutable().appendString(appendable);
        } else {
            immutableList.appendString(appendable);
        }
    }

    @Override
    public void appendString(Appendable appendable, String separator) {
        if (immutableList == null) {
            mutableList.toImmutable().appendString(appendable, separator);
        } else {
            immutableList.appendString(appendable, separator);
        }
    }

    @Override
    public void appendString(Appendable appendable, String s, String s1, String s2) {
        if (immutableList == null) {
            mutableList.toImmutable().appendString(appendable, s, s1, s2);
        } else {
            immutableList.appendString(appendable, s, s1, s2);
        }
    }

    @Override
    public <V, R extends MutableBagIterable<V>> R countBy(Function<? super T, ? extends V> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().countBy(function, target);
        }

        return immutableList.countBy(function, target);
    }

    @Override
    public <V, P, R extends MutableBagIterable<V>> R countByWith(Function2<? super T, ? super P, ? extends V> function, P parameter, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().countByWith(function, parameter, target);
        }

        return immutableList.countByWith(function, parameter, target);
    }

    @Override
    public <V, R extends MutableBagIterable<V>> R countByEach(Function<? super T, ? extends Iterable<V>> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().countByEach(function, target);
        }

        return immutableList.countByEach(function, target);
    }

    @Override
    public <V, R extends MutableMultimap<V, T>> R groupBy(Function<? super T, ? extends V> function, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().groupBy(function, r);
        }

        return immutableList.groupBy(function, r);
    }

    @Override
    public <V, R extends MutableMultimap<V, T>> R groupByEach(Function<? super T, ? extends Iterable<V>> function, R r) {
        if (immutableList == null) {
            return mutableList.toImmutable().groupByEach(function, r);
        }

        return immutableList.groupByEach(function, r);
    }

    @Override
    public <V, R extends MutableMapIterable<V, T>> R groupByUniqueKey(Function<? super T, ? extends V> function, R ts) {
        if (immutableList == null) {
            return mutableList.toImmutable().groupByUniqueKey(function, ts);
        }

        return immutableList.groupByUniqueKey(function, ts);
    }

    @Override
    @Deprecated
    public <S, R extends Collection<Pair<T, S>>> R zip(Iterable<S> iterable, R pairs) {
        if (immutableList == null) {
            return mutableList.toImmutable().zip(iterable, pairs);
        }

        return immutableList.zip(iterable, pairs);
    }

    @Override
    @Deprecated
    public <R extends Collection<Pair<T, Integer>>> R zipWithIndex(R pairs) {
        if (immutableList == null) {
            return mutableList.toImmutable().zipWithIndex(pairs);
        }

        return immutableList.zipWithIndex(pairs);
    }

    @Override
    public RichIterable<RichIterable<T>> chunk(int i) {
        if (immutableList == null) {
            return mutableList.toImmutable().chunk(i);
        }

        return immutableList.chunk(i);
    }

    @Override
    public <K, V, R extends MutableMapIterable<K, V>> R aggregateBy(Function<? super T, ? extends K> groupBy, Function0<? extends V> zeroValueFactory, Function2<? super V, ? super T, ? extends V> nonMutatingAggregator, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().aggregateBy(groupBy, zeroValueFactory, nonMutatingAggregator, target);
        }

        return immutableList.aggregateBy(groupBy, zeroValueFactory, nonMutatingAggregator, target);
    }

    @Override
    public <K, V, R extends MutableMultimap<K, V>> R groupByAndCollect(Function<? super T, ? extends K> groupByFunction, Function<? super T, ? extends V> collectFunction, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().groupByAndCollect(groupByFunction, collectFunction, target);
        }

        return immutableList.groupByAndCollect(groupByFunction, collectFunction, target);
    }

    @Override
    public void forEach(Consumer<? super T> consumer) {
        if (immutableList == null) {
            mutableList.toImmutable().forEach(consumer);
        } else {
            immutableList.forEach(consumer);
        }
    }

    @Override
    @Deprecated
    public void forEachWithIndex(ObjectIntProcedure<? super T> objectIntProcedure) {
        if (immutableList == null) {
            mutableList.toImmutable().forEachWithIndex(objectIntProcedure);
        } else {
            immutableList.forEachWithIndex(objectIntProcedure);
        }
    }

    @Override
    public <P> void forEachWith(Procedure2<? super T, ? super P> procedure2, P p) {
        if (immutableList == null) {
            mutableList.toImmutable().forEachWith(procedure2, p);
        } else {
            immutableList.forEachWith(procedure2, p);
        }
    }

    @Override
    public Iterator<T> iterator() {
        if (immutableList == null) {
            return mutableList.toImmutable().iterator();
        }

        return immutableList.iterator();
    }

    @Override
    public T get(int i) {
        if (immutableList == null) {
            return mutableList.toImmutable().get(i);
        }

        return immutableList.get(i);
    }

    @Override
    public int lastIndexOf(Object o) {
        if (immutableList == null) {
            return mutableList.toImmutable().lastIndexOf(o);
        }

        return immutableList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        if (immutableList == null) {
            return mutableList.toImmutable().listIterator();
        }

        return immutableList.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        if (immutableList == null) {
            return mutableList.toImmutable().listIterator(i);
        }

        return immutableList.listIterator(i);
    }

    @Override
    public ImmutableList<T> toImmutable() {
        if (immutableList == null) {
            return mutableList.toImmutable().toImmutable();
        }

        return immutableList.toImmutable();
    }

    @Override
    @Beta
    public ParallelListIterable<T> asParallel(ExecutorService executorService, int i) {
        if (immutableList == null) {
            return mutableList.toImmutable().asParallel(executorService, i);
        }

        return immutableList.asParallel(executorService, i);
    }

    @Override
    public int binarySearch(T key, Comparator<? super T> comparator) {
        if (immutableList == null) {
            return mutableList.toImmutable().binarySearch(key, comparator);
        }

        return immutableList.binarySearch(key, comparator);
    }

    @Override
    public int binarySearch(T key) {
        if (immutableList == null) {
            return mutableList.toImmutable().binarySearch(key);
        }

        return immutableList.binarySearch(key);
    }

    @Override
    public <T2> void forEachInBoth(ListIterable<T2> other, Procedure2<? super T, ? super T2> procedure) {
        if (immutableList == null) {
            mutableList.toImmutable().forEachInBoth(other, procedure);
        } else {
            immutableList.forEachInBoth(other, procedure);
        }
    }

    @Override
    public int hashCode() {
        if (immutableList == null) {
            return mutableList.toImmutable().hashCode();
        }

        return immutableList.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (immutableList == null) {
            return mutableList.toImmutable().equals(o);
        }

        return immutableList.equals(o);
    }

    @Override
    public String toString() {
        if (immutableList == null) {
            return mutableList.toImmutable().toString();
        }

        return immutableList.toString();
    }

    @Override
    public void reverseForEach(Procedure<? super T> procedure) {
        if (immutableList == null) {
            mutableList.toImmutable().reverseForEach(procedure);
        } else {
            immutableList.reverseForEach(procedure);
        }
     }

    @Override
    public void reverseForEachWithIndex(ObjectIntProcedure<? super T> procedure) {
        if (immutableList == null) {
            mutableList.toImmutable().reverseForEachWithIndex(procedure);
        } else {
            immutableList.reverseForEachWithIndex(procedure);
        }
    }

    @Override
    public LazyIterable<T> asReversed() {
        if (immutableList == null) {
            return mutableList.toImmutable().asReversed();
        }

        return immutableList.asReversed();
    }

    @Override
    public int detectLastIndex(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().detectLastIndex(predicate);
        }

        return immutableList.detectLastIndex(predicate);
    }

    @Override
    public int indexOf(Object o) {
        if (immutableList == null) {
            return mutableList.toImmutable().indexOf(o);
        }

        return immutableList.indexOf(o);
    }

    @Override
    public Optional<T> getFirstOptional() {
        if (immutableList == null) {
            return mutableList.toImmutable().getFirstOptional();
        }

        return immutableList.getFirstOptional();
    }

    @Override
    public Optional<T> getLastOptional() {
        if (immutableList == null) {
            return mutableList.toImmutable().getLastOptional();
        }

        return immutableList.getLastOptional();
    }

    @Override
    public <S> boolean corresponds(OrderedIterable<S> orderedIterable, Predicate2<? super T, ? super S> predicate2) {
        if (immutableList == null) {
            return mutableList.toImmutable().corresponds(orderedIterable, predicate2);
        }

        return immutableList.corresponds(orderedIterable, predicate2);
    }

    @Override
    public void forEach(int i, int i1, Procedure<? super T> procedure) {
        if (immutableList == null) {
            mutableList.toImmutable().forEach(i, i1, procedure);
        } else {
            immutableList.forEach(i, i1, procedure);
        }
    }

    @Override
    public void forEachWithIndex(int i, int i1, ObjectIntProcedure<? super T> objectIntProcedure) {
        if (immutableList == null) {
            mutableList.toImmutable().forEachWithIndex(i, i1, objectIntProcedure);
        } else {
            immutableList.forEachWithIndex(i, i1, objectIntProcedure);
        }
    }

    @Override
    public MutableStack<T> toStack() {
        if (immutableList == null) {
            return mutableList.toImmutable().toStack();
        }

        return immutableList.toStack();
    }

    @Override
    public <V, R extends Collection<V>> R collectWithIndex(ObjectIntToObjectFunction<? super T, ? extends V> function, R target) {
        if (immutableList == null) {
            return mutableList.toImmutable().collectWithIndex(function, target);
        }

        return immutableList.collectWithIndex(function, target);
    }

    @Override
    public int detectIndex(Predicate<? super T> predicate) {
        if (immutableList == null) {
            return mutableList.toImmutable().detectIndex(predicate);
        }

        return immutableList.detectIndex(predicate);
    }

}
