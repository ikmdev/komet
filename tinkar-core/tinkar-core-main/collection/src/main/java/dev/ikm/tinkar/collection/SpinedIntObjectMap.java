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
package dev.ikm.tinkar.collection;

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.ArrayUtil;
import dev.ikm.tinkar.common.util.time.Stopwatch;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <E> the generic type for the spined list.
 * 
 */
public class SpinedIntObjectMap<E> implements IntObjectMap<E> {

    private static final Logger LOG = LoggerFactory.getLogger(SpinedIntObjectMap.class);
    public static final int DEFAULT_SPINE_SIZE = 10240;
    public static final int DEFAULT_MAX_SPINE_COUNT = 10240;
    protected final Semaphore fileSemaphore = new Semaphore(1);
    protected final int maxSpineCount;
    protected final int spineSize;
    private final Semaphore newSpineSemaphore = new Semaphore(1);
    // TODO: consider growth strategies instead of just a large array expected to be big enough to hold all the spines...
    private final AtomicReferenceArray<AtomicReferenceArray<E>> spines;
    private final AtomicInteger spineCount = new AtomicInteger();
    private final boolean[] changedSpineIndexes;
    private final boolean ephemoral;
    private Function<E, String> elementStringConverter;

    public SpinedIntObjectMap() {
        this.ephemoral = true;
        this.maxSpineCount = DEFAULT_MAX_SPINE_COUNT;
        this.spineSize = DEFAULT_SPINE_SIZE;
        this.spines = new AtomicReferenceArray(this.maxSpineCount);
        this.changedSpineIndexes = new boolean[this.maxSpineCount * this.spineSize];
        this.spineCount.set(0);
    }

    public SpinedIntObjectMap(int spineCount) {
        this.ephemoral = false;
        this.maxSpineCount = DEFAULT_MAX_SPINE_COUNT;
        this.spineSize = DEFAULT_SPINE_SIZE;
        this.spines = new AtomicReferenceArray(this.maxSpineCount);
        this.changedSpineIndexes = new boolean[this.maxSpineCount * this.spineSize];
        this.spineCount.set(spineCount);
    }

    public void close() {
        // nothing to do...
    }

    public void setElementStringConverter(Function<E, String> elementStringConverter) {
        this.elementStringConverter = elementStringConverter;
    }

    public void forEachSpine(ObjIntConsumer<AtomicReferenceArray<E>> consumer) {
        int spineCountNow = spineCount.get();
        for (int spineIndex = 0; spineIndex < spineCountNow; spineIndex++) {
            consumer.accept(getSpine(spineIndex), spineIndex);
        }
    }

    private AtomicReferenceArray<E> getSpine(int spineIndex) {
        int startSpineCount = spineCount.get();
        if (spineIndex < startSpineCount) {
            AtomicReferenceArray<E> spine = this.spines.get(spineIndex);
            if (spine == null) {
                try {
                    newSpineSemaphore.acquireUninterruptibly();
                    spine = this.spines.get(spineIndex);
                    if (spine == null) {
                        spine = readSpine(spineIndex);
                        this.spines.compareAndSet(spineIndex, null, spine);
                    }
                } finally {
                    newSpineSemaphore.release();
                }
            }
            if (spine == null) {
                AlertStreams.dispatchToRoot(new IllegalStateException("(1) getSpine is returning null for index:" +
                        spineIndex + "..."));
            }
            return spine;
        }
        try {
            newSpineSemaphore.acquireUninterruptibly();
            if (spineIndex < spineCount.get()) {
                AtomicReferenceArray<E> spine = this.spines.updateAndGet(spineIndex, eAtomicReferenceArray -> {
                    if (eAtomicReferenceArray == null) {
                        eAtomicReferenceArray = readSpine(spineIndex);
                        spineCount.compareAndSet(startSpineCount, startSpineCount + 1);
                    }
                    return eAtomicReferenceArray;
                });
                if (spine == null) {
                    AlertStreams.dispatchToRoot(new IllegalStateException("(2) getSpine is returning null for index:" +
                            spineIndex + "..."));
                }
                return spine;
            }
            AtomicReferenceArray<E> spine = this.spines.updateAndGet(spineIndex, eAtomicReferenceArray -> {
                if (eAtomicReferenceArray == null) {
                    eAtomicReferenceArray = newSpine(spineIndex);
                    spineCount.compareAndSet(startSpineCount, startSpineCount + 1);
                }
                return eAtomicReferenceArray;
            });
            if (spine == null) {
                AlertStreams.dispatchToRoot(new IllegalStateException("(3) getSpine is returning null for index:" +
                        spineIndex + "..."));
            }
            return spine;
        } finally {
            newSpineSemaphore.release();
        }
    }

