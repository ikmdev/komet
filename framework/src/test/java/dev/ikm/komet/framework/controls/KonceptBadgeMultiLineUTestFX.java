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

import network.ike.docs.konceptcore.KonceptKind;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the badge's multi-line rendering ({@code IKE-Network/ike-issues#1036}): the contextual
 * form of the label-fidelity rule. A width-constrained badge in the default mode ellipsises its
 * name (the expansion gate); the same badge in the multi-line mode wraps the whole name and grows
 * in height; and {@link KonceptBadge#expandedRendering(double)} builds the multi-line twin — the
 * node the identity tooltip and the click-to-expand popover show — carrying the badge's displayed
 * state with its width capped so the name folds.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgeMultiLineUTestFX {

    private static final String LONG_NAME =
            "Severe chronic obstructive pulmonary disease with acute lower respiratory infection";

    /** The badge's name region ({@link EllipsisText}) — the parent of the .koncept-label text. */
    private static EllipsisText nameRegion(KonceptBadge badge) {
        Text name = (Text) badge.lookup(".koncept-label");
        assertNotNull(name, "the badge name is a .koncept-label Text node");
        return (EllipsisText) name.getParent();
    }

    /** Lays the badge out at the given width (its preferred height for that width). */
    private static void layoutAt(KonceptBadge badge, double width) {
        badge.resize(width, badge.prefHeight(width));
        badge.layout();
    }

    @Test
    void aConstrainedDefaultBadgeEllipsisesAndGatesTheExpansion() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), LONG_NAME);
        EllipsisText name = nameRegion(badge);
        double full = badge.prefWidth(-1);

        layoutAt(badge, full);
        assertFalse(name.isEllipsized(), "an unconstrained badge shows the whole name");

        layoutAt(badge, full / 3);
        assertTrue(name.isEllipsized(),
                "a width-constrained badge truncates — the state the click-to-expand gate reads");
    }

    @Test
    void theMultiLineBadgeWrapsTheWholeNameAndGrowsInHeight() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), LONG_NAME);
        badge.setMultiLineLabel(true);
        assertTrue(badge.isMultiLineLabel());
        EllipsisText name = nameRegion(badge);
        assertTrue(name.isWrapText(), "the multi-line badge puts its name region in wrap mode");
        assertEquals(javafx.geometry.Pos.TOP_LEFT, badge.getAlignment(),
                "companions seat on the first line — sigil and identicon upper-left, "
                        + "popout upper-right — never beside the block's middle");
        javafx.scene.Node identicon = badge.lookup(".koncept-identicon");
        assertNotNull(identicon);
        javafx.geometry.Insets margin = javafx.scene.layout.HBox.getMargin(identicon);
        assertTrue(margin == null || margin.getTop() < badge.prefHeight(200) / 2,
                "the identicon's seating margin stays within the first line's band");
        badge.setMultiLineLabel(false);
        assertEquals(javafx.geometry.Pos.CENTER_LEFT, badge.getAlignment(),
                "the single-line badge keeps its centre seating (ikmdev/komet#883)");
        assertEquals(null, javafx.scene.layout.HBox.getMargin(identicon),
                "centre seating clears the first-line margins");
        badge.setMultiLineLabel(true);

        double full = badge.prefWidth(-1);
        double oneLine = badge.prefHeight(full);
        layoutAt(badge, full / 3);
        assertEquals(name.getText().replaceAll("\\s", ""),
                name.textNode().getText().replaceAll("\\s", ""),
                "the multi-line badge never truncates its name — it folds");
        assertFalse(name.isEllipsized(), "wrapping is not truncation — the gate stays closed");
        assertTrue(badge.prefHeight(full / 3) > oneLine * 1.5,
                "the narrowed multi-line badge grows in height (the name folds into lines)");
    }

    @Test
    void expandedRenderingCarriesTheDisplayedStateMultiLine() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), LONG_NAME);
        badge.setKind(KonceptKind.PATTERN);
        badge.setStatus(KonceptStatus.DEFINED);
        badge.setInactive(true);
        badge.setSctid("73211009");

        KonceptBadge expanded = badge.expandedRendering(340);

        assertTrue(expanded.isMultiLineLabel(), "the expansion is the multi-line rendering");
        assertEquals(KonceptKind.PATTERN, expanded.getKind(), "kind honesty carries over");
        assertEquals(KonceptStatus.DEFINED, expanded.getStatus(), "the status cluster carries over");
        assertTrue(expanded.isInactive(), "the retired state carries over");
        assertEquals(340, expanded.getMaxWidth(), 0.001, "the wrap cap bounds the expansion");
        assertEquals(340, expanded.getPrefWidth(), 0.001,
                "a name wider than the cap folds: the preference is the cap, not the one-line width");
        assertEquals(nameRegion(badge).getText(), nameRegion(expanded).getText(),
                "the expansion names the same concept, in full");
    }

    @Test
    void aShortExpansionKeepsItsNaturalWidth() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), "Flu A");
        KonceptBadge expanded = badge.expandedRendering(340);
        assertEquals(Region.USE_COMPUTED_SIZE, expanded.getPrefWidth(), 0.001,
                "a name narrower than the cap keeps its natural pill width — no stretched pill");
    }

    @Test
    void firstLineSeatingSurvivesKometCss() throws Exception {
        // komet.css pins `.koncept-chip { -fx-alignment: center-left; }`, and an author
        // stylesheet outranks a setAlignment call — the exact silent defeat that shipped the
        // centre-floating companions. The badge's inline style layer must outrank the
        // stylesheet in turn, in every scene.
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), LONG_NAME);
        badge.setMultiLineLabel(true);

        java.nio.file.Path css = java.nio.file.Files.createTempFile("komet", ".css");
        try (java.io.InputStream in = KonceptBadge.class.getResourceAsStream(
                "/dev/ikm/komet/framework/graphics/komet.css")) {
            java.nio.file.Files.copy(in, css, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        css.toFile().deleteOnExit();
        javafx.scene.Scene scene = new javafx.scene.Scene(new javafx.scene.Group(badge));
        scene.getStylesheets().add(css.toUri().toString());
        badge.applyCss();

        assertEquals(javafx.geometry.Pos.TOP_LEFT, badge.getAlignment(),
                "the inline alignment layer outranks komet.css's center-left");

        badge.setMultiLineLabel(false);
        badge.applyCss();
        assertEquals(javafx.geometry.Pos.CENTER_LEFT, badge.getAlignment(),
                "single-line seating agrees with the stylesheet again");
    }

    @Test
    void styleLayersCompose() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), LONG_NAME);
        badge.setStandaloneStyling(true);
        badge.setMultiLineLabel(true);
        assertTrue(badge.getStyle().contains("-fx-background-color"),
                "the standalone pill layer survives the multi-line layer");
        assertTrue(badge.getStyle().contains("-fx-alignment: top-left"),
                "the multi-line layer survives the standalone pill layer");
        badge.setMultiLineLabel(false);
        assertTrue(badge.getStyle().contains("-fx-background-color")
                        && !badge.getStyle().contains("top-left"),
                "leaving multi-line keeps the pill and drops only the alignment");
    }
}
