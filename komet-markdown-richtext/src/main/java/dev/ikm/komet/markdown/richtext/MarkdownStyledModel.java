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

import javafx.scene.input.DataFormat;
import jfx.incubator.scene.control.richtext.StyleResolver;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.DataFormatHandler;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import jfx.incubator.scene.control.richtext.model.StyledInput;
import jfx.incubator.scene.control.richtext.model.StyledSegment;
import jfx.incubator.scene.control.richtext.model.StyledTextModelViewOnlyBase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A view-only {@link jfx.incubator.scene.control.richtext.model.StyledTextModel} serving a
 * fixed list of pre-rendered {@link RichParagraph}s with a parallel plain-text projection
 * (for copy / accessibility). Build the paragraphs with {@link MarkdownRichTextRenderer}.
 *
 * <p><b>Copying embedded blocks.</b> The incubator copies an embedded node (e.g. a rendered
 * table {@code GridPane}) as a single placeholder character — its export walks paragraph
 * <em>segments</em>, not {@link #getPlainText(int)}. So a table would otherwise copy as a blank.
 * This model registers a {@linkplain #registerDataFormatHandler higher-priority plain-text
 * handler} that exports each paragraph's {@link #getPlainText(int)} instead — which for a table
 * is its GFM serialization — so a normal select-and-copy yields the #596 interchange text. To
 * keep caret/selection math consistent with what is actually rendered, {@link #getParagraphLength}
 * reports the rendered length (a table paragraph is one node, length 1), independent of the longer
 * GFM copy text.
 */
public final class MarkdownStyledModel extends StyledTextModelViewOnlyBase {

    private final List<RichParagraph> paragraphs;
    private final List<String> plain;

    /**
     * @param paragraphs the rendered paragraphs (must be non-empty and the same size as
     *                   {@code plain}); a defensive copy is taken
     * @param plain      the parallel plain-text projection, one entry per paragraph (a table
     *                   paragraph's entry is its GFM serialization, so copy yields GFM)
     */
    public MarkdownStyledModel(List<RichParagraph> paragraphs, List<String> plain) {
        this.paragraphs = List.copyOf(paragraphs);
        this.plain = List.copyOf(plain);
        // Override the default plain-text export (priority 0) so copy uses per-paragraph
        // getPlainText — which carries the GFM for embedded table paragraphs.
        registerDataFormatHandler(new ParagraphPlainTextHandler(), true, false, 10);
    }

    /** An empty single-paragraph model. */
    public static MarkdownStyledModel empty() {
        return new MarkdownStyledModel(List.of(RichParagraph.builder().build()), List.of(""));
    }

    @Override
    public int size() {
        return paragraphs.size();
    }

    @Override
    public String getPlainText(int index) {
        return plain.get(index);
    }

    @Override
    public RichParagraph getParagraph(int index) {
        return paragraphs.get(index);
    }

    /**
     * Reports the <em>rendered</em> length of a paragraph (not the copy text). An atomic embedded
     * paragraph — a single inline node, e.g. a table {@code GridPane} — occupies one position,
     * even though its {@link #getPlainText(int)} is the much longer GFM table. Keeping this aligned
     * with the rendered content keeps caret navigation and selection correct.
     */
    @Override
    public int getParagraphLength(int index) {
        return isAtomic(index) ? 1 : getPlainText(index).length();
    }

    @Override
    public StyleAttributeMap getStyleAttributeMap(StyleResolver resolver, TextPos pos) {
        return StyleAttributeMap.EMPTY;
    }

    /**
     * True when the paragraph is a single embedded node (a table {@code GridPane}), whose copy
     * text ({@link #getPlainText(int)}) is a multi-line projection rather than rendered characters.
     */
    boolean isAtomic(int index) {
        RichParagraph p = paragraphs.get(index);
        return p.getSegmentCount() == 1
                && p.getSegment(0).getType() == StyledSegment.Type.INLINE_NODE;
    }

    /**
     * Builds the plain-text export for {@code [start, end]} from per-paragraph
     * {@link #getPlainText(int)}. An atomic (table) paragraph contributes its full GFM whenever the
     * selection touches it; other paragraphs are trimmed to the selected character offsets.
     */
    String exportPlainText(TextPos start, TextPos end) {
        String nl = getLineEnding().getText();
        StringBuilder sb = new StringBuilder();
        int last = Math.min(end.index(), size() - 1);
        for (int ix = start.index(); ix <= last; ix++) {
            String text = getPlainText(ix);
            if (isAtomic(ix)) {
                sb.append(text);
            } else {
                int from = (ix == start.index()) ? Math.min(start.offset(), text.length()) : 0;
                int to = (ix == end.index()) ? Math.min(end.offset(), text.length()) : text.length();
                if (from < to) {
                    sb.append(text, from, to);
                }
            }
            if (ix < last) {
                sb.append(nl);
            }
        }
        return sb.toString();
    }

    /**
     * Plain-text {@link DataFormatHandler} that exports per-paragraph {@link #getPlainText(int)}
     * (so embedded table paragraphs copy as GFM), replacing the incubator default whose
     * segment-walk would emit a single placeholder character for an embedded node. Export only.
     */
    private static final class ParagraphPlainTextHandler extends DataFormatHandler {
        ParagraphPlainTextHandler() {
            super(DataFormat.PLAIN_TEXT);
        }

        @Override
        public Object copy(jfx.incubator.scene.control.richtext.model.StyledTextModel model,
                           StyleResolver resolver, TextPos start, TextPos end) {
            return ((MarkdownStyledModel) model).exportPlainText(start, end);
        }

        @Override
        public void save(jfx.incubator.scene.control.richtext.model.StyledTextModel model,
                         StyleResolver resolver, TextPos start, TextPos end, OutputStream out)
                throws IOException {
            out.write(((MarkdownStyledModel) model).exportPlainText(start, end)
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        @Override
        public StyledInput createStyledInput(String text, StyleAttributeMap attr) {
            throw new UnsupportedOperationException("view-only model: import not supported");
        }
    }
}
