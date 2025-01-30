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
package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLDropRegion;
import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.fxutils.window.WindowSupport;
import dev.ikm.komet.kview.klwindows.ChapterKlWindow;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.ikm.komet.kview.controls.KLWorkspace.*;

/**
 * A custom skin implementation for the {@link KLWorkspace} control. This skin sets up
 * a scrollable "desktop" area in which {@code ChapterKlWindow} objects.
 *
 * <p>Primary features include:</p>
 * <ul>
 *     <li>A fallback "3-row" placement logic for automatically positioning windows.</li>
 *     <li>Panning the desktop pane (via holding down the Control key).</li>
 *     <li>Displaying a drop region when external data is dragged over the workspace.</li>
 *     <li>Adding or removing windows from the desktop in response to changes in the
 *         workspace's list of {@code ChapterKlWindow} objects.</li>
 *     <li>Resizing and dragging support for individual windows via {@link WindowSupport}.</li>
 * </ul>
 */
public class KLWorkspaceSkin extends SkinBase<KLWorkspace> {

    /**
     * The pane that holds all {@link ChapterKlWindow} nodes within the workspace.
     * This pane is scrollable through a {@link ScrollPane}.
     */
    private final DesktopPane desktopPane;

    /**
     * The {@link ScrollPane} that allows panning around the {@code desktopPane}.
     */
    private final ScrollPane desktopScrollPane;

    /**
     * The current list of {@link ChapterKlWindow} instances from the {@link KLWorkspace}.
     */
    private ObservableList<ChapterKlWindow<Pane>> workspaceWindows;

    /**
     * Listener to track changes in the workspace's window list, such as additions or removals.
     */
    private final ListChangeListener<ChapterKlWindow<Pane>> windowsListChangeListener;

    /**
     * A weak wrapper around {@link #windowsListChangeListener} to avoid strong reference leaks.
     */
    private final WeakListChangeListener<ChapterKlWindow<Pane>> weakWindowsListChangeListener;

