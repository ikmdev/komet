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

import javafx.scene.text.Font;

import java.util.List;

/**
 * Resolves the bundled dedicated small-caps family, so any framework surface — the concept drag
 * glyph, the {@link dev.ikm.komet.framework.controls.KonceptBadge} label — can render true small
 * caps ({@code IKE-Network/ike-issues#855}).
 *
 * <p>JavaFX text has no OpenType small-caps feature, so true small caps (capitals full height, the
 * rest small capitals) needs a family whose glyphs already are small caps. {@link LoadFonts}
 * registers <b>Alegreya Sans SC</b> at startup; JavaFX folds its non-standard {@code Medium}
 * subfamily into its own family name, so the Medium face — heavier strokes read better as small
 * caps at UI sizes — is preferred, with the Regular family as the fallback. The name is resolved
 * from the live family registry (never a load return value) and memoised, so it degrades gracefully
 * to {@code null} when the font is absent (e.g. a headless context), letting callers fall back to
 * shrunken all-caps. FX thread only.
 */
public final class SmallCapsFonts {

    private static String family;
    private static boolean resolved;

    private SmallCapsFonts() {
    }

    /**
     * The registered small-caps family name, or {@code null} when the bundled font is unavailable.
     *
     * @return {@code "Alegreya Sans SC Medium"}, else {@code "Alegreya Sans SC"}, else {@code null}
     */
    public static String family() {
        if (!resolved) {
            resolved = true;
            List<String> families = Font.getFamilies();
            if (families.contains("Alegreya Sans SC Medium")) {
                family = "Alegreya Sans SC Medium";
            } else if (families.contains("Alegreya Sans SC")) {
                family = "Alegreya Sans SC";
            }
        }
        return family;
    }

    /**
     * Test hook: drops the memoised resolution so a test that registers the bundled family
     * <em>after</em> an earlier test already resolved {@code null} can re-resolve. Production code
     * must never call this — the family registry only grows at startup ({@link LoadFonts}).
     */
    static void reset() {
        resolved = false;
        family = null;
    }
}
