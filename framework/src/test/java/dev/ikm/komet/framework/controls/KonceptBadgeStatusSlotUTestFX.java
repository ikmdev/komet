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
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the reserved status-cluster slot ({@code IKE-Network/ike-issues#1049}): opted in, every
 * badge reserves one constant width for the cluster ahead of its identicon — the widest cluster
 * any status can render — so identicons seat on one column down stacked sibling rows, whether a
 * row's cluster is one glyph, two, or absent. Off by default, a chip hugs its content, the
 * pre-#1049 behavior an inline chip in flowing prose keeps.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgeStatusSlotUTestFX {

    private static final String NAME = "Chronic sinusitis";

    /** The badge's status-cluster box: second child of the published anatomy. */
    private static HBox statusBox(KonceptBadge badge) {
        return (HBox) badge.getChildrenUnmodifiable().get(1);
    }

    private static KonceptBadge badge(KonceptStatus status, boolean reserved) {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        badge.setStatus(status);
        badge.setStatusSlotReserved(reserved);
        return badge;
    }

    @Test
    void huggingByDefaultAnEmptyClusterLeavesTheRow() {
        KonceptBadge badge = badge(KonceptStatus.NONE, false);
        assertFalse(statusBox(badge).isManaged(),
                "off by default: no cluster, no slot — an inline chip hugs its content");
        assertEquals(Region.USE_COMPUTED_SIZE, statusBox(badge).getMinWidth(),
                "no reservation constrains the hugging box");
    }

    @Test
    void reservedSlotHoldsOneWidthAcrossEveryStatus() {
        double reservedWidth = statusBox(badge(KonceptStatus.NONE, true)).getMinWidth();
        assertTrue(reservedWidth > 0, "the reserved slot has real width even with no cluster");
        for (KonceptStatus status : KonceptStatus.values()) {
            KonceptBadge badge = badge(status, true);
            assertEquals(reservedWidth, statusBox(badge).getMinWidth(), 0.01,
                    "one constant slot width for " + status);
            assertEquals(reservedWidth, statusBox(badge).getPrefWidth(), 0.01,
                    "pref matches min, so the slot neither shrinks nor stretches for " + status);
            assertTrue(statusBox(badge).isManaged(),
                    "the slot is kept — empty or not — for " + status);
        }
    }

    @Test
    void reservedSlotSeatsTheClusterAgainstTheIdenticon() {
        KonceptBadge badge = badge(KonceptStatus.PRIMITIVE, true);
        assertEquals(Pos.CENTER_RIGHT, statusBox(badge).getAlignment(),
                "the cluster right-aligns against the identicon it precedes — the adjacency is"
                        + " spatial, not just structural");
    }

    @Test
    void reservedSlotAlignsIdenticonsAcrossStackedSiblings() {
        KonceptBadge oneGlyph = badge(KonceptStatus.PRIMITIVE, true);
        KonceptBadge twoGlyphs = badge(KonceptStatus.PRIMITIVE_MULTIPARENT, true);
        KonceptBadge noCluster = badge(KonceptStatus.NONE, true);
        VBox column = new VBox(oneGlyph, twoGlyphs, noCluster);
        Scene scene = new Scene(column);
        column.applyCss();
        column.layout();

        double x1 = identiconX(oneGlyph);
        double x2 = identiconX(twoGlyphs);
        double x3 = identiconX(noCluster);
        assertEquals(x1, x2, 0.01, "one-glyph and two-glyph rows seat identicons on one column");
        assertEquals(x1, x3, 0.01, "a row with no cluster keeps the column too");
    }

    @Test
    void turningTheSlotOffRestoresHugging() {
        KonceptBadge badge = badge(KonceptStatus.NONE, true);
        badge.setStatusSlotReserved(false);
        assertFalse(statusBox(badge).isManaged(), "an empty cluster leaves the row again");
        assertEquals(Region.USE_COMPUTED_SIZE, statusBox(badge).getMinWidth());
        assertEquals(Region.USE_COMPUTED_SIZE, statusBox(badge).getPrefWidth());
    }

    /** The identicon's x within its badge; the badges share a column, so this is comparable. */
    private static double identiconX(KonceptBadge badge) {
        return badge.getChildrenUnmodifiable().get(2).getBoundsInParent().getMinX();
    }
}
