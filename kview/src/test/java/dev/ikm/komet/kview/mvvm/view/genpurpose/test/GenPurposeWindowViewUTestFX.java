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

import dev.ikm.komet.kview.mvvm.view.genpurpose.GenPurposeWindowView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The window view is matched by kview.css through descendant chains of style classes. This
 * guards the chains the stylesheet relies on, so a node that moves or loses a class shows up
 * here rather than as a silently unstyled window.
 */
class GenPurposeWindowViewUTestFX {

    /** Selector chains kview.css uses for the general-purpose window, in the shape it writes them. */
    private static final List<String> SELECTORS = List.of(
            ".pattern-window.concept-detail-pane",
            ".gen-purpose-window",
            ".gen-purpose-window > #detailsCenterBorderPane",
            ".lidr-container",
            ".window-body-frame",
            ".window-body-frame .create-mode-hint",
            ".window-body-frame .unpublished-hint",
            ".unpublished-hint > .label",
            ".unpublished-hint .unpublished-show-link",
            ".create-mode-hint .create-mode-hint-icon",
            ".pattern-window .main-right-container",
            ".main-right-container > .slideout-tray-pane",
            ".pattern-window.concept-detail-pane .main-center-container",
            ".main-center-container .concept-detail-banner-background",
            ".concept-detail-banner-background .window-banner-grid",
            ".window-banner-grid > .concept-definition-title",
            ".window-banner-grid > .window-banner-title-box",
            ".window-banner-title-box > .window-main-title",
            ".window-banner-title-box > .window-banner-pattern-line",
            ".window-banner-pattern-line .banner-pattern-link",
            ".main-center-container .split-pane");

    private GenPurposeWindowView view;
    /** A focusable node outside the window, for taking focus away from it. */
    private Button elsewhere;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showView() throws Exception {
        FxToolkit.setupStage(stage -> {
            view = new GenPurposeWindowView();
            view.setWindowTitle("Concept");
            elsewhere = new Button("Elsewhere");
            HBox desktop = new HBox(24, view, elsewhere);
            desktop.setStyle("-fx-background-color: #CAD3E2; -fx-padding: 24;");
            // The palette is declared on .root, so the stylesheet goes on the scene's root as it
            // does in the journal; on the window alone its colours don't resolve.
            desktop.getStylesheets().addAll(view.getStylesheets());
            stage.setScene(new Scene(desktop, 900, 600));
            stage.show();
        });
        // Apply CSS so skins exist and controls' own style classes are on the tree.
        FxToolkit.setupFixture(() -> view.applyCss());
    }

    @AfterEach
    void hideView() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("Every selector chain kview.css uses for the window resolves on the code-built tree")
    void stylesheetSelectorChainsResolve() {
        for (String selector : SELECTORS) {
            assertNotNull(view.lookup(selector), "kview.css selector no longer matches: " + selector);
        }
    }

    @Test
    @DisplayName("The focused window is ringed, and the ring goes when focus leaves it")
    void focusedWindowIsRinged() throws Exception {
        Node frame = view.lookup("#detailsCenterBorderPane");
        FxRobot robot = new FxRobot();

        FxToolkit.setupFixture(() -> {
            view.requestFocus();
            view.applyCss();
        });
        assertInstanceOf(DropShadow.class, frame.getEffect(), "the focused window is ringed");
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(view.getParent()).getImage(),
                Path.of("target", "gen-purpose-window-focused.png"));

        FxToolkit.setupFixture(() -> {
            elsewhere.requestFocus();
            view.applyCss();
        });
        assertNull(frame.getEffect(), "an unfocused window has no ring");
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(view.getParent()).getImage(),
                Path.of("target", "gen-purpose-window-unfocused.png"));
    }

    @Test
    @DisplayName("The standard Semantic window's chrome is teal, and washed out while it creates its semantic")
    void semanticWindowIsTeal() throws Exception {
        FxRobot robot = new FxRobot();

        FxToolkit.setupFixture(() -> {
            view.setTheme(GenPurposeWindowView.Theme.SEMANTIC);
            view.setWindowTitle("Comment semantic");
            view.requestFocus();
            view.applyCss();
        });
        assertEquals(Color.web("#2A6A6F"), fillOf(".kl-window-control-toolbar .window-header-control"),
                "the Semantic window's bar is teal");
        assertEquals(Color.web("#1F5054"), fillOf(".lidr-rounded-tab"), "its tab is the darker teal");
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(view.getParent()).getImage(),
                Path.of("target", "semantic-window-chrome.png"));

        FxToolkit.setupFixture(() -> {
            view.setCreating(true);
            view.setCreateModeHintText("This Semantic doesn't exist yet - it's created when you fill out the required values and hit Publish.");
            view.showPlaceholderTitle("New Comment semantic");
            view.applyCss();
        });
        assertEquals(Color.web("#7FA7AA"), fillOf(".kl-window-control-toolbar .window-header-control"),
                "the bar is washed out while the semantic doesn't exist");
        assertEquals(Color.web("#5F8E92"), fillOf(".lidr-rounded-tab"), "and so is the tab");
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(view.getParent()).getImage(),
                Path.of("target", "semantic-window-chrome-create.png"));
    }

    /** The fill of the first background layer of the node the selector finds in the window. */
    private Color fillOf(String selector) {
        Region region = (Region) view.lookup(selector);
        return (Color) region.getBackground().getFills().getFirst().getFill();
    }
}
