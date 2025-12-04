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

import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.bag.ImmutableBag;
import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.api.bag.MutableBagIterable;
import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.api.collection.primitive.MutableBooleanCollection;
import org.eclipse.collections.api.collection.primitive.MutableByteCollection;
import org.eclipse.collections.api.collection.primitive.MutableCharCollection;
import org.eclipse.collections.api.collection.primitive.MutableDoubleCollection;
import org.eclipse.collections.api.collection.primitive.MutableFloatCollection;
import org.eclipse.collections.api.collection.primitive.MutableIntCollection;
import org.eclipse.collections.api.collection.primitive.MutableLongCollection;
import org.eclipse.collections.api.collection.primitive.MutableShortCollection;
import org.eclipse.collections.api.list.ImmutableList;
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
import org.eclipse.collections.api.partition.list.PartitionImmutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.api.stack.MutableStack;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecordListBuilderTest {

    private static final Logger LOG = LoggerFactory.getLogger(RecordListBuilderTest.class);

    RecordListBuilder recordListBuilder = new RecordListBuilder();
    ImmutableList immutableList = null;
    PartitionImmutableList partitionImmutableList = null;
    ImmutableBooleanList immutableBooleanList = null;
    ImmutableByteList immutableByteList = null;
    ImmutableCharList immutableCharList = null;
    ImmutableDoubleList immutableDoubleList = null;
    ImmutableFloatList immutableFloatList = null;
    ImmutableIntList immutableIntList = null;
    ImmutableLongList immutableLongList = null;
    ImmutableShortList immutableShortList = null;
    ImmutableListMultimap immutableListMultimap = null;
    ImmutableObjectLongMap immutableObjectIntMap = null;
    ImmutableObjectDoubleMap immutableObjectFloatMap = null;
    ImmutableObjectLongMap immutableObjectLongMap = null;
    ImmutableObjectDoubleMap immutableObjectDoubleMap = null;
    ImmutableBag immutableBag = null;
    ImmutableMap immutableMap = null;
    Stream stream = null;
    Spliterator spliterator = null;
    Collection collection = null;
    MutableBooleanCollection mutableBooleanCollection = null;
    MutableByteCollection mutableByteCollection = null;
    MutableCharCollection mutableCharCollection = null;
    MutableDoubleCollection mutableDoubleCollection = null;
    MutableFloatCollection mutableFloatCollection = null;
    MutableIntCollection mutableIntCollection = null;
    MutableLongCollection mutableLongCollection = null;
    MutableShortCollection mutableShortCollection = null;
    MutableList mutableList = null;
    MutableSet mutableSet = null;
    MutableSortedSet mutableSortedSet = null;
    MutableBag mutableBag = null;
    MutableSortedBag mutableSortedBag = null;
    Map map = null;
    MutableMap mutableMap = null;
    MutableSortedMap mutableSortedMap = null;
    MutableBiMap mutableBiMap = null;
    IntSummaryStatistics intSummaryStatistics = null;
    DoubleSummaryStatistics doubleSummaryStatistics = null;
    LongSummaryStatistics longSummaryStatistics = null;
    Optional optional = null;
    Comparable comparable = null;
    Object [] objectArray = null;
    MutableBagIterable mutableBagIterable = null;
    MutableMultimap mutableMultiMap = null;
    MutableMapIterable mutableMapIterable = null;
    RichIterable richIterable = null;
    Iterator iterator = null;
    ListIterator listIterator = null;
    ParallelListIterable parallelListIterable = null;
    LazyIterable lazyIterable = null;
    MutableStack mutableStack = null;


    /*
    UNIT TESTS

    References:
    https://gitlab.tinkarbuild.com/fda-shield/tinkar-core/-/merge_requests/63
    https://ikmdev.atlassian.net/browse/IKM-1241
    https://ikmdev.atlassian.net/browse/IKM-1362
    https://gitlab.tinkarbuild.com/fda-shield/tinkar-core/-/tree/feature/IKM-1241-add-testing
     */

    @Test
    @Order(1)
    public void testNotNullNewWithout() {
        LOG.warn("testNotNullNewWithout");
        immutableList = recordListBuilder.newWithout("Test String");
        assertNotNull(immutableList, "testNotNullNewWithout");
    }

    @Test
    @Order(1)
    public void testInstanceOfNewWithout() {
        LOG.warn("testInstanceOfNewWithout");
        immutableList = recordListBuilder.newWithout("Test String");
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfNewWithout");
    }

    @Test
    @Order(2)
    public void testNotNullNewWithAll() {
        LOG.warn("testNotNullNewWithAll");
        immutableList = recordListBuilder.newWithout("Test String");
        immutableList = recordListBuilder.newWithAll(Collections.singleton(immutableList.iterator()));
        assertNotNull(immutableList, "testNotNullNewWithAll");
    }

    @Test
    @Order(2)
    public void testInstanceOfNewWithAll() {
        LOG.warn("testInstanceOfNewWithAll");
        immutableList = recordListBuilder.newWithout("Test String");
        immutableList = recordListBuilder.newWithAll(Collections.singleton(immutableList.iterator()));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfNewWithAll");
    }

    @Test
    @Order(3)
    public void testNotNullNewWithoutAll() {
        LOG.warn("testNotNullNewWithoutAll");
        immutableList = recordListBuilder.newWithout("Test String");
        immutableList = recordListBuilder.newWithoutAll(Collections.singleton(immutableList.iterator()));
        assertNotNull(immutableList, "testNotNullNewWithoutAll");
    }

    @Test
    @Order(3)
    public void testInstanceOfNewWithoutAll() {
        LOG.warn("testInstanceOfNewWithoutAll");
        immutableList = recordListBuilder.newWithout("Test String");
        immutableList = recordListBuilder.newWithoutAll(Collections.singleton(immutableList.iterator()));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfNewWithoutAll");
    }

    @Test
    @Order(4)
    public void testNotNullTap() {
        LOG.warn("testNotNullTap");
        immutableList = recordListBuilder.tap(RLBTImpl.procedure);
        assertNotNull(immutableList, "testNotNullTap");
    }

    @Test
    @Order(4)
    public void testInstanceOfTap() {
        LOG.warn("testInstanceOfTap");
        immutableList = recordListBuilder.tap(RLBTImpl.procedure);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfTap");
    }

    @Test
    @Order(5)
    public void testNotNullSelect() {
        LOG.warn("testNotNullSelect");
        immutableList = recordListBuilder.select(RLBTImpl.predicate);
        assertNotNull(immutableList, "testNotNullSelect");
    }

    @Test
    @Order(5)
    public void testInstanceOfSelect() {
        LOG.warn("testInstanceOfSelect");
        immutableList = recordListBuilder.select(RLBTImpl.predicate);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfSelect");
    }

    @Test
    @Order(6)
    public void testNotNullSelectWith() {
        LOG.warn("testNotNullSelectWith");
        immutableList = recordListBuilder.selectWith(RLBTImpl.predicate2, new Object());
        assertNotNull(immutableList, "testNotNullSelectWith");
    }

    @Test
    @Order(6)
    public void testInstanceOfSelectWith() {
        LOG.warn("testInstanceOfSelectWith");
        immutableList = recordListBuilder.selectWith(RLBTImpl.predicate2, new Object());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfSelectWith");
    }

    @Test
    @Order(7)
    public void testNotNullReject() {
        LOG.warn("testNotNullReject");
        immutableList = recordListBuilder.reject(RLBTImpl.predicate);
        assertNotNull(immutableList, "testNotNullReject");
    }

    @Test
    @Order(7)
    public void testInstanceOfReject() {
        LOG.warn("testInstanceOfReject");
        immutableList = recordListBuilder.reject(RLBTImpl.predicate);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfReject");
    }

    @Test
    @Order(8)
    public void testNotNullRejectWith() {
        LOG.warn("testNotNullRejectWith");
        immutableList = recordListBuilder.rejectWith(RLBTImpl.predicate2, new Object());
        assertNotNull(immutableList, "testNotNullRejectWith");
    }

    @Test
    @Order(8)
    public void testInstanceOfRejectWith() {
        LOG.warn("testInstanceOfRejectWith");
        immutableList = recordListBuilder.rejectWith(RLBTImpl.predicate2, new Object());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfRejectWith");
    }

    @Test
    @Order(9)
    public void testNotNullPartition() {
        LOG.warn("testNotNullPartition");
        partitionImmutableList = recordListBuilder.partition(RLBTImpl.predicate);
        assertNotNull(partitionImmutableList, "testNotNullPartition");
    }

    @Test
    @Order(9)
    public void testInstanceOfPartition() {
        LOG.warn("testInstanceOfPartition");
        partitionImmutableList = recordListBuilder.partition(RLBTImpl.predicate);
        assertTrue(partitionImmutableList instanceof PartitionImmutableList, "testInstanceOfPartition");
    }

    @Test
    @Order(10)
    public void testNotNullPartitionWith() {
        LOG.warn("testNotNullPartitionWith");
        partitionImmutableList = recordListBuilder.partitionWith(RLBTImpl.predicate2, new Object());
        assertNotNull(partitionImmutableList, "testNotNullPartitionWith");
    }

    @Test
    @Order(10)
    public void testInstanceOfPartitionWith() {
        LOG.warn("testInstanceOfPartitionWith");
        partitionImmutableList = recordListBuilder.partitionWith(RLBTImpl.predicate2, new Object());
        assertTrue(partitionImmutableList instanceof PartitionImmutableList, "testInstanceOfPartitionWith");
    }

    @Test
    @Order(11)
    public void testNotNullSelectInstancesOf() {
        LOG.warn("testNotNullSelectInstancesOf");
        immutableList = recordListBuilder.selectInstancesOf(ImmutableList.class);
        assertNotNull(immutableList, "testNotNullSelectInstancesOf");
    }

    @Test
    @Order(11)
    public void testInstanceOfSelectInstancesOf() {
        LOG.warn("testInstanceOfSelectInstancesOf");
        immutableList = recordListBuilder.selectInstancesOf(ImmutableList.class);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfSelectInstancesOf");
    }

    @Test
    @Order(12)
    public void testNotNullCollect() {
        LOG.warn("testNotNullCollect");
        immutableList = recordListBuilder.collect(RLBTImpl.function);
        assertNotNull(immutableList, "testNotNullCollect");
    }

    @Test
    @Order(12)
    public void testInstanceOfCollect() {
        LOG.warn("testInstanceOfCollect");
        immutableList = recordListBuilder.collect(RLBTImpl.function);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfCollect");
    }

    @Test
    @Order(13)
    public void testNotNullCollectWithIndex() {
        LOG.warn("testNotNullCollectWithIndex");
        immutableList = recordListBuilder.collectWithIndex(RLBTImpl.objectIntToObjectFunction);
        assertNotNull(immutableList, "testNotNullCollectWithIndex");
    }

    @Test
    @Order(13)
    public void testInstanceOfCollectWithIndex() {
        LOG.warn("testInstanceOfCollectWithIndex");
        immutableList = recordListBuilder.collectWithIndex(RLBTImpl.objectIntToObjectFunction);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfCollectWithIndex");
    }

//    @Test
//    @Order(14)
//    public void testNotNullCollectBoolean() {
//        LOG.warn("testNotNullCollectBoolean");
//        immutableBooleanList = recordListBuilder.collectBoolean(RLBTImpl.booleanFunction);
//        assertNotNull(immutableBooleanList, "testNotNullCollectBoolean");
//    }
//
//    @Test
//    @Order(14)
//    public void testInstanceOfCollectBoolean() {
//        LOG.warn("testInstanceOfCollectBoolean");
//        immutableBooleanList = recordListBuilder.collectBoolean(RLBTImpl.booleanFunction);
////        immutableBooleanList = (ImmutableList<Boolean>) recordListBuilder.collectBoolean(RLBImpl.booleanFunction);
//        assertTrue(immutableBooleanList instanceof ImmutableBooleanList, "testInstanceOfCollectBoolean");
//    }
//
//    @Test
//    @Order(15)
//    public void testNotNullCollectByte() {
//        LOG.warn("testNotNullCollectByte");
//        immutableByteList = recordListBuilder.collectByte(RLBTImpl.byteFunction);
//        assertNotNull(immutableByteList, "testNotNullCollectByte");
//    }
//
//    @Test
//    @Order(15)
//    public void testInstanceOfCollectByte() {
//        LOG.warn("testInstanceOfCollectByte");
//        immutableByteList = recordListBuilder.collectByte(RLBTImpl.byteFunction);
//        assertTrue(immutableByteList instanceof ImmutableByteList, "testInstanceOfCollectByte");
//    }
//
//    @Test
//    @Order(16)
//    public void testNotNullCollectChar() {
//        LOG.warn("testNotNullCollectChar");
//        immutableCharList = recordListBuilder.collectChar(RLBTImpl.charFunction);
//        assertNotNull(immutableCharList, "testNotNullCollectChar");
//    }
//
//    @Test
//    @Order(16)
//    public void testInstanceOfCollectChar() {
//        LOG.warn("testInstanceOfCollectChar");
//        immutableCharList = recordListBuilder.collectChar(RLBTImpl.charFunction);
//        assertTrue(immutableCharList instanceof ImmutableCharList, "testInstanceOfCollectChar");
//    }
//
//    @Test
//    @Order(17)
//    public void testNotNullCollectDouble() {
//        LOG.warn("testNotNullCollectDouble");
//        immutableDoubleList = recordListBuilder.collectDouble(RLBTImpl.doubleFunction);
//        assertNotNull(immutableList, "testNotNullCollectDouble");
//    }
//
//    @Test
//    @Order(17)
//    public void testInstanceOfCollectDouble() {
//        LOG.warn("testInstanceOfCollectDouble");
//        immutableDoubleList = recordListBuilder.collectDouble(RLBTImpl.doubleFunction);
//        assertTrue(immutableDoubleList instanceof ImmutableDoubleList, "testInstanceOfCollectDouble");
//    }
//
//    @Test
//    @Order(18)
//    public void testNotNullCollectFloat() {
//        LOG.warn("testNotNullCollectFloat");
//        immutableFloatList = recordListBuilder.collectFloat(RLBTImpl.floatFunction);
//        assertNotNull(immutableFloatList, "testNotNullCollectFloat");
//    }
//
//    @Test
//    @Order(18)
//    public void testInstanceOfCollectFloat() {
//        LOG.warn("testInstanceOfCollectFloat");
//        immutableFloatList = recordListBuilder.collectFloat(RLBTImpl.floatFunction);
//        assertTrue(immutableFloatList instanceof ImmutableFloatList, "testInstanceOfCollectFloat");
//    }
//
//    @Test
//    @Order(19)
//    public void testNotNullCollectInt() {
//        LOG.warn("testNotNullCollectInt");
//        immutableIntList = recordListBuilder.collectInt(RLBTImpl.intFunction);
//        assertNotNull(immutableIntList, "testNotNullCollectInt");
//    }
//
//    @Test
//    @Order(19)
//    public void testInstanceOfCollectInt() {
//        LOG.warn("testInstanceOfCollectInt");
//        immutableIntList = recordListBuilder.collectInt(RLBTImpl.intFunction);
//        assertTrue(immutableIntList instanceof ImmutableIntList, "testInstanceOfCollectInt");
//    }
//
//    @Test
//    @Order(20)
//    public void testNotNullCollectLong() {
//        LOG.warn("testNotNullCollect");
//        immutableLongList = recordListBuilder.collectLong(RLBTImpl.longFunction);
//        assertNotNull(immutableLongList, "testNotNullCollectLong");
//    }
//
//    @Test
//    @Order(20)
//    public void testInstanceOfCollectLong() {
//        LOG.warn("testInstanceOfCollectLong");
//        immutableLongList = recordListBuilder.collectLong(RLBTImpl.longFunction);
//        assertTrue(immutableLongList instanceof ImmutableLongList, "testInstanceOfCollectLong");
//    }
//
//    @Test
//    @Order(21)
//    public void testNotNullCollectShort() {
//        LOG.warn("testNotNullCollectShort");
//        immutableShortList = recordListBuilder.collectShort(RLBTImpl.shortFunction);
//        assertNotNull(immutableShortList, "testNotNullCollectShort");
//    }
//
//    @Test
//    @Order(21)
//    public void testInstanceOfCollectShort() {
//        LOG.warn("testInstanceOfCollectShort");
//        immutableShortList = recordListBuilder.collectShort(RLBTImpl.shortFunction);
//        assertTrue(immutableShortList instanceof ImmutableShortList, "testInstanceOfCollectShort");
//    }

    @Test
    @Order(22)
    public void testNotNullCollectWith() {
        LOG.warn("testNotNullCollectWith");
        immutableList = recordListBuilder.collectWith(RLBTImpl.function2, new Object());
        assertNotNull(immutableList, "testNotNullCollectWith");
    }

    @Test
    @Order(22)
    public void testInstanceOfCollectWith() {
        LOG.warn("testInstanceOfCollectWith");
        immutableList = recordListBuilder.collectWith(RLBTImpl.function2, new Object());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfCollectWith");
    }

    @Test
    @Order(23)
    public void testNotNullCollectIf() {
        LOG.warn("testNotNullCollectIf");
        immutableList = recordListBuilder.collectIf(RLBTImpl.predicate, RLBTImpl.function);
        assertNotNull(immutableList, "testNotNullCollectIf");
    }

    @Test
    @Order(23)
    public void testInstanceOfCollectIf() {
        LOG.warn("testInstanceOfCollectIf");
        immutableList = recordListBuilder.collectIf(RLBTImpl.predicate, RLBTImpl.function);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfCollectIf");
    }

    @Test
    @Order(24)
    public void testNotNullFlatCollect() {
        LOG.warn("testNotNullFlatCollect");
        immutableList = recordListBuilder.flatCollect(RLBTImpl.function);
        assertNotNull(immutableList, "testNotNullFlatCollect");
    }

    @Test
    @Order(24)
    public void testInstanceOfFlatCollect() {
        LOG.warn("testInstanceOfFlatCollect");
        immutableList = recordListBuilder.flatCollect(RLBTImpl.function);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfFlatCollect");
    }

    @Test
    @Order(25)
    public void testNotNullFlatCollectWith() {
        LOG.warn("testNotNullFlatCollectWith");
        immutableList = recordListBuilder.flatCollectWith(RLBTImpl.function2, new Object());
        assertNotNull(immutableList, "testNotNullFlatCollectWith");
    }

    @Test
    @Order(25)
    public void testInstanceOfFlatCollectWith() {
        LOG.warn("testInstanceOfFlatCollectWith");
        immutableList = recordListBuilder.flatCollectWith(RLBTImpl.function2, new Object());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfFlatCollectWith");
    }

    @Test
    @Order(26)
    public void testNotNullGroupBy() {
        LOG.warn("testNotNullGroupBy");
        immutableListMultimap = recordListBuilder.groupBy(RLBTImpl.function);
        assertNotNull(immutableListMultimap, "testNotNullGroupBy");
    }

    @Test
    @Order(26)
    public void testInstanceOfGroupBy() {
        LOG.warn("testInstanceOfGroupBy");
        immutableListMultimap = recordListBuilder.groupBy(RLBTImpl.function);
        assertTrue(immutableListMultimap instanceof ImmutableListMultimap, "testInstanceOfGroupBy");
    }

    @Test
    @Order(27)
    public void testNotNullGroupByEach() {
        LOG.warn("testNotNullGroupByEach");
        immutableListMultimap = recordListBuilder.groupByEach(RLBTImpl.function);
        assertNotNull(immutableListMultimap, "testNotNullGroupByEach");
    }

    @Test
    @Order(27)
    public void testInstanceOfGroupByEach() {
        LOG.warn("testInstanceOfGroupByEach");
        immutableListMultimap = recordListBuilder.groupByEach(RLBTImpl.function);
        assertTrue(immutableListMultimap instanceof ImmutableListMultimap, "testInstanceOfGroupByEach");
    }

    @Test
    @Order(28)
    public void testNotNullDistinct() {
        LOG.warn("testNotNullDistinct");
        immutableList = recordListBuilder.distinct();
        assertNotNull(immutableList, "testNotNullDistinct");
    }

    @Test
    @Order(28)
    public void testInstanceOfDistinct() {
        LOG.warn("testInstanceOfDistinct");
        immutableList = recordListBuilder.distinct();
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDistinct");
    }

    @Test
    @Order(29)
    public void testNotNullDistinctHash() {
        LOG.warn("testNotNullDistinctHash");
        immutableList = recordListBuilder.distinct(RLBTImpl.hashingStrategy);
        assertNotNull(immutableList, "testNotNullDistinctHash");
    }

    @Test
    @Order(29)
    public void testInstanceOfDistinctHash() {
        LOG.warn("testInstanceOfDistinctHash");
        immutableList = recordListBuilder.distinct(RLBTImpl.hashingStrategy);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDistinctHash");
    }

    @Test
    @Order(30)
    public void testNotNullDistinctBy() {
        LOG.warn("testNotNullDistinctBy");
        immutableList = recordListBuilder.distinctBy(RLBTImpl.function);
        assertNotNull(immutableList, "testNotNullDistinctBy");
    }

    @Test
    @Order(30)
    public void testInstanceOfDistinctBy() {
        LOG.warn("testInstanceOfDistinctBy");
        immutableList = recordListBuilder.distinctBy(RLBTImpl.function);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDistinctBy");
    }

    @Test
    @Order(31)
    public void testNotNullZip() {
        LOG.warn("testNotNullZip");
        immutableList = recordListBuilder.newWithout("Test String");
        immutableList = recordListBuilder.zip(Collections.singleton(immutableList.iterator()));
        assertNotNull(immutableList, "testNotNullZip");
    }

    @Test
    @Order(31)
    public void testInstanceOfZip() {
        LOG.warn("testInstanceOfZip");
        immutableList = recordListBuilder.newWithout("Test String");
        immutableList = recordListBuilder.zip(Collections.singleton(immutableList.iterator()));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfZip");
    }

    @Test
    @Order(32)
    public void testNotNullZipWithIndex() {
        LOG.warn("testNotNullZipWithIndex");
        immutableList = recordListBuilder.newWithout("Test String");
        immutableList = recordListBuilder.zipWithIndex();
        assertNotNull(immutableList, "testNotNullZipWithIndex");
    }

    @Test
    @Order(32)
    public void testInstanceOfZipWithIndex() {
        LOG.warn("testInstanceOfZipWithIndex");
        immutableList = recordListBuilder.newWithout("Test String");
        immutableList = recordListBuilder.zipWithIndex();
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfZipWithIndex");
    }

    @Test
    @Order(33)
    public void testNotNullTake() {
        LOG.warn("testNotNullTake");
        immutableList = recordListBuilder.take(33);
        assertNotNull(immutableList, "testNotNullTake");
    }

    @Test
    @Order(33)
    public void testInstanceOfTake() {
        LOG.warn("testInstanceOfTake");
        immutableList = recordListBuilder.take(33);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfTake");
    }

    @Test
    @Order(34)
    public void testNotNullTakeWhile() {
        LOG.warn("testNotNullTakeWhile");
        immutableList = recordListBuilder.takeWhile(RLBTImpl.predicate);
        assertNotNull(immutableList, "testNotNullTakeWhile");
    }

    @Test
    @Order(34)
    public void testInstanceOfTakeWhile() {
        LOG.warn("testInstanceOfTakeWhile");
        immutableList = recordListBuilder.takeWhile(RLBTImpl.predicate);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfTakeWhile");
    }

    @Test
    @Order(35)
    public void testNotNullDrop() {
        LOG.warn("testNotNullDrop");
        immutableList = recordListBuilder.drop(35);
        assertNotNull(immutableList, "testNotNullDrop");
    }

    @Test
    @Order(35)
    public void testInstanceOfDrop() {
        LOG.warn("testInstanceOfDrop");
        immutableList = recordListBuilder.drop(35);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDrop");
    }

    @Test
    @Order(36)
    public void testNotNullDropWhile() {
        LOG.warn("testNotNullDropWhile");
        immutableList = recordListBuilder.dropWhile(RLBTImpl.predicate);
        assertNotNull(immutableList, "testNotNullDropWhile");
    }

    @Test
    @Order(36)
    public void testInstanceOfDropWhile() {
        LOG.warn("testInstanceOfDropWhile");
        immutableList = recordListBuilder.dropWhile(RLBTImpl.predicate);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDropWhile");
    }

    @Test
    @Order(37)
    public void testNotNullPartitionWhile() {
        LOG.warn("testNotNullPartitionWhile");
        partitionImmutableList = recordListBuilder.partitionWhile(RLBTImpl.predicate);
        assertNotNull(partitionImmutableList, "testNotNullPartitionWhile");
    }

    @Test
    @Order(37)
    public void testInstanceOfPartitionWhile() {
        LOG.warn("testInstanceOfPartitionWhile");
        partitionImmutableList = recordListBuilder.partitionWhile(RLBTImpl.predicate);
        assertTrue(partitionImmutableList instanceof PartitionImmutableList, "testInstanceOfPartitionWhile");
    }

    @Test
    @Order(38)
    public void testNotNullCastToList() {
        LOG.warn("testNotNullCastToList");
        immutableList = (ImmutableList) recordListBuilder.castToList();
        assertNotNull(immutableList, "testNotNullCastToList");
    }

    @Test
    @Order(38)
    public void testInstanceOfCastToList() {
        LOG.warn("testInstanceOfCastToList");
        immutableList = (ImmutableList) recordListBuilder.castToList();
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfCastToList");
    }


    @Test
    @Order(39)
    public void testNotNullSubList() {
        LOG.warn("testNotNullSubList");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.subList(0,1);
        assertNotNull(immutableList, "testNotNullSubList");
    }

    @Test
    @Order(39)
    public void testInstanceOfSubList() {
        LOG.warn("testInstanceOfSubList");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.subList(0,1);
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfSubList");
    }

    @Test
    @Order(40)
    public void testNotNullToReversed() {
        LOG.warn("testNotNullToReversed");
        immutableList = recordListBuilder.toReversed();
        assertNotNull(immutableList, "testNotNullToReversed");
    }

    @Test
    @Order(40)
    public void testInstanceOfToReversed() {
        LOG.warn("testInstanceOfToReversed");
        immutableList = recordListBuilder.toReversed();
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfToReversed");
    }

    @Test
    @Order(41)
    public void testNotNullSumByInt() {
        LOG.warn("testNotNullSumByInt");
        immutableObjectIntMap = recordListBuilder.sumByInt(RLBTImpl.function, RLBTImpl.intFunction);
        assertNotNull(immutableObjectIntMap, "testNotNullSumByInt");
    }

    @Test
    @Order(41)
    public void testInstanceOfSumByInt() {
        LOG.warn("testInstanceOfSumByInt");
        immutableObjectIntMap = recordListBuilder.sumByInt(RLBTImpl.function, RLBTImpl.intFunction);
        assertTrue(immutableObjectIntMap instanceof ImmutableObjectLongMap, "testInstanceOfSumByInt");
    }

    @Test
    @Order(42)
    public void testNotNullSumByFloat() {
        LOG.warn("testNotNullSumByFloat");
        immutableObjectFloatMap = recordListBuilder.sumByFloat(RLBTImpl.function, RLBTImpl.floatFunction);
        assertNotNull(immutableObjectFloatMap, "testNotNullSumByFloat");
    }

    @Test
    @Order(42)
    public void testInstanceOfSumByFloat() {
        LOG.warn("testInstanceOfSumByFloat");
        immutableObjectFloatMap = recordListBuilder.sumByFloat(RLBTImpl.function, RLBTImpl.floatFunction);
        assertTrue(immutableObjectFloatMap instanceof ImmutableObjectDoubleMap, "testInstanceOfSumByFloat");
    }

    @Test
    @Order(43)
    public void testNotNullSumByLong() {
        LOG.warn("testNotNullSumByLong");
        immutableObjectLongMap = recordListBuilder.sumByLong(RLBTImpl.function, RLBTImpl.longFunction);
        assertNotNull(immutableObjectLongMap, "testNotNullSumByLong");
    }

    @Test
    @Order(43)
    public void testInstanceOfSumByLong() {
        LOG.warn("testInstanceOfSumByLong");
        immutableObjectLongMap = recordListBuilder.sumByLong(RLBTImpl.function, RLBTImpl.longFunction);
        assertTrue(immutableObjectLongMap instanceof ImmutableObjectLongMap, "testInstanceOfSumByLong");
    }

    @Test
    @Order(44)
    public void testNotNullSumByDouble() {
        LOG.warn("testNotNullSumByDouble");
        immutableObjectDoubleMap = recordListBuilder.sumByDouble(RLBTImpl.function, RLBTImpl.doubleFunction);
        assertNotNull(immutableObjectDoubleMap, "testNotNullSumByDouble");
    }

    @Test
    @Order(44)
    public void testInstanceOfSumByDouble() {
        LOG.warn("testInstanceOfSumByDouble");
        immutableObjectDoubleMap = recordListBuilder.sumByDouble(RLBTImpl.function, RLBTImpl.doubleFunction);
        assertTrue(immutableObjectDoubleMap instanceof ImmutableObjectDoubleMap, "testInstanceOfSumByDouble");
    }

    @Test
    @Order(45)
    public void testNotNullCountBy() {
        LOG.warn("testNotNullCountBy");
        immutableBag = recordListBuilder.countBy(RLBTImpl.function);
        assertNotNull(immutableBag, "testNotNullCountBy");
    }

    @Test
    @Order(45)
    public void testInstanceOfCountBy() {
        LOG.warn("testInstanceOfCountBy");
        immutableBag = recordListBuilder.countBy(RLBTImpl.function);
        assertTrue(immutableBag instanceof ImmutableBag, "testInstanceOfCountBy");
    }

    @Test
    @Order(46)
    public void testNotNullCountByWith() {
        LOG.warn("testNotNullCountByWith");
        immutableBag = recordListBuilder.countByWith(RLBTImpl.function2, new Object());
        assertNotNull(immutableBag, "testNotNullCountByWith");
    }

    @Test
    @Order(46)
    public void testInstanceOfCountByWith() {
        LOG.warn("testInstanceOfCountByWith");
        immutableBag = recordListBuilder.countByWith(RLBTImpl.function2, new Object());
        assertTrue(immutableBag instanceof ImmutableBag, "testInstanceOfCountByWith");
    }

    @Test
    @Order(47)
    public void testNotNullCountByEach() {
        LOG.warn("testNotNullCountByEach");
        immutableBag = recordListBuilder.countByEach(RLBTImpl.function);
        assertNotNull(immutableBag, "testNotNullCountByEach");
    }

    @Test
    @Order(47)
    public void testInstanceOfCountByEach() {
        LOG.warn("testInstanceOfCountByEach");
        immutableBag = recordListBuilder.countByEach(RLBTImpl.function);
        assertTrue(immutableBag instanceof ImmutableBag, "testInstanceOfCountByEach");
    }

    @Test
    @Order(48)
    public void testNotNullGroupByUniqueKey() {
        LOG.warn("testNotNullGroupByUniqueKey");
        immutableMap = recordListBuilder.groupByUniqueKey(RLBTImpl.function);
        assertNotNull(immutableMap, "testNotNullGroupByUniqueKey");
    }

    @Test
    @Order(48)
    public void testInstanceOfGroupByUniqueKey() {
        LOG.warn("testInstanceOfGroupByUniqueKey");
        immutableMap = recordListBuilder.groupByUniqueKey(RLBTImpl.function);
        assertTrue(immutableMap instanceof ImmutableMap, "testInstanceOfGroupByUniqueKey");
    }

    @Test
    @Order(49)
    public void testNotNullAggregateInPlaceBy() {
        LOG.warn("testNotNullAggregateInPlaceBy");
        immutableMap = recordListBuilder.aggregateInPlaceBy(RLBTImpl.function, RLBTImpl.function0, RLBTImpl.procedure2);
        assertNotNull(immutableMap, "testNotNullAggregateInPlaceBy");
    }

    @Test
    @Order(49)
    public void testInstanceOfAggregateInPlaceBy() {
        LOG.warn("testInstanceOfAggregateInPlaceBy");
        immutableMap = recordListBuilder.aggregateInPlaceBy(RLBTImpl.function, RLBTImpl.function0, RLBTImpl.procedure2);
        assertTrue(immutableMap instanceof ImmutableMap, "testInstanceOfAggregateInPlaceBy");
    }

    @Test
    @Order(50)
    public void testNotNullAggregateBy() {
        LOG.warn("testNotNullAggregateBy");
        immutableMap = recordListBuilder.aggregateBy(RLBTImpl.function, RLBTImpl.function0, RLBTImpl.function2);
        assertNotNull(immutableMap, "testNotNullAggregateBy");
    }

    @Test
    @Order(50)
    public void testInstanceOfAggregateBy() {
        LOG.warn("testInstanceOfAggregateBy");
        immutableMap = recordListBuilder.aggregateBy(RLBTImpl.function, RLBTImpl.function0, RLBTImpl.function2);
        assertTrue(immutableMap instanceof ImmutableMap, "testInstanceOfAggregateBy");
    }

    @Test
    @Order(51)
    public void testNotNullStream() {
        LOG.warn("testNotNullStream");
        stream = recordListBuilder.stream();
        assertNotNull(stream, "testNotNullStream");
    }

    @Test
    @Order(51)
    public void testInstanceOfStream() {
        LOG.warn("testInstanceOfStream");
        stream = recordListBuilder.parallelStream();
        assertTrue(stream instanceof Stream, "testInstanceOfStream");
    }

    @Test
    @Order(52)
    public void testNotNullParallelStream() {
        LOG.warn("testNotNullParallelStream");
        stream = recordListBuilder.parallelStream();
        assertNotNull(stream, "testNotNullParallelStream");
    }

    @Test
    @Order(52)
    public void testInstanceOfParallelStream() {
        LOG.warn("testInstanceOfParallelStream");
        stream = recordListBuilder.parallelStream();
        assertTrue(stream instanceof Stream, "testInstanceOfParallelStream");
    }

    @Test
    @Order(53)
    public void testNotNullSplitIterator() {
        LOG.warn("testNotNullSplitIterator");
        spliterator = recordListBuilder.spliterator();
        assertNotNull(spliterator, "testNotNullSplitIterator");
    }

    @Test
    @Order(53)
    public void testInstanceOfSplitIterator() {
        LOG.warn("testInstanceOfSplitIterator");
        spliterator = recordListBuilder.spliterator();
        assertTrue(spliterator instanceof Spliterator, "testInstanceOfSplitIterator");
    }

    @Test
    @Order(54)
    public void testNotNullCastToCollection() {
        LOG.warn("testNotNullCastToCollection");
        collection = recordListBuilder.castToCollection();
        assertNotNull(collection, "testNotNullCastToCollection");
    }

    @Test
    @Order(54)
    public void testInstanceOfCastToCollection() {
        LOG.warn("testInstanceOfCastToCollection");
        collection = recordListBuilder.castToCollection();
        assertTrue(collection instanceof Collection, "testInstanceOfCastToCollection");
    }

    @Test
    @Order(55)
    public void testNotNullForEach() {
        LOG.warn("testNotNullForEach");
        recordListBuilder.forEach(RLBTImpl.procedure);
        assertNotNull(recordListBuilder, "testNotNullForEach");
    }

    @Test
    @Order(55)
    public void testInstanceOfForEach() {
        LOG.warn("testInstanceOfForEach");
        recordListBuilder.forEach(RLBTImpl.procedure);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfForEach");
    }

    @Test
    @Order(56)
    public void testNotNullSize() {
        LOG.warn("testNotNullSize");
        assertNotNull(recordListBuilder.size(), "testNotNullSize");
    }

    @Test
    @Order(56)
    public void testEqualsSize() {
        LOG.warn("testInstanceOfSize");
        assertEquals(0, recordListBuilder.size(), "testInstanceOfSize");
    }

    @Test
    @Order(57)
    public void testNotNullIsEmpty() {
        LOG.warn("testNotNullIsEmpty");
        assertNotNull(recordListBuilder.isEmpty(), "testNotNullSize");
    }

    @Test
    @Order(57)
    public void testTrueIsEmpty() {
        LOG.warn("testTrueIsEmpty");
        assertTrue(recordListBuilder.isEmpty(), "testTrueIsEmpty");
    }

    @Test
    @Order(58)
    public void testNotNullNotEmpty() {
        LOG.warn("testNotNullNotEmpty");
        assertNotNull(recordListBuilder.notEmpty(), "testNotNullNotEmpty");
    }

    @Test
    @Order(58)
    public void testFalseNotEmpty() {
        LOG.warn("testFalseNotEmpty");
        assertFalse(recordListBuilder.notEmpty(), "testFalseNotEmpty");
    }

    @Test
    @Order(59)
    public void testNotNullGetAny() {
        LOG.warn("testNotNullGetAny");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.getAny());
        assertNotNull(immutableList, "testNotNullGetAny");
    }

    @Test
    @Order(59)
    public void testInstanceOfGetAny() {
        LOG.warn("testInstanceOfGetAny");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.getAny());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfGetAny");
    }

    @Test
    @Order(60)
    public void testNotNullGetFirst() {
        LOG.warn("testNotNullGetFirst");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.getFirst());
        assertNotNull(immutableList, "testNotNullGetFirst");
    }

    @Test
    @Order(60)
    public void testInstanceOfGetFirst() {
        LOG.warn("testInstanceOfGetFirst");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.getFirst());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfGetFirst");
    }

    @Test
    @Order(61)
    public void testNotNullGetLast() {
        LOG.warn("testNotNullGetLast");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.getLast());
        assertNotNull(immutableList, "testNotNullGetLast");
    }

    @Test
    @Order(61)
    public void testInstanceOfGetLast() {
        LOG.warn("testInstanceOfGetLast");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.getLast());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfGetLast");
    }

    @Test
    @Order(62)
    public void testNotNullGetOnly() {
        LOG.warn("testNotNullGetOnly");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.getOnly());
        assertNotNull(immutableList, "testNotNullGetOnly");
    }

    @Test
    @Order(62)
    public void testInstanceOfGetOnly() {
        LOG.warn("testInstanceOfGetOnly");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.getOnly());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfGetOnly");
    }

    @Test
    @Order(63)
    public void testNotNullContains() {
        LOG.warn("testNotNullContains");
        assertNotNull(recordListBuilder.contains("Test String"), "testNotNullContains");
    }

    @Test
    @Order(63)
    public void testTrueContains() {
        LOG.warn("testTrueContains");
        recordListBuilder.add("Test String");
        assertTrue(recordListBuilder.contains("Test String"), "testTrueContains");
    }

    @Test
    @Order(64)
    public void testNotNullContainsBy() {
        LOG.warn("testNotNullContainsBy");
        assertNotNull(recordListBuilder.containsBy(RLBTImpl.function, "Test String"), "testNotNullContainsBy");
    }

    @Test
    @Order(64)
    public void testFalseContainsBy() {
        LOG.warn("testFalseContainsBy");
        assertFalse(recordListBuilder.containsBy(RLBTImpl.function, "Test String"), "testFalseContainsBy");
    }

    @Test
    @Order(65)
    public void testNotNullContainsAllIterable() {
        LOG.warn("testNotNullContainsAllIterable");
        immutableList = recordListBuilder.newWithout("Test String");
        assertNotNull(recordListBuilder.containsAllIterable(Collections.singleton(immutableList.iterator())), "testNotNullContainsAllIterable");
    }

    @Test
    @Order(65)
    public void testFalseContainsAllIterable() {
        LOG.warn("testFalseContainsAllIterable");
        immutableList = recordListBuilder.newWithout("Test String");
        assertFalse(recordListBuilder.containsAllIterable(Collections.singleton(immutableList.iterator())), "testFalseContainsAllIterable");
    }

    @Test
    @Order(66)
    public void testNotNullContainsAll() {
        LOG.warn("testNotNullContainsAll");
        immutableList = recordListBuilder.newWithout("Test String");
        assertNotNull(recordListBuilder.containsAll(Collections.singleton(immutableList.iterator())), "testNotNullContainsAll");
    }

    @Test
    @Order(66)
    public void testFalseContainsAll() {
        LOG.warn("testFalseContainsAll");
        immutableList = recordListBuilder.newWithout("Test String");
        assertFalse(recordListBuilder.containsAllIterable(Collections.singleton(immutableList.iterator())), "testFalseContainsAll");
    }

    @Test
    @Order(67)
    public void testNotNullContainsAllArguments() {
        LOG.warn("testNotNullContainsAllArguments");
        immutableList = recordListBuilder.newWithout("Test String");
        assertNotNull(recordListBuilder.containsAllArguments(Collections.singleton(immutableList.iterator())), "testNotNullContainsAllArguments");
    }

    @Test
    @Order(67)
    public void testFalseContainsAllArguments() {
        LOG.warn("testFalseContainsAllArguments");
        immutableList = recordListBuilder.newWithout("Test String");
        assertFalse(recordListBuilder.containsAllArguments(Collections.singleton(immutableList.iterator())), "testFalseContainsAllArguments");
    }

    @Test
    @Order(68)
    public void testNotNullEach() {
        LOG.warn("testNotNullEach");
        recordListBuilder.each(RLBTImpl.procedure);
        assertNotNull(recordListBuilder, "testNotNullEach");
    }

    @Test
    @Order(68)
    public void testInstanceOfEach() {
        LOG.warn("testInstanceOfEach");
        recordListBuilder.each(RLBTImpl.procedure);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfEach");
    }

    @Test
    @Order(69)
    public void testNotNullSelectCollection() {
        LOG.warn("testNotNullSelectCollection");
        collection = recordListBuilder.select(RLBTImpl.predicate, RLBTImpl.collection);
        assertNotNull(collection, "testNotNullSelectCollection");
    }

    @Test
    @Order(69)
    public void testInstanceOfSelectCollection() {
        LOG.warn("testInstanceOfSelectCollection");
        collection = recordListBuilder.select(RLBTImpl.predicate, RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfSelectCollection");
    }

    @Test
    @Order(70)
    public void testNotNullSelectWithCollection() {
        LOG.warn("testNotNullSelectWithCollection");
        collection = recordListBuilder.selectWith(RLBTImpl.predicate2, new Object(), RLBTImpl.collection);
        assertNotNull(collection, "testNotNullSelectWithCollection");
    }

    @Test
    @Order(70)
    public void testInstanceOfSelectWithCollection() {
        LOG.warn("testInstanceOfSelectWithCollection");
        collection = recordListBuilder.selectWith(RLBTImpl.predicate2, new Object(), RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfSelectWithCollection");
    }

    @Test
    @Order(71)
    public void testNotNullRejectCollection() {
        LOG.warn("testNotNullRejectCollection");
        collection = recordListBuilder.reject(RLBTImpl.predicate, RLBTImpl.collection);
        assertNotNull(collection, "testNotNullRejectCollection");
    }

    @Test
    @Order(71)
    public void testInstanceOfRejectCollection() {
        LOG.warn("testInstanceOfRejectCollection");
        collection = recordListBuilder.reject(RLBTImpl.predicate, RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfRejectCollection");
    }

    @Test
    @Order(72)
    public void testNotNullRejectWithCollection() {
        LOG.warn("testNotNullRejectWithCollection");
        collection = recordListBuilder.rejectWith(RLBTImpl.predicate2, new Object(), RLBTImpl.collection);
        assertNotNull(collection, "testNotNullRejectWithCollection");
    }

    @Test
    @Order(72)
    public void testInstanceOfRejectWithCollection() {
        LOG.warn("testInstanceOfRejectWithCollection");
        collection = recordListBuilder.rejectWith(RLBTImpl.predicate2, new Object(), RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfRejectWithCollection");
    }

    @Test
    @Order(73)
    public void testNotNullCollectCollection() {
        LOG.warn("testNotNullCollectCollection");
        collection = recordListBuilder.collect(RLBTImpl.function, RLBTImpl.collection);
        assertNotNull(collection, "testNotNullCollectCollection");
    }

    @Test
    @Order(73)
    public void testInstanceOfCollectCollection() {
        LOG.warn("testInstanceOfCollectCollection");
        collection = recordListBuilder.collect(RLBTImpl.function, RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfCollectCollection");
    }

    @Test
    @Order(74)
    public void testNotNullCollectBooleanMutable() {
        LOG.warn("testNotNullCollectBooleanMutable");
        mutableBooleanCollection = recordListBuilder.collectBoolean(RLBTImpl.booleanFunction, RLBTImpl.mutableBooleanCollection);
        assertNotNull(mutableBooleanCollection, "testNotNullCollectBooleanMutable");
    }

    @Test
    @Order(74)
    public void testInstanceOfCollectBooleanMutable() {
        LOG.warn("testInstanceOfCollectBooleanMutable");
        mutableBooleanCollection = recordListBuilder.collectBoolean(RLBTImpl.booleanFunction, RLBTImpl.mutableBooleanCollection);
        assertTrue(mutableBooleanCollection instanceof MutableBooleanCollection, "testInstanceOfCollectBooleanMutable");
    }

    @Test
    @Order(75)
    public void testNotNullCollectByteMutable() {
        LOG.warn("testNotNullCollectByteMutable");
        mutableByteCollection = recordListBuilder.collectByte(RLBTImpl.byteFunction, RLBTImpl.mutableByteCollection);
        assertNotNull(mutableByteCollection, "testNotNullCollectByteMutable");
    }

    @Test
    @Order(75)
    public void testInstanceOfCollectByteMutable() {
        LOG.warn("testInstanceOfCollectByteMutable");
        mutableByteCollection = recordListBuilder.collectByte(RLBTImpl.byteFunction, RLBTImpl.mutableByteCollection);
        assertTrue(mutableByteCollection instanceof MutableByteCollection, "testInstanceOfCollectByteMutable");
    }

    @Test
    @Order(76)
    public void testNotNullCollectCharMutable() {
        LOG.warn("testNotNullCollectCharMutable");
        mutableCharCollection = recordListBuilder.collectChar(RLBTImpl.charFunction, RLBTImpl.mutableCharCollection);
        assertNotNull(mutableCharCollection, "testNotNullCollectCharMutable");
    }

    @Test
    @Order(76)
    public void testInstanceOfCollectCharMutable() {
        LOG.warn("testInstanceOfCollectCharMutable");
        mutableCharCollection = recordListBuilder.collectChar(RLBTImpl.charFunction, RLBTImpl.mutableCharCollection);
        assertTrue(mutableCharCollection instanceof MutableCharCollection, "testInstanceOfCollectCharMutable");
    }

    @Test
    @Order(77)
    public void testNotNullCollectDoubleMutable() {
        LOG.warn("testNotNullCollectDoubleMutable");
        mutableDoubleCollection = recordListBuilder.collectDouble(RLBTImpl.doubleFunction, RLBTImpl.mutableDoubleCollection);
        assertNotNull(mutableDoubleCollection, "testNotNullCollectDoubleMutable");
    }

    @Test
    @Order(77)
    public void testInstanceOfCollectDoubleMutable() {
        LOG.warn("testInstanceOfCollectDoubleMutable");
        mutableDoubleCollection = recordListBuilder.collectDouble(RLBTImpl.doubleFunction, RLBTImpl.mutableDoubleCollection);
        assertTrue(mutableDoubleCollection instanceof MutableDoubleCollection, "testInstanceOfCollectDoubleMutable");
    }

    @Test
    @Order(78)
    public void testNotNullCollectFloatMutable() {
        LOG.warn("testNotNullCollectFloatMutable");
        mutableFloatCollection = recordListBuilder.collectFloat(RLBTImpl.floatFunction, RLBTImpl.mutableFloatCollection);
        assertNotNull(mutableFloatCollection, "testNotNullCollectFloatMutable");
    }

    @Test
    @Order(78)
    public void testInstanceOfCollectFloatMutable() {
        LOG.warn("testInstanceOfCollectFloatMutable");
        mutableFloatCollection = recordListBuilder.collectFloat(RLBTImpl.floatFunction, RLBTImpl.mutableFloatCollection);
        assertTrue(mutableFloatCollection instanceof MutableFloatCollection, "testInstanceOfCollectFloatMutable");
    }

    @Test
    @Order(79)
    public void testNotNullCollectIntMutable() {
        LOG.warn("testNotNullCollectIntMutable");
        mutableIntCollection = recordListBuilder.collectInt(RLBTImpl.intFunction, RLBTImpl.mutableIntCollection);
        assertNotNull(mutableIntCollection, "testNotNullCollectIntMutable");
    }

    @Test
    @Order(79)
    public void testInstanceOfCollectIntMutable() {
        LOG.warn("testInstanceOfCollectIntMutable");
        mutableIntCollection = recordListBuilder.collectInt(RLBTImpl.intFunction, RLBTImpl.mutableIntCollection);
        assertTrue(mutableIntCollection instanceof MutableIntCollection, "testInstanceOfCollectIntMutable");
    }

    @Test
    @Order(80)
    public void testNotNullCollectLongMutable() {
        LOG.warn("testNotNullCollectLongMutable");
        mutableLongCollection = recordListBuilder.collectLong(RLBTImpl.longFunction, RLBTImpl.mutableLongCollection);
        assertNotNull(mutableLongCollection, "testNotNullCollectLongMutable");
    }

    @Test
    @Order(80)
    public void testInstanceOfCollectLongMutable() {
        LOG.warn("testInstanceOfCollectLongMutable");
        mutableLongCollection = recordListBuilder.collectLong(RLBTImpl.longFunction, RLBTImpl.mutableLongCollection);
        assertTrue(mutableLongCollection instanceof MutableLongCollection, "testInstanceOfCollectLongMutable");
    }

    @Test
    @Order(81)
    public void testNotNullCollectShortMutable() {
        LOG.warn("testNotNullCollectShortMutable");
        mutableShortCollection = recordListBuilder.collectShort(RLBTImpl.shortFunction, RLBTImpl.mutableShortCollection);
        assertNotNull(mutableShortCollection, "testNotNullCollectShortMutable");
    }

    @Test
    @Order(81)
    public void testInstanceOfCollectShortMutable() {
        LOG.warn("testInstanceOfCollectShortMutable");
        mutableShortCollection = recordListBuilder.collectShort(RLBTImpl.shortFunction, RLBTImpl.mutableShortCollection);
        assertTrue(mutableShortCollection instanceof MutableShortCollection, "testInstanceOfCollectShortMutable");
    }

    @Test
    @Order(82)
    public void testNotNullCollectWithCollection() {
        LOG.warn("testNotNullCollectWithCollection");
        collection = recordListBuilder.collectWith(RLBTImpl.function2, new Object(), RLBTImpl.collection);
        assertNotNull(collection, "testNotNullCollectWithCollection");
    }

    @Test
    @Order(82)
    public void testInstanceOfCollectWithCollection() {
        LOG.warn("testInstanceOfCollectWithCollection");
        collection = recordListBuilder.collectWith(RLBTImpl.function2, new Object(), RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfCollectWithCollection");
    }

    @Test
    @Order(83)
    public void testNotNullCollectIfCollection() {
        LOG.warn("testNotNullCollectIfCollection");
        collection = recordListBuilder.collectIf(RLBTImpl.predicate, RLBTImpl.function, RLBTImpl.collection);
        assertNotNull(collection, "testNotNullCollectIfCollection");
    }

    @Test
    @Order(83)
    public void testInstanceOfCollectIfCollection() {
        LOG.warn("testInstanceOfCollectIfCollection");
        collection = recordListBuilder.collectIf(RLBTImpl.predicate, RLBTImpl.function, RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfCollectIfCollection");
    }

    @Test
    @Order(84)
    public void testNotNullFlatCollectByte() {
        LOG.warn("testNotNullFlatCollectByte");
        mutableByteCollection = recordListBuilder.flatCollectByte(RLBTImpl.function, RLBTImpl.mutableByteCollection);
        assertNotNull(mutableByteCollection, "testNotNullFlatCollectByte");
    }

    @Test
    @Order(84)
    public void testInstanceOfFlatCollectByte() {
        LOG.warn("testInstanceOfFlatCollectByte");
        mutableByteCollection = recordListBuilder.collectByte(RLBTImpl.byteFunction, RLBTImpl.mutableByteCollection);
        assertTrue(mutableByteCollection instanceof MutableByteCollection, "testInstanceOfFlatCollectByte");
    }

    @Test
    @Order(85)
    public void testNotNullFlatCollectChar() {
        LOG.warn("testNotNullFlatCollectChar");
        mutableCharCollection = recordListBuilder.flatCollectChar(RLBTImpl.function, RLBTImpl.mutableCharCollection);
        assertNotNull(mutableCharCollection, "testNotNullFlatCollectChar");
    }

    @Test
    @Order(85)
    public void testInstanceOfFlatCollectChar() {
        LOG.warn("testInstanceOfFlatCollectChar");
        mutableCharCollection = recordListBuilder.flatCollectChar(RLBTImpl.function, RLBTImpl.mutableCharCollection);
        assertTrue(mutableCharCollection instanceof MutableCharCollection, "testInstanceOfFlatCollectChar");
    }

    @Test
    @Order(86)
    public void testNotNullFlatCollectInt() {
        LOG.warn("testNotNullFlatCollectInt");
        mutableIntCollection = recordListBuilder.flatCollectInt(RLBTImpl.function, RLBTImpl.mutableIntCollection);
        assertNotNull(mutableIntCollection, "testNotNullFlatCollectInt");
    }

    @Test
    @Order(86)
    public void testInstanceOfFlatCollectInt() {
        LOG.warn("testInstanceOfFlatCollectInt");
        mutableIntCollection = recordListBuilder.flatCollectInt(RLBTImpl.function, RLBTImpl.mutableIntCollection);
        assertTrue(mutableIntCollection instanceof MutableIntCollection, "testInstanceOfFlatCollectInt");
    }

    @Test
    @Order(87)
    public void testNotNullFlatCollectShort() {
        LOG.warn("testNotNullFlatCollectShort");
        mutableShortCollection = recordListBuilder.flatCollectShort(RLBTImpl.function, RLBTImpl.mutableShortCollection);
        assertNotNull(mutableShortCollection, "testNotNullFlatCollectShort");
    }

    @Test
    @Order(87)
    public void testInstanceOfFlatCollectShort() {
        LOG.warn("testInstanceOfFlatCollectShort");
        mutableShortCollection = recordListBuilder.flatCollectShort(RLBTImpl.function, RLBTImpl.mutableShortCollection);
        assertTrue(mutableShortCollection instanceof MutableShortCollection, "testInstanceOfFlatCollectShort");
    }

    @Test
    @Order(88)
    public void testNotNullFlatCollectDouble() {
        LOG.warn("testNotNullFlatCollectDouble");
        mutableDoubleCollection = recordListBuilder.flatCollectDouble(RLBTImpl.function, RLBTImpl.mutableDoubleCollection);
        assertNotNull(mutableDoubleCollection, "testNotNullFlatCollectDouble");
    }

    @Test
    @Order(88)
    public void testInstanceOfFlatCollectDouble() {
        LOG.warn("testInstanceOfFlatCollectDouble");
        mutableDoubleCollection = recordListBuilder.flatCollectDouble(RLBTImpl.function, RLBTImpl.mutableDoubleCollection);
        assertTrue(mutableDoubleCollection instanceof MutableDoubleCollection, "testInstanceOfFlatCollectDouble");
    }

    @Test
    @Order(89)
    public void testNotNullFlatCollectFloat() {
        LOG.warn("testNotNullFlatCollectFloat");
        mutableFloatCollection = recordListBuilder.flatCollectFloat(RLBTImpl.function, RLBTImpl.mutableFloatCollection);
        assertNotNull(mutableFloatCollection, "testNotNullFlatCollectFloat");
    }

    @Test
    @Order(89)
    public void testInstanceOfFlatCollectFloat() {
        LOG.warn("testInstanceOfFlatCollectFloat");
        mutableFloatCollection = recordListBuilder.flatCollectFloat(RLBTImpl.function, RLBTImpl.mutableFloatCollection);
        assertTrue(mutableFloatCollection instanceof MutableFloatCollection, "testInstanceOfFlatCollectFloat");
    }

    @Test
    @Order(90)
    public void testNotNullFlatCollectLong() {
        LOG.warn("testNotNullFlatCollectLong");
        mutableLongCollection = recordListBuilder.flatCollectLong(RLBTImpl.function, RLBTImpl.mutableLongCollection);
        assertNotNull(mutableLongCollection, "testNotNullFlatCollectLong");
    }

    @Test
    @Order(90)
    public void testInstanceOfFlatCollectLong() {
        LOG.warn("testInstanceOfFlatCollectLong");
        mutableLongCollection = recordListBuilder.flatCollectLong(RLBTImpl.function, RLBTImpl.mutableLongCollection);
        assertTrue(mutableLongCollection instanceof MutableLongCollection, "testInstanceOfFlatCollectLong");
    }

    @Test
    @Order(91)
    public void testNotNullFlatCollectBoolean() {
        LOG.warn("testNotNullFlatCollectBoolean");
        mutableBooleanCollection = recordListBuilder.flatCollectBoolean(RLBTImpl.function, RLBTImpl.mutableBooleanCollection);
        assertNotNull(mutableBooleanCollection, "testNotNullFlatCollectBoolean");
    }

    @Test
    @Order(91)
    public void testInstanceOfFlatCollectBoolean() {
        LOG.warn("testInstanceOfFlatCollectBoolean");
        mutableBooleanCollection = recordListBuilder.flatCollectBoolean(RLBTImpl.function, RLBTImpl.mutableBooleanCollection);
        assertTrue(mutableBooleanCollection instanceof MutableBooleanCollection, "testInstanceOfFlatCollectBoolean");
    }

    @Test
    @Order(92)
    public void testNotNullFlatCollectCollection() {
        LOG.warn("testNotNullFlatCollectCollection");
        collection = recordListBuilder.flatCollect(RLBTImpl.function, RLBTImpl.collection);
        assertNotNull(collection, "testNotNullFlatCollectCollection");
    }

    @Test
    @Order(92)
    public void testInstanceOfFlatCollectCollection() {
        LOG.warn("testInstanceOfFlatCollectCollection");
        collection = recordListBuilder.flatCollect(RLBTImpl.function, RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfFlatCollectCollection");
    }

    @Test
    @Order(93)
    public void testNotNullFlatCollectWithCollection() {
        LOG.warn("testNotNullFlatCollectWithCollection");
        collection = recordListBuilder.flatCollectWith(RLBTImpl.function2, new Object(), RLBTImpl.collection);
        assertNotNull(collection, "testNotNullFlatCollectWithCollection");
    }

    @Test
    @Order(93)
    public void testInstanceOfFlatCollectWithCollection() {
        LOG.warn("testInstanceOfFlatCollectWithCollection");
        collection = recordListBuilder.flatCollectWith(RLBTImpl.function2, new Object(), RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfFlatCollectWithCollection");
    }

    @Test
    @Order(94)
    public void testNotNullDetect() {
        LOG.warn("testNotNullDetect");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.detect(RLBTImpl.predicate));
        assertNotNull(immutableList, "testNotNullDetect");
    }

    @Test
    @Order(94)
    public void testInstanceOfDetect() {
        LOG.warn("testInstanceOfDetect");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.detect(RLBTImpl.predicate));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDetect");
    }

    @Test
    @Order(95)
    public void testNotNullDetectWith() {
        LOG.warn("testNotNullDetectWith");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.detectWith(RLBTImpl.predicate2, new Object()));
        assertNotNull(immutableList, "testNotNullDetectWith");
    }

    @Test
    @Order(95)
    public void testInstanceOfDetectWith() {
        LOG.warn("testInstanceOfDetectWith");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.detectWith(RLBTImpl.predicate2, new Object()));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDetectWith");
    }

    @Test
    @Order(96)
    public void testNotNullDetectOptional() {
        LOG.warn("testNotNullDetectOptional");
        optional = recordListBuilder.detectOptional(RLBTImpl.predicate);
        assertNotNull(optional, "testNotNullDetectOptional");
    }

    @Test
    @Order(96)
    public void testInstanceOfDetectOptional() {
        LOG.warn("testInstanceOfDetectOptional");
        optional = recordListBuilder.detectOptional(RLBTImpl.predicate);
        assertTrue(optional instanceof Optional, "testInstanceOfDetectOptional");
    }

    @Test
    @Order(97)
    public void testNotNullDetectWithOptional() {
        LOG.warn("testNotNullDetectWithOptional");
        optional = recordListBuilder.detectWithOptional(RLBTImpl.predicate2, "Test String");
        assertNotNull(optional, "testNotNullDetectWithOptional");
    }

    @Test
    @Order(97)
    public void testInstanceOfDetectWithOptional() {
        LOG.warn("testInstanceOfDetectWithOptional");
        optional = recordListBuilder.detectWithOptional(RLBTImpl.predicate2, "Test String");
        assertTrue(optional instanceof Optional, "testInstanceOfDetectWithOptional");
    }

    @Test
    @Order(98)
    public void testNotNullDetectIfNone() {
        LOG.warn("testNotNullDetectIfNone");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.detectIfNone(RLBTImpl.predicate, RLBTImpl.function0));
        assertNotNull(immutableList, "testNotNullDetectIfNone");
    }

    @Test
    @Order(98)
    public void testInstanceOfDetectIfNone() {
        LOG.warn("testInstanceOfDetectIfNone");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.detectIfNone(RLBTImpl.predicate, RLBTImpl.function0));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDetectIfNone");
    }

    @Test
    @Order(99)
    public void testNotNullDetectWithIfNone() {
        LOG.warn("testNotNullDetectWithIfNone");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.detectWithIfNone(RLBTImpl.predicate2, new Object(), RLBTImpl.function0));
        assertNotNull(immutableList, "testNotNullDetectWithIfNone");
    }

    @Test
    @Order(99)
    public void testInstanceOfDetectWithIfNone() {
        LOG.warn("testInstanceOfDetectWithIfNone");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.detectWithIfNone(RLBTImpl.predicate2, new Object(), RLBTImpl.function0));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfDetectWithIfNone");
    }

    @Test
    @Order(100)
    public void testNotNullCount() {
        LOG.warn("testNotNullCount");
        assertNotNull(recordListBuilder.count(RLBTImpl.predicate), "testNotNullCount");
    }

    @Test
    @Order(100)
    public void testEqualsCount() {
        LOG.warn("testInstanceOfCount");
        assertEquals(0, recordListBuilder.count(RLBTImpl.predicate), "testInstanceOfCount");
    }

    @Test
    @Order(101)
    public void testNotNullCountWith() {
        LOG.warn("testNotNullCountWith");
        assertNotNull(recordListBuilder.countWith(RLBTImpl.predicate2, new Object()), "testNotNullCountWith");
    }

    @Test
    @Order(101)
    public void testEqualsCountWith() {
        LOG.warn("testInstanceOfCountWith");
        assertEquals(0, recordListBuilder.countWith(RLBTImpl.predicate2, new Object()), "testInstanceOfCountWith");
    }

    @Test
    @Order(102)
    public void testNotNullAnySatisfy() {
        LOG.warn("testNotNullAnySatisfy");
        assertNotNull(recordListBuilder.anySatisfy(RLBTImpl.predicate), "testNotNullAnySatisfy");
    }

    @Test
    @Order(102)
    public void testFalseAnySatisfy() {
        LOG.warn("testFalseAnySatisfy");
        recordListBuilder.add("Test String");
        assertFalse(recordListBuilder.anySatisfy(RLBTImpl.predicate), "testFalseAnySatisfy");
    }

    @Test
    @Order(103)
    public void testNotNullAnySatisfyWith() {
        LOG.warn("testNotNullAnySatisfyWith");
        assertNotNull(recordListBuilder.anySatisfyWith(RLBTImpl.predicate2, "Test String"), "testNotNullAnySatisfyWith");
    }

    @Test
    @Order(103)
    public void testFalseAnySatisfyWith() {
        LOG.warn("testFalseAnySatisfyWith");
        recordListBuilder.add("Test String");
        assertFalse(recordListBuilder.anySatisfyWith(RLBTImpl.predicate2, "Test String"), "testFalseAnySatisfyWith");
    }

    @Test
    @Order(104)
    public void testNotNullAllSatisfy() {
        LOG.warn("testNotNullAllSatisfy");
        assertNotNull(recordListBuilder.allSatisfy(RLBTImpl.predicate), "testNotNullAllSatisfy");
    }

    @Test
    @Order(104)
    public void testFalseAllSatisfy() {
        LOG.warn("testFalseAllSatisfy");
        recordListBuilder.add("Test String");
        assertFalse(recordListBuilder.allSatisfy(RLBTImpl.predicate), "testFalseAllSatisfy");
    }

    @Test
    @Order(105)
    public void testNotNullAllSatisfyWith() {
        LOG.warn("testNotNullAllSatisfyWith");
        assertNotNull(recordListBuilder.allSatisfyWith(RLBTImpl.predicate2, "Test String"), "testNotNullAllSatisfyWith");
    }

    @Test
    @Order(105)
    public void testFalseAllSatisfyWith() {
        LOG.warn("testFalseAllSatisfyWith");
        recordListBuilder.add("Test String");
        assertFalse(recordListBuilder.allSatisfyWith(RLBTImpl.predicate2, "Test String"), "testFalseAllSatisfyWith");
    }

    @Test
    @Order(106)
    public void testNotNullNoneSatisfy() {
        LOG.warn("testNotNullNoneSatisfy");
        assertNotNull(recordListBuilder.noneSatisfy(RLBTImpl.predicate), "testNotNullNoneSatisfy");
    }

    @Test
    @Order(106)
    public void testTrueNoneSatisfy() {
        LOG.warn("testTrueNoneSatisfy");
        recordListBuilder.add("Test String");
        assertTrue(recordListBuilder.noneSatisfy(RLBTImpl.predicate), "testTrueNoneSatisfy");
    }

    @Test
    @Order(107)
    public void testNotNullNoneSatisfyWith() {
        LOG.warn("testNotNullNoneSatisfyWith");
        assertNotNull(recordListBuilder.noneSatisfyWith(RLBTImpl.predicate2, "Test String"), "testNotNullNoneSatisfyWith");
    }

    @Test
    @Order(107)
    public void testTrueNoneSatisfyWith() {
        LOG.warn("testTrueNoneSatisfyWith");
        recordListBuilder.add("Test String");
        assertTrue(recordListBuilder.noneSatisfyWith(RLBTImpl.predicate2, "Test String"), "testTrueNoneSatisfyWith");
    }

    @Test
    @Order(108)
    public void testNotNullInjectIntoIV() {
        LOG.warn("testNotNullInjectIntoIV");
        assertNotNull(recordListBuilder.injectInto(0, RLBTImpl.function2), "testNotNullInjectIntoIV");
    }

    @Test
    @Order(108)
    public void testNotEqualsInjectIntoIV() {
        LOG.warn("testNotEqualsInjectIntoIV");
        assertNotEquals(0, RLBTImpl.function2, "testNotEqualsInjectIntoIV");
    }

    @Test
    @Order(109)
    public void testNotNullInjectIntoInt() {
        LOG.warn("testNotNullInjectIntoInt");
        assertNotNull(recordListBuilder.injectInto(0, RLBTImpl.intObjectToIntFunction), "testNotNullInjectIntoInt");
    }

    @Test
    @Order(109)
    public void testEqualsInjectIntoInt() {
        LOG.warn("testEqualsInjectIntoInt");
        assertEquals(0, recordListBuilder.injectInto(0, RLBTImpl.intObjectToIntFunction), "testEqualsInjectIntoInt");
    }

    @Test
    @Order(110)
    public void testNotNullInjectIntoLong() {
        LOG.warn("testNotNullInjectIntoLong");
        assertNotNull(recordListBuilder.injectInto(0, RLBTImpl.longObjectToLongFunction), "testNotNullInjectIntoLong");
    }

    @Test
    @Order(110)
    public void testEqualsInjectIntoLong() {
        LOG.warn("testEqualsInjectIntoLong");
        assertEquals(0, recordListBuilder.injectInto(0, RLBTImpl.longObjectToLongFunction), "testEqualsInjectIntoLong");
    }

    @Test
    @Order(111)
    public void testNotNullInjectIntoFloat() {
        LOG.warn("testNotNullInjectIntoFloat");
        assertNotNull(recordListBuilder.injectInto(0, RLBTImpl.floatObjectToFloatFunction), "testNotNullInjectIntoFloat");
    }

    @Test
    @Order(111)
    public void testEqualsInjectIntoFloat() {
        LOG.warn("testEqualsInjectIntoFloat");
        assertEquals(0, recordListBuilder.injectInto(0, RLBTImpl.floatObjectToFloatFunction), "testEqualsInjectIntoFloat");
    }

    @Test
    @Order(112)
    public void testNotNullInjectIntoDouble() {
        LOG.warn("testNotNullInjectIntoDouble");
        assertNotNull(recordListBuilder.injectInto(0.0, RLBTImpl.doubleObjectToDoubleFunction), "testNotNullInjectIntoDouble");
    }

    @Test
    @Order(112)
    public void testEqualsInjectIntoDouble() {
        LOG.warn("testEqualsInjectIntoDouble");
        assertEquals(0, recordListBuilder.injectInto(0.0, RLBTImpl.doubleObjectToDoubleFunction), "testEqualsInjectIntoDouble");
    }

    @Test
    @Order(113)
    public void testNotNullInto() {
        LOG.warn("testNotNullInto");
        collection = recordListBuilder.into(RLBTImpl.collection);
        assertNotNull(collection, "testNotNullInto");
    }

    @Test
    @Order(113)
    public void testInstanceOfInto() {
        LOG.warn("testInstanceOfInto");
        collection = recordListBuilder.into(RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfInto");
    }

    @Test
    @Order(114)
    public void testNotNullToList() {
        LOG.warn("testNotNullToList");
        mutableList = recordListBuilder.toList();
        assertNotNull(mutableList, "testNotNullToList");
    }

    @Test
    @Order(114)
    public void testInstanceOfToList() {
        LOG.warn("testInstanceOfToList");
        mutableList = recordListBuilder.toList();
        assertTrue(mutableList instanceof MutableList, "testInstanceOfToList");
    }

    @Test
    @Order(115)
    public void testNotNullToSortedList() {
        LOG.warn("testNotNullToSortedList");
        mutableList = recordListBuilder.toSortedList();
        assertNotNull(mutableList, "testNotNullToSortedList");
    }

    @Test
    @Order(115)
    public void testInstanceOfToSortedList() {
        LOG.warn("testInstanceOfToSortedList");
        mutableList = recordListBuilder.toSortedList();
        assertTrue(mutableList instanceof MutableList, "testInstanceOfToSortedList");
    }

    @Test
    @Order(116)
    public void testNotNullToSortedListComparator() {
        LOG.warn("testNotNullToSortedListComparator");
        mutableList = recordListBuilder.toSortedList(RLBTImpl.comparator);
        assertNotNull(mutableList, "testNotNullToSortedListComparator");
    }

    @Test
    @Order(116)
    public void testInstanceOfToSortedListComparator() {
        LOG.warn("testInstanceOfToSortedListComparator");
        mutableList = recordListBuilder.toSortedList(RLBTImpl.comparator);
        assertTrue(mutableList instanceof MutableList, "testInstanceOfToSortedListComparator");
    }

    @Test
    @Order(117)
    public void testNotNullToSortedListBy() {
        LOG.warn("testNotNullToSortedListBy");
        mutableList = recordListBuilder.toSortedListBy(RLBTImpl.function);
        assertNotNull(mutableList, "testNotNullToSortedListBy");
    }

    @Test
    @Order(117)
    public void testInstanceOfToSortedListBy() {
        LOG.warn("testInstanceOfToSortedListBy");
        mutableList = recordListBuilder.toSortedListBy(RLBTImpl.function);
        assertTrue(mutableList instanceof MutableList, "testInstanceOfToSortedListBy");
    }

    @Test
    @Order(118)
    public void testNotNullToSet() {
        LOG.warn("testNotNullToSet");
        mutableSet = recordListBuilder.toSet();
        assertNotNull(mutableSet, "testNotNullToSet");
    }

    @Test
    @Order(118)
    public void testInstanceOfToSet() {
        LOG.warn("testInstanceOfToSet");
        mutableSet = recordListBuilder.toSet();
        assertTrue(mutableSet instanceof MutableSet, "testInstanceOfToSet");
    }

    @Test
    @Order(119)
    public void testNotNullToSortedSet() {
        LOG.warn("testNotNullToSortedSet");
        mutableSortedSet = recordListBuilder.toSortedSet();
        assertNotNull(mutableSortedSet, "testNotNullToSortedSet");
    }

    @Test
    @Order(119)
    public void testInstanceOfToSortedSet() {
        LOG.warn("testInstanceOfToSortedSet");
        mutableSortedSet = recordListBuilder.toSortedSet();
        assertTrue(mutableSortedSet instanceof MutableSortedSet, "testInstanceOfToSortedSet");
    }

    @Test
    @Order(120)
    public void testNotNullToSortedSetComparator() {
        LOG.warn("testNotNullToSortedSetComparator");
        mutableSortedSet = recordListBuilder.toSortedSet(RLBTImpl.comparator);
        assertNotNull(mutableSortedSet, "testNotNullToSortedSetComparator");
    }

    @Test
    @Order(120)
    public void testInstanceOfToSortedSetComparator() {
        LOG.warn("testInstanceOfToSortedSetComparator");
        mutableSortedSet = recordListBuilder.toSortedSet(RLBTImpl.comparator);
        assertTrue(mutableSortedSet instanceof MutableSortedSet, "testInstanceOfToSortedSetComparator");
    }

    @Test
    @Order(121)
    public void testNotNullToSortedSetBy() {
        LOG.warn("testNotNullToSortedSetBy");
        mutableSortedSet = recordListBuilder.toSortedSetBy(RLBTImpl.function);
        assertNotNull(mutableSortedSet, "testNotNullToSortedSetBy");
    }

    @Test
    @Order(121)
    public void testInstanceOfToSortedSetBy() {
        LOG.warn("testInstanceOfToSortedSetBy");
        mutableSortedSet = recordListBuilder.toSortedSetBy(RLBTImpl.function);
        assertTrue(mutableSortedSet instanceof MutableSortedSet, "testInstanceOfToSortedSetBy");
    }

    @Test
    @Order(122)
    public void testNotNullToBag() {
        LOG.warn("testNotNullToBag");
        mutableBag = recordListBuilder.toBag();
        assertNotNull(mutableBag, "testNotNullToBag");
    }

    @Test
    @Order(122)
    public void testInstanceOfToBag() {
        LOG.warn("testInstanceOfToBag");
        mutableBag = recordListBuilder.toBag();
        assertTrue(mutableBag instanceof MutableBag, "testInstanceOfToBag");
    }

    @Test
    @Order(123)
    public void testNotNullToSortedBag() {
        LOG.warn("testNotNullToSortedBag");
        mutableSortedBag = recordListBuilder.toSortedBag();
        assertNotNull(mutableSortedBag, "testNotNullToSortedBag");
    }

    @Test
    @Order(123)
    public void testInstanceOfToSortedBag() {
        LOG.warn("testInstanceOfToSortedSet");
        mutableSortedBag = recordListBuilder.toSortedBag();
        assertTrue(mutableSortedBag instanceof MutableSortedBag, "testInstanceOfToSortedSet");
    }

    @Test
    @Order(124)
    public void testNotNullToSortedBagComparator() {
        LOG.warn("testNotNullToSortedBagComparator");
        mutableSortedBag = recordListBuilder.toSortedBag(RLBTImpl.comparator);
        assertNotNull(mutableSortedBag, "testNotNullToSortedBagComparator");
    }

    @Test
    @Order(124)
    public void testInstanceOfToSortedBagComparator() {
        LOG.warn("testInstanceOfToSortedBagComparator");
        mutableSortedBag = recordListBuilder.toSortedBag(RLBTImpl.comparator);
        assertTrue(mutableSortedBag instanceof MutableSortedBag, "testInstanceOfToSortedBagComparator");
    }

    @Test
    @Order(125)
    public void testNotNullToSortedBagBy() {
        LOG.warn("testNotNullToSortedBagBy");
        mutableSortedBag = recordListBuilder.toSortedBagBy(RLBTImpl.function);
        assertNotNull(mutableSortedBag, "testNotNullToSortedBagBy");
    }

    @Test
    @Order(125)
    public void testInstanceOfToSortedBagBy() {
        LOG.warn("testInstanceOfToSortedBagBy");
        mutableSortedBag = recordListBuilder.toSortedBagBy(RLBTImpl.function);
        assertTrue(mutableSortedBag instanceof MutableSortedBag, "testInstanceOfToSortedBagBy");
    }

    @Test
    @Order(126)
    public void testNotNullToMapMutable() {
        LOG.warn("testNotNullToMapMutable");
        mutableMap = recordListBuilder.toMap(RLBTImpl.function, RLBTImpl.function);
        assertNotNull(mutableMap, "testNotNullToMapMutable");
    }

    @Test
    @Order(126)
    public void testInstanceOfToMapMutable() {
        LOG.warn("testInstanceOfToMapMutable");
        mutableMap = recordListBuilder.toMap(RLBTImpl.function, RLBTImpl.function);
        assertTrue(mutableMap instanceof MutableMap, "testInstanceOfToMapMutable");
    }

    @Test
    @Order(127)
    public void testNotNullToMap() {
        LOG.warn("testNotNullToMap");
        map = recordListBuilder.toMap(RLBTImpl.function, RLBTImpl.function, RLBTImpl.map);
        assertNotNull(map, "testNotNullToMap");
    }

    @Test
    @Order(127)
    public void testInstanceOfToMap() {
        LOG.warn("testInstanceOfToMap");
        map = recordListBuilder.toMap(RLBTImpl.function, RLBTImpl.function, RLBTImpl.map);
        assertTrue(map instanceof Map, "testInstanceOfToMap");
    }

    @Test
    @Order(128)
    public void testNotNullToSortedMap() {
        LOG.warn("testNotNullToSortedMap");
        mutableSortedMap = recordListBuilder.toSortedMap(RLBTImpl.function, RLBTImpl.function);
        assertNotNull(mutableSortedMap, "testNotNullToSortedMap");
    }

    @Test
    @Order(128)
    public void testInstanceOfToSortedMap() {
        LOG.warn("testInstanceOfToSortedMap");
        mutableSortedMap = recordListBuilder.toSortedMap(RLBTImpl.function, RLBTImpl.function);
        assertTrue(mutableSortedMap instanceof MutableSortedMap, "testInstanceOfToSortedMap");
    }

    @Test
    @Order(129)
    public void testNotNullToSortedMapComparator() {
        LOG.warn("testNotNullToSortedMapComparator");
        mutableSortedMap = recordListBuilder.toSortedMap(RLBTImpl.comparator, RLBTImpl.function, RLBTImpl.function);
        assertNotNull(mutableSortedMap, "testNotNullToSortedMapComparator");
    }

    @Test
    @Order(129)
    public void testInstanceOfToSortedMapComparator() {
        LOG.warn("testInstanceOfToSortedMapComparator");
        mutableSortedMap = recordListBuilder.toSortedMap(RLBTImpl.comparator, RLBTImpl.function, RLBTImpl.function);
        assertTrue(mutableSortedMap instanceof MutableSortedMap, "testInstanceOfToSortedMapComparator");
    }

    @Test
    @Order(130)
    public void testNotNullToSortedMapBy() {
        LOG.warn("testNotNullToSortedMapBy");
        mutableSortedMap = recordListBuilder.toSortedMapBy(RLBTImpl.function, RLBTImpl.function, RLBTImpl.function);
        assertNotNull(mutableSortedMap, "testNotNullToSortedMapBy");
    }

    @Test
    @Order(130)
    public void testInstanceOfToSortedMapBy() {
        LOG.warn("testInstanceOfToSortedMapBy");
        mutableSortedMap = recordListBuilder.toSortedMapBy(RLBTImpl.function, RLBTImpl.function, RLBTImpl.function);
        assertTrue(mutableSortedMap instanceof MutableSortedMap, "testInstanceOfToSortedMapBy");
    }

//    @Test
//    @Order(131)
//    public void testNotNullToBiMap() {
//        LOG.warn("testNotNullToBiMap");
//        mutableBiMap = recordListBuilder.toBiMap(RLBTImpl.function, RLBTImpl.function);
//        assertNotNull(mutableBiMap, "testNotNullToBiMap");
//    }
//
//    @Test
//    @Order(131)
//    public void testInstanceOfToBiMap() {
//        LOG.warn("testInstanceOfToBiMap");
//        mutableBiMap = recordListBuilder.toBiMap(RLBTImpl.function, RLBTImpl.function);
//        assertTrue(mutableBiMap instanceof MutableBiMap, "testInstanceOfToBiMap");
//    }

    @Test
    @Order(132)
    public void testNotNullAsLazy() {
        LOG.warn("testNotNullAsLazy");
        lazyIterable = recordListBuilder.asLazy();
        assertNotNull(lazyIterable, "testNotNullAsLazy");
    }

    @Test
    @Order(132)
    public void testInstanceOfAsLazy() {
        LOG.warn("testInstanceOfAsLazy");
        lazyIterable = recordListBuilder.asLazy();
        assertTrue(lazyIterable instanceof LazyIterable, "testInstanceOfAsLazy");
    }

    @Test
    @Order(133)
    public void testNotNullToArray() {
        LOG.warn("testNotNullToArray");
        objectArray = recordListBuilder.toArray();
        assertNotNull(objectArray, "testNotNullToArray");
    }

    @Test
    @Order(133)
    public void testInstanceOfToArray() {
        LOG.warn("testInstanceOfToArray");
        objectArray = recordListBuilder.toArray();
        assertTrue(objectArray instanceof Object[], "testInstanceOfToArray");
    }

    @Test
    @Order(134)
    public void testNotNullToArray2() {
        LOG.warn("testNotNullToArray2");
        objectArray = recordListBuilder.toArray(new Object[] {1,2,3,4});
        assertNotNull(objectArray, "testNotNullToArray2");
    }

    @Test
    @Order(134)
    public void testInstanceOfToArray2() {
        LOG.warn("testInstanceOfToArray2");
        objectArray = recordListBuilder.toArray(new Object[] {1,2,3,4});
        assertTrue(objectArray instanceof Object[], "testInstanceOfToArray2");
    }

    @Test
    @Order(135)
    public void testNotNullMinComparator() {
        LOG.warn("testNotNullMinComparator");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.min(RLBTImpl.comparator));
        assertNotNull(immutableList, "testNotNullMinComparator");
    }

    @Test
    @Order(135)
    public void testInstanceOfMinComparator() {
        LOG.warn("testInstanceOfMinComparator");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.min(RLBTImpl.comparator));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfMinComparator");
    }

    @Test
    @Order(136)
    public void testNotNullMaxComparator() {
        LOG.warn("testNotNullMaxComparator");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.max(RLBTImpl.comparator));
        assertNotNull(immutableList, "testNotNullMaxComparator");
    }

    @Test
    @Order(136)
    public void testInstanceOfMaxComparator() {
        LOG.warn("testInstanceOfMaxComparator");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.max(RLBTImpl.comparator));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfMaxComparator");
    }

    @Test
    @Order(137)
    public void testNotNullMinOptionalComp() {
        LOG.warn("testNotNullMinOptionalComp");
        optional = recordListBuilder.minOptional(RLBTImpl.comparator);
        assertNotNull(optional, "testNotNullMinOptionalComp");
    }

    @Test
    @Order(137)
    public void testInstanceOfMinOptionalComp() {
        LOG.warn("testInstanceOfMinOptionalComp");
        optional = recordListBuilder.minOptional(RLBTImpl.comparator);
        assertTrue(optional instanceof Optional, "testInstanceOfMinOptionalComp");
    }

    @Test
    @Order(138)
    public void testNotNullMaxOptionalComp() {
        LOG.warn("testNotNullMaxOptionalComp");
        optional = recordListBuilder.maxOptional(RLBTImpl.comparator);
        assertNotNull(optional, "testNotNullMaxOptionalComp");
    }

    @Test
    @Order(138)
    public void testInstanceOfMaxOptionalComp() {
        LOG.warn("testInstanceOfMaxOptionalComp");
        optional = recordListBuilder.maxOptional(RLBTImpl.comparator);
        assertTrue(optional instanceof Optional, "testInstanceOfMaxOptionalComp");
    }

    @Test
    @Order(139)
    public void testNotNullMin() {
        LOG.warn("testNotNullMin");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.min());
        assertNotNull(immutableList, "testNotNullMin");
    }

    @Test
    @Order(139)
    public void testInstanceOfMin() {
        LOG.warn("testInstanceOfMin");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.min());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfMin");
    }

    @Test
    @Order(140)
    public void testNotNullMax() {
        LOG.warn("testNotNullMax");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.max());
        assertNotNull(immutableList, "testNotNullMax");
    }

    @Test
    @Order(140)
    public void testInstanceOfMax() {
        LOG.warn("testInstanceOfMax");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.max());
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfMax");
    }

    @Test
    @Order(141)
    public void testNotNullMinOptional() {
        LOG.warn("testNotNullMinOptional");
        optional = recordListBuilder.minOptional();
        assertNotNull(optional, "testNotNullMinOptional");
    }

    @Test
    @Order(141)
    public void testInstanceOfMinOptional() {
        LOG.warn("testInstanceOfMinOptional");
        optional = recordListBuilder.minOptional();
        assertTrue(optional instanceof Optional, "testInstanceOfMinOptional");
    }

    @Test
    @Order(142)
    public void testNotNullMaxOptional() {
        LOG.warn("testNotNullMaxOptional");
        optional = recordListBuilder.maxOptional();
        assertNotNull(optional, "testNotNullMaxOptional");
    }

    @Test
    @Order(142)
    public void testInstanceOfMaxOptional() {
        LOG.warn("testInstanceOfMaxOptional");
        optional = recordListBuilder.maxOptional();
        assertTrue(optional instanceof Optional, "testInstanceOfMaxOptional");
    }

    @Test
    @Order(143)
    public void testNotNullMinBy() {
        LOG.warn("testNotNullMinBy");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.minBy(RLBTImpl.function));
        assertNotNull(immutableList, "testNotNullMinBy");
    }

    @Test
    @Order(143)
    public void testInstanceOfMinBy() {
        LOG.warn("testInstanceOfMinBy");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.minBy(RLBTImpl.function));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfMinBy");
    }

    @Test
    @Order(144)
    public void testNotNullMaxBy() {
        LOG.warn("testNotNullMaxBy");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.maxBy(RLBTImpl.function));
        assertNotNull(immutableList, "testNotNullMaxBy");
    }

    @Test
    @Order(144)
    public void testInstanceOfMaxBy() {
        LOG.warn("testInstanceOfMaxBy");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.maxBy(RLBTImpl.function));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfMaxBy");
    }

    @Test
    @Order(145)
    public void testNotNullMinByOptional() {
        LOG.warn("testNotNullMinByOptional");
        optional = recordListBuilder.minByOptional(RLBTImpl.function);
        assertNotNull(optional, "testNotNullMinByOptional");
    }

    @Test
    @Order(145)
    public void testInstanceOfMinByOptional() {
        LOG.warn("testInstanceOfMinByOptional");
        optional = recordListBuilder.minByOptional(RLBTImpl.function);
        assertTrue(optional instanceof Optional, "testInstanceOfMinByOptional");
    }

    @Test
    @Order(146)
    public void testNotNullMaxByOptional() {
        LOG.warn("testNotNullMaxByOptional");
        optional = recordListBuilder.maxByOptional(RLBTImpl.function);
        assertNotNull(optional, "testNotNullMaxByOptional");
    }

    @Test
    @Order(146)
    public void testInstanceOfMaxByOptional() {
        LOG.warn("testInstanceOfMaxByOptional");
        optional = recordListBuilder.maxByOptional(RLBTImpl.function);
        assertTrue(optional instanceof Optional, "testInstanceOfMaxByOptional");
    }

    @Test
    @Order(147)
    public void testNotNullSumOfInt() {
        LOG.warn("testNotNullSumOfInt");
        assertNotNull(recordListBuilder.sumOfInt(RLBTImpl.intFunction), "testNotNullSumOfInt");
    }

    @Test
    @Order(147)
    public void testEqualsSumOfInt() {
        LOG.warn("testEqualsSumOfInt");
        assertEquals(0, recordListBuilder.sumOfInt(RLBTImpl.intFunction), "testEqualsSumOfInt");
    }

    @Test
    @Order(148)
    public void testNotNullSumOfFloat() {
        LOG.warn("testNotNullSumOfFloat");
        assertNotNull(recordListBuilder.sumOfFloat(RLBTImpl.floatFunction), "testNotNullSumOfFloat");
    }

    @Test
    @Order(148)
    public void testEqualsSumOfFloat() {
        LOG.warn("testEqualsSumOfFloat");
        assertEquals(0, recordListBuilder.sumOfFloat(RLBTImpl.floatFunction), "testEqualsSumOfFloat");
    }

    @Test
    @Order(149)
    public void testNotNullSumOfLong() {
        LOG.warn("testNotNullSumOfLong");
        assertNotNull(recordListBuilder.sumOfLong(RLBTImpl.longFunction), "testNotNullSumOfLong");
    }

    @Test
    @Order(149)
    public void testEqualsSumOfLong() {
        LOG.warn("testEqualsSumOfLong");
        assertEquals(0, recordListBuilder.sumOfLong(RLBTImpl.longFunction), "testEqualsSumOfLong");
    }

    @Test
    @Order(150)
    public void testNotNullSumOfDouble() {
        LOG.warn("testNotNullSumOfDouble");
        assertNotNull(recordListBuilder.sumOfDouble(RLBTImpl.doubleFunction), "testNotNullSumOfDouble");
    }

    @Test
    @Order(150)
    public void testEqualsSumOfDouble() {
        LOG.warn("testEqualsSumOfDouble");
        assertEquals(0, recordListBuilder.sumOfDouble(RLBTImpl.doubleFunction), "testEqualsSumOfDouble");
    }

    @Test
    @Order(151)
    public void testNotNullSummarizeInt() {
        LOG.warn("testNotNullSummarizeInt");
        intSummaryStatistics = recordListBuilder.summarizeInt(RLBTImpl.intFunction);
        assertNotNull(intSummaryStatistics, "testNotNullSummarizeInt");
    }

    @Test
    @Order(151)
    public void testInstanceOfSummarizeInt() {
        LOG.warn("testInstanceOfSummarizeInt");
        intSummaryStatistics = recordListBuilder.summarizeInt(RLBTImpl.intFunction);
        assertTrue(intSummaryStatistics instanceof IntSummaryStatistics, "testInstanceOfSummarizeInt");
    }

    @Test
    @Order(152)
    public void testNotNullSummarizeFloat() {
        LOG.warn("testNotNullSummarizeFloat");
        doubleSummaryStatistics = recordListBuilder.summarizeFloat(RLBTImpl.floatFunction);
        assertNotNull(doubleSummaryStatistics, "testNotNullSummarizeFloat");
    }

    @Test
    @Order(152)
    public void testInstanceOfSummarizeFloat() {
        LOG.warn("testInstanceOfSummarizeFloat");
        doubleSummaryStatistics = recordListBuilder.summarizeFloat(RLBTImpl.floatFunction);
        assertTrue(doubleSummaryStatistics instanceof DoubleSummaryStatistics, "testInstanceOfSummarizeFloat");
    }

    @Test
    @Order(153)
    public void testNotNullSummarizeLong() {
        LOG.warn("testNotNullSummarizeLong");
        longSummaryStatistics = recordListBuilder.summarizeLong(RLBTImpl.longFunction);
        assertNotNull(longSummaryStatistics, "testNotNullSummarizeLong");
    }

    @Test
    @Order(153)
    public void testInstanceOfSummarizeLong() {
        LOG.warn("testInstanceOfSummarizeLong");
        longSummaryStatistics = recordListBuilder.summarizeLong(RLBTImpl.longFunction);
        assertTrue(longSummaryStatistics instanceof LongSummaryStatistics, "testInstanceOfSummarizeLong");
    }

    @Test
    @Order(154)
    public void testNotNullSummarizeDouble() {
        LOG.warn("testNotNullSummarizeDouble");
        doubleSummaryStatistics = recordListBuilder.summarizeDouble(RLBTImpl.doubleFunction);
        assertNotNull(doubleSummaryStatistics, "testNotNullSummarizeDouble");
    }

    @Test
    @Order(154)
    public void testInstanceOfSummarizeDouble() {
        LOG.warn("testInstanceOfSummarizeDouble");
        doubleSummaryStatistics = recordListBuilder.summarizeDouble(RLBTImpl.doubleFunction);
        assertTrue(doubleSummaryStatistics instanceof DoubleSummaryStatistics, "testInstanceOfSummarizeDouble");
    }

