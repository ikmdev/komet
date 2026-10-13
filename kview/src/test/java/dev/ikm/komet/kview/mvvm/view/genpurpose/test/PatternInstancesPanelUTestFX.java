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

import dev.ikm.komet.kview.mvvm.view.genpurpose.PatternInstancesPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;
import org.testfx.service.support.CaptureSupport;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Every selector chain kview.css styles the INSTANCES tab's panel with resolves on the code-built
 * tree — the panel is only styled under the window's {@code .gen-purpose-window} class, so the
 * test wraps it the way the window does, with the stylesheet (and its palette on {@code .root})
 * on the scene root as the window has it.
 */
class PatternInstancesPanelUTestFX {

    private static final List<String> SELECTORS = List.of(
            ".gen-purpose-window .pattern-instances",
            ".pattern-instances .instances-header",
            ".instances-header .instances-title-bar",
            ".instances-title-bar .lidr-device-title-label",
            ".instances-title-bar .instances-count",
            ".instances-header .instances-search",
            ".instances-search .text-field",
            ".instances-search .search-icon",
            ".instances-search .filter-icon",
            ".pattern-instances .instances-list");

    private StackPane window;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void showPanel() throws Exception {
        FxToolkit.setupStage(stage -> {
            // No view: the cells only read it when an instance is listed, and none is here.
            PatternInstancesPanel panel = new PatternInstancesPanel(null);
            window = new StackPane(panel);
            window.getStyleClass().add("gen-purpose-window");
            window.getStylesheets().add(PatternInstancesPanel.class
                    .getResource("/dev/ikm/komet/kview/mvvm/view/kview.css").toExternalForm());
            stage.setScene(new Scene(window, 518, 600));
            stage.show();
        });
        FxToolkit.setupFixture(() -> window.applyCss());
    }

    @AfterEach
    void hidePanel() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("Every selector chain kview.css uses for the instances panel resolves on the code-built tree")
    void stylesheetSelectorChainsResolve() {
        for (String selector : SELECTORS) {
            assertNotNull(window.lookup(selector), "kview.css selector no longer matches: " + selector);
        }
    }

    @Test
    @DisplayName("Renders the empty panel to target/pattern-instances/panel.png for a visual check")
    void renderPanel() {
        FxRobot robot = new FxRobot();
        Path path = Path.of("target", "pattern-instances", "panel.png");
        robot.interact(() -> {
            path.getParent().toFile().mkdirs();
            CaptureSupport capture = FxService.serviceContext().getCaptureSupport();
            capture.saveImage(robot.capture(window).getImage(), path);
        });
        assertNotNull(window.lookup(".instances-list"));
    }
}
