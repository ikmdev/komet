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

import java.util.Arrays;
import java.util.Objects;

import static dev.ikm.komet.kview.fxutils.window.WindowSupport.setupWindowSupport;

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
 * Subscription windowSub = DraggableSupport.addDraggableNodes(window, header);
 *
 * // Later, when the window is no longer needed:
 * windowSub.unsubscribe();
 * }</pre>
 *
 * @see WindowSupport The underlying implementation that manages the dragging behavior
 * @see Subscription The resource management pattern used for cleanup
 */
public interface DraggableSupport {

    Logger LOG = LoggerFactory.getLogger(DraggableSupport.class);

    /**
     * Adds one or more draggable nodes to a container.
     * <p>
     * Creates WindowSupport if it doesn't exist. This method can be used for both
     * initial setup and incremental additions of drag handles when the window
     * structure changes dynamically.
     * <p>
     * Each draggable node allows users to drag the entire window by clicking
     * and dragging that specific element. If a node is already draggable, its
     * existing handlers will be replaced with new ones.
     *
     * @param container      the container pane; must not be null
     * @param draggableNodes one or more nodes that should act as draggable handles;
     *                       null nodes in the array are ignored
     * @return a subscription to remove these specific draggable nodes;
     * returns {@link Subscription#EMPTY} if no valid nodes were provided
     * @throws IllegalArgumentException if the container is null
     * @see #removeDraggableNodes(Pane, Node...) To remove draggable regions
     */
    static Subscription addDraggableNodes(Pane container, Node... draggableNodes) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null");
        }

        if (draggableNodes == null || draggableNodes.length == 0) {
            LOG.warn("No draggable elements specified for container");
            return Subscription.EMPTY;
        }

        // Get or create WindowSupport
        final WindowSupport support = setupWindowSupport(container);

        // Add each draggable node to the support
        final Subscription[] subscriptions = Arrays.stream(draggableNodes)
                .filter(Objects::nonNull)
                .map(support::addDraggableNode)
                .toArray(Subscription[]::new);

        if (subscriptions.length == 0) {
            LOG.warn("All provided draggable nodes were null for container");
            return Subscription.EMPTY;
        }

        return Subscription.combine(subscriptions);
    }

    /**
     * Removes specific draggable nodes from a container.
     * <p>
     * Safely removes the specified nodes from acting as drag handles for the window.
     * Null nodes are ignored. If the container has no WindowSupport, no action is taken.
     *
     * @param container      The container pane must not be null
     * @param draggableNodes The nodes to remove; null nodes are ignored
     * @throws IllegalArgumentException if the container is null
     * @see #addDraggableNodes(Pane, Node...) To add draggable regions
     */
    static void removeDraggableNodes(Pane container, Node... draggableNodes) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null");
        }

        if (draggableNodes == null || draggableNodes.length == 0) {
            LOG.warn("No draggable elements specified for removal from container");
            return;
        }

        // Get or create WindowSupport
        final WindowSupport support = setupWindowSupport(container);

        final long removedCount = Arrays.stream(draggableNodes)
                .filter(Objects::nonNull)
                .mapToLong(node -> support.removeDraggableNode(node) ? 1 : 0)
                .sum();

        LOG.debug("Removed {} draggable nodes from {} container", removedCount, container.getClass().getSimpleName());
    }
}