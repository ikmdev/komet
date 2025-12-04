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
package dev.ikm.tinkar.common.id;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public interface IntIdCollection extends IdCollection {

    IntStream intStream();

    boolean contains(int value);

    default boolean notEmpty() {
        return !isEmpty();
    }

    boolean isEmpty();

    default <T extends Object> T[] mapToArray(IntFunction<T> function, Class<T> clazz) {
        T[] array = (T[]) Array.newInstance(clazz, size());
        int[] nids = toArray();
        for (int i = 0; i < array.length; i++) {
            array[i] = function.apply(nids[i]);
        }
        return array;
    }

    int[] toArray();

    default <T extends Object> List<T> mapToList(IntFunction<T> function) {
        ArrayList<T> list = new ArrayList<>(size());
        forEach(nid -> list.add(function.apply(nid)));
        return list;
    }

    void forEach(IntConsumer consumer);

    default <T extends Object> Set<T> mapToSet(IntFunction<T> function) {
        HashSet<T> set = new HashSet<>(size());
        forEach(nid -> set.add(function.apply(nid)));
        return set;
    }

    IntIdCollection with(int... valuesToAdd);


}
