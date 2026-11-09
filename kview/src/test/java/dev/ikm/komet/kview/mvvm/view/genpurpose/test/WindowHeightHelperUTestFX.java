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

import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.mvvm.view.genpurpose.WindowHeightHelper;
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
 * window's shape, as far as {@link WindowHeightHelper} is concerned.
 */
class WindowHeightHelperUTestFX {

    private static final double CHROME_HEIGHT = 50;
    private static final double CONTENT_HEIGHT = 300;
    private static final double WINDOW_Y = 20;
    private static final double TOLERANCE = 0.5;

    private ScrollPane workspace;
    private BorderPane window;
    private WindowHeightHelper helper;

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
            helper = new WindowHeightHelper(window, tray);

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

        runAndAwaitAnimation(() -> helper.growToFitProperties(450));
        assertEquals(CHROME_HEIGHT + 450, window.getHeight(), TOLERANCE);

        runAndAwaitAnimation(helper::restorePreviousHeight);
        assertEquals(CHROME_HEIGHT + CONTENT_HEIGHT, window.getHeight(), TOLERANCE);
        assertEquals(Region.USE_COMPUTED_SIZE, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("The growth is animated rather than applied at once")
    void growthIsAnimated() throws Exception {
        FxToolkit.setupFixture(() -> helper.growToFitProperties(450));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(window.getHeight() < CHROME_HEIGHT + 450, "the window jumped to its grown height");
    }

    @Test
    @DisplayName("A window with a height of its own goes back to that height on restore")
    void restoresAFixedHeight() throws Exception {
        FxToolkit.setupFixture(() -> window.setPrefHeight(400));
        WaitForAsyncUtils.waitForFxEvents();

        runAndAwaitAnimation(() -> helper.growToFitProperties(450));
        assertEquals(CHROME_HEIGHT + 450, window.getHeight(), TOLERANCE);

        runAndAwaitAnimation(helper::restorePreviousHeight);
        assertEquals(400, window.getPrefHeight(), 0);
        assertEquals(400, window.getHeight(), TOLERANCE);
    }

    @Test
    @DisplayName("A tray that needs less than the window has leaves the window alone")
    void neverShrinksTheWindow() throws Exception {
        runAndAwaitAnimation(() -> helper.growToFitProperties(100));
        assertEquals(CHROME_HEIGHT + CONTENT_HEIGHT, window.getHeight(), TOLERANCE);
        assertEquals(Region.USE_COMPUTED_SIZE, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("Growth stops short of the bottom of the workspace's visible area")
    void growthIsCappedAtTheVisibleWorkspace() throws Exception {
        runAndAwaitAnimation(() -> helper.growToFitProperties(5000));

        double visibleHeight = workspace.getViewportBounds().getHeight();
        assertEquals(Math.ceil(visibleHeight - WINDOW_Y - WindowHeightHelper.BOTTOM_GAP), window.getHeight(), 1);
    }

    @Test
    @DisplayName("A grown window the user resized keeps the user's height on restore")
    void keepsAUserResize() throws Exception {
        runAndAwaitAnimation(() -> helper.growToFitProperties(450));

        FxToolkit.setupFixture(() -> window.setPrefHeight(420));
        runAndAwaitAnimation(helper::restorePreviousHeight);
        assertEquals(420, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("A section collapsing in a grown window comes out of the restored height too")
    void heightChangesCarryOverToTheRestoredHeight() throws Exception {
        FxToolkit.setupFixture(() -> window.setPrefHeight(400));
        WaitForAsyncUtils.waitForFxEvents();

        runAndAwaitAnimation(() -> helper.growToFitProperties(450));
        FxToolkit.setupFixture(() -> helper.changeHeightBy(-60));
        assertEquals(CHROME_HEIGHT + 450 - 60, window.getPrefHeight(), 0);

        runAndAwaitAnimation(helper::restorePreviousHeight);
        assertEquals(340, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("A window dragged taller than the workspace's ceiling keeps that height over a section collapse and expand")
    void keepsAHeightAboveTheWorkspaceCeiling() throws Exception {
        // The workspace raises the window's maximum height when the user drags it past the ceiling.
        final double draggedHeight = KLWorkspace.MAX_WINDOW_HEIGHT + 400;
        FxToolkit.setupFixture(() -> {
            window.setMaxHeight(draggedHeight);
            window.setPrefHeight(draggedHeight);
        });

        FxToolkit.setupFixture(() -> helper.changeHeightBy(-300));
        assertEquals(draggedHeight - 300, window.getPrefHeight(), 0);

        FxToolkit.setupFixture(() -> helper.changeHeightBy(300));
        assertEquals(draggedHeight, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("A window at the workspace's ceiling grows no further when a section expands")
    void staysWithinTheWindowsOwnMaximum() throws Exception {
        FxToolkit.setupFixture(() -> {
            window.setMaxHeight(KLWorkspace.MAX_WINDOW_HEIGHT);
            window.setPrefHeight(KLWorkspace.MAX_WINDOW_HEIGHT);
        });

        FxToolkit.setupFixture(() -> helper.changeHeightBy(300));
        assertEquals(KLWorkspace.MAX_WINDOW_HEIGHT, window.getPrefHeight(), 0);
    }

    @Test
    @DisplayName("A divider drag grows a window with a height of its own past its ceiling, raising the ceiling")
    void dividerDragGrowsAFixedHeightWindowPastItsCeiling() throws Exception {
        FxToolkit.setupFixture(() -> {
            window.setMaxHeight(500);
            window.setPrefHeight(400);
        });

        double[] granted = new double[1];
        FxToolkit.setupFixture(() -> granted[0] = helper.growBy(200));
        assertEquals(200, granted[0], 0);
        assertEquals(600, window.getPrefHeight(), 0);
        assertEquals(600, window.getMaxHeight(), 0);
    }

    @Test
    @DisplayName("A divider drag leaves a content-sized window to its content, and only raises its ceiling")
    void dividerDragRaisesAContentSizedWindowsCeiling() throws Exception {
        FxToolkit.setupFixture(() -> window.setMaxHeight(CHROME_HEIGHT + CONTENT_HEIGHT + 10));

        double[] granted = new double[1];
        FxToolkit.setupFixture(() -> granted[0] = helper.growBy(50));
        assertEquals(50, granted[0], 0);
        assertEquals(Region.USE_COMPUTED_SIZE, window.getPrefHeight(), 0);
        assertEquals(CHROME_HEIGHT + CONTENT_HEIGHT + 50, window.getMaxHeight(), 0);
    }
}
