package dev.ikm.komet.kview.controls;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;

/**
 * A responsive extension of JavaFX's TextFlow component that properly handles
 * text wrapping and layout recalculation based on parent container dimensions.
 * <p>
 * This component improves upon the standard TextFlow by:
 * <ul>
 *   <li>Calculating preferred height based on parent width for proper text wrapping</li>
 *   <li>Deferring layout requests to the next JavaFX pulse cycle</li>
 *   <li>Ensuring text properly wraps and reflows when container dimensions change</li>
 * </ul>
 * <p>
 * Use this component when you need text that automatically adjusts to its container
 * while maintaining proper text wrapping behavior.
 *
 * @see TextFlow
 * @see Region
 */
public class ResponsiveTextFlow extends TextFlow {

    /**
     * Constructs a new responsive text flow component with deferred layout behavior.
     */
    public ResponsiveTextFlow() {
        super();
    }

    /**
     * Requests a layout pass on this component with deferred scheduling.
     * <p>
     * This implementation improves upon the standard TextFlow layout behavior by
     * deferring layout requests to the next pulse via Platform.runLater().
     * <p>
     * The deferred processing allows text wrapping calculations to complete before
     * final layout positioning occurs, ensuring that the component's size properly
     * accounts for text that needs to wrap based on the parent container's dimensions.
     */
    @Override
    public void requestLayout() {
        Platform.runLater(super::requestLayout);
    }

    /**
     * Computes the preferred height of this text flow component based on the
     * given width.
     * <p>
     * This implementation uses the parent container's width (accounting for insets)
     * rather than the provided width parameter, which allows for accurate text wrapping
     * and height calculations even before the component is fully laid out.
     *
     * @param width The width available for layout (not used directly in this implementation)
     * @return The preferred height for this component based on parent width
     */
    @Override
    protected double computePrefHeight(double width) {
        return super.computePrefHeight(getParentWidth());
    }

    /**
     * Determines the effective width of the parent container, accounting for
     * insets when applicable.
     * <p>
     * This method handles different parent types:
     * <ul>
     *   <li>For Region parents, uses width minus insets</li>
     *   <li>For other parent types, uses the layout bounds width</li>
     * </ul>
     * <p>
     * If no parent exists or the parent width is not positive, returns
     * {@link Region#USE_COMPUTED_SIZE} to indicate that a computed size should be used.
     *
     * @return The effective width of the parent container, or {@link Region#USE_COMPUTED_SIZE}
     *         if the parent width cannot be determined
     */
    private double getParentWidth() {
        final Parent parent = getParent();
        double parentWidth;
        if (parent != null) {
            if (parent instanceof Region region) {
                parentWidth = region.getWidth();
                Insets insets = region.getInsets();
                if (insets != null) {
                    parentWidth -= insets.getLeft() + insets.getRight();
                }
            } else {
                parentWidth = parent.getLayoutBounds().getWidth();
            }

            return parentWidth > 0 ? parentWidth : Region.USE_COMPUTED_SIZE;
        }
        return Region.USE_COMPUTED_SIZE;
    }
}
