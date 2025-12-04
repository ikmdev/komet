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
package dev.ikm.tinkar.common.util;

import java.util.Arrays;
import java.util.function.Supplier;

public class ArrayUtil {
    public static long[] createAndFill(int size, long fill) {
        long[] arrayToFill = new long[size];
        Arrays.fill(arrayToFill, fill);
        return arrayToFill;
    }
    public static int[] createAndFill(int size, int fill) {
        int[] arrayToFill = new int[size];
        Arrays.fill(arrayToFill, fill);
        return arrayToFill;
    }

    public static int[] createAndFillWithMinusOne(int size) {
        int[] arrayToFill = new int[size];
        Arrays.fill(arrayToFill, -1);
        return arrayToFill;
    }

    public static <T> T getIfAbsentPut(T[] array, int index, Supplier<T> supplier) {
        if (array[index] == null) {
            array[index] = supplier.get();
        }
        return array[index];
    }
}
