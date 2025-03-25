package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLExpandableNodeListControl;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.ResourceBundle;

/**
 * A skin implementation for the {@link KLExpandableNodeListControl} that provides
 * animated expansion and collapse functionality for a list of nodes.
 * <p>
 * This skin divides the control's items into two sections:
 * <ul>
 *   <li>A "visible" section that always displays the first N items (where N is determined by
 *       {@link KLExpandableNodeListControl#getVisibleCount()})</li>
 *   <li>A "hidden" section that contains the remaining items and can be shown/hidden by toggling
 *       the control's {@link KLExpandableNodeListControl#expandedProperty()}</li>
 * </ul>
 * <p>
 * The skin provides a toggle button that allows users to expand or collapse the hidden content
 * with a smooth animation. The button is only displayed when there are more items than the
 * visible count.
 *
 * @see KLExpandableNodeListControl
 */
public class KLExpandableNodeListControlSkin extends SkinBase<KLExpandableNodeListControl> {

    /**
     * The resource bundle for the skin.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
            "dev.ikm.komet.kview.controls.expandable-node-list-control");
    /**
     * The duration of the expand/collapse animation.
     */
    private static final Duration ANIMATION_DURATION = Duration.millis(350);

    /**
     * The main container for all content (both visible and hidden).
     */
    private final VBox contentContainer;

    /**
     * Container for the always-visible content items.
     */
    private final VBox visibleContentContainer;

    /**
     * Container for the expandable/collapsible content items.
     */
    private final VBox hiddenContentContainer;

    /**
     * Rectangle used for clipping the content during animations.
     */
    private final Rectangle clipRect;

    /**
     * Button that toggles the expanded/collapsed state.
     */
    private final ToggleButton toggleButton;

    /**
     * The timeline used for animations.
     */
    private Timeline timeline = null;

    /**
     * The transition value at the start of an animation.
     */
    private double transitionStartValue;

    /**
     * Flag to prevent recursive layout operations.
     */
    private boolean layoutInProgress = false;

    /**
     * Constructs a new skin for the KLExpandableNodeListControl.
     * <p>
     * This initializes all UI components, sets up property bindings, and registers
     * listeners for control property changes.
     *
     * @param control The control that this skin is attached to
     */
    public KLExpandableNodeListControlSkin(KLExpandableNodeListControl control) {
        super(control);

        visibleContentContainer = new VBox();
        hiddenContentContainer = new VBox();

        // Create the main container for all content items
        contentContainer = new VBox(visibleContentContainer);
        contentContainer.getStyleClass().add("content-container");
        contentContainer.setMaxWidth(Double.MAX_VALUE);
        visibleContentContainer.spacingProperty().bind(contentContainer.spacingProperty());
        hiddenContentContainer.spacingProperty().bind(contentContainer.spacingProperty());

        // Set up the clip rectangle to control visibility during animations
        clipRect = new Rectangle();
        contentContainer.setClip(clipRect);

        // Create and configure the arrow button that indicates expanded/collapsed state
        StackPane arrowRegion = new StackPane();
        arrowRegion.getStyleClass().setAll("arrow-button");

        StackPane arrow = new StackPane();
        arrow.setId("arrow");
        arrow.getStyleClass().setAll("arrow");

        // Rotate the arrow based on the transition state (0-1)
        arrow.rotateProperty().bind(new DoubleBinding() {
            {
                bind(transitionProperty());
            }

            @Override
            protected double computeValue() {
                return -180 * getTransition();
            }
        });
        arrowRegion.getChildren().setAll(arrow);

        // Create and configure the toggle button with the arrow indicator
        toggleButton = new ToggleButton(getString("show.more.button.text"), arrowRegion);

        // Bind button selected state to control's expanded property (two-way)
        toggleButton.selectedProperty().bindBidirectional(control.expandedProperty());

        // Update button text based on expanded state
        toggleButton.textProperty().bind(Bindings.createStringBinding(
                () -> control.isExpanded() ?
                        getString("show.less.button.text") : getString("show.more.button.text"),
                control.expandedProperty()));

        // Set initial transition state based on the control's expanded property
        if (control.isExpanded()) {
            setTransition(1.0); // Fully expanded
        } else {
            setTransition(0.0); // Fully collapsed
        }

        // Add components to the skin
        getChildren().setAll(contentContainer, toggleButton);

        // Initialize content based on current items
        updateNodes();

        // Register listeners to react to changes in control properties
        registerListChangeListener(control.getItems(), o -> updateNodes());
        registerChangeListener(control.expandedProperty(), o -> setExpanded(control.isExpanded()));
        registerChangeListener(control.visibleCountProperty(), o -> updateNodes());

        // Enhanced width listener that forces layout on ALL components
        registerChangeListener(control.widthProperty(), o -> {
            // Update clip width
            clipRect.setWidth(control.getWidth());

            // Force layout on all child components when width changes
            for (Node item : control.getItems()) {
                if (item instanceof Region region) {
                    region.setPrefWidth(control.getWidth());
                }
            }

            // After we've completed layout, use Platform.runLater to ensure
            // that layout gets recalculated after text wrapping has had a chance to complete
            Platform.runLater(control::requestLayout);
        });
    }

