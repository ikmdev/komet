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

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import dev.ikm.komet.terms.KometTerm;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

public class ObservableStampCoordinateNoOverride extends ObservableStampCoordinateBase {


    private ObservableStampCoordinateNoOverride(StampCoordinateRecord stampCoordinate, String coordinateName) {
        super(stampCoordinate, coordinateName);
    }

    protected ObservableStampCoordinateNoOverride(StampCoordinate stampCoordinate) {
        super(stampCoordinate, "Stamp filter");
    }

    @Override
    protected StampCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampCoordinateRecord> observable, StampCoordinateRecord oldValue, StampCoordinateRecord newValue) {
        this.pathConceptProperty().setValue(newValue.pathForFilter());
        this.timeProperty().set(newValue.stampPosition().time());
        this.modulePriorityOrderProperty().setValue(newValue.modulePriorityNidList().map(ConceptFacade::make));

        if (newValue.allowedStates() != this.allowedStatesProperty().get()) {
            this.allowedStatesProperty().setValue(newValue.allowedStates());
        }

        this.excludedModuleSpecificationsProperty().setValue(newValue.excludedModuleNids().map(ConceptFacade::make));
        this.moduleSpecificationsProperty().setValue(newValue.moduleNids().map(ConceptFacade::make));
        return newValue;
    }

    @Override
    public void setExceptOverrides(StampCoordinateRecord updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<ConceptFacade>> makeModulePriorityOrderProperty(StampCoordinate stampCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.MODULE_PREFERENCE_ORDER_FOR_STAMP_COORDINATE.toXmlFragment(),
                stampCoordinate.modulePriorityNidList().map(ConceptFacade::make));
    }

    @Override
    protected LongProperty makeTimeProperty(StampCoordinate stampCoordinate) {
        return new SimpleLongProperty(this,
                KometTerm.POSITION_ON_PATH.toXmlFragment(),
                stampCoordinate.stampPosition().time());
    }

    @Override
    protected ObjectProperty<ConceptFacade> makePathConceptProperty(StampCoordinate stampCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.PATH_FOR_PATH_COORDINATE.toXmlFragment(),
                stampCoordinate.pathForFilter());
    }

    @Override
    protected ObjectProperty<StateSet> makeAllowedStatusProperty(StampCoordinate stampCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.ALLOWED_STATES_FOR_STAMP_COORDINATE.toXmlFragment(),
                stampCoordinate.allowedStates());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableSet<ConceptFacade>> makeExcludedModuleSpecificationsProperty(StampCoordinate stampCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.MODULE_EXCLUSION_SET_FOR_STAMP_COORDINATE.toXmlFragment(),
                stampCoordinate.excludedModuleNids().map(ConceptFacade::make));
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableSet<ConceptFacade>> makeModuleSpecificationsProperty(StampCoordinate stampCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.MODULES_FOR_STAMP_COORDINATE.toXmlFragment(),
                stampCoordinate.moduleNids().map(ConceptFacade::make));
    }

    public static ObservableStampCoordinateNoOverride make(StampCoordinate stampCoordinate) {
        return new ObservableStampCoordinateNoOverride(stampCoordinate.toStampCoordinateRecord());
    }

    public static ObservableStampCoordinateNoOverride make(StampCoordinate stampCoordinate, String coordinateName) {
        return new ObservableStampCoordinateNoOverride(stampCoordinate.toStampCoordinateRecord(), coordinateName);
    }

    @Override
    public StampCoordinateRecord getOriginalValue() {
        return getValue();
    }

}
