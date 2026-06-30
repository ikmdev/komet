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

import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the conversation Find search (case-insensitive, per-paragraph, toolkit-free). */
class RichTextSearchTest {

    private static MarkdownStyledModel model(String markdown) {
        List<RichParagraph> paragraphs = new ArrayList<>();
        List<String> plain = new ArrayList<>();
        new MarkdownRichTextRenderer(13, null).render(markdown, StyleAttributeMap.EMPTY, paragraphs, plain);
        return new MarkdownStyledModel(paragraphs, plain);
    }

    @Test
    void findsCaseInsensitivelyAcrossParagraphs() {
        MarkdownStyledModel m = model("# SARS Title\n\nDetects SARS-CoV-2 and sars markers.");
        List<RichTextSearch.Match> matches = RichTextSearch.findAll(m, "sars");
        // "SARS" (heading) + "SARS"(-CoV-2) + "sars" (paragraph) = 3, regardless of case.
        assertEquals(3, matches.size());
        assertEquals(0, matches.get(0).paragraphIndex(), "first match is in the heading");
        // Offsets map onto the paragraph plain text exactly.
        RichTextSearch.Match first = matches.get(0);
        assertEquals("SARS", m.getPlainText(0).substring(first.start(), first.end()));
    }

    @Test
    void nonOverlappingMatches() {
        MarkdownStyledModel m = model("aaaa");
        assertEquals(2, RichTextSearch.findAll(m, "aa").size());
    }

    @Test
    void emptyOrNullQueryYieldsNoMatches() {
        MarkdownStyledModel m = model("anything");
        assertTrue(RichTextSearch.findAll(m, "").isEmpty());
        assertTrue(RichTextSearch.findAll(m, null).isEmpty());
        assertTrue(RichTextSearch.findAll(null, "x").isEmpty());
    }

    @Test
    void matchesInsideTableProjection() {
        // A table paragraph is searched on its GFM projection.
        MarkdownStyledModel m = model("| Code | Name |\n| --- | --- |\n| 1 | SARS panel |");
        List<RichTextSearch.Match> matches = RichTextSearch.findAll(m, "sars");
        assertEquals(1, matches.size());
        assertEquals(m.size() - 1, matches.get(0).paragraphIndex(), "match is in the table paragraph");
    }
}
