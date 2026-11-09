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

import dev.ikm.komet.kview.controls.ContentSizedSplitPane;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The sections of a KL window sit in a {@link ContentSizedSplitPane}, and the window closes and
 * opens by a section's content height as the section collapses and expands (see
 * GenPurposeDetailsController.resizeWindowWithSection), and grows by what a section's divider is
 * dragged past the height the sections have between them (see
 * WindowHeightHelper.growBy). This replays the steps of komet-desktop#205 on that
 * pair — a section the user dragged to a height must come back to that height after a collapse,
 * and the other sections must hold theirs throughout — and of komet-desktop#219: a section is
 * enlarged top-down, with the window growing once the lowest section has no more to give.
 */
@ExtendWith(ApplicationExtension.class)
class ContentSizedSplitPaneUTestFX {

    /** Tall enough for the three sections' content, so the last section holds the leftover. */
    private static final double WINDOW_HEIGHT = 800;
    private static final double CONTENT_HEIGHT = 150;
    private static final double TOLERANCE = 1;

    /**
     * The most the window may grow to for a divider drag; what the window's maximum height does
     * for the real one. Unbounded unless a test lowers it.
     */
    private double windowCeiling = Double.MAX_VALUE;

    private VBox window;
    private ContentSizedSplitPane splitPane;
    private TitledPane definition;
    private TitledPane description;
    private TitledPane fields;

    /** What the controller remembers for a collapsed section: the content height it gave up. */
    private final Map<TitledPane, Double> collapsedContentHeights = new HashMap<>();

    @BeforeEach
    void showWindow() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            definition = section("Pattern Definition");
            description = section("Description");
            fields = section("Fields");

            splitPane = new ContentSizedSplitPane();
            splitPane.setOrientation(Orientation.VERTICAL);
            splitPane.getItems().addAll(definition, description, fields);
            splitPane.setGrowHandler(this::growWindowBy);

            window = new VBox(splitPane);
            VBox.setVgrow(splitPane, Priority.ALWAYS);
            window.setPrefHeight(WINDOW_HEIGHT);
            window.setMaxHeight(Region.USE_PREF_SIZE);
            for (TitledPane section : List.of(definition, description, fields)) {
                section.expandedProperty().subscribe((_, expanded) -> resizeWindowWithSection(section, expanded));
            }

            StackPane root = new StackPane(window);
            root.setAlignment(Pos.TOP_LEFT);
            stage.setScene(new Scene(root, 500, 1000));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void hideWindow() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    void draggedSectionReopensAtItsDraggedHeightAndLeavesTheOthersAlone(FxRobot robot) throws Exception {
        // Shrink Fields: the window's bottom edge is dragged up, and the last section follows it.
        double fieldsBefore = fields.getHeight();
        onFxThread(() -> window.setPrefHeight(WINDOW_HEIGHT - 100));
        assertEquals(fieldsBefore - 100, fields.getHeight(), TOLERANCE, "Fields follows the window edge");

        // Increase Pattern Definition: drag the divider below it down. Description moves down with
        // it, and Fields, the lowest section, gives the height up.
        double definitionBefore = definition.getHeight();
        double descriptionBefore = description.getHeight();
        fieldsBefore = fields.getHeight();
        robot.moveTo(dividers().getFirst()).press(MouseButton.PRIMARY).moveBy(0, 60).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(definitionBefore + 60, definition.getHeight(), TOLERANCE, "Pattern Definition grows by the drag");
        assertEquals(descriptionBefore, description.getHeight(), TOLERANCE, "Description holds its height");
        assertEquals(fieldsBefore - 60, fields.getHeight(), TOLERANCE, "Fields gives the drag up");
        assertEquals(WINDOW_HEIGHT - 100, window.getHeight(), TOLERANCE, "the window has room, so it doesn't grow");

        double definitionHeight = definition.getHeight();
        double descriptionHeight = description.getHeight();
        double fieldsHeight = fields.getHeight();

        // Collapse Pattern Definition: it closes to its title bar, the others hold their heights.
        onFxThread(() -> definition.setExpanded(false));
        assertEquals(definition.prefHeight(-1), definition.getHeight(), TOLERANCE, "collapsed to its title bar");
        assertEquals(descriptionHeight, description.getHeight(), TOLERANCE, "Description unaffected by the collapse");
        assertEquals(fieldsHeight, fields.getHeight(), TOLERANCE, "Fields unaffected by the collapse");

        // Expand it again: back to the height it was dragged to, the others still untouched.
        onFxThread(() -> definition.setExpanded(true));
        assertEquals(definitionHeight, definition.getHeight(), TOLERANCE, "Pattern Definition back to its dragged height");
        assertEquals(descriptionHeight, description.getHeight(), TOLERANCE, "Description unaffected by the expand");
        assertEquals(fieldsHeight, fields.getHeight(), TOLERANCE, "Fields unaffected by the expand");
    }

