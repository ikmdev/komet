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

import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class SpinedIntIntMap {

    protected static final int DEFAULT_SPINE_SIZE = 10240;
    ;
    private static final Logger LOG = LoggerFactory.getLogger(SpinedIntIntMap.class);
    protected final int spineSize;
    protected final ConcurrentMap<Integer, AtomicIntegerArray> spines = new ConcurrentHashMap<>();
    protected final int INITIALIZATION_VALUE = Integer.MAX_VALUE;
    protected final AtomicInteger spineCount = new AtomicInteger();
    protected final ConcurrentSkipListSet<Integer> changedSpineIndexes = new ConcurrentSkipListSet<>();
    protected final AtomicInteger nextIndex = new AtomicInteger(0);
    private final Semaphore diskSemaphore = new Semaphore(1);
    private final KeyType keyType;

    public SpinedIntIntMap(KeyType keyType) {
        this.spineSize = DEFAULT_SPINE_SIZE;
        this.keyType = keyType;
    }

    public int nextIndex() {
        return nextIndex.getAndIncrement();
    }

    public int sizeInBytes() {
        int sizeInBytes = 0;
        sizeInBytes = sizeInBytes + ((spineSize * 4) * spines.size()); // 4 bytes = bytes of 32 bit integer
        return sizeInBytes;
    }

    /**
     * @param directory
     * @return the number of spine files read.
     */
    public int read(File directory) {
        diskSemaphore.acquireUninterruptibly();
        try {
            spineCount.set(SpineFileUtil.readSpineCount(directory));

            File[] files = directory.listFiles((pathname) -> pathname.getName().startsWith(SpineFileUtil.SPINE_PREFIX));
            int spineFilesRead = 0;
            for (File spineFile : files) {
                spineFilesRead++;
                int spine = Integer.parseInt(spineFile.getName().substring(SpineFileUtil.SPINE_PREFIX.length()));
                try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
                    int arraySize = dis.readInt();
                    int[] spineArray = new int[arraySize];
                    for (int i = 0; i < arraySize; i++) {
                        spineArray[i] = dis.readInt();
                    }
                    spines.put(spine, new AtomicIntegerArray(spineArray));
                } catch (IOException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                    throw new RuntimeException(ex);
                }
            }
            return spineFilesRead;
        } finally {
            diskSemaphore.release();
        }
    }

    public boolean write(File directory) {
        AtomicBoolean wroteAny = new AtomicBoolean(false);

        try {
            SpineFileUtil.writeSpineCount(directory, spineCount.get());
            spines.forEach((Integer key, AtomicIntegerArray spine) -> {
                String spineKey = SpineFileUtil.SPINE_PREFIX + key;
                boolean spineChanged = changedSpineIndexes.contains(key);

                if (spineChanged) {
                    wroteAny.set(true);
                    File spineFile = new File(directory, spineKey);
                    diskSemaphore.acquireUninterruptibly();
                    try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
                        dos.writeInt(spine.length());
                        for (int i = 0; i < spine.length(); i++) {
                            dos.writeInt(spine.get(i));
                        }
                    } catch (IOException ex) {
                        LOG.error(ex.getLocalizedMessage(), ex);
                        throw new RuntimeException(ex);
                    } finally {
                        diskSemaphore.release();
                    }
                }

            });
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        }
        return wroteAny.get();
    }

    public ConcurrentMap<Integer, AtomicIntegerArray> getSpines() {
        return spines;
    }

    public void put(int index, int element) {
        if (index < 0) {
            if (index < 0) {
                index = Integer.MAX_VALUE + index;
            }
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
//        Had an occasion where this illegal state was thrown...
//        Trying to add spine: 507 for: 5198069
//        if (spineIndex > this.spines.size() + 2) {
//            throw new IllegalStateException("Trying to add spine: " + spineIndex + " for: " + index);
//        }
        this.changedSpineIndexes.add(spineIndex);
        this.spines.computeIfAbsent(spineIndex, this::newSpine).set(indexInSpine, element);
    }

    private AtomicIntegerArray newSpine(Integer spineKey) {
        int[] spine = ArrayUtil.createAndFill(spineSize, INITIALIZATION_VALUE);
        this.spineCount.set(Math.max(this.spineCount.get(), spineKey + 1));
        return new AtomicIntegerArray(spine);
    }

    public int get(int index) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine);
    }

    public int getAndUpdate(int index, IntUnaryOperator generator) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        this.changedSpineIndexes.add(spineIndex);
        return this.spines.computeIfAbsent(spineIndex, this::newSpine).updateAndGet(indexInSpine, generator);
    }

    public boolean containsKey(int index) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine) != INITIALIZATION_VALUE;
    }

    public void forEach(Processor processor) {
        int currentSpineCount = getSpineCount();
        int key = 0;
        if (this.keyType == KeyType.NID_KEY) {
            key = PrimitiveDataService.FIRST_NID;
        }
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            AtomicIntegerArray spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
            for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
                int value = spine.get(indexInSpine);
                if (value != INITIALIZATION_VALUE) {
                    processor.process(key, value);
                }
                key++;
            }
        }
    }

    private int getSpineCount() {
        return spineCount.get();
    }

