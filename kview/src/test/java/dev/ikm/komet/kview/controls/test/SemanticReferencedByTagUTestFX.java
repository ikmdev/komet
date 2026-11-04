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

import dev.ikm.komet.kview.fxutils.CssHelper;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard.PatternSemanticsStandardControl;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard.SemanticStandardControl;
import dev.ikm.komet.layout.PatternSemanticsPresenter.ReferencingSection;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mark a semantic's standard view carries while sections are anchored on it: one bar in
 * those sections' color, in the gutter left of the fields.
 */
class SemanticReferencedByTagUTestFX {

    private SemanticStandardControl first;
    private SemanticStandardControl second;
    private StackPane root;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showSemantics() throws Exception {
        FxToolkit.setupStage(stage -> {
            first = new SemanticStandardControl();
            first.setPlaceholderText("First semantic's fields");
            second = new SemanticStandardControl();
            second.setPlaceholderText("Second semantic's fields");
            PatternSemanticsStandardControl pattern = PatternSemanticsStandardControl.create();
            pattern.getSemantics().addAll(first, second);

            VBox center = new VBox(pattern);
            center.getStyleClass().add("main-center-container");
            root = new StackPane(center);
            root.getStyleClass().addAll("concept-detail-pane", "lidr-container", "pattern-window", "gen-purpose-window");
            root.getStylesheets().add(CssHelper.defaultStyleSheet());
            stage.setScene(new Scene(root, 520, 200));
            stage.show();
        });
        FxToolkit.setupFixture(() -> {
            root.applyCss();
            root.layout();
        });
    }

    @AfterEach
    void hideSemantics() throws Exception {
        FxToolkit.cleanupStages();
    }

    private List<Node> marksOf(SemanticStandardControl semantic) {
        return new ArrayList<>(semantic.lookupAll(".reference-mark"));
    }

    @Test
    @DisplayName("Without a referencing section a semantic shows no mark")
    void unreferencedShowsNothing() {
        assertTrue(marksOf(first).isEmpty());
        assertTrue(marksOf(second).isEmpty());
    }

    @Test
    @DisplayName("A referenced semantic shows one bar in its sections' color left of the fields, recolored and removed as told")
    void referencedShowsBarInSectionsColor() throws Exception {
        FxToolkit.setupFixture(() -> {
            first.setReferencedBy(new ReferencingSection("Allowed Results, Result Units", 1));
            root.applyCss();
            root.layout();
        });
        WaitForAsyncUtils.waitForFxEvents();

        List<Node> marks = marksOf(first);
        assertEquals(1, marks.size());
        assertTrue(marksOf(second).isEmpty());
        Node mark = marks.getFirst();
        assertTrue(mark.getPseudoClassStates().contains(PseudoClass.getPseudoClass("reference-color-1")));

        Bounds firstInScene = first.localToScene(first.getLayoutBounds());
        Bounds markInScene = mark.localToScene(mark.getLayoutBounds());
        assertTrue(markInScene.getMaxX() < firstInScene.getMinX(), "bar sits in the gutter left of the fields");
        assertEquals(firstInScene.getMinY(), markInScene.getMinY(), 0.5, "bar spans the block");
        assertEquals(firstInScene.getMaxY(), markInScene.getMaxY(), 0.5, "bar spans the block");

        FxToolkit.setupFixture(() -> first.setReferencedBy(new ReferencingSection("Allowed Results", 3)));
        assertEquals(List.of(mark), marksOf(first), "the same bar, recolored");
        assertTrue(mark.getPseudoClassStates().contains(PseudoClass.getPseudoClass("reference-color-3")));
        assertFalse(mark.getPseudoClassStates().contains(PseudoClass.getPseudoClass("reference-color-1")));

        FxToolkit.setupFixture(() -> first.setReferencedBy(null));
        assertTrue(marksOf(first).isEmpty());
    }
}
