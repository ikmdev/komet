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
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

public class ObservableNavigationCoordinateWithOverride extends ObservableNavigationCoordinateBase {

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate navigationCoordinate, String coordinateName) {
        super(navigationCoordinate, coordinateName);
        // Depth-independent override nesting (ike-issues#663): an override may wrap another override.

    }

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate navigationCoordinate) {
        this(navigationCoordinate, navigationCoordinate.getName());
    }

    @Override
    public OverrideOf<ImmutableSet<PatternFacade>> navigationPatternsProperty() {
        return (OverrideOf<ImmutableSet<PatternFacade>>) super.navigationPatternsProperty();
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
    protected SimpleEqualityBasedObjectProperty<ImmutableSet<PatternFacade>> makeNavigationPatternsProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinate observableNavigationCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new OverrideOf<>(observableNavigationCoordinate.navigationPatternsProperty(), this);
    }

    @Override
    public OverrideOf<StateSet> vertexStatesProperty() {
        return (OverrideOf<StateSet>) super.vertexStatesProperty();
    }

    @Override
    protected OverrideOf<StateSet> makeVertexStatesProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinate observableNavigationCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new OverrideOf<>(observableNavigationCoordinate.vertexStatesProperty(), this);
    }

    @Override
    public OverrideOf<Boolean> sortVerticesProperty() {
        return (OverrideOf<Boolean>) super.sortVerticesProperty();
    }

    @Override
    protected OverrideOf<Boolean> makeSortVerticesProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinate observableNavigationCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new OverrideOf<>(observableNavigationCoordinate.sortVerticesProperty(), this) ;
    }

    @Override
    public OverrideOf<ImmutableList<PatternFacade>> verticesSortPatternListProperty() {
        return (OverrideOf<ImmutableList<PatternFacade>>) super.verticesSortPatternListProperty();
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<PatternFacade>> makeVerticesSortPatternListProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinate observableNavigationCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new OverrideOf<>(observableNavigationCoordinate.verticesSortPatternListProperty(), this);
    }

    /**
     * Applies {@code coordinateWithOverrides} as this coordinate's override state: each dimension is
     * {@link OverrideOf#set set}, which pins it when the value differs from the inherited parent and clears
     * the pin (reverting to inheriting) when it equals the parent. Dimensions matching the parent stay
     * inherited, so cascade tracking is preserved.
     *
     * @param coordinateWithOverrides the desired resolved navigation coordinate
     */
    public void setOverrides(NavigationCoordinateRecord coordinateWithOverrides) {
        navigationPatternsProperty().setValue(coordinateWithOverrides.navigationPatternNids().map(PatternFacade::make));
        vertexStatesProperty().set(coordinateWithOverrides.vertexStates());
        sortVerticesProperty().set(coordinateWithOverrides.sortVertices());
        verticesSortPatternListProperty().setValue(coordinateWithOverrides.verticesSortPatternNidList().map(PatternFacade::make));
    }

    @Override
    public NavigationCoordinateRecord getOriginalValue() {
        /**
         IntIdSet navigationConceptNids,
         StateSet vertexStates,
         boolean sortVertices,
         IntIdList verticesSortPatternNidList
         */
        return NavigationCoordinateRecord.make(IntIds.set.of(navigationPatternsProperty().getOriginalValue().castToSet(), EntityFacade::toNid),
                vertexStatesProperty().getOriginalValue(),
                sortVerticesProperty().getOriginalValue(),
                IntIds.list.of(verticesSortPatternListProperty().get().castToList(), EntityFacade::toNid)
                );
    }


    @Override
    protected NavigationCoordinateRecord baseCoordinateChangedListenersRemoved(
            ObservableValue<? extends NavigationCoordinateRecord> observable,
            NavigationCoordinateRecord oldValue,
            NavigationCoordinateRecord newValue) {

        if (!this.navigationPatternsProperty().isOverridden()) {
            this.navigationPatternsProperty().setValue(newValue.navigationPatternNids().map(PatternFacade::make));
        }

        if (!this.vertexStatesProperty().isOverridden()) {
            this.vertexStatesProperty().set(newValue.vertexStates());
        }

        if (!this.sortVerticesProperty().isOverridden()) {
            this.sortVerticesProperty().set(newValue.sortVertices());
        }

        if (!this.verticesSortPatternListProperty().isOverridden()) {
            this.verticesSortPatternListProperty().setValue(newValue.verticesSortPatternNidList().map(PatternFacade::make));
        }

        return newValue;
    }
}
