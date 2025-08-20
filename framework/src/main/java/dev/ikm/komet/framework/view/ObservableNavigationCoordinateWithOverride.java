/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.view;

import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinate;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;

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

        if (!this.navigationPatternsProperty().isOverridden()) {
            this.navigationPatternsProperty().setAll(newValue.navigationPatternNids()
                    .map(nid -> (PatternFacade) EntityProxy.Pattern.make(nid)).toSet());
        }

        if (!this.vertexStatesProperty().isOverridden()) {
            this.vertexStatesProperty().set(newValue.vertexStates());
        }

        if (!this.sortVerticesProperty().isOverridden()) {
            this.sortVerticesProperty().set(newValue.sortVertices());
        }

        if (!this.verticesSortPatternListProperty().isOverridden()) {
            this.verticesSortPatternListProperty().setAll(
                    newValue.verticesSortPatternNidList().map(nid -> EntityProxy.Pattern.make(nid)).castToList());
        }

        return newValue;
    }
}
