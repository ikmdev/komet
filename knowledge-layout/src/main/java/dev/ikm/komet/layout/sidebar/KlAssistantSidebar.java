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

    private final BorderPane root;
    private final Supplier<Node> workingArea;
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
        this.provider = provider;
        this.preferences = preferences;
        this.viewProperties = viewProperties;
        this.journalTopic = journalTopic;

        String stylesheet = KlAssistantSidebar.class.getResource("kl-assistant-sidebar.css").toExternalForm();

        Region spark = new Region();
        spark.getStyleClass().add("kl-assistant-spark");
        openButton.setGraphic(spark);
        openButton.setId("assistantSidebarOpen");
        openButton.getStyleClass().add("kl-assistant-open-button");
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
        dock.setPrefWidth(clampWidth(preferences.getDouble(PREF_WIDTH, DEFAULT_WIDTH)));
        dock.setVisible(false);
        dock.setManaged(false);
        root.setRight(dock);

        // Read before the subscription below, which runs at once and records the collapsed state.
        boolean wasExpanded = preferences.getBoolean(PREF_EXPANDED, false);
        expanded.subscribe(this::showExpanded);
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
            Region paneNode = pane.fxObject();
            paneNode.setMinWidth(0);
            paneNode.setMaxWidth(Double.MAX_VALUE);
            dock.getChildren().add(0, paneNode);   // under the grip
        }
        dock.setVisible(show);
        dock.setManaged(show);
        openButton.setVisible(!show);
        preferences.putBoolean(PREF_EXPANDED, show);
    }

    private static double clampWidth(double width) {
        return Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, width));
    }

    /**
     * Places the open button after the layout pass that asked for it: the root's flag clears before its
     * children are laid out, so the working area's bounds are read one pulse later.
     */
    private void schedulePlacement() {
        if (placementPending) {
            return;
        }
        placementPending = true;
        Platform.runLater(() -> {
            placementPending = false;
            placeOpenButton();
        });
    }

    private void placeOpenButton() {
        Node area = workingArea.get();
        Bounds bounds = area == null || area.getScene() == null
                ? root.getLayoutBounds()
                : root.sceneToLocal(area.localToScene(area.getLayoutBounds()));
        openButton.resizeRelocate(bounds.getMaxX() - BUTTON_SIZE - BUTTON_RIGHT_GAP, bounds.getMinY() + BUTTON_TOP_GAP,
                BUTTON_SIZE, BUTTON_SIZE);
    }
}
