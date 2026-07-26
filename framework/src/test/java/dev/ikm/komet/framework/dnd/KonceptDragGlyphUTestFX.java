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
package dev.ikm.komet.framework.dnd;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the canonical concept drag glyph ({@code IKE-Network/ike-issues#854}): built store-free from
 * a {@link PublicId}, so every drag source that routes through it produces the identical pill. Uses
 * {@link PublicIds#newRandom()} — the identicon and pill are deterministic functions of the id, with
 * no store.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptDragGlyphUTestFX {

    private static final int ICON = 22;
    private static final int MAX_LABEL_WIDTH = 260;

    @Test
    void cornersAreOpaqueWhileDragViewAlphaIsFlattened() {
        // INTERIM assertion (ikmdev/komet#885). With a transparent fill, the four corners were
        // verified alpha-0 in the produced image — proving the white notches are manufactured
        // downstream, where the drag-view-to-native conversion flattens alpha. The interim keeps
        // the pill rounded and colour-treats the rectangle's corners (CORNER_TINT), so the image
        // is opaque edge to edge. When #885 resolves, restore Color.TRANSPARENT as the snapshot
        // fill and flip this test back to asserting transparent corners — the form that proves
        // the pipeline's alpha is correct.
        Image image = KonceptDragGlyph.image(PublicIds.newRandom(), "Chronic disease (disorder)", false);

        int w = (int) image.getWidth() - 1;
        int h = (int) image.getHeight() - 1;
        var reader = image.getPixelReader();
        assertTrue((reader.getArgb(0, 0) >>> 24) != 0, "top-left corner painted, no notch");
        assertTrue((reader.getArgb(w, 0) >>> 24) != 0, "top-right corner painted, no notch");
        assertTrue((reader.getArgb(0, h) >>> 24) != 0, "bottom-left corner painted, no notch");
        assertTrue((reader.getArgb(w, h) >>> 24) != 0, "bottom-right corner painted, no notch");
    }

    @Test
    void singleGlyphIsBuiltAtTheFixedPillHeight() {
        Image image = KonceptDragGlyph.image(PublicIds.newRandom(), "Chronic disease (disorder)", false);

        assertNotNull(image, "the glyph must always build");
        // Pill (identicon 22 + symmetric padding + border) plus the 3px outset frame on each side
        // (2px bevel + 1px reveal, ikmdev/komet#885 interim) — drop the frame from this window
        // when #885 resolves.
        assertTrue(image.getHeight() >= 36 && image.getHeight() <= 46,
                "fixed pill geometry in the outset frame, got " + image.getHeight());
        assertTrue(image.getWidth() > ICON, "the pill is wider than the identicon alone");
    }

    @Test
    void aLongNameEllipsisesToTheBoundedWidth() {
        PublicId pid = PublicIds.newRandom();
        String veryLong = "A really extraordinarily long concept name ".repeat(20);

        double longWidth = KonceptDragGlyph.image(pid, veryLong, false).getWidth();
        double shortWidth = KonceptDragGlyph.image(pid, "Short", false).getWidth();

        assertTrue(shortWidth < longWidth, "a short name hugs, a long name is wider");
        // Bounded: identicon + gaps + paddings + the capped label, never the full unbounded text.
        assertTrue(longWidth <= MAX_LABEL_WIDTH + ICON + 40,
                "a long name ellipsises at the width bound, got " + longWidth);
    }

    @Test
    void multiConceptGlyphAddsACountBadge() {
        PublicId pid = PublicIds.newRandom();
        double single = KonceptDragGlyph.image(pid, "Diabetes mellitus", false).getWidth();
        double multi = KonceptDragGlyph.multiImage(pid, "Diabetes mellitus", false, 3).getWidth();

        assertTrue(multi > single, "the count badge widens the glyph beyond the lead pill");
        // count == 1 shows no badge — identical to the single glyph.
        double multiOne = KonceptDragGlyph.multiImage(pid, "Diabetes mellitus", false, 1).getWidth();
        assertTrue(Math.abs(multiOne - single) < 0.5, "a single-item multi drag has no badge");
    }

    @Test
    void configurableBorderStillBuilds() {
        // Changing the border must never break the builder (the appearance is revisable).
        KonceptDragGlyph.setBorder(Color.web("#2F5FA6"), 2.0);
        assertNotNull(KonceptDragGlyph.image(PublicIds.newRandom(), "Body mass index", false));
        // Restore the SPEC default (the shared floating border, #861) — restoring anything else
        // would poison order-dependent parity assertions in this class.
        network.ike.docs.konceptcore.KonceptAppearance spec =
                network.ike.docs.konceptcore.KonceptAppearance.defaults();
        KonceptDragGlyph.setBorder(Color.web(spec.floatingBorderHex()), spec.floatingBorderWidthPx());
    }

    @Test
    void glyphEmbedsTheSharedAppearanceGoldenValues() {
        // The #865 parity gate for the drag medium: the rendered image carries the spec's pill
        // fill, floating border, and label colours — active and retired — pixel-verified.
        network.ike.docs.konceptcore.KonceptAppearance spec =
                network.ike.docs.konceptcore.KonceptAppearance.defaults();
        Image active = KonceptDragGlyph.image(
                network.ike.docs.konceptcore.KonceptKind.CONCEPT,
                dev.ikm.komet.framework.controls.KonceptStatus.PRIMITIVE,
                PublicIds.newRandom(), "Chronic disease (disorder)", false);
        assertTrue(hasColorNear(active, spec.pillFillHex(), 10), "spec pill fill");
        assertTrue(hasColorNear(active, spec.floatingBorderHex(), 20), "spec floating border");
        assertTrue(hasColorNear(active, spec.labelColorHex(), 60), "spec label colour");

        Image retired = KonceptDragGlyph.image(
                network.ike.docs.konceptcore.KonceptKind.CONCEPT,
                dev.ikm.komet.framework.controls.KonceptStatus.PRIMITIVE,
                PublicIds.newRandom(), "Chronic disease (disorder)", true);
        assertTrue(hasColorNear(retired, spec.labelColorInactiveHex(), 60),
                "the retired label colour (#742 parity)");
    }

    private static boolean hasColorNear(Image image, String hex, int tolerance) {
        Color target = Color.web(hex);
        int tr = (int) Math.round(target.getRed() * 255);
        int tg = (int) Math.round(target.getGreen() * 255);
        int tb = (int) Math.round(target.getBlue() * 255);
        var reader = image.getPixelReader();
        for (int y = 0; y < (int) image.getHeight(); y++) {
            for (int x = 0; x < (int) image.getWidth(); x++) {
                int argb = reader.getArgb(x, y);
                if ((argb >>> 24) > 30
                        && Math.abs(((argb >> 16) & 0xFF) - tr) <= tolerance
                        && Math.abs(((argb >> 8) & 0xFF) - tg) <= tolerance
                        && Math.abs((argb & 0xFF) - tb) <= tolerance) {
                    return true;
                }
            }
        }
        return false;
    }

    // Small-caps rendering (the bundled Alegreya Sans SC family) is NOT asserted headless: Monocle's
    // font pipeline returns null from Font.loadFont, so neither the load nor Font.getFamilies()
    // registration is exercisable here. SmallCapsFonts.family() therefore returns null under test and
    // the glyph falls back to all-caps — the true small-caps path is proven by the live app smoke.
}
