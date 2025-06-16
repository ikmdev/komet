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
package dev.ikm.komet.kview.fxutils;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

public abstract class FXUtils {

    /**
     * Executor that ensures tasks run on the JavaFX application thread.
     */
    public static final Executor FX_THREAD_EXECUTOR = FXUtils::runOnFxThread;

    /**
     * Default animation duration for transitions.
     */
    public static final Duration DEFAULT_ANIMATION_DURATION = Duration.millis(300);

    /**
     * Property key for storing scene synchronization listener in a node's properties map.
     */
    private static final String WINDOW_SCENE_SYNC_LISTENER = "windowSceneSyncListener";

    /**
     * Property key for storing height synchronization listener in a node's properties map.
     */
    private static final String WINDOW_HEIGHT_SYNC_LISTENER = "windowHeightSyncListener";

    /**
     * The number of height changes to wait before setting the preferred height.
     */
    private static final int SIMPLE_HEIGHT_UPDATE_THRESHOLD = 3;

    /**
     * Given a Text node including its CSS styling this function can determine the bounds.
     * This allows you to create callouts and predetermine the width and height of a Text node.
     *
     * @param origText - A Text node to measure.
     * @return Bounds The bounds of a Text Node.
     */
    public static Bounds textFontMetricsBounds(final Text origText) {
        Text text = new Text(origText.getText());
        text.getStyleClass().addAll(origText.getStyleClass());
        text.setStyle(origText.getStyle());

        new Scene(new Group(text));
        text.applyCss();
        return text.getLayoutBounds();
    }

    /**
     * Find the bounds of a node relative to a parent levels up the hiearchy. For example
     * A Parent(Pane) can have a child (HBox) that contains a Node (Button).
     * <pre>
     *     Bounds b = localToParent(targetNode, 2);
     *     double x = b.getMinX();
     *     double y = b.getMinY();
     * </pre>
     *
     * @param node     The target node to find out it's bounds relative to a parent up the graph.
     * @param levelsUp How many levels up
     * @return The bounds of the node relative to the parent.
     */
    public static Bounds localToParent(Node node, int levelsUp) {
        Bounds bounds = node.getBoundsInParent();
        Parent parent = node.getParent();

        for (int i = 0; i < levelsUp; i++) {
            bounds = parent.localToParent(bounds);
            parent = parent.getParent();
        }
        return bounds;
    }

    /**
     * Fits an ImageView to the given bounds if the Image width or height doesn't fit
     * within those bounds. Otherwise, sets the ImageView to the original dimensions of
     * its Image.
     *
     * @param imageView      the ImageView we want to update.
     * @param maxImageWidth  the maximum image bounds width.
     * @param maxImageHeight the maximum image bounds height.
     */
    public static void fitImageToBounds(ImageView imageView, int maxImageWidth, int maxImageHeight) {
        Image image = imageView.getImage();
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        if (imageWidth > maxImageWidth || imageHeight > maxImageHeight) {
            imageView.setFitWidth(maxImageWidth);
            imageView.setFitHeight(maxImageHeight);
        } else {
            imageView.setFitWidth(imageWidth);
            imageView.setFitHeight(imageHeight);
        }
    }

    public static Bounds localToParent(Node node, Parent targetParent) {
        Bounds bounds = node.getBoundsInParent();
        Parent parent = node.getParent();

        while (!targetParent.equals(parent)) {
            bounds = parent.localToParent(bounds);
            parent = parent.getParent();
        }
        return bounds;
    }

    public static Point2D centerPointRelativeToParent(Circle circle, Pane targetParent) {
        Bounds bounds = localToParent(circle, targetParent);
        return new Point2D(bounds.getCenterX(), bounds.getCenterY());
    }

    public static Optional<Parent> findParent(Node child, String styleClass) {
        Parent parent = child.getParent();
        if (parent == null) {
            return Optional.empty();
        } else if (parent.getStyleClass().contains(styleClass)) {
            return Optional.of(parent);
        } else {
            return findParent(parent, styleClass);
        }

    }

    /**
     * Ensures that a code segment is run on the FX thread.
     *
     * @param runnable a {@code Runnable} encapsulating the code
     */
    public static void runOnFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    /**
     * Retrieves the currently focused {@link Window}.
     * <p>
     * This method iterates through all available windows and returns the first window
     * that is currently focused. If no window is focused, it returns {@code null}.
     *
     * @return the focused {@link Window}, or {@code null} if no window is currently focused.
     */
    public static Window getFocusedWindow() {
        return Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
    }

