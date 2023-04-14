package dev.ikm.komet.navigator.graph;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.ArrayList;

public class EmptyNavigator implements Navigator {
    final ArrayList<Navigator> navigators = new ArrayList<>();
    final ArrayList<Navigator> reverseNavigators = new ArrayList<>();
    final ArrayList<ConceptFacade> roots = new ArrayList<>();
    final ViewCoordinateRecord viewCoordinateRecord;

    public EmptyNavigator(ViewCoordinate viewCoordinate) {
        if (viewCoordinate == null) {
            throw new NullPointerException("manifoldCoordinate cannot be null. ");
        }
        this.viewCoordinateRecord = viewCoordinate.toViewCoordinateRecord();
    }

    @Override
    public ViewCalculator getViewCalculator() {
        return ViewCalculatorWithCache.getCalculator(viewCoordinateRecord);
    }

    public ArrayList<ConceptFacade> getRoots() {
        return roots;
    }

    public ArrayList<Navigator> getNavigators() {
        return navigators;
    }

    public ArrayList<Navigator> getReverseNavigators() {
        return reverseNavigators;
    }

    @Override
    public int[] getChildNids(int parentNid) {
        return new int[0];
    }


    @Override
    public int[] getParentNids(int childNid) {
        return new int[0];
    }

    @Override
    public int[] getRootNids() {
        if (roots.isEmpty()) {
            return new int[]{TinkarTerm.UNINITIALIZED_COMPONENT.nid()};
        }
        return roots.stream().mapToInt(value -> value.nid()).toArray();
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        return false;
    }

    @Override
    public boolean isLeaf(int conceptNid) {
        return true;
    }

    @Override
    public boolean isDescendentOf(int descendantNid, int ancestorNid) {
        throw new UnsupportedOperationException();
    }

    public ViewCoordinateRecord getViewCoordinateRecord() {
        return this.viewCoordinateRecord;
    }

    @Override
    public ImmutableList<Edge> getParentEdges(int parentConceptNid) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Edge> getChildEdges(int childConceptNid) {
        return Lists.immutable.empty();
    }

    public void reset() {
        this.navigators.clear();
        this.reverseNavigators.clear();
        this.roots.clear();
    }
}
