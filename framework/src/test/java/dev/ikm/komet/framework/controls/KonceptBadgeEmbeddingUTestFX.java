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
import dev.ikm.tinkar.common.id.PublicIds;
import network.ike.docs.konceptcore.KonceptKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The three capabilities that let a host embed the koncept atom instead of hand-rolling its own
 * identicon + label (ikmdev/komet#742): ambient font scaling, stylesheet-independent styling, and a
 * text baseline. Each was a blocker for the assistant's compose chip, and each is what a future
 * embedder will need too — so they are pinned rather than left to the one caller.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgeEmbeddingUTestFX {

    private static KonceptBadge badge() {
        return new KonceptBadge(PublicIds.newRandom(), "Chronic disease (disorder)");
    }

    @Test
    void ambientFontSizeScalesTheName() {
        KonceptBadge small = badge();
        KonceptBadge large = badge();

        small.setAmbientFontSize(12);
        large.setAmbientFontSize(24);

        assertTrue(large.getNameFontSize() > small.getNameFontSize(),
                "a host with its own font control must be able to scale the atom");
        assertEquals(2.0, large.getNameFontSize() / small.getNameFontSize(), 0.001,
                "scaling is proportional to the ambient size");
    }

    @Test
    void anAmbientSizeOfZeroOrLessIsIgnoredRatherThanCollapsingTheBadge() {
        KonceptBadge badge = badge();
        double before = badge.getNameFontSize();

        badge.setAmbientFontSize(0);
        badge.setAmbientFontSize(-8);

        assertEquals(before, badge.getNameFontSize(), 0.001);
    }

    @Test
    void ambientScalingAlsoSizesTheKindSigil() {
        // A sigil left at the stylesheet's size would dwarf or vanish beside a scaled name.
        KonceptBadge small = badge();
        KonceptBadge large = badge();
        small.setKind(KonceptKind.PATTERN);
        large.setKind(KonceptKind.PATTERN);

        small.setAmbientFontSize(12);
        large.setAmbientFontSize(24);

        assertTrue(large.getSigilFontSize() > small.getSigilFontSize(),
                "the sigil scales with the name it sits beside");
    }

    @Test
    void standaloneStylingPaintsThePillWithoutAStylesheet() {
        KonceptBadge badge = badge();

        badge.setStandaloneStyling(true);

        assertTrue(badge.getStyle().contains("-fx-background-color"),
                "a scene without komet.css must still get the pill");
        assertNotEquals(null, badge.getNameFill(), "and the label must still get its colour");
    }

    @Test
    void standaloneStylingCanBeTurnedBackOffForACssDrivenHost() {
        KonceptBadge badge = badge();
        badge.setStandaloneStyling(true);

        badge.setStandaloneStyling(false);

        assertTrue(badge.getStyle() == null || badge.getStyle().isEmpty(),
                "a CSS-driven host must not be overridden by inline styling");
    }

    @Test
    void retiredStateIsSettableForAHostThatResolvedItItself() {
        // A badge built without a view cannot compute this, and would always render as active.
        KonceptBadge badge = badge();
        assertFalse(badge.isInactive());

        badge.setInactive(true);

        assertTrue(badge.isInactive());
    }

    @Test
    void retiredStateRepaintsInlineStylingRatherThanGoingUnnoticed() {
        KonceptBadge badge = badge();
        badge.setStandaloneStyling(true);
        Object activeFill = badge.getNameFill();

        badge.setInactive(true);

        assertNotEquals(activeFill, badge.getNameFill(), "the retired colour must take effect");
    }

    @Test
    void theBaselineIsTheNamesBaselineNotTheBoxes() {
        KonceptBadge badge = badge();
        badge.setAmbientFontSize(14);
        badge.applyCss();
        badge.resize(badge.prefWidth(-1), badge.prefHeight(-1));
        badge.layout();

        double baseline = badge.getBaselineOffset();

        assertTrue(baseline > 0, "a host seating the badge on a text line needs a real baseline");
        assertTrue(baseline < badge.prefHeight(-1),
                "the baseline sits within the badge, not below it");
    }
}
