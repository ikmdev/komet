package dev.ikm.komet.kview.controls;

import org.eclipse.collections.api.LongIterable;
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.LongFunction;

/**
 * The concepts a filtered concept navigator shows: the concepts that match the filter, and the
 * ancestors that are kept so each match can be found at its place in the hierarchy.
 * <p>
 * A match is kept only when it can be reached from a root of the hierarchy; a candidate with no
 * path to a root has no place in the tree, so it is neither shown nor counted.
 *
 * @see FilteredNavigator
 */
public final class ConceptFilterResult {

    /** The concepts that satisfy the filter and have a place in the hierarchy. */
    private final MutableLongSet matchingNids;
    /** The matching concepts and every ancestor of one: all that the tree shows. */
    private final MutableLongSet matchingAndAncestorNids;
    /** The shown concepts that have at least one shown concept under them. */
    private final MutableLongSet nidsWithVisibleChildren;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    private ConceptFilterResult(MutableLongSet matchingNids, MutableLongSet matchingAndAncestorNids, MutableLongSet nidsWithVisibleChildren) {
        this.matchingNids = matchingNids;
        this.matchingAndAncestorNids = matchingAndAncestorNids;
        this.nidsWithVisibleChildren = nidsWithVisibleChildren;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Works out what the tree shows for a set of candidate concepts in a hierarchy.
     *
     * @param candidates the nids that satisfy the filter; nids with no path to a root are dropped
     * @param parentsOf  the parents of a nid in the hierarchy, in any order
     * @param rootNids   the roots of the hierarchy
     * @return the matches that have a place in the hierarchy, with all their ancestors
     */
    public static ConceptFilterResult resolve(LongIterable candidates, LongFunction<long[]> parentsOf, long[] rootNids) {
        Resolver resolver = new Resolver(parentsOf, rootNids);
        candidates.forEach(resolver::addCandidate);
        return new ConceptFilterResult(resolver.matchingNids, resolver.matchingAndAncestorNids, resolver.nidsWithVisibleChildren);
    }

    /**
     * Whether the concept satisfies the filter and has a place in the hierarchy.
     *
     * @param nid the nid of a concept
     */
    public boolean isMatch(long nid) {
        return matchingNids.contains(nid);
    }

    /**
     * Whether the concept is shown: it is a match, or an ancestor of one.
     *
     * @param nid the nid of a concept
     */
    public boolean isVisible(long nid) {
        return matchingAndAncestorNids.contains(nid);
    }

    /**
     * Whether the concept is shown with at least one shown concept under it. Known from the walk
     * that found the ancestors, so it costs no reading of the concept's children.
     *
     * @param nid the nid of a concept
     */
    public boolean hasVisibleChildren(long nid) {
        return nidsWithVisibleChildren.contains(nid);
    }

    /**
     * The number of concepts that satisfy the filter and have a place in the hierarchy.
     */
    public int getMatchCount() {
        return matchingNids.size();
    }

    /**
     * The nids of the concepts that satisfy the filter and have a place in the hierarchy, in no
     * particular order.
     */
    public long[] getMatchNids() {
        return matchingNids.toArray();
    }

    /***************************************************************************
     *                                                                         *
     * Supporting Classes                                                      *
     *                                                                         *
     **************************************************************************/

    /** Walks up from each candidate, remembering which nids lead to a root and which do not. */
    private static final class Resolver {

        private final LongFunction<long[]> parentsOf;
        private final MutableLongSet matchingNids = LongSets.mutable.empty();
        private final MutableLongSet matchingAndAncestorNids = LongSets.mutable.empty();
        private final MutableLongSet nidsWithVisibleChildren = LongSets.mutable.empty();
        private final MutableLongSet reaching = LongSets.mutable.empty();
        private final MutableLongSet stranded = LongSets.mutable.empty();
        private final MutableLongSet walking = LongSets.mutable.empty();

        Resolver(LongFunction<long[]> parentsOf, long[] rootNids) {
            this.parentsOf = parentsOf;
            reaching.addAll(rootNids);
        }

        void addCandidate(long nid) {
            if (!reachesRoot(nid)) {
                return;
            }
            matchingNids.add(nid);
            Deque<Long> pending = new ArrayDeque<>();
            pending.push(nid);
            while (!pending.isEmpty()) {
                long current = pending.pop();
                if (matchingAndAncestorNids.add(current)) {
                    for (long parent : parentsOf.apply(current)) {
                        if (reachesRoot(parent)) {
                            nidsWithVisibleChildren.add(parent);
                            pending.push(parent);
                        }
                    }
                }
            }
        }

        /** A nid met again while it is still being walked is on a cycle, which leads nowhere. */
        private boolean reachesRoot(long nid) {
            if (reaching.contains(nid)) {
                return true;
            }
            if (stranded.contains(nid) || !walking.add(nid)) {
                return false;
            }
            boolean reaches = false;
            for (long parent : parentsOf.apply(nid)) {
                if (reachesRoot(parent)) {
                    reaches = true;
                }
            }
            walking.remove(nid);
            (reaches ? reaching : stranded).add(nid);
            return reaches;
        }
    }
}
