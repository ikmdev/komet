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

    /**
     * The words on the tab of a window that shows a semantic of a pattern: the pattern's name
     * without a trailing "Pattern" word, then the word semantic. "Test Performed Pattern" makes
     * "Test Performed semantic", so two semantic windows are told apart by their tabs. With no
     * name the tab reads "Semantic".
     *
     * <p>A tag in parentheses at the end of the name, as in "Model Class Pattern
     * (IkeFoundation)", is left off: a tab is read at a glance, and the window's header names
     * the pattern in full.
     *
     * @param patternName the pattern's name; may be {@code null} or blank
     * @return the tab's words
     */
    public static String semanticTabTitle(String patternName) {
        if (patternName == null || patternName.isBlank()) {
            return "Semantic";
        }
        return stripPatternSuffix(withoutTag(patternName)) + " semantic";
    }

    /** The name without a tag in parentheses at its end; a name that is nothing but a tag is kept whole. */
    private static String withoutTag(String name) {
        String trimmed = name.strip();
        int open = trimmed.lastIndexOf('(');
        boolean endsWithTag = trimmed.endsWith(")") && open > 0 && Character.isWhitespace(trimmed.charAt(open - 1));
        return endsWithTag ? trimmed.substring(0, open).strip() : trimmed;
    }
}
