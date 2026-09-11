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
package dev.ikm.komet.framework.settings;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the stylesheet each text-size step produces: the one {@code .root} rule that Komet's
 * em-based stylesheets scale from, carried as a {@code data:} URL so it needs no file.
 */
class TextSizeStylesheetTest {

    private static final String DATA_PREFIX = "data:text/css;base64,";

    private static String css(TextSize size) {
        String url = TextSizeStylesheet.stylesheetUrlFor(size);
        assertTrue(url.startsWith(DATA_PREFIX), "a data: URL carrying CSS, got: " + url);
        return new String(Base64.getDecoder().decode(url.substring(DATA_PREFIX.length())), StandardCharsets.UTF_8);
    }

    @Test
    void theDefaultStepSizesTheRootAtTheBaseFontSize() {
        assertEquals(TextSize.DEFAULT_FONT_SIZE, TextSize.DEFAULT.fontSize());
        assertEquals(".root { -fx-font-size: 12.0px; }", css(TextSize.DEFAULT));
    }

    @Test
    void everyOtherStepScalesTheBaseByItsPercent() {
        assertEquals(10.2, TextSize.SMALL.fontSize(), 1e-9);
        assertEquals(13.8, TextSize.LARGE.fontSize(), 1e-9);
        assertEquals(15.6, TextSize.EXTRA_LARGE.fontSize(), 1e-9);
        assertEquals(".root { -fx-font-size: 13.8px; }", css(TextSize.LARGE));
    }

    @Test
    void eachStepHasItsOwnUrlSoASceneCanSwapOneForAnother() {
        for (TextSize a : TextSize.values()) {
            for (TextSize b : TextSize.values()) {
                if (a != b) {
                    assertNotEquals(TextSizeStylesheet.stylesheetUrlFor(a), TextSizeStylesheet.stylesheetUrlFor(b));
                }
            }
        }
        assertEquals(TextSizeStylesheet.stylesheetUrlFor(TextSize.LARGE), TextSizeStylesheet.stylesheetUrlFor(TextSize.LARGE),
                "the same step always yields the same URL, so it can be found on a scene again");
    }
}
