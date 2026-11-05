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

import dev.ikm.komet.kview.mvvm.view.genpurpose.GenPurposeFieldsView;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The edit-fields form is matched by kview.css through descendant chains of style classes, most
 * of them rooted at the window and the properties panel the form sits in. This guards the chains
 * the stylesheet relies on, with the form hosted the way the panel hosts it.
 */
class GenPurposeFieldsViewUTestFX {

    /** Selector chains kview.css uses for the form, in the shape it writes them. */
    private static final List<String> SELECTORS = List.of(
            ".gen-purpose-window .sub-container",
            ".sub-container .scroll-pane > .viewport",
            ".properties-tab-container-content .controls-container",
            ".gen-purpose-window .fields-title-bar",
            ".gen-purpose-window .fields-title-bar .lidr-device-title-label",
            ".gen-purpose-window .sub-container .button-bar",
            ".gen-purpose-window .sub-container .button-bar .button",
            ".gen-purpose-window .sub-container .button-bar .primary.button");

    private GenPurposeFieldsView view;
    private StackPane host;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showView() throws Exception {
        FxToolkit.setupStage(stage -> {
            view = new GenPurposeFieldsView();
            // The window's and the panel content's style classes, which the chains start from.
            host = new StackPane(view);
            host.getStyleClass().addAll("gen-purpose-window", "properties-tab-container-content");
            stage.setScene(new Scene(host, 600, 500));
            stage.show();
        });
        // Apply CSS so skins exist and controls' own style classes are on the tree.
        FxToolkit.setupFixture(() -> host.applyCss());
    }

    @AfterEach
    void hideView() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("Every selector chain kview.css uses for the form resolves on the code-built tree")
    void stylesheetSelectorChainsResolve() {
        for (String selector : SELECTORS) {
            assertNotNull(host.lookup(selector), "kview.css selector no longer matches: " + selector);
        }
    }

    @Test
    @DisplayName("The buttons read Cancel, Clear form and Submit, and the title Pattern Fields")
    void controlsStartWithTheirTexts() {
        List<String> buttonTexts = view.lookupAll(".button-bar .button").stream()
                .map(button -> ((Button) button).getText())
                .toList();
        assertEquals(List.of("Cancel", "Clear form", "Submit"), buttonTexts);
        assertEquals("Pattern Fields", ((Label) view.lookup(".lidr-device-title-label")).getText());
    }
}
