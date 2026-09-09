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
package dev.ikm.komet.kview.mvvm.view.genpurpose.test;

import dev.ikm.komet.kview.mvvm.view.genpurpose.PatternUIUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatternUIUtilsTest {

    @Test
    @DisplayName("A trailing Pattern word is dropped, whatever its case or surrounding space")
    void trailingPatternWordIsDropped() {
        assertEquals("Description", PatternUIUtils.stripPatternSuffix("Description Pattern"));
        assertEquals("Identifier", PatternUIUtils.stripPatternSuffix("  Identifier pattern "));
        assertEquals("Stated axioms", PatternUIUtils.stripPatternSuffix("Stated axioms   PATTERN"));
    }

    @Test
    @DisplayName("A name that is nothing but the word Pattern is kept whole")
    void bareWordIsKept() {
        assertEquals("Pattern", PatternUIUtils.stripPatternSuffix("Pattern"));
        assertEquals("Pattern", PatternUIUtils.stripPatternSuffix("  Pattern  "));
    }

    @Test
    @DisplayName("Only a whole trailing word counts")
    void onlyAWholeTrailingWordCounts() {
        assertEquals("DescriptionPattern", PatternUIUtils.stripPatternSuffix("DescriptionPattern"));
        assertEquals("Patterns", PatternUIUtils.stripPatternSuffix("Patterns"));
        assertEquals("Pattern of use", PatternUIUtils.stripPatternSuffix("Pattern of use"));
    }
}
