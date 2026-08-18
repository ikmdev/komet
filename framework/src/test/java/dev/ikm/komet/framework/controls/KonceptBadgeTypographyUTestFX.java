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

import dev.ikm.komet.framework.graphics.SmallCapsFonts;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the configurable label typography ({@code IKE-Network/ike-issues#1050}): plain text
 * renders the name's natural case in the default family — never the small-caps family, and never
 * the all-caps fallback transform — bold raises the requested weight, and the default typography
 * is byte-for-byte the ike-issues#855 small-caps rendering the rest of the suite locks. The
 * identicon rides the ambient size alone, so typography never moves it.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgeTypographyUTestFX {

    private static final String NAME = "Chronic sinusitis";
    private static final KonceptLabelTypography PLAIN = new KonceptLabelTypography(false, false);
    private static final KonceptLabelTypography PLAIN_BOLD = new KonceptLabelTypography(false, true);

    private static Text name(KonceptBadge badge) {
        Text name = (Text) badge.lookup(".koncept-label");
        assertNotNull(name, "the badge name is a .koncept-label Text node");
        return name;
    }

    @Test
    void theDefaultTypographyIsSmallCaps() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        assertEquals(KonceptLabelTypography.DEFAULT, badge.getLabelTypography());
        assertEquals(KonceptBadge.displayText(NAME, SmallCapsFonts.family()), name(badge).getText(),
                "an untouched badge renders exactly the ike-issues#855 policy");
    }

    @Test
    void plainTextKeepsNaturalCaseEvenWithoutTheFamily() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        badge.setLabelTypography(PLAIN);
        assertEquals(NAME, name(badge).getText(),
                "plain text never upper-cases — the all-caps transform belongs to the small-caps"
                        + " fallback alone");
    }

    @Test
    void plainTextUsesTheDefaultFamilyAtTheSmallCapsScale() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        badge.setAmbientFontSize(14);
        badge.setLabelTypography(PLAIN);
        assertEquals(14 * 0.9, name(badge).getFont().getSize(), 0.01,
                "plain text holds the 0.9 name-to-ambient ratio");
    }

    @Test
    void boldRaisesTheRequestedWeightInPlainText() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        badge.setLabelTypography(PLAIN_BOLD);
        assertTrue(name(badge).getFont().getStyle().toLowerCase().contains("bold"),
                "the default family always registers a bold face");
    }

    @Test
    void switchingBackRestoresTheSmallCapsPolicy() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        badge.setLabelTypography(PLAIN);
        badge.setLabelTypography(null);
        assertEquals(KonceptLabelTypography.DEFAULT, badge.getLabelTypography(),
                "null restores the default");
        assertEquals(KonceptBadge.displayText(NAME, SmallCapsFonts.family()), name(badge).getText(),
                "the round trip is lossless — the raw name was kept");
    }

    @Test
    void typographyNeverMovesTheIdenticon() {
        KonceptBadge smallCaps = new KonceptBadge(PublicIds.newRandom(), NAME);
        smallCaps.setAmbientFontSize(14);
        KonceptBadge plain = new KonceptBadge(PublicIds.newRandom(), NAME);
        plain.setAmbientFontSize(14);
        plain.setLabelTypography(PLAIN_BOLD);
        assertEquals(identiconEdge(smallCaps), identiconEdge(plain), 0.01,
                "the identicon rides the ambient size alone");
    }

    @Test
    void theExpandedRenderingCarriesTheTypography() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        badge.setLabelTypography(PLAIN_BOLD);
        assertEquals(PLAIN_BOLD, badge.expandedRendering(300).getLabelTypography(),
                "the twin renders with this badge's typography");
    }

    /** The identicon's fit edge: third child of the published anatomy. */
    private static double identiconEdge(KonceptBadge badge) {
        return ((javafx.scene.image.ImageView) badge.getChildrenUnmodifiable().get(2)).getFitWidth();
    }
}
