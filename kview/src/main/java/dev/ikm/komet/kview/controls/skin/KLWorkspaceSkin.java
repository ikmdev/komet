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
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
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

import static dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;
import static dev.ikm.komet.kview.controls.KLDropRegion.Type.BOX;
import static dev.ikm.komet.kview.controls.KLDropRegion.Type.LINE;
import static dev.ikm.komet.kview.controls.KLWorkspace.*;
import static dev.ikm.komet.kview.fxutils.FXUtils.synchronizeHeightWithSceneAwareness;

/**
 * A custom skin implementation for the {@link KLWorkspace} control that provides a scrollable
 * "desktop" area capable of hosting multiple {@link ChapterKlWindow} instances.
 *
 * <p>This skin offers several key features:
 * <ul>
 *   <li><strong>Desktop Pane Layout:</strong> Arranges windows within a user-defined scrollable pane,
 *       employing a simplified "three-row placement" strategy for optimal use of space.</li>
 *   <li><strong>Drag and Drop:</strong> Displays a drop region when a valid item is dragged over the workspace,
 *       indicating where new content can be inserted (either fully as a BOX if space allows,
 *       or as a LINE if the horizontal gap is narrower).</li>
 *   <li><strong>Panning:</strong> Enables the user to pan the workspace by holding the {@code Control} key
 *       and dragging, with visual cues (open/closed hand cursors) to enhance interaction feedback.</li>
 *   <li><strong>Dynamic Window Management:</strong> Listens for additions and removals of
 *       {@link ChapterKlWindow} objects in the workspace and updates the desktop pane accordingly.</li>
 *   <li><strong>Window Support:</strong> Integrates {@link WindowSupport} to make each
 *       {@code ChapterKlWindow} resizable and draggable within the desktop pane.</li>
 * </ul>
 *
 * <p>The skin also implements advanced behaviors for drop-region placement:
 * <ul>
 *   <li>
 *     <strong>Line vs. Box Drop Region:</strong>
 *     <ul>
 *       <li>If there is sufficient horizontal space in a gap to fit a default-size window, a <strong>BOX</strong>
 *           drop region is shown, indicating that the new window can fully occupy that gap.</li>
 *       <li>If the gap is too narrow for a default-size window but at least as wide as the default horizontal gap,
 *           a <strong>LINE</strong> drop region is displayed. In this case, the line is anchored at
 *           <code>occupantRight + (horizontalGap - lineWidth) / 2.0</code> to center it within the available space.</li>
 *       <li>No drop region is shown on rows without any windows or when the gap is smaller than the default horizontal gap.</li>
 *       <li>Additionally, if the mouse is to the right of the last occupant in a row (but still within the row’s right boundary),
 *           the skin checks if a BOX or LINE region can be placed at that location.</li>
 *     </ul>
 *   </li>
 *   <li>
 *     <strong>Three-Row Placement Enhancement:</strong>
 *     <ul>
 *       <li>The three-row placement strategy can start from either the top-left corner of the workspace
 *           (the original behavior) or from a specified window boundary (a given X/Y location within a row).</li>
 *       <li>This enhancement allows windows removed during a LINE drop insertion to be re-laid out starting immediately
 *           after the insertion point, rather than always beginning at the top-left.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @see KLWorkspace
 * @see ChapterKlWindow
 * @see WindowSupport
 */
public class KLWorkspaceSkin extends SkinBase<KLWorkspace> {

    /**
     * Internal property key for storing the window state in each window’s properties map.
     */
    private static final String WINDOW_STATE_LISTENER = "windowStateListener";

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
     * The timeline used to animate auto-scrolling when newly creating or re-laying out windows.
     */
    private Timeline autoScrollTimeline;

    /**
     * The flag to indicate whether this is the first time the skin is being initialized.
     * Used to prevent unnecessary auto-scrolling during the initial setup.
     */
    private boolean firstTime = true;

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
        this.desktopPane = new DesktopPane();
        this.desktopPane.getStyleClass().add(DESKTOP_PANE_STYLE_CLASS);

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
                    // For each removed window, remove it from the workspace
                    change.getRemoved().forEach(this::removeWindow);
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

        // Enable auto-scrolling after the initial setup
        firstTime = false;

        // --------------------------------------------------------------------
        // 4) Configure user interactions (panning and drag-drop)
        // --------------------------------------------------------------------
        configurePanningHandlers(workspace);
        configureDragDropHandlers(workspace);

        // --------------------------------------------------------------------
        // 5) Listen for property changes that affect layout
        // --------------------------------------------------------------------
        registerChangeListener(workspace.windowsProperty(), o -> updateWorkspaceWindows());
        registerChangeListener(desktopPane.widthProperty(), o ->
                workspaceWindows.forEach(win -> clampWindowPosition(win.fxGadget())));
        registerChangeListener(desktopPane.heightProperty(), o ->
                workspaceWindows.forEach(win -> clampWindowPosition(win.fxGadget())));
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

    // =========================================================================
    //                                PANNING
    // =========================================================================

