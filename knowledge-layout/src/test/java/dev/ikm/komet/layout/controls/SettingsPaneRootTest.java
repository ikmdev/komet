/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.layout.controls;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Locks the settings pane's structure and navigation ({@code IKE-Network/ike-issues#1043}):
 * the root carries the shared View Options style family, sections register as cards with live
 * summaries, drill-in swaps the body to the section's freshly built content, and drill-back
 * returns to the list with summaries re-read. If the JavaFX toolkit cannot start (headless),
 * the tests skip rather than fail — the {@code KlDrawerTest} idiom.
 */
class SettingsPaneRootTest {

    private static boolean fxReady;

    @BeforeAll
    static void startFx() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            fxReady = latch.await(5, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyRunning) {
            fxReady = true;
        } catch (Exception e) {
            fxReady = false;
        }
    }

    private static void onFx(Runnable work) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                work.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX work completed");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    @Test
    void sharesTheViewOptionsStyleFamilyAndNavigates() throws Exception {
        assumeTrue(fxReady, "JavaFX toolkit unavailable (headless)");
        onFx(() -> {
            AtomicReference<String> summaryValue = new AtomicReference<>("13 px");
            int[] contentBuilds = {0};
            SettingsPaneRoot root = new SettingsPaneRoot("Assistant Settings", () -> { });
            root.addSection("Text size", summaryValue::get, () -> {
                contentBuilds[0]++;
                return new Label("content " + contentBuilds[0]);
            });
            root.addSection("API key", () -> "Not set", () -> new Label("key"));

            assertTrue(root.getStyleClass().contains("filter-options-popup"),
                    "the root wears the shared View Options family — one stylesheet, no drift");
            assertTrue(root.getStyleClass().contains("settings-pane"));
            assertEquals(2, root.sectionCount());
            assertTrue(root.isShowingSections(), "the pane opens on the section list");

            // Drill in: content built fresh; the list is replaced.
            Label card = (Label) root.lookup(".section-title");
            assertEquals("TEXT SIZE", card.getText(), "card titles render upper-case per the grammar");
            root.lookup(".settings-section-card").getOnMouseClicked()
                    .handle(null);
            assertFalse(root.isShowingSections(), "drill-in replaces the list");
            assertEquals(1, contentBuilds[0], "content is built on entry");

            // Back: list returns and summaries re-read.
            summaryValue.set("15 px");
            root.showSections();
            assertTrue(root.isShowingSections());
            assertTrue(root.lookupAll(".section-value").stream()
                            .anyMatch(node -> node instanceof Label label
                                    && "15 px".equals(label.getText())),
                    "drill-back re-reads the live summary");
        });
    }

    @Test
    void drillContentCheckBoxTextIsReadableOnTheDarkGround() throws Exception {
        assumeTrue(fxReady, "JavaFX toolkit unavailable (headless)");
        onFx(() -> {
            SettingsPaneRoot root = new SettingsPaneRoot("Assistant Settings", () -> { });
            CheckBox smallCaps = new CheckBox("Small caps");
            root.addSection("Chip labels", () -> "Small caps", () -> smallCaps);
            // Styling only resolves inside a scene; the popup normally provides one.
            new Scene(root);
            root.lookup(".settings-section-card").getOnMouseClicked().handle(null);
            root.applyCss();
            smallCaps.applyCss();
            assertEquals(Color.web("#E1E8F1"), smallCaps.getTextFill(),
                    "drill-content checkbox text carries the readable fill — a CheckBox is not"
                            + " a .label, so the dark-ground rule must name it (ike-issues#1050)");
        });
    }
}
