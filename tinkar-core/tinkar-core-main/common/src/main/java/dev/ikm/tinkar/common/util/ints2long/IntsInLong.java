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
package dev.ikm.tinkar.common.util.ints2long;

public class IntsInLong {
    public static long ints2Long(int int1, int int2) {
        return (((long) int1) << 32) | (int2 & 0xffffffffL);
    }

    public static int int1FromLong(long combinedInts) {
        return (int) (combinedInts >> 32);
    }

    public static int int2FromLong(long combinedInts) {
        return (int) combinedInts;
    }
}
