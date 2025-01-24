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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import static dev.ikm.komet.kview.controls.KLWorkspace.DEFAULT_HORIZONTAL_GAP;
import static dev.ikm.komet.kview.controls.KLWorkspace.DEFAULT_VERTICAL_GAP;
import static dev.ikm.komet.kview.controls.KLWorkspace.DESKTOP_PANE_STYLE_CLASS;
import static dev.ikm.komet.kview.controls.KLWorkspace.ROWS;
import static dev.ikm.komet.kview.controls.KLWorkspace.COLUMNS;
import static dev.ikm.komet.kview.controls.KLWorkspace.STANDARD_HEIGHT;
import static dev.ikm.komet.kview.controls.KLWorkspace.STANDARD_WIDTH;

/**
 * A custom skin implementation for the {@link KLWorkspace} control. This skin sets up
 * a scrollable "desktop" area in which {@code ChapterKlWindow} objects (each containing
 * a {@link Pane} root) can be placed and manipulated.
 *
 * <p>This skin handles the following features:
 * <ul>
 *     <li>Panning the desktop pane (via holding down the Control key).</li>
 *     <li>Displaying a drop region when external data is dragged over the workspace.</li>
 *     <li>Adding or removing windows from the desktop in response to changes in the
 *         workspace's list of {@code ChapterKlWindow} objects.</li>
 *     <li>Resizing and dragging support for individual windows via {@link WindowSupport}.</li>
 * </ul>
 *
 * @see KLWorkspace
 */
public class KLWorkspaceSkin extends SkinBase<KLWorkspace> {

    /**
     * The main panel where all workspace windows (nodes) are placed.
     */
    private final DesktopPane desktopPane = new DesktopPane();

    /**
     * The scrollable container that allows panning over the desktop pane.
     */
    private final ScrollPane desktopScrollPane;

    /**
     * A reference to the list of windows in the associated {@link KLWorkspace}.
     *
     * <p>These windows are displayed as child nodes on the {@link DesktopPane}.
     */
    private ObservableList<ChapterKlWindow<Pane>> workspaceWindows;