    /**
     * Updates the node containers based on the current control state.
     * <p>
     * This method distributes the control's items between the visible and hidden
     * containers based on the {@link KLExpandableNodeListControl#getVisibleCount()}.
     * It also manages the visibility of the toggle button based on whether there
     * are expandable items.
     */
    private void updateNodes() {
        if (layoutInProgress) {
            return;
        }

        layoutInProgress = true;
        try {
            KLExpandableNodeListControl control = getSkinnable();
            List<Node> items = control.getItems();

            // Remove all existing items
            visibleContentContainer.getChildren().clear();
            hiddenContentContainer.getChildren().clear();

            // Process items if there are any
            if (!items.isEmpty()) {
                // Calculate how many items should be in the always-visible section
                int visibleCount = Math.min(items.size(), control.getVisibleCount());

                if (visibleCount > 0) {
                    // Add visible items to the visible content container
                    visibleContentContainer.getChildren().setAll(items.subList(0, visibleCount));
                }

                // Only show the toggle button if there are expandable items
                boolean hasExpandableItems = items.size() > control.getVisibleCount();
                if (hasExpandableItems) {
                    // Add expandable items to the hidden content container
                    hiddenContentContainer.getChildren().setAll(items.subList(visibleCount, items.size()));
                }
                toggleButton.setVisible(hasExpandableItems);
                toggleButton.setManaged(hasExpandableItems);
            } else {
                // No items to display, so hide the toggle button
                toggleButton.setVisible(false);
                toggleButton.setManaged(false);
            }

            // Request layout
            getSkinnable().requestLayout();
        } finally {
            layoutInProgress = false;
        }
    }

    /**
     * Updates the expanded state of the control.
     * <p>
     * When the expanded state changes, this method initiates the appropriate
     * transition animation and updates the content containers accordingly.
     *
     * @param expanded The new expanded state
     */
    private void setExpanded(boolean expanded) {
        KLExpandableNodeListControl control = getSkinnable();
        List<Node> items = control.getItems();

        // Do nothing if there are no expandable items
        if (items.isEmpty() || items.size() <= control.getVisibleCount()) {
            return;
        }

        // Record current transition value for smooth animation from current state
        transitionStartValue = getTransition();

        if (expanded) {
            // Add hidden content container to content container when expanding
            contentContainer.getChildren().add(hiddenContentContainer);
        }

        // Start the appropriate animation
        animateTransition(expanded);
    }

