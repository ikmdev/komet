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

import java.util.List;

/**
 * Hook for decomposing a Markdown <em>content</em> text run into {@link InlinePiece}s — styled
 * text and/or deferred inline nodes (e.g. concept chips). Returning pieces (rather than writing
 * straight into a {@link RichParagraph.Builder}) is what lets the same decorator output render in
 * <em>both</em> flowing text and inside table cells: {@link MarkdownRichTextRenderer} appends the
 * pieces to a paragraph for flowing text, and materialises them into a {@code TextFlow} for grid
 * cells.
 *
 * <p>{@link MarkdownRichTextRenderer} delegates every content run (paragraph, heading, list,
 * table-cell, and inline text/code) to a decorator. Structural text — horizontal rules and
 * fenced-code lines — is emitted directly by the renderer and is never passed through a decorator.
 *
 * <p>Implementations must be safe to call repeatedly and from the JavaFX application thread, and
 * must not <em>restyle</em> the text they are handed (they may split it and inject nodes, but text
 * pieces are expected to carry the supplied {@code style}); the cell renderer relies on this so it
 * can apply the ambient inline style to text within a cell.
 */
@FunctionalInterface
public interface InlineDecorator {

    /**
     * Decomposes {@code text} (to be styled with {@code style}) into a list of pieces, optionally
     * splitting it and injecting inline nodes between the text pieces.
     *
     * @param text  the text run to decorate (may be null/empty, in which case an empty list is fine)
     * @param style the style to apply to text pieces, or {@code null}/empty for the default
     * @return the ordered pieces (never null; may be empty)
     */
    List<InlinePiece> decorate(String text, StyleAttributeMap style);

    /**
     * Emits {@code text} into {@code builder} by appending each decorated piece. Provided so the
     * flowing-paragraph path stays a one-liner; equivalent to appending {@link #decorate}'s result.
     *
     * @param builder the paragraph under construction
     * @param text    the text run to emit (may be empty)
     * @param style   the style to apply, or {@code null}/empty for the builder default
     */
    default void emit(RichParagraph.Builder builder, String text, StyleAttributeMap style) {
        for (InlinePiece piece : decorate(text, style)) {
            piece.appendTo(builder);
        }
    }

    /** Default decorator: emit the run as a single styled text piece, injecting no inline nodes. */
    InlineDecorator PLAIN = (text, style) ->
            (text == null || text.isEmpty())
                    ? List.of()
                    : List.of(new InlinePiece.TextRun(text, style));
}
