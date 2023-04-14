package dev.ikm.komet.framework.view;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import dev.ikm.komet.terms.KometTerm;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.Set;

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
        this.modulePriorityOrderProperty().setAll(newValue.modulePriorityNidList().map(nid -> EntityProxy.Concept.make(nid)).castToList());

        if (newValue.allowedStates() != this.allowedStatesProperty().get()) {
            this.allowedStatesProperty().setValue(newValue.allowedStates());
        }

        Set<? extends ConceptFacade> excludedModuleSet = newValue.excludedModuleNids().map(nid -> EntityProxy.Concept.make(nid)).castToSet();
        if (!excludedModuleSet.equals(this.excludedModuleSpecificationsProperty().get())) {
            this.excludedModuleSpecificationsProperty().retainAll(excludedModuleSet);
            this.excludedModuleSpecificationsProperty().addAll(excludedModuleSet);
        }

        Set<? extends ConceptFacade> moduleSet = newValue.moduleNids().map(nid -> EntityProxy.Concept.make(nid)).castToSet();
        if (!moduleSet.equals(this.moduleSpecificationsProperty().get())) {
            this.moduleSpecificationsProperty().retainAll(moduleSet);
            this.moduleSpecificationsProperty().addAll(moduleSet);
        }
        return newValue;
    }

    @Override
    public void setExceptOverrides(StampCoordinateRecord updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    protected ListProperty<ConceptFacade> makeModulePriorityOrderProperty(StampCoordinate stampCoordinate) {
        return new SimpleEqualityBasedListProperty<>(this,
                KometTerm.MODULE_PREFERENCE_ORDER_FOR_STAMP_COORDINATE.toXmlFragment(),
                FXCollections.observableArrayList(stampCoordinate.modulePriorityNidList().mapToList(EntityProxy.Concept::make)));
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
    protected SetProperty<ConceptFacade> makeExcludedModuleSpecificationsProperty(StampCoordinate stampCoordinate) {
        return new SimpleEqualityBasedSetProperty<>(this,
                KometTerm.MODULE_EXCLUSION_SET_FOR_STAMP_COORDINATE.toXmlFragment(),
                FXCollections.observableSet(stampCoordinate.excludedModuleNids().mapToSet(ConceptFacade::make)));
    }

    @Override
    protected SetProperty<ConceptFacade> makeModuleSpecificationsProperty(StampCoordinate stampCoordinate) {
        return new SimpleEqualityBasedSetProperty<>(this,
                KometTerm.MODULES_FOR_STAMP_COORDINATE.toXmlFragment(),
                FXCollections.observableSet(stampCoordinate.moduleNids().mapToSet(ConceptFacade::make)));
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
