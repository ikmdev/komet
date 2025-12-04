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
package dev.ikm.tinkar.common.util.time;

import dev.ikm.tinkar.common.service.PrimitiveData;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static dev.ikm.tinkar.common.util.time.DateTimeUtil.CANCELED;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.LATEST;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.PREMUNDANE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeUtilTest {

    @Test
    void parse() {
        assertEquals(Long.MAX_VALUE, DateTimeUtil.parse(LATEST));
        assertEquals(Long.MIN_VALUE, DateTimeUtil.parse(CANCELED));
        assertEquals(PrimitiveData.PREMUNDANE_TIME, DateTimeUtil.parse(PREMUNDANE));
    }


    @Test
    void format() {
        assertEquals(LATEST, DateTimeUtil.format(Instant.MAX));
        assertEquals(CANCELED, DateTimeUtil.format(Instant.MIN));
        assertEquals(PREMUNDANE, DateTimeUtil.format(PrimitiveData.PREMUNDANE_INSTANT));

        assertEquals(LATEST, DateTimeUtil.format(Long.MAX_VALUE));
        assertEquals(CANCELED, DateTimeUtil.format(Long.MIN_VALUE));
        assertEquals(PREMUNDANE, DateTimeUtil.format(PrimitiveData.PREMUNDANE_TIME));
    }
}