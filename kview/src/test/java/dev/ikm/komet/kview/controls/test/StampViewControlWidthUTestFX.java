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

import dev.ikm.komet.framework.settings.TextSize;
import dev.ikm.komet.framework.settings.TextSizeStylesheet;
import dev.ikm.komet.kview.controls.StampViewControl;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The stamp block's width follows the font: at a larger text size the block widens rather than
 * clipping "Module:" and "Path:" to an ellipsis. The width is one em rule in the control's
 * stylesheet, so nothing in code or FXML may pin it in px.
 */
@ExtendWith(ApplicationExtension.class)
class StampViewControlWidthUTestFX {

    /** {@code -fx-pref-width} in stamp-view-control.css, in em. */
    private static final double WIDTH_EM = 17.5;
    private static final double TOLERANCE = 0.01;

    private Stage stage;
    private StampViewControl stamp;

    @BeforeEach
    void showStamp() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            this.stage = stage;
            stamp = new StampViewControl();
            stamp.setModule("SNOMED CT core module");
            stamp.setPath("Development path");
            stage.setScene(new Scene(new StackPane(stamp), 600, 300));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeStage() throws Exception {
        FxToolkit.setupFixture(() -> stage.close());
    }

    private double prefWidthAt(TextSize size) throws Exception {
        return FxToolkit.setupFixture(() -> {
            stage.getScene().getStylesheets().removeIf(url -> url.startsWith("data:"));
            stage.getScene().getStylesheets().add(TextSizeStylesheet.stylesheetUrlFor(size));
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            return stamp.prefWidth(-1);
        });
    }

    @Test
    void theWidthIsAnEmMultipleOfTheRootFontSize() throws Exception {
        assertEquals(WIDTH_EM * TextSize.DEFAULT.fontSize(), prefWidthAt(TextSize.DEFAULT), TOLERANCE);
        assertEquals(WIDTH_EM * TextSize.EXTRA_LARGE.fontSize(), prefWidthAt(TextSize.EXTRA_LARGE), TOLERANCE);
    }
}
