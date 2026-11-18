package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.kview.controls.ConceptFilterResult;
import dev.ikm.komet.kview.controls.FilteredNavigator;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.id.LongIdSet;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The concept navigator's Show filter, without a store: which concepts a {@link ConceptFilterResult}
 * keeps, and what a {@link FilteredNavigator} answers for them.
 * <pre>
 *   1 (root)
 *   ├─ 2
 *   │  ├─ 4      candidate
 *   │  ├─ 5
 *   │  └─ 6      candidate, also under 3
 *   └─ 3
 *      └─ 6
 *   7            candidate with no parents
 *   8 ⇄ 9        candidates that are each other's parent
 * </pre>
 */
class FilteredNavigatorTest {

    private final Hierarchy hierarchy = new Hierarchy()
            .child(1, 2).child(1, 3)
            .child(2, 4).child(2, 5).child(2, 6)
            .child(3, 6)
            .child(8, 9).child(9, 8);

    private final ConceptFilterResult result = ConceptFilterResult.resolve(LongSets.immutable.of(4, 6, 7, 8, 9),
            hierarchy::getParentNids, hierarchy.getRootNids());

    private final FilteredNavigator filtered = new FilteredNavigator(hierarchy, result);

    @Test
    void candidatesWithAPathToTheRootMatch() {
        assertTrue(result.isMatch(4));
        assertTrue(result.isMatch(6));
        assertEquals(2, result.getMatchCount());
        long[] matchNids = result.getMatchNids();
        Arrays.sort(matchNids);
        assertArrayEquals(new long[]{4, 6}, matchNids);
    }

    @Test
    void candidatesWithNoPathToTheRootAreDropped() {
        assertFalse(result.isMatch(7), "a candidate with no parents has no place in the tree");
        assertFalse(result.isVisible(7));
        assertFalse(result.isMatch(8), "a candidate on a cycle that never reaches the root has no place in the tree");
        assertFalse(result.isVisible(9));
    }

    @Test
    void ancestorsOnEveryPathAreKeptButAreNotMatches() {
        for (long ancestor : new long[]{1, 2, 3}) {
            assertTrue(result.isVisible(ancestor), ancestor + " leads to a match");
            assertFalse(result.isMatch(ancestor), ancestor + " is context, not a match");
        }
        assertFalse(result.isVisible(5), "a sibling that does not match is not kept");
    }

    @Test
    void childrenAreNarrowedToTheFilterResult() {
        assertArrayEquals(new long[]{2, 3}, filtered.getChildNids(1));
        assertArrayEquals(new long[]{4, 6}, filtered.getChildNids(2));
        assertArrayEquals(new long[]{4, 6},
                filtered.getChildEdges(2).collectLong(Edge::destinationNid).toSortedArray());
        assertArrayEquals(new long[]{6}, filtered.getChildNids(3));
    }

    @Test
    void aMatchWithNoMatchingDescendantsIsALeaf() {
        assertTrue(filtered.isLeaf(4));
        assertFalse(filtered.isLeaf(2));
    }

    @Test
    void aConceptIsALeafExactlyWhenItHasNoChildrenInTheFilterResult() {
        for (long nid = 1; nid <= 9; nid++) {
            assertEquals(filtered.getChildNids(nid).length == 0, filtered.isLeaf(nid), "leaf state of " + nid);
        }
    }

    @Test
    void theLeafStateIsAnsweredWithoutReadingChildren() {
        Hierarchy counting = new Hierarchy().child(1, 2).child(2, 4);
        ConceptFilterResult countedResult = ConceptFilterResult.resolve(LongSets.immutable.of(4), counting::getParentNids, counting.getRootNids());
        FilteredNavigator navigator = new FilteredNavigator(counting, countedResult);
        counting.childReads = 0;

        assertFalse(navigator.isLeaf(2));
        assertTrue(navigator.isLeaf(4));
        assertEquals(0, counting.childReads, "the tree asks for the leaf state of every row it draws");
    }

    @Test
    void aConceptOutsideTheFilterResultHasNoParentsAndNoChildren() {
        assertArrayEquals(new long[0], filtered.getParentNids(5));
        assertArrayEquals(new long[0], filtered.getChildNids(5));
        assertTrue(filtered.getParentEdges(5).isEmpty());
        assertFalse(filtered.isChildOf(5, 2));
    }

    @Test
    void aMatchKeepsAllItsParents() {
        assertArrayEquals(new long[]{2, 3}, filtered.getParentNids(6));
        assertTrue(filtered.isChildOf(6, 3));
    }

    @Test
    void theRootsAreTheNarrowedNavigatorsRoots() {
        assertArrayEquals(new long[]{1}, filtered.getRootNids());
    }

    @Test
    void aFilterNothingMatchesShowsNothingUnderTheRoot() {
        ConceptFilterResult empty = ConceptFilterResult.resolve(LongSets.immutable.of(7), hierarchy::getParentNids, hierarchy.getRootNids());
        FilteredNavigator nothing = new FilteredNavigator(hierarchy, empty);

        assertEquals(0, empty.getMatchCount());
        assertArrayEquals(new long[0], nothing.getChildNids(1));
        assertTrue(nothing.getChildEdges(1).isEmpty());
    }

    /** A navigator over parent and child links held in memory; node 1 is its root. */
    private static final class Hierarchy implements Navigator {

        private final Map<Long, long[]> children = new HashMap<>();
        private final Map<Long, long[]> parents = new HashMap<>();
        /** How many times the children of a concept were read. */
        private int childReads;

        Hierarchy child(long parentNid, long childNid) {
            children.merge(parentNid, new long[]{childNid}, Hierarchy::concat);
            parents.merge(childNid, new long[]{parentNid}, Hierarchy::concat);
            return this;
        }

        private static long[] concat(long[] first, long[] second) {
            long[] both = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, both, first.length, second.length);
            return both;
        }

        private static ImmutableList<Edge> edgesTo(long[] nids) {
            return Lists.immutable.ofAll(Arrays.stream(nids).<Edge>mapToObj(nid -> new Edge() {
                @Override
                public LongIdSet typeNids() {
                    return null;
                }

                @Override
                public long destinationNid() {
                    return nid;
                }

                @Override
                public String comparisonString() {
                    return Long.toString(nid);
                }
            }).toList());
        }

        @Override
        public long[] getParentNids(long childNid) {
            return parents.getOrDefault(childNid, new long[0]);
        }

        @Override
        public long[] getChildNids(long parentNid) {
            childReads++;
            return children.getOrDefault(parentNid, new long[0]);
        }

        @Override
        public ImmutableList<Edge> getParentEdges(long childNid) {
            return edgesTo(getParentNids(childNid));
        }

        @Override
        public ImmutableList<Edge> getChildEdges(long parentNid) {
            return edgesTo(getChildNids(parentNid));
        }

        @Override
        public boolean isLeaf(long conceptNid) {
            return getChildNids(conceptNid).length == 0;
        }

        @Override
        public boolean isChildOf(long childNid, long parentNid) {
            return Arrays.stream(getChildNids(parentNid)).anyMatch(nid -> nid == childNid);
        }

        @Override
        public boolean isDescendentOf(long descendantNid, long ancestorNid) {
            return Arrays.stream(getParentNids(descendantNid))
                    .anyMatch(parent -> parent == ancestorNid || isDescendentOf(parent, ancestorNid));
        }

        @Override
        public long[] getRootNids() {
            return new long[]{1};
        }

        @Override
        public ViewCalculator getViewCalculator() {
            return null;
        }
    }
}
