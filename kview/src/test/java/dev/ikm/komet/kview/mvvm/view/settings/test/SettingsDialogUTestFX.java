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
package dev.ikm.komet.kview.mvvm.view.settings.test;

import dev.ikm.komet.framework.settings.DisplayLanguage;
import dev.ikm.komet.framework.settings.KometSettings;
import dev.ikm.komet.framework.settings.TextSize;
import dev.ikm.komet.kview.mvvm.view.settings.SettingsDialog;
import dev.ikm.komet.kview.mvvm.view.settings.SettingsDialogController;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesServiceFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.BackingStoreException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads the Settings dialog's FXML the way {@link SettingsDialog} does and drives its pages.
 * The settings under test are backed by a throw-away preferences node, removed afterwards, so
 * the real per-user settings of whoever runs the build are never touched.
 */
@ExtendWith(ApplicationExtension.class)
class SettingsDialogUTestFX {

    private static final String TEST_NODE = "dev/ikm/komet/kview/test/settings-dialog";

    private static KometPreferences preferences;
    private static KometSettings settings;

    private final FxRobot robot = new FxRobot();
    private Parent root;
    private Stage stage;

    @BeforeAll
    static void settingsOnThrowAwayNode() throws BackingStoreException {
        preferences = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
        preferences.clear();
        settings = KometSettings.backedBy(preferences);
    }

    @AfterAll
    static void removeThrowAwayNode() throws BackingStoreException {
        preferences.removeNode();
    }

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();
        showFreshDialog();
    }

    /** Loads the dialog anew, as {@link SettingsDialog#show} does on every opening. */
    private void showFreshDialog() throws Exception {
        FxToolkit.setupStage(stage -> {
            FXMLLoader loader = new FXMLLoader(SettingsDialog.class.getResource("settings-dialog.fxml"));
            loader.setControllerFactory(type -> new SettingsDialogController(settings));
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            this.stage = stage;
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void cleanup() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("Opens on Appearance; the navigation switches pages and cannot land on none")
    void navigationSwitchesPages() {
        assertTrue(page("appearancePage").isVisible());
        assertFalse(page("componentsPage").isVisible());

        robot.clickOn("#componentsNav");
        assertTrue(page("componentsPage").isVisible());
        assertFalse(page("appearancePage").isVisible());

        // Clicking the selected page again keeps it selected.
        robot.clickOn("#componentsNav");
        assertTrue(page("componentsPage").isVisible());
        assertTrue(robot.lookup("#componentsNav").queryAs(ToggleButton.class).isSelected());

        robot.clickOn("#languageNav");
        assertTrue(page("languagePage").isVisible());
    }

    @Test
    @DisplayName("Text-size pills drive the preview and its percentage")
    void textSizePillsDrivePreview() {
        assertEquals("100%", label("textSizePercent").getText());
        String defaultStyle = label("previewTitle").getStyle();

        robot.clickOn("#largePill");
        assertEquals("115%", label("textSizePercent").getText());
        assertTrue(label("previewTitle").getStyle().contains("20.7px"), label("previewTitle").getStyle());

        robot.clickOn("#smallPill");
        assertEquals("85%", label("textSizePercent").getText());

        robot.clickOn("#defaultPill");
        assertEquals(defaultStyle, label("previewTitle").getStyle());
    }

    @Test
    @DisplayName("Multiple parents follows Definition status, and Reset restores the page's defaults")
    void multipleParentsFollowsDefinitionStatus() {
        robot.clickOn("#componentsNav");
        assertFalse(page("multipleParentsRow").isDisabled());

        robot.clickOn("#definitionStatusCheck");
        assertFalse(checkBox("definitionStatusCheck").isSelected());
        assertTrue(page("multipleParentsRow").isDisabled());

        robot.clickOn("#kindSigilCheck");
        assertFalse(checkBox("kindSigilCheck").isSelected());

        robot.clickOn("Reset to default");
        assertTrue(checkBox("definitionStatusCheck").isSelected());
        assertTrue(checkBox("kindSigilCheck").isSelected());
        assertFalse(page("multipleParentsRow").isDisabled());
    }

    @Test
    @DisplayName("Cancel discards the working copy; OK writes it to the settings and closes")
    void okWritesCancelDiscards() throws Exception {
        robot.clickOn("#extraLargePill");
        robot.clickOn("Cancel");
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(stage.isShowing());
        assertEquals(TextSize.DEFAULT, settings.getTextSize());

        showFreshDialog();
        assertEquals("100%", label("textSizePercent").getText());

        robot.clickOn("#largePill");
        robot.clickOn("#componentsNav");
        robot.clickOn("#kindSigilCheck");
        robot.clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(stage.isShowing());
        assertEquals(TextSize.LARGE, settings.getTextSize());
        assertFalse(settings.isShowKindSigil());
        assertTrue(settings.isShowDefinitionStatus());
        assertEquals(DisplayLanguage.SYSTEM_DEFAULT, settings.getDisplayLanguage());
        assertEquals("LARGE", preferences.get("dev.ikm.komet.settings.textSize", ""));
        assertFalse(preferences.getBoolean("dev.ikm.komet.settings.showKindSigil", true));

        settings.setTextSize(TextSize.DEFAULT);
        settings.setShowKindSigil(true);
    }

    @Test
    @DisplayName("Cancel and OK share the same height and baseline")
    void footerButtonsAlign() {
        Region cancel = page("cancelButton");
        Region ok = page("okButton");
        System.err.println("FOOTER cancel=" + cancel.getLayoutBounds() + " ok=" + ok.getLayoutBounds());
        assertEquals(ok.getHeight(), cancel.getHeight(), 0.01);
        assertEquals(ok.localToScene(0, 0).getY(), cancel.localToScene(0, 0).getY(), 0.01);
    }

    @Test
    @DisplayName("Captures each page for a visual check")
    void captureEachPage() throws Exception {
        Path out = Files.createDirectories(Path.of("target", "settings-dialog"));
        save(out.resolve("appearance.png"));
        robot.clickOn("#componentsNav");
        save(out.resolve("components.png"));
        robot.clickOn("#languageNav");
        save(out.resolve("language.png"));
        // The open popup is its own window; lookup reaches into popups, so capture its list directly.
        robot.clickOn("#displayLanguageCombo");
        WaitForAsyncUtils.waitForFxEvents();
        FxService.serviceContext().getCaptureSupport().saveImage(
                robot.capture(robot.lookup(".combo-box-popup .list-view").<Node>query()).getImage(),
                out.resolve("language-popup.png"));
        robot.type(KeyCode.ESCAPE);
    }

    private void save(Path path) {
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(root).getImage(), path);
    }

    private Region page(String fxId) {
        return robot.lookup("#" + fxId).queryAs(Region.class);
    }

    private Label label(String fxId) {
        return robot.lookup("#" + fxId).queryAs(Label.class);
    }

    private CheckBox checkBox(String fxId) {
        return robot.lookup("#" + fxId).queryAs(CheckBox.class);
    }
}
