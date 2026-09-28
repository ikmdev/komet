package dev.ikm.komet.kview.controls;

import java.util.List;

/**
 * <p>Geometry of the pinned ancestors that are laid over the top of the {@link KLConceptNavigatorControl},
 * so the ancestors of the concepts at the current scroll position are always known.
 * <p>The pinned ancestors are an overlay: they cover the first rows of the viewport instead of pushing
 * them down. The concept they describe is therefore not the first row of the viewport, but the first row
 * that is still visible below the overlay its own ancestors need. That row is the <em>anchor</em>.
 * <p>This class has no JavaFX dependencies: it works on the position, height and depth of the visible rows.
 */
public final class PinnedAncestorsLayout {

    /**
     * The number of ancestors that are pinned while the overlay is collapsed. A deeper concept pins only
     * its nearest ancestors, and a toggle offers the rest.
     */
    public static final int MAX_COLLAPSED_ANCESTORS = 3;

    private PinnedAncestorsLayout() {
    }

    /**
     * A visible row of the viewport.
     *
     * @param top    the y coordinate of the top of the row, relative to the top of the viewport; it is
     *               negative for a first row that is partially scrolled out
     * @param height the height of the row
     * @param depth  the number of ancestors the concept of the row has in the tree
     */
    public record Row(double top, double height, int depth) {
    }

    /**
     * The sizes that are needed to estimate the height of the overlay.
     *
     * @param rowHeight    the height of a pinned ancestor
     * @param toggleHeight the height of the toggle that expands or collapses the hidden ancestors
     * @param insets       the sum of the top and bottom insets of the overlay
     */
    public record Metrics(double rowHeight, double toggleHeight, double insets) {
    }

    /**
     * The pinned ancestors for a given scroll position.
     *
     * @param anchorIndex   the index, in the list of visible rows, of the row whose ancestors are pinned,
     *                      or -1 if there is no such row
     * @param ancestorCount the number of ancestors of the anchor
     * @param shownCount    the number of ancestors that are pinned, which are the nearest ones
     * @param toggleVisible whether the anchor has more ancestors than the collapsed overlay shows, so the
     *                      toggle is needed
     * @param height        the estimated height of the overlay
     */
    public record Result(int anchorIndex, int ancestorCount, int shownCount, boolean toggleVisible, double height) {

        /**
         * The result when nothing is pinned.
         */
        public static final Result NONE = new Result(-1, 0, 0, false, 0);
    }

    /**
     * <p>Number of ancestors that are pinned for a concept with the given depth.
     *
     * @param depth    the number of ancestors of the concept
     * @param expanded whether the user asked to see all the ancestors
     * @return the number of pinned ancestors
     */
    public static int shownCount(int depth, boolean expanded) {
        return expanded ? depth : Math.min(depth, MAX_COLLAPSED_ANCESTORS);
    }

    /**
     * <p>Estimated height of the overlay for a concept with the given depth.
     *
     * @param depth    the number of ancestors of the concept
     * @param expanded whether the user asked to see all the ancestors
     * @param metrics  the sizes of the parts of the overlay
     * @return the height of the overlay, which is 0 for a concept without ancestors
     */
    public static double height(int depth, boolean expanded, Metrics metrics) {
        if (depth <= 0) {
            return 0;
        }
        double toggle = depth > MAX_COLLAPSED_ANCESTORS ? metrics.toggleHeight() : 0;
        return metrics.insets() + toggle + shownCount(depth, expanded) * metrics.rowHeight();
    }

    /**
     * <p>Finds the anchor: the first row, starting at {@code fromIndex}, that remains visible below the
     * overlay that its own ancestors need.
     *
     * @param rows      the visible rows of the viewport, from top to bottom
     * @param fromIndex the index of the first row that can be the anchor
     * @param expanded  whether the user asked to see all the ancestors
     * @param metrics   the sizes of the parts of the overlay
     * @return the pinned ancestors, or {@link Result#NONE} if no row can be the anchor
     */
    public static Result compute(List<Row> rows, int fromIndex, boolean expanded, Metrics metrics) {
        for (int i = Math.max(0, fromIndex); i < rows.size(); i++) {
            Row row = rows.get(i);
            double height = height(row.depth(), expanded, metrics);
            if (row.top() + row.height() > height) {
                return new Result(i, row.depth(), shownCount(row.depth(), expanded),
                        row.depth() > MAX_COLLAPSED_ANCESTORS, height);
            }
        }
        return Result.NONE;
    }
}
