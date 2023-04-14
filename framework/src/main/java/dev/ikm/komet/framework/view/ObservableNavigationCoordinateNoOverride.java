package dev.ikm.komet.framework.view;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import dev.ikm.komet.terms.KometTerm;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinate;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.*;

public class ObservableNavigationCoordinateNoOverride extends ObservableNavigationCoordinateBase {

    public ObservableNavigationCoordinateNoOverride(NavigationCoordinate navigationCoordinate, String coordinateName) {
        super(navigationCoordinate, coordinateName);
    }

    public ObservableNavigationCoordinateNoOverride(NavigationCoordinate navigationCoordinate) {
        super(navigationCoordinate, "Navigation coordinate (fix)");
    }

    @Override
    public void setExceptOverrides(NavigationCoordinateRecord updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    protected SimpleEqualityBasedSetProperty<PatternFacade> makeNavigationPatternsProperty(NavigationCoordinate navigationCoordinate) {
        return new SimpleEqualityBasedSetProperty<>(this,
                KometTerm.NAVIGATION_CONCEPT_SET.toXmlFragment(),
                FXCollections.observableSet(navigationCoordinate.navigationPatternNids().mapToSet(PatternFacade::make)));
    }

    @Override
    protected ObjectProperty<StateSet> makeVertexStatesProperty(NavigationCoordinate navigationCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this, KometTerm.VERTEX_STATE_SET.toXmlFragment(), navigationCoordinate.vertexStates());
    }

    @Override
    protected ObjectProperty<Boolean> makeSortVerticesProperty(NavigationCoordinate navigationCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this, KometTerm.VERTEX_SORT.toXmlFragment(),
                navigationCoordinate.sortVertices());
    }

    @Override
    protected ListProperty<PatternFacade> makeVerticesSortPatternListProperty(NavigationCoordinate navigationCoordinate) {
        return new SimpleEqualityBasedListProperty<>(this, "Vertex Sort Patterns",
                FXCollections.observableArrayList(
                        navigationCoordinate.verticesSortPatternNidList().mapToList(EntityProxy.Pattern::make)));
    }

    @Override
    protected NavigationCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends NavigationCoordinateRecord> observable, NavigationCoordinateRecord oldValue, NavigationCoordinateRecord newValue) {
        this.navigationPatternsProperty().setAll(newValue.navigationPatternNids()
                .map(nid -> (PatternFacade) EntityProxy.Pattern.make(nid)).toSet());
        this.vertexStatesProperty().set(newValue.vertexStates());
        return newValue;
    }

    @Override
    public NavigationCoordinateRecord getOriginalValue() {
        return getValue();
    }
}
