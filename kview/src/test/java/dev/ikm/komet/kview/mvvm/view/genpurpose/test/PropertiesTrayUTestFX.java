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
package dev.ikm.komet.kview.mvvm.view.genpurpose.test;

import dev.ikm.komet.kview.mvvm.view.genpurpose.PropertiesTray;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The properties tray on a stand-in window: a tray pane on a border pane's right side, holding
 * a panel with a label for a drag handle.
 */
class PropertiesTrayUTestFX {

    private Pane trayPane;
    private BorderPane panel;
    private PropertiesTray tray;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showWindow() throws Exception {
        FxToolkit.setupStage(stage -> {
            Label dragHandle = new Label("Properties");
            panel = new BorderPane();
            panel.setTop(dragHandle);
            panel.setPrefWidth(300);

            trayPane = new Pane();
            BorderPane window = new BorderPane();
            window.setRight(trayPane);
            window.setPrefWidth(500);

            tray = new PropertiesTray(window, trayPane, panel, dragHandle);
            stage.setScene(new Scene(window, 900, 400));
            stage.show();
        });
    }

    @AfterEach
    void hideWindow() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("The tray holds the panel and starts out closed, asking for no size of its own")
    void startsClosedHoldingThePanel() {
        assertFalse(tray.isOpen());
        assertEquals(panel, trayPane.getChildren().getFirst());
        assertTrue(panel.getStyleClass().contains("slideout-tray-pane"));
        assertEquals(0, trayPane.getMaxWidth());
        assertEquals(0, trayPane.getMinHeight());
        assertEquals(0, trayPane.getPrefHeight());
        assertTrue(panel.prefHeightProperty().isBound());
    }

    @Test
    @DisplayName("Opening slides the tray out and closing slides it back in")
    void opensAndCloses() throws Exception {
        FxToolkit.setupFixture(tray::open);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, tray::isOpen);

        FxToolkit.setupFixture(tray::close);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !tray.isOpen());
    }

    @Test
    @DisplayName("Opening an open tray and closing a closed one change nothing")
    void openAndCloseAreIdempotent() throws Exception {
        FxToolkit.setupFixture(tray::close);
        assertFalse(tray.isOpen());

        FxToolkit.setupFixture(tray::open);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, tray::isOpen);
        FxToolkit.setupFixture(tray::open);
        assertTrue(tray.isOpen());
    }
}
