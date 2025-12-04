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
package dev.ikm.tinkar.common.util.text;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NaturalOrderTest {

    @Test
    public void testCompareStrings() {
        Comparator<String> naturalOrder = NaturalOrder.getStringComparator();
        int result = naturalOrder.compare("Test123", "Test124");
        assertEquals(-1, result);

        result = naturalOrder.compare("Test124", "Test123");
        assertEquals(1, result);

        result = naturalOrder.compare("Test123", "Test123");
        assertEquals(0, result);

        result = naturalOrder.compare("Test123.", "Test123 ");
        assertEquals(-1, result);

        result = naturalOrder.compare("Test123 ", "Test123.");
        assertEquals(1, result);

        result = naturalOrder.compare("Test123 ", "Test123 ");
        assertEquals(0, result);

        result = naturalOrder.compare("Test124", "Test123abc");
        assertEquals(1, result);

        result = naturalOrder.compare("Test123.", "Test123a");
        assertTrue(result < 0);

        result = naturalOrder.compare("Test123a", "Test123.");
        assertTrue(result > 0);

        result = naturalOrder.compare("Test123abc", "Test123abc");
        assertEquals(0, result);

        result = naturalOrder.compare("1Test123abc", "Test123abc");
        assertTrue(result > 0);

        result = naturalOrder.compare("1Test123abc", "10Test123abc");
        assertTrue(result < 0);
    }

    @Test
    public void testCompareObjects() {
        Comparator<Object> naturalOrder =  NaturalOrder.getObjectComparator();
        int result = naturalOrder.compare(new StringBuilder("Test123"), new StringBuilder("Test124"));
        assertEquals(-1, result);

        result = naturalOrder.compare(new StringBuilder("Test124"), new StringBuilder("Test123"));
        assertEquals(1, result);

        result = naturalOrder.compare(new StringBuilder("Test123"), new StringBuilder("Test123"));
        assertEquals(0, result);
    }

}