    @Test
    void lastSectionReopensAtItsHeightAfterAWindowResizeWhileCollapsed(FxRobot robot) throws Exception {
        // Drag the divider above Fields down, so Fields holds a height the user chose.
        robot.moveTo(dividers().getLast()).press(MouseButton.PRIMARY).moveBy(0, 40).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        double definitionHeight = definition.getHeight();
        double descriptionHeight = description.getHeight();
        double fieldsHeight = fields.getHeight();

        onFxThread(() -> fields.setExpanded(false));
        assertEquals(definitionHeight, definition.getHeight(), TOLERANCE, "Pattern Definition unaffected by the collapse");
        assertEquals(descriptionHeight, description.getHeight(), TOLERANCE, "Description unaffected by the collapse");

        // With Fields closed, the window's edge resizes the lowest open section.
        onFxThread(() -> window.setPrefHeight(window.getPrefHeight() + 80));
        assertEquals(definitionHeight, definition.getHeight(), TOLERANCE, "Pattern Definition unaffected by the window resize");
        assertEquals(descriptionHeight + 80, description.getHeight(), TOLERANCE, "Description takes the window's growth");

        onFxThread(() -> fields.setExpanded(true));
        assertEquals(fieldsHeight, fields.getHeight(), TOLERANCE, "Fields back to its dragged height");
        assertEquals(definitionHeight, definition.getHeight(), TOLERANCE, "Pattern Definition unaffected by the expand");
        assertEquals(descriptionHeight + 80, description.getHeight(), TOLERANCE, "Description keeps the window's growth");
    }

    @Test
    void dividerDraggedPastTheLowestSectionGrowsTheWindow(FxRobot robot) throws Exception {
        takeSizingOver(robot);
        // Shrink the window until Fields, the lowest section, is down at its minimum: it has
        // nothing left to give a section above it.
        double fieldsMin = fields.minHeight(-1);
        onFxThread(() -> window.setPrefHeight(WINDOW_HEIGHT - (fields.getHeight() - fieldsMin)));
        assertEquals(fieldsMin, fields.getHeight(), TOLERANCE, "Fields at its minimum");
        double windowBefore = window.getHeight();
        double definitionBefore = definition.getHeight();
        double descriptionBefore = description.getHeight();

        // Drag the divider below Pattern Definition down: the window grows by the drag, the
        // sections below move down with it.
        robot.moveTo(dividers().getFirst()).press(MouseButton.PRIMARY).moveBy(0, 60).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(definitionBefore + 60, definition.getHeight(), TOLERANCE, "Pattern Definition grows by the drag");
        assertEquals(descriptionBefore, description.getHeight(), TOLERANCE, "Description holds its height");
        assertEquals(fieldsMin, fields.getHeight(), TOLERANCE, "Fields stays at its minimum");
        assertEquals(windowBefore + 60, window.getHeight(), TOLERANCE, "the window grows by the drag");
    }

    @Test
    void dividerDraggedDownTakesFromTheLowestSectionBeforeGrowingTheWindow(FxRobot robot) throws Exception {
        takeSizingOver(robot);
        // Leave Fields 30px above its minimum.
        double fieldsMin = fields.minHeight(-1);
        onFxThread(() -> window.setPrefHeight(WINDOW_HEIGHT - (fields.getHeight() - fieldsMin) + 30));
        double windowBefore = window.getHeight();
        double descriptionBefore = description.getHeight();

        // A drag of 100 takes the 30 Fields has to give, then grows the window by the other 70.
        robot.moveTo(dividers().getLast()).press(MouseButton.PRIMARY).moveBy(0, 100).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(descriptionBefore + 100, description.getHeight(), TOLERANCE, "Description grows by the drag");
        assertEquals(fieldsMin, fields.getHeight(), TOLERANCE, "Fields gives what it has, down to its minimum");
        assertEquals(windowBefore + 70, window.getHeight(), TOLERANCE, "the window grows by the rest");
    }

