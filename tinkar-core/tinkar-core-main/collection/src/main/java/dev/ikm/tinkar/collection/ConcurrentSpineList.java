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

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;

public class ConcurrentSpineList<E extends Object> {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentSpineList.class);
    private final int incrementSize = 4096;
    private final AtomicReference<AtomicReferenceArray<E>> spineArrayReference = new AtomicReference<>();
    private final Supplier<E> supplier;

    public ConcurrentSpineList(int size, Supplier<E> supplier) {
        this.spineArrayReference.set(new AtomicReferenceArray<>(size));
        this.supplier = supplier;
    }

    public ConcurrentSpineList(E[] elements, Supplier<E> supplier) {
        this.spineArrayReference.set(new AtomicReferenceArray<>(elements));
        this.supplier = supplier;
    }

    public E getSpine(int spineIndex) {
        AtomicReferenceArray<E> spineArray = spineArrayReference.get();
        if (spineIndex < spineArray.length()) {
            E spine = spineArray.get(spineIndex);
            if (spine != null) {
                return spine;
            }
            spineArrayReference.get().compareAndExchange(spineIndex, null, supplier.get());
            return spineArrayReference.get().get(spineIndex);
        }
        // need to grow array
        // need to add spine to array
        growArray(spineIndex);
        return getSpine(spineIndex);
    }

    public void setSpine(int spineIndex, E spine) {
        AtomicReferenceArray<E> spineArray = spineArrayReference.get();
        if (spineIndex >= spineArray.length()) {
            LOG.info("Growing for length: " + spineIndex);
            growArray(spineIndex);
            spineArray = spineArrayReference.get();
            LOG.info("new length: " + spineArray.length());
        }
        spineArray.set(spineIndex, spine);
    }

    private void growArray(int spineIndex) {
        AtomicReferenceArray<E> spineArray = spineArrayReference.get();
        if (spineIndex >= spineArray.length()) {
            AtomicReferenceArray<E> newSpineArray = new AtomicReferenceArray<>(spineIndex + incrementSize);
            for (int i = 0; i < spineArray.length(); i++) {
                newSpineArray.set(i, spineArray.get(i));
            }
            spineArrayReference.compareAndSet(spineArray, newSpineArray);
        }
    }

    public int getSpineCount() {
        if (spineArrayReference.get() == null) {
            return 0;
        }
        return spineArrayReference.get().length();
    }

    public void clear() {
        spineArrayReference.set(new AtomicReferenceArray<>(0));
    }

    public AtomicReferenceArray<E> getSpines() {
        return spineArrayReference.get();
    }
}
