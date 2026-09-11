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
package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.framework.controls.KonceptStatus;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import network.ike.docs.konceptcore.KonceptKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the {@link ComponentItemNode} presentation properties the glyph settings drive
 * (ikmdev/komet-desktop#153): each hides its mark and collapses the mark's slot while the
 * resolved kind and status stay put, so a setting can be turned back on without re-resolving.
 * The wiring to the app-wide settings is the factory's; the bare node renders every mark.
 */
@ExtendWith(ApplicationExtension.class)
class ComponentItemNodeGlyphSettingsUTestFX {

    @BeforeEach
    void toolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    /** The label's graphic: [sigil][status][identicon]. */
    private static HBox graphic(ComponentItemNode node) {
        return (HBox) ((Label) node.getChildrenUnmodifiable().get(0)).getGraphic();
    }

    private static HBox sigilBox(ComponentItemNode node) {
        return (HBox) graphic(node).getChildren().get(0);
    }

    private static HBox statusBox(ComponentItemNode node) {
        return (HBox) graphic(node).getChildren().get(1);
    }

    @Test
    void theStatusClusterFollowsTheDefinitionStatusAndMultipleParentsProperties() throws Exception {
        FxToolkit.setupFixture(() -> {
            ComponentItemNode node = new ComponentItemNode();
            node.setKonceptStatus(KonceptStatus.PRIMITIVE_MULTIPARENT);
            assertEquals(2, statusBox(node).getChildren().size(), "every mark on: the glyph and the fork");

            node.setShowMultipleParents(false);
            assertEquals(1, statusBox(node).getChildren().size(), "the fork is dropped, the glyph stays");
            assertEquals(KonceptStatus.PRIMITIVE_MULTIPARENT, node.getKonceptStatus(), "the resolved status is kept");

            node.setShowDefinitionStatus(false);
            assertTrue(statusBox(node).getChildren().isEmpty(), "no cluster at all");
            assertFalse(statusBox(node).isManaged(), "the empty box opens no gap");

            node.setShowDefinitionStatus(true);
            node.setShowMultipleParents(true);
            assertEquals(2, statusBox(node).getChildren().size(), "both marks return");
        });
    }

    @Test
    void theSigilFollowsTheKindSigilProperty() throws Exception {
        FxToolkit.setupFixture(() -> {
            ComponentItemNode node = new ComponentItemNode();
            node.setKonceptKind(KonceptKind.PATTERN);
            assertEquals(1, sigilBox(node).getChildren().size(), "a pattern leads with its sigil");
            assertTrue(sigilBox(node).isManaged());

            node.setShowKindSigil(false);
            assertTrue(sigilBox(node).getChildren().isEmpty(), "rendered as a bare concept would be");
            assertFalse(sigilBox(node).isManaged(), "the empty box opens no gap");
            assertEquals(KonceptKind.PATTERN, node.getKonceptKind(), "the kind is kept: only the mark is hidden");

            node.setShowKindSigil(true);
            assertEquals(1, sigilBox(node).getChildren().size(), "the sigil returns");
        });
    }
}
