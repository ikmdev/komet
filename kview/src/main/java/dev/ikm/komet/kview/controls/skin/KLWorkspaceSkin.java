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
import javafx.event.EventHandler;
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
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Objects;

import static dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;
import static dev.ikm.komet.kview.controls.KLDropRegion.Type.BOX;
import static dev.ikm.komet.kview.controls.KLDropRegion.Type.LINE;
import static dev.ikm.komet.kview.controls.KLWorkspace.COLUMNS;
import static dev.ikm.komet.kview.controls.KLWorkspace.DEFAULT_WINDOW_HEIGHT;
import static dev.ikm.komet.kview.controls.KLWorkspace.DEFAULT_WINDOW_WIDTH;
import static dev.ikm.komet.kview.controls.KLWorkspace.DESKTOP_PANE_STYLE_CLASS;
import static dev.ikm.komet.kview.controls.KLWorkspace.MAX_WINDOW_HEIGHT;
import static dev.ikm.komet.kview.controls.KLWorkspace.ROWS;
import static dev.ikm.komet.kview.controls.KLWorkspace.STANDARD_HEIGHT;
import static dev.ikm.komet.kview.controls.KLWorkspace.STANDARD_WIDTH;
import static dev.ikm.komet.kview.controls.KLWorkspace.USE_COMPUTED_SIZE;
import static dev.ikm.komet.kview.fxutils.FXUtils.DEFAULT_ANIMATION_DURATION;
import static dev.ikm.komet.kview.fxutils.window.WindowSupport.WINDOW_SUPPORT_KEY;
import static dev.ikm.komet.kview.fxutils.window.WindowSupport.setupWindowSupport;
import static dev.ikm.komet.kview.fxutils.window.WindowSupport.removeWindowSupport;

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
 *   <li><strong>Smooth Window Shifting:</strong> When dropping windows in LINE regions, existing windows
 *       smoothly animate to new positions rather than being removed and re-added.</li>
 * </ul>
 *
 * <p>The skin also implements advanced behaviors for drop-region placement.
 * <p><strong>Line vs. Box Drop Region:</strong>
 * <ul>
 *   <li>If there is sufficient horizontal space in a gap to fit a default-size window, a <strong>BOX</strong>
 *       drop region is shown, indicating that the new window can fully occupy that gap.</li>
 *   <li>If the gap is too narrow for a default-size window but at least as wide as the default horizontal gap,
 *       a <strong>LINE</strong> drop region is displayed. In this case, the line is anchored at
 *       <code>occupantRight + (hgap - lineWidth) / 2.0</code> to center it within the available space.</li>
 *   <li>No drop region is shown on rows without any windows or when the gap is smaller than the default horizontal gap.</li>
 *   <li>Additionally, if the mouse is to the right of the last occupant in a row (but still within the row's right boundary),
 *       the skin checks if a BOX or LINE region can be placed at that location.</li>
 * </ul>
 * <p></p><strong>Three-Row Placement Enhancement:</strong>
 * <ul>
 *   <li>The three-row placement strategy can start from either the top-left corner of the workspace
 *       (the original behavior) or from a specified window boundary (a given X/Y location within a row).</li>
 *   <li>This enhancement allows windows to be re-laid out starting immediately
 *       after the insertion point, rather than always beginning at the top-left.</li>
 * </ul>
 *
 * @see KLWorkspace
 * @see ChapterKlWindow
 * @see WindowSupport
 */
public class KLWorkspaceSkin extends SkinBase<KLWorkspace> {

    private static final Logger LOG = LoggerFactory.getLogger(KLWorkspaceSkin.class);

    /**
     * Internal property key for storing window Subscription in the window's properties map.
     */
    private static final String WINDOW_SUBSCRIPTION_KEY = "windowSubscription";

    /**
     * Internal property key for storing the desktop pane's resize subscription in the window's properties map.
     */
    private static final String DESKTOP_RESIZE_SUBSCRIPTION_KEY = "desktopResizeSubscription";

    /**
     * Internal property key for storing the panning subscription in the window's properties map.
     */
    private static final String PANNING_SUBSCRIPTION_KEY = "panningSubscription";

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
     * The timeline used to shift existing windows when a new window is added in a LINE drop region.
     */
    private Timeline shiftTimeline;

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
        // Setup panning handlers and store subscription for cleanup
        Subscription panningSubscription = configurePanningHandlers(workspace);
        workspace.getProperties().put(PANNING_SUBSCRIPTION_KEY, panningSubscription);

        configureDragDropHandlers(workspace);

        // --------------------------------------------------------------------
        // 5) Listen for property changes that affect layout
        // --------------------------------------------------------------------
        registerChangeListener(workspace.windowsProperty(), _ -> updateWorkspaceWindows());

        // Setup desktop resize subscription for window constraints
        Subscription desktopResizeSubscription = createDesktopResizeSubscription();
        workspace.getProperties().put(DESKTOP_RESIZE_SUBSCRIPTION_KEY, desktopResizeSubscription);
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
     * Creates a subscription that ensures windows remain within the desktop pane's boundaries when its size changes.
     * <p>
     * This method establishes listeners on the desktop pane's width and height properties that, when invalidated,
     * adjust all workspace windows to ensure they remain fully visible within the resized desktop. This prevents
     * windows from becoming inaccessible by being positioned outside the viewable area after a desktop resize.
     *
     * @return A {@link Subscription} that, when unsubscribed, detaches the resize listeners from the desktop pane
     * @see WindowSupport#constrainToParentBounds(double, double) For the underlying constraint implementation
     * @see Subscription The resource management pattern used for cleanup
     */
    private Subscription createDesktopResizeSubscription() {
        InvalidationListener resizeListener = obs -> {
            double width = desktopPane.getWidth();
            double height = desktopPane.getHeight();

            workspaceWindows.forEach(win -> {
                Pane windowPanel = win.fxGadget();
                WindowSupport support = (WindowSupport) windowPanel.getProperties().get(WINDOW_SUPPORT_KEY);
                if (support != null) {
                    support.constrainToParentBounds(width, height);
                }
            });
        };

        desktopPane.widthProperty().addListener(resizeListener);
        desktopPane.heightProperty().addListener(resizeListener);

        return () -> {
            desktopPane.widthProperty().removeListener(resizeListener);
            desktopPane.heightProperty().removeListener(resizeListener);
        };
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
     * @return A subscription that can be used to clean up the panning handlers
     */
    private Subscription configurePanningHandlers(KLWorkspace workspace) {
        MutableList<Subscription> panningSubscriptions = Lists.mutable.empty();

        // Ctrl key handlers
        final EventHandler<KeyEvent> keyPressHandler = keyEvent -> {
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                desktopPane.setMouseTransparent(true);
                desktopScrollPane.setPannable(true);
                changeViewportCursor(Cursor.OPEN_HAND);
                desktopScrollPane.requestFocus();
            }
        };

        final EventHandler<KeyEvent> keyReleaseHandler = keyEvent -> {
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                desktopPane.setMouseTransparent(false);
                desktopScrollPane.setPannable(false);
                changeViewportCursor(Cursor.DEFAULT);
            }
        };

        workspace.addEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler);
        workspace.addEventHandler(KeyEvent.KEY_RELEASED, keyReleaseHandler);

        panningSubscriptions.add(() -> {
            workspace.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler);
            workspace.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleaseHandler);
        });

        // Mouse handlers for visual feedback
        final EventHandler<MouseEvent> mousePressHandler = mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown() && desktopScrollPane.isPannable()) {
                changeViewportCursor(Cursor.CLOSED_HAND);
            }
        };

        final EventHandler<MouseEvent> mouseReleaseHandler = mouseEvent -> {
            if (desktopScrollPane.isPannable()) {
                changeViewportCursor(Cursor.OPEN_HAND);
            }
        };

        desktopScrollPane.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressHandler);
        desktopScrollPane.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleaseHandler);

        panningSubscriptions.add(() -> {
            desktopScrollPane.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressHandler);
            desktopScrollPane.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleaseHandler);
        });

        return Subscription.combine(panningSubscriptions.toArray(Subscription[]::new));
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
     *       item and then delegates to {@link #findDropRegionPlacement(double, double)} to determine
     *       the appropriate drop region placement based on mouse position.</li>
     *   <li>If a valid drop placement is found, the drop region is displayed with the calculated
     *       bounds and type.</li>
     *   <li>If no valid placement is available, the drop region is hidden.</li>
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
     *
     * @param bounds The bounding box for the drop region
     * @param type The type of drop region (LINE or BOX)
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

        // 1) If there are no windows, place first window at standard position with gaps for visual consistency
        if (workspace.getWindows().isEmpty()) {
            final double x = workspace.getHorizontalGap();
            final double y = workspace.getVerticalGap();
            // Ensure it fits in the desktop:
            if (canPlace(x, y, width, height, desktopWidth, desktopHeight)) {
                final Bounds bounds = new BoundingBox(x, y, width, height);
                return new DropResult(bounds, BOX);
            }
            return null;
        }

        // 2) If there are windows, check if the mouse is hovering in a suitable "gap"
        //    that can show a BOX or LINE region.
        final DropResult gapResult = findHorizontalGapForDrag(mouseX, mouseY, desktopWidth, desktopHeight);
        if (gapResult != null && gapResult.bounds() != null) {
            return gapResult;
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
     *   <li>If the gap is at least {@link KLWorkspace#DEFAULT_WINDOW_WIDTH} + 2 * hgap,
     *       a {@code BOX} drop region is placed at {@code occupantRight + hgap} (or at
     *       the row's left boundary if the occupant is "synthetic").</li>
     *   <li>If the gap is smaller than the default window width but is at least {@code hgap},
     *       we show a {@code LINE} region. The line is anchored at
     *       {@code occupantRight + (hgap - lineWidth)/2.0}.</li>
     *   <li>If the gap is smaller than {@code hgap}, we do not show any drop region.</li>
     *   <li>If the mouse is to the right of the last occupant in that row but still within the row,
     *       this logic similarly checks for BOX or LINE placement in that "right boundary" gap.</li>
     * </ul>
     *
     * @param mouseX the x-coordinate of the mouse in desktop-pane coordinates
     * @param mouseY the y-coordinate of the mouse in desktop-pane coordinates
     * @param desktopWidth the total width of the desktop pane
     * @param desktopHeight the total height of the desktop pane
     * @return a {@link DropResult} or {@code null} if none found
     */
    private DropResult findHorizontalGapForDrag(double mouseX, double mouseY,
                                                double desktopWidth, double desktopHeight) {
        final KLWorkspace workspace = getSkinnable();
        final double defaultWinWidth = DEFAULT_WINDOW_WIDTH;
        final double defaultWinHeight = DEFAULT_WINDOW_HEIGHT;
        final double hgap = workspace.getHorizontalGap();
        final double vgap = workspace.getVerticalGap();
        final double lineWidth = hgap / 2.0;

        // Calculate row height and determine which row the mouse is in
        final double rowHeight = desktopHeight / ROWS;
        int currentRowIndex = (int) Math.floor(mouseY / rowHeight);
        currentRowIndex = Math.max(0, Math.min(ROWS - 1, currentRowIndex));
        final double rowTop = currentRowIndex * rowHeight;
        final double rowBottom = rowTop + rowHeight;
        final double positionY = rowTop + vgap;

        // Get windows in the current row only
        final MutableList<Bounds> rowWindows = Lists.mutable.empty();
        for (Node node : desktopPane.getChildrenUnmodifiable()) {
            if (node == desktopPane.getDropRegion() || !node.isVisible()) {
                continue;
            }
            final Bounds windowBounds = getWindowBounds(node);
            // Check if window is in the current row
            if (windowBounds.getMinY() < rowBottom && windowBounds.getMaxY() > rowTop) {
                rowWindows.add(windowBounds);
            }
        }

        // Sort windows by X position
        rowWindows.sort(Comparator.comparingDouble(Bounds::getMinX));

        // If the row is empty, no occupant-based gap (we return null).
        // We only want to show a drop region between or around occupants.
        // (This will be handled by the fallback three-row placement if no occupant is found.)
        if (rowWindows.isEmpty()) {
            return null;
        }

        // Determine the row boundaries: minY and maxY among row occupants
        final double rowMinY = rowWindows.stream().mapToDouble(Bounds::getMinY).min().orElse(mouseY - vgap);
        final double rowMaxY = rowWindows.stream().mapToDouble(Bounds::getMaxY).max().orElse(mouseY + vgap);

        // We'll treat the row as having a synthetic "left boundary occupant" at x=0
        // and a synthetic "right boundary occupant" at x=desktop width
        final Bounds leftBoundary = new BoundingBox(0, rowMinY, 0, rowMaxY - rowMinY);
        final Bounds rightBoundary = new BoundingBox(desktopWidth, rowMinY, 0, rowMaxY - rowMinY);

        // Insert them as "occupants" at the start/end of this row occupant list
        rowWindows.addFirst(leftBoundary);
        rowWindows.add(rightBoundary);

        // Re-sort after adding boundaries
        rowWindows.sort(Comparator.comparingDouble(Bounds::getMinX));

        // Now examine each gap between occupant i and occupant i+1
        for (int i = 0; i < rowWindows.size() - 1; i++) {
            final Bounds left = rowWindows.get(i);
            final Bounds right = rowWindows.get(i + 1);

            final double gapStartX = left.getMaxX();
            final double gapEndX = right.getMinX();

            if (gapEndX <= gapStartX) {
                // No actual gap if occupant i overlaps occupant i+1
                continue;
            }

            // The user's pointer must be within [gapStartX, gapEndX].
            if (mouseX >= gapStartX && mouseX <= gapEndX) {
                final double gapSize = gapEndX - gapStartX;
                final double occupantRight = left.getMaxX();

                // 1) If enough space for the default-size window -> BOX
                if (gapSize >= (defaultWinWidth + 2 * hgap)) {
                    final double boxX = occupantRight + hgap;
                    // Ensure the BOX does not exceed the gap
                    if (boxX + defaultWinWidth <= gapEndX) {
                        // Use the properly calculated Y position with vertical gap
                        final double boxY = positionY;
                        double boxHeight = defaultWinHeight;

                        // Ensure the box fits within the row height
                        if (boxY + boxHeight > rowTop + rowHeight - vgap) {
                            boxHeight = rowTop + rowHeight - vgap - boxY;
                        }

                        final BoundingBox boxBounds = new BoundingBox(boxX, boxY, defaultWinWidth, boxHeight);
                        if (canPlace(boxBounds.getMinX(), boxBounds.getMinY(),
                                boxBounds.getWidth(), boxBounds.getHeight(),
                                desktopWidth, desktopHeight)) {
                            return new DropResult(boxBounds, BOX);
                        }
                    }
                }

                // 2) Otherwise, check if the gap is at least hgap to show a LINE
                else if (gapSize >= hgap) {
                    // Position line region so that it is centered in the sub-gap of hgap
                    final double lineX = occupantRight + (hgap - lineWidth) / 2.0;

                    // Use the properly calculated Y position with vertical gap
                    final double lineY = positionY;
                    double lineHeight = defaultWinHeight;

                    // Ensure the line fits within the row height
                    if (lineY + lineHeight > rowTop + rowHeight - vgap) {
                        lineHeight = rowTop + rowHeight - vgap - lineY;
                    }

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
     *     coordinates (top-left) with the drop region's dimensions.</li>
     *     <li>If the drop region is visible and is of type {@code LINE}, shift existing windows to the right
     *     (in the same row and in subsequent rows) to make room, place the new window at a position that respects
     *     horizontal gaps from the left occupant. The shifted windows smoothly animate to their new positions
     *     in a continuous three-row flow.</li>
     *     <li>If the window has a saved position that fits within the desktop and does not overlap
     *         existing windows, use that saved position.</li>
     *     <li>Otherwise, fall back on the three-row placement strategy from the top-left.</li>
     * </ol>
     * <p>
     * Note: We only manipulate {@code desktopPane.getChildren()} here, not {@code workspace.getWindows()}.
     *
     * @param window The {@link ChapterKlWindow} to be added.
     * @throws NullPointerException if window or window.fxGadget() is null
     * @see #removeWindow(ChapterKlWindow) for the inverse operation
     * @see #findThreeRowPlacement for the placement algorithm details
     */
    private void addWindow(ChapterKlWindow<Pane> window) {
        Objects.requireNonNull(window, "Window cannot be null");
        final Pane windowPanel = window.fxGadget();
        Objects.requireNonNull(windowPanel, "Window panel cannot be null");

        // Create subscriptions list for all window-related subscriptions
        MutableList<Subscription> windowSubscriptions = Lists.mutable.empty();
        WindowSupport windowSupport;

        try {
            // Create WindowSupport with proper configuration
            windowSupport = setupWindowSupport(windowPanel);

            // Configure auto height with subscription
            windowSubscriptions.add(windowSupport.configureAutoHeight(true, MAX_WINDOW_HEIGHT));

            // Add position constraints with state saving callback
            windowSubscriptions.add(windowSupport.setupPositionConstraints(obs -> window.save()));
        } catch (Exception ex) {
            // Clean up WindowSupport if it was created
            removeWindowSupport(windowPanel);
            LOG.error(ex.getMessage(), ex);
            return;
        } finally {
            // Store the combined subscription for cleanup
            Subscription combinedSubscription = Subscription.combine(windowSubscriptions.toArray(Subscription[]::new));
            windowPanel.getProperties().put(WINDOW_SUBSCRIPTION_KEY, combinedSubscription);
        }

        windowPanel.setMinWidth(USE_COMPUTED_SIZE);

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
                    // --------------------------------------------------------------------
                    // 1) Identify windows that need to be shifted
                    // --------------------------------------------------------------------
                    final ImmutableList<WindowShiftInfo> windowsToShift = identifyWindowsToShift(dropX, dropY);

                    // --------------------------------------------------------------------
                    // 2) Calculate the new position for the dropped window
                    // --------------------------------------------------------------------
                    final double rowHeight = desktopHeight / ROWS;
                    int currentRow = (int) Math.floor(dropY / rowHeight);
                    final double occupantRight = findRightmostOccupantBefore(dropX);
                    double newX = occupantRight + workspace.getHorizontalGap();
                    double newY = dropY;

                    // Check if window needs to wrap to next row
                    if (newX + windowWidth > desktopWidth) {
                        if (currentRow < ROWS - 1) {
                            currentRow++;
                            newX = workspace.getHorizontalGap();
                            newY = currentRow * rowHeight + workspace.getVerticalGap();
                        }
                    }

                    // --------------------------------------------------------------------
                    // 3) Calculate shift positions for existing windows
                    // --------------------------------------------------------------------
                    final ImmutableList<WindowShiftInfo> updatedShifts = calculateShiftPositions(
                            windowsToShift, newX + windowWidth + workspace.getHorizontalGap(),
                            newY, desktopWidth, desktopHeight, workspace);

                    // --------------------------------------------------------------------
                    // 4) Animate the shift of existing windows
                    // --------------------------------------------------------------------
                    animateWindowShifts(updatedShifts);

                    // --------------------------------------------------------------------
                    // 5) Place the new window
                    // --------------------------------------------------------------------
                    windowPanel.setLayoutX(newX);
                    windowPanel.setLayoutY(newY);
                    windowPanel.setPrefWidth(windowWidth);
                    desktopPane.getChildren().add(windowPanel);

                    // Auto-scroll the workspace to reveal the newly dropped window
                    autoScrollToTopEdge(windowPanel, desktopWidth, desktopHeight);
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
     * associated subscriptions.
     *
     * @param window the window to remove
     */
    private void removeWindow(ChapterKlWindow<Pane> window) {
        final Pane windowPanel = window.fxGadget();
        desktopPane.getChildren().remove(windowPanel);

        // Clean up WindowSupport and its subscriptions
        removeWindowSupport(windowPanel);

        // Clean up resources using a subscription
        if (windowPanel.getProperties().containsKey(WINDOW_SUBSCRIPTION_KEY)) {
            Subscription subscription = (Subscription) windowPanel.getProperties().get(WINDOW_SUBSCRIPTION_KEY);
            subscription.unsubscribe();
            windowPanel.getProperties().remove(WINDOW_SUBSCRIPTION_KEY);
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
        for (Node child : desktopPane.getChildrenUnmodifiable()) {
            if (child == desktopPane.getDropRegion() || !child.isVisible()) {
                continue;
            }
            Bounds bounds = getWindowBounds(child);
            if (bounds.getMaxX() < dropX && bounds.getMaxX() > occupantRight) {
                occupantRight = bounds.getMaxX();
            }
        }
        return occupantRight;
    }

    // =========================================================================
    //                          WINDOW SHIFTING
    // =========================================================================

    /**
     * Container record for window shift information.
     * Immutable data structure holding a window reference along with its current and target positions.
     *
     * @param window The window pane to be shifted
     * @param currentX Current X position of the window
     * @param currentY Current Y position of the window
     * @param targetX Target X position for the window after shifting
     * @param targetY Target Y position for the window after shifting
     */
    private record WindowShiftInfo(Pane window, double currentX, double currentY, double targetX, double targetY) {

        /**
         * Creates a WindowShiftInfo with target positions initialized to current positions.
         *
         * @param window The window pane
         * @param currentX Current X position
         * @param currentY Current Y position
         * @return A new WindowShiftInfo with target positions equal to current positions
         */
        static WindowShiftInfo fromCurrentPosition(Pane window, double currentX, double currentY) {
            return new WindowShiftInfo(window, currentX, currentY, currentX, currentY);
        }

        /**
         * Returns a new WindowShiftInfo with updated target positions.
         *
         * @param newTargetX New target X position
         * @param newTargetY New target Y position
         * @return A new WindowShiftInfo instance with updated target positions
         */
        WindowShiftInfo withTargetPosition(double newTargetX, double newTargetY) {
            return new WindowShiftInfo(window, currentX, currentY, newTargetX, newTargetY);
        }
    }

    /**
     * Identifies windows that need to be shifted due to a LINE drop insertion.
     * These are windows in the same row whose minX >= lineX, and all windows in subsequent rows.
     * <p>
     * The returned list is sorted in ascending order by row index, then by minX. This
     * ensures that when these windows are shifted, they maintain their relative positions
     * and flow naturally from one row to the next.
     *
     * @param lineX the x-coordinate marking the drop line
     * @param lineY the y-coordinate of the drop line (used to determine the row)
     * @return a list of WindowShiftInfo objects for windows that need shifting
     */
    private ImmutableList<WindowShiftInfo> identifyWindowsToShift(double lineX, double lineY) {
        MutableList<WindowShiftInfo> windowsToShift = Lists.mutable.empty();
        final KLDropRegion dropRegion = desktopPane.getDropRegion();

        if (dropRegion == null || desktopPane.getHeight() <= 0) {
            return windowsToShift.toImmutable();
        }

        final double rowHeight = desktopPane.getHeight() / ROWS;
        final double lineMiddleY = lineY + (dropRegion.getHeight() / 2.0);
        final int lineRowIndex = (int) Math.floor(lineMiddleY / rowHeight);

        for (Node child : desktopPane.getChildrenUnmodifiable()) {
            if (child == dropRegion || !child.isVisible() || !(child instanceof Pane)) {
                continue;
            }

            Pane pane = (Pane) child;
            Bounds b = getWindowBounds(child);
            double occupantMiddleY = (b.getMinY() + b.getMaxY()) / 2.0;
            int occupantRowIndex = (int) Math.floor(occupantMiddleY / rowHeight);

            if ((occupantRowIndex == lineRowIndex && b.getMinX() >= lineX) || occupantRowIndex > lineRowIndex) {
                // Use the factory method to create WindowShiftInfo
                windowsToShift.add(WindowShiftInfo.fromCurrentPosition(pane, b.getMinX(), b.getMinY()));
            }
        }

        // Sort by row first, then by minX
        windowsToShift.sort((w1, w2) -> {
            int r1 = (int) Math.floor(w1.currentY() / rowHeight);
            int r2 = (int) Math.floor(w2.currentY() / rowHeight);

            if (r1 != r2) {
                return Integer.compare(r1, r2);
            }
            return Double.compare(w1.currentX(), w2.currentX());
        });

        return windowsToShift.toImmutable();
    }

    /**
     * Calculates the target positions for windows that need to be shifted.
     * Windows flow continuously from one row to the next as needed, maintaining
     * proper spacing and wrapping to subsequent rows when necessary.
     * <p>
     * The algorithm starts placing windows from the specified (startX, startY) position
     * and continues in a left-to-right, top-to-bottom flow, respecting the horizontal
     * and vertical gaps between windows.
     *
     * @param windowsToShift the list of windows to shift with their current positions
     * @param startX the x-coordinate from which to start placing shifted windows
     * @param startY the y-coordinate of the row where shifting starts
     * @param desktopWidth the total width of the desktop
     * @param desktopHeight the total height of the desktop
     * @param workspace the workspace for gap values
     * @return a new list of WindowShiftInfo with calculated target positions
     */
    private ImmutableList<WindowShiftInfo> calculateShiftPositions(
            ImmutableList<WindowShiftInfo> windowsToShift,
            double startX, double startY,
            double desktopWidth, double desktopHeight,
            KLWorkspace workspace) {

        if (windowsToShift.isEmpty()) {
            return windowsToShift;
        }

        final double hgap = workspace.getHorizontalGap();
        final double vgap = workspace.getVerticalGap();
        final double rowHeight = desktopHeight / ROWS;

        double currentX = startX;
        double currentY = startY;
        int currentRow = (int) Math.floor(currentY / rowHeight);

        MutableList<WindowShiftInfo> updatedShifts = Lists.mutable.empty();

        for (WindowShiftInfo shiftInfo : windowsToShift) {
            final double windowWidth = shiftInfo.window().getWidth();
            final double windowHeight = shiftInfo.window().getHeight();

            // Check if window fits in current position
            if (currentX + windowWidth > desktopWidth) {
                // Move to next row
                currentRow++;
                if (currentRow >= ROWS) {
                    // No more rows available, keep at current position
                    updatedShifts.add(shiftInfo); // Keep original positions
                    continue;
                }
                currentX = hgap;
                currentY = currentRow * rowHeight + vgap;
            }

            // Create new WindowShiftInfo with calculated target position
            WindowShiftInfo updatedShift = shiftInfo.withTargetPosition(currentX, currentY);
            updatedShifts.add(updatedShift);

            // Prepare position for next window
            currentX = currentX + windowWidth + hgap;
        }

        return updatedShifts.toImmutable();
    }

    /**
     * Animates the shifting of windows to their new positions.
     * Uses a smooth animation with easing for better user experience.
     * All windows are animated simultaneously in a single timeline for
     * coordinated movement.
     *
     * @param windowsToShift the list of windows with their target positions
     */
    private void animateWindowShifts(ImmutableList<WindowShiftInfo> windowsToShift) {
        if (windowsToShift.isEmpty()) {
            return;
        }

        // Stop any existing shift animation
        if (shiftTimeline != null) {
            shiftTimeline.stop();
            shiftTimeline = null;
        }

        // Create a single timeline for all window shifts
        shiftTimeline = new Timeline();

        for (WindowShiftInfo shiftInfo : windowsToShift) {
            if (shiftInfo.targetX != shiftInfo.currentX || shiftInfo.targetY != shiftInfo.currentY) {
                KeyFrame startFrame = new KeyFrame(Duration.ZERO,
                        new KeyValue(shiftInfo.window.layoutXProperty(), shiftInfo.currentX),
                        new KeyValue(shiftInfo.window.layoutYProperty(), shiftInfo.currentY)
                );

                KeyFrame endFrame = new KeyFrame(DEFAULT_ANIMATION_DURATION,
                        new KeyValue(shiftInfo.window.layoutXProperty(), shiftInfo.targetX, Interpolator.EASE_BOTH),
                        new KeyValue(shiftInfo.window.layoutYProperty(), shiftInfo.targetY, Interpolator.EASE_BOTH));

                shiftTimeline.getKeyFrames().addAll(startFrame, endFrame);
            }
        }

        shiftTimeline.play();
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
        final ImmutableList<Bounds> occupantBounds = Lists.immutable.fromStream(desktopPane.getChildrenUnmodifiable().stream()
                .filter(n -> n != desktopPane.getDropRegion() && n.isVisible())
                .map(this::getWindowBounds));

        return doThreeRowPlacement(width, height, startX, startY, desktopWidth, desktopHeight,
                hgap, vgap, occupantBounds);
    }

    /**
     * Core internal method for scanning up to three rows for a valid placement that does not overlap
     * occupant windows. Used by the general {@code findThreeRowPlacement}.
     * <p>
     * The algorithm attempts to place the window in each row, starting from the row containing
     * {@code startY} and continuing through subsequent rows. Within each row, it tries multiple
     * candidate X positions based on the edges of existing windows and attempts to find a Y position
     * that avoids overlaps.
     *
     * @param width The width of the window to place
     * @param height The height of the window to place
     * @param startX The initial X coordinate to start searching from
     * @param startY The initial Y coordinate used to determine the starting row
     * @param desktopWidth The total width of the desktop
     * @param desktopHeight The total height of the desktop
     * @param hgap The horizontal gap between windows
     * @param vgap The vertical gap between windows
     * @param occupantBounds the bounding boxes of windows to be considered for overlap checks
     * @return a {@link Point2D} representing the top-left corner for the new window,
     *         or {@code null} if no valid placement is found
     */
    private Point2D doThreeRowPlacement(double width, double height,
                                        double startX, double startY,
                                        double desktopWidth, double desktopHeight,
                                        double hgap, double vgap,
                                        ImmutableList<Bounds> occupantBounds) {

        final double rowHeight = desktopHeight / ROWS;

        // Identify which row to start in
        int startRowIndex = (int) Math.floor(startY / rowHeight);
        startRowIndex = Math.max(0, Math.min(ROWS - 1, startRowIndex));

        // Generate row order based on startRowIndex
        MutableList<Integer> rowOrder = Lists.mutable.empty();
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
            final ImmutableList<Bounds> rowOccupants = Lists.immutable.fromStream(occupantBounds.stream()
                    .filter(b -> b.getMaxY() > rowTop && b.getMinY() < rowBottom)
                    .sorted(Comparator.comparingDouble(Bounds::getMinX)));

            final double rowLeft = 0;
            final double rowRight = desktopWidth;
            final double initialX = (rowIndex == startRowIndex)
                    ? Math.max(startX, rowLeft + hgap) : (rowLeft + hgap);

            // Build candidate X positions from occupant edges
            MutableList<Double> xCandidates = Lists.mutable.empty();
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

    /**
     * Retrieves the bounds of a given node, handling both Pane nodes (which have
     * layout coordinates and actual dimensions) and other nodes (which use
     * preferred dimensions).
     *
     * @param node The node to get bounds for
     * @return A BoundingBox representing the node's position and dimensions
     */
    private Bounds getWindowBounds(Node node) {
        if (node instanceof Pane pane) {
            return new BoundingBox(pane.getLayoutX(), pane.getLayoutY(),
                    pane.getWidth(), pane.getHeight());
        } else return node.getLayoutBounds();
    }

    // =========================================================================
    //                             SCROLLING UTILS
    // =========================================================================

    /**
     * Smoothly scrolls the {@link ScrollPane} so that the top edge of the specified node
     * is visible. Uses a brief animation for a user-friendly experience.
     * <p>
     * The scroll animation centers the viewport horizontally on the node while positioning
     * the top of the node (minus the vertical gap) at the top of the viewport. This ensures
     * the newly placed window is fully visible with appropriate spacing.
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
        final double nodeTopY = nodeBounds.getMinY() - desktopBounds.getMinY() - getSkinnable().getVerticalGap();
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
                new KeyFrame(DEFAULT_ANIMATION_DURATION,
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
     * @param x             the x-coordinate of the rectangle's top-left corner
     * @param y             the y-coordinate of the rectangle's top-left corner
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
     * @param x             the x-coordinate of the rectangle's top-left corner
     * @param y             the y-coordinate of the rectangle's top-left corner
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
     * @param x      the x-coordinate of the rectangle's top-left corner
     * @param y      the y-coordinate of the rectangle's top-left corner
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @return {@code true} if the rectangle intersects at least one existing window;
     *         {@code false} otherwise
     */
    private boolean overlapsWithExistingWindows(double x, double y, double width, double height) {
        final Bounds newWindowBounds = new BoundingBox(x, y, width, height);
        return desktopPane.getChildrenUnmodifiable().stream()
                .filter(n -> n != desktopPane.getDropRegion() && n.isVisible())
                .map(this::getWindowBounds)
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
     * @param x              the x-coordinate of the rectangle's top-left corner
     * @param y              the y-coordinate of the rectangle's top-left corner
     * @param width          the width of the rectangle
     * @param height         the height of the rectangle
     * @param occupantBounds the bounding boxes of existing occupants to check against
     * @return the first occupant {@link Bounds} that intersects the rectangle, or
     *         {@code null} if no intersection occurs
     */
    private Bounds occupantIntersect(double x, double y, double width, double height,
                                     ImmutableList<Bounds> occupantBounds) {
        final BoundingBox candidate = new BoundingBox(x, y, width, height);
        for (Bounds b : occupantBounds) {
            if (candidate.intersects(b)) {
                return b;
            }
        }
        return null;
    }

    // =========================================================================
    //                            DISPOSAL / CLEANUP
    // =========================================================================

    /**
     * Cleans up listeners and references to avoid potential memory leaks.
     * This method is called when the skin is detached from its control.
     * <p>
     * The cleanup process includes:
     * <ul>
     *   <li>Removing the window list change listener</li>
     *   <li>Unsubscribing from desktop resize notifications</li>
     *   <li>Unsubscribing from panning event handlers</li>
     *   <li>Removing window support from all windows</li>
     *   <li>Clearing all children from the skin</li>
     * </ul>
     */
    @Override
    public void dispose() {
        // Stop any running animations
        if (autoScrollTimeline != null) {
            autoScrollTimeline.stop();
            autoScrollTimeline = null;
        }

        if (shiftTimeline != null) {
            shiftTimeline.stop();
            shiftTimeline = null;
        }

        // Clean up window list listener
        if (workspaceWindows != null) {
            workspaceWindows.removeListener(weakWindowsListChangeListener);
            workspaceWindows = null;
        }

        if (getSkinnable() != null) {
            // Clean up size change subscription
            cleanupSubscription(DESKTOP_RESIZE_SUBSCRIPTION_KEY);
            cleanupSubscription(PANNING_SUBSCRIPTION_KEY);

            getSkinnable().setOnDragOver(null);
            getSkinnable().setOnDragExited(null);

            // Clean up all windows support
            Lists.immutable.ofAll(getSkinnable().getWindows()).forEach(this::removeWindow);
        }

        // Clear children and call super dispose
        getChildren().clear();
        super.dispose();
    }

    /**
     * Cleans up a subscription stored in the skinnable's properties map.
     * <p>
     * This utility method retrieves a {@link Subscription} from the control's properties
     * using the specified key, unsubscribes from it to release resources, and then
     * removes the entry from the properties map. If no subscription exists for the
     * given key, this method does nothing.
     *
     * @param key the property key under which the subscription is stored
     */
    private void cleanupSubscription(String key) {
        if (getSkinnable().getProperties().containsKey(key)) {
            Subscription subscription = (Subscription) getSkinnable().getProperties().get(key);
            subscription.unsubscribe();
            getSkinnable().getProperties().remove(key);
        }
    }

    // =========================================================================
    //                            DESKTOP PANE CLASS
    // =========================================================================

    /**
     * A custom pane that acts as the "desktop" for the workspace.
     * It hosts multiple {@link ChapterKlWindow} nodes and displays
     * a {@link KLDropRegion} when an external drag enters the workspace.
     * <p>
     * The desktop pane provides methods to show and hide drop regions
     * during drag-and-drop operations, indicating where new windows
     * can be placed.
     */
    private static class DesktopPane extends Pane {

        /** The drop region indicator shown during drag operations */
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
         * The drop region is positioned and sized according to the provided bounds.
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