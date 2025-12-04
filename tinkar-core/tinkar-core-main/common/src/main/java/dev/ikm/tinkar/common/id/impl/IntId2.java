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

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class IntId2 {
    protected final int element;
    protected final int element2;

    public IntId2(int element, int element2) {
        this.element = element;
        this.element2 = element2;
    }

    public int size() {
        return 2;
    }

    public void forEach(IntConsumer consumer) {
        consumer.accept(element);
        consumer.accept(element2);
    }

    public IntStream intStream() {
        return IntStream.of(element, element2);
    }

    public int[] toArray() {
        return new int[]{element, element2};
    }

    public boolean contains(int value) {
        if (value == element) {
            return true;
        }
        return value == element2;
    }
}
