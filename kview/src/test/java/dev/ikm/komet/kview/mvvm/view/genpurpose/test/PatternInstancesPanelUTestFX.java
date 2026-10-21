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
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.Calculators;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;
import org.testfx.service.support.CaptureSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Every selector chain kview.css styles the INSTANCES tab's panel with resolves on the code-built
 * tree — the panel is only styled under the window's {@code .gen-purpose-window} class, so the
 * test wraps it the way the window does, with the stylesheet (and its palette on {@code .root})
 * on the scene root as the window has it. The panel's search field is built from a view
 * calculator, so the ephemeral store backs a default one.
 */
class PatternInstancesPanelUTestFX {

    private static final List<String> SELECTORS = List.of(
            ".gen-purpose-window .pattern-instances",
            ".pattern-instances .instances-header",
            ".instances-header .instances-title-bar",
            ".instances-title-bar .lidr-device-title-label",
            ".instances-title-bar .instances-count",
            ".instances-header .component-search-text-field",
            ".component-search-text-field .auto-complete-text-field",
            ".component-search-text-field .search-icon",
            ".component-search-text-field .filter-icon",
            ".pattern-instances .instances-list");

    private StackPane window;

    @BeforeAll
    static void startStoreAndToolkit() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("pattern-instances-test").toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        FxToolkit.registerPrimaryStage();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void showPanel() throws Exception {
        FxToolkit.setupStage(stage -> {
            PatternInstancesPanel panel = new PatternInstancesPanel(Calculators.View.Default());
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
    @DisplayName("Renders the panel, with text in its search field, to target/pattern-instances/panel.png for a visual check")
    void renderPanel() {
        FxRobot robot = new FxRobot();
        Path path = Path.of("target", "pattern-instances", "panel.png");
        // Text in the focused field brings up its clear button, next to the filter glyph.
        robot.interact(() -> {
            var field = (javafx.scene.control.TextField) window.lookup(".component-search-text-field .auto-complete-text-field");
            field.requestFocus();
            field.setText("Double");
        });
        robot.interact(() -> {
            path.getParent().toFile().mkdirs();
            CaptureSupport capture = FxService.serviceContext().getCaptureSupport();
            capture.saveImage(robot.capture(window).getImage(), path);
        });
        assertNotNull(window.lookup(".instances-list"));
    }
}