    /**
     * Animates the transition between expanded and collapsed states.
     * <p>
     * This method creates and plays a timeline animation that smoothly transitions
     * between states. It ensures that any running animation is properly stopped
     * before starting a new one to prevent conflicts.
     *
     * @param expanding True if animating to expanded state, false if animating to collapsed state
     */
    private void animateTransition(boolean expanding) {
        // Cancel any running animation to avoid conflicts
        if (timeline != null && (timeline.getStatus() != Animation.Status.STOPPED)) {
            timeline.stop();
            timeline = null;
        }

        // Create a new animation timeline
        timeline = new Timeline();
        timeline.setCycleCount(1);

        if (expanding) {
            // Animation for expanding: transition from current value to 1.0
            timeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(transitionProperty(), transitionStartValue)),
                    new KeyFrame(ANIMATION_DURATION,
                            new KeyValue(transitionProperty(), 1.0, Interpolator.EASE_OUT)
                    ));
        } else {
            // Animation for collapsing: transition from current value to 0.0
            timeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(transitionProperty(), transitionStartValue)),
                    new KeyFrame(ANIMATION_DURATION,
                            e -> {
                                // When collapsing, hide expandable items after the animation completes
                                contentContainer.getChildren().remove(hiddenContentContainer);
                            },
                            new KeyValue(transitionProperty(), 0.0, Interpolator.EASE_IN)
                    ));
        }

        // Start the animation
        timeline.play();
    }

    /**
     * The property that tracks the current transition state.
     * <p>
     * This value ranges from 0.0 (fully collapsed) to 1.0 (fully expanded),
     * with intermediate values representing transition states during animation.
     */
    private DoubleProperty transition;

    /**
     * Sets the current transition value.
     *
     * @param value The new transition value (between 0.0 and 1.0)
     */
    private void setTransition(double value) {
        transitionProperty().set(value);
    }

    /**
     * Gets the current transition value.
     *
     * @return The current transition value (between 0.0 and 1.0)
     */
    private double getTransition() {
        return transition == null ? 0.0 : transition.get();
    }

    /**
     * Gets the transition property.
     * <p>
     * This property is lazily initialized and triggers a layout request
     * whenever its value changes.
     *
     * @return The transition property
     */
    private DoubleProperty transitionProperty() {
        if (transition == null) {
            transition = new SimpleDoubleProperty(this, "transition", 0.0) {
                @Override
                protected void invalidated() {
                    // Request layout when transition value changes to update the height
                    getSkinnable().requestLayout();
                }
            };
        }
        return transition;
    }

    /**
     * Calculates the minimum width required to display the skin.
     * <p>
     * This considers both the content container and toggle button to determine
     * the minimum width required.
     *
     * @param height      The height constraint
     * @param topInset    The top inset
     * @param rightInset  The right inset
     * @param bottomInset The bottom inset
     * @param leftInset   The left inset
     * @return The minimum width
     */
    @Override
    protected double computeMinWidth(double height,
                                     double topInset, double rightInset,
                                     double bottomInset, double leftInset) {
        // Consider both content container and toggle button
        double contentWidth = snapSizeX(contentContainer.minWidth(height));
        double toggleWidth = toggleButton.isVisible() ? snapSizeX(toggleButton.minWidth(height)) : 0;

        // Return the larger of the two, plus insets
        return leftInset + Math.max(contentWidth, toggleWidth) + rightInset;
    }

    /**
     * Calculates the minimum height required to display the skin.
     * <p>
     * This accounts for the visible content, the toggle button, and the
     * expanded content based on the current transition value.
     *
     * @param width       The width constraint
     * @param topInset    The top inset
     * @param rightInset  The right inset
     * @param bottomInset The bottom inset
     * @param leftInset   The left inset
     * @return The minimum height
     */
    @Override
    protected double computeMinHeight(double width,
                                      double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        if (getSkinnable().getItems().isEmpty()) {
            return topInset + bottomInset;
        }

        // Calculate visible items height
        final double visibleContentHeight = visibleContentContainer.minHeight(width);

        // Add toggle button height if needed
        final double toggleButtonHeight = toggleButton.isVisible() ? toggleButton.minHeight(width) : 0;

        // Include expanded items height based on transition value (0.0-1.0)
        final double expandedContentHeight = hiddenContentContainer.minHeight(width) * getTransition();

        // Calculate spacing between sections
        final double spacing = contentContainer.getSpacing();

        // Calculate total height with all components
        double totalHeight = topInset + visibleContentHeight + bottomInset;

        // Add expanded section if visible
        if (expandedContentHeight > 0) {
            totalHeight += spacing + expandedContentHeight;
        }

        // Add toggle button if visible
        if (toggleButton.isVisible()) {
            totalHeight += spacing + toggleButtonHeight;
        }

        return totalHeight;
    }

    /**
     * Calculates the preferred width for the skin.
     * <p>
     * This considers both the content container and toggle button to determine
     * the preferred width.
     *
     * @param height      The height constraint
     * @param topInset    The top inset
     * @param rightInset  The right inset
     * @param bottomInset The bottom inset
     * @param leftInset   The left inset
     * @return The preferred width
     */
    @Override
    protected double computePrefWidth(double height,
                                      double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        // Consider both content container and toggle button
        double contentWidth = snapSizeX(contentContainer.prefWidth(height));
        double toggleWidth = toggleButton.isVisible() ? snapSizeX(toggleButton.prefWidth(height)) : 0;

        // Return the larger of the two, plus insets
        return leftInset + Math.max(contentWidth, toggleWidth) + rightInset;
    }

    /**
     * Calculates the preferred height for the skin.
     * <p>
     * This accounts for the visible content, the toggle button, and the
     * expanded content based on the current transition value.
     *
     * @param width       The width constraint
     * @param topInset    The top inset
     * @param rightInset  The right inset
     * @param bottomInset The bottom inset
     * @param leftInset   The left inset
     * @return The preferred height
     */
    @Override
    protected double computePrefHeight(double width,
                                       double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        if (getSkinnable().getItems().isEmpty()) {
            return topInset + bottomInset;
        }

        // Calculate visible items height
        final double visibleContentHeight = visibleContentContainer.prefHeight(width);

        // Add toggle button height if needed
        final double toggleButtonHeight = toggleButton.isVisible() ? toggleButton.prefHeight(width) : 0;

        // Include expanded items height based on transition value (0.0-1.0)
        final double expandedContentHeight = hiddenContentContainer.prefHeight(width) * getTransition();

        // Calculate spacing between sections
        final double spacing = contentContainer.getSpacing();

        // Calculate total height with all components
        double totalHeight = topInset + visibleContentHeight + bottomInset;

        // Add expanded section if visible
        if (expandedContentHeight > 0) {
            totalHeight += spacing + expandedContentHeight;
        }

        // Add toggle button if visible
        if (toggleButton.isVisible()) {
            totalHeight += spacing + toggleButtonHeight;
        }

        return totalHeight;
    }

    /**
     * Positions and sizes all child nodes during layout.
     * <p>
     * This method handles the layout of the content container and toggle button,
     * ensuring proper positioning and sizing based on the current state.
     * It also updates the clip rectangle to match the content dimensions.
     *
     * @param contentX      The x coordinate of the layout area
     * @param contentY      The y coordinate of the layout area
     * @param contentWidth  The width of the layout area
     * @param contentHeight The height of the layout area
     */
    @Override
    protected void layoutChildren(double contentX, double contentY,
                                  double contentWidth, double contentHeight) {
        if (layoutInProgress) {
            return;
        }

        layoutInProgress = true;
        try {
            KLExpandableNodeListControl control = getSkinnable();
            List<Node> items = control.getItems();

            if (items.isEmpty()) {
                return;
            }

            final double spacing = contentContainer.getSpacing();

            // Calculate heights for all components using the correct width constraint
            final double visibleContentHeight = visibleContentContainer.prefHeight(contentWidth);
            final double expandedContentHeight = hiddenContentContainer.prefHeight(contentWidth) * getTransition();

            // Fix: Pass correct dimension parameters to toggle button methods
            final double toggleButtonWidth = toggleButton.isVisible() ?
                    snapSizeX(Math.min(toggleButton.prefWidth(-1), contentWidth)) : 0;
            final double toggleButtonHeight = toggleButton.isVisible() ?
                    snapSizeY(toggleButton.prefHeight(-1)) : 0;

            // Calculate content container height based on visible + expanded sections
            double contentContainerHeight = visibleContentHeight;
            if (expandedContentHeight > 0) {
                contentContainerHeight += spacing + expandedContentHeight;
            }
            contentContainerHeight = snapSizeY(contentContainerHeight);

            // Position and resize the content container
            contentContainer.resize(contentWidth, contentContainerHeight);
            positionInArea(contentContainer, contentX, contentY, contentWidth, contentContainerHeight,
                    0, HPos.CENTER, VPos.CENTER);

            // Update the clip rectangle to match the actual content size exactly
            clipRect.setWidth(contentWidth);
            clipRect.setHeight(contentContainerHeight);

            // Position and resize the toggle button if visible
            if (toggleButton.isVisible()) {
                // Calculate Y position for toggle button (below content container)
                double toggleButtonY = contentY + contentContainerHeight + spacing;

                // Position and resize toggle button - ensure it gets the full width
                toggleButton.resize(toggleButtonWidth, toggleButtonHeight);
                positionInArea(toggleButton, contentX, toggleButtonY, toggleButtonWidth, toggleButtonHeight,
                        0, HPos.LEFT, VPos.BOTTOM);
            }
        } finally {
            layoutInProgress = false;
        }
    }

    /**
     * Cleans up resources when the skin is no longer needed.
     * <p>
     * This ensures that any running animations are properly stopped to
     * prevent memory leaks.
     */
    @Override
    public void dispose() {
        // Stop any running animation to prevent memory leaks
        if (timeline != null && timeline.getStatus() != Animation.Status.STOPPED) {
            timeline.stop();
        }

        super.dispose();
    }

    /**
     * Retrieves a localized string from the resource bundle.
     *
     * @param key The key for the localized string
     * @return The localized string
     */
    private static String getString(String key) {
        return RESOURCE_BUNDLE.getString(key);
    }
}