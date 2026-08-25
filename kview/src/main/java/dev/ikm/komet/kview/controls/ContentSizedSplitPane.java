package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.fxutils.FXUtils;
import javafx.css.PseudoClass;
import javafx.event.EventTarget;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.ArrayList;
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
 *
 * <p>A collapsed {@link javafx.scene.control.TitledPane} is the one exception: it holds no more than
 * its title bar in either mode, since there is nothing behind that title bar for the length to show.
 * What it gives up goes to the item carrying this pane's slack, and comes back at the length it was
 * last holding open — so a size the user dragged an item to outlives a collapse.
 */
public class ContentSizedSplitPane extends SplitPane {

    /** Style class the skin puts on its divider nodes. */
    private static final String DIVIDER_STYLE_CLASS = "split-pane-divider";

    /**
     * Active on a divider sitting below a collapsed {@link TitledPane}. That divider has nothing
     * left to drag — the pane above it is at its title bar and cannot give or take any height — so
     * a stylesheet can thin it down to whatever gap a collapsed pane should keep below it, instead
     * of the full grab-width every other divider needs.
     */
    private static final PseudoClass BELOW_COLLAPSED = PseudoClass.getPseudoClass("below-collapsed");

    /**
     * Divider positions are fractions of the pane's length, so this is well under a pixel for any
     * realistic pane. Positions closer than this to the ones already set are left alone: setting a
     * position asks for another layout pass, which a rounding difference alone shouldn't do.
     */
    private static final double POSITION_TOLERANCE = 0.0001;

    /** Sub-pixel differences in an item's length, which snapping alone accounts for, are left alone. */
    private static final double SIZE_TOLERANCE = 0.5;

    /**
     * Key marking an item this pane is holding closed, so that an item opening again is told apart
     * from one that was never closed — only the former is handed a length back.
     */
    private static final String HELD_CLOSED_KEY = "content-sized-split-pane-held-closed";

    /**
     * Key under which an item keeps the length it was last left holding open, which is the length it
     * opens back up to after being closed. Written when the user lets go of a divider — the one
     * moment the lengths are settled and known to be the ones they chose. An item whose divider was
     * never dragged has no entry, and opens to what its content asks for.
     */
    private static final String OPEN_LENGTH_KEY = "content-sized-split-pane-open-length";

    private boolean sizeToContent = true;

    private boolean draggingDivider;

    /**
     * This pane's length as of the previous layout pass. An item opening again is handed its length
     * back only on a pass where this has settled: while the pane is still growing to fit the item
     * that just opened, SplitPane's skin puts every item that is not
     * {@link SplitPane#setResizableWithParent(Node, Boolean) resizable with the parent} back to the
     * length it had before the growth — which would take the handed-back length straight off it again.
     */
    private double previousLength = -1;

