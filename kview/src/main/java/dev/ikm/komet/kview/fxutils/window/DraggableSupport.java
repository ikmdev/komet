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
package dev.ikm.komet.kview.fxutils.window;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides a clean, declarative API for adding window dragging behavior to JavaFX panes.
 * <p>
 * This class serves as a façade over the more complex {@link WindowSupport} implementation, providing
 * simplified static methods for common operations. It creates a clear separation between:
 * <ul>
 *   <li>The styling of UI elements (handled through CSS)</li>
 *   <li>The behavior of draggable regions (managed through explicit Java code)</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>{@code
 * // Create a draggable window with a header as the drag handle
 * Pane window = new VBox();
 * Label header = new Label("Window Title");
 * Subscription windowSub = DraggableSupport.setupDraggableWindow(window, header);
 *
 * // Later, when the window is no longer needed:
 * windowSub.unsubscribe();
 * }</pre>
 *
 * @see WindowSupport The underlying implementation that manages the dragging behavior
 * @see Subscription The resource management pattern used for cleanup
 */
public abstract class DraggableSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DraggableSupport.class);

    /**
     * The property key used to store WindowSupport instance in container properties.
     */
    private static final String WINDOW_SUPPORT_KEY = "windowSupport";

    /**
     * Sets up window dragging support with explicitly defined draggable regions.
     * <p>
     * This method creates a {@link WindowSupport} instance that enables dragging the container
     * by the specified draggable elements. When the user performs a mouse drag operation on
     * any of these elements, the entire container will move accordingly.
     * <p>
     * This method automatically cleans up any existing window support for the container
     * before setting up new support, making it safe to call multiple times.
     *
     * @param container The main container pane that represents the window to be made draggable
     * @param draggableElements Elements (such as headers or titlebars) that should serve as
     *                          drag handles for the window. The user must click and drag these
     *                          elements to move the window.
     * @return A subscription that can be used to clean up resources when the window is no longer
     *         needed. Call {@code unsubscribe()} on this object to remove all dragging behavior.
     * @throws IllegalArgumentException if container is null
     *
     * @see #cleanupDraggableWindow(Pane) An alternative cleanup method when the subscription is not retained
     * @see #addDraggableRegion(Pane, Node) For adding additional draggable regions after setup
     */
    public static Subscription setupDraggableWindow(Pane container, Node... draggableElements) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null");
        }

        if (draggableElements == null || draggableElements.length == 0) {
            LOG.warn("No draggable elements specified for container: {}", container);
        }

        // Clean up any existing support for this container
        cleanupExistingSupport(container);

        WindowSupport support = new WindowSupport(container, draggableElements);
        container.getProperties().put(WINDOW_SUPPORT_KEY, support);

        // Return a subscription that cleans up when unsubscribed
        return () -> {
            support.removeSupport();
            container.getProperties().remove(WINDOW_SUPPORT_KEY);
        };
    }

    /**
     * Adds a new draggable region to a window after initial setup.
     * <p>
     * This method allows incrementally adding more drag handles to an existing
     * draggable window. This is useful when the window structure changes dynamically
     * or when building composite UIs where drag handles are added as components are
     * created.
     * <p>
     * The container must have been previously configured with {@link #setupDraggableWindow}.
     *
     * @param container The window container that has already been set up with draggable support
     * @param draggableNode The node to add as an additional draggable region
     * @return A subscription that can be used to remove just this draggable region when needed,
     *         or {@link Subscription#EMPTY} if the operation couldn't be completed
     *
     * @see #setupDraggableWindow(Pane, Node...) To initialize draggable support
     * @see #removeDraggableRegion(Pane, Node) To remove a specific draggable region
     */
    public static Subscription addDraggableRegion(Pane container, Node draggableNode) {
        if (container == null) {
            LOG.warn("Cannot add draggable region - container is null");
            return Subscription.EMPTY;
        }

        if (draggableNode == null) {
            LOG.warn("Cannot add draggable region - node is null");
            return Subscription.EMPTY;
        }

        WindowSupport support = getWindowSupport(container);
        if (support != null) {
            return support.addDraggableRegion(draggableNode);
        } else {
            LOG.warn("Cannot add draggable region - no window support found for container");
            return Subscription.EMPTY;
        }
    }

    /**
     * Removes a node from the list of draggable regions.
     * <p>
     * This method removes a specific node from being a drag handle for the container,
     * while maintaining any other existing drag handles. This is useful when UI elements
     * that previously acted as drag handles need to be repurposed or removed.
     *
     * @param container The window container that has draggable support
     * @param draggableNode The node to remove from the draggable regions
     *
     * @see #addDraggableRegion(Pane, Node) To add a specific draggable region
     * @see #cleanupDraggableWindow(Pane) To remove all draggable behavior
     */
    public static void removeDraggableRegion(Pane container, Node draggableNode) {
        if (container == null) {
            LOG.warn("Cannot remove draggable region - container is null");
            return;
        }

        if (draggableNode == null) {
            LOG.warn("Cannot remove draggable region - node is null");
            return;
        }

        WindowSupport support = getWindowSupport(container);
        if (support != null) {
            support.removeDraggableRegion(draggableNode);
        } else {
            LOG.warn("Cannot remove draggable region - no window support found for container");
        }
    }

    /**
     * Removes all window dragging support and cleans up resources.
     * <p>
     * This method should be called when the window is being closed or disposed
     * to ensure proper cleanup of event handlers and other resources. It removes
     * all draggable behavior from the container.
     * <p>
     * This method is an alternative to calling {@code unsubscribe()} on the
     * {@link Subscription} returned by {@link #setupDraggableWindow}, and is
     * useful when the subscription object wasn't retained.
     *
     * @param container The container to remove draggable support from
     *
     * @see #setupDraggableWindow(Pane, Node...) To set up draggable support initially
     */
    public static void cleanupDraggableWindow(Pane container) {
        if (container == null) {
            LOG.debug("Cannot cleanup - container is null");
            return;
        }

        cleanupExistingSupport(container);
    }

    /**
     * Retrieves the WindowSupport instance from the container's properties.
     *
     * @param container The container to get support for
     * @return The WindowSupport instance, or null if not found or if container is null
     */
    public static WindowSupport getWindowSupport(Pane container) {
        if (container == null) return null;

        Object support = container.getProperties().get(WINDOW_SUPPORT_KEY);
        return (support instanceof WindowSupport windowSupport) ? windowSupport : null;
    }

    /**
     * Cleans up any existing window support for the container.
     *
     * @param container The container to clean up
     */
    private static void cleanupExistingSupport(Pane container) {
        if (container == null) return;

        WindowSupport support = getWindowSupport(container);
        if (support != null) {
            support.removeSupport();
            container.getProperties().remove(WINDOW_SUPPORT_KEY);
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DraggableSupport() {
        // Utility class should not be instantiated
    }
}