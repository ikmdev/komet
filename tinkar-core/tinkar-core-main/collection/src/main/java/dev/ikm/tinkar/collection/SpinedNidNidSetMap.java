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
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class SpinedNidNidSetMap {

    private static final Logger LOG = LoggerFactory.getLogger(SpinedNidNidSetMap.class);
    private static final int DEFAULT_SPINE_SIZE = 1024;
    protected final int spineSize;
    protected final ConcurrentMap<Integer, AtomicReferenceArray<int[]>> spines = new ConcurrentHashMap<>();
    //    private Function<int[], String> elementStringConverter;
    protected final AtomicInteger spineCount = new AtomicInteger();
    protected final ConcurrentSkipListSet<Integer> changedSpineIndexes = new ConcurrentSkipListSet<>();
    private final Semaphore diskSemaphore = new Semaphore(1);

    public SpinedNidNidSetMap() {
        this.spineSize = DEFAULT_SPINE_SIZE;
    }

    /**
     * Clear this map.  Does nothing to the directory it was read from.
     */
    public void clear() {
        spines.clear();
        spineCount.set(0);
        changedSpineIndexes.clear();
    }

    public int sizeInBytes() {
        int sizeInBytes = 0;
        sizeInBytes = sizeInBytes + ((spineSize * 8) * spines.size()); // 8 bytes = pointer to an object
        for (AtomicReferenceArray<int[]> spine : spines.values()) {
            for (int i = 0; i < spine.length(); i++) {
                int[] value = spine.get(i);
                if (value != null) {
                    sizeInBytes = sizeInBytes + (value.length * 4);
                }
            }
        }
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

            File[] files = directory.listFiles((pathname) -> {
                return pathname.getName().startsWith(SpineFileUtil.SPINE_PREFIX);
            });
            int spineFilesRead = 0;
            for (File spineFile : files) {
                spineFilesRead++;
                int spine = Integer.parseInt(spineFile.getName().substring(SpineFileUtil.SPINE_PREFIX.length()));
                try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
                    int arraySize = dis.readInt();
                    int offset = arraySize * spine;
                    for (int i = 0; i < arraySize; i++) {
                        int valueSize = dis.readInt();
                        if (valueSize != 0) {
                            int[] value = new int[valueSize];
                            put(offset + i, value);
                            for (int j = 0; j < valueSize; j++) {
                                value[j] = dis.readInt();
                            }
                        }
                    }
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

    public void put(int index, int[] element) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        this.changedSpineIndexes.add(spineIndex);
        this.spines.computeIfAbsent(spineIndex, this::newSpine).accumulateAndGet(indexInSpine, element, MergeIntArray::merge);
    }

    protected AtomicReferenceArray<int[]> newSpine(Integer spineKey) {
        AtomicReferenceArray<int[]> spine = new AtomicReferenceArray<>(spineSize);
        this.spineCount.set(Math.max(this.spineCount.get(), spineKey + 1));
        return spine;
    }

    /**
     * @param directory
     * @return true if data spineChangedArray since last write.
     */
    public boolean write(File directory) {
        AtomicBoolean wroteAny = new AtomicBoolean(false);
        try {
            directory.mkdirs();
            SpineFileUtil.writeSpineCount(directory, spineCount.get());
            spines.forEach((Integer key, AtomicReferenceArray<int[]> spine) -> {
                String spineKey = SpineFileUtil.SPINE_PREFIX + key;
                boolean spineChanged = changedSpineIndexes.contains(key);

                if (spineChanged) {
                    wroteAny.set(true);
                    changedSpineIndexes.remove(key);
                    File spineFile = new File(directory, spineKey);
                    diskSemaphore.acquireUninterruptibly();
                    try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
                        dos.writeInt(spine.length());
                        for (int i = 0; i < spine.length(); i++) {
                            int[] value = spine.get(i);
                            if (value == null) {
                                dos.writeInt(0);
                            } else {
                                dos.writeInt(value.length);
                                for (int valueElement : value) {
                                    dos.writeInt(valueElement);
                                }
                            }
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

    public synchronized void add(int index, int element) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        } else {
            throw new UnsupportedOperationException("index >= 0: " + index);
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        this.changedSpineIndexes.add(spineIndex);
        this.spines.computeIfAbsent(spineIndex, this::newSpine).accumulateAndGet(indexInSpine, new int[]{element}, MergeIntArray::merge);
    }

    public int[] get(int index) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        int[] result = this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine);
        if (result == null) {
            return new int[]{};
        }
        return result;
    }

    public boolean containsKey(int index) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        } else {
            throw new UnsupportedOperationException("index >= 0: " + index);
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine) != null;
    }

    public void forEach(Processor<int[]> processor) {
        int currentSpineCount = getSpineCount();
        int key = 0;
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            AtomicReferenceArray<int[]> spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
            for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
                int[] element = spine.get(indexInSpine);
                if (element != null) {
                    processor.process(key, element);
                }
                key++;
            }

        }
    }

    private int getSpineCount() {
        return spineCount.get();
    }

    public Stream<int[]> stream() {
        final Supplier<? extends Spliterator<int[]>> streamSupplier = this.get();

        return StreamSupport.stream(streamSupplier, streamSupplier.get()
                .characteristics(), false);
    }

    /**
     * Gets the value spliterator supplier.
     *
     * @return the {@code Supplier<? extends Spliterator<int[]>}}
     */
    protected Supplier<? extends Spliterator<int[]>> get() {
        return new SpliteratorSupplier();
    }

    public interface Processor<E> {

        public void process(int key, E value);
    }

    /**
     * The Class SpliteratorSupplier.
     */
    private class SpliteratorSupplier
            implements Supplier<Spliterator<int[]>> {

        /**
         * Gets the.
         *
         * @return the spliterator
         */
        @Override
        public Spliterator<int[]> get() {
            return new SpinedValueSpliterator();
        }
    }

    private class SpinedValueSpliterator implements Spliterator<int[]> {

        int end;
        int currentPosition;

        public SpinedValueSpliterator() {
            this.end = DEFAULT_SPINE_SIZE * getSpineCount();
            this.currentPosition = 0;
        }

        public SpinedValueSpliterator(int start, int end) {
            this.currentPosition = start;
            this.end = end;
        }

        @Override
        public boolean tryAdvance(Consumer<? super int[]> action) {
            while (currentPosition < end) {
                int[] value = get(currentPosition++);
                if (value != null) {
                    action.accept(value);
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<int[]> trySplit() {
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
