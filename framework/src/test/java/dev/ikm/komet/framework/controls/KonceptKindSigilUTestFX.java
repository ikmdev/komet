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
import network.ike.docs.konceptcore.StampSigilGeometry;
import network.ike.docs.konceptcore.KonceptKind;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the component-kind sigil scheme and the <em>locked</em> stamp pentagon geometry
 * (ike-issues#638), and confirms the {@link StampSigil} renders. Geometry assertions are pure; the
 * render check needs a scene, so the whole class runs on the FX thread.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptKindSigilUTestFX {

    @Test
    void conceptIsTheBareDefaultWithNoSigil() {
        assertTrue(KonceptKind.CONCEPT.isBare());
        assertFalse(KonceptKind.CONCEPT.hasSigil(), "concept must be bare — no sigil");
        assertNull(KonceptKind.CONCEPT.glyph());
        assertNull(KonceptKind.CONCEPT.colorHex());
        // Exactly one kind is bare.
        long bare = java.util.Arrays.stream(KonceptKind.values()).filter(KonceptKind::isBare).count();
        assertEquals(1, bare, "only CONCEPT is the bare default");
    }

    @Test
    void letterKindsCarryTheirSigil() {
        assertEquals("D", KonceptKind.DESCRIPTION.glyph());
        assertEquals("S", KonceptKind.SEMANTIC.glyph());
        assertEquals("P", KonceptKind.PATTERN.glyph());
        assertEquals("?", KonceptKind.UNKNOWN.glyph(), "unresolvable/presentation-only is ?, never bare");
        assertTrue(KonceptKind.DESCRIPTION.hasLetterGlyph());
        assertTrue(KonceptKind.UNKNOWN.hasSigil());
    }

    @Test
    void stampUsesThePentagonNotALetter() {
        assertTrue(KonceptKind.STAMP.isStamp());
        assertNull(KonceptKind.STAMP.glyph(), "stamp has no letter glyph — it uses the pentagon");
        assertFalse(KonceptKind.STAMP.hasLetterGlyph());
        assertTrue(KonceptKind.STAMP.hasSigil());
        assertEquals(StampSigilGeometry.COLOR, KonceptKind.STAMP.colorHex());
        // Exactly one kind is the stamp pentagon.
        long stamps = java.util.Arrays.stream(KonceptKind.values()).filter(KonceptKind::isStamp).count();
        assertEquals(1, stamps);
    }

    @Test
    void everySigilHasBothChannels() {
        // Accessibility: never colour alone. Every kind that shows a sigil must also have a
        // non-colour channel (a letter glyph or the pentagon) and an accessible name.
        for (KonceptKind kind : KonceptKind.values()) {
            assertNotNull(kind.accessibleName());
            if (kind.hasSigil()) {
                assertNotNull(kind.colorHex(), kind + " sigil must have a colour");
                assertTrue(kind.hasLetterGlyph() || kind.isStamp(),
                        kind + " sigil must have a non-colour channel (letter or pentagon)");
            }
        }
    }

    @Test
    void stampPentagonGeometryMatchesTheLockedSpec() {
        assertEquals(5, StampSigilGeometry.AXIS_COUNT);
        assertEquals(5, StampSigilGeometry.VERTICES.length);
        double[][] expectedVertices = {
                {0.0, -1.0}, {0.951, -0.309}, {0.588, 0.809}, {-0.588, 0.809}, {-0.951, -0.309}
        };
        for (int i = 0; i < expectedVertices.length; i++) {
            assertEquals(expectedVertices[i][0], StampSigilGeometry.VERTICES[i][0], 1e-9, "Vx" + i);
            assertEquals(expectedVertices[i][1], StampSigilGeometry.VERTICES[i][1], 1e-9, "Vy" + i);
            // Point-up pentagon: each vertex is a unit vector.
            double length = Math.hypot(StampSigilGeometry.VERTICES[i][0], StampSigilGeometry.VERTICES[i][1]);
            assertEquals(1.0, length, 1e-3, "vertex " + i + " is unit length");
        }
        double[] expectedRadii = {0.78, 0.48, 0.86, 0.56, 0.66};
        for (int i = 0; i < expectedRadii.length; i++) {
            assertEquals(expectedRadii[i], StampSigilGeometry.AXIS_DOT_RADII[i], 1e-9, "axis radius " + i);
        }
        assertEquals(0.10, StampSigilGeometry.DOT_RADIUS, 1e-9);
        assertEquals(0.12, StampSigilGeometry.HUB_RADIUS, 1e-9);
        assertEquals(1.4, StampSigilGeometry.STROKE_WIDTH_PX, 1e-9);
        assertEquals("#888780", StampSigilGeometry.COLOR);
    }

    @Test
    void stampSigilRendersTheGrayPentagon() {
        StampSigil sigil = new StampSigil(16);
        HBox root = new HBox(sigil);
        Scene scene = new Scene(root);
        root.applyCss();
        root.layout();

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image image = sigil.snapshot(params, null);

        assertEquals(16, image.getWidth(), 1.0);
        assertEquals(16, image.getHeight(), 1.0);

        PixelReader reader = image.getPixelReader();
        Color gray = Color.web(StampSigilGeometry.COLOR);
        int inked = 0;
        int grayish = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = reader.getColor(x, y);
                if (c.getOpacity() > 0.3) {
                    inked++;
                    if (Math.abs(c.getRed() - gray.getRed()) < 0.15
                            && Math.abs(c.getGreen() - gray.getGreen()) < 0.15
                            && Math.abs(c.getBlue() - gray.getBlue()) < 0.15) {
                        grayish++;
                    }
                }
            }
        }
        assertTrue(inked > 0, "the sigil must render visible (non-transparent) pixels");
        assertTrue(grayish > 0, "the sigil's ink must be the locked stamp gray");
    }
}
