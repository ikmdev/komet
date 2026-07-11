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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link MarkdownRichTextRenderer#renderSegments} — the block-stack projection. Runs without
 * a JavaFX toolkit: node suppliers are never invoked (the {@link MarkdownRichTextRendererTest}
 * discipline).
 */
class MarkdownSegmentsTest {

    /** A block renderer that accepts only the {@code widget} tag, emitting one node piece. */
    private static final BlockRenderer WIDGET_ONLY = (info, literal, style) ->
            "widget".equals(info == null ? null : info.strip())
                    ? List.of(new BlockPiece(() -> null, "[widget " + literal.strip() + "]"))
                    : List.of();

    private static List<DocumentSegment> segments(String markdown) {
        return new MarkdownRichTextRenderer(13, null, WIDGET_ONLY)
                .renderSegments(markdown, StyleAttributeMap.EMPTY);
    }

    @Test
    void proseOnlyIsOneProseRun() {
        List<DocumentSegment> segs = segments("First paragraph.\n\nSecond **bold** paragraph.");
        assertEquals(1, segs.size(), "prose-only document is one segment");
        DocumentSegment.ProseRun run = assertInstanceOf(DocumentSegment.ProseRun.class, segs.get(0));
        assertEquals(2, run.paragraphs().size(), "two paragraphs in the run");
        assertEquals(run.paragraphs().size(), run.plain().size(), "plain projection is parallel");
        assertEquals("First paragraph.", run.plain().get(0));
    }

    @Test
    void acceptedFenceSplitsProse() {
        List<DocumentSegment> segs = segments("""
                Before.

                ```widget
                payload
                ```

                After.
                """);
        assertEquals(3, segs.size(), "prose, node, prose");
        assertInstanceOf(DocumentSegment.ProseRun.class, segs.get(0));
        DocumentSegment.NodeBlock node = assertInstanceOf(DocumentSegment.NodeBlock.class, segs.get(1));
        assertEquals("[widget payload]", node.plainText());
        DocumentSegment.ProseRun after = assertInstanceOf(DocumentSegment.ProseRun.class, segs.get(2));
        assertEquals("After.", after.plain().get(0));
    }

    @Test
    void declinedFenceStaysInProse() {
        List<DocumentSegment> segs = segments("""
                Before.

                ```java
                int x = 1;
                ```

                After.
                """);
        assertEquals(1, segs.size(), "declined fence never splits the prose run");
        DocumentSegment.ProseRun run = assertInstanceOf(DocumentSegment.ProseRun.class, segs.get(0));
        assertTrue(run.plain().contains("int x = 1;"), "fence body preserved as preformatted prose");
    }

    @Test
    void topLevelTableBecomesNodeBlock() {
        List<DocumentSegment> segs = segments("Intro.\n\n| A | B |\n| --- | --- |\n| 1 | 2 |");
        assertEquals(2, segs.size(), "prose then table node");
        DocumentSegment.NodeBlock table = assertInstanceOf(DocumentSegment.NodeBlock.class, segs.get(1));
        assertTrue(table.plainText().contains("| A | B |"), "GFM projection carried on the node block");
    }

    @Test
    void leadingNodeBlockAndTrailingProse() {
        List<DocumentSegment> segs = segments("```widget\nx\n```\n\nTail.");
        assertEquals(2, segs.size());
        assertInstanceOf(DocumentSegment.NodeBlock.class, segs.get(0));
        assertInstanceOf(DocumentSegment.ProseRun.class, segs.get(1));
    }

    @Test
    void emptyAndNullInputYieldNoSegments() {
        assertTrue(segments("").isEmpty(), "empty source renders to nothing");
        assertTrue(new MarkdownRichTextRenderer(13, null, WIDGET_ONLY)
                .renderSegments(null, StyleAttributeMap.EMPTY).isEmpty(), "null source renders to nothing");
    }

    @Test
    void renderingMatchesRenderForProse() {
        // The packaging differs; the rendered prose must not. Compare plain projections.
        String md = "A *b* c.\n\n- one\n- two";
        List<RichParagraph> out = new ArrayList<>();
        List<String> plain = new ArrayList<>();
        MarkdownRichTextRenderer renderer = new MarkdownRichTextRenderer(13, null, WIDGET_ONLY);
        renderer.render(md, StyleAttributeMap.EMPTY, out, plain);

        List<DocumentSegment> segs = renderer.renderSegments(md, StyleAttributeMap.EMPTY);
        DocumentSegment.ProseRun run = assertInstanceOf(DocumentSegment.ProseRun.class, segs.get(0));
        assertEquals(plain, run.plain(), "segment prose projection identical to render()");
        assertEquals(out.size(), run.paragraphs().size(), "same paragraph count");
    }
}
