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

import dev.ikm.tinkar.collection.store.ByteArrayArrayStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * 
 */
public class SpinedByteArrayArrayMap extends SpinedIntObjectMap<byte[][]> {

    private static final Logger LOG = LoggerFactory.getLogger(SpinedByteArrayArrayMap.class);
    private final ByteArrayArrayStore byteArrayArrayStore;

    public SpinedByteArrayArrayMap(ByteArrayArrayStore byteArrayArrayStore) {
        super(byteArrayArrayStore.getSpineCount());
        this.byteArrayArrayStore = byteArrayArrayStore;
    }

    public int sizeOnDisk() {
        return byteArrayArrayStore.sizeOnDisk();
    }

    public int memoryInUse() {
        AtomicInteger sizeInBytes = new AtomicInteger();
        sizeInBytes.addAndGet((spineSize * 8) * getSpineCount()); // 8 bytes = pointer to an object
        forEachSpine((AtomicReferenceArray<byte[][]> spine, int spineIndex) -> {
            for (int i = 0; i < spine.length(); i++) {
                byte[][] value = spine.get(i);
                if (value != null) {
                    for (byte[] byteArray : value) {
                        sizeInBytes.addAndGet(byteArray.length + 4);// 4 bytes = integer length of the array of array length.
                    }
                }
            }
        });
        return sizeInBytes.get();
    }

    protected AtomicReferenceArray<byte[][]> readSpine(int spineIndex) {
        Optional<AtomicReferenceArray<byte[][]>> optionalSpine = this.byteArrayArrayStore.get(spineIndex);
        if (optionalSpine.isPresent()) {
            return optionalSpine.get();
        }
        return new AtomicReferenceArray<>(spineSize);
    }

    public boolean write() {
        try {
            fileSemaphore.acquireUninterruptibly();
            this.byteArrayArrayStore.writeSpineCount(getSpineCount());
            return forEachChangedSpine((AtomicReferenceArray<byte[][]> spine, int spineIndex) -> {
                this.byteArrayArrayStore.put(spineIndex, spine);
            });
        } finally {
            fileSemaphore.release();
        }
    }

    private byte[][] merge(byte[][] currentValue, byte[][] updateValue) {
        if (currentValue == null || currentValue.length == 0) {
            return updateValue;
        }
        if (updateValue == null) {
            throw new IllegalStateException("Update value is null");
        }
        Arrays.sort(updateValue, SpinedByteArrayArrayMap::compare);
        ArrayList<byte[]> mergedValues = new ArrayList<>(currentValue.length + updateValue.length);
        int updateIndex = 0;
        int currentIndex = 0;

        while (updateIndex < updateValue.length && currentIndex < currentValue.length) {
            int compare = compare(currentValue[currentIndex], updateValue[updateIndex]);
            if (compare == 0) {
                mergedValues.add(currentValue[currentIndex]);
                currentIndex++;
                updateIndex++;
                if (currentIndex == currentValue.length) {
                    while (updateIndex < updateValue.length) {
                        mergedValues.add(updateValue[updateIndex++]);
                    }
                }
                if (updateIndex == updateValue.length) {
                    while (currentIndex < currentValue.length) {
                        mergedValues.add(currentValue[currentIndex++]);
                    }
                }
            } else if (compare < 0) {
                mergedValues.add(currentValue[currentIndex]);
                currentIndex++;
                if (currentIndex == currentValue.length) {
                    while (updateIndex < updateValue.length) {
                        mergedValues.add(updateValue[updateIndex++]);
                    }
                }
            } else {
                mergedValues.add(updateValue[updateIndex]);
                updateIndex++;
                if (updateIndex == updateValue.length) {
                    while (currentIndex < currentValue.length) {
                        mergedValues.add(currentValue[currentIndex++]);
                    }
                }
            }
        }
        return mergedValues.toArray(new byte[mergedValues.size()][]);
    }

    private static int compare(byte[] one, byte[] another) {
        boolean oneStartsWithZero = false;
        boolean anotherStartsWithZero = false;

        if (one[0] == 0 && one[1] == 0 && one[2] == 0 && one[3] == 0) {
            oneStartsWithZero = true;
        }
        if (another[0] == 0 && another[1] == 0 && another[2] == 0 && another[3] == 0) {
            anotherStartsWithZero = true;
        }
        if (oneStartsWithZero && anotherStartsWithZero) {
            return 0;
        }
        if (oneStartsWithZero) {
            return -1;
        }
        if (anotherStartsWithZero) {
            return 1;
        }
        for (int i = 0; i < one.length && i < another.length; i++) {
            int compare = Byte.compare(one[i], another[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return one.length - another.length;
    }

    public void put(int elementSequence, List<byte[]> dataList) {
        put(elementSequence, dataList.toArray(new byte[dataList.size()][]));
    }

}
