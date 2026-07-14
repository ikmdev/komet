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
package dev.ikm.komet.framework.graphics;

import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;

public class LoadFonts {

    private static final String FONT_DIR = "/dev/ikm/komet/framework/graphics/fonts/";

    public static boolean load() {
        loadFont("OpenSans-Bold.ttf");
        loadFont("OpenSans-BoldItalic.ttf");
        loadFont("OpenSans-ExtraBold.ttf");
        loadFont("OpenSans-ExtraBoldItalic.ttf");
        loadFont("OpenSans-Italic.ttf");
        loadFont("OpenSans-Light.ttf");
        loadFont("OpenSans-LightItalic.ttf");
        loadFont("OpenSans-Regular.ttf");
        loadFont("OpenSans-SemiBold.ttf");
        loadFont("OpenSans-SemiBoldItalic.ttf");
        loadFont("OpenSansCondensed-Bold.ttf");
        loadFont("OpenSansCondensed-Light.ttf");
        loadFont("OpenSansCondensed-LightItalic.ttf");
        // Alegreya Sans SC — a dedicated small-caps sans (SIL OFL 1.1). JavaFX text has no
        // OpenType small-caps feature, so true small caps (the Koncept chip's cross-medium
        // signature) requires a family whose glyphs already are small caps. The Medium subfamily
        // is non-standard, so JavaFX registers it under family "Alegreya Sans SC Medium".
        loadFont("AlegreyaSansSC-Regular.ttf");
        loadFont("AlegreyaSansSC-Medium.ttf");
        return true;
    }

    /**
     * Registers a bundled font from the classpath by <em>stream</em>, not a URL string. A resource
     * URL for a workspace path containing a non-ASCII character — the {@code ꞉} (U+A789) feature-
     * sibling separator — is percent-encoded, and {@link Font#loadFont(String, double)} fails to
     * open it, silently dropping <em>every</em> bundled font (so the small-caps family never
     * registers and labels fall back to shrunken all-caps). A stream sidesteps URL parsing entirely.
     *
     * @param file the font file name under the bundled fonts directory
     */
    private static void loadFont(String file) {
        try (InputStream in = LoadFonts.class.getResourceAsStream(FONT_DIR + file)) {
            if (in != null) {
                Font.loadFont(in, 10);
            }
        } catch (IOException e) {
            // A missing or unreadable bundled font must never break startup; the UI falls back.
        }
    }

    /**
     * The classpath URL of a bundled font. Prefer {@link #load()} (stream-based) for registration —
     * this URL form fails to open from a workspace path with a non-ASCII character.
     *
     * @param file the font file name under the bundled fonts directory
     * @return the resource URL as a string
     */
    public static String getFontUrl(String file) {
        return LoadFonts.class.getResource(FONT_DIR + file).toString();
    }
}
