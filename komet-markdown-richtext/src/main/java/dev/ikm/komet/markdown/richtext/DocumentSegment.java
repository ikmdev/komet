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

import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.model.RichParagraph;

import java.util.List;
import java.util.function.Supplier;

/**
 * One segment of a rendered Markdown document, as produced by
 * {@link MarkdownRichTextRenderer#renderSegments}: either a run of flowing prose paragraphs or a
 * single native block node. This is the seam a <em>block-stack</em> surface consumes — each
 * {@link ProseRun} backs one view-only {@code RichTextArea} and each {@link NodeBlock} becomes a
 * direct child of the stack — in contrast to {@link MarkdownRichTextRenderer#render}, which folds
 * everything (block nodes included, as atomic inline-node paragraphs) into one paragraph list for
 * a single {@code RichTextArea}.
 *
 * <p>Both variants carry a parallel plain-text projection for copy, search, and accessibility.
 */
public sealed interface DocumentSegment {

    /**
     * A run of flowing prose paragraphs between (or outside) native blocks.
     *
     * @param paragraphs the styled paragraphs, in document order
     * @param plain      the parallel plain-text projection, one entry per paragraph
     */
    record ProseRun(List<RichParagraph> paragraphs, List<String> plain) implements DocumentSegment {

        /**
         * Canonical constructor; both lists are defensively copied.
         *
         * @param paragraphs the styled paragraphs, in document order
         * @param plain      the parallel plain-text projection, one entry per paragraph
         */
        public ProseRun {
            paragraphs = List.copyOf(paragraphs);
            plain = List.copyOf(plain);
        }
    }

    /**
     * One native block — a recognised fenced block (e.g. a concept tree) or a GFM table —
     * rendered as a real node rather than text.
     *
     * @param node      the deferred node factory; invoked lazily, on the JavaFX thread, when the
     *                  block is materialised
     * @param plainText the block's plain-text projection (what a copy of the block yields)
     */
    record NodeBlock(Supplier<Node> node, String plainText) implements DocumentSegment {
    }
}
