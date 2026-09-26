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
package dev.ikm.komet.kview.klwindows.test;

import dev.ikm.komet.kview.klwindows.AbstractChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A chapter window's saved geometry survives the save its constructor makes before the window is
 * laid out, and a size that was never measured restores as content-sized rather than zero
 * (IKE-Network/ike-issues#1148).
 */
class ChapterWindowGeometryTest {

    /** A chapter window over a bare pane, with no view, preferences, or entity. */
    private static final class TestWindow extends AbstractChapterKlWindow<Pane> {
        private final UUID topic = UUID.randomUUID();

        TestWindow() {
            super(null, null);
            paneWindow = new Pane();
        }

        void seed(EntityKlWindowState state) {
            setWindowState(state);
        }

        @Override
        public UUID getWindowTopic() {
            return topic;
        }

        @Override
        public EntityKlWindowType getWindowType() {
            return EntityKlWindowTypes.CONCEPT;
        }

        @Override
        public void onShown() {
        }

        @Override
        protected boolean isPropertyPanelOpen() {
            return false;
        }

        @Override
        protected void setPropertyPanelOpen(boolean isOpen) {
        }

        @Override
        protected String selectedPropertyPanel() {
            return null;
        }

        @Override
        protected void setSelectedPropertyPanel(String propertyPanel) {
        }

        @Override
        protected void captureAdditionalState(EntityKlWindowState state) {
        }

        @Override
        protected void applyAdditionalState(EntityKlWindowState state) {
        }
    }

    private static EntityKlWindowState saved(TestWindow window, double x, double y, double width, double height) {
        return EntityKlWindowState.builder()
                .windowId(window.getWindowTopic())
                .windowType(window.getWindowType())
                .position(x, y)
                .size(width, height)
                .build();
    }

    @Test
    void captureBeforeLayout_keepsTheSavedGeometry() {
        TestWindow window = new TestWindow();
        window.seed(saved(window, 120, 80, 672, 980));

        // The window is not in a scene yet, so its own geometry reads as zero.
        EntityKlWindowState captured = window.captureWindowState();

        assertEquals(120, captured.getXPos());
        assertEquals(80, captured.getYPos());
        assertEquals(672, captured.getWidth());
        assertEquals(980, captured.getHeight());
    }

    @Test
    void captureBeforeLayout_withNothingSaved_recordsNoSize() {
        TestWindow window = new TestWindow();

        EntityKlWindowState captured = window.captureWindowState();

        assertEquals(0, captured.getWidth());
        assertEquals(0, captured.getHeight());
    }

    @Test
    void applyUnmeasuredSize_leavesTheWindowContentSized() {
        TestWindow window = new TestWindow();
        window.fxObject().setPrefSize(672, 377);

        window.applyWindowState(saved(window, 10, 20, 0, 0));

        assertEquals(Region.USE_COMPUTED_SIZE, window.fxObject().getPrefWidth());
        assertEquals(Region.USE_COMPUTED_SIZE, window.fxObject().getPrefHeight());
        assertEquals(10, window.fxObject().getLayoutX());
        assertEquals(20, window.fxObject().getLayoutY());
    }

    @Test
    void applyMeasuredSize_setsIt() {
        TestWindow window = new TestWindow();

        window.applyWindowState(saved(window, 0, 0, 700, 499));

        assertEquals(700, window.fxObject().getPrefWidth());
        assertEquals(499, window.fxObject().getPrefHeight());
    }
}
