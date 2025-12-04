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

import dev.ikm.tinkar.collection.store.IntIntSetStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BinaryOperator;
import java.util.function.ObjIntConsumer;

public class SpinedIntIntSetMap implements IntObjectMap<ConcurrentSkipListSet<Integer>> {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedIntIntSetMap.class);

    private static final int DEFAULT_ELEMENTS_PER_SPINE = 10240;

    protected final IntIntSetStore intIntSetStore;
    protected final int elementsPerSpine;
    protected final ConcurrentSkipListSet<Integer> changedSpineIndexes = new ConcurrentSkipListSet<>();
    private final ConcurrentSpineList<AtomicReferenceArray<ConcurrentSkipListSet<Integer>>> spines = new ConcurrentSpineList<>(1, this::newSpine);

    public SpinedIntIntSetMap(IntIntSetStore intIntSetStore) {
        this.intIntSetStore = intIntSetStore;
        this.elementsPerSpine = DEFAULT_ELEMENTS_PER_SPINE;
    }

    private AtomicReferenceArray<ConcurrentSkipListSet<Integer>> newSpine() {
        AtomicReferenceArray<ConcurrentSkipListSet<Integer>> spine = new AtomicReferenceArray<>(elementsPerSpine);
        return spine;
    }

    @Override
    public boolean put(int index, ConcurrentSkipListSet<Integer> elementSet) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        this.changedSpineIndexes.add(spineIndex);
        return this.spines.getSpine(spineIndex).getAndSet(indexInSpine, elementSet) == null;
    }

    @Override
    public ConcurrentSkipListSet<Integer> getAndSet(int index, ConcurrentSkipListSet<Integer> elementSet) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        this.changedSpineIndexes.add(spineIndex);
        return this.spines.getSpine(spineIndex).getAndSet(indexInSpine, elementSet);
    }

    @Override
    public ConcurrentSkipListSet<Integer> get(int index) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        return this.spines.getSpine(spineIndex).get(indexInSpine);
    }

    @Override
    public Optional<ConcurrentSkipListSet<Integer>> getOptional(int key) {
        return Optional.empty();
    }

    @Override
    public boolean containsKey(int key) {
        return false;
    }

    @Override
    public int size() {
        int size = 0;
        int currentSpineCount = this.spines.getSpineCount();
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            AtomicReferenceArray<ConcurrentSkipListSet<Integer>> spine = this.spines.getSpine(spineIndex);
            for (int indexInSpine = 0; indexInSpine < elementsPerSpine; indexInSpine++) {
                ConcurrentSkipListSet<Integer> element = spine.get(indexInSpine);
                if (element != null) {
                    size++;
                }
            }
        }
        return size;
    }

    @Override
    public void clear() {
        this.spines.clear();
    }

    @Override
    public void forEach(ObjIntConsumer<ConcurrentSkipListSet<Integer>> consumer) {
        int currentSpineCount = getSpineCount();
        int key = 0;
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            AtomicReferenceArray<ConcurrentSkipListSet<Integer>> spine = this.spines.getSpine(spineIndex);
            for (int indexInSpine = 0; indexInSpine < elementsPerSpine; indexInSpine++) {
                ConcurrentSkipListSet<Integer> element = spine.get(indexInSpine);
                if (element != null) {
                    consumer.accept(element, key);
                }
                key++;
            }

        }
    }

    public int getSpineCount() {
        return spines.getSpineCount();
    }

    @Override
    public ConcurrentSkipListSet<Integer> accumulateAndGet(int index, ConcurrentSkipListSet<Integer> newValue, BinaryOperator<ConcurrentSkipListSet<Integer>> accumulatorFunction) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        this.changedSpineIndexes.add(spineIndex);
        return this.spines.getSpine(spineIndex)
                .accumulateAndGet(indexInSpine, newValue, accumulatorFunction);
    }

    public boolean addToSet(int index, int element) {
        if (index < 0) {
            index = Integer.MAX_VALUE + index;
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;

        AtomicReferenceArray<ConcurrentSkipListSet<Integer>> spine = this.spines.getSpine(spineIndex);
        ConcurrentSkipListSet<Integer> set = this.spines.getSpine(spineIndex)
                .updateAndGet(indexInSpine, integerSet -> {
                    if (integerSet == null) {
                        integerSet = new ConcurrentSkipListSet<>();
                    }
                    return integerSet;
                });
        if (set.add(element)) {
            this.changedSpineIndexes.add(spineIndex);
            return true;
        }
        return false;
    }
}
