package dev.ikm.komet.framework.view;

import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinate;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;

public class ObservableNavigationCoordinateWithOverride extends ObservableNavigationCoordinateBase {

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate navigationCoordinate, String coordinateName) {
        super(navigationCoordinate, coordinateName);
        if (navigationCoordinate instanceof ObservableNavigationCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }

    }

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate navigationCoordinate) {
        this(navigationCoordinate, navigationCoordinate.getName());
    }

    @Override
    public SetPropertyWithOverride<PatternFacade> navigationPatternsProperty() {
        return (SetPropertyWithOverride<PatternFacade>) super.navigationPatternsProperty();
    }

    @Override
    public void setExceptOverrides(NavigationCoordinateRecord updatedCoordinate) {
        if (navigationPatternsProperty().isOverridden()) {
            this.setValue(NavigationCoordinateRecord.make(navigationPatternNids()));
        } else {
            this.setValue(updatedCoordinate);
        }
    }

    @Override
    protected SimpleEqualityBasedSetProperty<PatternFacade> makeNavigationPatternsProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinate observableNavigationCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new SetPropertyWithOverride<>(observableNavigationCoordinate.navigationPatternsProperty(), this);
    }

    @Override
    public ObjectPropertyWithOverride<StateSet> vertexStatesProperty() {
        return (ObjectPropertyWithOverride<StateSet>) super.vertexStatesProperty();
    }

    @Override
    protected ObjectPropertyWithOverride<StateSet> makeVertexStatesProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinate observableNavigationCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new ObjectPropertyWithOverride<>(observableNavigationCoordinate.vertexStatesProperty(), this);
    }

    @Override
    public ObjectPropertyWithOverride<Boolean> sortVerticesProperty() {
        return (ObjectPropertyWithOverride<Boolean>) super.sortVerticesProperty();
    }

    @Override
    protected ObjectPropertyWithOverride<Boolean> makeSortVerticesProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinate observableNavigationCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new ObjectPropertyWithOverride<>(observableNavigationCoordinate.sortVerticesProperty(), this) ;
    }

    @Override
    public ListPropertyWithOverride<PatternFacade> verticesSortPatternListProperty() {
        return (ListPropertyWithOverride<PatternFacade>) super.verticesSortPatternListProperty();
    }

    @Override
    protected ListProperty<PatternFacade> makeVerticesSortPatternListProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinate observableNavigationCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new ListPropertyWithOverride<>(observableNavigationCoordinate.verticesSortPatternListProperty(), this);
    }

    @Override
    public NavigationCoordinateRecord getOriginalValue() {
        /**
         IntIdSet navigationConceptNids,
         StateSet vertexStates,
         boolean sortVertices,
         IntIdList verticesSortPatternNidList
         */
        return NavigationCoordinateRecord.make(IntIds.set.of(navigationPatternsProperty().getOriginalValue().stream().mapToInt(value -> value.nid()).toArray()),
                vertexStatesProperty().getOriginalValue(),
                sortVerticesProperty().getOriginalValue(),
                IntIds.list.of(verticesSortPatternListProperty().stream()
                        .mapToInt(patternFacade -> patternFacade.nid()).toArray())
                );
    }


    @Override
    protected NavigationCoordinateRecord baseCoordinateChangedListenersRemoved(
            ObservableValue<? extends NavigationCoordinateRecord> observable,
            NavigationCoordinateRecord oldValue,
            NavigationCoordinateRecord newValue) {
        this.navigationPatternsProperty().setAll(newValue.navigationPatternNids()
                .map(nid -> (PatternFacade) EntityProxy.Pattern.make(nid)).toSet());
        this.vertexStatesProperty().set(newValue.vertexStates());
        this.sortVerticesProperty().set(newValue.sortVertices());
        this.verticesSortPatternListProperty().setAll(newValue.verticesSortPatternNidList().map(nid -> EntityProxy.Pattern.make(nid)).castToList());
        return newValue;
    }
}
