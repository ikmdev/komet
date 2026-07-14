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
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the {@link KonceptBadge} name treatment after the true small-caps switch
 * ({@code IKE-Network/ike-issues#855}): the display-case policy (natural case with the dedicated
 * family, upper-cased fallback without it — the font itself cannot be asserted headless, Monocle
 * registers no bundled fonts), and the {@code komet.css} wiring now that the name is a {@code Text}
 * node — {@code -fx-fill} colours, and a real {@code -fx-strikethrough} (never the former
 * U+0336 combining-stroke overlay) as the retired signal under the {@code inactive} pseudo-class.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgeLabelUTestFX {

    private static final String NAME = "Chronic sinusitis";
    private static final PseudoClass INACTIVE = PseudoClass.getPseudoClass("inactive");
    private static final String KOMET_CSS = KonceptBadge.class
            .getResource("/dev/ikm/komet/framework/graphics/komet.css").toExternalForm();

    /** A badge whose {@code inactive} pseudo-class the test can force (it is store-computed). */
    private static final class Probe extends KonceptBadge {
        Probe() {
            super(PublicIds.newRandom(), NAME);
        }

        void forceInactive() {
            pseudoClassStateChanged(INACTIVE, true);
        }
    }

    /** Styles the badge with komet.css and returns its name {@code Text} node. */
    private static Text styledName(KonceptBadge badge) {
        HBox root = new HBox(badge);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(KOMET_CSS);
        root.applyCss();
        Text name = (Text) badge.lookup(".koncept-label");
        assertNotNull(name, "the badge name is a .koncept-label Text node");
        return name;
    }

    @Test
    void displayTextKeepsNaturalCaseWithTheSmallCapsFamily() {
        assertEquals("Chronic sinusitis",
                KonceptBadge.displayText("Chronic sinusitis", "Alegreya Sans SC Medium"),
                "with the dedicated family the glyphs are the small caps — the case stays natural");
    }

    @Test
    void displayTextFallsBackToAllCapsWithoutTheFamily() {
        assertEquals("CHRONIC SINUSITIS", KonceptBadge.displayText("Chronic sinusitis", null),
                "absent the font, the shrunken all-caps approximation");
        assertEquals("", KonceptBadge.displayText(null, null), "a null name displays as empty");
    }

    @Test
    void theBadgeDisplaysTheResolverDrivenForm() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        Text name = (Text) badge.lookup(".koncept-label");
        assertNotNull(name);
        // Environment-independent: whatever the resolver found (system-installed family or none),
        // the display is exactly the policy's output for it.
        assertEquals(KonceptBadge.displayText(NAME, SmallCapsFonts.family()), name.getText());
    }

    @Test
    void anActiveNameIsIkeBlueAndUnstruck() {
        Text name = styledName(new Probe());
        assertEquals(Color.web("#2a5a8a"), name.getFill(),
                "komet.css colours the Text via -fx-fill (a Text has no -fx-text-fill)");
        assertFalse(name.isStrikethrough(), "an active name is never struck through");
    }

    @Test
    void aRetiredNameIsStruckThroughInTheRetiredColourByCss() {
        Probe badge = new Probe();
        Text name = styledName(badge);

        badge.forceInactive();
        badge.applyCss();

        assertEquals(Color.web("#b00020"), name.getFill(), "the retired colour");
        assertTrue(name.isStrikethrough(),
                "the retired signal is a real strikethrough on the Text node (#586)");
        assertFalse(name.getText().contains("̶"),
                "the U+0336 combining-stroke overlay hack is retired");
    }

    @Test
    void theNameFontIsResolverDrivenAndSurvivesKometCss() {
        // The font is set in code (SC family at 12, else the all-caps fallback at 11). Assert it
        // AFTER komet.css is applied: JavaFX author stylesheets outrank code-set values, so a
        // .koncept-label font rule creeping back into komet.css would silently beat setFont —
        // exactly the regression this locks out, along with a dropped setFont (default ~13px).
        Text name = styledName(new Probe());
        double expectedSize = SmallCapsFonts.family() == null ? 11 : 12;
        assertEquals(expectedSize, name.getFont().getSize(), 0.01,
                "the code-set, resolver-driven size survives komet.css");
        if (SmallCapsFonts.family() != null) {
            assertEquals(SmallCapsFonts.family(), name.getFont().getFamily());
        }
    }
}
