/*
 * Copyright © 2026 Integrated Knowledge Management (support@ikm.dev)
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
package dev.ikm.komet.markdown.richtext;

import jfx.incubator.scene.control.richtext.model.StyledTextModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Case-insensitive plain-text search over any {@link StyledTextModel}, returning per-paragraph
 * match positions a caller can turn into a {@code RichTextArea} selection. Matching uses
 * {@link String#regionMatches(boolean, int, String, int, int)} with {@code ignoreCase=true}, so it
 * is offset-exact (no {@code toLowerCase} length drift) — the returned {@code start}/{@code end} map
 * directly onto the paragraph's {@link StyledTextModel#getPlainText(int) plain text}.
 *
 * <p>An embedded paragraph (e.g. a rendered table) is searched on its plain-text projection (the
 * GFM table); the caller clamps offsets to the paragraph length so the match reveals the block.
 */
public final class RichTextSearch {

    private RichTextSearch() {
    }

    /**
     * One match: a half-open character range {@code [start, end)} within paragraph
     * {@code paragraphIndex}, as offsets into that paragraph's {@link StyledTextModel#getPlainText}.
     *
     * @param paragraphIndex the paragraph holding the match
     * @param start          the start offset (inclusive)
     * @param end            the end offset (exclusive)
     */
    public record Match(int paragraphIndex, int start, int end) {
    }

    /**
     * Finds every non-overlapping, case-insensitive occurrence of {@code query} across the model's
     * paragraphs, in document order.
     *
     * @param model the model to search (null yields no matches)
     * @param query the text to find (null/empty yields no matches)
     * @return an immutable list of matches in document order
     */
    public static List<Match> findAll(StyledTextModel model, String query) {
        List<Match> matches = new ArrayList<>();
        if (model == null || query == null || query.isEmpty()) {
            return List.copyOf(matches);
        }
        int qlen = query.length();
        for (int i = 0; i < model.size(); i++) {
            String hay = model.getPlainText(i);
            if (hay == null || hay.length() < qlen) {
                continue;
            }
            int p = 0;
            while (p + qlen <= hay.length()) {
                if (hay.regionMatches(true, p, query, 0, qlen)) {
                    matches.add(new Match(i, p, p + qlen));
                    p += qlen;
                } else {
                    p++;
                }
            }
        }
        return List.copyOf(matches);
    }
}
