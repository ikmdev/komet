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

import jfx.incubator.scene.control.richtext.StyleResolver;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import jfx.incubator.scene.control.richtext.model.StyledTextModelViewOnlyBase;

import java.util.List;

/**
 * A view-only {@link jfx.incubator.scene.control.richtext.model.StyledTextModel} serving a
 * fixed list of pre-rendered {@link RichParagraph}s with a parallel plain-text projection
 * (for copy / accessibility). Build the paragraphs with {@link MarkdownRichTextRenderer}.
 */
public final class MarkdownStyledModel extends StyledTextModelViewOnlyBase {

    private final List<RichParagraph> paragraphs;
    private final List<String> plain;

    /**
     * @param paragraphs the rendered paragraphs (must be non-empty and the same size as
     *                   {@code plain}); a defensive copy is taken
     * @param plain      the parallel plain-text projection, one entry per paragraph
     */
    public MarkdownStyledModel(List<RichParagraph> paragraphs, List<String> plain) {
        this.paragraphs = List.copyOf(paragraphs);
        this.plain = List.copyOf(plain);
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

    @Override
    public StyleAttributeMap getStyleAttributeMap(StyleResolver resolver, TextPos pos) {
        return StyleAttributeMap.EMPTY;
    }
}
