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
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

public class ObservableStampCoordinateWithOverride extends ObservableStampCoordinateBase {

    public ObservableStampCoordinateWithOverride(ObservableStampCoordinate stampFilter) {
        this(stampFilter, stampFilter.getName());
    }

    public ObservableStampCoordinateWithOverride(ObservableStampCoordinate stampFilter, String coordinateName) {
        super(stampFilter, coordinateName);
        // Depth-independent override nesting (ike-issues#663): an override may wrap another override.
    }

    @Override
    public LongPropertyWithOverride timeProperty() {
        return (LongPropertyWithOverride) super.timeProperty();
    }

    @Override
    public OverrideOf<ConceptFacade> pathConceptProperty() {
        return (OverrideOf) super.pathConceptProperty();
    }

    @Override
    public OverrideOf<ImmutableSet<ConceptFacade>> moduleSpecificationsProperty() {
        return (OverrideOf<ImmutableSet<ConceptFacade>>) super.moduleSpecificationsProperty();
    }

    @Override
    public OverrideOf<ImmutableSet<ConceptFacade>> excludedModuleSpecificationsProperty() {
        return (OverrideOf<ImmutableSet<ConceptFacade>>) super.excludedModuleSpecificationsProperty();
    }

    @Override
    public OverrideOf<ImmutableList<ConceptFacade>> modulePriorityOrderProperty() {
        return (OverrideOf<ImmutableList<ConceptFacade>>) super.modulePriorityOrderProperty();
    }

    @Override
    public OverrideOf<StateSet> allowedStatesProperty() {
        return (OverrideOf) super.allowedStatesProperty();
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
    protected SimpleEqualityBasedObjectProperty<ImmutableList<ConceptFacade>> makeModulePriorityOrderProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new OverrideOf<>(observableStampFilter.modulePriorityOrderProperty(), this);
    }

    @Override
    protected OverrideOf makeAllowedStatusProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new OverrideOf<>(observableStampFilter.allowedStatesProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableSet<ConceptFacade>> makeExcludedModuleSpecificationsProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new OverrideOf<>(observableStampFilter.excludedModuleSpecificationsProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableSet<ConceptFacade>> makeModuleSpecificationsProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new OverrideOf<>(observableStampFilter.moduleSpecificationsProperty(), this);
    }

    @Override
    protected LongPropertyWithOverride makeTimeProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new LongPropertyWithOverride(observableStampFilter.timeProperty(), this);
    }

    @Override
    protected OverrideOf<ConceptFacade> makePathConceptProperty(StampCoordinate stampCoordinate) {
        ObservableStampCoordinate observableStampFilter = (ObservableStampCoordinate) stampCoordinate;
        return new OverrideOf<>(observableStampFilter.pathConceptProperty(), this);
    }

    @Override
    public StampCoordinateRecord getOriginalValue() {
        return StampCoordinateRecord.make(this.allowedStatesProperty().getOriginalValue(),
                StampPositionRecord.make(timeProperty().getOriginalValue().longValue(),
                        pathConceptProperty().getOriginalValue()),
                IntIds.set.of(moduleSpecificationsProperty().getOriginalValue().castToSet(), EntityFacade::toNid),
                IntIds.set.of(excludedModuleSpecificationsProperty().getOriginalValue().castToSet(), EntityFacade::toNid),
                IntIds.list.of(modulePriorityOrderProperty().getOriginalValue().castToList(), EntityFacade::toNid));
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
            this.modulePriorityOrderProperty().setValue(newValue.modulePriorityNidList().map(ConceptFacade::make));
        }

        if (!this.allowedStatesProperty().isOverridden()) {
            if (newValue.allowedStates() != this.allowedStatesProperty().get()) {
                this.allowedStatesProperty().setValue(newValue.allowedStates());
            }
        }

        if (!this.excludedModuleSpecificationsProperty().isOverridden()) {
            this.excludedModuleSpecificationsProperty().setValue(newValue.excludedModuleNids().map(ConceptFacade::make));
        }
        if (!this.moduleSpecificationsProperty().isOverridden()) {
            this.moduleSpecificationsProperty().setValue(newValue.moduleNids().map(ConceptFacade::make));
        }
        return StampCoordinateRecord.make(this.allowedStatesProperty().get(),
                StampPositionRecord.make(timeProperty().get(),
                        pathConceptProperty().get().nid()),
                IntIds.set.of(moduleSpecificationsProperty().get().castToSet(), EntityFacade::toNid),
                IntIds.set.of(excludedModuleSpecificationsProperty().get().castToSet(), EntityFacade::toNid),
                IntIds.list.of(modulePriorityOrderProperty().getOriginalValue().castToList(), EntityFacade::toNid));
    }

}
