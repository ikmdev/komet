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

import dev.ikm.komet.kview.controls.KLComponentComboBoxControl;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link KLComponentComboBoxControl} picks its options in a {@code FilterComboBox}, so a user
 * tabbing through a description's fields can type to choose e.g. its case significance
 * (komet-desktop#186). The search matches the rendered component names.
 */
@ExtendWith(ApplicationExtension.class)
class KLComponentComboBoxControlUTestFX {

    private static final EntityProxy.Concept CASE_SENSITIVE =
            EntityProxy.Concept.make("Case sensitive", UUID.randomUUID());
    private static final EntityProxy.Concept CASE_INSENSITIVE =
            EntityProxy.Concept.make("Case insensitive", UUID.randomUUID());
    private static final List<EntityProxy> OPTIONS = List.of(CASE_SENSITIVE, CASE_INSENSITIVE);
    private static final List<String> NAMES = List.of("Case sensitive", "Case insensitive");

    private Stage stage;
    private KLComponentComboBoxControl control;

    /** The control resolves nids (e.g. to tell a blank value), so a store must be running */
    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("component-combo-box-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void showControl() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            this.stage = stage;
            control = new KLComponentComboBoxControl();
            // Renders from the test's own names: these proxies are not in the store
            control.setComponentNameRenderer(proxy -> NAMES.get(OPTIONS.indexOf(proxy)));
            control.getItems().setAll(OPTIONS);
            control.setValue(CASE_SENSITIVE);
            stage.setScene(new Scene(new StackPane(control), 400, 300));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        FxToolkit.setupFixture(() -> comboBox().requestFocus());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeStage() throws Exception {
        FxToolkit.setupFixture(() -> stage.close());
    }

    private ComboBox<EntityProxy> comboBox() {
        return new FxRobot().lookup(".component-combo-box .combo-box").queryAs(ComboBox.class);
    }

    @Test
    void typingTheNameAndEnterPicksTheOption() {
        FxRobot robot = new FxRobot();
        robot.write("insen");
        robot.type(KeyCode.ENTER);

        assertEquals(CASE_INSENSITIVE, control.getValue());
    }

    @Test
    void aBlankValueShowsThePromptText() throws Exception {
        FxToolkit.setupFixture(() -> control.setValue(null));

        assertEquals(control.getPromptText(), comboBox().getButtonCell().getText());
    }
}
