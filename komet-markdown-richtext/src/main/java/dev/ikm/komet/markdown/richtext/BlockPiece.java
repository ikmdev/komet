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
 * One rendered block produced by a {@link BlockRenderer} from a fenced code block: a deferred
 * atomic JavaFX node (built lazily when the paragraph is laid out) paired with the plain-text
 * projection copy / accessibility should see in the node's place.
 *
 * <p>The block-level counterpart of {@link InlinePiece.NodeRun}: {@link MarkdownRichTextRenderer}
 * materialises each piece as one {@link RichParagraph} carrying a single inline node (the same
 * shape as a rendered GFM table), keeping the paragraph atomic so
 * {@link MarkdownStyledModel#isAtomic(int)} treats it as one unit. The plain projection is added
 * to the parallel plain-text list, so a {@code RichTextArea} copy yields a faithful text form of
 * the block (e.g. the original fenced source) rather than an empty line.
 *
 * @param node      the deferred node factory (never null)
 * @param plainText the plain-text projection of the block (may be multi-line); the source the
 *                  renderer was handed is a sensible default, so copy round-trips
 */
public record BlockPiece(Supplier<Node> node, String plainText) {

    /**
     * The plain-text projection (custom accessor: never returns null).
     *
     * @return the plain-text projection, or the empty string if none was supplied
     */
    public String plainText() {
        return plainText == null ? "" : plainText;
    }
}