    protected AtomicReferenceArray<E> readSpine(int spineIndex) {
        if (ephemoral) {
            return newSpine(spineIndex);
        }
        throw new IllegalStateException("Subclass must implement readSpine");
    }

    private AtomicReferenceArray<E> newSpine(int spineKey) {
        return makeNewSpine(spineKey);
    }

    public AtomicReferenceArray<E> makeNewSpine(int spineKey) {
        AtomicReferenceArray<E> spine = new AtomicReferenceArray<>(spineSize);
        this.spineCount.set(Math.max(this.spineCount.get(), spineKey + 1));
        return spine;
    }

    public boolean forEachChangedSpine(ObjIntConsumer<AtomicReferenceArray<E>> consumer) {
        boolean foundChange = false;
        int spineCountNow = spineCount.get();
        for (int spineIndex = 0; spineIndex < spineCountNow; spineIndex++) {
            if (changedSpineIndexes[spineIndex]) {
                foundChange = true;
                consumer.accept(getSpine(spineIndex), spineIndex);
                changedSpineIndexes[spineIndex] = false;
            }
        }
        return foundChange;
    }

    public int getSpineCount() {
        return spineCount.get();
    }

    public void printToConsole() {
        if (elementStringConverter != null) {
            forEach((E value, int key) -> {
                LOG.info(key + ": " + elementStringConverter.apply(value));
            });
        } else {
            forEach((E value, int key) -> {
                LOG.info(key + ": " + value);
            });
        }
    }

