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
package dev.ikm.komet.framework.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the {@code <B>...</B>} markup that the Lucene
 * {@code SimpleHTMLFormatter} emits in highlighted search snippets.
 *
 * <p>Two flavors of consumer exist in this workspace and the helper serves
 * both via the same parse output:
 * <ul>
 *   <li>The classic search panel cell renders flat {@code Text} runs in a
 *       {@code TextFlow}, styled with the {@code SEARCH_MATCH} /
 *       {@code SEARCH_NOT_MATCHED} CSS classes.</li>
 *   <li>The next-gen search cells in {@code kview} render per-word
 *       {@code StackPane(Text)} wrappers, styled with the
 *       {@code .word-container} and {@code .word-container.highlight} CSS
 *       classes. They split on whitespace first and ask whether each word
 *       carries any highlight markup; for that case use
 *       {@link #containsMarkup(String)} and {@link #stripMarkup(String)}.</li>
 * </ul>
 *
 * <p>The helper does not render anything itself — rendering decisions
 * (element type, CSS classes, container wrapping) stay with the cell. This
 * is purely a parser.
 */
public final class HighlightedSegments {
    private static final String OPEN = "<B>";
    private static final String CLOSE = "</B>";

    private HighlightedSegments() {
    }

    /**
     * One run of text in a parsed marked-up string. Adjacent segments differ
     * in {@link #matched()} — the parser collapses neighboring same-flag
     * segments so a caller never sees two consecutive matched (or two
     * consecutive unmatched) entries.
     *
     * @param text    the literal text content with markup stripped
     * @param matched true if this run was wrapped in {@code <B>...</B>}
     */
    public record Segment(String text, boolean matched) {
    }

    /**
     * Parse a marked-up string into match-segment granularity. A malformed
     * input with an unclosed {@code <B>} treats the trailing text as matched
     * up to end-of-input — matching the classic {@code SearchResultCell}
     * fallback behavior. Empty matched segments (e.g. {@code <B></B>}) are
     * dropped; empty unmatched runs between adjacent matches are likewise
     * dropped.
     *
     * @param markedUpText the input string, possibly null or empty
     * @return ordered list of segments; empty list for null/blank input
     */
    public static List<Segment> parse(String markedUpText) {
        List<Segment> out = new ArrayList<>();
        if (markedUpText == null || markedUpText.isEmpty()) {
            return out;
        }
        int cursor = 0;
        while (cursor < markedUpText.length()) {
            int openAt = markedUpText.indexOf(OPEN, cursor);
            if (openAt < 0) {
                addIfNonEmpty(out, markedUpText.substring(cursor), false);
                break;
            }
            addIfNonEmpty(out, markedUpText.substring(cursor, openAt), false);
            int contentStart = openAt + OPEN.length();
            int closeAt = markedUpText.indexOf(CLOSE, contentStart);
            if (closeAt < 0) {
                // Malformed: treat the rest as matched.
                addIfNonEmpty(out, markedUpText.substring(contentStart), true);
                break;
            }
            addIfNonEmpty(out, markedUpText.substring(contentStart, closeAt), true);
            cursor = closeAt + CLOSE.length();
        }
        return out;
    }

    /**
     * Remove all {@code <B>} and {@code </B>} markers, leaving plain text.
     * Suitable for sort keys and any path that wants the search snippet as
     * plain text without losing punctuation or whitespace.
     *
     * @param markedUpText the input, possibly null
     * @return the input with markers removed; empty string for null input
     */
    public static String stripMarkup(String markedUpText) {
        if (markedUpText == null) {
            return "";
        }
        return markedUpText.replace(OPEN, "").replace(CLOSE, "");
    }

    /**
     * @param s the string to test, possibly null
     * @return true iff {@code s} contains an opening {@code <B>} marker
     */
    public static boolean containsMarkup(String s) {
        return s != null && s.contains(OPEN);
    }

    private static void addIfNonEmpty(List<Segment> out, String text, boolean matched) {
        if (!text.isEmpty()) {
            out.add(new Segment(text, matched));
        }
    }
}