    /**
     * Sets up keyboard and mouse event handlers to enable panning of the {@link KLWorkspace}'s
     * desktop via Ctrl+drag.
     * <p>
     * When the user presses and holds the {@code Control} key, the {@code DesktopPane} becomes
     * mouse-transparent, and the enclosing {@link ScrollPane} is set to pannable. This allows
     * the user to click and drag anywhere on the desktop to pan the visible area.
     * <p>
     * Upon releasing the Control key, default interaction is restored: the desktop accepts
     * mouse events, and the scroll pane is no longer pannable. The mouse cursor is updated
     * accordingly (e.g., open hand, closed hand) during these interactions.
     *
     * @param workspace the {@link KLWorkspace} to be configured for panning
     */
    private void configurePanningHandlers(KLWorkspace workspace) {
        // Activate panning when Ctrl is pressed
        workspace.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            // Enable panning when the Control key is pressed.
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                desktopPane.setMouseTransparent(true);
                desktopScrollPane.setPannable(true);
                changeViewportCursor(Cursor.OPEN_HAND); // Indicate dragging with an open hand
                desktopScrollPane.requestFocus();
            }
        });

        workspace.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            // Disable panning and restore cursor when the Control key is released.
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                desktopPane.setMouseTransparent(false);
                desktopScrollPane.setPannable(false);
                changeViewportCursor(Cursor.DEFAULT); // Revert to default cursor
            }
        });

        // Register mouse event handlers on the scroll pane to provide visual cues during panning.
        desktopScrollPane.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown() && desktopScrollPane.isPannable()) {
                changeViewportCursor(Cursor.CLOSED_HAND); // Indicate active dragging
            }
        });

        desktopScrollPane.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            if (desktopScrollPane.isPannable()) {
                changeViewportCursor(Cursor.OPEN_HAND); // Revert to open hand after drag
            }
        });
    }

    // =========================================================================
    //                                DRAG & DROP
    // =========================================================================

    /**
     * Configures drag-and-drop behavior for the specified {@code KLWorkspace}. This method sets up the
     * necessary event handlers to display or hide a drop region on the desktop pane when a valid drag
     * action occurs over the workspace.
     * <p>
     * Specifically:
     * <ul>
     *   <li>When an item is dragged over the workspace, this method checks if it is a valid draggable
     *       item and then determines whether the drop region should be shown or hidden based on mouse
     *       position and collision with existing windows.</li>
     *   <li>If the mouse is over an existing window within the desktop pane, the drop region is hidden
     *       and no drop placement is shown.</li>
     *   <li>If the mouse is over an empty area of the desktop pane, the method calculates and displays
     *       the appropriate drop region (bounds and type).</li>
     *   <li>When the drag exits the workspace, the drop region is hidden.</li>
     * </ul>
     *
     * @param workspace the {@code KLWorkspace} to which drag-and-drop handlers are attached
     * @see #findDropRegionPlacement(double, double)
     */
    private void configureDragDropHandlers(KLWorkspace workspace) {
        // Show drop-region if a valid item is dragged over
        workspace.setOnDragOver(event -> {
            if (event.getGestureSource() != null && (event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT) || event.getDragboard().hasString())) {
                // Convert screen coordinates to local coordinates of the desktop pane
                final Point2D localCoords = desktopPane.screenToLocal(event.getScreenX(), event.getScreenY());
                final double mouseX = localCoords.getX();
                final double mouseY = localCoords.getY();

                final Node intersectedNode = event.getPickResult().getIntersectedNode();
                final boolean mouseOverWindow = intersectedNode != null &&
                        intersectedNode != desktopPane && intersectedNode != desktopPane.getDropRegion();

                if (mouseOverWindow) {
                    // If the mouse is over an existing window, hide any drop region and skip drop placement
                    desktopPane.hideDropRegion();
                    event.consume();
                    return;
                }

                // Determine the appropriate drop region placement
                final DropResult dropResult = findDropRegionPlacement(mouseX, mouseY);
                if (dropResult != null && dropResult.bounds() != null) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    desktopPane.showDropRegion(dropResult.bounds(), dropResult.type());
                } else {
                    desktopPane.hideDropRegion();
                }
            }
            event.consume();
        });

        // Hide the drop region when the drag exits the workspace
        workspace.setOnDragExited(event -> {
            desktopPane.hideDropRegion();
            event.consume();
        });
    }

    /**
     * A small record to hold both the drop region bounds and the chosen type
     * (LINE vs. BOX).
     */
    private record DropResult(Bounds bounds, KLDropRegion.Type type) {
    }

    /**
     * Attempts to locate a bounding region for a new window (the drop region) based on
     * the current mouse position.
     * <ul>
     *   <li>If the workspace has no existing windows, we return a bounding region
     *       anchored to the top-left corner of the desktop (using horizontal/vertical gap),
     *       with a {@code BOX} type.</li>
     *   <li>If there are existing windows and the mouse is hovering in a horizontal gap
     *       between two windows—or to the left of the first occupant, or to the right of
     *       the last occupant in that row—the code checks
     *       whether that gap can fit a default-size window or at least the default horizontal gap:
     *       <ul>
     *           <li>If the gap is large enough to fit the default window, a {@code BOX} drop region is used.</li>
     *           <li>If the gap is at least as large as the default horizontal gap but less than the default window width,
     *               a {@code LINE} region is used. Otherwise, no region is shown.</li>
     *       </ul>
     *   </li>
     *   <li>If no gap-based solution is found, we provide a {@code BOX} region in the
     *       top-left of the next free three-row placement (if any).</li>
     * </ul>
     *
     * @param mouseX the x-coordinate of the mouse in the desktop pane's local space
     * @param mouseY the y-coordinate of the mouse in the desktop pane's local space
     * @return a {@link DropResult} containing the drop bounds and type (or {@code null} if none found)
     */
    private DropResult findDropRegionPlacement(double mouseX, double mouseY) {
        final KLWorkspace workspace = getSkinnable();
        final double width = DEFAULT_WINDOW_WIDTH;
        final double height = DEFAULT_WINDOW_HEIGHT;

        final double desktopWidth = (desktopPane.getWidth() > 0)
                ? desktopPane.getWidth()
                : desktopPane.getPrefWidth();
        final double desktopHeight = (desktopPane.getHeight() > 0)
                ? desktopPane.getHeight()
                : desktopPane.getPrefHeight();

        // 1) If there are no windows, place in top-left as BOX:
        if (workspace.getWindows().isEmpty()) {
            final double x = workspace.getHorizontalGap();
            final double y = workspace.getVerticalGap();
            // Ensure it fits in the desktop:
            if (canPlace(x, y, width, height, desktopWidth, desktopHeight)) {
                Bounds bounds = new BoundingBox(x, y, width, height);
                return new DropResult(bounds, BOX);
            }
            return null;
        }

        // 2) If there are windows, check if the mouse is hovering in a suitable "gap"
        //    that can show a BOX or LINE region.
        final DropResult gapResult = findHorizontalGapForDrag(mouseX, mouseY);
        if (gapResult != null && gapResult.bounds() != null) {
            return gapResult;
        }

        // 3) Otherwise, fallback to a BOX indicator near the top-left of
        //    the next free three-row placement (if any).
        final Point2D placement = findThreeRowPlacement(width, height,
                0, 0, desktopWidth, desktopHeight,
                workspace.getHorizontalGap(),
                workspace.getVerticalGap());

        if (placement != null) {
            final Bounds bounds = new BoundingBox(placement.getX(), placement.getY(), width, height);
            return new DropResult(bounds, BOX);
        }

        return null;
    }

    /**
     * Finds a horizontal gap (including the left side of the first occupant in a row, or the
     * right side of the last occupant in the row) under the mouse position and determines whether
     * it can show a default-size BOX region or a LINE region that indicates insertion.
     * <p>
     * <strong>Key Details:</strong>
     * <ul>
     *   <li>If the gap is at least {@link KLWorkspace#DEFAULT_WINDOW_WIDTH} + 2 * horizontalGap,
     *       a {@code BOX} drop region is placed at {@code occupantRight + horizontalGap} (or at
     *       the row’s left boundary if the occupant is "synthetic").</li>
     *   <li>If the gap is smaller than the default window width but is at least {@code horizontalGap},
     *       we show a {@code LINE} region. The line is anchored at
     *       {@code occupantRight + (horizontalGap - lineWidth)/2.0}.</li>
     *   <li>If the gap is smaller than {@code horizontalGap}, we do not show any drop region.</li>
     *   <li>If the mouse is to the right of the last occupant in that row but still within the row,
     *       this logic similarly checks for BOX or LINE placement in that "right boundary" gap.</li>
     * </ul>
     *
     * @param mouseX the x-coordinate of the mouse in desktop-pane coordinates
     * @param mouseY the y-coordinate of the mouse in desktop-pane coordinates
     * @return a {@link DropResult} or {@code null} if none found
     */
    private DropResult findHorizontalGapForDrag(double mouseX, double mouseY) {
        final KLWorkspace workspace = getSkinnable();
        final double rowTolerance = DEFAULT_VERTICAL_GAP;
        final double defaultWinWidth = DEFAULT_WINDOW_WIDTH;
        final double defaultWinHeight = DEFAULT_WINDOW_HEIGHT;
        final double hgap = workspace.getHorizontalGap();

        // Identify which row occupant(s) might be relevant by checking vertical overlap
        final List<Node> visibleWindows = desktopPane.getChildren().stream()
                .filter(n -> n != desktopPane.getDropRegion() && n.isVisible())
                .toList();

        final List<Bounds> candidateRow = new ArrayList<>();
        for (Node node : visibleWindows) {
            Bounds b = node.getBoundsInParent();
            // If the mouse's Y is within the vertical span of this occupant (with tolerance),
            // consider it a candidate for gap checks in that row.
            if (mouseY >= b.getMinY() - rowTolerance && mouseY <= b.getMaxY() + rowTolerance) {
                candidateRow.add(b);
            }
        }

        // Sort these by minX
        candidateRow.sort(Comparator.comparingDouble(Bounds::getMinX));

        // If the row is empty, no occupant-based gap (we return null).
        // We only want to show a drop region between or around occupants.
        // (This will be handled by the fallback three-row placement if no occupant is found.)
        if (candidateRow.isEmpty()) {
            return null;
        }

        // Determine the row boundaries: minY and maxY among row occupants
        double rowMinY = candidateRow.stream().mapToDouble(Bounds::getMinY).min().orElse(mouseY - rowTolerance);
        double rowMaxY = candidateRow.stream().mapToDouble(Bounds::getMaxY).max().orElse(mouseY + rowTolerance);

        // We'll treat the row as having a synthetic "left boundary occupant" at x=0
        // and a synthetic "right boundary occupant" at x=some large number
        // (e.g., the desktop width).
        final Bounds leftBoundary = new BoundingBox(0, rowMinY,
                0, rowMaxY - rowMinY);
        // We assume the row extends to the full width of the desktop.
        final Bounds rightBoundary = new BoundingBox(desktopPane.getWidth(), rowMinY,
                0, rowMaxY - rowMinY);

        // Insert them as "occupants" at the start/end of this row occupant list
        candidateRow.addFirst(leftBoundary);
        candidateRow.add(rightBoundary);

        // Re-sort after adding boundaries
        candidateRow.sort(Comparator.comparingDouble(Bounds::getMinX));

        // Now examine each gap between occupant i and occupant i+1
        for (int i = 0; i < candidateRow.size() - 1; i++) {
            final Bounds left = candidateRow.get(i);
            final Bounds right = candidateRow.get(i + 1);

            double gapStartX = left.getMaxX();
            double gapEndX = right.getMinX();
            if (gapEndX <= gapStartX) {
                // No actual gap if occupant i overlaps occupant i+1
                continue;
            }

            // The user’s pointer must be within [gapStartX, gapEndX].
            if (mouseX >= gapStartX && mouseX <= gapEndX) {
                final double gapSize = gapEndX - gapStartX;

                // We'll define a vertical position for the drop region.
                double occupantRight = left.getMaxX();
                double yTop = Math.min(left.getMinY(), right.getMinY());

                // 1) If enough space for the default-size window -> BOX
                if (gapSize >= (defaultWinWidth + 2 * hgap)) {
                    final double boxX = occupantRight + hgap;
                    // Ensure the BOX does not exceed the gap
                    if (boxX + defaultWinWidth <= gapEndX) {
                        double boxY = yTop + 5.0; // Offset from the top due to window borders
                        double boxHeight = defaultWinHeight;

                        BoundingBox boxBounds = new BoundingBox(boxX, boxY, defaultWinWidth, boxHeight);
                        return new DropResult(boxBounds, BOX);
                    }
                }

                // 2) Otherwise, check if the gap is at least hgap to show a LINE
                else if (gapSize >= hgap - LINE.getWidth() / 2.0) {
                    // Position line region so that it is centered in the sub-gap of hgap
                    final double lineWidth = DEFAULT_HORIZONTAL_GAP / 2.0;
                    final double lineX = occupantRight + (hgap - lineWidth) / 2.0 - lineWidth / 4.0;

                    final double lineY = Math.max(0, yTop + 5.0); // Offset from the top due to window borders
                    final double lineHeight = defaultWinHeight;

                    final BoundingBox lineBounds = new BoundingBox(lineX, lineY, lineWidth, lineHeight);
                    return new DropResult(lineBounds, LINE);
                }

                // 3) If gap < hgap or neither BOX nor LINE can be placed, we return null
                return null;
            }
        }
        return null;
    }

    // =========================================================================
    //                               ADD WINDOWS
    // =========================================================================

    /**
     * Adds a {@link ChapterKlWindow} to the {@code desktopPane}.
     * <p>
     * The logic for determining the initial placement of the new window is as follows:
     * <ol>
     *     <li>If the drop region is visible and is of type {@code BOX}, place the window at the drop region
     *     coordinates (top-left) with the drop region’s dimensions.</li>
     *     <li>If the drop region is visible and is of type {@code LINE}, remove all windows to the right of
     *     {@code dropX} (in that same row and in subsequent rows), place the new window at a position that respects
     *     horizontal gaps from the left occupant, and then re-add the removed windows in sorted order. The re-added
     *     windows are placed in a <em>continuous three-row flow</em>, but ignoring windows on the left and in earlier rows.</li>
     *     <li>If the window has a saved position that fits within the desktop and does not overlap
     *         existing windows, use that saved position.</li>
     *     <li>Otherwise, fall back on the three-row placement strategy from the top-left.</li>
     * </ol>
     * <p>
     * Note: We only manipulate {@code desktopPane.getChildren()} here, not {@code workspace.getWindows()}.
     *
     * @param window The {@link ChapterKlWindow} to be added.
     */
    private void addWindow(ChapterKlWindow<Pane> window) {
        final Pane windowPanel = window.fxGadget();
        // Make the window draggable/resizable
        new WindowSupport(windowPanel);

        // Add listeners to keep the window in bounds and save its state
        final InvalidationListener windowStateListener = obs -> {
            clampWindowPosition(windowPanel);
            window.save();
        };
        windowPanel.layoutXProperty().addListener(windowStateListener);
        windowPanel.layoutYProperty().addListener(windowStateListener);
        windowPanel.widthProperty().addListener(windowStateListener);
        windowPanel.heightProperty().addListener(windowStateListener);
        windowPanel.getProperties().put(WINDOW_STATE_LISTENER, windowStateListener);

        // Apply a minimum width constraint
        windowPanel.setMinWidth(MIN_WINDOW_WIDTH);

        // Apply a maximum height constraint
        windowPanel.setMaxHeight(MAX_WINDOW_HEIGHT);

        // Synchronize the window panel's preferred height with its actual height
        synchronizeHeightWithSceneAwareness(windowPanel);

        final KLDropRegion dropRegion = desktopPane.getDropRegion();
        final KLWorkspace workspace = getSkinnable();

        final double desktopWidth = (desktopPane.getWidth() > 0)
                ? desktopPane.getWidth()
                : desktopPane.getPrefWidth();

        final double desktopHeight = (desktopPane.getHeight() > 0)
                ? desktopPane.getHeight()
                : desktopPane.getPrefHeight();

        // Default dimensions for a new window
        final double windowWidth = DEFAULT_WINDOW_WIDTH;
        final double windowHeight = DEFAULT_WINDOW_HEIGHT;

        // --------------------------------------------------------------------
        // 1) If the drop region is visible, handle accordingly
        // --------------------------------------------------------------------
        if (dropRegion.isVisible()) {
            final KLDropRegion.Type regionType = dropRegion.getType();
            final double dropX = dropRegion.getLayoutX();
            final double dropY = dropRegion.getLayoutY();
            final double dropW = dropRegion.getWidth();
            final double dropH = dropRegion.getHeight();

            switch (regionType) {
                case BOX -> {
                    // Use the drop region's bounding box directly
                    if (canPlace(dropX, dropY, dropW, dropH, desktopWidth, desktopHeight)) {
                        windowPanel.setLayoutX(dropX);
                        windowPanel.setLayoutY(dropY);
                        windowPanel.setPrefWidth(dropW);
                        desktopPane.getChildren().add(windowPanel);

                        // Auto-scroll the workspace to reveal the newly dropped window
                        autoScrollToTopEdge(windowPanel, desktopWidth, desktopHeight);
                    }
                    return;
                }

                case LINE -> {
                    // -------------------------------------------------------------
                    // 1) Remove windows in the SAME ROW (to the right of dropX)
                    //    AND in subsequent rows, in sorted order.
                    // -------------------------------------------------------------
                    final List<Pane> removedWindows = removeWindowsRightOfLine(dropX);

                    // -------------------------------------------------------------
                    // 2) Place the new window at a position that respects horizontal gap from occupant on the left
                    //
                    // Before placing the window, check if the dropX + windowWidth exceeds the available width.
                    // If so, and if we are not in the last row, move the window to the start of the next row.
                    // Determine the row based on dropY.
                    final double rowHeight = desktopHeight / ROWS;
                    int currentRow = (int) Math.floor(dropY / rowHeight);
                    final double occupantRight = findRightmostOccupantBefore(dropX);
                    double newX = occupantRight + workspace.getHorizontalGap();
                    double newY = dropY;
                    if (newX + windowWidth > desktopWidth) {
                        if (currentRow < ROWS - 1) {
                            // Move to the beginning of the next row
                            currentRow++;
                            newX = workspace.getHorizontalGap();
                            newY = currentRow * rowHeight + workspace.getVerticalGap();
                        }
                    }

                    windowPanel.setLayoutX(newX);
                    windowPanel.setLayoutY(newY);
                    windowPanel.setPrefWidth(windowWidth);
                    desktopPane.getChildren().add(windowPanel);

                    // Auto-scroll the workspace to reveal the newly dropped window
                    autoScrollToTopEdge(windowPanel, desktopWidth, desktopHeight);

                    // -------------------------------------------------------------
                    // 3) Re-add previously removed windows (in the order they
                    //    were sorted), placing them continuously to the right of
                    //    the newly placed window and in subsequent rows if needed.
                    // -------------------------------------------------------------
                    final double nextStartX = newX + workspace.getHorizontalGap();
                    final double nextStartY = newY;
                    reLayoutRemovedWindows(removedWindows, nextStartX, nextStartY, desktopWidth, desktopHeight);
                    return;
                }
            }
        }

        // --------------------------------------------------------------------
        // 2) If the window has a saved position and it is valid
        // --------------------------------------------------------------------
        final double savedX = windowPanel.getLayoutX();
        final double savedY = windowPanel.getLayoutY();
        final boolean hasSavedPos = (savedX != 0 || savedY != 0);

        if (hasSavedPos) {
            desktopPane.getChildren().add(windowPanel);
            desktopPane.layout();
            // No auto-scrolling for returning windows
            return;
        }

        // --------------------------------------------------------------------
        // 3) Otherwise, use the three-row placement strategy from the top-left
        // --------------------------------------------------------------------
        final Point2D placement = findThreeRowPlacement(windowWidth, windowHeight,
                0, 0, desktopWidth, desktopHeight,
                workspace.getHorizontalGap(), workspace.getVerticalGap());

        if (placement != null) {
            windowPanel.setLayoutX(placement.getX());
            windowPanel.setLayoutY(placement.getY());
            windowPanel.setPrefWidth(windowWidth);
            desktopPane.getChildren().add(windowPanel);

            if (firstTime) {
                // Skip auto-scrolling during the initial setup
                return;
            }

            // Auto-scroll the workspace to reveal the newly placed window
            autoScrollToTopEdge(windowPanel, desktopWidth, desktopHeight);
        }

        // Save the window's position and size
        window.save();
    }

    /**
     * Removes a {@link ChapterKlWindow} from the desktop pane and detaches any
     * associated clamp listeners.
     *
     * @param window the window to remove
     */
    private void removeWindow(ChapterKlWindow<Pane> window) {
        final Pane windowPanel = window.fxGadget();
        desktopPane.getChildren().remove(windowPanel);

        // Remove clamp listeners stored in the window's properties
        if (windowPanel.getProperties().containsKey(WINDOW_STATE_LISTENER)) {
            final InvalidationListener clampListener =
                    (InvalidationListener) windowPanel.getProperties().get(WINDOW_STATE_LISTENER);
            windowPanel.layoutXProperty().removeListener(clampListener);
            windowPanel.layoutYProperty().removeListener(clampListener);
            windowPanel.widthProperty().removeListener(clampListener);
            windowPanel.heightProperty().removeListener(clampListener);
            windowPanel.getProperties().remove(WINDOW_STATE_LISTENER);
        }
    }

    /**
     * Finds the occupant on the left side of the specified drop line (i.e., the window
     * whose {@code maxX} is strictly less than {@code dropX} and is the rightmost among such).
     * This occupant might be in the same row or an earlier row, but only the occupant
     * in the same row effectively matters for the immediate "horizontal gap."
     *
     * @param dropX the drop line's X coordinate
     * @return the maximum X boundary of any occupant that ends to the left of {@code dropX},
     * or 0 if none is found
     */
    private double findRightmostOccupantBefore(double dropX) {
        double occupantRight = 0;
        for (Node child : desktopPane.getChildren()) {
            if (child == desktopPane.getDropRegion()) {
                continue;
            }
            Bounds b = child.getBoundsInParent();
            if (b.getMaxX() < dropX && b.getMaxX() > occupantRight) {
                occupantRight = b.getMaxX();
            }
        }
        return occupantRight;
    }

    /**
     * Removes and returns a list of windows that must be re-laid out due to a LINE drop insertion:
     * <ul>
     *   <li>All windows in the same row whose {@code minX >= lineX}</li>
     *   <li>All windows in subsequent rows (row index greater than the row of the drop line)</li>
     * </ul>
     * <p>
     * The returned list is sorted in ascending order by row index, then by {@code minX}. This
     * ensures that when these windows are re-laid out
     * (in {@link #reLayoutRemovedWindows(List, double, double, double, double)}),
     * they appear in a left-to-right flow for each row, beginning with the row of the drop line,
     * followed by subsequent rows.
     *
     * @param lineX the x-coordinate marking the drop line
     * @return a list of windows to remove in row-based ascending order
     */
    private List<Pane> removeWindowsRightOfLine(double lineX) {
        List<Pane> removedWindows = new ArrayList<>();
        final KLDropRegion dropRegion = desktopPane.getDropRegion();

        // If we don't have a drop region or cannot calculate rows, fallback to a simpler approach
        if (dropRegion == null || desktopPane.getHeight() <= 0) {
            for (Node child : new ArrayList<>(desktopPane.getChildren())) {
                if (child == dropRegion) {
                    continue;
                }
                Bounds b = child.getBoundsInParent();
                if (b.getMinX() >= lineX) {
                    removedWindows.add((Pane) child);
                    desktopPane.getChildren().remove(child);
                }
            }
            return removedWindows;
        }

        // Identify which row the line is in by dividing the desktopPane into three rows
        final double rowHeight = desktopPane.getHeight() / ROWS;
        final double lineMiddleY = dropRegion.getLayoutY() + (dropRegion.getHeight() / 2.0);
        final int lineRowIndex = (int) Math.floor(lineMiddleY / rowHeight);

        // We'll gather windows that are:
        // (1) in the same row (== lineRowIndex) and minX >= lineX, or
        // (2) in rows below (> lineRowIndex).
        List<Node> toRemove = new ArrayList<>();
        for (Node child : new ArrayList<>(desktopPane.getChildren())) {
            if (child == dropRegion) {
                continue;
            }
            Bounds b = child.getBoundsInParent();
            double occupantMiddleY = (b.getMinY() + b.getMaxY()) / 2.0;
            int occupantRowIndex = (int) Math.floor(occupantMiddleY / rowHeight);

            if ((occupantRowIndex == lineRowIndex && b.getMinX() >= lineX)
                    || occupantRowIndex > lineRowIndex) {
                toRemove.add(child);
            }
        }

        // Sort by row first, then by minX
        toRemove.sort((n1, n2) -> {
            Bounds b1 = n1.getBoundsInParent();
            Bounds b2 = n2.getBoundsInParent();

            double midY1 = (b1.getMinY() + b1.getMaxY()) / 2.0;
            double midY2 = (b2.getMinY() + b2.getMaxY()) / 2.0;
            int r1 = (int) Math.floor(midY1 / rowHeight);
            int r2 = (int) Math.floor(midY2 / rowHeight);

            if (r1 != r2) {
                return Integer.compare(r1, r2);
            }
            return Double.compare(b1.getMinX(), b2.getMinX());
        });

        // Now remove them from the pane
        for (Node child : toRemove) {
            removedWindows.add((Pane) child);
            desktopPane.getChildren().remove(child);
        }
        return removedWindows;
    }

    /**
     * Re-lays the specified list of windows (removed due to a LINE drop) <strong>to the right of
     * the newly placed window and in subsequent rows</strong>, preserving the sorted order
     * (row first, then left-to-right).
     * <p>
     * Specifically:
     * <ul>
     *   <li>We place each window in turn using a refined three-row approach, starting from the
     *       current row of the newly placed window ({@code startY}) onward.</li>
     *   <li>We skip occupant windows that are to the left or in earlier rows, so they remain
     *       untouched in their previous location. Thus, only the windows in the {@code removedWindows}
     *       list are re-laid out here.</li>
     *   <li>Each placed window is added to the occupant list, so subsequent windows do not overlap
     *       it.</li>
     * </ul>
     *
     * @param windowsToReLayout the windows to re-layout, already sorted
     * @param startX            the horizontal coordinate from which to begin placing the windows
     * @param startY            the vertical coordinate that determines which row the first insertion
     *                          should occupy (the row of the dropped window)
     * @param desktopWidth  the total width of the desktop pane
     * @param desktopHeight the total height of the desktop pane
     */
    private void reLayoutRemovedWindows(List<Pane> windowsToReLayout, double startX, double startY,
                                        double desktopWidth, double desktopHeight) {
        if (windowsToReLayout.isEmpty()) {
            return;
        }

        final KLWorkspace workspace = getSkinnable();

        // Ensure layout is current
        desktopPane.layout();

        // Collect occupant windows relevant for re-laying out
        List<Bounds> occupantBounds = collectRelevantOccupants(startX, startY, desktopHeight);

        // Place each removed window in sorted order
        for (Pane pane : windowsToReLayout) {
            final double wWidth = pane.getWidth();
            final double wHeight = pane.getHeight();

            final Point2D pos = doThreeRowPlacement(wWidth, wHeight,
                    startX, startY, desktopWidth, desktopHeight,
                    workspace.getHorizontalGap(), workspace.getVerticalGap(),
                    occupantBounds);

            if (pos != null) {
                pane.setLayoutX(pos.getX());
                pane.setLayoutY(pos.getY());
            }

            desktopPane.getChildren().add(pane);

            // Add newly placed window to occupant bounds
            Bounds newOccupant = pane.getBoundsInParent();
            occupantBounds.add(newOccupant);
        }
    }

    /**
     * Collects occupant bounds relevant for re-laying out windows to the right of (startX,startY).
     * <p>
     * Specifically, we skip occupant windows that are in earlier rows, or whose entire bounding
     * box is to the left in the same row (i.e., {@code maxX < startX}).
     *
     * @param startX the horizontal boundary from which windows to the left are ignored in that row
     * @param startY used to determine the start row so earlier rows can be ignored
     * @param desktopHeight the total height of the desktop pane
     * @return a list of occupant {@link Bounds} that affect the layout
     */
    private List<Bounds> collectRelevantOccupants(double startX, double startY, double desktopHeight) {
        List<Bounds> occupantBounds = new ArrayList<>();
        final KLDropRegion dropRegion = desktopPane.getDropRegion();
        final double rowHeight = desktopHeight / ROWS;
        final int startRowIndex = Math.max(0, Math.min(ROWS - 1, (int) Math.floor(startY / rowHeight)));

        for (Node node : desktopPane.getChildren()) {
            if (node == dropRegion || !node.isVisible()) {
                continue;
            }
            Bounds b = node.getBoundsInParent();

            final double occupantMidY = (b.getMinY() + b.getMaxY()) / 2.0;
            final int occupantRow = (int) Math.floor(occupantMidY / rowHeight);

            // Skip occupant in earlier row
            if (occupantRow < startRowIndex) {
                continue;
            }

            // Skip occupant if fully to the left in the same row
            if (occupantRow == startRowIndex && b.getMaxX() < startX) {
                continue;
            }
            occupantBounds.add(b);
        }
        return occupantBounds;
    }

    // =========================================================================
    //                            THREE-ROW PLACEMENT
    // =========================================================================

    /**
     * Attempts to locate a free placement for a new window within a desktop pane divided into three horizontal rows.
     * Each row represents roughly one-third of the total desktop height. The algorithm generates candidate (x, y)
     * positions for the top-left corner of the new window in each row and checks these positions for collisions with
     * existing windows.
     *
     * <p>This method first determines the row corresponding to the given {@code startY} coordinate and then tries
     * to place the window in that row starting from {@code startX}. If no suitable placement is found in that row,
     * the search continues in subsequent rows and, if necessary, wraps around to earlier rows.
     * When {@code (startX, startY)} is {@code (0, 0)}, the behavior is identical to the original top-left placement
     * strategy.
     *
     * <p>In addition, the method performs a robust overlap check using {@link #overlapsWithExistingWindows(double,
     * double, double, double)} to ensure that the proposed position does not intersect any existing window—even in
     * edge cases where the row-based scanning might not detect a collision. If an overlap is detected, the algorithm
     * adjusts the y-position to skip below the blocking window.
     *
     * @param width         the width of the new window
     * @param height        the height of the new window
     * @param startX        the initial x-coordinate from which to begin the placement search
     * @param startY        the initial y-coordinate used to determine the starting row
     * @param desktopWidth  the total width of the desktop pane
     * @param desktopHeight the total height of the desktop pane
     * @param hgap          the horizontal gap to maintain between windows
     * @param vgap          the vertical gap to maintain between windows
     * @return a {@link Point2D} representing the top-left corner for the new window,
     *         or {@code null} if no valid placement is found
     */
    private Point2D findThreeRowPlacement(double width, double height,
                                          double startX, double startY,
                                          double desktopWidth, double desktopHeight,
                                          double hgap, double vgap) {
        // Force layout to obtain accurate bounds
        desktopPane.layout();

        // Collect occupant bounds once, excluding the drop region
        final List<Bounds> occupantBounds = desktopPane.getChildren().stream()
                .filter(n -> n != desktopPane.getDropRegion() && n.isVisible())
                .map(Node::getBoundsInParent)
                .toList();

        return doThreeRowPlacement(width, height, startX, startY, desktopWidth, desktopHeight,
                hgap, vgap, occupantBounds);
    }

    /**
     * Core internal method for scanning up to three rows for a valid placement that does not overlap
     * occupant windows. Used by both the general {@code findThreeRowPlacement} and the specialized
     * {@code reLayoutRemovedWindows}.
     *
     * @param occupantBounds the bounding boxes of windows to be considered for overlap checks
     * @return a {@link Point2D} representing the top-left corner for the new window,
     *         or {@code null} if no valid placement is found
     */
    private Point2D doThreeRowPlacement(double width, double height,
                                        double startX, double startY,
                                        double desktopWidth, double desktopHeight,
                                        double hgap, double vgap,
                                        List<Bounds> occupantBounds) {

        final double rowHeight = desktopHeight / ROWS;

        // Identify which row to start in
        int startRowIndex = (int) Math.floor(startY / rowHeight);
        startRowIndex = Math.max(0, Math.min(ROWS - 1, startRowIndex));

        // Generate row order based on startRowIndex
        List<Integer> rowOrder = new ArrayList<>();
        for (int i = startRowIndex; i < ROWS; i++) {
            rowOrder.add(i);
        }

        for (int rowIndex : rowOrder) {
            final double rowTop = rowIndex * rowHeight;
            final double rowBottom = rowTop + rowHeight;

            // If the window is too tall for this row, skip
            if (height > (rowHeight - 2 * vgap)) {
                continue;
            }

            // Identify occupant bounding boxes in this row
            final List<Bounds> rowOccupants = occupantBounds.stream()
                    .filter(b -> b.getMaxY() > rowTop && b.getMinY() < rowBottom)
                    .sorted(Comparator.comparingDouble(Bounds::getMinX))
                    .toList();

            final double rowLeft = 0;
            final double rowRight = desktopWidth;
            final double initialX = (rowIndex == startRowIndex)
                    ? Math.max(startX, rowLeft + hgap) : (rowLeft + hgap);

            // Build candidate X positions from occupant edges
            List<Double> xCandidates = new ArrayList<>();
            xCandidates.add(initialX);
            for (Bounds occ : rowOccupants) {
                double candidateX = occ.getMaxX() + hgap;
                if (candidateX + width <= rowRight) {
                    xCandidates.add(candidateX);
                }
            }
            xCandidates.sort(Double::compare);

            // Attempt to place at each candidate X
            for (double xCand : xCandidates) {
                double yCursor = rowTop + vgap;

                while ((yCursor + height) <= (rowBottom - vgap)) {
                    if (occupantIntersect(xCand, yCursor, width, height, rowOccupants) == null) {
                        return new Point2D(xCand, yCursor);
                    }
                    // If overlapped, jump below the blocking occupant
                    Bounds blocking = occupantIntersect(xCand, yCursor, width, height, rowOccupants);
                    if (blocking != null) {
                        double newY = blocking.getMaxY() + vgap;
                        if (newY + height > rowBottom) {
                            break;
                        }
                        yCursor = newY;
                    } else {
                        // If there's overlap but no occupant found (edge case),
                        // push down anyway to avoid overlap
                        yCursor += (height + vgap);
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
     * @param desktopWidth  the total width of the desktop pane
     * @param desktopHeight the total height of the desktop pane
     */
    private void autoScrollToTopEdge(Node nodeToView, double desktopWidth, double desktopHeight) {
        if (autoScrollTimeline != null && autoScrollTimeline.getStatus() == Timeline.Status.RUNNING) {
            return;
        }

        // Force layout to obtain accurate bounds
        desktopPane.layout();

        final Bounds nodeBounds = nodeToView.localToScene(nodeToView.getBoundsInLocal());
        final Bounds desktopBounds = desktopPane.localToScene(desktopPane.getBoundsInLocal());
        final Bounds viewportBounds = desktopScrollPane.getViewportBounds();

        // Calculate node's top relative to the desktop
        final double nodeTopY = nodeBounds.getMinY() - desktopBounds.getMinY() - DEFAULT_VERTICAL_GAP;
        // Center horizontally on the node
        final double nodeCenterX = nodeBounds.getMinX() + (nodeBounds.getWidth() / 2.0) - desktopBounds.getMinX();

        final double viewportWidth = viewportBounds.getWidth();
        final double viewportHeight = viewportBounds.getHeight();

        final double desiredLeft = nodeCenterX - (viewportWidth / 2.0);
        final double desiredTop = nodeTopY;

        final double maxX = Math.max(0, desktopWidth - viewportWidth);
        final double maxY = Math.max(0, desktopHeight - viewportHeight);

        final double startH = desktopScrollPane.getHvalue();
        final double startV = desktopScrollPane.getVvalue();

        double newH = (maxX == 0) ? 0 : (desiredLeft / maxX);
        double newV = (maxY == 0) ? 0 : (desiredTop / maxY);

        // Clamp values
        newH = Math.max(0, Math.min(newH, 1));
        newV = Math.max(0, Math.min(newV, 1));

        // Animate scroll
        autoScrollTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(desktopScrollPane.hvalueProperty(), startH),
                        new KeyValue(desktopScrollPane.vvalueProperty(), startV)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(desktopScrollPane.hvalueProperty(), newH, Interpolator.EASE_OUT),
                        new KeyValue(desktopScrollPane.vvalueProperty(), newV, Interpolator.EASE_OUT))
        );
        autoScrollTimeline.play();
    }

    // =========================================================================
    //                              HELPER METHODS
    // =========================================================================

    /**
     * Determines whether the specified rectangle can be placed on the desktop without
     * exceeding its boundaries or overlapping any existing windows (excluding the drop region).
     *
     * @param x             the x-coordinate of the rectangle’s top-left corner
     * @param y             the y-coordinate of the rectangle’s top-left corner
     * @param width         the width of the rectangle
     * @param height        the height of the rectangle
     * @param desktopWidth  the total width of the desktop
     * @param desktopHeight the total height of the desktop
     * @return {@code true} if the rectangle is entirely within the desktop bounds
     *         and does not intersect any existing window; {@code false} otherwise
     */
    private boolean canPlace(double x, double y, double width, double height,
                             double desktopWidth, double desktopHeight) {
        return fitsInDesktop(x, y, width, height, desktopWidth, desktopHeight)
                && !overlapsWithExistingWindows(x, y, width, height);
    }

    /**
     * Checks if the specified rectangle (x, y, width, height) falls completely within
     * the boundaries of the desktop area.
     *
     * @param x             the x-coordinate of the rectangle’s top-left corner
     * @param y             the y-coordinate of the rectangle’s top-left corner
     * @param width         the width of the rectangle
     * @param height        the height of the rectangle
     * @param desktopWidth  the width of the desktop area
     * @param desktopHeight the height of the desktop area
     * @return {@code true} if the entire rectangle lies within the desktop;
     *         {@code false} otherwise
     */
    private boolean fitsInDesktop(double x, double y, double width, double height,
                                  double desktopWidth, double desktopHeight) {
        return (x >= 0) && (y >= 0) && ((x + width) <= desktopWidth) && ((y + height) <= desktopHeight);
    }

    /**
     * Determines whether a rectangle at the given coordinates overlaps any
     * existing window in the desktop. The drop region is excluded from this check.
     *
     * @param x      the x-coordinate of the rectangle’s top-left corner
     * @param y      the y-coordinate of the rectangle’s top-left corner
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @return {@code true} if the rectangle intersects at least one existing window;
     *         {@code false} otherwise
     */
    private boolean overlapsWithExistingWindows(double x, double y, double width, double height) {
        final Bounds newWindowBounds = new BoundingBox(x, y, width, height);
        return desktopPane.getChildren().stream()
                .filter(n -> n != desktopPane.getDropRegion() && n.isVisible())
                .map(Node::getBoundsInParent)
                .anyMatch(newWindowBounds::intersects);
    }

    /**
     * Sets the specified cursor on the {@code ScrollPane}'s internal viewport, if available.
     * <p>
     * This method looks up the node with the CSS selector <em>.viewport</em> inside the
     * associated {@code ScrollPane} and updates its cursor, typically to indicate
     * user interactions such as panning or dragging. If the viewport node cannot be found,
     * this method does nothing.
     *
     * @param cursor the new {@link Cursor} to apply to the viewport
     */
    private void changeViewportCursor(Cursor cursor) {
        final StackPane viewport = (StackPane) desktopScrollPane.lookup(".viewport");
        if (viewport != null) {
            viewport.setCursor(cursor);
        }
    }

    /**
     * Checks if the proposed rectangle (x, y, width, height) intersects any occupant
     * in the given list of bounding boxes. If an intersection is found, this method
     * returns the first occupant's bounding box that collides; otherwise, it returns
     * {@code null}.
     *
     * @param x              the x-coordinate of the rectangle’s top-left corner
     * @param y              the y-coordinate of the rectangle’s top-left corner
     * @param width          the width of the rectangle
     * @param height         the height of the rectangle
     * @param occupantBounds the bounding boxes of existing occupants to check against
     * @return the first occupant {@link Bounds} that intersects the rectangle, or
     *         {@code null} if no intersection occurs
     */
    private Bounds occupantIntersect(double x, double y, double width, double height, List<Bounds> occupantBounds) {
        final BoundingBox candidate = new BoundingBox(x, y, width, height);
        for (Bounds b : occupantBounds) {
            if (candidate.intersects(b)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Clamps the position and size of the specified window pane so that it remains fully visible
     * within the bounds of the desktop pane.
     * <p>
     * This method adjusts the pane's horizontal (layoutX) and vertical (layoutY) positions
     * to ensure that the entire window fits inside the desktop pane. If any part of the window
     * extends beyond the desktop boundaries, its position is modified to bring it back into view.
     * Additionally, if the window's size exceeds the dimensions of the desktop pane, the preferred
     * width and height are reduced accordingly.
     * <p>
     * Typically, this method is registered as a change listener on the pane's position and size
     * properties (i.e., {@code layoutXProperty()}, {@code layoutYProperty()},
     * {@code widthProperty()}, and {@code heightProperty()}) to dynamically enforce that the window
     * remains within the visible workspace area.
     *
     * @param pane the window pane to be clamped within the desktop pane's boundaries.
     */
    private void clampWindowPosition(Pane pane) {
        final double desktopPaneWidth = desktopPane.getWidth();
        final double desktopPaneHeight = desktopPane.getHeight();
        final double windowWidth = pane.getWidth();
        final double windowHeight = pane.getHeight();

        if (desktopPaneWidth <= 0 || desktopPaneHeight <= 0 || windowWidth <= 0 || windowHeight <= 0) {
            return;
        }

        final double newX = pane.getLayoutX();
        final double newY = pane.getLayoutY();

        if (newX < 0) {
            pane.setLayoutX(0);
        } else if (newX + windowWidth > desktopPaneWidth) {
            pane.setLayoutX(desktopPaneWidth - windowWidth);
        }
        if (newY < 0) {
            pane.setLayoutY(0);
        } else if (newY + windowHeight > desktopPaneHeight) {
            pane.setLayoutY(desktopPaneHeight - windowHeight);
        }

        if (pane.getWidth() > desktopPaneWidth) {
            pane.setPrefWidth(desktopPaneWidth);
        }
        if (pane.getHeight() > desktopPaneHeight) {
            pane.setPrefHeight(desktopPaneHeight);
        }
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
        super.dispose();
    }

    // =========================================================================
    //                            DESKTOP PANE CLASS
    // =========================================================================

    /**
     * A custom pane that acts as the "desktop" for the workspace.
     * It hosts multiple {@link ChapterKlWindow} nodes and displays
     * a {@link KLDropRegion} when an external drag enters the workspace.
     */
    private static class DesktopPane extends Pane {

        private final KLDropRegion dropRegion;

        /**
         * Constructs a new DesktopPane.
         */
        DesktopPane() {
            this.dropRegion = new KLDropRegion();
            dropRegion.setManaged(false);
            dropRegion.setVisible(false);
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
         *
         * @param bounds the bounding box (x, y, width, height) to show the drop region
         * @param type   the {@link KLDropRegion.Type} of drop indicator (LINE or BOX)
         */
        final void showDropRegion(Bounds bounds, KLDropRegion.Type type) {
            dropRegion.setType(type);
            if (!getChildren().contains(dropRegion)) {
                getChildren().add(dropRegion);
            }
            dropRegion.setVisible(true);

            // Position and size the region directly
            dropRegion.resizeRelocate(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }

        /**
         * Hides the drop region and removes it from this pane.
         */
        final void hideDropRegion() {
            getChildren().remove(dropRegion);
            dropRegion.setVisible(false);
        }
    }
}
