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

import dev.ikm.komet.kview.fxutils.FXUtils;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FXUtils#isOnScreen}: a node counts as on screen while enough of it lies inside the
 * viewport of every scroll pane around it and no titled pane around it is collapsed.
 */
class OnScreenUTestFX {

    private Region top;
    private Region bottom;
    private ScrollPane scrollPane;
    private TitledPane titledPane;
    private StackPane root;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showStack() throws Exception {
        FxToolkit.setupStage(stage -> {
            top = new Region();
            top.setPrefSize(200, 100);
            bottom = new Region();
            bottom.setPrefSize(200, 100);
            scrollPane = new ScrollPane(new VBox(top, bottom));
            scrollPane.setPrefViewportHeight(120);
            titledPane = new TitledPane("Section", scrollPane);
            titledPane.setAnimated(false);
            root = new StackPane(titledPane);
            stage.setScene(new Scene(root, 300, 200));
            stage.show();
        });
        FxToolkit.setupFixture(() -> {
            root.applyCss();
            root.layout();
        });
    }

    @AfterEach
    void hideStack() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("Scrolling decides: the node in the viewport is on screen, the one below it is not, and vice versa")
    void scrollingDecides() throws Exception {
        assertTrue(FXUtils.isOnScreen(top));
        assertFalse(FXUtils.isOnScreen(bottom), "only 20px of its 100 show");

        FxToolkit.setupFixture(() -> {
            scrollPane.setVvalue(1);
            root.layout();
        });
        assertFalse(FXUtils.isOnScreen(top));
        assertTrue(FXUtils.isOnScreen(bottom));
    }

    @Test
    @DisplayName("A collapsed titled pane hides everything in it")
    void collapsedHides() throws Exception {
        FxToolkit.setupFixture(() -> {
            titledPane.setExpanded(false);
            root.layout();
        });
        assertFalse(FXUtils.isOnScreen(top));
    }
}
