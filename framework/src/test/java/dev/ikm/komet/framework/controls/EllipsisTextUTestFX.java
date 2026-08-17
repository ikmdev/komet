/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.text.BreakIterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks {@link EllipsisText}: a {@code Text} that ellipsises to the width layout allocates it —
 * the overrun a {@code Label} provides, kept on a {@code Text} node so the koncept badge name can
 * carry a real strikethrough ({@code IKE-Network/ike-issues#855}).
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class EllipsisTextUTestFX {

    private static final String NAME = "Chronic sinusitis with nasal polyps (disorder)";

    /** Lays the region out at the given width (its preferred height). */
    private static void layoutAt(EllipsisText node, double width) {
        node.resize(width, node.prefHeight(-1));
        node.layout();
    }

    @Test
    void theFullStringShowsWhenTheAllocatedWidthFits() {
        EllipsisText node = new EllipsisText();
        node.setText(NAME);
        layoutAt(node, node.prefWidth(-1));
        assertEquals(NAME, node.textNode().getText(), "an unconstrained name is never truncated");
    }

    @Test
    void aNarrowAllocationEllipsisesAndReWideningRestores() {
        EllipsisText node = new EllipsisText();
        node.setText(NAME);
        double full = node.prefWidth(-1);

        layoutAt(node, full / 3);
        String display = node.textNode().getText();
        assertNotEquals(NAME, display, "a narrow allocation must truncate");
        assertTrue(display.endsWith("…"), "the truncated form carries the ellipsis, got: " + display);
        Text probe = new Text(display);
        probe.setFont(node.textNode().getFont());
        assertTrue(probe.getLayoutBounds().getWidth() <= full / 3,
                "the displayed string fits the allocated width");

        // Re-widening restores the full string — the ellipsised display must not stick.
        layoutAt(node, full);
        assertEquals(NAME, node.textNode().getText(), "re-widening restores the full name");
    }

    @Test
    void thePreferredWidthAlwaysTracksTheFullString() {
        EllipsisText node = new EllipsisText();
        node.setText(NAME);
        double full = node.prefWidth(-1);
        layoutAt(node, full / 3);
        assertEquals(full, node.prefWidth(-1), 0.5,
                "pref width is the full string's width, independent of the ellipsised display");
        assertEquals(0, node.minWidth(-1), 0.5, "the region may shrink to nothing");
    }

    @Test
    void wrapModeShowsTheFullStringAndGrowsInHeight() {
        EllipsisText node = new EllipsisText();
        node.setText(NAME);
        node.setWrapText(true);
        double full = node.prefWidth(-1);

        assertEquals(javafx.geometry.Orientation.HORIZONTAL, node.getContentBias(),
                "wrap mode is height-for-width");
        layoutAt(node, full / 3);
        String display = node.textNode().getText();
        assertTrue(display.contains("\n"), "the narrowed string folds into explicit lines");
        assertEquals(NAME.replaceAll("\\s", ""), display.replaceAll("\\s", ""),
                "wrap mode never drops a character — the full string shows at any width");
        assertEquals(0, node.textNode().getWrappingWidth(), 0.001,
                "the semantic wrap owns the folding; the engine's whitespace wrap stays off");
        assertTrue(node.prefHeight(full / 3) > node.prefHeight(full) * 1.5,
                "a narrowed wrap-mode region grows in height (multiple lines)");
    }

    @Test
    void wrapBreaksAfterSemanticCharactersNotMidWord() {
        EllipsisText node = new EllipsisText();
        node.setText("Human Rhinovirus/Enterovirus");
        node.setWrapText(true);
        // A width that holds everything through the slash but not the whole name: the fold
        // must land AFTER the slash — never inside "Enterovirus" (the mid-word break KEC
        // flagged: "…/Enterovi / rus").
        Text probe = new Text("Human Rhinovirus/");
        probe.setFont(node.textNode().getFont());
        double width = probe.getLayoutBounds().getWidth() + 2;
        layoutAt(node, width);
        assertEquals("Human Rhinovirus/\nEnterovirus", node.textNode().getText(),
                "the fold lands after the slash, the semantic break");

        assertEquals("Flu A", EllipsisText.wrapToWidth("Flu A", probe.getFont(), 1_000),
                "a fitting string is returned unchanged");
        String forced = EllipsisText.wrapToWidth("Metapneumovirus", probe.getFont(), 40);
        assertTrue(forced.contains("\n"),
                "a single token with no break opportunity still force-folds, got: " + forced);
        assertEquals("Metapneumovirus", forced.replace("\n", ""),
                "the forced fold drops nothing");
    }

    @Test
    void isEllipsizedTracksTruncationAndIsNeverSetInWrapMode() {
        EllipsisText node = new EllipsisText();
        node.setText(NAME);
        assertTrue(node.getContentBias() == null, "single-line mode has no content bias");
        double full = node.prefWidth(-1);
        layoutAt(node, full);
        assertTrue(!node.isEllipsized(), "an unconstrained name is not ellipsized");

        layoutAt(node, full / 3);
        assertTrue(node.isEllipsized(), "a truncated name reports ellipsized — the expansion gate");

        layoutAt(node, full);
        assertTrue(!node.isEllipsized(), "re-widening clears the ellipsized state");

        node.setWrapText(true);
        layoutAt(node, full / 3);
        assertTrue(!node.isEllipsized(), "wrap mode shows the whole string, never ellipsized");
    }

    @Test
    void switchingModesRestoresAndReTruncates() {
        EllipsisText node = new EllipsisText();
        node.setText(NAME);
        double full = node.prefWidth(-1);

        layoutAt(node, full / 3);
        assertNotEquals(NAME, node.textNode().getText(), "starts truncated in the default mode");

        node.setWrapText(true);
        layoutAt(node, full / 3);
        assertEquals(NAME.replaceAll("\\s", ""), node.textNode().getText().replaceAll("\\s", ""),
                "entering wrap mode restores the full string, folded");

        node.setWrapText(false);
        layoutAt(node, full / 3);
        String display = node.textNode().getText();
        assertNotEquals(NAME, display, "leaving wrap mode truncates again");
        assertTrue(display.endsWith("…") && !display.contains("\n"),
                "the single-line mode is back: one ellipsised line");
    }

    @Test
    void aTrailingNodeFlowsAfterTheLastWrappedWord() {
        EllipsisText node = new EllipsisText();
        node.setText(NAME);
        node.setWrapText(true);
        double bare = node.prefWidth(-1);

        javafx.scene.layout.Region glyph = new javafx.scene.layout.Region();
        glyph.setMinSize(12, 12);
        glyph.setPrefSize(12, 12);
        glyph.setMaxSize(12, 12);
        node.setTrailingNode(glyph);

        assertTrue(node.prefWidth(-1) > bare + 12,
                "the preference reserves the trailing node and its gap on one line");

        double width = bare / 3;
        node.resize(width, node.prefHeight(width));
        node.layout();
        assertTrue(glyph.getLayoutX() >= 0 && glyph.getLayoutX() + 12 <= width + 0.5,
                "the trailing node stays inside the allocated width");
        double blockHeight = node.getHeight();
        assertTrue(glyph.getLayoutY() + 6 > blockHeight / 2,
                "the trailing node seats on the LAST line's band, in the block's lower half");

        node.setTrailingNode(null);
        assertEquals(bare, node.prefWidth(-1), 0.5, "removing the trailer restores the preference");
        assertTrue(node.getChildrenUnmodifiable().stream().noneMatch(child -> child == glyph),
                "the removed trailer leaves the children");
    }

    @Test
    void fitToWidthReturnsAFittingStringUnchanged() {
        Font font = Font.font(12);
        assertEquals("Short", EllipsisText.fitToWidth("Short", font, 1_000));
        assertEquals("", EllipsisText.fitToWidth("", font, 10), "empty stays empty");
    }

    @Test
    void fitToWidthTruncatesWithATrailingEllipsis() {
        Font font = Font.font(12);
        String fitted = EllipsisText.fitToWidth(NAME, font, 60);
        assertTrue(fitted.endsWith("…"), "got: " + fitted);
        assertTrue(fitted.length() < NAME.length());
        Text probe = new Text(fitted);
        probe.setFont(font);
        assertTrue(probe.getLayoutBounds().getWidth() <= 60, "the fitted string honours the bound");
    }

    @Test
    void belowTheEllipsisWidthNothingRenders() {
        // Matching Label's OverrunStyle: when not even "…" fits, render nothing — the region does
        // not clip, so an overflowing ellipsis would paint outside its bounds into the host.
        Font font = Font.font(12);
        assertEquals("", EllipsisText.fitToWidth(NAME, font, 2), "narrower than the ellipsis glyph");
        assertEquals("", EllipsisText.fitToWidth(NAME, font, 0), "zero width");
        assertEquals("", EllipsisText.fitToWidth("", font, -1), "negative width, empty string");
        assertEquals("", EllipsisText.fitToWidth(null, font, 100), "null is treated as empty");
    }

    @Test
    void fitToWidthNeverCutsInsideAGraphemeCluster() {
        Font font = Font.font(12);
        // A supplementary-plane character (surrogate pair) and a decomposed base + combining mark
        // (an explicit e + \u0301, never precomposed): sweep the width range so some cut lands
        // mid-cluster if the implementation allows it.
        String[] names = {"AB😀 chronic sinusitis with polyps", "Cafe\u0301 au lait spots (finding)"};
        BreakIterator graphemes = BreakIterator.getCharacterInstance();
        for (String name : names) {
            for (double width = 4; width <= 120; width += 0.5) {
                String fitted = EllipsisText.fitToWidth(name, font, width);
                for (int i = 0; i < fitted.length(); i++) {
                    if (Character.isHighSurrogate(fitted.charAt(i))) {
                        assertTrue(i + 1 < fitted.length() && Character.isLowSurrogate(fitted.charAt(i + 1)),
                                "lone high surrogate at " + i + " in \"" + fitted + "\" (width " + width + ")");
                    }
                }
                if (fitted.endsWith("…")) {
                    String prefix = fitted.substring(0, fitted.length() - 1);
                    graphemes.setText(name);
                    assertTrue(graphemes.isBoundary(prefix.length()),
                            "cut inside a grapheme cluster at " + prefix.length()
                                    + " for width " + width + ": \"" + fitted + "\"");
                }
            }
        }
    }
}
