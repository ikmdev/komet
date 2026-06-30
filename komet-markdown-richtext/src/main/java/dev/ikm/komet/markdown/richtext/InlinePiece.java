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
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;

import java.util.function.Supplier;

/**
 * One rendered fragment of an inline content run: either a styled run of text or a deferred
 * inline JavaFX node (e.g. a concept chip). {@link InlineDecorator#decorate} returns a list of
 * these, so the same decorator output can be materialised into <em>both</em> a flowing
 * {@link RichParagraph} (via {@link #appendTo}) and a table cell's {@code TextFlow} (via the
 * renderer's cell builder, which switches on the piece type). This is the "piece-based decorator"
 * that lets concept chips render inside grid cells as well as in flowing text.
 *
 * <p>Every piece also carries a {@link #plainText()} projection used for copy / accessibility.
 */
public sealed interface InlinePiece permits InlinePiece.TextRun, InlinePiece.NodeRun {

    /** Append this piece to a flowing-paragraph builder. */
    void appendTo(RichParagraph.Builder builder);

    /** The plain-text projection of this piece (for copy / accessibility). */
    String plainText();

    /**
     * A styled run of text.
     *
     * @param text  the run text (never null; may be empty, in which case it is a no-op)
     * @param style the run style, or {@code null}/empty for the builder default
     */
    record TextRun(String text, StyleAttributeMap style) implements InlinePiece {
        @Override
        public void appendTo(RichParagraph.Builder builder) {
            if (text == null || text.isEmpty()) {
                return;
            }
            if (style == null || style.isEmpty()) {
                builder.addSegment(text);
            } else {
                builder.addSegment(text, style);
            }
        }

        @Override
        public String plainText() {
            return text == null ? "" : text;
        }
    }

    /**
     * A deferred inline node (the {@link Supplier} is invoked lazily when the paragraph is laid
     * out). The {@code plainText} is what copy / accessibility see in the node's place.
     *
     * @param node      the deferred node factory (never null)
     * @param plainText the plain-text projection (e.g. the underlying identifier or alt text)
     */
    record NodeRun(Supplier<Node> node, String plainText) implements InlinePiece {
        @Override
        public void appendTo(RichParagraph.Builder builder) {
            builder.addInlineNode(node);
        }

        @Override
        public String plainText() {
            return plainText == null ? "" : plainText;
        }
    }
}
