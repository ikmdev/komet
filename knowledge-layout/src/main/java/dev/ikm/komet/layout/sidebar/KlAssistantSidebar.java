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
package dev.ikm.komet.layout.sidebar;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The assistant sidebar of a top-level window (a journal, the landing page, the KL editor). Collapsed, it
 * takes no room: a floating button sits over the top-right corner of the window's working area. Expanded,
 * the {@linkplain KlAssistantPane pane} docks in the right slot of the window's root and the working area
 * gives up the width; the pane's own header carries the control that collapses it again.
 * {@linkplain #maximizedProperty() Maximized}, the open pane covers the working area instead.
 *
 * <p>The pane is made on first opening, by the {@link KlAssistantPaneProvider} a plugin provides, and is
 * then only hidden, never taken out of the scene graph: a card forgets its saved state when its node leaves
 * it. Open state and width are kept under the window's preferences node.
 */
public final class KlAssistantSidebar {

    private static final double DEFAULT_WIDTH = 380;
    private static final double MIN_WIDTH = 320;
    private static final double MAX_WIDTH = 760;
    private static final double BUTTON_SIZE = 38;
    /** Clear of the working area's own scrollbar. */
    private static final double BUTTON_RIGHT_GAP = 18;
    private static final double BUTTON_TOP_GAP = 12;

    private static final String PREF_EXPANDED = "expanded";
    private static final String PREF_WIDTH = "width";
    private static final String PREF_MAXIMIZED = "maximized";

    private final BorderPane root;
    private final Supplier<Node> workingArea;
    private Supplier<Node> maximizedArea;
    private final KlAssistantPaneProvider provider;
    private final KometPreferences preferences;
    private final ViewProperties viewProperties;
    private final UUID journalTopic;

    private final Button openButton = new Button();
    private final Region grip = new Region();
    /** The pane fills the dock; the grip lies over its left edge, so it takes no width of its own. */
    private final StackPane dock = new StackPane(grip);
    private final BooleanProperty expanded = new SimpleBooleanProperty(false);
    private KlAssistantPane pane;
    private boolean placementPending;
    private double dragStartScreenX;
    private double dragStartWidth;

    /**
     * Gives a window the assistant sidebar, when a plugin provides the assistant.
     *
     * @param root           the window's root, whose right slot the sidebar takes
     * @param workingArea    the node whose top-right corner the open button floats over; read at each
     *                       layout, so a window that swaps its content names the current one
     * @param preferences    the node the sidebar and its pane keep their state under, this window's alone
     * @param viewProperties the window's view
     * @param journalTopic   the journal's event topic, or {@code null} when the window is not a journal
     * @return the sidebar, or empty when no plugin provides the assistant
     */
    public static Optional<KlAssistantSidebar> install(BorderPane root, Supplier<Node> workingArea,
                                                       KometPreferences preferences, ViewProperties viewProperties,
                                                       UUID journalTopic) {
        return PluggableService.load(KlAssistantPaneProvider.class).findFirst()
                .map(provider -> new KlAssistantSidebar(root, workingArea, provider, preferences, viewProperties,
                        journalTopic));
    }

    private KlAssistantSidebar(BorderPane root, Supplier<Node> workingArea, KlAssistantPaneProvider provider,
                               KometPreferences preferences, ViewProperties viewProperties, UUID journalTopic) {
        this.root = root;
        this.workingArea = workingArea;
        this.maximizedArea = workingArea;
        this.provider = provider;
        this.preferences = preferences;
        this.viewProperties = viewProperties;
        this.journalTopic = journalTopic;

        String stylesheet = KlAssistantSidebar.class.getResource("kl-assistant-sidebar.css").toExternalForm();

        Region spark = new Region();
        spark.getStyleClass().add("kl-assistant-spark");
        openButton.setGraphic(spark);
        openButton.setId("assistantSidebarOpen");
        // In place of "button": each window's scene styles ".button" its own way (the landing page's
        // ".landing-container .button" outranks any single-class rule, the KL editor's adds a border),
        // and this one looks the same in all of them.
        openButton.getStyleClass().setAll("kl-assistant-open-button");
        openButton.getStylesheets().add(stylesheet);
        openButton.setTooltip(new Tooltip("Open Komet Assistant"));
        openButton.setFocusTraversable(false);
        openButton.setManaged(false);
        // The window's root lays out its slots only; drawn above them whatever the order of its children,
        // which changes when a window swaps its content.
        openButton.setViewOrder(-1000);
        openButton.setOnAction(e -> expanded.set(true));
        root.getChildren().add(openButton);

        grip.getStyleClass().add("kl-assistant-sidebar-grip");
        StackPane.setAlignment(grip, Pos.CENTER_LEFT);
        grip.setMaxHeight(Double.MAX_VALUE);
        grip.setOnMousePressed(e -> {
            dragStartScreenX = e.getScreenX();
            dragStartWidth = dock.getPrefWidth();
        });
        grip.setOnMouseDragged(e -> dock.setPrefWidth(clampWidth(dragStartWidth + dragStartScreenX - e.getScreenX())));
        grip.setOnMouseReleased(e -> preferences.putDouble(PREF_WIDTH, dock.getPrefWidth()));

        dock.getStyleClass().add("kl-assistant-sidebar");
        dock.getStylesheets().add(stylesheet);
        dock.setId("assistantSidebar");
        dock.setMinWidth(MIN_WIDTH);
        // As tall as the window, whatever the pane's content asks for. A window root can declare its minimum
        // height as its preferred height (the journal's does), and a long reply in the pane would then raise
        // both, pushing the root past the bottom of its window. The right slot is filled whatever these are.
        dock.setMinHeight(0);
        dock.setPrefHeight(0);
        dock.setPrefWidth(clampWidth(preferences.getDouble(PREF_WIDTH, DEFAULT_WIDTH)));
        dock.setVisible(false);
        dock.setManaged(false);
        root.setRight(dock);

        // Read before the subscriptions below, which run at once and record the state as it is now.
        boolean wasExpanded = preferences.getBoolean(PREF_EXPANDED, false);
        maximized.set(preferences.getBoolean(PREF_MAXIMIZED, false));
        expanded.subscribe(this::showExpanded);
        maximized.subscribe(this::showMaximized);
        root.needsLayoutProperty().subscribe(needsLayout -> schedulePlacement());
        schedulePlacement();
        if (wasExpanded) {
            // Once the window is up: the pane binds to the window's view as it is made.
            Platform.runLater(() -> expanded.set(true));
        }
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /** @return the docked container the pane sits in, for a window whose scene lacks stylesheets the pane needs */
    public Region getDock() {
        return dock;
    }

    /**
     * Names the part of the window the maximized pane covers, when it is more than the working area the open
     * button floats over.
     *
     * @param maximizedArea the node whose bounds the maximized pane takes; read at each layout
     */
    public void setMaximizedArea(Supplier<Node> maximizedArea) {
        this.maximizedArea = maximizedArea;
    }

    /** The window is closing: the pane saves what it keeps and lets go of what outlives it. */
    public void shutdown() {
        if (pane != null) {
            pane.shutdown();
        }
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- host name
    /**
     * The window's name as the person knows it, such as a journal's name. The pane shows it and tells the
     * assistant where it is. A window whose name can change binds this to the name's source.
     */
    private final StringProperty hostName = new SimpleStringProperty(this, "hostName", "");
    public StringProperty hostNameProperty() { return hostName; }
    public String getHostName() { return hostName.get(); }
    public void setHostName(String value) { hostName.set(value); }

    // -- maximized
    /**
     * Whether the open pane covers the window's working area in place of docking at its right edge. The
     * pane's header carries the control that switches it; kept under the window's preferences node.
     */
    private final BooleanProperty maximized = new SimpleBooleanProperty(this, "maximized", false);
    public BooleanProperty maximizedProperty() { return maximized; }
    public boolean isMaximized() { return maximized.get(); }
    public void setMaximized(boolean value) { maximized.set(value); }

    /***************************************************************************
     *                                                                         *
     * Implementation                                                          *
     *                                                                         *
     **************************************************************************/

    private void showExpanded(boolean show) {
        if (show && pane == null) {
            pane = provider.create(new KlAssistantPaneProvider.Host(preferences, viewProperties, journalTopic,
                    () -> expanded.set(false)));
            pane.hostNameProperty().bind(hostName);
            pane.maximizedProperty().bindBidirectional(maximized);
            Region paneNode = pane.fxObject();
            paneNode.setMinWidth(0);
            paneNode.setMinHeight(0);
            paneNode.setMaxWidth(Double.MAX_VALUE);
            dock.getChildren().add(0, paneNode);   // under the grip
        }
        dock.setVisible(show);
        openButton.setVisible(!show);
        preferences.putBoolean(PREF_EXPANDED, show);
        placeDock();
    }

    private void showMaximized(boolean maximize) {
        preferences.putBoolean(PREF_MAXIMIZED, maximize);
        placeDock();
    }

    /**
     * Docked, the pane is the root's right slot. Maximized, it leaves the root's layout, so the working area
     * takes the slot's width back, and is placed over that area, above the window's content; its width is
     * then not the person's to drag, and the docked width is kept for the return.
     */
    private void placeDock() {
        boolean floating = maximized.get();
        dock.setManaged(expanded.get() && !floating);
        dock.setViewOrder(floating ? -1000 : 0);
        grip.setVisible(!floating);
        schedulePlacement();
    }

    private static double clampWidth(double width) {
        return Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, width));
    }

    /**
     * Places the open button and the maximized pane after the layout pass that asked for it: the root's flag
     * clears before its children are laid out, so the working area's bounds are read one pulse later.
     */
    private void schedulePlacement() {
        if (placementPending) {
            return;
        }
        placementPending = true;
        Platform.runLater(() -> {
            placementPending = false;
            placeOpenButton();
            placeMaximizedDock();
        });
    }

    private void placeOpenButton() {
        Bounds bounds = boundsInRoot(workingArea.get());
        openButton.resizeRelocate(bounds.getMaxX() - BUTTON_SIZE - BUTTON_RIGHT_GAP, bounds.getMinY() + BUTTON_TOP_GAP,
                BUTTON_SIZE, BUTTON_SIZE);
    }

    private void placeMaximizedDock() {
        if (expanded.get() && maximized.get()) {
            Bounds bounds = boundsInRoot(maximizedArea.get());
            dock.resizeRelocate(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }
    }

    private Bounds boundsInRoot(Node area) {
        return area == null || area.getScene() == null
                ? root.getLayoutBounds()
                : root.sceneToLocal(area.localToScene(area.getLayoutBounds()));
    }
}
