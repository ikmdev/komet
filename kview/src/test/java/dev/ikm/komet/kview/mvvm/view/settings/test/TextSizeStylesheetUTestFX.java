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

import dev.ikm.komet.framework.settings.KometSettings;
import dev.ikm.komet.framework.settings.TextSize;
import dev.ikm.komet.framework.settings.TextSizeStylesheet;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesServiceFactory;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Subscription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives {@link TextSizeStylesheet} against real windows: the text-size setting must reach the
 * root font of every window that is open, and of every window opened later, and popups must keep
 * following the node that owns them. The settings under test are backed by a throw-away
 * preferences node, removed afterwards.
 */
@ExtendWith(ApplicationExtension.class)
class TextSizeStylesheetUTestFX {

    private static final String TEST_NODE = "dev/ikm/komet/kview/test/text-size-stylesheet";
    private static final double TOLERANCE = 0.01;

    private static KometPreferences preferences;
    private static KometSettings settings;

    private final List<Stage> stages = new ArrayList<>();
    private Subscription installed;

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
    void install() throws Exception {
        FxToolkit.registerPrimaryStage();
        settings.setTextSize(TextSize.DEFAULT);
        installed = FxToolkit.setupFixture(() -> TextSizeStylesheet.install(settings));
    }

    @AfterEach
    void uninstall() throws Exception {
        FxToolkit.setupFixture(() -> {
            installed.unsubscribe();
            stages.forEach(Stage::close);
            stages.clear();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Shows a new window holding one label and returns that label. */
    private Label showLabel() throws Exception {
        Label label = FxToolkit.setupFixture(() -> {
            Label shown = new Label("Myocardial infarction (disorder)");
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(shown), 300, 100));
            stage.show();
            stages.add(stage);
            return shown;
        });
        WaitForAsyncUtils.waitForFxEvents();
        return label;
    }

    /** The label's font size once styles are applied. */
    private static double fontSize(Label label) throws Exception {
        return FxToolkit.setupFixture(() -> {
            label.getScene().getRoot().applyCss();
            return label.getFont().getSize();
        });
    }

    @Test
    void aWindowShowsAtTheBaseFontSizeAtTheDefaultStep() throws Exception {
        Label label = showLabel();
        assertEquals(TextSize.DEFAULT_FONT_SIZE, fontSize(label), TOLERANCE);
        assertTrue(label.getScene().getStylesheets().contains(TextSizeStylesheet.stylesheetUrlFor(TextSize.DEFAULT)));
    }

    @Test
    void changingTheStepReflowsEveryOpenWindow() throws Exception {
        Label first = showLabel();
        Label second = showLabel();

        FxToolkit.setupFixture(() -> settings.setTextSize(TextSize.LARGE));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(TextSize.LARGE.fontSize(), fontSize(first), TOLERANCE);
        assertEquals(TextSize.LARGE.fontSize(), fontSize(second), TOLERANCE);
        assertFalse(first.getScene().getStylesheets().contains(TextSizeStylesheet.stylesheetUrlFor(TextSize.DEFAULT)),
                "the previous step's stylesheet is taken off, not stacked under the new one");
    }

    @Test
    void aWindowOpenedLaterGetsTheCurrentStep() throws Exception {
        FxToolkit.setupFixture(() -> settings.setTextSize(TextSize.EXTRA_LARGE));
        Label label = showLabel();
        assertEquals(TextSize.EXTRA_LARGE.fontSize(), fontSize(label), TOLERANCE);
    }

    @Test
    void aPopupFollowsTheNodeThatOwnsIt() throws Exception {
        Label owner = showLabel();
        double atDefault = tooltipTextSize(owner);

        FxToolkit.setupFixture(() -> settings.setTextSize(TextSize.LARGE));
        WaitForAsyncUtils.waitForFxEvents();
        double atLarge = tooltipTextSize(owner);

        // JavaFX mirrors the owner scene's stylesheets into the popup, so the URL shows up there
        // too, but nothing is done to popups themselves: a popup inherits its font from the node
        // that owns it, so the tooltip (0.85em of that in modena) grows with the label.
        assertEquals(TextSize.LARGE.scale(), atLarge / atDefault, 0.02);
    }

    /** Shows a tooltip on {@code owner}, reads the size of its text, and hides it again. */
    private static double tooltipTextSize(Label owner) throws Exception {
        Tooltip tooltip = FxToolkit.setupFixture(() -> {
            Tooltip shown = new Tooltip("Heart attack");
            shown.show(owner, 0, 0);
            return shown;
        });
        WaitForAsyncUtils.waitForFxEvents();
        return FxToolkit.setupFixture(() -> {
            Label text = (Label) tooltip.getSkin().getNode();
            text.applyCss();
            double size = text.getFont().getSize();
            tooltip.hide();
            return size;
        });
    }

    @Test
    void unsubscribingTakesTheStylesheetOffAndLeavesLaterWindowsAlone() throws Exception {
        FxToolkit.setupFixture(() -> settings.setTextSize(TextSize.LARGE));
        Label before = showLabel();

        FxToolkit.setupFixture(() -> installed.unsubscribe());
        Label after = showLabel();

        double platformDefault = Font.getDefault().getSize();
        assertEquals(platformDefault, fontSize(before), TOLERANCE);
        assertEquals(platformDefault, fontSize(after), TOLERANCE);
        assertFalse(before.getScene().getStylesheets().contains(TextSizeStylesheet.stylesheetUrlFor(TextSize.LARGE)));
        assertTrue(after.getScene().getStylesheets().isEmpty());
    }
}
