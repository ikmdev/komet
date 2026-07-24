/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
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
package dev.ikm.komet.framework.controls;

import dev.ikm.komet.framework.StyleClasses;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import network.ike.docs.konceptcore.KonceptKind;

import java.util.Optional;

/**
 * Builds the component-kind sigil, for every renderer that shows one (ikmdev/komet#883, under the
 * KonceptAppearance spec of #742).
 *
 * <p>{@link KonceptKind} already owns the <em>mapping</em> — which kinds carry a sigil, each one's
 * glyph, colour, and accessible name. What this owns is the <em>node</em>: a concept stays bare, a
 * {@link KonceptKind#STAMP} gets the {@link StampSigil} pentagon, every other kind gets its coloured
 * letter, and the accessible kind name is always installed as the non-colour affordance. Renderers
 * call this instead of assembling those pieces themselves, so a sigil cannot come out different
 * depending on where it is drawn.
 *
 * <p>Two renderers with genuinely different mechanics consume it: {@link KonceptBadge} is a laid-out
 * control styled by the stylesheet, while
 * {@link dev.ikm.komet.framework.dnd.KonceptDragGlyph} snapshots into fixed integer geometry in a
 * throwaway scene that has no stylesheet attached. Hence {@code letterSizePx}: a renderer that CSS
 * reaches passes {@code 0} and lets the stylesheet size the glyph; one that CSS does not reach sizes
 * it explicitly, and still gets the same letter, colour, and name.
 */
public final class KonceptSigils {

    private KonceptSigils() {
    }

    /**
     * The sigil node for {@code kind}, or empty when the kind carries none.
     *
     * @param kind         the component kind; {@code null} is treated as {@link KonceptKind#CONCEPT}
     * @param stampSizePx  edge length of the {@link KonceptKind#STAMP} pentagon, in px
     * @param letterSizePx font size of the letter glyph in px, or {@code 0} to leave it to the
     *                     stylesheet — use a positive value only where CSS does not reach
     * @return the sigil node with its accessible-name tooltip installed, or empty for a bare concept
     */
    public static Optional<Node> create(KonceptKind kind, double stampSizePx, double letterSizePx) {
        KonceptKind resolved = (kind == null) ? KonceptKind.CONCEPT : kind;
        if (!resolved.hasSigil()) {
            return Optional.empty();
        }
        Node sigil = resolved.isStamp()
                ? new StampSigil(stampSizePx)
                : letterSigil(resolved, letterSizePx);
        // The accessible name is the non-colour affordance: colour alone never carries the kind.
        Tooltip.install(sigil, new Tooltip(resolved.accessibleName()));
        return Optional.of(sigil);
    }

    /**
     * The coloured letter glyph for a non-stamp kind.
     *
     * @param kind         the component kind
     * @param letterSizePx the font size in px, or {@code 0} to leave sizing to the stylesheet
     * @return the letter node
     */
    private static Text letterSigil(KonceptKind kind, double letterSizePx) {
        Text glyph = new Text(kind.glyph());
        glyph.getStyleClass().add(StyleClasses.KONCEPT_SIGIL.toString());
        glyph.setFill(Color.web(kind.colorHex()));
        // Bold always: the normative koncept.css (koncept-asciidoc-extension — the stylesheet that
        // renders the written badge spec) declares the sigil {@code font-weight: bold}, mirroring
        // the styles the doc renderer emits inline. Weight, like glyph and colour, is data every
        // medium agrees on, so it is set in code rather than left to whichever stylesheet loads.
        glyph.setFont(Font.font(glyph.getFont().getFamily(), FontWeight.BOLD,
                letterSizePx > 0 ? letterSizePx : glyph.getFont().getSize()));
        return glyph;
    }
}
