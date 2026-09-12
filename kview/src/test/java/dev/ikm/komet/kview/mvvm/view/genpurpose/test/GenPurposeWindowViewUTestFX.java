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
import javafx.scene.Scene;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            ".lidr-container",
            ".window-body-frame",
            ".window-body-frame > .create-mode-hint",
            ".create-mode-hint .create-mode-hint-icon",
            ".pattern-window .main-right-container",
            ".main-right-container > .slideout-tray-pane",
            ".pattern-window.concept-detail-pane .main-center-container",
            ".main-center-container .concept-detail-banner-background",
            ".concept-detail-banner-background .window-banner-grid",
            ".window-banner-grid > .concept-definition-title",
            ".window-banner-grid > .window-main-title",
            ".main-center-container .split-pane");

    private GenPurposeWindowView view;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showView() throws Exception {
        FxToolkit.setupStage(stage -> {
            view = new GenPurposeWindowView();
            stage.setScene(new Scene(view, 800, 600));
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
}
