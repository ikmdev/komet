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

import network.ike.docs.konceptcore.KonceptAppearance;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The #861 drift gate: komet.css cannot read {@link KonceptAppearance}, so its
 * {@code .koncept-*} rules <em>mirror</em> the spec's values — this test fails the build when a
 * mirrored value and the record disagree, in either direction. The standalone (stylesheet-free)
 * badge style is asserted against the same record, so the two on-screen renderings cannot
 * diverge from the spec or from each other.
 */
class KonceptAppearanceMirrorTest {

    private static String komet_css() throws IOException {
        try (InputStream in = KonceptAppearanceMirrorTest.class.getResourceAsStream(
                "/dev/ikm/komet/framework/graphics/komet.css")) {
            assertNotNull(in, "komet.css ships in the framework jar");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void kometCssMirrorsThePillSpec() throws IOException {
        KonceptAppearance spec = KonceptAppearance.defaults();
        String css = komet_css();

        assertTrue(css.contains("-fx-background-color: " + spec.pillFillHex() + ";"),
                ".koncept-chip fill mirrors the spec");
        assertTrue(css.contains("-fx-background-radius: " + (int) spec.cornerRadiusPx() + ";"),
                ".koncept-chip radius mirrors the spec");
        assertTrue(css.contains("-fx-padding: " + (int) spec.padTopPx() + " "
                        + (int) spec.padRightPx() + " " + (int) spec.padBottomPx() + " "
                        + (int) spec.padLeftPx() + ";"),
                ".koncept-chip padding mirrors the spec's unified pads");
        assertTrue(css.contains("-fx-fill: " + spec.labelColorHex() + ";"),
                ".koncept-label colour mirrors the spec");
        assertTrue(css.contains("-fx-fill: " + spec.labelColorInactiveHex() + ";"),
                ".koncept-label:inactive colour mirrors the spec");
    }

    @Test
    void kometCssMirrorsTheGlyphFamily() throws IOException {
        assertTrue(komet_css().contains(
                        "-fx-font-family: \"" + KonceptAppearance.defaults().glyphFamilyName() + "\";"),
                ".koncept-status family mirrors the spec's bundled glyph face (ike-issues#953)");
    }

    @Test
    void kometCssMirrorsTheStatusVocabulary() throws IOException {
        String css = komet_css();
        assertTrue(css.contains(KonceptStatus.DEFINED.core().colorHex()),
                "defined status colour mirrors koncept-core");
        assertTrue(css.contains(KonceptStatus.PRIMITIVE.core().colorHex()),
                "primitive status colour mirrors koncept-core");
        assertTrue(css.contains(KonceptStatus.ROOT.core().colorHex()),
                "root status colour mirrors koncept-core");
        assertTrue(css.contains(
                        network.ike.docs.konceptcore.KonceptStatus.MULTI_PARENT_COLOR_HEX),
                "multi-parent fork colour mirrors koncept-core");
    }

    @Test
    void standaloneBadgeStyleIsBuiltFromTheSpec() {
        KonceptAppearance spec = KonceptAppearance.defaults();
        String style = KonceptBadge.standalonePillStyle(spec);

        assertTrue(style.contains(spec.pillFillHex()), "standalone fill reads the spec");
        assertTrue(style.contains("-fx-background-radius: " + (int) spec.cornerRadiusPx()),
                "standalone radius reads the spec");
        assertTrue(style.contains("-fx-padding: 1 6 1 4"),
                "standalone pads are the spec's unified 1/6/1/4");
    }
}
