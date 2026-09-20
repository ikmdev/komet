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

import dev.ikm.komet.kview.controls.KLDiTreeControl;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The marks a section carries while it holds semantics whose latest version is saved but not
 * published yet: the NOT PUBLISHED chip and note on the section header, and the pseudo-class a
 * field washes its changed value on.
 */
class SectionTitledPaneUnpublishedChipUTestFX {

    private static final PseudoClass UNPUBLISHED = PseudoClass.getPseudoClass("unpublished");

    private SectionTitledPane<Object> section;
    private StackPane root;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showSection() throws Exception {
        FxToolkit.setupStage(stage -> {
            section = new SectionTitledPane<>();
            section.setText("Description");
            root = new StackPane(section);
            stage.setScene(new Scene(root, 500, 200));
            stage.show();
        });
        FxToolkit.setupFixture(() -> {
            root.applyCss();
            root.layout();
        });
    }

    @AfterEach
    void hideSection() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("Without a note the header shows neither chip nor note")
    void noNoteShowsNothing() {
        assertFalse(root.lookup(".unpublished-chip").isVisible());
        assertFalse(root.lookup(".unpublished-note").isVisible());
    }

    @Test
    @DisplayName("A note shows the NOT PUBLISHED chip after the title, then the note")
    void noteShowsChipAndNote() throws Exception {
        FxToolkit.setupFixture(() -> {
            section.setUnpublishedNote("2 changes by you");
            root.applyCss();
            root.layout();
        });

        Label chip = (Label) root.lookup(".unpublished-chip");
        Label note = (Label) root.lookup(".unpublished-note");
        Node title = root.lookup(".title .text");
        assertTrue(chip.isVisible());
        assertEquals("NOT PUBLISHED", chip.getText());
        assertTrue(note.isVisible());
        assertEquals("2 changes by you", note.getText());
        double titleRight = title.localToScene(title.getLayoutBounds()).getMaxX();
        double chipLeft = chip.localToScene(chip.getLayoutBounds()).getMinX();
        double chipRight = chip.localToScene(chip.getLayoutBounds()).getMaxX();
        double noteLeft = note.localToScene(note.getLayoutBounds()).getMinX();
        assertTrue(titleRight < chipLeft, "chip follows the title");
        assertTrue(chipRight < noteLeft, "note follows the chip");
    }

    @Test
    @DisplayName("Clearing the note hides chip and note again")
    void clearingNoteHidesBoth() throws Exception {
        FxToolkit.setupFixture(() -> {
            section.setUnpublishedNote("1 change by you");
            section.setUnpublishedNote(null);
            root.applyCss();
        });

        assertFalse(root.lookup(".unpublished-chip").isVisible());
        assertFalse(root.lookup(".unpublished-note").isVisible());
    }

    @Test
    @DisplayName("A field marked unpublished carries the :unpublished pseudo-class, and drops it when cleared")
    void fieldUnpublishedPseudoClass() throws Exception {
        KLReadOnlyBaseControl field = new KLReadOnlyBaseControl() {
        };
        FxToolkit.setupFixture(() -> field.setUnpublished(true));
        assertTrue(field.getPseudoClassStates().contains(UNPUBLISHED));

        FxToolkit.setupFixture(() -> field.setUnpublished(false));
        assertFalse(field.getPseudoClassStates().contains(UNPUBLISHED));
    }

    @Test
    @DisplayName("An axiom tree marked unpublished shows the amber dot after its title")
    void axiomTreeUnpublishedDot() throws Exception {
        KLDiTreeControl axiomTree = new KLDiTreeControl();
        FxToolkit.setupFixture(() -> {
            axiomTree.setTitle("Stated definition");
            root.getChildren().setAll(axiomTree);
            root.applyCss();
            root.layout();
        });
        Node dot = axiomTree.lookup(".ditree-title-row > .unpublished-dot");
        assertFalse(dot.isVisible());

        FxToolkit.setupFixture(() -> {
            axiomTree.setUnpublished(true);
            root.applyCss();
            root.layout();
        });
        Node title = axiomTree.lookup(".ditree-title-row > .title");
        assertTrue(dot.isVisible());
        assertEquals(6, dot.getLayoutBounds().getWidth());
        assertTrue(title.localToScene(title.getLayoutBounds()).getMaxX()
                < dot.localToScene(dot.getLayoutBounds()).getMinX(), "dot follows the title");
    }
}
