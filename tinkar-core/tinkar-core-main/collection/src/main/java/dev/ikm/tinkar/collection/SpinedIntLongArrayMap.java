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

import dev.ikm.tinkar.collection.store.IntLongArrayStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class SpinedIntLongArrayMap extends SpinedIntObjectMap<long[]> {

    private static final Logger LOG = LoggerFactory.getLogger(SpinedIntLongArrayMap.class);
    private final IntLongArrayStore intLongArrayStore;

    public SpinedIntLongArrayMap(IntLongArrayStore intLongArrayStore) {
        super(intLongArrayStore.getSpineCount());
        this.intLongArrayStore = intLongArrayStore;
    }

    public int sizeOnDisk() {
        return intLongArrayStore.sizeOnDisk();
    }

    public int memoryInUse() {
        AtomicInteger sizeInBytes = new AtomicInteger();
        sizeInBytes.addAndGet(((spineSize * 8) * getSpineCount()));
        forEachSpine((AtomicReferenceArray<long[]> spine, int spineIndex) -> {
            for (int i = 0; i < spine.length(); i++) {
                long[] value = spine.get(i);
                sizeInBytes.addAndGet(value.length + 4); // 4 bytes = integer length of the array of array length.
            }
        });
        return sizeInBytes.get();
    }


    protected AtomicReferenceArray<long[]> readSpine(int spineIndex) {
        Optional<AtomicReferenceArray<long[]>> optionalSpine = this.intLongArrayStore.get(spineIndex);
        if (optionalSpine.isPresent()) {
            return optionalSpine.get();
        }
        return new AtomicReferenceArray<>(spineSize);
    }

    public boolean write() {

        try {
            fileSemaphore.acquireUninterruptibly();
            this.intLongArrayStore.writeSpineCount(getSpineCount());
            return forEachChangedSpine((AtomicReferenceArray<long[]> spine, int spineIndex) -> {
                this.intLongArrayStore.put(spineIndex, spine);
            });
        } finally {
            fileSemaphore.release();
        }
    }

}
