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

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.function.BiConsumer;

import static dev.ikm.komet.kview.fxutils.window.CursorMappings.ResizeDirection;
import static dev.ikm.komet.kview.fxutils.window.CursorMappings.createCursorSubscription;
import static dev.ikm.komet.kview.fxutils.window.CursorMappings.getDirection;
import static dev.ikm.komet.kview.fxutils.window.CursorMappings.handleResize;
import static dev.ikm.komet.kview.fxutils.window.SubscriptionUtils.createConsumerSubscription;
import static dev.ikm.komet.kview.fxutils.window.SubscriptionUtils.createContextEventSubscription;
import static dev.ikm.komet.kview.fxutils.window.SubscriptionUtils.createListSubscription;
import static dev.ikm.komet.kview.fxutils.window.SubscriptionUtils.createMultiPropertySubscription;
import static dev.ikm.komet.kview.fxutils.window.SubscriptionUtils.createPropertySubscription;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static javafx.scene.paint.Color.TRANSPARENT;

/**
 * Provides window-like behaviors for JavaFX Panes, including resizing, dragging, and visual feedback.
 * <p>
 * This class transforms a regular JavaFX {@link Pane} into a window-like component with the
 * following capabilities:
 * <ul>
 *   <li><b>Resizing</b>: Allows resizing from any edge or corner with appropriate cursor feedback</li>
 *   <li><b>Dragging</b>: Enables moving the window by dragging designated regions (like a title bar)</li>
 *   <li><b>Visual Feedback</b>: Shows highlights when hovering and during interactions</li>
 *   <li><b>Parent Bounds Constraints</b>: Optionally constrains the window within its parent bounds</li>
 *   <li><b>Auto-height</b>: Optional configuration for automatic height adjustment based on content</li>
 * </ul>
 * <p>
 * The implementation follows modern JavaFX patterns for resource management:
 * <ul>
 *   <li>Uses the {@link Subscription} pattern for clean, deterministic cleanup of resources</li>
 *   <li>Categorizes subscriptions for better organization and lifecycle management</li>
 * </ul>
 *
 * @see DraggableSupport For a higher-level utility API
 * @see ResizeHandle For the individual resize handles
 * @see Subscription For the resource management pattern
 */
public class WindowSupport {

    private static final Logger LOG = LoggerFactory.getLogger(WindowSupport.class);

    /**
     * The property key used to store WindowSupport instance in container properties.
     */
    public static final String WINDOW_SUPPORT_KEY = "windowSupport";

    /**
     * When true, renders resize handles with visible colors for debugging UI interactions.
     * Set to false for production where handles should be invisible to users.
     */
    private static final boolean DEBUG_MODE = false;

    // Constants
    private static final double MIN_DIMENSION = 1.0;
    private static final Color HIGHLIGHT_COLOR = Color.web("#67BA3FFF");
    private static final double BASE_UNIT = 3.0;
    private static final double HIGHLIGHT_WIDTH = BASE_UNIT;
    private static final double HIGHLIGHT_PADDING = 4 * HIGHLIGHT_WIDTH;
    private static final double RESIZE_HIT_AREA = 4 * HIGHLIGHT_WIDTH;
    private static final double CORNER_HIT_AREA = 3 * HIGHLIGHT_WIDTH;

    /**
     * Represents the current interaction state of the window.
     * <p>
     * The window can be in one of three states:
     * <ul>
     *   <li>{@code IDLE}: No active interaction, the window is at rest</li>
     *   <li>{@code DRAGGING}: The window is being moved via a drag operation</li>
     *   <li>{@code RESIZING}: The window is being resized via an edge or corner handle</li>
     * </ul>
     * The current state affects how mouse events are processed and which visual feedback is shown.
     */
    public enum WindowState {IDLE, DRAGGING, RESIZING}

    private WindowState windowState = WindowState.IDLE;

    // Window position properties
    private final ObjectProperty<Point2D> anchorWindowPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<Point2D> anchorMousePosition = new SimpleObjectProperty<>();
    private final DoubleProperty windowPositionX = new SimpleDoubleProperty();
    private final DoubleProperty windowPositionY = new SimpleDoubleProperty();

    // Window size properties
    private final DoubleProperty anchorWidth = new SimpleDoubleProperty();
    private final DoubleProperty anchorHeight = new SimpleDoubleProperty();
    private final DoubleProperty currentWidth = new SimpleDoubleProperty();
    private final DoubleProperty currentHeight = new SimpleDoubleProperty();

    // Resize handlers
    private BiConsumer<MouseEvent, WindowSupport> resizeMousePressHandler;
    private BiConsumer<MouseEvent, WindowSupport> resizeMouseDragHandler;
    private BiConsumer<MouseEvent, WindowSupport> resizeMouseReleaseHandler;

    // Resize direction tracking
    private final ObjectProperty<ResizeDirection> resizeDirection =
            new SimpleObjectProperty<>(ResizeDirection.NONE);

    // Visual elements
    private Rectangle outlineRect;

    // Core components
    private final Pane pane;
    private final MutableList<Node> draggableNodes = Lists.mutable.empty();

    // Resize handles
    private MutableMap<ResizeDirection, ResizeHandle> resizeHandles;