    @Test
    void dividerDraggedUpGrowsTheLowestSectionAndLeavesTheWindowAlone(FxRobot robot) throws Exception {
        double definitionBefore = definition.getHeight();
        double descriptionBefore = description.getHeight();
        double fieldsBefore = fields.getHeight();

        robot.moveTo(dividers().getFirst()).press(MouseButton.PRIMARY).moveBy(0, -50).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(definitionBefore - 50, definition.getHeight(), TOLERANCE, "Pattern Definition shrinks by the drag");
        assertEquals(descriptionBefore, description.getHeight(), TOLERANCE, "Description holds its height");
        assertEquals(fieldsBefore + 50, fields.getHeight(), TOLERANCE, "Fields takes the freed height");
        assertEquals(WINDOW_HEIGHT, window.getHeight(), TOLERANCE, "the window never shrinks");
    }

    @Test
    void sectionGrowsOnlyByWhatTheWindowCan(FxRobot robot) throws Exception {
        takeSizingOver(robot);
        double fieldsMin = fields.minHeight(-1);
        onFxThread(() -> window.setPrefHeight(WINDOW_HEIGHT - (fields.getHeight() - fieldsMin)));
        double windowBefore = window.getHeight();
        double definitionBefore = definition.getHeight();
        windowCeiling = windowBefore + 20;

        robot.moveTo(dividers().getFirst()).press(MouseButton.PRIMARY).moveBy(0, 60).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(windowBefore + 20, window.getHeight(), TOLERANCE, "the window grows to its ceiling");
        assertEquals(definitionBefore + 20, definition.getHeight(), TOLERANCE, "Pattern Definition grows by what the window could");
        assertEquals(fieldsMin, fields.getHeight(), TOLERANCE, "Fields stays at its minimum");
    }

    @Test
    void contentSizedWindowGrowsWithTheDrag(FxRobot robot) throws Exception {
        // A window with no height of its own takes the height its sections ask for.
        onFxThread(() -> window.setPrefHeight(Region.USE_COMPUTED_SIZE));
        double windowBefore = window.getHeight();
        double descriptionBefore = description.getHeight();
        double fieldsRoom = fields.getHeight() - fields.minHeight(-1);

        // Fields gives what it has above its minimum, and the window grows by the rest.
        double drag = fieldsRoom + 50;
        robot.moveTo(dividers().getLast()).press(MouseButton.PRIMARY).moveBy(0, drag).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(descriptionBefore + drag, description.getHeight(), TOLERANCE, "Description grows by the drag");
        assertEquals(fields.minHeight(-1), fields.getHeight(), TOLERANCE, "Fields down to its minimum");
        assertEquals(windowBefore + 50, window.getHeight(), TOLERANCE, "the window grows by what Fields couldn't give");
    }

    /**
     * Grabs and lets go of a divider without moving it, which hands the sizing over to the user:
     * from here on the sections hold their heights and the lowest one follows the window, where
     * before the pane shares a window resize out between them.
     */
    private void takeSizingOver(FxRobot robot) {
        robot.moveTo(dividers().getFirst()).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * What WindowHeightHelper.growBy does for a divider dragged past the height the
     * sections have between them: grows the window by it, within the ceiling this test sets. A
     * window sized to its content follows the sections on its own.
     */
    private double growWindowBy(double delta) {
        if (window.getPrefHeight() <= 0) {
            return delta;
        }
        double granted = Math.max(0, Math.min(delta, windowCeiling - window.getPrefHeight()));
        window.setPrefHeight(window.getPrefHeight() + granted);
        return granted;
    }

    /**
     * What GenPurposeDetailsController.resizeWindowWithSection does: the window closes by the
     * content height a collapsing section gives up, and opens by that same height when it expands.
     */
    private void resizeWindowWithSection(TitledPane section, boolean expanded) {
        Region content = (Region) section.getContent();
        double delta;
        if (expanded) {
            delta = collapsedContentHeights.remove(section);
        } else {
            delta = -content.getHeight();
            collapsedContentHeights.put(section, content.getHeight());
        }
        window.setPrefHeight(window.getPrefHeight() + delta);
    }

    private static TitledPane section(String title) {
        Region content = new Region();
        content.setPrefHeight(CONTENT_HEIGHT);
        content.setMinHeight(40);
        TitledPane section = new TitledPane(title, content);
        section.setAnimated(false);
        section.setMaxHeight(Double.MAX_VALUE);
        return section;
    }

    /** The split pane's divider nodes, top to bottom. */
    private List<Node> dividers() {
        return splitPane.lookupAll(".split-pane-divider").stream()
                .sorted(Comparator.comparingDouble(Node::getLayoutY))
                .toList();
    }

    private static void onFxThread(Runnable action) throws Exception {
        FxToolkit.setupFixture(action::run);
        WaitForAsyncUtils.waitForFxEvents();
    }
}
