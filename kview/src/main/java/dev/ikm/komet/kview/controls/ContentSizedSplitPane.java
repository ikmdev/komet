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
 * with it — until the user drags a divider. From that first drag on, the items hold the lengths the
 * user gives them: every open item keeps the length it had when a divider was last let go of, and
 * the lowest open item takes up the slack, growing and shrinking with the pane. A collapsed
 * {@link TitledPane} holds no more than its title bar, since there is nothing behind that title bar
 * for the length to show, and opens back up to the length it was last holding open — so a size the
 * user dragged an item to outlives a collapse, and the items around it never move.
 *
 * <p>SplitPane's own skin spreads a change of the pane's length over the items that are
 * {@link SplitPane#setResizableWithParent(Node, Boolean) resizable with the parent}, and pins the
 * others to the lengths they had. The lengths here are placed afresh on every pass instead, so all
 * items must be left resizable with the parent (the default): an item pinned by the skin would be
 * put back to its old length right after this pane placed it, and a collapsing one would hold on
 * to the length of its content.
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

    /**
     * Key under which an item keeps the length it holds while open, once the user has taken the
     * sizing over: the length it is placed at on every layout pass, and opens back up to after
     * being collapsed. Written when the user lets go of a divider — the one moment the lengths are
     * settled and known to be the ones they chose — and kept up to date for the item carrying the
     * pane's slack, whose length follows the pane's. An item that was never seen open has no entry,
     * and opens to what its content asks for.
     */
    private static final String HELD_LENGTH_KEY = "content-sized-split-pane-held-length";

    private boolean sizeToContent = true;

    private boolean draggingDivider;

    public ContentSizedSplitPane() {
        // Grabbing a divider hands the sizing over to the user, for good. While the divider is
        // held, SplitPane's own drag handling places it; the lengths are picked up on release.
        addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (isDivider(mouseEvent.getTarget())) {
                sizeToContent = false;
                draggingDivider = true;
            }
        });

        addEventFilter(MouseEvent.MOUSE_RELEASED, _ -> {
            if (draggingDivider) {
                draggingDivider = false;
                rememberHeldLengths();
            }
        });
    }

    /**
     * Writes down the length each open item is holding, which is the length it is held at from here
     * on and returns to after being collapsed and opened again. Taken at the end of a divider drag,
     * when the lengths are the ones the user settled on and nothing is mid-transition. A collapsed
     * item keeps the entry it has: its length now is its title bar, not a length it holds open.
     */
    private void rememberHeldLengths() {
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
            if (!isCollapsed(items.get(i))) {
                items.get(i).getProperties().put(HELD_LENGTH_KEY, sizes[i]);
            }
        }
    }

    @Override
    protected void layoutChildren() {
        markDividersBelowCollapsedItems();
        if (sizeToContent) {
            sizeItemsToContent();
        } else if (!draggingDivider) {
            holdItemLengths();
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
            boolean belowCollapsed = i < items.size() && isCollapsed(items.get(i));
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
        Layout layout = Layout.of(this);
        if (layout == null) {
            return;
        }

        double[] prefSizes = new double[layout.items.size()];
        for (int i = 0; i < prefSizes.length; i++) {
            prefSizes[i] = prefLength(layout.items.get(i), layout.breadth, layout.vertical);
        }

        double[] sizes = FXUtils.capAtFairShare(prefSizes, layout.availableLength);
        double allotted = 0;
        for (double size : sizes) {
            allotted += size;
        }
        sizes[sizes.length - 1] += layout.availableLength - allotted;

        applySizes(sizes, layout.length, layout.dividers, layout.dividerNodes);
    }

    /**
     * Places the dividers so every item holds its length (see {@link #HELD_LENGTH_KEY}) — its title
     * bar, for a collapsed item — and the lowest open item takes what the others leave, following
     * the pane as it grows and shrinks. Should that leave the lowest open item less than its
     * minimum, the others give up the shortfall in proportion to what they hold above their own
     * minimums; their held lengths stay, so they fill out again as the room comes back.
     */
    private void holdItemLengths() {
        Layout layout = Layout.of(this);
        if (layout == null) {
            return;
        }

        List<Node> items = layout.items;
        int slack = slackIndex(items);
        double[] sizes = new double[items.size()];
        double[] mins = new double[items.size()];
        double others = 0;
        for (int i = 0; i < items.size(); i++) {
            mins[i] = minLength(items.get(i), layout.breadth, layout.vertical);
            if (i == slack) {
                continue;
            }
            sizes[i] = Math.max(mins[i], heldLength(items.get(i), layout.breadth, layout.vertical));
            others += sizes[i];
        }

        double shortfall = mins[slack] - (layout.availableLength - others);
        if (shortfall > 0) {
            double room = 0;
            for (int i = 0; i < items.size(); i++) {
                if (i != slack) {
                    room += sizes[i] - mins[i];
                }
            }
            double taken = Math.min(shortfall, room);
            if (taken > 0) {
                for (int i = 0; i < items.size(); i++) {
                    if (i != slack) {
                        sizes[i] -= (sizes[i] - mins[i]) / room * taken;
                    }
                }
                others -= taken;
            }
        }
        sizes[slack] = layout.availableLength - others;

        // The slack item's length is whatever the pane leaves it, so that is the length it holds
        // from here on — in particular once another item below it opens and takes the slack over.
        items.get(slack).getProperties().put(HELD_LENGTH_KEY, sizes[slack]);

        applySizes(sizes, layout.length, layout.dividers, layout.dividerNodes);
    }

    /** The item carrying the pane's slack: the lowest open one, or the last when none is open. */
    private static int slackIndex(List<Node> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!isCollapsed(items.get(i))) {
                return i;
            }
        }
        return items.size() - 1;
    }

    /**
     * The length an item is held at: its title bar when collapsed, the length it was last holding
     * open otherwise — or, for an item this pane never saw open, what its content asks for.
     */
    private static double heldLength(Node item, double breadth, boolean vertical) {
        if (!isCollapsed(item) && item.getProperties().get(HELD_LENGTH_KEY) instanceof Number held) {
            return held.doubleValue();
        }
        return prefLength(item, breadth, vertical);
    }

    private static boolean isCollapsed(Node item) {
        return item instanceof TitledPane pane && !pane.isExpanded();
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

    /**
     * What a sizing pass works from: the items, the dividers and their nodes, and the pane's
     * length and breadth with the insets taken off. Null while there is nothing to place — the skin
     * has not created a divider per gap yet, or the pane has no size.
     */
    private record Layout(List<Node> items, List<Divider> dividers, List<Region> dividerNodes,
                          boolean vertical, double length, double breadth, double availableLength) {

        static Layout of(ContentSizedSplitPane pane) {
            List<Node> items = pane.getItems();
            List<Divider> dividers = pane.getDividers();
            List<Region> dividerNodes = pane.dividerNodes();
            if (dividers.isEmpty() || items.size() != dividers.size() + 1
                    || dividerNodes.size() != dividers.size()) {
                return null;
            }

            boolean vertical = pane.getOrientation() == Orientation.VERTICAL;
            double length = vertical
                    ? pane.getHeight() - pane.snappedTopInset() - pane.snappedBottomInset()
                    : pane.getWidth() - pane.snappedLeftInset() - pane.snappedRightInset();
            double breadth = vertical
                    ? pane.getWidth() - pane.snappedLeftInset() - pane.snappedRightInset()
                    : pane.getHeight() - pane.snappedTopInset() - pane.snappedBottomInset();
            double availableLength = length - pane.totalDividerThickness();
            if (availableLength <= 0 || breadth <= 0) {
                return null;
            }
            return new Layout(items, dividers, dividerNodes, vertical, length, breadth, availableLength);
        }
    }
}