    // Drag position tracking
    private final SimpleObjectProperty<Point2D> dragAnchorPoint = new SimpleObjectProperty<>(new Point2D(0, 0));
    private final SimpleObjectProperty<Point2D> dragPreviousLocation = new SimpleObjectProperty<>(new Point2D(0, 0));

    // Drag handlers
    private BiConsumer<MouseEvent, WindowSupport> dragMousePressHandler;
    private BiConsumer<MouseEvent, WindowSupport> dragMouseDragHandler;
    private BiConsumer<MouseEvent, WindowSupport> dragMouseReleaseHandler;

    // Subscription management
    private final MutableMap<Node, Subscription> nodeSubscriptions = Maps.mutable.empty();
    private final MutableMap<String, Subscription> categorySubscriptions = Maps.mutable.empty();

    /**
     * Creates a new WindowSupport instance that transforms the specified pane into a window-like component.
     * <p>
     * This constructor initializes all necessary parts for window functionality, including
     * resize handles for all edges and corners, visual feedback elements, event handlers for
     * resize operations, and property bindings for synchronized layout updates.
     * <p>
     * After construction, the pane will support resizing with visual feedback and be ready for
     * dragging behavior addition via {@link DraggableSupport}.
     * <p>
     * Note: This constructor is private. Use {@link #setupWindowSupport(Pane)} to get WindowSupport instances.
     *
     * @param container The pane to transform into a window-like component; must not be null
     * @throws IllegalArgumentException if the container is null
     * @see #setupWindowSupport(Pane) for the preferred way to create WindowSupport instances
     * @see DraggableSupport#addDraggableNodes(Pane, Node...) for adding drag behavior
     */
    private WindowSupport(final Pane container) {
        this.pane = container;

        initializePrimaryFields();
        initializeDefaultHandlersAndPaneEvents();
        initializeVisualElements(container);
        initializeResizeHandles();
        bindPaneProperties();
        initializeEventHandlers();
    }

    /**
     * Initializes primary fields of the WindowSupport instance.
     */
    private void initializePrimaryFields() {
        this.outlineRect = new Rectangle();

        // Resize handle management
        ResizeHandleFactory handleFactory = new ResizeHandleFactory(RESIZE_HIT_AREA, CORNER_HIT_AREA, DEBUG_MODE);
        this.resizeHandles = handleFactory.createAllHandles();

        // Initialize resize values with current dimensions
        currentWidth.set(Math.max(MIN_DIMENSION, pane.getWidth()));
        currentHeight.set(Math.max(MIN_DIMENSION, pane.getHeight()));
    }

    /**
     * Initializes default event handlers for drag/resize operations and basic pane interactions.
     */
    private void initializeDefaultHandlersAndPaneEvents() {
        // Default handlers for resizing
        this.resizeMousePressHandler = this::handleResizeMousePressed;
        this.resizeMouseDragHandler = this::handleResizeMouseDragged;
        this.resizeMouseReleaseHandler = this::handleResizeMouseReleased;

        // Initialize drag handlers to defaults
        this.dragMousePressHandler = (mouseEvent, support) -> {
            support.dragAnchorPoint.set(new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY()));
            support.dragPreviousLocation.set(new Point2D(pane.getLayoutX(), pane.getLayoutY()));
        };

        this.dragMouseDragHandler = (mouseEvent, support) -> {
            if (support.dragAnchorPoint.isNotNull().get() && support.dragPreviousLocation.isNotNull().get()) {
                pane.setLayoutX(support.dragPreviousLocation.get().getX() + mouseEvent.getSceneX() - support.dragAnchorPoint.get().getX());
                pane.setLayoutY(support.dragPreviousLocation.get().getY() + mouseEvent.getSceneY() - support.dragAnchorPoint.get().getY());
            }
        };

