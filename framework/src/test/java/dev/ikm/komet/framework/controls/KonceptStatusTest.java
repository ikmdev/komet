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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the ike-issues#861 convergence: the JavaFX {@link KonceptStatus} delegates its
 * vocabulary — glyphs, fork, accessible names — to the single-sourced koncept-core
 * {@code KonceptStatus}, so the on-screen badge, the drag glyph, and every generated
 * medium render the same marks by construction.
 */
class KonceptStatusTest {

    @Test
    void glyphsAreTheKonceptCoreVocabulary() {
        assertEquals(network.ike.docs.konceptcore.KonceptStatus.DEFINED.glyph(),
                KonceptStatus.DEFINED.glyph());
        assertEquals(network.ike.docs.konceptcore.KonceptStatus.DEFINED.glyph(),
                KonceptStatus.DEFINED_MULTIPARENT.glyph());
        assertEquals(network.ike.docs.konceptcore.KonceptStatus.PRIMITIVE.glyph(),
                KonceptStatus.PRIMITIVE.glyph());
        assertEquals(network.ike.docs.konceptcore.KonceptStatus.PRIMITIVE.glyph(),
                KonceptStatus.PRIMITIVE_MULTIPARENT.glyph());
        assertEquals(network.ike.docs.konceptcore.KonceptStatus.ROOT.glyph(),
                KonceptStatus.ROOT.glyph());
        assertNull(KonceptStatus.NONE.glyph());
        assertEquals(network.ike.docs.konceptcore.KonceptStatus.MULTI_PARENT_GLYPH,
                KonceptStatus.MULTI_PARENT_GLYPH);
    }

    @Test
    void coreExposesTheColourChannelForStylesheetFreeRenderers() {
        assertEquals("#3b8c2f", KonceptStatus.DEFINED.core().colorHex());
        assertEquals("#6b7682", KonceptStatus.PRIMITIVE_MULTIPARENT.core().colorHex());
        assertEquals("#8a6d00", KonceptStatus.ROOT.core().colorHex());
        assertFalse(KonceptStatus.NONE.core().hasGlyph());
    }

    @Test
    void accessibleTextReadsTheClusterNotJustTheCopula() {
        assertEquals("Sufficiently defined", KonceptStatus.DEFINED.accessibleText());
        assertEquals("Primitive · Multiple parents",
                KonceptStatus.PRIMITIVE_MULTIPARENT.accessibleText());
        assertTrue(KonceptStatus.DEFINED_MULTIPARENT.accessibleText().contains("Multiple parents"));
    }

    @Test
    void multiParentIsTheJavaFxSideProduct() {
        assertTrue(KonceptStatus.DEFINED_MULTIPARENT.isMultiParent());
        assertTrue(KonceptStatus.PRIMITIVE_MULTIPARENT.isMultiParent());
        assertFalse(KonceptStatus.DEFINED.isMultiParent());
        assertFalse(KonceptStatus.ROOT.isMultiParent());
        assertFalse(KonceptStatus.NONE.isMultiParent());
    }
}
