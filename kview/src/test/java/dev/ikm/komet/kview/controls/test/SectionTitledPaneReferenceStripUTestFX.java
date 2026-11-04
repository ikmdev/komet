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

import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.fxutils.CssHelper;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The reference strip a section shows under its title bar while it carries a reference summary,
 * styled as in the general-purpose window (kview.css under the window's style classes).
 */
class SectionTitledPaneReferenceStripUTestFX {

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
            section.setText("Allowed Results");
            section.getStyleClass().add("pattern-titled-pane");
            section.getItems().add(new Label("[empty]"));

            VBox center = new VBox(section);
            center.getStyleClass().add("main-center-container");
            root = new StackPane(center);
            root.getStyleClass().addAll("concept-detail-pane", "lidr-container", "pattern-window", "gen-purpose-window");
            root.getStylesheets().add(CssHelper.defaultStyleSheet());
            stage.setScene(new Scene(root, 600, 220));
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
    @DisplayName("The strip shows only when told to, whatever the summary holds")
    void stripShowsOnlyWhenTold() throws Exception {
        assertFalse(root.lookup(".reference-strip").isVisible(), "hidden by default");

        FxToolkit.setupFixture(() -> {
            section.getReferenceSummary().setAll(
                    new SectionTitledPane.ReferenceField("Analyte", "Electronystagmogram"),
                    new SectionTitledPane.ReferenceField("Target", "Nissl granule"));
            root.applyCss();
            root.layout();
        });
        assertFalse(root.lookup(".reference-strip").isVisible(), "a summary alone shows nothing");

        FxToolkit.setupFixture(() -> {
            section.setReferenceStripVisible(true);
            root.applyCss();
            root.layout();
        });
        assertTrue(root.lookup(".reference-strip").isVisible());

        FxToolkit.setupFixture(() -> section.setReferenceStripVisible(false));
        assertFalse(root.lookup(".reference-strip").isVisible());
    }

    @Test
    @DisplayName("Several fields show the strip flush under the title bar, spanning the section's width")
    void summaryShowsStripUnderTitle() throws Exception {
        FxToolkit.setupFixture(() -> {
            section.setReferenceStripVisible(true);
            section.getReferenceSummary().setAll(
                    new SectionTitledPane.ReferenceField("Analyte", "English plantain IgE Ab [Presence] in Serum by Radioallergosorbent test (RAST)"),
                    new SectionTitledPane.ReferenceField("Target", "Electronystagmogram (procedure)"),
                    new SectionTitledPane.ReferenceField("Test performed", "Electronystagmogram (procedure)"));
            root.applyCss();
            root.layout();
        });
        WaitForAsyncUtils.waitForFxEvents();

        Node strip = root.lookup(".reference-strip");
        Node title = root.lookup(".title");
        Node caption = root.lookup(".reference-strip-caption");
        assertTrue(strip.isVisible());

        // Flush under the title bar and spanning the section (modena would pad the content box).
        Bounds titleInScene = title.localToScene(title.getLayoutBounds());
        Bounds stripInScene = strip.localToScene(strip.getLayoutBounds());
        Bounds sectionInScene = section.localToScene(section.getLayoutBounds());
        assertEquals(titleInScene.getMaxY(), stripInScene.getMinY(), 0.5, "strip starts where the title bar ends");
        assertEquals(sectionInScene.getMinX(), stripInScene.getMinX(), 0.5, "strip starts at the section's left edge");
        assertEquals(sectionInScene.getMaxX(), stripInScene.getMaxX(), 0.5, "strip ends at the section's right edge");

        // One line: the fields elide, while the caption keeps its full width.
        Bounds captionInScene = caption.localToScene(caption.getLayoutBounds());
        assertEquals(caption.prefWidth(-1), captionInScene.getWidth(), 1, "caption shown whole");
        Text elided = (Text) root.lookup(".reference-strip-field-value");
        assertTrue(elided.getText().endsWith("…"), "first value cut off with an ellipsis: " + elided.getText());
        assertTrue(root.lookup(".reference-strip-field-name").localToScene(root.lookup(".reference-strip-field-name").getLayoutBounds()).getHeight()
                < stripInScene.getHeight(), "fields stay on one line");
    }
}
