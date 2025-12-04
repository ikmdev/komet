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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.ObjIntConsumer;

/**
 *
 *
 * @param <T>
 */
public class IntObjectMapImpl<T> extends ConcurrentHashMap<Integer, T> implements IntObjectMap<T> {

    @Override
    public T getAndSet(int key, T value) {
        return super.put(key, value);
    }

    @Override
    public Optional<T> getOptional(int key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public void forEach(ObjIntConsumer<T> consumer) {
        super.forEach((Integer first, T second) -> {
            consumer.accept(second, (int) first);
        });
    }

    @Override
    public boolean put(int key, T value) {
        T existing = super.put(key, value);
        return existing == null;
    }

    @Override
    public T get(int key) {
       return super.get(key);
    }

    @Override
    public boolean containsKey(int key) {
        return super.containsKey(key);
    }

    @Override
    public T accumulateAndGet(int index, T newValue, BinaryOperator<T> accumulatorFunction) {
        return super.merge(index, newValue, accumulatorFunction);
    }

}