    private int forEachOnSpine(ObjIntConsumer<E> consumer, int spineIndex) {
        AtomicReferenceArray<E> spine = getSpine(spineIndex);
        int index = spineIndex * spineSize;
        int processed = 0;
        for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
            E element = spine.get(indexInSpine);
            if (element != null) {
                int nid = PrimitiveDataService.FIRST_NID + index;
                consumer.accept(element, nid);
                processed++;
            }
            index++;
        }
        //if (processed < spineSize) {
        // TODO where do the null values come from?
        //LOG.info(spineSize - processed + " null values on spine: " + spineIndex);
        //}
        return processed;
    }

    public final boolean compareAndSet(int index, E expectedValue, E newValue) {
        int spineIndex = toSpineIndex(index);
        this.changedSpineIndexes[spineIndex] = true;
        return getSpine(spineIndex).compareAndSet(toIndexInSpine(index), expectedValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean put(int index, E element) {
        int spineIndex = toSpineIndex(index);
        this.changedSpineIndexes[spineIndex] = true;
        return getSpine(spineIndex).getAndSet(toIndexInSpine(index), element) == null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final E getAndSet(int index, E element) {
        int spineIndex = toSpineIndex(index);
        this.changedSpineIndexes[spineIndex] = true;
        return getSpine(spineIndex).getAndSet(toIndexInSpine(index), element);
    }
    private final int toSpineIndex(int index) {
        if (index == 0) {
            throw new IllegalStateException("Index cannot be 0...");
        }
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        return index / spineSize;
    }

    private final int toIndexInSpine(int index) {
        if (index == 0) {
            throw new IllegalStateException("Index cannot be 0...");
        }
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        return index % spineSize;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public final E get(int index) {
        return getSpine(toSpineIndex(index)).get(toIndexInSpine(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Optional<E> getOptional(int index) {
        return Optional.ofNullable(get(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean containsKey(int index) {
        return get(index) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int size() {
        int size = 0;
        int currentSpineCount = this.spineCount.get();
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            AtomicReferenceArray<E> spine = getSpine(spineIndex);
            for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
                E element = spine.get(indexInSpine);
                if (element != null) {
                    size++;
                }
            }
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        for (int i = 0; i < spines.length(); i++) {
            spines.set(i, null);
        }
    }

    public final void forEach(ObjIntConsumer<E> consumer) {
        int currentSpineCount = this.spineCount.get();
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            forEachOnSpine(consumer, spineIndex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final E accumulateAndGet(int index, E x, BinaryOperator<E> accumulatorFunction) {
        int spineIndex = toSpineIndex(index);
        this.changedSpineIndexes[spineIndex] = true;
        return getSpine(toSpineIndex(index))
                .accumulateAndGet(toIndexInSpine(index), x, accumulatorFunction);

    }

    public final void forEachParallel(ObjIntConsumer<E> consumer) throws ExecutionException, InterruptedException {
        int currentSpineCount = this.spineCount.get();
        ArrayList<Future<?>> futures = new ArrayList<>(currentSpineCount);
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            final int indexToProcess = spineIndex;
            Future<?> future = TinkExecutor.threadPool().submit(() -> forEachOnSpine(consumer, indexToProcess));
            futures.add(future);
        }
        for (Future<?> future : futures) {
            Object obj = future.get();
        }
    }

    public final void forEachParallel(ImmutableIntList nids, ObjIntConsumer<E> consumer) throws ExecutionException, InterruptedException {
        Stopwatch sw = new Stopwatch();
        int[][] nidLists = splitIntoArrayOfArrays(nids);
        sw.stop();
        LOG.info("Split and sort nid list time: " + sw.durationString());
        sw = new Stopwatch();
        doParallelWork(consumer, nidLists);
        LOG.info("doParallelWork2 time: " + sw.durationString());
    }

    private int[][] splitIntoArrayOfArrays(ImmutableIntList nids) {
        int[] nidsArray = nids
                .toSet().toArray();
        Arrays.parallelSort(nidsArray);
        final int setSize = 10240;
        final int setCount = (nids.size() / setSize) + 1;
        int[][] nidLists = new int[setCount][];
        int nidsSize = nids.size();
        int nidIndex = 0;
        for (int setIndex = 0; setIndex < setCount; setIndex++) {
            nidLists[setIndex] = ArrayUtil.createAndFill(setSize, Integer.MIN_VALUE);
            for (int indexInSet = 0; indexInSet < setSize; indexInSet++) {
                if (nidIndex < nidsSize) {
                    nidLists[setIndex][indexInSet] = nidsArray[nidIndex];
                    nidIndex++;
                }
            }
        }
        return nidLists;
    }

    private void doParallelWork(ObjIntConsumer<E> consumer, int[][] nidLists) {
        final int permitCount = TinkExecutor.defaultParallelBatchSize();
        Semaphore permits = new Semaphore(permitCount);
        for (int[] nidList : nidLists) {
            try {
                permits.acquire();
                TinkExecutor.threadPool().execute(() -> {
                    try {

                        int nidListIndex = 0;
                        int nid = nidList[nidListIndex];
                        while (nid != Integer.MIN_VALUE) {
                            int spineIndex = (nid + Integer.MAX_VALUE) / spineSize;
                            AtomicReferenceArray<E> spine = getSpine(spineIndex);
                            while (nid != Integer.MIN_VALUE &&
                                    (nid + Integer.MAX_VALUE) / spineSize == spineIndex) {
                                int indexInSpine = (nid + Integer.MAX_VALUE) % spineSize;
                                consumer.accept(spine.get(indexInSpine), nid);
                                nidListIndex++;
                                if (nidListIndex < nidList.length) {
                                    nid = nidList[nidListIndex];
                                } else {
                                    nid = Integer.MIN_VALUE;
                                }
                            }
                        }
                    } finally {
                        permits.release();
                    }
                });
            } catch (Throwable ex) {
                AlertStreams.dispatchToRoot(ex);
            }
        }

        try {
            permits.acquire(permitCount);
        } catch (InterruptedException e) {
            AlertStreams.dispatchToRoot(e);
        }
    }

    public final Stream<E> stream() {
        final Supplier<? extends Spliterator<E>> streamSupplier = this.get();

        return StreamSupport.stream(streamSupplier, streamSupplier.get()
                .characteristics(), false);
    }

    /**
     * Gets the value spliterator supplier.
     *
     * @return the {@code Supplier<? extends Spliterator<E>>}
     */
    protected Supplier<? extends Spliterator<E>> get() {
        return new SpliteratorSupplier();
    }

    public boolean containsSpine(int spineIndex) {
        return this.spines.get(spineIndex) != null;
    }

    /**
     * The Class SpliteratorSupplier.
     */
    private class SpliteratorSupplier
            implements Supplier<Spliterator<E>> {

        /**
         * Gets the.
         *
         * @return the spliterator
         */
        @Override
        public Spliterator<E> get() {
            return new SpinedValueSpliterator();
        }
    }

    private class SpinedValueSpliterator implements Spliterator<E> {

        int end;
        int currentPosition;

        public SpinedValueSpliterator() {
            this.end = spineSize * spineCount.get();
            this.currentPosition = 0;
        }

        public SpinedValueSpliterator(int start, int end) {
            this.currentPosition = start;
            this.end = end;
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            while (currentPosition < end) {
                E value = get(currentPosition++);
                if (value != null) {
                    action.accept(value);
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<E> trySplit() {
            int splitEnd = end;
            int split = end - currentPosition;
            int half = split / 2;
            this.end = currentPosition + half;
            return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
        }

        @Override
        public long estimateSize() {
            return end - currentPosition;
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                    | Spliterator.SIZED;
        }

    }
}
