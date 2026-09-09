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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

/**
 * How pattern titles read in the window's own labels. Pure string work.
 */
public final class PatternUIUtils {

    private PatternUIUtils() {
    }

    /**
     * The passed in name without a trailing "Pattern" word — "Description Pattern" makes the
     * create entry "Add Description". A name that is nothing but that word is kept whole.
     */
    public static String stripPatternSuffix(String name) {
        String trimmed = name.strip();
        int suffixStart = trimmed.length() - "Pattern".length();
        boolean endsWithPatternWord = suffixStart > 0
                && Character.isWhitespace(trimmed.charAt(suffixStart - 1))
                && trimmed.regionMatches(true, suffixStart, "Pattern", 0, "Pattern".length());
        return endsWithPatternWord ? trimmed.substring(0, suffixStart).strip() : trimmed;
    }
}
