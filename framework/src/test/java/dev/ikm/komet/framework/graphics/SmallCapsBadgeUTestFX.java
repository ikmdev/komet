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
package dev.ikm.komet.framework.graphics;

import dev.ikm.komet.framework.controls.KonceptBadge;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Locks the live small-caps wiring at the badge call site ({@code IKE-Network/ike-issues#855}):
 * with the bundled Alegreya Sans SC family actually registered ({@link LoadFonts} stream-loading,
 * exactly as the application startup does), a {@link KonceptBadge} name renders in its
 * <em>natural case</em> in that family at the small-caps size — so a revert to unconditional
 * upper-casing (or a dropped {@code setFont}) fails here, not only in a live smoke.
 *
 * <p>On a toolkit whose font pipeline cannot register bundled fonts (headless Monocle), the load
 * silently no-ops and the test is skipped via an assumption — the policy split itself is locked
 * environment-independently by {@code KonceptBadgeLabelUTestFX}. Lives in the {@code graphics}
 * package for the {@link SmallCapsFonts#reset()} hook: the resolver memoises, and another test
 * class may already have resolved {@code null} before the family was registered.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class SmallCapsBadgeUTestFX {

    /** The small-caps name font size KonceptBadge sets (its SC_FONT_SIZE policy constant). */
    private static final double SC_FONT_SIZE = 12;

    @Test
    void aBadgeRendersNaturalCaseInTheRegisteredSmallCapsFamily() {
        LoadFonts.load();
        SmallCapsFonts.reset();
        String family = SmallCapsFonts.family();
        assumeTrue(family != null,
                "bundled family did not register (headless font pipeline) — covered by the live smoke");

        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), "Chronic sinusitis");
        Text name = (Text) badge.lookup(".koncept-label");
        assertNotNull(name, "the badge name is a .koncept-label Text node");
        assertEquals("Chronic sinusitis", name.getText(),
                "with the SC family the name keeps its natural case — the glyphs are the small caps");
        assertEquals(family, name.getFont().getFamily(),
                "the name is set in the resolved dedicated small-caps family");
        assertEquals(SC_FONT_SIZE, name.getFont().getSize(), 0.01,
                "the small-caps mode uses the SC size, not the all-caps fallback size");
    }
}