//  Dan notes KeyStream is not implemented correctly, and should not be used without being fixed....
//    public IntStream keyStream() {
//        final Supplier<? extends Spliterator.OfInt> streamSupplier = this.getKeySpliterator();
//
//        return StreamSupport.intStream(streamSupplier, streamSupplier.get()
//                .characteristics(), false);
//    }

    public IntStream parallelValueStream() {
        final Supplier<? extends Spliterator.OfInt> streamSupplier = this.getValueSpliterator();

        return StreamSupport.intStream(streamSupplier, streamSupplier.get()
                .characteristics(), true).filter(value -> value != Integer.MAX_VALUE);
    }

    /**
     * Gets the value spliterator supplier.
     *
     * @return the {@code Supplier<? extends Spliterator.OfInt}}
     */
    protected Supplier<? extends Spliterator.OfInt> getValueSpliterator() {
        return new ValueSpliteratorSupplier();
    }

    public IntStream valueStream() {
        final Supplier<? extends Spliterator.OfInt> streamSupplier = this.getValueSpliterator();

        return StreamSupport.intStream(streamSupplier, streamSupplier.get()
                .characteristics(), false).filter(value -> value != Integer.MAX_VALUE);
    }

    public interface Processor {

        public void process(int key, int value);
    }

//    /**
//     * Gets the value spliterator.
//     *
//     * @return the supplier<? extends spliterator. of int>
//     */
//    protected Supplier<? extends Spliterator.OfInt> getKeySpliterator() {
//        return new KeySpliteratorSupplier();
//    }

//    /**
//     * The Class KeySpliteratorSupplier.
//     */
//    private class KeySpliteratorSupplier
//            implements Supplier<Spliterator.OfInt> {
//
//        /**
//         * Gets the.
//         *
//         * @return the spliterator of int
//         */
//        @Override
//        public Spliterator.OfInt get() {
//            return new SpinedKeySpliterator();
//        }
//    }

    /**
     * The Class ValueSpliteratorSupplier.
     */
    private class ValueSpliteratorSupplier
            implements Supplier<Spliterator.OfInt> {

        /**
         * Gets the.
         *
         * @return the spliterator of int
         */
        @Override
        public Spliterator.OfInt get() {
            return new SpinedValueSpliterator();
        }
    }

    private class SpinedValueSpliterator implements Spliterator.OfInt {

        int end;
        int currentPosition;
        boolean split = false;

        public SpinedValueSpliterator() {
            this.end = DEFAULT_SPINE_SIZE * getSpineCount();
            this.currentPosition = 0;
        }

        private SpinedValueSpliterator(int start, int end) {
            this.currentPosition = start;
            this.end = end;
            this.split = true;
        }

        @Override
        public OfInt trySplit() {
            if (estimateSize() < 10) {
                return null;
            }
            int splitEnd = end;
            int split = end - currentPosition;
            int half = split / 2;
            if ((currentPosition + half) == splitEnd) {
                return null;
            }
            this.end = currentPosition + half;
            return new SpinedValueSpliterator(this.end, splitEnd);
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            while (currentPosition < end) {
                int value = get(currentPosition++);
                if (value != INITIALIZATION_VALUE) {
                    action.accept(value);
                    return true;
                }
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - currentPosition;
        }

        @Override
        public int characteristics() {
            return split ? Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED :
                    Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED;
        }

    }

//    private class SpinedKeySpliterator implements Spliterator.OfInt {
//
//        int end = DEFAULT_SPINE_SIZE * getSpineCount();
//        int currentPosition = 0;
//
//        public SpinedKeySpliterator() {
//        }
//
//        public SpinedKeySpliterator(int start, int end) {
//            this.currentPosition = start;
//            this.end = end;
//        }
//
//        @Override
    //Dan Notes - this split function has off-by-one errors due to the handling of half
//        public Spliterator.OfInt trySplit() {
//            int splitEnd = end;
//            int split = end - currentPosition;
//            int half = split / 2;
//            this.end = currentPosition + half;
//            return new SpinedKeySpliterator(currentPosition + half + 1, splitEnd);
//        }
//
//        @Override
//        public boolean tryAdvance(IntConsumer action) {
//            while (currentPosition < end) {
//                int key = currentPosition++;
//                int value = get(key);
//                if (value != INITIALIZATION_VALUE) {
//                    action.accept(key);
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        @Override
//        public long estimateSize() {
//            return end - currentPosition;
//        }
//
//        @Override
//        public int characteristics() {
//            return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
//                    | Spliterator.SIZED | Spliterator.SORTED;
//        }
//
//    }
}
