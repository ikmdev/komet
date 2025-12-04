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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static dev.ikm.tinkar.collection.SpineFileUtil.SPINE_PREFIX;

/**
 * Use when mapping all nids, not just a subset.
 *
 *
 */
public class SpinedNidIntMap {

    private static final Logger LOG = LoggerFactory.getLogger(SpinedNidIntMap.class);

    private static final int DEFAULT_ELEMENTS_PER_SPINE = 1024;
    protected final ConcurrentSkipListSet<Integer> changedSpineIndexes = new ConcurrentSkipListSet<>();
    private final int elementsPerSpine;
    private final int INITIALIZATION_VALUE = Integer.MAX_VALUE;
    private final ConcurrentSpineList<AtomicIntegerArray> spines = new ConcurrentSpineList<>(16884, this::newSpine);
    private final Semaphore diskSemaphore = new Semaphore(1);

    public SpinedNidIntMap() {
        this.elementsPerSpine = DEFAULT_ELEMENTS_PER_SPINE;
    }

    /**
     * Empty this data structure (does nothing to the disk location it was read from)
     */
    public void clear() {
        spines.clear();
        changedSpineIndexes.clear();
    }

    /**
     * @param directory
     * @return the number of spine files read.
     */
    public int read(File directory) {
        diskSemaphore.acquireUninterruptibly();
        try {
            File[] files = directory.listFiles((pathname) -> {
                return pathname.getName().startsWith(SPINE_PREFIX);
            });
            int spineFilesRead = 0;
            for (File spineFile : files) {
                spineFilesRead++;
                int spine = Integer.parseInt(spineFile.getName().substring(SPINE_PREFIX.length()));
                try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
                    int arraySize = dis.readInt();
                    int[] spineArray = new int[arraySize];
                    for (int i = 0; i < arraySize; i++) {
                        spineArray[i] = dis.readInt();
                    }
                    spines.setSpine(spine, new AtomicIntegerArray(spineArray));
                } catch (IOException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                    throw new RuntimeException(ex);
                }
            }
            LOG.info("Spine count read: " + getSpineCount());
            return spineFilesRead;
        } finally {
            diskSemaphore.release();
        }
    }

    private int getSpineCount() {
        return spines.getSpineCount();
    }

    public boolean write(File directory) {
        AtomicBoolean wroteAny = new AtomicBoolean(false);
        try {
            directory.mkdirs();
            SpineFileUtil.writeSpineCount(directory, spines.getSpineCount());
            AtomicReferenceArray<AtomicIntegerArray> spineArray = spines.getSpines();
            int length = spineArray.length();
            for (int key = 0; key < length; key++) {
                AtomicIntegerArray aSpine = spineArray.get(key);
                if (aSpine != null) {

                    String spineKey = SPINE_PREFIX + key;
                    boolean spineChanged = changedSpineIndexes.contains(key);

                    if (spineChanged) {
                        wroteAny.set(true);
                        changedSpineIndexes.remove(key);
                        File spineFile = new File(directory, spineKey);
                        diskSemaphore.acquireUninterruptibly();
                        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
                            dos.writeInt(aSpine.length());
                            for (int i = 0; i < aSpine.length(); i++) {
                                dos.writeInt(aSpine.get(i));
                            }
                        } catch (IOException ex) {
                            LOG.error(ex.getLocalizedMessage(), ex);
                            throw new RuntimeException(ex);
                        } finally {
                            diskSemaphore.release();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        }
        return wroteAny.get();
    }

    public long sizeInBytes() {
        long sizeInBytes = 0;

        sizeInBytes = sizeInBytes + ((elementsPerSpine * 4) * getSpineCount());  // 4 bytes = bytes of 32 bit integer
        return sizeInBytes;
    }

    private AtomicIntegerArray newSpine() {
        return new AtomicIntegerArray(ArrayUtil.createAndFill(elementsPerSpine, INITIALIZATION_VALUE));
    }

    public ConcurrentSpineList<AtomicIntegerArray> getSpines() {
        return spines;
    }

    public void put(int index, int element) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        this.changedSpineIndexes.add(spineIndex);
        this.spines.getSpine(spineIndex).set(indexInSpine, element);
    }

    public int get(int index) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        return this.spines.getSpine(spineIndex).get(indexInSpine);
    }

    public int getAndUpdate(int index, IntUnaryOperator generator) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        AtomicIntegerArray spine = this.spines.getSpine(spineIndex);
        int currentValue = spine.get(indexInSpine);
        if (currentValue != INITIALIZATION_VALUE) {
            return currentValue;
        }
        this.changedSpineIndexes.add(spineIndex);
        return spine.updateAndGet(indexInSpine, generator);
    }

    public boolean containsKey(int index) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        return this.spines.getSpine(spineIndex).get(indexInSpine) != INITIALIZATION_VALUE;
    }

    public void forEach(Processor processor) {
        int currentSpineCount = getSpineCount();
        int key = 0;
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            AtomicIntegerArray spine = this.spines.getSpine(spineIndex);
            for (int indexInSpine = 0; indexInSpine < elementsPerSpine; indexInSpine++) {
                int value = spine.get(indexInSpine);
                if (value != INITIALIZATION_VALUE) {
                    processor.process(key, value);
                }
            }
            key++;
        }
    }

    public IntStream keyStream() {
        final Supplier<? extends Spliterator.OfInt> streamSupplier = this.getKeySpliterator();

        return StreamSupport.intStream(streamSupplier, streamSupplier.get()
                .characteristics(), false);
    }

    /**
     * Gets the key spliterator supplier.
     *
     * @return the {@code Supplier<? extends Spliterator.OfInt}}
     */
    protected Supplier<? extends Spliterator.OfInt> getKeySpliterator() {
        return new KeySpliteratorSupplier();
    }

    public IntStream valueStream() {
        final Supplier<? extends Spliterator.OfInt> streamSupplier = this.getValueSpliterator();

        return StreamSupport.intStream(streamSupplier, streamSupplier.get()
                .characteristics(), false);
    }

    /**
     * Gets the value spliterator supplier.
     *
     * @return the {@code Supplier<? extends Spliterator.OfInt}}
     */
    protected Supplier<? extends Spliterator.OfInt> getValueSpliterator() {
        return new ValueSpliteratorSupplier();
    }

    public void addSpine(int spineKey, AtomicIntegerArray spineData) {
        spines.setSpine(spineKey, spineData);
    }

    public interface Processor {

        public void process(int key, int value);
    }

    /**
     * The Class KeySpliteratorSupplier.
     */
    private class KeySpliteratorSupplier
            implements Supplier<Spliterator.OfInt> {

        /**
         * Gets the.
         *
         * @return the spliterator of int
         */
        @Override
        public Spliterator.OfInt get() {
            return new SpinedKeySpliterator();
        }
    }

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

        public SpinedValueSpliterator() {
            this.end = DEFAULT_ELEMENTS_PER_SPINE * getSpineCount();
            this.currentPosition = 0;
        }

        public SpinedValueSpliterator(int start, int end) {
            this.currentPosition = start;
            this.end = end;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            int splitEnd = end;
            int split = end - currentPosition;
            int half = split / 2;
            this.end = currentPosition + half;
            return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
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
            return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                    | Spliterator.SIZED;
        }

    }

    private class SpinedKeySpliterator implements Spliterator.OfInt {

        int end;
        int currentPosition;

        public SpinedKeySpliterator() {
            this.end = DEFAULT_ELEMENTS_PER_SPINE * getSpineCount();
            this.currentPosition = 0;
        }

        public SpinedKeySpliterator(int start, int end) {
            this.currentPosition = start;
            this.end = end;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            int splitEnd = end;
            int split = end - currentPosition;
            int half = split / 2;
            this.end = currentPosition + half;
            return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            while (currentPosition < end) {
                int key = currentPosition++;
                int value = get(key);
                if (value != INITIALIZATION_VALUE) {
                    action.accept(key);
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
            return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                    | Spliterator.SIZED | Spliterator.SORTED;
        }

    }
}
