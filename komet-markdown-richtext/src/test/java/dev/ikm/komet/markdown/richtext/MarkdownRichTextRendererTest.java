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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link MarkdownRichTextRenderer}'s block handling via its plain-text projection. These run
 * without a JavaFX toolkit because a table's {@code GridPane} (and inline image/chip nodes) are
 * built by deferred suppliers that {@code render()} never invokes — the scene graph itself is
 * exercised by running Komet, not here.
 */
class MarkdownRichTextRendererTest {

    private record Rendered(List<RichParagraph> paragraphs, List<String> plain) {
    }

    private static Rendered render(String markdown) {
        return render(markdown, null);
    }

    private static Rendered render(String markdown, BlockRenderer blockRenderer) {
        List<RichParagraph> out = new ArrayList<>();
        List<String> plain = new ArrayList<>();
        new MarkdownRichTextRenderer(13, null, blockRenderer).render(markdown, StyleAttributeMap.EMPTY, out, plain);
        return new Rendered(out, plain);
    }

    @Test
    void tableBecomesOneParagraphWithGfmProjection() {
        String md = "| A | B |\n| --- | --- |\n| 1 | 2 |\n| 3 | 4 |";
        Rendered r = render(md);

        // The whole table is a single paragraph (one inline GridPane), not one-paragraph-per-row.
        assertEquals(1, r.paragraphs().size(), "table should render as a single paragraph");

        String projection = r.plain().get(0);
        assertTrue(projection.contains("| A | B |"), "header row in GFM projection");
        assertTrue(projection.contains("| --- | --- |"), "delimiter row in GFM projection");
        assertTrue(projection.contains("| 1 | 2 |"), "body row in GFM projection");
    }

    @Test
    void tableProjectionHasNoBoxDrawingSeparator() {
        // Regression: the old renderer flattened tables to pipe text and emitted a "──────────"
        // line under the header. The GFM projection must contain neither.
        String md = "| A | B |\n| --- | --- |\n| 1 | 2 |";
        String projection = render(md).plain().get(0);
        assertFalse(projection.contains("─"), "no box-drawing separator line");
        assertFalse(projection.contains("  |  "), "no double-spaced flattened pipes");
    }

    @Test
    void inlineImageProducesNoBracketImageText() {
        // Regression: images used to render as the literal text "[image: ...]".
        String md = "Here ![alt text](http://example.com/x.png) inline.";
        Rendered r = render(md);
        String projection = String.join("\n", r.plain());
        assertFalse(projection.contains("[image"), "no literal [image: ...] text");
        assertTrue(projection.contains("alt text"), "image alt text kept in projection");
    }

    @Test
    void paragraphAndHeadingProjectAsText() {
        Rendered r = render("# Title\n\nA paragraph.");
        assertEquals(2, r.paragraphs().size());
        assertEquals("Title", r.plain().get(0));
        assertEquals("A paragraph.", r.plain().get(1));
    }

    @Test
    void fencedBlockFallsThroughToPreformattedWithoutBlockRenderer() {
        // With BlockRenderer.NONE (the 2-arg constructor), a fenced block renders as
        // preformatted text: one paragraph per line, projecting the raw code lines.
        String md = "```koncept-tree\nk:sctid=772222008[Medical devices]\n  k:sctid=118956008[Microbiology device]\n```";
        Rendered r = render(md);
        assertEquals(2, r.paragraphs().size(), "each code line is its own preformatted paragraph");
        assertEquals("k:sctid=772222008[Medical devices]", r.plain().get(0));
        assertEquals("  k:sctid=118956008[Microbiology device]", r.plain().get(1));
    }

    @Test
    void recognisedFenceDispatchesToBlockRenderer() {
        // A renderer that recognises "koncept-tree" turns the whole block into a single atomic
        // paragraph carrying its plain-text projection (the raw source, so copy round-trips).
        List<String> seenInfo = new ArrayList<>();
        BlockRenderer treeOnly = (info, literal, style) -> {
            seenInfo.add(info);
            if (!"koncept-tree".equals(info)) {
                return List.of();
            }
            return List.of(new BlockPiece(javafx.scene.control.Label::new, literal.stripTrailing()));
        };
        String md = "```koncept-tree\nk:sctid=772222008[Medical devices]\n```";
        Rendered r = render(md, treeOnly);

        assertEquals(List.of("koncept-tree"), seenInfo, "info string handed to the block renderer");
        assertEquals(1, r.paragraphs().size(), "recognised block collapses to one atomic paragraph");
        assertEquals("k:sctid=772222008[Medical devices]", r.plain().get(0),
                "block plain projection is the renderer-supplied source");
    }

    @Test
    void unrecognisedFenceFallsThroughDespiteBlockRenderer() {
        // A block renderer that declines (empty list) must not swallow the block: it falls
        // through to preformatted text exactly as if no renderer were present.
        BlockRenderer declines = (info, literal, style) -> List.of();
        String md = "```java\nint x = 1;\n```";
        Rendered r = render(md, declines);
        assertEquals(1, r.paragraphs().size());
        assertEquals("int x = 1;", r.plain().get(0), "declined block renders as preformatted code");
    }

    @Test
    void headerlessTableStillProjectsValidGfm() {
        // A table whose header cells are blank must still produce a delimiter row so it stays
        // valid GFM (and round-trips).
        String md = "|  |  |\n| --- | --- |\n| x | y |";
        String projection = render(md).plain().get(0);
        assertTrue(GfmTableParser.looksLikeTable(projection), "projection must be valid GFM");
        TableModel parsed = GfmTableParser.parse(projection);
        assertEquals(List.of("x", "y"), parsed.rows().get(0));
    }
}