    /**
     * Constructs a new skin for the provided {@link KLWorkspace}.
     *
     * @param workspace The KLWorkspace to be skinned.
     */
    public KLWorkspaceSkin(KLWorkspace workspace) {
        super(workspace);

        // --------------------------------------------------------------------
        // 1) Initialize the Desktop Pane
        // --------------------------------------------------------------------
        this.desktopPane = new DesktopPane(workspace);
        this.desktopPane.getStyleClass().add(DESKTOP_PANE_STYLE_CLASS);
        this.desktopPane.setHGap(workspace.getHorizontalGap());
        this.desktopPane.setVGap(workspace.getVerticalGap());

        // --------------------------------------------------------------------
        // 2) Prepare the ScrollPane
        // --------------------------------------------------------------------
        this.desktopScrollPane = new ScrollPane(desktopPane);
        this.desktopPane.setPrefWidth(STANDARD_WIDTH * COLUMNS);
        this.desktopPane.setPrefHeight(STANDARD_HEIGHT * ROWS);

        // Place the ScrollPane into this skin's children
        getChildren().add(desktopScrollPane);

        // --------------------------------------------------------------------
        // 3) Listen for windows being added/removed
        // --------------------------------------------------------------------
        this.windowsListChangeListener = change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    // Remove the ChapterKlWindows' root panes from the desktop
                    desktopPane.getChildren().removeAll(
                            change.getRemoved().stream()
                                    .map(ChapterKlWindow::getRootPane)
                                    .toList()
                    );
                }
                if (change.wasAdded()) {
                    // For each newly added window, place it in the workspace
                    change.getAddedSubList().forEach(this::addWindow);
                }
            }
        };
        this.weakWindowsListChangeListener = new WeakListChangeListener<>(windowsListChangeListener);

        // Initialize the current windows list and attach the listener
        updateWorkspaceWindows();

        // If there are already windows in the workspace, add them now
        for (ChapterKlWindow<Pane> window : workspaceWindows) {
            addWindow(window);
        }

        // --------------------------------------------------------------------
        // 4) Configure user interactions (panning and drag-drop)
        // --------------------------------------------------------------------
        configurePanningHandlers(workspace);
        configureDragDropHandlers(workspace);

        // --------------------------------------------------------------------
        // 5) Listen for property changes that affect layout
        // --------------------------------------------------------------------
        registerChangeListener(workspace.windowsProperty(), o -> updateWorkspaceWindows());
        registerChangeListener(workspace.horizontalGapProperty(), o -> {
            desktopPane.setHGap(workspace.getHorizontalGap());
            desktopPane.requestLayout();
        });
        registerChangeListener(workspace.verticalGapProperty(), o -> {
            desktopPane.setVGap(workspace.getVerticalGap());
            desktopPane.requestLayout();
        });
    }

    // =========================================================================
    //                                PANNING
    // =========================================================================

    /**
     * Configures the workspace to support panning with Ctrl+drag.
     *
     * <p>When the Control key is pressed:
     * <ul>
     *     <li>The {@code desktopPane} becomes mouse-transparent.</li>
     *     <li>The {@code desktopScrollPane} is set to pannable.</li>
     *     <li>The mouse cursor changes to an open-hand (grab) icon.</li>
     * </ul>
     * When the Control key is released, these changes are reverted.</p>
     *
     * @param workspace the {@link KLWorkspace} to be panned.
     */
    private void configurePanningHandlers(KLWorkspace workspace) {
        // Activate panning when Ctrl is pressed
        workspace.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            // Enable panning when the Control key is pressed.
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                desktopPane.setMouseTransparent(true);
                desktopScrollPane.setPannable(true);
                StackPane viewport = (StackPane) desktopScrollPane.lookup(".viewport");
                viewport.setCursor(Cursor.OPEN_HAND); // Indicate dragging with an open hand
                desktopScrollPane.requestFocus();
            }
        });

        workspace.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            // Disable panning and restore cursor when the Control key is released.
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                desktopPane.setMouseTransparent(false);
                desktopScrollPane.setPannable(false);
                StackPane viewport = (StackPane) desktopScrollPane.lookup(".viewport");
                viewport.setCursor(Cursor.DEFAULT); // Revert to default cursor
            }
        });

        // Register mouse event handlers on the scroll pane to provide visual cues during panning.
        desktopScrollPane.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown() && desktopScrollPane.isPannable()) {
                StackPane viewport = (StackPane) desktopScrollPane.lookup(".viewport");
                viewport.setCursor(Cursor.CLOSED_HAND); // Indicate active dragging
            }
        });

        desktopScrollPane.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            if (desktopScrollPane.isPannable()) {
                StackPane viewport = (StackPane) desktopScrollPane.lookup(".viewport");
                viewport.setCursor(Cursor.OPEN_HAND); // Revert to open hand after drag
            }
        });
    }

    // =========================================================================
    //                                DRAG & DROP
    // =========================================================================

    /**
     * Configures drag-over and drag-exit handlers for placing new windows in the workspace.
     *
     * <p>When a valid item (e.g., a string payload in the clipboard) is dragged over the
     * {@code KLWorkspace}, the fixed drop region at the top-left is shown. If the user
     * drops the item, the newly created window will appear at (0, 0) or wherever you have
     * chosen within the skin logic.</p>
     *
     * @param workspace The {@link KLWorkspace} control for which drag handling is being configured.
     */
    private void configureDragDropHandlers(KLWorkspace workspace) {
        workspace.setOnDragOver(event -> {
            desktopPane.showDropRegion();
            // Accept drag if the payload has string content (adjust as needed)
            if (event.getGestureSource() != null && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        // Hide the drop region when the drag exits the workspace
        workspace.setOnDragExited(event -> desktopPane.hideDropRegion());
    }

    // =========================================================================
    //                               ADD WINDOWS
    // =========================================================================

    /**
     * Adds a {@link ChapterKlWindow} to the {@code desktopPane}.
     *
     * <p>The logic for determining the initial placement of the new window is as follows:</p>
     * <ol>
     *     <li>If the drop region is visible, place the window at the drop region coordinates (top-left).</li>
     *     <li>If the window has a saved position that fits within the desktop and does not overlap
     *         existing windows, use that saved position.</li>
     *     <li>Otherwise, fall back on a "3-row" occupant-based placement strategy.</li>
     * </ol>
     *
     * @param window The {@link ChapterKlWindow} to be added.
     */
    private void addWindow(ChapterKlWindow<Pane> window) {
        Pane windowPanel = window.getRootPane();
        // Make the window draggable/resizable
        new WindowSupport(windowPanel, desktopPane);

        // Apply a maximum height constraint to the window panel
        windowPanel.setMaxHeight(KLWorkspace.MAX_WINDOW_HEIGHT);

        final KLDropRegion dropRegion = desktopPane.getDropRegion();
        final KLWorkspace workspace = getSkinnable();

        double desktopWidth = (desktopPane.getWidth() > 0)
                ? desktopPane.getWidth()
                : desktopPane.getPrefWidth();

        double desktopHeight = (desktopPane.getHeight() > 0)
                ? desktopPane.getHeight()
                : desktopPane.getPrefHeight();

        // Default size for a new window
        final double windowWidth = DEFAULT_WINDOW_WIDTH;
        final double windowHeight = DEFAULT_WINDOW_HEIGHT;

        // --------------------------------------------------------------------
        // 1) If the drop region is visible, place the window at the drop spot
        // --------------------------------------------------------------------
        if (dropRegion.isVisible()) {
            windowPanel.setTranslateX(dropRegion.getLayoutX());
            windowPanel.setTranslateY(dropRegion.getLayoutY());
            windowPanel.setPrefWidth(dropRegion.getWidth());
            windowPanel.setPrefHeight(dropRegion.getHeight());
        }

        // --------------------------------------------------------------------
        // 2) If the window has a saved position that is valid and non-overlapping, use it
        // --------------------------------------------------------------------
        final double savedX = windowPanel.getTranslateX();
        final double savedY = windowPanel.getTranslateY();
        boolean hasSavedPos = (savedX != 0 || savedY != 0);

        if (hasSavedPos) {
            boolean fits = fitsInDesktop(savedX, savedY, windowWidth, windowHeight,
                    desktopWidth, desktopHeight);
            boolean noOverlap = !overlapsWithExistingWindows(savedX, savedY, windowWidth, windowHeight);

            if (fits && noOverlap) {
                windowPanel.setPrefSize(windowWidth, windowHeight);
                desktopPane.getChildren().add(windowPanel);
                // No auto-scrolling for returning windows
                return;
            }
        }

        // --------------------------------------------------------------------
        // 3) Otherwise, use the occupant-based 3-row approach
        // --------------------------------------------------------------------
        Point2D placement = findThreeRowPlacement(windowWidth, windowHeight,
                desktopWidth, desktopHeight, workspace.getHorizontalGap(), workspace.getVerticalGap());

        if (dropRegion.isVisible()) {
            desktopPane.getChildren().add(windowPanel);
        } else if (placement != null) {
            windowPanel.setTranslateX(placement.getX());
            windowPanel.setTranslateY(placement.getY());
            windowPanel.setPrefSize(windowWidth, windowHeight);
            desktopPane.getChildren().add(windowPanel);

            // Auto-scroll the workspace to reveal the newly placed window
            autoScrollToTopEdge(windowPanel);
        }
    }

    /**
     * Attempts to find a free spot among three horizontal rows without overlapping existing windows.
     * Each row is approximately one-third of the total desktop height.
     *
     * <p>The method generates candidate (x, y) positions in each row, checking for collisions with
     * existing windows. If a collision is found, the y-position is incremented to skip past the
     * obstructing window. If a valid, non-overlapping slot is identified, it returns the first match.</p>
     *
     * @return A {@link Point2D} indicating the top-left corner for the new window, or {@code null} if no suitable spot is found.
     */
    private Point2D findThreeRowPlacement(double width, double height,
                                          double desktopWidth, double desktopHeight,
                                          double hgap, double vgap) {
        double rowHeight = desktopHeight / 3.0;

        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
            double rowTop = rowIndex * rowHeight;
            double rowBottom = rowTop + rowHeight;

            // Skip this row if it cannot accommodate the window's height (with gap)
            if (height > (rowHeight - 2 * vgap)) {
                continue;
            }

            // Collect windows that intersect this row (by vertical overlap)
            List<Bounds> rowOccupants = desktopPane.getChildren().stream()
                    .filter(n -> n != desktopPane.getDropRegion() && n.isVisible())
                    .map(Node::getBoundsInParent)
                    .filter(b -> b.getMaxY() > rowTop && b.getMinY() < rowBottom)
                    .sorted(Comparator.comparingDouble(Bounds::getMinX))
                    .toList();

            double rowLeft = 0;
            double rowRight = desktopWidth;

            // Initial candidate x-position is just the left edge plus the horizontal gap
            List<Double> xCandidates = new ArrayList<>();
            xCandidates.add(rowLeft + hgap);

            // Generate additional x-candidates by moving to the right of each occupant
            for (Bounds occ : rowOccupants) {
                double candidateX = occ.getMaxX() + hgap;
                if (candidateX + width <= rowRight) {
                    xCandidates.add(candidateX);
                }
            }
            xCandidates = xCandidates.stream().distinct().sorted().toList();

            // Attempt to place the window at each candidate x-position,
            // iterating downward until there's no overlap or the row ends
            for (double xCand : xCandidates) {
                double yCursor = rowTop + vgap;
                while ((yCursor + height) <= (rowBottom - vgap)) {
                    BoundingBox candidateBox = new BoundingBox(xCand, yCursor, width, height);
                    boolean overlaps = rowOccupants.stream().anyMatch(b -> b.intersects(candidateBox));
                    if (!overlaps) {
                        return new Point2D(xCand, yCursor);
                    }
                    // If overlapping, jump to below the blocking occupant
                    Bounds blocking = rowOccupants.stream()
                            .filter(b -> b.intersects(candidateBox))
                            .findFirst()
                            .orElse(null);
                    if (blocking == null) {
                        yCursor += (height + vgap);
                    } else {
                        yCursor = blocking.getMaxY() + vgap;
                    }
                }
            }
        }
        // No valid placement found
        return null;
    }

    // =========================================================================
    //                             SCROLLING UTILS
    // =========================================================================

    /**
     * Smoothly scrolls the {@link ScrollPane} so that the top edge of the specified node
     * is visible. Uses a brief animation for a user-friendly experience.
     *
     * @param nodeToView The node to bring into view (toward the top).
     */
    private void autoScrollToTopEdge(Node nodeToView) {
        // Force layout to obtain accurate bounds
        desktopPane.layout();

        Bounds nodeBounds = nodeToView.localToScene(nodeToView.getBoundsInLocal());
        Bounds desktopBounds = desktopPane.localToScene(desktopPane.getBoundsInLocal());
        Bounds viewportBounds = desktopScrollPane.getViewportBounds();

        double contentWidth = desktopPane.getBoundsInLocal().getWidth();
        double contentHeight = desktopPane.getBoundsInLocal().getHeight();

        // Calculate node's top relative to the desktop
        double nodeTopY = nodeBounds.getMinY() - desktopBounds.getMinY() - DEFAULT_VERTICAL_GAP;
        // Center horizontally on the node
        double nodeCenterX = nodeBounds.getMinX() + (nodeBounds.getWidth() / 2.0)
                - desktopBounds.getMinX();

        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();

        double desiredLeft = nodeCenterX - (viewportWidth / 2.0);
        double desiredTop = nodeTopY;

        double maxX = Math.max(0, contentWidth - viewportWidth);
        double maxY = Math.max(0, contentHeight - viewportHeight);

        double startH = desktopScrollPane.getHvalue();
        double startV = desktopScrollPane.getVvalue();

        double newH = (maxX == 0) ? 0 : (desiredLeft / maxX);
        double newV = (maxY == 0) ? 0 : (desiredTop / maxY);

        // Clamp values to [0, 1]
        newH = Math.max(0, Math.min(newH, 1));
        newV = Math.max(0, Math.min(newV, 1));

        // Animate scroll
        Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO,
                new KeyValue(desktopScrollPane.hvalueProperty(), startH, Interpolator.EASE_BOTH),
                new KeyValue(desktopScrollPane.vvalueProperty(), startV, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(desktopScrollPane.hvalueProperty(), newH, Interpolator.EASE_BOTH),
                        new KeyValue(desktopScrollPane.vvalueProperty(), newV, Interpolator.EASE_BOTH)));
        timeline.play();
    }

    // =========================================================================
    //                              HELPER METHODS
    // =========================================================================

    /**
     * Determines whether the specified rectangle (x, y, width, height)
     * fits entirely within the workspace (desktop) boundaries.
     *
     * @return {@code true} if the rectangle is fully within the desktop bounds; {@code false} otherwise.
     */
    private boolean fitsInDesktop(double x, double y, double width, double height,
                                  double desktopWidth, double desktopHeight) {
        return (x >= 0) && (y >= 0) && (x + width <= desktopWidth) && (y + height <= desktopHeight);
    }

    /**
     * Checks if a proposed rectangle for a new window overlaps with
     * any existing window (excluding the drop region).
     *
     * @return {@code true} if an overlap is detected; {@code false} otherwise.
     */
    private boolean overlapsWithExistingWindows(double x, double y, double width, double height) {
        Bounds newWindowBounds = new BoundingBox(x, y, width, height);
        return desktopPane.getChildren().stream()
                .filter(n -> n != desktopPane.getDropRegion() && n.isVisible())
                .map(Node::getBoundsInParent)
                .anyMatch(newWindowBounds::intersects);
    }

    // =========================================================================
    //                            DISPOSAL / CLEANUP
    // =========================================================================

    /**
     * Cleans up listeners and references to avoid potential memory leaks.
     */
    @Override
    public void dispose() {
        if (getSkinnable() != null && workspaceWindows != null) {
            workspaceWindows.removeListener(weakWindowsListChangeListener);
            workspaceWindows = null;
        }
        getChildren().clear();
        desktopPane.dispose();
        super.dispose();
    }

    /**
     * Synchronizes the local {@code workspaceWindows} reference with the skin's {@code KLWorkspace}.
     * Attaches or detaches the list-change listener as appropriate.
     */
    private void updateWorkspaceWindows() {
        // Detach the old listener, if any.
        if (workspaceWindows != null) {
            workspaceWindows.removeListener(weakWindowsListChangeListener);
        }
        workspaceWindows = getSkinnable().getWindows();
        if (workspaceWindows != null) {
            workspaceWindows.addListener(weakWindowsListChangeListener);
        }

        // Request a re-layout of the desktop pane.
        desktopPane.requestLayout();
    }

    /**
     * A custom pane that acts as the "desktop" for the workspace.
     * It hosts multiple {@link ChapterKlWindow} nodes and displays
     * a {@link KLDropRegion} when an external drag enters the workspace.
     */
    private static class DesktopPane extends Pane {

        private double hgap = 0.0;
        private double vgap = 0.0;

        private final KLDropRegion dropRegion;
        private final ListChangeListener<Node> desktopPaneChildrenListener;

        /**
         * Constructs a new DesktopPane.
         *
         * @param workspace The {@link KLWorkspace} associated with this pane.
         */
        DesktopPane(final KLWorkspace workspace) {
            // Listen for direct child removals to keep the workspace
            // window list consistent if a window node is removed from the pane.
            desktopPaneChildrenListener = change -> {
                while (change.next()) {
                    if (change.wasRemoved()) {
                        for (Node node : change.getRemoved()) {
                            workspace.getWindows().removeIf(w -> w.getRootPane() == node);
                        }
                    }
                }
            };
            getChildren().addListener(desktopPaneChildrenListener);

            this.dropRegion = new KLDropRegion();
            dropRegion.setManaged(false);
            dropRegion.setVisible(false);
        }

        /**
         * Sets the horizontal gap between items on the desktop pane.
         *
         * @param value the new horizontal gap
         */
        final void setHGap(double value) {
            this.hgap = value;
        }

        /**
         * Sets the vertical gap between items on the desktop pane.
         *
         * @param value the new vertical gap
         */
        final void setVGap(double value) {
            this.vgap = value;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            final double width = getWidth();
            final double height = getHeight();

            if (dropRegion != null) {
                final double dropRegionWidth = dropRegion.prefWidth(height);
                final double dropRegionHeight = dropRegion.prefHeight(width);
                layoutInArea(dropRegion, hgap, vgap,
                        dropRegionWidth, dropRegionHeight, 0, HPos.CENTER, VPos.CENTER);
            }
        }

        /**
         * Returns the {@link KLDropRegion} representing the drop target area.
         *
         * @return The drop region used within this pane.
         */
        final KLDropRegion getDropRegion() {
            return dropRegion;
        }

        /**
         * Makes the drop region visible and adds it to the pane if not already present.
         */
        final void showDropRegion() {
            if (!getChildren().contains(dropRegion)) {
                getChildren().add(dropRegion);
            }
            dropRegion.setVisible(true);
        }

        /**
         * Hides the drop region and removes it from this pane.
         */
        final void hideDropRegion() {
            getChildren().remove(dropRegion);
            dropRegion.setVisible(false);
        }

        /**
         * Disposes of this desktop pane, removing the children listener.
         */
        final void dispose() {
            getChildren().removeListener(desktopPaneChildrenListener);
        }
    }
}
