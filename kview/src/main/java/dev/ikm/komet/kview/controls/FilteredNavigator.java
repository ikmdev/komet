package dev.ikm.komet.kview.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Arrays;

/**
 * A {@link Navigator} that shows only the concepts of a {@link ConceptFilterResult}: the concepts that
 * match a filter and the ancestors that lead to them. Everything else is answered by the navigator
 * it narrows, so the hierarchy, the names and the view are that navigator's.
 * <p>
 * A concept the filter does not keep has no parents and no children here, which is how the rest of the
 * concept navigator (search results, lineage, "show in navigator") learns that it is not shown.
 */
public final class FilteredNavigator implements Navigator {

    private final Navigator navigator;
    private final ConceptFilterResult result;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * @param navigator the navigator to narrow
     * @param result    the concepts to keep
     */
    public FilteredNavigator(Navigator navigator, ConceptFilterResult result) {
        this.navigator = navigator;
        this.result = result;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Whether the concept matches the filter, as opposed to being shown only because it leads to
     * a concept that does.
     *
     * @param nid the nid of a concept
     */
    public boolean isMatch(long nid) {
        return result.isMatch(nid);
    }

    /**
     * Whether the concept is shown by this navigator.
     *
     * @param nid the nid of a concept
     */
    public boolean isVisible(long nid) {
        return result.isVisible(nid);
    }

    /**
     * The number of concepts that match the filter.
     */
    public int getMatchCount() {
        return result.getMatchCount();
    }

    @Override
    public long[] getParentNids(long childNid) {
        if (!result.isVisible(childNid)) {
            return new long[0];
        }
        return Arrays.stream(navigator.getParentNids(childNid)).filter(result::isVisible).toArray();
    }

    @Override
    public long[] getChildNids(long parentNid) {
        if (!result.isVisible(parentNid)) {
            return new long[0];
        }
        return Arrays.stream(navigator.getChildNids(parentNid)).filter(result::isVisible).toArray();
    }

    @Override
    public ImmutableList<Edge> getParentEdges(long childNid) {
        return navigator.getParentEdges(childNid)
                .select(edge -> result.isVisible(childNid) && result.isVisible(edge.destinationNid()));
    }

    @Override
    public ImmutableList<Edge> getChildEdges(long parentNid) {
        return navigator.getChildEdges(parentNid)
                .select(edge -> result.isVisible(parentNid) && result.isVisible(edge.destinationNid()));
    }

    @Override
    public boolean isLeaf(long conceptNid) {
        // Asked for every row the tree draws. The filter result already knows the answer; reading the
        // concept's children to find it would sort them by name each time.
        return !result.hasVisibleChildren(conceptNid);
    }

    @Override
    public boolean isChildOf(long childNid, long parentNid) {
        return result.isVisible(childNid) && result.isVisible(parentNid) && navigator.isChildOf(childNid, parentNid);
    }

    @Override
    public boolean isDescendentOf(long descendantNid, long ancestorNid) {
        return result.isVisible(descendantNid) && result.isVisible(ancestorNid)
                && navigator.isDescendentOf(descendantNid, ancestorNid);
    }

    @Override
    public long[] getRootNids() {
        return navigator.getRootNids();
    }

    @Override
    public ViewCalculator getViewCalculator() {
        return navigator.getViewCalculator();
    }
}
