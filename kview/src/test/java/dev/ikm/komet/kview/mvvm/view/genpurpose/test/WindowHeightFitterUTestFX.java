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

import dev.ikm.komet.kview.mvvm.view.genpurpose.WindowHeightFitter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A window in a scrollable workspace: 50px of chrome on top, 300px of content, and a tray on the
 * right that fills the height below the chrome without asking for any — the general-purpose
 * window's shape, as far as {@link WindowHeightFitter} is concerned.
 */
class WindowHeightFitterUTestFX {

    private static final double CHROME_HEIGHT = 50;
    private static final double CONTENT_HEIGHT = 300;
    private static final double WINDOW_Y = 20;
    private static final double TOLERANCE = 0.5;

    private ScrollPane workspace;
    private BorderPane window;
    private final DoubleProperty requiredTrayHeight = new SimpleDoubleProperty();
    private WindowHeightFitter fitter;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showWindow() throws Exception {
        FxToolkit.setupStage(stage -> {
            Region chrome = new Region();
            chrome.setPrefHeight(CHROME_HEIGHT);
            Region content = new Region();
            content.setPrefSize(300, CONTENT_HEIGHT);
            Pane tray = new Pane();
            tray.setMinHeight(0);
            tray.setPrefSize(100, 0);

            window = new BorderPane(content, chrome, tray, null, null);
            window.setLayoutY(WINDOW_Y);

            workspace = new ScrollPane(new Pane(window));
            fitter = new WindowHeightFitter(window, tray, requiredTrayHeight);

            stage.setScene(new Scene(workspace, 800, 700));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void hideWindow() throws Exception {
        FxToolkit.cleanupStages();
    }

    /** Runs {@code action} on the FX thread, then waits out the animation it starts. */
    private void runAndAwaitAnimation(Runnable action) throws Exception {
        FxToolkit.setupFixture(action);
        WaitForAsyncUtils.sleep(700, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("A window sized to its content grows to fit the tray, and is handed back to its content on restore")
    void growsAndRestoresAContentSizedWindow() throws Exception {
        assertEquals(CHROME_HEIGHT + CONTENT_HEIGHT, window.getHeight(), TOLERANCE);

        requiredTrayHeight.set(450);
        runAndAwaitAnimation(fitter::growToFitProperties);
        assertEquals(CHROME_HEIGHT + 450, window.getHeight(), TOLERANCE);

        runAndAwaitAnimation(fitter::restorePreviousHeight);
        assertEquals(CHROME_HEIGHT + CONTENT_HEIGHT, window.getHeight(), TOLERANCE);
        assertEquals(Region.USE_COMPUTED_SIZE, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("The growth is animated rather than applied at once")
    void growthIsAnimated() throws Exception {
        requiredTrayHeight.set(450);
        FxToolkit.setupFixture(fitter::growToFitProperties);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(window.getHeight() < CHROME_HEIGHT + 450, "the window jumped to its grown height");
    }

    @Test
    @DisplayName("A window with a height of its own goes back to that height on restore")
    void restoresAFixedHeight() throws Exception {
        FxToolkit.setupFixture(() -> window.setPrefHeight(400));
        WaitForAsyncUtils.waitForFxEvents();

        requiredTrayHeight.set(450);
        runAndAwaitAnimation(fitter::growToFitProperties);
        assertEquals(CHROME_HEIGHT + 450, window.getHeight(), TOLERANCE);

        runAndAwaitAnimation(fitter::restorePreviousHeight);
        assertEquals(400, window.getPrefHeight(), 0);
        assertEquals(400, window.getHeight(), TOLERANCE);
    }

    @Test
    @DisplayName("A tray that needs less than the window has leaves the window alone")
    void neverShrinksTheWindow() throws Exception {
        requiredTrayHeight.set(100);
        runAndAwaitAnimation(fitter::growToFitProperties);
        assertEquals(CHROME_HEIGHT + CONTENT_HEIGHT, window.getHeight(), TOLERANCE);
        assertEquals(Region.USE_COMPUTED_SIZE, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("Growth stops short of the bottom of the workspace's visible area")
    void growthIsCappedAtTheVisibleWorkspace() throws Exception {
        requiredTrayHeight.set(5000);
        runAndAwaitAnimation(fitter::growToFitProperties);

        double visibleHeight = workspace.getViewportBounds().getHeight();
        assertEquals(Math.ceil(visibleHeight - WINDOW_Y - WindowHeightFitter.BOTTOM_GAP), window.getHeight(), 1);
    }

    @Test
    @DisplayName("A grown window the user resized keeps the user's height on restore")
    void keepsAUserResize() throws Exception {
        requiredTrayHeight.set(450);
        runAndAwaitAnimation(fitter::growToFitProperties);

        FxToolkit.setupFixture(() -> window.setPrefHeight(420));
        runAndAwaitAnimation(fitter::restorePreviousHeight);
        assertEquals(420, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("A section collapsing in a grown window comes out of the restored height too")
    void heightChangesCarryOverToTheRestoredHeight() throws Exception {
        FxToolkit.setupFixture(() -> window.setPrefHeight(400));
        WaitForAsyncUtils.waitForFxEvents();

        requiredTrayHeight.set(450);
        runAndAwaitAnimation(fitter::growToFitProperties);
        FxToolkit.setupFixture(() -> fitter.changeHeightBy(-60));
        assertEquals(CHROME_HEIGHT + 450 - 60, window.getPrefHeight(), 0);

        runAndAwaitAnimation(fitter::restorePreviousHeight);
        assertEquals(340, window.getPrefHeight(), 0);
    }
}