//    @Test
//    @Order(155)
//    public void testNotNullReduceInPlace() {
//        LOG.warn("testNotNullReduceInPlace");
//        recordListBuilder.add("Test String");
//        immutableList = recordListBuilder.newWithout(recordListBuilder.reduceInPlace(RLBTImpl.collector));
//        assertNotNull(immutableList, "testNotNullReduceInPlace");
//    }
//
//    @Test
//    @Order(155)
//    public void testInstanceOfReduceInPlace() {
//        LOG.warn("testInstanceOfReduceInPlace");
//        recordListBuilder.add("Test String");
//        immutableList = recordListBuilder.newWithout(recordListBuilder.reduceInPlace(RLBTImpl.collector));
//        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfReduceInPlace");
//    }

    @Test
    @Order(156)
    public void testNotNullReduceInPlaceSupplier() {
        LOG.warn("testNotNullReduceInPlaceSupplier");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.reduceInPlace(RLBTImpl.supplier, RLBTImpl.biConsumer));
        assertNotNull(immutableList, "testNotNullReduceInPlaceSupplier");
    }

    @Test
    @Order(156)
    public void testInstanceOfReduceInPlaceSupplier() {
        LOG.warn("testInstanceOfReduceInPlaceSupplier");
        recordListBuilder.add("Test String");
        immutableList = recordListBuilder.newWithout(recordListBuilder.reduceInPlace(RLBTImpl.supplier, RLBTImpl.biConsumer));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfReduceInPlaceSupplier");
    }

    @Test
    @Order(157)
    public void testNotNullReduce() {
        LOG.warn("testNotNullReduce");
        optional = recordListBuilder.reduce(RLBTImpl.binaryOperator);
        assertNotNull(optional, "testNotNullReduce");
    }

    @Test
    @Order(157)
    public void testInstanceOfReduce() {
        LOG.warn("testInstanceOfReduce");
        optional = recordListBuilder.reduce(RLBTImpl.binaryOperator);
        assertTrue(optional instanceof Optional, "testInstanceOfReduce");
    }

    @Test
    @Order(158)
    public void testNotNullMakeString() {
        LOG.warn("testNotNullMakeString");
        assertNotNull(recordListBuilder.makeString(), "testNotNullMakeString");
    }

    @Test
    @Order(158)
    public void testInstanceOfMakeString() {
        LOG.warn("testInstanceOfMakeString");
        assertTrue(recordListBuilder.makeString() instanceof String, "testInstanceOfMakeString");
    }

    @Test
    @Order(159)
    public void testNotNullMakeString2() {
        LOG.warn("testNotNullMakeString2");
        assertNotNull(recordListBuilder.makeString(","), "testNotNullMakeString2");
    }

    @Test
    @Order(159)
    public void testInstanceOfMakeString2() {
        LOG.warn("testInstanceOfMakeString2");
        assertTrue(recordListBuilder.makeString(",") instanceof String, "testInstanceOfMakeString2");
    }

    @Test
    @Order(160)
    public void testNotNullMakeString3() {
        LOG.warn("testNotNullMakeString3");
        assertNotNull(recordListBuilder.makeString("start", ",", "end"), "testNotNullMakeString3");
    }

    @Test
    @Order(160)
    public void testInstanceOfMakeString3() {
        LOG.warn("testInstanceOfMakeString3");
        assertTrue(recordListBuilder.makeString("start", ",", "end") instanceof String, "testInstanceOfMakeString3");
    }

    @Test
    @Order(161)
    public void testNotNullAppendString() {
        LOG.warn("testNotNullAppendString");
        recordListBuilder.appendString(RLBTImpl.appendable);
        assertNotNull(recordListBuilder, "testNotNullAppendString");
    }

    @Test
    @Order(161)
    public void testInstanceOfAppendString() {
        LOG.warn("testInstanceOfAppendString");
        recordListBuilder.appendString(RLBTImpl.appendable);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfAppendString");
    }

    @Test
    @Order(162)
    public void testNotNullAppendString2() {
        LOG.warn("testNotNullAppendString2");
        recordListBuilder.appendString(RLBTImpl.appendable, ",");
        assertNotNull(recordListBuilder, "testNotNullAppendString2");
    }

    @Test
    @Order(162)
    public void testInstanceOfAppendString2() {
        LOG.warn("testInstanceOfAppendString2");
        recordListBuilder.appendString(RLBTImpl.appendable, ",");
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfAppendString2");
    }

    @Test
    @Order(163)
    public void testNotNullAppendString3() {
        LOG.warn("testNotNullAppendString3");
        recordListBuilder.appendString(RLBTImpl.appendable, "s", "s1", "s2");
        assertNotNull(recordListBuilder, "testNotNullAppendString3");
    }

    @Test
    @Order(163)
    public void testInstanceOfAppendString3() {
        LOG.warn("testInstanceOfAppendString3");
        recordListBuilder.appendString(RLBTImpl.appendable, "s", "s1", "s2");
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfAppendString3");
    }

    @Test
    @Order(164)
    public void testNotNullCountByMutable() {
        LOG.warn("testNotNullCountByMutable");
        mutableBagIterable = recordListBuilder.countBy(RLBTImpl.function, RLBTImpl.mutableBagIterable);
        assertNotNull(mutableBagIterable, "testNotNullCountByMutable");
    }

    @Test
    @Order(164)
    public void testInstanceOfCountByMutable() {
        LOG.warn("testInstanceOfCountByMutable");
        mutableBagIterable = recordListBuilder.countBy(RLBTImpl.function, RLBTImpl.mutableBagIterable);
        assertTrue(mutableBagIterable instanceof MutableBagIterable, "testInstanceOfCountByMutable");
    }

    @Test
    @Order(165)
    public void testNotNullCountByWithMutable() {
        LOG.warn("testNotNullCountByWithMutable");
        mutableBagIterable = recordListBuilder.countByWith(RLBTImpl.function2, new Object(), RLBTImpl.mutableBagIterable);
        assertNotNull(mutableBagIterable, "testNotNullCountByWithMutable");
    }

    @Test
    @Order(165)
    public void testInstanceOfCountByWithMutable() {
        LOG.warn("testInstanceOfCountByWithMutable");
        mutableBagIterable = recordListBuilder.countByWith(RLBTImpl.function2, new Object(), RLBTImpl.mutableBagIterable);
        assertTrue(mutableBagIterable instanceof MutableBagIterable, "testInstanceOfCountByWithMutable");
    }

    @Test
    @Order(166)
    public void testNotNullCountByEachMutable() {
        LOG.warn("testNotNullCountByEachMutable");
        mutableBagIterable = recordListBuilder.countByEach(RLBTImpl.function, RLBTImpl.mutableBagIterable);
        assertNotNull(mutableBagIterable, "testNotNullCountByEachMutable");
    }

    @Test
    @Order(166)
    public void testInstanceOfCountByEachMutable() {
        LOG.warn("testInstanceOfCountByEachMutable");
        mutableBagIterable = recordListBuilder.countByEach(RLBTImpl.function, RLBTImpl.mutableBagIterable);
        assertTrue(mutableBagIterable instanceof MutableBagIterable, "testInstanceOfCountByEachMutable");
    }

    @Test
    @Order(167)
    public void testNotNullGroupByMutable() {
        LOG.warn("testNotNullGroupByMutable");
        mutableMultiMap = recordListBuilder.groupBy(RLBTImpl.function, RLBTImpl.mutableMultiMap);
        assertNotNull(mutableMultiMap, "testNotNullGroupByMutable");
    }

    @Test
    @Order(167)
    public void testInstanceOfGroupByMutable() {
        LOG.warn("testInstanceOfGroupByMutable");
        mutableMultiMap = recordListBuilder.groupBy(RLBTImpl.function, RLBTImpl.mutableMultiMap);
        assertTrue(mutableMultiMap instanceof MutableMultimap, "testInstanceOfGroupByMutable");
    }

    @Test
    @Order(168)
    public void testNotNullGroupByEachMutable() {
        LOG.warn("testNotNullGroupByEachMutable");
        mutableMultiMap = recordListBuilder.groupByEach(RLBTImpl.function, RLBTImpl.mutableMultiMap);
        assertNotNull(mutableMultiMap, "testNotNullGroupByEachMutable");
    }

    @Test
    @Order(168)
    public void testInstanceOfGroupByEachMutable() {
        LOG.warn("testInstanceOfGroupByEachMutable");
        mutableMultiMap = recordListBuilder.groupByEach(RLBTImpl.function, RLBTImpl.mutableMultiMap);
        assertTrue(mutableMultiMap instanceof MutableMultimap, "testInstanceOfGroupByEachMutable");
    }

    @Test
    @Order(169)
    public void testNotNullGroupByUniqueKeyMutable() {
        LOG.warn("testNotNullGroupByUniqueKeyMutable");
        mutableMapIterable = recordListBuilder.groupByUniqueKey(RLBTImpl.function, RLBTImpl.mutableMapIterable);
        assertNotNull(mutableMapIterable, "testNotNullGroupByUniqueKeyMutable");
    }

    @Test
    @Order(169)
    public void testInstanceOfGroupByUniqueKeyMutable() {
        LOG.warn("testInstanceOfGroupByUniqueKeyMutable");
        mutableMapIterable = recordListBuilder.groupByUniqueKey(RLBTImpl.function, RLBTImpl.mutableMapIterable);
        assertTrue(mutableMapIterable instanceof MutableMapIterable, "testInstanceOfGroupByUniqueKeyMutable");
    }

    @Test
    @Order(170)
    public void testNotNullZipCollection() {
        LOG.warn("testNotNullZipCollection");
        immutableList = recordListBuilder.newWithout("Test String");
        collection = recordListBuilder.zip(Collections.singleton(immutableList.iterator()), RLBTImpl.collection);
        assertNotNull(collection, "testNotNullZipCollection");
    }

    @Test
    @Order(170)
    public void testInstanceOfZipCollection() {
        LOG.warn("testInstanceOfZipCollection");
        immutableList = recordListBuilder.newWithout("Test String");
        collection = recordListBuilder.zip(Collections.singleton(immutableList.iterator()), RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfZipCollection");
    }

    @Test
    @Order(171)
    public void testNotNullZipWithIndexCollection() {
        LOG.warn("testNotNullZipWithIndexCollection");
        collection = recordListBuilder.zipWithIndex(RLBTImpl.collection);
        assertNotNull(collection, "testNotNullZipWithIndexCollection");
    }

    @Test
    @Order(171)
    public void testInstanceOfZipWithIndexCollection() {
        LOG.warn("testInstanceOfZipWithIndexCollection");
        collection = recordListBuilder.zipWithIndex(RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfZipWithIndexCollection");
    }

    @Test
    @Order(172)
    public void testNotNullChunk() {
        LOG.warn("testNotNullChunk");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        richIterable = recordListBuilder.newWithout(recordListBuilder.chunk(1));
        assertNotNull(richIterable, "testNotNullChunk");
    }

    @Test
    @Order(172)
    public void testInstanceOfChunk() {
        LOG.warn("testInstanceOfChunk");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        richIterable = recordListBuilder.newWithout(recordListBuilder.chunk(1));
        assertTrue(richIterable instanceof RichIterable, "testInstanceOfChunk");
    }

    @Test
    @Order(173)
    public void testNotNullAggregateByMutable() {
        LOG.warn("testNotNullAggregateByMutable");
        mutableMapIterable = recordListBuilder.aggregateBy(RLBTImpl.function, RLBTImpl.function0, RLBTImpl.function2, RLBTImpl.mutableMapIterable);
        assertNotNull(mutableMapIterable, "testNotNullAggregateByMutable");
    }

    @Test
    @Order(173)
    public void testInstanceOfAggregateByMutable() {
        LOG.warn("testInstanceOfAggregateByMutable");
        mutableMapIterable = recordListBuilder.aggregateBy(RLBTImpl.function, RLBTImpl.function0, RLBTImpl.function2, RLBTImpl.mutableMapIterable);
        assertTrue(mutableMapIterable instanceof MutableMapIterable, "testInstanceOfAggregateByMutable");
    }

    @Test
    @Order(174)
    public void testNotNullGroupByAndCollect() {
        LOG.warn("testNotNullGroupByAndCollect");
        mutableMultiMap = recordListBuilder.groupByAndCollect(RLBTImpl.function, RLBTImpl.function, RLBTImpl.mutableMultiMap);
        assertNotNull(mutableMultiMap, "testNotNullGroupByAndCollect");
    }

    @Test
    @Order(174)
    public void testInstanceOfGroupByAndCollect() {
        LOG.warn("testInstanceOfGroupByAndCollect");
        mutableMultiMap = recordListBuilder.groupByAndCollect(RLBTImpl.function, RLBTImpl.function, RLBTImpl.mutableMultiMap);
        assertTrue(mutableMultiMap instanceof MutableMultimap, "testInstanceOfGroupByAndCollect");
    }

    @Test
    @Order(175)
    public void testNotNullForEachConsumer() {
        LOG.warn("testNotNullForEachConsumer");
        recordListBuilder.forEach(RLBTImpl.consumer);
        assertNotNull(recordListBuilder, "testNotNullForEachConsumer");
    }

    @Test
    @Order(175)
    public void testInstanceOfForEachConsumer() {
        LOG.warn("testInstanceOfForEachConsumer");
        recordListBuilder.forEach(RLBTImpl.consumer);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfForEachConsumer");
    }

    @Test
    @Order(176)
    public void testNotNullForEachWithIndex() {
        LOG.warn("testNotNullForEachWithIndex");
        recordListBuilder.forEachWithIndex(RLBTImpl.objectIntProcedure);
        assertNotNull(recordListBuilder, "testNotNullForEachWithIndex");
    }

    @Test
    @Order(176)
    public void testInstanceOfForEachWithIndex() {
        LOG.warn("testInstanceOfForEachWithIndex");
        recordListBuilder.forEachWithIndex(RLBTImpl.objectIntProcedure);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfForEachWithIndex");
    }

    @Test
    @Order(177)
    public void testNotNullForEachWith() {
        LOG.warn("testNotNullForEachWith");
        recordListBuilder.forEachWith(RLBTImpl.procedure2, "Test String");
        assertNotNull(recordListBuilder, "testNotNullForEachWith");
    }

    @Test
    @Order(177)
    public void testInstanceOfForEachWith() {
        LOG.warn("testInstanceOfForEachWith");
        recordListBuilder.forEachWith(RLBTImpl.procedure2, "Test String");
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfForEachWith");
    }

    @Test
    @Order(178)
    public void testNotNullIterator() {
        LOG.warn("testNotNullIterator");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        iterator = recordListBuilder.iterator();
        assertNotNull(iterator, "testNotNullIterator");
    }

    @Test
    @Order(178)
    public void testInstanceOfIterator() {
        LOG.warn("testInstanceOfIterator");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        iterator = recordListBuilder.iterator();
        assertTrue(iterator instanceof Iterator, "testInstanceOfIterator");
    }

    @Test
    @Order(179)
    public void testNotNullGet() {
        LOG.warn("testNotNullGet");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        immutableList = recordListBuilder.newWithout(recordListBuilder.get(1));
        assertNotNull(immutableList, "testNotNullGet");
    }

    @Test
    @Order(179)
    public void testInstanceOfGet() {
        LOG.warn("testInstanceOfGet");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        immutableList = recordListBuilder.newWithout(recordListBuilder.get(1));
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfGet");
    }

    @Test
    @Order(180)
    public void testNotNullLastIndexOf() {
        LOG.warn("testNotNullLastIndexOf");
        immutableList = recordListBuilder.newWithout("Test String");
        assertNotNull(recordListBuilder.lastIndexOf(immutableList), "testNotNullLastIndexOf");
    }

    @Test
    @Order(180)
    public void testNotEqualsLastIndexOf() {
        LOG.warn("testEqualsLastIndexOf");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        immutableList = recordListBuilder.toImmutable();
        assertNotEquals(2, recordListBuilder.lastIndexOf(immutableList), "testEqualsLastIndexOf");
    }

    @Test
    @Order(181)
    public void testNotNullListIterator() {
        LOG.warn("testNotNullListIterator");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        listIterator = recordListBuilder.listIterator();
        assertNotNull(listIterator, "testNotNullListIterator");
    }

    @Test
    @Order(181)
    public void testInstanceOfListIterator() {
        LOG.warn("testInstanceOfListIterator");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        listIterator = recordListBuilder.listIterator();
        assertTrue(listIterator instanceof ListIterator, "testInstanceOfListIterator");
    }

    @Test
    @Order(182)
    public void testNotNullListIteratorInt() {
        LOG.warn("testNotNullListIteratorInt");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        listIterator = recordListBuilder.listIterator(1);
        assertNotNull(listIterator, "testNotNullListIteratorInt");
    }

    @Test
    @Order(182)
    public void testInstanceOfListIteratorInt() {
        LOG.warn("testInstanceOfListIteratorInt");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        listIterator = recordListBuilder.listIterator(1);
        assertTrue(listIterator instanceof ListIterator, "testInstanceOfListIteratorInt");
    }

    @Test
    @Order(183)
    public void testNotNullToImmutable() {
        LOG.warn("testNotNullToImmutable");
        immutableList = recordListBuilder.toImmutable();
        assertNotNull(immutableList, "testNotNullToImmutable");
    }

    @Test
    @Order(183)
    public void testInstanceOfToImmutable() {
        LOG.warn("testInstanceOfToImmutable");
        immutableList = recordListBuilder.toReversed();
        assertTrue(immutableList instanceof ImmutableList, "testInstanceOfToImmutable");
    }

    @Test
    @Order(184)
    public void testNotNullAsParallel() {
        LOG.warn("testNotNullAsParallel");
        parallelListIterable = recordListBuilder.asParallel(RLBTImpl.executorService, 2);
        assertNotNull(parallelListIterable, "testNotNullAsParallel");
    }

    @Test
    @Order(184)
    public void testInstanceOfAsParallel() {
        LOG.warn("testInstanceOfAsParallel");
        parallelListIterable = recordListBuilder.asParallel(RLBTImpl.executorService, 2);
        assertTrue(parallelListIterable instanceof ParallelListIterable, "testInstanceOfAsParallel");
    }

    @Test
    @Order(185)
    public void testNotNullBinarySearch2() {
        LOG.warn("testNotNullBinarySearch2");
        assertNotNull(recordListBuilder.binarySearch("Test String", RLBTImpl.comparator), "testNotNullBinarySearch2");
    }

    @Test
    @Order(185)
    public void testNotEqualsBinarySearch2() {
        LOG.warn("testNotEqualsBinarySearch2");
        assertNotEquals(2, recordListBuilder.binarySearch("Test String", RLBTImpl.comparator), "testNotEqualsBinarySearch2");
    }

    @Test
    @Order(186)
    public void testNotNullBinarySearch() {
        LOG.warn("testNotNullBinarySearch");
        assertNotNull(recordListBuilder.binarySearch("Test String"), "testNotNullBinarySearch");
    }

    @Test
    @Order(186)
    public void testNotEqualsBinarySearch() {
        LOG.warn("testEqualsBinarySearch");
        assertNotEquals(2, recordListBuilder.binarySearch("Test String"), "testEqualsBinarySearch");
    }

    @Test
    @Order(187)
    public void testNotNullForEachInBoth() {
        LOG.warn("testNotNullForEachInBoth");
        recordListBuilder.forEachInBoth(RLBTImpl.listIterable, RLBTImpl.procedure2);
        assertNotNull(recordListBuilder, "testNotNullForEachInBoth");
    }

    @Test
    @Order(187)
    public void testInstanceOfForEachInBoth() {
        LOG.warn("testInstanceOfForEachInBoth");
        recordListBuilder.forEachInBoth(RLBTImpl.listIterable, RLBTImpl.procedure2);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfForEachInBoth");
    }

    @Test
    @Order(188)
    public void testNotNullHashCode() {
        LOG.warn("testNotNullHashCode");
        assertNotNull(recordListBuilder.hashCode(), "testNotNullHashCode");
    }

    @Test
    @Order(188)
    public void testNotEqualsHashCode() {
        LOG.warn("testNotEqualsHashCode");
        assertNotEquals(0, hashCode(), "testNotEqualsHashCode");
    }

    @Test
    @Order(189)
    public void testNotNullEquals() {
        LOG.warn("testNotNullEquals");
        assertNotNull(recordListBuilder.equals("Test String"), "testNotNullEquals");
    }

    @Test
    @Order(189)
    public void testFalseEquals() {
        LOG.warn("testFalseEquals");
        recordListBuilder.add("Test String");
        assertFalse(recordListBuilder.equals("Test String"), "testFalseEquals");
    }

    @Test
    @Order(190)
    public void testNotNullToString() {
        LOG.warn("testNotNullToString");
        assertNotNull(recordListBuilder.toString(), "testNotNullToString");
    }

    @Test
    @Order(190)
    public void testInstanceOfToString() {
        LOG.warn("testInstanceOfToString");
        assertTrue(recordListBuilder.toString() instanceof String, "testInstanceOfToString");
    }

    @Test
    @Order(191)
    public void testNotNullReverseForEach() {
        LOG.warn("testNotNullReverseForEach");
        recordListBuilder.reverseForEach(RLBTImpl.procedure);
        assertNotNull(recordListBuilder, "testNotNullReverseForEach");
    }

    @Test
    @Order(191)
    public void testInstanceOfReverseForEach() {
        LOG.warn("testInstanceOfReverseForEach");
        recordListBuilder.reverseForEach(RLBTImpl.procedure);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfReverseForEach");
    }

    @Test
    @Order(192)
    public void testNotNullReverseForEachWithIndex() {
        LOG.warn("testNotNullReverseForEachWithIndex");
        recordListBuilder.reverseForEachWithIndex(RLBTImpl.objectIntProcedure);
        assertNotNull(recordListBuilder, "testNotNullReverseForEachWithIndex");
    }

    @Test
    @Order(192)
    public void testInstanceOfReverseForEachWithIndex() {
        LOG.warn("testInstanceOfReverseForEachWithIndex");
        recordListBuilder.reverseForEachWithIndex(RLBTImpl.objectIntProcedure);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfReverseForEachWithIndex");
    }

    @Test
    @Order(193)
    public void testNotNullAsReversed() {
        LOG.warn("testNotNullAsReversed");
        lazyIterable = recordListBuilder.asReversed();
        assertNotNull(lazyIterable, "testNotNullAsReversed");
    }

    @Test
    @Order(193)
    public void testInstanceOfAsReversed() {
        LOG.warn("testInstanceOfAsReversed");
        lazyIterable = recordListBuilder.asReversed();
        assertTrue(lazyIterable instanceof LazyIterable, "testInstanceOfAsReversed");
    }

    @Test
    @Order(194)
    public void testNotNullDetectLastIndex() {
        LOG.warn("testNotNullDetectLastIndex");
        assertNotNull(recordListBuilder.detectLastIndex(RLBTImpl.predicate), "testNotNullDetectLastIndex");
    }

    @Test
    @Order(194)
    public void testNotEqualsDetectLastIndex() {
        LOG.warn("testNotEqualsDetectLastIndex");
        assertNotEquals(2, recordListBuilder.detectLastIndex(RLBTImpl.predicate), "testNotEqualsDetectLastIndex");
    }

    @Test
    @Order(195)
    public void testNotNullIndexOf() {
        LOG.warn("testNotNullIndexOf");
        assertNotNull(recordListBuilder.indexOf("Test String"), "testNotNullIndexOf");
    }

    @Test
    @Order(195)
    public void testNotEqualsIndexOf() {
        LOG.warn("testNotEqualsIndexOf");
        assertNotEquals(2, recordListBuilder.indexOf("Test String"), "testNotEqualsIndexOf");
    }

    @Test
    @Order(196)
    public void testNotNullGetFirstOptional() {
        LOG.warn("testNotNullGetFirstOptional");
        optional = recordListBuilder.getFirstOptional();
        assertNotNull(optional, "testNotNullGetFirstOptional");
    }

    @Test
    @Order(196)
    public void testInstanceOfGetFirstOptional() {
        LOG.warn("testInstanceOfGetFirstOptional");
        optional = recordListBuilder.getFirstOptional();
        assertTrue(optional instanceof Optional, "testInstanceOfGetFirstOptional");
    }

    @Test
    @Order(197)
    public void testNotNullGetLastOptional() {
        LOG.warn("testNotNullGetLastOptional");
        optional = recordListBuilder.getLastOptional();
        assertNotNull(optional, "testNotNullGetLastOptional");
    }

    @Test
    @Order(197)
    public void testInstanceOfGetLastOptional() {
        LOG.warn("testInstanceOfGetLastOptional");
        optional = recordListBuilder.getLastOptional();
        assertTrue(optional instanceof Optional, "testInstanceOfGetLastOptional");
    }

    @Test
    @Order(198)
    public void testNotNullCorresponds() {
        LOG.warn("testNotNullCorresponds");
        assertNotNull(recordListBuilder.corresponds(RLBTImpl.orderedIterable, RLBTImpl.predicate2), "testNotNullCorresponds");
    }

    @Test
    @Order(198)
    public void testTrueCarresponds() {
        LOG.warn("testTrueCarresponds");
        assertTrue(recordListBuilder.corresponds(RLBTImpl.orderedIterable, RLBTImpl.predicate2), "testTrueCarresponds");
    }

    @Test
    @Order(199)
    public void testNotNullForEach2() {
        LOG.warn("testNotNullForEach2");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        recordListBuilder.forEach(0, 1, RLBTImpl.procedure);
        assertNotNull(recordListBuilder, "testNotNullForEach2");
    }

    @Test
    @Order(199)
    public void testInstanceOfForEach2() {
        LOG.warn("testInstanceOfForEach2");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        recordListBuilder.forEach(0, 1, RLBTImpl.procedure);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfForEach2");
    }

    @Test
    @Order(200)
    public void testNotNullForEachWithIndex2() {
        LOG.warn("testNotNullForEachWithIndex2");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        recordListBuilder.forEachWithIndex(0, 1, RLBTImpl.objectIntProcedure);
        assertNotNull(recordListBuilder, "testNotNullForEachWithIndex2");
    }

    @Test
    @Order(200)
    public void testInstanceOfForEachWithIndex2() {
        LOG.warn("testInstanceOfForEachWithIndex2");
        recordListBuilder.add("Test String");
        recordListBuilder.add("Test String 2");
        recordListBuilder.forEachWithIndex(0, 1, RLBTImpl.objectIntProcedure);
        assertTrue(recordListBuilder instanceof RecordListBuilder, "testInstanceOfForEachWithIndex2");
    }

//    @Test
//    @Order(201)
//    public void testNotNullToStack() {
//        LOG.warn("testNotNullToStack");
//        mutableStack = recordListBuilder.toStack();
//        assertNotNull(mutableStack, "testNotNullToStack");
//    }
//
//    @Test
//    @Order(201)
//    public void testInstanceOfToStack() {
//        LOG.warn("testInstanceOfToStack");
//        mutableStack = recordListBuilder.toStack();
//        assertTrue(mutableStack instanceof MutableStack, "testInstanceOfToStack");
//    }

    @Test
    @Order(202)
    public void testNotNullCollectWithIndexCollection() {
        LOG.warn("testNotNullCollectWithIndexCollection");
        collection = recordListBuilder.collectWithIndex(RLBTImpl.objectIntToObjectFunction, RLBTImpl.collection);
        assertNotNull(collection, "testNotNullCollectWithIndexCollection");
    }

    @Test
    @Order(202)
    public void testInstanceOfCollectWithIndexCollection() {
        LOG.warn("testInstanceOfCollectWithIndexCollection");
        collection = recordListBuilder.collectWithIndex(RLBTImpl.objectIntToObjectFunction, RLBTImpl.collection);
        assertTrue(collection instanceof Collection, "testInstanceOfCollectWithIndexCollection");
    }

    @Test
    @Order(203)
    public void testNotNullDetectIndex() {
        LOG.warn("testNotNullDetectIndex");
        assertNotNull(recordListBuilder.detectIndex(RLBTImpl.predicate), "testNotNullDetectIndex");
    }

    @Test
    @Order(203)
    public void testNotEqualsDetectIndex() {
        LOG.warn("testNotEqualsDetectIndex");
        assertNotEquals(2, recordListBuilder.detectIndex(RLBTImpl.predicate), "testNotEqualsDetectIndex");
    }

}