    /**
     * A listener to monitor changes in the workspace's windows list and update
     * the {@link #desktopPane} accordingly.
     */
    private final ListChangeListener<ChapterKlWindow<Pane>> workspaceWindowsListChangeListener =
            change -> {
                while (change.next()) {
                    if (change.wasRemoved()) {
                        desktopPane.getChildren().removeAll(change.getRemoved().stream()
                                .map(ChapterKlWindow::getRootPane)
                                .toList());
                    }
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(this::addWindow);
                    }
                }
            };

    /**
     * A weak reference to the list-change listener to prevent memory leaks.
     */
    private final WeakListChangeListener<ChapterKlWindow<Pane>> weakItemListChangeListener =
            new WeakListChangeListener<>(workspaceWindowsListChangeListener);

    /**
     * Constructs a new skin for the specified {@link KLWorkspace} control.
     *
     * @param workspace the {@link KLWorkspace} control to which this skin is attached
     */
    public KLWorkspaceSkin(KLWorkspace workspace) {
        super(workspace);

        // Style class to allow custom CSS for the desktop pane.
        desktopPane.getStyleClass().add(DESKTOP_PANE_STYLE_CLASS);

        // Create and configure the scroll pane for the desktop pane.
        desktopScrollPane = new ScrollPane(desktopPane);
        desktopPane.setPrefWidth(STANDARD_WIDTH * COLUMNS);
        desktopPane.setPrefHeight(STANDARD_HEIGHT * ROWS);
        getChildren().add(desktopScrollPane);

        // Initialize workspace windows and add existing nodes to the desktop pane.
        updateWorkspaceWindows();
        for (ChapterKlWindow<Pane> window : workspaceWindows) {
            addWindow(window);
        }

        // Register key event handlers on the workspace for dynamic panning behavior.
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

        // Handle drag-and-drop events to show or hide the drop region in the desktop pane.
        workspace.setOnDragOver(event -> {
            desktopPane.showDropRegion();
            if (event.getGestureSource() != null && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        workspace.setOnDragExited(event -> desktopPane.hideDropRegion());

        // Listen for changes to the workspace properties.
        registerChangeListener(workspace.windowsProperty(), o -> updateWorkspaceWindows());
        registerChangeListener(workspace.horizontalGapProperty(), o -> {
            desktopPane.setHorizontalGap(workspace.getHorizontalGap());
            desktopPane.requestLayout();
        });
        registerChangeListener(workspace.verticalGapProperty(), o -> {
            desktopPane.setVerticalGap(workspace.getVerticalGap());
            desktopPane.requestLayout();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (getSkinnable() == null) {
            return;
        }
        if (workspaceWindows != null) {
            workspaceWindows.removeListener(weakItemListChangeListener);
            workspaceWindows = null;
        }
        getChildren().clear();
        super.dispose();

        desktopPane.prefWidthProperty().unbind();
        desktopPane.prefHeightProperty().unbind();
    }

    /**
     * Invoked whenever the workspace's windows list changes. Updates the local
     * {@link #workspaceWindows} reference and attaches the necessary listener.
     */
    private void updateWorkspaceWindows() {
        // Detach the old listener, if any.
        if (workspaceWindows != null) {
            workspaceWindows.removeListener(weakItemListChangeListener);
        }

        // Update reference and attach a new listener.
        this.workspaceWindows = getSkinnable().getWindows();
        if (workspaceWindows != null) {
            workspaceWindows.addListener(weakItemListChangeListener);
        }

        // Request a re-layout of the desktop pane.
        desktopPane.requestLayout();
    }

    /**
     * Adds a node to the {@link DesktopPane} and configures it for window-like behavior
     * via {@link WindowSupport}.
     *
     * @param window the {@link ChapterKlWindow} to add to the desktop pane
     */
    private void addWindow(ChapterKlWindow<Pane> window) {
        final Pane windowPanel = window.getRootPane();

        // Attach window dragging/resizing functionality.
        WindowSupport windowSupport = new WindowSupport(windowPanel, desktopPane);

        // If the drop region is visible, position and size the new window accordingly.
        final KLDropRegion dropRegion = desktopPane.getDropRegion();
        if (dropRegion.isVisible()) {
            windowPanel.setTranslateX(dropRegion.getLayoutX());
            windowPanel.setTranslateY(dropRegion.getLayoutY());
            windowPanel.setPrefWidth(dropRegion.getWidth());
            windowPanel.setPrefHeight(dropRegion.getHeight());
        }

        // Add the window panel to the desktop pane.
        desktopPane.getChildren().add(windowPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        // Make sure the ScrollPane is sized to fill the entire available area.
        desktopScrollPane.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    /**
     * A custom pane serving as the "desktop" for the workspace. It hosts multiple
     * child windows and displays a {@link KLDropRegion} when dragging external data
     * over the workspace.
     */
    private static class DesktopPane extends Pane {

        private double hgap = DEFAULT_HORIZONTAL_GAP;
        private double vgap = DEFAULT_VERTICAL_GAP;

        /**
         * The drop region that appears when dragging items over the desktop pane.
         */
        private final KLDropRegion dropRegion;

        /**
         * Constructs a new {@code DesktopPane} with a hidden {@link KLDropRegion}.
         */
        public DesktopPane() {
            dropRegion = new KLDropRegion();
            dropRegion.setManaged(false);
            dropRegion.setVisible(false);
        }

        /**
         * Sets the horizontal gap between items on the desktop pane.
         *
         * @param value the new horizontal gap
         */
        final void setHorizontalGap(double value) {
            this.hgap = value;
        }

        /**
         * Sets the vertical gap between items on the desktop pane.
         *
         * @param value the new vertical gap
         */
        final void setVerticalGap(double value) {
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
                layoutInArea(dropRegion, 2 * hgap, 2 * vgap,
                        dropRegionWidth, dropRegionHeight, 0, HPos.CENTER, VPos.CENTER);
            }
        }

        /**
         * Returns the {@link KLDropRegion} used to show where items can be dropped.
         *
         * @return the drop region for this pane
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
                dropRegion.setVisible(true);
            }
        }

        /**
         * Hides the drop region and removes it from the pane if present.
         */
        final void hideDropRegion() {
            getChildren().remove(dropRegion);
            dropRegion.setVisible(false);
        }
    }
}
