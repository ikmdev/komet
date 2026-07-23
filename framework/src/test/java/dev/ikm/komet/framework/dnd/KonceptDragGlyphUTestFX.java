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
    void singleGlyphIsBuiltAtTheFixedPillHeight() {
        Image image = KonceptDragGlyph.image(PublicIds.newRandom(), "Chronic disease (disorder)", false);

        assertNotNull(image, "the glyph must always build");
        assertTrue(image.getHeight() >= 28 && image.getHeight() <= 40,
                "fixed pill geometry: identicon(22) + symmetric padding + border, got " + image.getHeight());
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
        KonceptDragGlyph.setBorder(Color.web("#6E9BD1"), 1.5); // restore default subtle blue
    }

    // Small-caps rendering (the bundled Alegreya Sans SC family) is NOT asserted headless: Monocle's
    // font pipeline returns null from Font.loadFont, so neither the load nor Font.getFamilies()
    // registration is exercisable here. SmallCapsFonts.family() therefore returns null under test and
    // the glyph falls back to all-caps — the true small-caps path is proven by the live app smoke.
}