    /**
     * Synchronizes pane's preferred height using a counter-based approach.
     * Waits for {@value #SIMPLE_HEIGHT_UPDATE_THRESHOLD} height changes before
     * setting preferred height to match actual height and removing itself.
     * Suitable for simple UI scenarios with stable content.
     *
     * @param targetPane pane to synchronize (not null)
     * @throws NullPointerException if targetPane is null
     * @see #synchronizeHeightWithSceneAwareness(Pane)
     */
    public static void synchronizeHeightWithCounter(Pane targetPane) {
        Objects.requireNonNull(targetPane, "Target pane cannot be null");

        // Register a one-time resize listener to set the final preferred height
        InvalidationListener resizeListener = new InvalidationListener() {
            private int updateCount = 0;
            @Override
            public void invalidated(Observable observable) {
                updateCount++;

                // Wait for the threshold number of height changes
                if (updateCount >= SIMPLE_HEIGHT_UPDATE_THRESHOLD) {
                    double height = targetPane.getHeight();
                    if (height > 0) {
                        targetPane.setPrefHeight(height);
                    }

                    // Remove the listener once done
                    targetPane.heightProperty().removeListener(this);
                }
            }
        };

        targetPane.heightProperty().addListener(resizeListener);
    }

    /**
     * Synchronizes pane's preferred height using scene-aware approach with property-based
     * listener tracking. Registers listeners for scene changes and height changes with
     * proper cleanup. More robust for dynamic UI scenarios where panes may be added or
     * removed from scene graph.
     *
     * @param targetPane pane to synchronize (not null)
     * @throws NullPointerException if targetPane is null
     * @see #synchronizeHeightWithCounter(Pane)
     */
    public static void synchronizeHeightWithSceneAwareness(Pane targetPane) {
        Objects.requireNonNull(targetPane, "Target pane cannot be null");

        // Create the window scene sync listener
        InvalidationListener windowSceneSyncListener = sceneObservable -> {
            Scene scene = targetPane.getScene();
            if (scene != null) {
                // Synchronize the window panel's preferred height with its actual height after rendering.
                Platform.runLater(() -> {
                    targetPane.layout();

                    // Create the window height sync listener
                    InvalidationListener windowHeightSyncListener = heightObservable -> {
                        final double height = targetPane.getHeight();
                        if (height > 0) {
                            targetPane.setPrefHeight(height);

                            if (targetPane.getProperties().containsKey(WINDOW_HEIGHT_SYNC_LISTENER)) {
                                final InvalidationListener heightListener =
                                        (InvalidationListener) targetPane.getProperties().get(WINDOW_HEIGHT_SYNC_LISTENER);
                                targetPane.heightProperty().removeListener(heightListener);
                                targetPane.getProperties().remove(WINDOW_HEIGHT_SYNC_LISTENER);
                            }
                        }
                    };

                    // Store a reference for later cleanup
                    targetPane.getProperties().put(WINDOW_HEIGHT_SYNC_LISTENER, windowHeightSyncListener);

                    // Add the listener to the window panel
                    targetPane.heightProperty().addListener(windowHeightSyncListener);
                });
            }

            if (targetPane.getProperties().containsKey(WINDOW_SCENE_SYNC_LISTENER)) {
                final InvalidationListener sceneListener =
                        (InvalidationListener) targetPane.getProperties().get(WINDOW_SCENE_SYNC_LISTENER);
                targetPane.sceneProperty().removeListener(sceneListener);
                targetPane.getProperties().remove(WINDOW_SCENE_SYNC_LISTENER);
            }
        };

        // Store a reference for later cleanup
        targetPane.getProperties().put(WINDOW_SCENE_SYNC_LISTENER, windowSceneSyncListener);

        // Add the listener to the window panel
        targetPane.sceneProperty().addListener(windowSceneSyncListener);
    }

    /**
     * Gets the topmost Pane from the provided parentNode.
     * @param parentNode The node to start from
     * @return The topmost pane, or null if
     */
    public static Pane getTopmostPane(Node parentNode) {
        List<Pane> paneHierarchy = new ArrayList<>();

        getTopmostPaneHierarchy(paneHierarchy, parentNode);

        return paneHierarchy.isEmpty() ? null : paneHierarchy.getLast();
    }

    /**
     * Recursively goes through each parent and accumulates in order the Panes that are
     * are in the hierarchy.
     * @param paneHierarchy List to put the Pane objects into
     * @param node The current node in the parent hierarchy
     */
    private static void getTopmostPaneHierarchy(List<Pane> paneHierarchy, Node node) {
        var parent = node.getParent();

        if (parent instanceof Pane pane) {
            paneHierarchy.add(pane);
        }

        if (parent != null) {
            getTopmostPaneHierarchy(paneHierarchy, parent);
        }
    }

}