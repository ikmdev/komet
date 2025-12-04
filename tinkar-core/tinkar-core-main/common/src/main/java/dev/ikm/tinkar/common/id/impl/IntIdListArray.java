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
package dev.ikm.tinkar.common.id.impl;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.service.PrimitiveData;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 *
 */
public final class IntIdListArray
        implements IntIdList {
    private final int[] elements;

    public IntIdListArray(int... newElements) {
        this.elements = newElements;
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public IntStream intStream() {
        return IntStream.of(elements);
    }

    @Override
    public boolean contains(int value) {
        for (int element : elements) {
            if (value == element) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] toArray() {
        return elements;
    }

    @Override
    public void forEach(IntConsumer consumer) {
        for (int element : elements) {
            consumer.accept(element);
        }
    }

    @Override
    public int get(int index) {
        return elements[index];
    }

    @Override
    public boolean isEmpty() {
        return elements.length == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IntIdList intIdList) {
            if (intIdList.size() == elements.length && Arrays.equals(this.toArray(), intIdList.toArray())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        for(int element : elements) {
            h = 31 * h + element;
        }
        return h;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("IntIdList[");
        for (int i = 0; i < elements.length && i <= TO_STRING_LIMIT; i++) {
            sb.append(PrimitiveData.textWithNid(elements[i])).append(", ");
            if (i == TO_STRING_LIMIT) {
                sb.append("..., ");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }

}
