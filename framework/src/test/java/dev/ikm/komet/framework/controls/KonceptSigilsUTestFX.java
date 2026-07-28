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

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import network.ike.docs.konceptcore.KonceptKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The one place a component-kind sigil is built (ikmdev/komet#883). The badge and the drag glyph
 * both come through here, so a pattern cannot show its {@code P} in one renderer and nothing in the
 * other — the defect this factory was extracted to end.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptSigilsUTestFX {

    private static final double STAMP_SIZE = 14;

    @Test
    void aConceptStaysBare() {
        assertTrue(KonceptSigils.create(KonceptKind.CONCEPT, STAMP_SIZE, 0).isEmpty(),
                "a concept carries no sigil");
    }

    @Test
    void aNullKindIsTreatedAsAConcept() {
        assertTrue(KonceptSigils.create(null, STAMP_SIZE, 0).isEmpty());
    }

    @Test
    void aPatternGetsItsLetterFromTheKindItself() {
        Optional<Node> sigil = KonceptSigils.create(KonceptKind.PATTERN, STAMP_SIZE, 0);

        assertTrue(sigil.isPresent(), "a pattern carries a sigil");
        Text letter = assertInstanceOf(Text.class, sigil.get());
        // The glyph and colour are KonceptKind's to state; the factory must not restate them.
        assertEquals(KonceptKind.PATTERN.glyph(), letter.getText());
        assertEquals(Color.web(KonceptKind.PATTERN.colorHex()), letter.getFill());
    }

    @Test
    void everySigilBearingKindProducesItsOwnGlyph() {
        for (KonceptKind kind : KonceptKind.values()) {
            Optional<Node> sigil = KonceptSigils.create(kind, STAMP_SIZE, 0);
            assertEquals(kind.hasSigil(), sigil.isPresent(), kind + " sigil presence");
            if (sigil.isPresent() && !kind.isStamp()) {
                assertEquals(kind.glyph(), assertInstanceOf(Text.class, sigil.get()).getText(), kind + " glyph");
            }
        }
    }

    @Test
    void aStampGetsThePentagonNotALetter() {
        Optional<Node> sigil = KonceptSigils.create(KonceptKind.STAMP, STAMP_SIZE, 0);

        assertTrue(sigil.isPresent());
        assertInstanceOf(StampSigil.class, sigil.get());
    }

    @Test
    void anExplicitLetterSizeIsAppliedForRenderersCssCannotReach() {
        // The drag glyph snapshots in a throwaway scene with no stylesheet: a CSS-sized letter would
        // render at the default size there, which is the wrong-magnification half of #883.
        Text sized = assertInstanceOf(Text.class,
                KonceptSigils.create(KonceptKind.PATTERN, STAMP_SIZE, 15).orElseThrow());
        Text styled = assertInstanceOf(Text.class,
                KonceptSigils.create(KonceptKind.PATTERN, STAMP_SIZE, 0).orElseThrow());

        assertEquals(15, sized.getFont().getSize(), 0.001, "explicit size wins");
        assertFalse(styled.getStyleClass().isEmpty(), "size 0 leaves the glyph to the stylesheet");
    }

    @Test
    void theSigilIsBoldPerTheNormativeKonceptCss() {
        // koncept-asciidoc-extension's koncept.css — the stylesheet that renders the written badge
        // spec — declares the sigil font-weight: bold. Weight is data every medium agrees on.
        Text styled = assertInstanceOf(Text.class,
                KonceptSigils.create(KonceptKind.PATTERN, STAMP_SIZE, 0).orElseThrow());
        Text sized = assertInstanceOf(Text.class,
                KonceptSigils.create(KonceptKind.PATTERN, STAMP_SIZE, 15).orElseThrow());

        assertTrue(styled.getFont().getStyle().toLowerCase().contains("bold"),
                "stylesheet-sized sigil is bold");
        assertTrue(sized.getFont().getStyle().toLowerCase().contains("bold"),
                "explicitly-sized sigil is bold");
    }

    @Test
    void theAccessibleKindNameRidesAlongAsTheNonColourAffordance() {
        Node sigil = KonceptSigils.create(KonceptKind.PATTERN, STAMP_SIZE, 0).orElseThrow();

        assertTrue(sigil.getProperties().values().stream()
                        .anyMatch(value -> value instanceof javafx.scene.control.Tooltip tooltip
                                && KonceptKind.PATTERN.accessibleName().equals(tooltip.getText())),
                "colour alone must not carry the kind");
    }
}
