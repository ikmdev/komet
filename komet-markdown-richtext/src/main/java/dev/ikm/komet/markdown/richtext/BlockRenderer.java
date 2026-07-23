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
 * Block-level counterpart of {@link InlineDecorator}: a hook for turning a fenced code block into
 * one or more rendered {@link BlockPiece}s (deferred JavaFX nodes) dispatched by the block's
 * <em>info string</em> — the language tag after the opening fence (e.g. {@code koncept-tree},
 * {@code dot}). This is the render-dispatch seam described in
 * {@code IKE-Network/ike-issues#801}: the assistant names a presentation from a closed menu and a
 * renderer owns the layout, rather than hand-drawing structure as ASCII inside a code block.
 *
 * <p>{@link MarkdownRichTextRenderer} passes every {@code FencedCodeBlock} to its renderer. A
 * renderer that recognises the info string returns the pieces to emit; a renderer that does
 * <em>not</em> recognise it returns an empty list, and the caller falls through to today's styled
 * preformatted rendering — so an unknown or absent tag never regresses and nothing is lost.
 *
 * <p>Like {@link InlineDecorator}, implementations are the tinkar-aware, JavaFX-node-producing
 * consumer's responsibility; this module stays generic and tinkar-free, seeing only the raw info
 * string, the block body, and a deferred {@code Supplier<Node>}. Implementations must be safe to
 * call from the JavaFX application thread.
 */
@FunctionalInterface
public interface BlockRenderer {

    /**
     * Renders a fenced code block, dispatching on its info string.
     *
     * @param info      the fenced block's info string (language tag), e.g. {@code "koncept-tree"};
     *                  may be {@code null} or blank when the block opened with a bare fence
     * @param literal   the raw block body (the text between the fences); never null
     * @param baseStyle the ambient base run style, for renderers that fall back to styled text
     * @return the ordered pieces to emit, or an <em>empty</em> list to decline this block (the
     *         caller then renders it as preformatted text); never null
     */
    List<BlockPiece> render(String info, String literal, StyleAttributeMap baseStyle);

    /** Default renderer: recognises nothing, so every fenced block falls through to preformatted. */
    BlockRenderer NONE = (info, literal, baseStyle) -> List.of();
}
