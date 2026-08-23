package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.fxutils.FXUtils;
import javafx.event.EventTarget;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * A {@link SplitPane} that gives each of its items the size its content asks for, instead of
 * splitting its length evenly between them. An item holding a couple of rows takes the height
 * those rows need and no more; the space it doesn't use goes to the last item, which is also the
 * one the pane's own resizing grows and shrinks.
 *
 * <p>When the items' combined preferred size doesn't fit, each is capped at its fair share of what
 * there is (see {@link FXUtils#capAtFairShare(double[], double)}): the small items keep their full
 * preferred size, and what they leave over raises the share of the larger ones.
 *
 * <p>The sizing is re-applied on every layout pass, so an item that gains or loses content resizes
 * with it — until the user drags a divider. From that first drag on, this behaves as an ordinary
 * SplitPane and the items keep the sizes the user gives them.
 */
public class ContentSizedSplitPane extends SplitPane {

    /** Style class the skin puts on its divider nodes. */
    private static final String DIVIDER_STYLE_CLASS = "split-pane-divider";

    /**
     * Divider positions are fractions of the pane's length, so this is well under a pixel for any
     * realistic pane. Positions closer than this to the ones already set are left alone: setting a
     * position asks for another layout pass, which a rounding difference alone shouldn't do.
     */
    private static final double POSITION_TOLERANCE = 0.0001;

    private boolean sizeToContent = true;

    public ContentSizedSplitPane() {
        // Grabbing a divider hands the sizing over to the user, for good.
        addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (isDivider(mouseEvent.getTarget())) {
                sizeToContent = false;
            }
        });
    }

    @Override
    protected void layoutChildren() {
        if (sizeToContent) {
            sizeItemsToContent();
        }
        super.layoutChildren();
    }

    /**
     * Places the dividers so every item gets its preferred size, with the last item taking whatever
     * the others didn't need.
     */
    private void sizeItemsToContent() {
        List<Node> items = getItems();
        List<Divider> dividers = getDividers();
        if (dividers.isEmpty() || items.size() != dividers.size() + 1) {
            // The skin creates a divider per gap between items; until it has, there is nothing to
            // place (and nothing shown either).
            return;
        }

        boolean vertical = getOrientation() == Orientation.VERTICAL;
        double length = vertical
                ? getHeight() - snappedTopInset() - snappedBottomInset()
                : getWidth() - snappedLeftInset() - snappedRightInset();
        double breadth = vertical
                ? getWidth() - snappedLeftInset() - snappedRightInset()
                : getHeight() - snappedTopInset() - snappedBottomInset();
        double dividerThickness = dividerThickness();
        double availableLength = length - dividerThickness * dividers.size();
        if (availableLength <= 0 || breadth <= 0) {
            return;
        }

        double[] prefSizes = new double[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Node item = items.get(i);
            prefSizes[i] = vertical ? item.prefHeight(breadth) : item.prefWidth(breadth);
        }

        double[] sizes = FXUtils.capAtFairShare(prefSizes, availableLength);
        double allotted = 0;
        for (double size : sizes) {
            allotted += size;
        }
        sizes[sizes.length - 1] += availableLength - allotted;

        // A divider's position is where its centre sits, as a fraction of the pane's length (see
        // SplitPaneSkin), so walk the items adding each one's size and the divider that follows it.
        double edge = 0;
        for (int i = 0; i < dividers.size(); i++) {
            edge += sizes[i];
            double position = (edge + dividerThickness / 2) / length;
            Divider divider = dividers.get(i);
            if (Math.abs(divider.getPosition() - position) > POSITION_TOLERANCE) {
                divider.setPosition(position);
            }
            edge += dividerThickness;
        }
    }

    /**
     * How much of the pane's length a divider takes up. The skin sizes its dividers by their
     * preferred width whichever way the pane is oriented, so this reads the same measure.
     */
    private double dividerThickness() {
        for (Node child : getChildrenUnmodifiable()) {
            if (child instanceof Region divider && divider.getStyleClass().contains(DIVIDER_STYLE_CLASS)) {
                return divider.prefWidth(-1);
            }
        }
        return 0;
    }

    /** Whether the passed in event target is a divider, or a part of one (its grabber). */
    private boolean isDivider(EventTarget target) {
        for (Node node = target instanceof Node hit ? hit : null; node != null; node = node.getParent()) {
            if (node.getStyleClass().contains(DIVIDER_STYLE_CLASS)) {
                return true;
            }
            if (node == this) {
                return false;
            }
        }
        return false;
    }
}
