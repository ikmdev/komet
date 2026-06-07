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

/**
 * Hook for emitting a Markdown <em>content</em> text run into a {@link RichParagraph.Builder},
 * with the opportunity to inject inline nodes (e.g. concept chips) alongside the styled text.
 *
 * <p>{@link MarkdownRichTextRenderer} delegates every content run (paragraph, heading, list,
 * table-cell, and inline text/code) to a decorator. Structural text — table separators,
 * horizontal rules, fenced-code lines — is emitted directly by the renderer and is never
 * passed through a decorator.
 *
 * <p>Implementations must be safe to call repeatedly and from the JavaFX application thread.
 */
@FunctionalInterface
public interface InlineDecorator {

    /**
     * Emits {@code text} (styled with {@code style}) into {@code builder}, optionally adding
     * inline nodes via {@link RichParagraph.Builder#addInlineNode(java.util.function.Supplier)}.
     *
     * @param builder the paragraph under construction
     * @param text    the text run to emit (may be empty)
     * @param style   the style to apply, or {@code null}/empty for the builder default
     */
    void emit(RichParagraph.Builder builder, String text, StyleAttributeMap style);

    /** Default decorator: emit the run as a single styled segment, injecting no inline nodes. */
    InlineDecorator PLAIN = (builder, text, style) -> {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (style == null || style.isEmpty()) {
            builder.addSegment(text);
        } else {
            builder.addSegment(text, style);
        }
    };
}
