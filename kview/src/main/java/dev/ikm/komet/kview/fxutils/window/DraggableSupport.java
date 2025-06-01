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

import static dev.ikm.komet.kview.fxutils.window.SubscriptionUtils.safeUnsubscribe;
import static dev.ikm.komet.kview.fxutils.window.WindowSupport.WINDOW_SUPPORT_KEY;

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
     * and dragging that specific element.
     *
     * @param container      The container pane must not be null
     * @param draggableNodes One or more nodes that should act as draggable handles
     * @return A subscription to remove these specific draggable nodes, or
     * {@link Subscription#EMPTY} if the operation couldn't be completed
     * @throws IllegalArgumentException if the container is null
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
        WindowSupport support = getOrCreateWindowSupport(container);

        // Add all nodes and collect their subscriptions
        Subscription[] subscriptions = Arrays.stream(draggableNodes)
                .filter(Objects::nonNull)
                .map(support::addDraggableNode)
                .toArray(Subscription[]::new);

        return subscriptions.length > 0 ? Subscription.combine(subscriptions) : Subscription.EMPTY;
    }

    /**
     * Removes draggable nodes by safely unsubscribing their subscriptions.
     * <p>
     * This method provides a domain-specific way to clean up draggable nodes,
     * handling null subscriptions gracefully and returning null for convenient
     * assignment back to subscription fields.
     *
     * @param subscriptions The draggable node subscriptions to remove
     * @param <T>           Generic type for assignment convenience
     * @return Always null (for convenient assignment)
     */
    static <T> T removeDraggableNodes(Subscription... subscriptions) {
        return safeUnsubscribe(subscriptions);
    }

    /**
     * Gets existing WindowSupport for a container or creates a new one if it doesn't exist.
     *
     * @param container The container to get or create WindowSupport for must not be null
     * @return The WindowSupport instance, never null
     * @throws IllegalArgumentException if the container is null
     */
    static WindowSupport getOrCreateWindowSupport(Pane container) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null");
        }

        WindowSupport support = container.getProperties().get(WINDOW_SUPPORT_KEY) instanceof WindowSupport ws ? ws : null;

        if (support == null) {
            support = new WindowSupport(container);
            container.getProperties().put(WINDOW_SUPPORT_KEY, support);
        }

        return support;
    }
}