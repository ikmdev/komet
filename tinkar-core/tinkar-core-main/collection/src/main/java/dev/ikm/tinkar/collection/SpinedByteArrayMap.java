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

import dev.ikm.tinkar.collection.store.ByteArrayStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class SpinedByteArrayMap extends SpinedIntObjectMap<byte[]> {

    private static final Logger LOG = LoggerFactory.getLogger(SpinedByteArrayMap.class);
    private final ByteArrayStore byteArrayStore;

    public SpinedByteArrayMap(ByteArrayStore byteArrayStore) {
        super(byteArrayStore.getSpineCount());
        this.byteArrayStore = byteArrayStore;
    }

    public int sizeOnDisk() {
        return byteArrayStore.sizeOnDisk();
    }

    public int memoryInUse() {
        AtomicInteger sizeInBytes = new AtomicInteger();
        sizeInBytes.addAndGet(((spineSize * 8) * getSpineCount()));
        forEachSpine((AtomicReferenceArray<byte[]> spine, int spineIndex) -> {
            for (int i = 0; i < spine.length(); i++) {
                byte[] value = spine.get(i);
                sizeInBytes.addAndGet(value.length + 4); // 4 bytes = integer length of the array of array length.
            }
        });
        return sizeInBytes.get();
    }


    protected AtomicReferenceArray<byte[]> readSpine(int spineIndex) {
        Optional<AtomicReferenceArray<byte[]>> optionalSpine = this.byteArrayStore.get(spineIndex);
        if (optionalSpine.isPresent()) {
            return optionalSpine.get();
        }
        return new AtomicReferenceArray<>(spineSize);
    }

    public boolean write() {

        try {
            fileSemaphore.acquireUninterruptibly();
            this.byteArrayStore.writeSpineCount(getSpineCount());
            return forEachChangedSpine((AtomicReferenceArray<byte[]> spine, int spineIndex) -> {
                this.byteArrayStore.put(spineIndex, spine);
            });
        } finally {
            fileSemaphore.release();
        }
    }

}