        this.dragMouseReleaseHandler = (_, support) ->
                support.dragPreviousLocation.set(new Point2D(pane.getLayoutX(), pane.getLayoutY()));
    }

    /**
     * Sets up position constraints that keep the window within its parent's bounds.
     * <p>
     * When position constraints are active, the window will automatically adjust its position
     * to stay completely visible within its parent container, even after resize operations
     * or when the parent container changes size.
     * <p>
     * An optional callback can be provided to perform additional actions after constraints
     * are applied, such as saving the window state.
     *
     * @param afterConstraintCallback Optional callback to execute after constraints are applied,
     *                                or null if no additional action is needed
     * @return A subscription that can be used to remove the constraints when no longer needed
     * @see #constrainToParentBounds(double, double) For the underlying constraint implementation
     */
    public Subscription setupPositionConstraints(InvalidationListener afterConstraintCallback) {
        // Create constraints listener
        final InvalidationListener constraintsListener = obs -> {
            // Constrain position
            final double parentWidth = pane.getParent() != null ?
                    pane.getParent().getLayoutBounds().getWidth() :
                    Double.MAX_VALUE;

            final double parentHeight = pane.getParent() != null ?
                    pane.getParent().getLayoutBounds().getHeight() :
                    Double.MAX_VALUE;

            constrainToParentBounds(parentWidth, parentHeight);

            // Call the after-constraint callback if provided
            if (afterConstraintCallback != null) {
                afterConstraintCallback.invalidated(obs);
            }
        };

        // Create subscriptions for all properties
        final Subscription combined = createMultiPropertySubscription(
                constraintsListener,
                pane.layoutXProperty(),
                pane.layoutYProperty(),
                pane.widthProperty(),
                pane.heightProperty());

        // Add to the category subscriptions for lifecycle management
        addSubscription("constraints", combined);

        // Return subscription for cleanup
        return combined;
    }

    /**
     * Initializes the visual elements for the window.
     *
     * @param container The pane to which visual elements will be added
     */
    private void initializeVisualElements(Pane container) {
        // Configure the outline rectangle for highlighting
        outlineRect.setStroke(TRANSPARENT);
        outlineRect.setStrokeWidth(HIGHLIGHT_WIDTH);
        outlineRect.setFill(TRANSPARENT);
        outlineRect.setMouseTransparent(true);

        // Add outline rectangle to parent
        container.getChildren().add(outlineRect);

        // Apply minimum width to ensure highlight borders have enough space
        final double contentMinWidth = Math.max(0, container.minWidth(USE_COMPUTED_SIZE)) + HIGHLIGHT_PADDING;
        container.setMinWidth(contentMinWidth);
    }

    /**
     * Initializes the resize handles for all directions.
     */
    private void initializeResizeHandles() {
        // Add all handles to the pane in the correct Z-order
        resizeHandles.values().stream()
                .sorted(Comparator.comparingInt(ResizeHandle::getZOrder))
                .forEach(handle -> pane.getChildren().add(handle.getNode()));

        // Ensure the outline rectangle is at the front for clear visibility
        outlineRect.toFront();
    }

    /**
     * Sets up all event handlers for the window.
     */
    private void initializeEventHandlers() {
        try {
            addSubscription("handlers", createWindowBaseSubscriptions());
            addLayoutListener();
            addResizeHandlers();
        } catch (Exception e) {
            LOG.error("Error setting up event handlers", e);
        }
    }

    /**
     * Creates base window subscriptions for mouse events.
     *
     * @return A combined subscription for base window events
     */
    private Subscription createWindowBaseSubscriptions() {
        return Subscription.combine(
                createConsumerSubscription(pane, MouseEvent.MOUSE_PRESSED, _ -> pane.toFront()),
                createConsumerSubscription(pane, MouseEvent.MOUSE_ENTERED, _ -> outlineRect.setStroke(HIGHLIGHT_COLOR)),
                createConsumerSubscription(pane, MouseEvent.MOUSE_EXITED, _ -> {
                    if (windowState == WindowState.IDLE) {
                        outlineRect.setStroke(TRANSPARENT);
                    }
                })
        );
    }

    /**
     * Adds a node as a draggable region for the window.
     * <p>
     * This method registers mouse event handlers for the specified node to allow
     * dragging the window by clicking and dragging that specific element. If a node
     * is already draggable, its existing handlers will be replaced with new ones.
     *
     * @param draggableNode The node to make draggable; must not be null
     * @return A subscription that can be used to remove this draggable node;
     * returns {@link Subscription#EMPTY} if the node is null
     */
    public Subscription addDraggableNode(Node draggableNode) {
        if (draggableNode == null) {
            LOG.warn("Cannot add null as draggable region");
            return Subscription.EMPTY;
        }

        // Unregister existing handlers to prevent duplicates
        if (nodeSubscriptions.containsKey(draggableNode)) {
            Subscription oldSubscription = nodeSubscriptions.get(draggableNode);
            if (oldSubscription != null) {
                oldSubscription.unsubscribe();
            }
        }

        // Add to the tracked nodes list if not already present
        if (!draggableNodes.contains(draggableNode)) {
            draggableNodes.add(draggableNode);
        }

        // Create and register mouse event handlers using the utility methods
        Subscription dragSubscription = Subscription.combine(
                createContextEventSubscription(draggableNode, MouseEvent.MOUSE_PRESSED, this,
                        (event, support) -> support.handlePositionWindowMousePressed(event)),
                createContextEventSubscription(draggableNode, MouseEvent.MOUSE_DRAGGED, this,
                        (event, support) -> support.handlePositionWindowMouseDragged(event)),
                createContextEventSubscription(draggableNode, MouseEvent.MOUSE_RELEASED, this,
                        (event, support) -> support.handlePositionWindowMouseReleased(event))
        );

        // Store for later access and cleanup
        nodeSubscriptions.put(draggableNode, dragSubscription);

        // Return cleanup subscription
        return () -> removeDraggableNode(draggableNode);
    }

    /**
     * Removes a draggable node from the window.
     * <p>
     * This method unregisters the mouse event handlers for the specified node and
     * removes it from the list of draggable nodes. If the node was not previously
     * registered, no action is taken.
     *
     * @param draggableNode The node to remove; must not be null
     * @return true if the node was successfully removed, false if it was not found
     */
    public boolean removeDraggableNode(Node draggableNode) {
        if (draggableNode == null) {
            return false;
        }

        // Cleanup subscription - this handles the event handlers
        final Subscription eventHandlerSubscription = nodeSubscriptions.remove(draggableNode);
        if (eventHandlerSubscription != null) {
            try {
                eventHandlerSubscription.unsubscribe();
            } catch (Exception e) {
                LOG.error("Error during draggable node cleanup", e);
            }
        }

        // Remove from a tracking list
        final boolean wasRemoved = draggableNodes.remove(draggableNode);

        if (wasRemoved) {
            LOG.debug("Removed draggable node: {}", draggableNode.getClass().getSimpleName());
        }

        return wasRemoved;
    }

    /**
     * Adds a listener to update visual elements when layout bounds change.
     */
    private void addLayoutListener() {
        // Create and add a subscription for the layout bounds property
        InvalidationListener layoutListener = _ -> updateResizeHandlePositions();

        Subscription layoutSubscription = createPropertySubscription(
                pane.layoutBoundsProperty(), layoutListener);

        addSubscription("layout", layoutSubscription);
    }

    /**
     * Adds resize handlers to all handles.
     */
    private void addResizeHandlers() {
        MutableList<Subscription> handleSubscriptions = Lists.mutable.empty();

        // Add handlers for all resize handles
        for (ResizeHandle handle : resizeHandles.values()) {
            handleSubscriptions.add(setupResizeHandlers(handle.getNode(), handle.getDirection()));
        }

        // Combine all subscriptions
        addSubscription("resize", Subscription.combine(handleSubscriptions.toArray(Subscription[]::new)));
    }

    /**
     * Creates a subscription for resize event handlers on a node.
     *
     * @param node      The node to attach resize handlers to
     * @param direction The resize direction for the node
     * @return A subscription that unregisters the handlers when unsubscribed
     */
    private Subscription setupResizeHandlers(Node node, ResizeDirection direction) {
        if (node == null) return Subscription.EMPTY;

        // Create a cursor change subscription
        Subscription cursorSubscription = createCursorSubscription(node, direction);

        // Create a resize handler subscription
        Subscription resizeHandlerSubscription = createResizeHandlerSubscription(node);

        return Subscription.combine(cursorSubscription, resizeHandlerSubscription);
    }

    /**
     * Creates a subscription for resize event handlers on a node.
     *
     * @param node The node to attach resize handlers to
     * @return A subscription that unregisters the handlers when unsubscribed
     */
    private Subscription createResizeHandlerSubscription(Node node) {
        return Subscription.combine(
                createContextEventSubscription(node, MouseEvent.MOUSE_PRESSED, this,
                        (e, support) -> {
                            if (resizeMousePressHandler != null) {
                                resizeMousePressHandler.accept(e, support);
                            } else {
                                support.handleResizeMousePressed(e, support);
                            }
                        }
                ),

                createContextEventSubscription(node, MouseEvent.MOUSE_DRAGGED, this,
                        (e, support) -> {
                            if (windowState == WindowState.RESIZING) {
                                if (resizeMouseDragHandler != null) {
                                    resizeMouseDragHandler.accept(e, support);
                                } else {
                                    support.handleResizeMouseDragged(e, support);
                                }
                            }
                        }
                ),

                createContextEventSubscription(node, MouseEvent.MOUSE_RELEASED, this,
                        (e, support) -> {
                            if (resizeMouseReleaseHandler != null) {
                                resizeMouseReleaseHandler.accept(e, support);
                            } else {
                                support.handleResizeMouseReleased(e, support);
                            }
                        }
                )
        );
    }

    /**
     * Binds window properties to synchronize the window state with visual representation.
     */
    private void bindPaneProperties() {
        // Set up a unified property listener for all layout updates
        InvalidationListener updateLayout = _ -> {
            pane.setLayoutX(windowPositionX.get());
            pane.setLayoutY(windowPositionY.get());
            pane.setPrefWidth(currentWidth.get());
            pane.setPrefHeight(currentHeight.get());
        };

        // Create subscriptions for all properties
        MutableList<Subscription> propertySubscriptions = Lists.mutable.empty();

        propertySubscriptions.add(createPropertySubscription(windowPositionX, updateLayout));
        propertySubscriptions.add(createPropertySubscription(windowPositionY, updateLayout));
        propertySubscriptions.add(createPropertySubscription(currentWidth, updateLayout));
        propertySubscriptions.add(createPropertySubscription(currentHeight, updateLayout));
        // Create a subscription for cleanup
        addSubscription("properties", Subscription.combine(propertySubscriptions.toArray(Subscription[]::new)));
    }

    /**
     * Updates the positions of all resize handles based on the current window size.
     */
    private void updateResizeHandlePositions() {
        // Get current layout bounds ensuring minimum dimensions
        Bounds layoutBounds = pane.getLayoutBounds();
        double width = Math.max(MIN_DIMENSION, layoutBounds.getWidth());
        double height = Math.max(MIN_DIMENSION, layoutBounds.getHeight());

        // Position visible highlight rectangle
        updateOutlineRectangle(width, height);

        // Update all resize handles with a single call
        resizeHandles.values().forEach(handle -> handle.updatePosition(width, height));
    }

    /**
     * Updates the outline rectangle to match the current window size.
     *
     * @param width  The current width of the window
     * @param height The current height of the window
     */
    private void updateOutlineRectangle(double width, double height) {
        outlineRect.setX(0);
        outlineRect.setY(0);
        outlineRect.setWidth(width);
        outlineRect.setHeight(height);
    }

    /**
     * Handles mouse press events for window positioning.
     *
     * @param mouseEvent The mouse event that initiated the drag
     */
    public void handlePositionWindowMousePressed(MouseEvent mouseEvent) {
        if (mouseEvent == null) {
            return;
        }

        if (getDragMousePressHandler() != null) {
            getDragMousePressHandler().accept(mouseEvent, this);
        }

        windowState = WindowState.DRAGGING;
    }

    /**
     * Handles mouse drag events for window positioning.
     *
     * @param mouseEvent The mouse event containing the current drag position
     */
    public void handlePositionWindowMouseDragged(MouseEvent mouseEvent) {
        if (mouseEvent == null || windowState != WindowState.DRAGGING) {
            return;
        }

        if (getDragMouseDragHandler() != null) {
            getDragMouseDragHandler().accept(mouseEvent, this);
        }
    }

    /**
     * Handles mouse release events for window positioning.
     *
     * @param mouseEvent The mouse event marking the end of the drag operation
     */
    public void handlePositionWindowMouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent == null || windowState != WindowState.DRAGGING) {
            return;
        }

        if (getDragMouseReleaseHandler() != null) {
            getDragMouseReleaseHandler().accept(mouseEvent, this);
        }

        windowState = WindowState.IDLE;
    }

    /**
     * Handles mouse press for resize operations.
     *
     * @param mouseEvent    The mouse event containing the initial press position
     * @param windowSupport The WindowSupport instance (this)
     */
    public void handleResizeMousePressed(MouseEvent mouseEvent, WindowSupport windowSupport) {
        try {
            // Store initial position and dimensions for the resize operation
            initializeResizeOperation(mouseEvent);

            // Update state to indicate we're now in a resize operation
            windowState = WindowState.RESIZING;

            // Show the highlight border to provide visual feedback during resize
            outlineRect.setStroke(HIGHLIGHT_COLOR);
        } catch (Exception e) {
            LOG.error("Error in mouse press handler", e);
            resetWindowState();
        }
    }

    /**
     * Initializes a resize operation by storing initial position and size values.
     *
     * @param mouseEvent The mouse event that initiated the resize
     */
    private void initializeResizeOperation(MouseEvent mouseEvent) {
        // Store the initial window position
        Point2D windowXY = new Point2D(pane.getLayoutX(), pane.getLayoutY());
        this.anchorWindowPosition.set(windowXY);
        this.windowPositionX.set(windowXY.getX());
        this.windowPositionY.set(windowXY.getY());

        // Store the initial mouse position
        Point2D mouseDesktopXY = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        this.anchorMousePosition.set(mouseDesktopXY);

        // Store the initial window size with minimum safeguards
        double windowWidth = Math.max(MIN_DIMENSION, pane.getWidth());
        double windowHeight = Math.max(MIN_DIMENSION, pane.getHeight());

        this.anchorWidth.set(windowWidth);
        this.anchorHeight.set(windowHeight);

        // Initialize resize values with current dimensions
        this.currentWidth.set(windowWidth);
        this.currentHeight.set(windowHeight);

        // Set the resize direction based on which handle was grabbed
        setResizeDirection(mouseEvent);
    }

    /**
     * Handles mouse drag for resize operations.
     *
     * @param mouseEvent    The mouse event containing the current drag position
     * @param windowSupport The WindowSupport instance (this)
     */
    public void handleResizeMouseDragged(MouseEvent mouseEvent, WindowSupport windowSupport) {
        if (windowState != WindowState.RESIZING || this.resizeDirection.get() == null) {
            return;
        }
        handleResizeByDirection(mouseEvent);
    }

    /**
     * Handles mouse release for resize operations.
     *
     * @param mouseEvent    The mouse event marking the end of the resize
     * @param windowSupport The WindowSupport instance (this)
     */
    public void handleResizeMouseReleased(MouseEvent mouseEvent, WindowSupport windowSupport) {
        if (windowState == WindowState.RESIZING) {
            resetWindowState();
            dragPreviousLocation.set(new Point2D(pane.getLayoutX(), pane.getLayoutY()));
        }
    }

    /**
     * Sets the resize direction based on the mouse event source.
     *
     * @param mouseEvent The mouse event that initiated the resize
     */
    private void setResizeDirection(MouseEvent mouseEvent) {
        if (mouseEvent == null) {
            return;
        }

        Node shapeNode = (Node) mouseEvent.getSource();
        setResizeDirection(getResizeDirectionFromNode(shapeNode));
    }

    /**
     * Sets the current resize direction.
     *
     * @param direction The resize direction to set
     */
    private void setResizeDirection(ResizeDirection direction) {
        this.resizeDirection.set(direction);
    }

    /**
     * Gets resize the direction from a node's userData.
     *
     * @param node The node to check for a direction
     * @return The resize direction associated with the node's cursor, or NONE if not found
     */
    private ResizeDirection getResizeDirectionFromNode(Node node) {
        if (node == null || !(node.getUserData() instanceof Cursor cursor)) {
            return ResizeDirection.NONE;
        }

        return getDirection(cursor);
    }

    /**
     * Handles the resize operation based on the current resize direction.
     *
     * @param mouseEvent The mouse event containing the current drag position
     */
    private void handleResizeByDirection(MouseEvent mouseEvent) {
        if (mouseEvent == null) {
            return;
        }

        ResizeDirection direction = this.resizeDirection.get();
        if (direction == null) {
            return;
        }

        handleResize(direction, this, mouseEvent);
    }

    /**
     * Gets the width of the parent container.
     *
     * @return The width of the parent, or MAX_VALUE if no parent is available
     */
    private double getParentWidth() {
        if (pane.getParent() != null) {
            return pane.getParent().getLayoutBounds().getWidth();
        }
        return Double.MAX_VALUE;
    }

    /**
     * Gets the height of the parent container.
     *
     * @return The height of the parent, or MAX_VALUE if no parent is available
     */
    private double getParentHeight() {
        if (pane.getParent() != null) {
            return pane.getParent().getLayoutBounds().getHeight();
        }
        return Double.MAX_VALUE;
    }

    /**
     * Handle for resizing from the north (top) edge.
     * <p>
     * This method implements the resize logic for the north edge or north corners,
     * adjusting both the window's Y position and height while respecting parent bounds
     * and minimum/maximum size constraints.
     *
     * @param mouseEvent The mouse event containing the current cursor position
     */
    void resizeNorth(MouseEvent mouseEvent) {
        final Point2D anchorPaneXY = this.anchorWindowPosition.get();
        if (anchorPaneXY == null) {
            return;
        }

        // Convert mouse Y-coordinate from local (handle) to the parent container's coordinate system.
        final Point2D desktopPoint = pane.localToParent(mouseEvent.getX(), mouseEvent.getY());
        final double currentMouseYInParent = desktopPoint.getY();
        final double initialPaneYInParent = anchorPaneXY.getY();

        // Calculate distance moved from the initial mouse press position.
        // A positive distance means the mouse moved upwards relative to the initial pane top.
        double distance = initialPaneYInParent - currentMouseYInParent;

        // Calculate new potential Y position and height.
        // Moving upwards decreases Y and increases height.
        double newY = initialPaneYInParent - distance;
        double newHeight = this.anchorHeight.get() + distance;

        // Boundary check: Adjust if the new Y position would be outside the parent's top boundary (less than 0).
        if (newY < 0) {
            // The pane hits the top of the parent.
            // The distance it can move is limited by its initial Y position.
            distance = initialPaneYInParent;
            newY = 0; // Pin to the top boundary.
            newHeight = this.anchorHeight.get() + distance; // Recalculate height based on pinned position.
        }

        // Apply the new height and adjusted position, respecting min/max constraints.
        final double minHeight = Math.max(MIN_DIMENSION, pane.minHeight(pane.getWidth()));
        final double maxHeight = pane.getMaxHeight();

        if (isValidDimension(newHeight, minHeight, maxHeight)) {
            windowPositionY.set(newY);
            currentHeight.set(newHeight);
        }
    }

    /**
     * Handle for resizing from the south (bottom) edge.
     * <p>
     * This method implements the resize logic for the south edge or south corners,
     * adjusting the window's height while respecting parent bounds and
     * minimum/maximum size constraints.
     *
     * @param mouseEvent The mouse event containing the current cursor position
     */
    void resizeSouth(MouseEvent mouseEvent) {
        final Point2D anchorPoint = this.anchorMousePosition.get();
        final Point2D anchorPaneXY = this.anchorWindowPosition.get();

        if (anchorPoint == null || anchorPaneXY == null) {
            return;
        }

        // Mouse Y relative to the node where the drag started (typically a resize handle).
        final double currentMouseY = mouseEvent.getY();
        // Initial mouse Y when the resize operation began.
        final double initialMouseY = anchorPoint.getY();
        final double initialPaneHeight = this.anchorHeight.get();
        final double initialPaneY = anchorPaneXY.getY();

        // Calculate new height: original height plus change in mouse Y position.
        double newHeight = initialPaneHeight + (currentMouseY - initialMouseY);

        // Boundary check: Adjust if the new height would make the pane exceed the parent's bottom boundary.
        final double parentHeight = getParentHeight();
        if (initialPaneY + newHeight > parentHeight) {
            // Pane hits the bottom of the parent.
            // Adjust height to stop exactly at the boundary.
            newHeight = parentHeight - initialPaneY;
        }

        // Apply the new height, respecting min/max constraints.
        final double minHeight = Math.max(MIN_DIMENSION, pane.minHeight(pane.getWidth()));
        final double maxHeight = pane.getMaxHeight();

        if (isValidDimension(newHeight, minHeight, maxHeight)) {
            currentHeight.set(newHeight);
        }
    }

    /**
     * Handle for resizing from the east (right) edge.
     * <p>
     * This method implements the resize logic for the east edge or east corners,
     * adjusting the window's width while respecting parent bounds and
     * minimum/maximum size constraints.
     *
     * @param mouseEvent The mouse event containing the current cursor position
     */
    void resizeEast(MouseEvent mouseEvent) {
        final Point2D anchorPoint = this.anchorMousePosition.get();
        final Point2D anchorPaneXY = this.anchorWindowPosition.get();

        if (anchorPoint == null || anchorPaneXY == null) {
            return;
        }

        // Mouse X relative to the node where the drag started.
        final double currentMouseX = mouseEvent.getX();
        // Initial mouse X when the resize operation began.
        final double initialMouseX = anchorPoint.getX();
        final double initialPaneWidth = this.anchorWidth.get();
        final double initialPaneX = anchorPaneXY.getX();

        // Calculate new width: original width plus change in mouse X position.
        double newWidth = initialPaneWidth + (currentMouseX - initialMouseX);

        // Boundary check: Adjust if the new width would make the pane exceed the parent's right boundary.
        final double parentWidth = getParentWidth();
        if (initialPaneX + newWidth > parentWidth) {
            // Pane hits the right edge of the parent.
            // Adjust width to stop exactly at the boundary.
            newWidth = parentWidth - initialPaneX;
        }

        // Apply the new width, respecting min/max constraints.
        final double minWidth = Math.max(MIN_DIMENSION, pane.minWidth(pane.getHeight()));
        final double maxWidth = pane.getMaxWidth();

        if (isValidDimension(newWidth, minWidth, maxWidth)) {
            currentWidth.set(newWidth);
        }
    }

    /**
     * Handle for resizing from the west (left) edge.
     * <p>
     * This method implements the resize logic for the west edge or west corners,
     * adjusting both the window's X position and width while respecting parent bounds
     * and minimum/maximum size constraints.
     *
     * @param mouseEvent The mouse event containing the current cursor position
     */
    void resizeWest(MouseEvent mouseEvent) {
        final Point2D anchorPaneXY = this.anchorWindowPosition.get();
        if (anchorPaneXY == null) {
            return;
        }

        // Convert mouse X-coordinate from local (handle) to the parent container's coordinate system.
        final Point2D desktopPoint = pane.localToParent(mouseEvent.getX(), mouseEvent.getY());
        final double currentMouseXInParent = desktopPoint.getX();
        final double initialPaneXInParent = anchorPaneXY.getX();

        // Calculate distance moved from the initial mouse press position.
        // A positive distance means the mouse moved leftwards relative to the initial pane's left edge.
        double distance = initialPaneXInParent - currentMouseXInParent;

        // Calculate the new potential X position and width.
        // Moving leftwards decreases X and increases width.
        double newX = initialPaneXInParent - distance;
        double newWidth = this.anchorWidth.get() + distance;

        // Boundary check: Adjust if the new X position would be outside the parent's left boundary (less than 0).
        if (newX < 0) {
            // The pane hits the left of the parent.
            // The distance it can move is limited by its initial X position.
            distance = initialPaneXInParent;
            newX = 0; // Pin to the left boundary.
            newWidth = this.anchorWidth.get() + distance; // Recalculate width based on pinned position.
        }

        // Apply the new width and adjusted position, respecting min/max constraints.
        final double minWidth = Math.max(MIN_DIMENSION, pane.minWidth(pane.getHeight()));
        final double maxWidth = pane.getMaxWidth();

        if (isValidDimension(newWidth, minWidth, maxWidth)) {
            windowPositionX.set(newX);
            currentWidth.set(newWidth);
        }
    }

    /**
     * Ensures that the window stays within the bounds of its container.
     * <p>
     * This method adjusts the window's position to keep it fully visible within
     * the specified container dimensions. It only adjusts position, not size,
     * and handles cases where the window is larger than the container by
     * aligning with the top-left.
     *
     * @param containerWidth  The width of the container that holds this window
     * @param containerHeight The height of the container that holds this window
     * @see #setupPositionConstraints(InvalidationListener) To set up automatic constraints
     */
    public void constrainToParentBounds(double containerWidth, double containerHeight) {
        final double windowWidth = pane.getWidth();
        final double windowHeight = pane.getHeight();

        if (containerWidth <= 0 || containerHeight <= 0 || windowWidth <= 0 || windowHeight <= 0) {
            return;
        }

        final double newX = pane.getLayoutX();
        final double newY = pane.getLayoutY();

        // Only constrain position, not size
        if (newX < 0) {
            pane.setLayoutX(0);
        } else if (newX + windowWidth > containerWidth) {
            pane.setLayoutX(containerWidth - windowWidth);
        }

        if (newY < 0) {
            pane.setLayoutY(0);
        } else if (newY + windowHeight > containerHeight) {
            pane.setLayoutY(containerHeight - windowHeight);
        }
    }

    /**
     * Configures the window pane to automatically adjust its height based on content.
     * <p>
     * When enabled, the window will dynamically resize its height to fit its content
     * up to the specified maximum height.
     *
     * @param useComputedHeight When true, the window's minimum height will be set to
     *                          USE_COMPUTED_SIZE, enabling automatic height calculation
     * @param maxHeight         Maximum height constraint in pixels (use negative value for no constraint)
     * @return A subscription to manage the auto-height feature (can be used to disable it)
     */
    public Subscription configureAutoHeight(boolean useComputedHeight, double maxHeight) {
        if (useComputedHeight) {
            pane.setMinHeight(USE_COMPUTED_SIZE);
        }

        if (maxHeight > 0) {
            pane.setMaxHeight(maxHeight);
        }

        // Add a layout listener to adjust height when content changes
        InvalidationListener heightListener = _ -> {
            if (pane.getMinHeight() == USE_COMPUTED_SIZE) {
                pane.requestLayout();
            }
        };

        // Create a subscription for the children listener using SubscriptionUtils
        Subscription heightSubscription = createListSubscription(pane.getChildren(), heightListener);

        // Register the subscription
        addSubscription("autoHeight", heightSubscription);

        return heightSubscription;
    }

    /**
     * Validates if the proposed dimension (width or height) is within constraints.
     *
     * @param newValue The proposed new value
     * @param minValue The minimum allowed value
     * @param maxValue The maximum allowed value (0 or negative means no maximum)
     * @return <code>true</code> if the value is valid, <code>false</code> otherwise
     */
    private boolean isValidDimension(double newValue, double minValue, double maxValue) {
        boolean aboveMinimum = newValue >= minValue;
        boolean belowMaximum = (maxValue <= 0) || (newValue <= maxValue);

        return aboveMinimum && belowMaximum;
    }

    /**
     * Resets the window state when an operation is canceled or completed.
     */
    void resetWindowState() {
        windowState = WindowState.IDLE;
        resizeDirection.set(ResizeDirection.NONE);
    }

    /**
     * Removes all window support functionality and cleans up resources.
     * <p>
     * This method:
     * <ul>
     *   <li>Unregisters all event handlers and listeners</li>
     *   <li>Removes visual elements (resize handles, highlight outline)</li>
     *   <li>Resets internal state</li>
     *   <li>Releases all resources held by this WindowSupport instance</li>
     * </ul>
     */
    public void removeSupport() {
        try {
            // Unsubscribe node-based subscriptions
            Lists.immutable.ofAll(nodeSubscriptions.values()).forEach(Subscription::unsubscribe);

            // Unsubscribe category-based subscriptions
            Lists.immutable.ofAll(categorySubscriptions.values()).forEach(Subscription::unsubscribe);

            // Clear all subscription collections
            nodeSubscriptions.clear();
            categorySubscriptions.clear();

            // Remove visual elements from the scene
            removeVisualElements();

            // Reset window state to idle
            resetWindowState();

            // Clear property values
            clearPropertyValues();

            // Remove this WindowSupport from the pane's properties
            pane.getProperties().remove(WINDOW_SUPPORT_KEY);
        } catch (Exception e) {
            LOG.error("Error removing window support", e);
        }
    }

    /**
     * Removes all visual elements from the pane.
     */
    private void removeVisualElements() {
        pane.getChildren().remove(outlineRect);

        // Remove all resize handles
        if (!resizeHandles.isEmpty()) {
            resizeHandles.values().forEach(handle ->
                    pane.getChildren().remove(handle.getNode()));
        }
    }

    /**
     * Clears all property values.
     */
    private void clearPropertyValues() {
        anchorWindowPosition.set(null);
        anchorMousePosition.set(null);
        dragAnchorPoint.set(null);
        dragPreviousLocation.set(null);
    }

    /**
     * Adds a subscription to a named category for logical organization and lifecycle management.
     */
    private void addSubscription(String category, Subscription subscription) {
        categorySubscriptions.merge(category, subscription, Subscription::combine);
    }

    /**
     * Gets existing WindowSupport for a container or creates a new one if it doesn't exist.
     *
     * @param container The container to get or create WindowSupport for must not be null
     * @return The WindowSupport instance, never null
     * @throws IllegalArgumentException if the container is null
     */
    public static WindowSupport setupWindowSupport(final Pane container) {
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

    /**
     * Clean up the WindowSupport from a container.
     * <p>
     * This method will remove the WindowSupport instance from the container's properties
     * and clean up any resources associated with it. If no support exists, no action is taken.
     *
     * @param container The container to remove support from; must not be null
     * @throws IllegalArgumentException if the container is null
     */
    public static void cleanupWindowSupport(final Pane container) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null");
        }

        WindowSupport support = container.getProperties().get(WINDOW_SUPPORT_KEY) instanceof WindowSupport ws ? ws : null;

        if (support != null) {
            support.removeSupport();
        }

    }

    // Getter and setter methods

    /**
     * Gets the current mouse press handler for window positioning.
     */
    public BiConsumer<MouseEvent, WindowSupport> getDragMousePressHandler() {
        return dragMousePressHandler;
    }

    /**
     * Gets the current mouse drag handler for window positioning.
     */
    public BiConsumer<MouseEvent, WindowSupport> getDragMouseDragHandler() {
        return dragMouseDragHandler;
    }

    /**
     * Gets the current mouse release handler for window positioning.
     */
    public BiConsumer<MouseEvent, WindowSupport> getDragMouseReleaseHandler() {
        return dragMouseReleaseHandler;
    }

    /**
     * Returns the pane being managed by this WindowSupport instance.
     *
     * @return The Pane that this instance is providing window behavior for
     */
    public Pane getPane() {
        return pane;
    }

    /**
     * Indicates whether a resize or drag operation is currently in progress.
     *
     * @return true if the window is currently being dragged or resized, false otherwise
     */
    public boolean isWindowActive() {
        return windowState != WindowState.IDLE;
    }
}