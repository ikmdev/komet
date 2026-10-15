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
package dev.ikm.komet.kview.mvvm.view.navigation.test;

import dev.ikm.komet.kview.controls.ComponentSearchTextField;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.mvvm.view.navigation.ConceptPatternNavController;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.Calculators;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The reference-component search text field over the pattern browser's list, in the browser's own
 * containers (navigation-concept-pattern.fxml's grey column, the search container, the scroll
 * pane below), styled by kview.css on the scene root as the journal has it: the box takes the
 * field styling it has in the KL Pattern window's INSTANCES tab.
 */
class PatternBrowserSearchUTestFX {

    private VBox column;

    @BeforeAll
    static void startStoreAndToolkit() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("pattern-browser-search-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        FxToolkit.registerPrimaryStage();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void showBrowser() throws Exception {
        FxToolkit.setupStage(stage -> {
            ComponentSearchTextField searchTextField = KLComponentControlFactory.createComponentSearchTextField(Calculators.View.Default());
            searchTextField.setPromptText("Search instances by component");
            // A list far taller than the column, as the real browser's is: the search row must not
            // be squeezed to make room for it.
            Region tallList = new Region();
            tallList.setPrefHeight(4000);
            ScrollPane patternList = new ScrollPane(tallList);
            patternList.getStyleClass().add("pattern-navigation-scroll-pane");
            VBox patternsPanel = ConceptPatternNavController.createPatternsPanel(searchTextField, patternList);
            column = new VBox(patternsPanel);
            column.getStyleClass().addAll("concept-pattern-container", "grey-vertical-scroll-area");
            column.getStylesheets().add(ComponentSearchTextField.class
                    .getResource("/dev/ikm/komet/kview/mvvm/view/kview.css").toExternalForm());
            stage.setScene(new Scene(column, 430, 300));
            stage.show();
        });
        FxToolkit.setupFixture(() -> column.applyCss());
    }

    @AfterEach
    void hideBrowser() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("The search field takes the box styling (30px tall) and renders to target/pattern-browser-search/search.png")
    void searchFieldIsStyled() {
        TextField field = (TextField) column.lookup(".component-search-text-field .auto-complete-text-field");
        assertNotNull(field);
        FxRobot robot = new FxRobot();
        robot.interact(() -> {
            column.layout();
            field.requestFocus();
            field.setText("Author");
        });
        Path path = Path.of("target", "pattern-browser-search", "search.png");
        robot.interact(() -> {
            path.getParent().toFile().mkdirs();
            FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(column).getImage(), path);
        });
        assertEquals(30, field.getHeight(), 0.5, "field height");
    }
}