    public ContentSizedSplitPane() {
        // Grabbing a divider hands the sizing over to the user, for good.
        addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (isDivider(mouseEvent.getTarget())) {
                sizeToContent = false;
                draggingDivider = true;
            }
        });

        // The lengths the user lets go of are the ones the items open back up to after a collapse.
        addEventFilter(MouseEvent.MOUSE_RELEASED, _ -> {
            if (draggingDivider) {
                draggingDivider = false;
                rememberOpenLengths();
            }
        });
    }

    /**
     * Writes down the length each open item is holding, which is where it returns to after being
     * collapsed and opened again. Taken at the end of a divider drag, when the lengths are the ones
     * the user settled on and nothing is mid-transition.
     */
    private void rememberOpenLengths() {
        List<Node> items = getItems();
        List<Divider> dividers = getDividers();
        List<Region> dividerNodes = dividerNodes();
        if (dividers.isEmpty() || items.size() != dividers.size() + 1
                || dividerNodes.size() != dividers.size()) {
            return;
        }

        double length = getOrientation() == Orientation.VERTICAL
                ? getHeight() - snappedTopInset() - snappedBottomInset()
                : getWidth() - snappedLeftInset() - snappedRightInset();
        if (length <= 0) {
            return;
        }

        double[] sizes = currentSizes(length, dividers, dividerNodes);
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof TitledPane titledPane && titledPane.isExpanded()) {
                titledPane.getProperties().put(OPEN_LENGTH_KEY, sizes[i]);
            }
        }
    }

    @Override
    protected void layoutChildren() {
        markDividersBelowCollapsedItems();
        if (sizeToContent) {
            sizeItemsToContent();
        } else {
            closeCollapsedItems();
        }
        super.layoutChildren();
    }

    /**
     * Flags every divider that sits below a collapsed {@link TitledPane} with {@link #BELOW_COLLAPSED},
     * so a stylesheet can give it a gap of its own rather than the width a draggable divider needs.
     */
    private void markDividersBelowCollapsedItems() {
        List<Node> items = getItems();
        List<Region> dividers = dividerNodes();
        for (int i = 0; i < dividers.size(); i++) {
            boolean belowCollapsed = i < items.size()
                    && items.get(i) instanceof TitledPane pane
                    && !pane.isExpanded();
            dividers.get(i).pseudoClassStateChanged(BELOW_COLLAPSED, belowCollapsed);
        }
    }

    /**
     * The length this pane's items ask for, measured across the breadth the pane actually has —
     * which is the breadth {@link #sizeItemsToContent()} measures them at when it hands out their
     * sizes. SplitPane's own skin measures its items without a breadth, and an item whose height
     * depends on its width (wrapped text, say) answers differently then, so the length the pane
     * asks its parent for would not be the length its items go on to fill: the difference reaches
     * the window as space the sections never take up.
     */
    @Override
    protected double computePrefHeight(double width) {
        if (!sizeToContent || getOrientation() != Orientation.VERTICAL) {
            return super.computePrefHeight(width);
        }

        List<Node> items = getItems();
        double breadth = (width < 0 ? getWidth() : width) - snappedLeftInset() - snappedRightInset();
        if (items.isEmpty() || breadth <= 0) {
            return super.computePrefHeight(width);
        }

        double length = totalDividerThickness();
        for (Node item : items) {
            length += item.prefHeight(breadth);
        }
        return snappedTopInset() + length + snappedBottomInset();
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
        List<Region> dividerNodes = dividerNodes();
        double availableLength = length - totalDividerThickness();
        if (availableLength <= 0 || breadth <= 0 || dividerNodes.size() != dividers.size()) {
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

        applySizes(sizes, length, dividers, dividerNodes);
    }

    /**
     * Closes a collapsed item down to its title bar once the user has taken the sizing over. From
     * that first drag on nothing re-places the dividers, so a collapsed item would go on holding the
     * length it had while it was open. The length it gives up goes to the item carrying this pane's
     * slack — the last one, or the first when the last is itself the one collapsing — and comes back
     * from there when it opens again, at the length the item was last holding open (see
     * {@link #OPEN_LENGTH_KEY}). A size the user dragged an item to therefore survives being
     * collapsed and reopened.
     */
    private void closeCollapsedItems() {
        List<Node> items = getItems();
        List<Divider> dividers = getDividers();
        List<Region> dividerNodes = dividerNodes();
        if (dividers.isEmpty() || items.size() != dividers.size() + 1
                || dividerNodes.size() != dividers.size()) {
            return;
        }

        boolean vertical = getOrientation() == Orientation.VERTICAL;
        double length = vertical
                ? getHeight() - snappedTopInset() - snappedBottomInset()
                : getWidth() - snappedLeftInset() - snappedRightInset();
        double breadth = vertical
                ? getWidth() - snappedLeftInset() - snappedRightInset()
                : getHeight() - snappedTopInset() - snappedBottomInset();
        if (length <= 0 || breadth <= 0) {
            return;
        }

        boolean lengthSettled = Math.abs(length - previousLength) <= SIZE_TOLERANCE;
        previousLength = length;

        int slack = items.size() - 1;
        double[] sizes = currentSizes(length, dividers, dividerNodes);
        boolean changed = false;

        for (int i = 0; i < items.size(); i++) {
            if (!(items.get(i) instanceof TitledPane pane)) {
                continue;
            }
            int giveTo = i == slack ? 0 : slack;
            double pref = prefLength(pane, breadth, vertical);

            if (!pane.isExpanded()) {
                // Marked whether or not there is anything to give up, so that opening the pane again
                // is recognised as the pane opening rather than as a pane that was never closed.
                pane.getProperties().put(HELD_CLOSED_KEY, Boolean.TRUE);
                if (sizes[i] > pref + SIZE_TOLERANCE) {
                    sizes[giveTo] += sizes[i] - pref;
                    sizes[i] = pref;
                    changed = true;
                }
                continue;
            }

            if (!pane.getProperties().containsKey(HELD_CLOSED_KEY)) {
                continue;
            }
            if (!lengthSettled) {
                // Still growing to fit the item that just opened. Handing the length back now would
                // only have it taken off again, so keep the mark and come back on the next pass.
                continue;
            }
            pane.getProperties().remove(HELD_CLOSED_KEY);

            // Opening. Back to the length it was last holding open, so a size the user dragged it to
            // survives the round trip; one this pane never saw open takes what its content asks for.
            Object remembered = pane.getProperties().get(OPEN_LENGTH_KEY);
            double openLength = remembered instanceof Number held ? held.doubleValue() : pref;
            double wanted = Math.min(openLength - sizes[i],
                    sizes[giveTo] - minLength(items.get(giveTo), breadth, vertical));
            if (wanted > SIZE_TOLERANCE) {
                sizes[i] += wanted;
                sizes[giveTo] -= wanted;
                changed = true;
            }
        }

        if (changed) {
            applySizes(sizes, length, dividers, dividerNodes);
        }
    }

    /** The length each item holds as things stand, read back from where the dividers sit. */
    private double[] currentSizes(double length, List<Divider> dividers, List<Region> dividerNodes) {
        double[] sizes = new double[dividers.size() + 1];
        double edge = 0;
        for (int i = 0; i < dividers.size(); i++) {
            double thickness = dividerThickness(dividerNodes.get(i));
            double dividerTop = dividers.get(i).getPosition() * length - thickness / 2;
            sizes[i] = Math.max(0, dividerTop - edge);
            edge = dividerTop + thickness;
        }
        sizes[sizes.length - 1] = Math.max(0, length - edge);
        return sizes;
    }

    /**
     * Places the dividers so each item holds the length it was allotted. A divider's position is
     * where its centre sits, as a fraction of the pane's length (see SplitPaneSkin), so walk the
     * items adding each one's size and the divider that follows it.
     */
    private void applySizes(double[] sizes, double length, List<Divider> dividers, List<Region> dividerNodes) {
        double edge = 0;
        for (int i = 0; i < dividers.size(); i++) {
            edge += sizes[i];
            double thickness = dividerThickness(dividerNodes.get(i));
            double position = (edge + thickness / 2) / length;
            Divider divider = dividers.get(i);
            if (Math.abs(divider.getPosition() - position) > POSITION_TOLERANCE) {
                divider.setPosition(position);
            }
            edge += thickness;
        }
    }

    private static double prefLength(Node item, double breadth, boolean vertical) {
        return vertical ? item.prefHeight(breadth) : item.prefWidth(breadth);
    }

    private static double minLength(Node item, double breadth, boolean vertical) {
        return vertical ? item.minHeight(breadth) : item.minWidth(breadth);
    }

    /**
     * How much of the pane's length all the dividers take up between them. Dividers are not all the
     * same thickness — the one below a collapsed item thins to its {@link #BELOW_COLLAPSED} gap — so
     * each is measured in turn.
     */
    private double totalDividerThickness() {
        double thickness = 0;
        for (Region divider : dividerNodes()) {
            thickness += dividerThickness(divider);
        }
        return thickness;
    }

    /**
     * How much of the pane's length one divider takes up. The skin sizes its dividers by their
     * preferred width whichever way the pane is oriented, so this reads the same measure.
     */
    private static double dividerThickness(Region divider) {
        return divider.prefWidth(-1);
    }

    /** The skin's divider nodes, in the order of the gaps they fill. */
    private List<Region> dividerNodes() {
        List<Region> dividers = new ArrayList<>();
        for (Node child : getChildrenUnmodifiable()) {
            if (child instanceof Region divider && divider.getStyleClass().contains(DIVIDER_STYLE_CLASS)) {
                dividers.add(divider);
            }
        }
        return dividers;
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
