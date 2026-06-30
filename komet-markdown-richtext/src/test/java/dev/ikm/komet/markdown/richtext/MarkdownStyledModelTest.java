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

import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that an embedded table paragraph copies as GFM (not a blank placeholder), while caret
 * length stays consistent with the single rendered node. Toolkit-free: the table's {@code GridPane}
 * supplier is never invoked here.
 */
class MarkdownStyledModelTest {

    private static MarkdownStyledModel model(String markdown) {
        List<RichParagraph> paragraphs = new ArrayList<>();
        List<String> plain = new ArrayList<>();
        new MarkdownRichTextRenderer(13, null).render(markdown, StyleAttributeMap.EMPTY, paragraphs, plain);
        return new MarkdownStyledModel(paragraphs, plain);
    }

    @Test
    void tableParagraphIsAtomicWithDecoupledLength() {
        MarkdownStyledModel m = model("# Title\n\n| A | B |\n| --- | --- |\n| 1 | 2 |");
        int tableIx = m.size() - 1;

        assertTrue(m.isAtomic(tableIx), "the table paragraph is a single embedded node");
        assertFalse(m.isAtomic(0), "the heading paragraph is not atomic");

        // Caret length is 1 (one rendered node), but the copy text is the much longer GFM.
        assertEquals(1, m.getParagraphLength(tableIx));
        assertTrue(m.getPlainText(tableIx).length() > 1);
    }

    @Test
    void copyExportsGfmForTheTable() {
        MarkdownStyledModel m = model("# Title\n\n| A | B |\n| --- | --- |\n| 1 | 2 |");
        String copied = m.exportPlainText(TextPos.ZERO, m.getDocumentEnd());

        assertTrue(copied.contains("Title"), "heading copied");
        assertTrue(copied.contains("| A | B |"), "header row copied as GFM");
        assertTrue(copied.contains("| --- | --- |"), "delimiter row copied as GFM");
        assertTrue(copied.contains("| 1 | 2 |"), "body row copied as GFM");
    }

    @Test
    void copyTrimsNonAtomicParagraphToSelection() {
        // Selecting just "Tit" out of the "Title" heading copies only that slice.
        MarkdownStyledModel m = model("# Title\n\nbody");
        String copied = m.exportPlainText(TextPos.ZERO, TextPos.ofLeading(0, 3));
        assertEquals("Tit", copied);
    }
}
