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

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.set.ImmutableSet;

public class ObservableStampCoordinateWithOverride extends ObservableStampCoordinateBase {

    public ObservableStampCoordinateWithOverride(ObservableStampCoordinate stampFilter) {
        this(stampFilter, stampFilter.getName());
    }

    public ObservableStampCoordinateWithOverride(ObservableStampCoordinate stampFilter, String coordinateName) {
        super(stampFilter, coordinateName);
        if (stampFilter instanceof ObservableStampCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }
    }

    @Override
    public LongPropertyWithOverride timeProperty() {
        return (LongPropertyWithOverride) super.timeProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> pathConceptProperty() {
        return (ObjectPropertyWithOverride) super.pathConceptProperty();
    }

    @Override
    public SetPropertyWithOverride<ConceptFacade> moduleSpecificationsProperty() {
        return (SetPropertyWithOverride) super.moduleSpecificationsProperty();
    }

    @Override
    public SetPropertyWithOverride<ConceptFacade> excludedModuleSpecificationsProperty() {
        return (SetPropertyWithOverride) super.excludedModuleSpecificationsProperty();
    }

    @Override
    public ListPropertyWithOverride<ConceptFacade> modulePriorityOrderProperty() {
        return (ListPropertyWithOverride) super.modulePriorityOrderProperty();
    }

    @Override
    public ObjectPropertyWithOverride<StateSet> allowedStatesProperty() {
        return (ObjectPropertyWithOverride) super.allowedStatesProperty();
    }

    @Override
    public void setExceptOverrides(StampCoordinateRecord updatedCoordinate) {
        if (this.hasOverrides()) {
            long time = updatedCoordinate.time();
            if (timeProperty().isOverridden()) {
                time = time();
            }
            int pathConceptNid = updatedCoordinate.pathNidForFilter();
            if (pathConceptProperty().isOverridden()) {
                pathConceptNid = pathConceptProperty().get().nid();
            }
            IntIdSet moduleSpecificationNids = updatedCoordinate.moduleNids();
            if (moduleSpecificationsProperty().isOverridden()) {
                moduleSpecificationNids = moduleNids();
            }
            IntIdSet moduleExclusionNids = updatedCoordinate.excludedModuleNids();
            if (excludedModuleSpecificationsProperty().isOverridden()) {
                moduleExclusionNids = excludedModuleNids();
            }
            IntIdList modulePriorityOrder = updatedCoordinate.modulePriorityNidList();
            if (modulePriorityOrderProperty().isOverridden()) {
                modulePriorityOrder = modulePriorityNidList();
            }
            StateSet StateSet = updatedCoordinate.allowedStates();
            if (this.allowedStatesProperty().isOverridden()) {
                StateSet = allowedStates();
            }
            setValue(StampCoordinateRecord.make(StateSet,
                    StampPositionRecord.make(time, pathConceptNid),
                    moduleSpecificationNids,
                    moduleExclusionNids,
                    modulePriorityOrder));

        } else {
            setValue(updatedCoordinate);
        }
    }

    @Override
    protected ListPropertyWithOverride<ConceptFacade> makeModulePriorityOrderProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new ListPropertyWithOverride<>(observableStampFilter.modulePriorityOrderProperty(), this);
    }

    @Override
    protected ObjectPropertyWithOverride makeAllowedStatusProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new ObjectPropertyWithOverride<>(observableStampFilter.allowedStatesProperty(), this);
    }

    @Override
    protected SetPropertyWithOverride<ConceptFacade> makeExcludedModuleSpecificationsProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new SetPropertyWithOverride<>(observableStampFilter.excludedModuleSpecificationsProperty(), this);
    }

    @Override
    protected SetPropertyWithOverride<ConceptFacade> makeModuleSpecificationsProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new SetPropertyWithOverride<>(observableStampFilter.moduleSpecificationsProperty(), this);
    }

    @Override
    protected LongPropertyWithOverride makeTimeProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new LongPropertyWithOverride(observableStampFilter.timeProperty(), this);
    }

    @Override
    protected ObjectPropertyWithOverride<ConceptFacade> makePathConceptProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new ObjectPropertyWithOverride<>(observableStampFilter.pathConceptProperty(), this);
    }

    @Override
    public StampCoordinateRecord getOriginalValue() {
        return StampCoordinateRecord.make(this.allowedStatesProperty().getOriginalValue(),
                StampPositionRecord.make(timeProperty().getOriginalValue().longValue(),
                        pathConceptProperty().getOriginalValue()),
                IntIds.set.of(moduleSpecificationsProperty().getOriginalValue().stream().mapToInt(value -> value.nid()).toArray()),
                IntIds.set.of(excludedModuleSpecificationsProperty().getOriginalValue().stream().mapToInt(value -> value.nid()).toArray()),
                IntIds.list.of(modulePriorityOrderProperty().getOriginalValue().stream().mapToInt(value -> value.nid()).toArray()));
    }


    @Override
    protected StampCoordinateRecord baseCoordinateChangedListenersRemoved(
            ObservableValue<? extends StampCoordinateRecord> observable,
            StampCoordinateRecord oldValue, StampCoordinateRecord newValue) {
        if (!this.pathConceptProperty().isOverridden()) {
            this.pathConceptProperty().setValue(newValue.pathForFilter());
        }

        if (!this.timeProperty().isOverridden()) {
            this.timeProperty().set(newValue.stampPosition().time());
        }

        if (!this.modulePriorityOrderProperty().isOverridden()) {
            this.modulePriorityOrderProperty().setAll(newValue.modulePriorityNidList().map(nid -> EntityProxy.Concept.make(nid)).castToList());
        }

        if (!this.allowedStatesProperty().isOverridden()) {
            if (newValue.allowedStates() != this.allowedStatesProperty().get()) {
                this.allowedStatesProperty().setValue(newValue.allowedStates());
            }
        }

        if (!this.excludedModuleSpecificationsProperty().isOverridden()) {
            ImmutableSet<ConceptFacade> excludedModuleSet = newValue.excludedModuleNids().map(nid -> EntityProxy.Concept.make(nid));
            if (!excludedModuleSet.equals(this.excludedModuleSpecificationsProperty().get())) {
                this.excludedModuleSpecificationsProperty().setAll(excludedModuleSet.castToSet());
            }
        }
        if (!this.moduleSpecificationsProperty().isOverridden()) {
            ImmutableSet<ConceptFacade> moduleSet = newValue.moduleNids().map(nid -> EntityProxy.Concept.make(nid));
            if (!moduleSet.equals(this.moduleSpecificationsProperty().get())) {
                this.moduleSpecificationsProperty().setAll(moduleSet.castToSet());
            }
        }
        return StampCoordinateRecord.make(this.allowedStatesProperty().get(),
                StampPositionRecord.make(timeProperty().get(),
                        pathConceptProperty().get().nid()),
                IntIds.set.of(moduleSpecificationsProperty().stream().mapToInt(value -> value.nid()).toArray()),
                IntIds.set.of(excludedModuleSpecificationsProperty().stream().mapToInt(value -> value.nid()).toArray()),
                IntIds.list.of(modulePriorityOrderProperty().getOriginalValue().stream().mapToInt(value -> value.nid()).toArray()));
    }

}
