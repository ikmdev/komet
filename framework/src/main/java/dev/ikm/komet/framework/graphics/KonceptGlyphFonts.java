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

/**
 * Resolves the bundled <em>IKE Koncept Glyphs</em> family, so any framework surface — the badge's
 * status cluster, the drag glyph's cluster — renders the Koncept symbol inventory from the one
 * spec-owned face instead of ambient OS font fallback ({@code IKE-Network/ike-issues#953}: fallback
 * differs per requested base font and per platform, and macOS's Apple SD Gothic Neo draws the
 * {@code ⋎} fork upside-down).
 *
 * <p>{@link LoadFonts} registers the face at startup from the koncept-core jar (the same single
 * file every raster consumer loads). The name is resolved from the live family registry (never a
 * load return value) and memoised, so it degrades gracefully to {@code null} when the face is
 * absent (e.g. a headless context), letting callers fall back to the default family. FX thread
 * only.
 */
public final class KonceptGlyphFonts {

    private static String family;
    private static boolean resolved;

    private KonceptGlyphFonts() {
    }

    /**
     * The registered symbol-glyph family name, or {@code null} when the bundled face is
     * unavailable.
     *
     * @return {@code "IKE Koncept Glyphs"} when registered, else {@code null}
     */
    public static String family() {
        if (!resolved) {
            resolved = true;
            String preferred = network.ike.docs.konceptcore.KonceptAppearance.defaults()
                    .glyphFamilyName();
            if (Font.getFamilies().contains(preferred)) {
                family = preferred;
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
