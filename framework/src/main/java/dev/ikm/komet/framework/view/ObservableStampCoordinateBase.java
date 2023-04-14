package dev.ikm.komet.framework.view;

import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.ConceptFacade;

public abstract class ObservableStampCoordinateBase
        extends ObservableCoordinateAbstract<StampCoordinateRecord>
        implements ObservableStampCoordinate {

    private final LongProperty timeProperty;

    /**
     *
     * @return the property that identifies the path concept for this path coordinate
     */
    private final ObjectProperty<ConceptFacade> pathConceptProperty;

    private final SetProperty<ConceptFacade> moduleSpecificationsProperty;

    private final SetProperty<ConceptFacade> excludedModuleSpecificationsProperty;

    private final ObjectProperty<StateSet> allowedStatusProperty;

    private final ListProperty<ConceptFacade> modulePriorityOrderProperty;

    /**
     * Remove listener note...
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptFacade> pathConceptListener = this::pathConceptChanged;
    private final ChangeListener<Number> timeListener = this::timeChanged;
    private final ChangeListener<StateSet> statusSetListener = this::statusSetChanged;
    private final ListChangeListener<ConceptFacade> modulePreferenceOrderListener = this::modulePreferenceOrderChanged;
    private final SetChangeListener<ConceptFacade> excludedModuleSetListener = this::excludedModuleSetChanged;
    private final SetChangeListener<ConceptFacade> moduleSetListener = this::moduleSetChanged;

    protected ObservableStampCoordinateBase(StampCoordinate stampCoordinate, String coordinateName) {
        super(stampCoordinate.toStampCoordinateRecord(), coordinateName);
        this.pathConceptProperty = makePathConceptProperty(stampCoordinate);
        this.timeProperty = makeTimeProperty(stampCoordinate);
        this.moduleSpecificationsProperty = makeModuleSpecificationsProperty(stampCoordinate);
        this.excludedModuleSpecificationsProperty = makeExcludedModuleSpecificationsProperty(stampCoordinate);
        this.allowedStatusProperty = makeAllowedStatusProperty(stampCoordinate);
        this.modulePriorityOrderProperty = makeModulePriorityOrderProperty(stampCoordinate);
        addListeners();
    }

    protected abstract ListProperty<ConceptFacade> makeModulePriorityOrderProperty(StampCoordinate stampCoordinate);

    protected abstract ObjectProperty<StateSet> makeAllowedStatusProperty(StampCoordinate stampCoordinate);

    protected abstract SetProperty<ConceptFacade> makeExcludedModuleSpecificationsProperty(StampCoordinate stampCoordinate);

    protected abstract SetProperty<ConceptFacade> makeModuleSpecificationsProperty(StampCoordinate stampCoordinate);

    protected abstract LongProperty makeTimeProperty(StampCoordinate stampCoordinate);

    protected abstract ObjectProperty<ConceptFacade> makePathConceptProperty(StampCoordinate stampCoordinate);

    @Override
    protected void addListeners() {
        this.pathConceptProperty.addListener(this.pathConceptListener);
        this.timeProperty.addListener(this.timeListener);
        this.allowedStatusProperty.addListener(this.statusSetListener);
        this.modulePriorityOrderProperty.addListener(this.modulePreferenceOrderListener);
        this.excludedModuleSpecificationsProperty.addListener(this.excludedModuleSetListener);
        this.moduleSpecificationsProperty.addListener(this.moduleSetListener);
    }

    @Override
    protected void removeListeners() {
        this.pathConceptProperty.removeListener(this.pathConceptListener);
        this.timeProperty.removeListener(this.timeListener);
        this.allowedStatusProperty.removeListener(this.statusSetListener);
        this.modulePriorityOrderProperty.removeListener(this.modulePreferenceOrderListener);
        this.excludedModuleSpecificationsProperty.removeListener(this.excludedModuleSetListener);
        this.moduleSpecificationsProperty.removeListener(this.moduleSetListener);
    }

    private void timeChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newTime) {
        this.setValue(StampCoordinateRecord.make(allowedStates(),
                StampPositionRecord.make(newTime.longValue(), pathNidForFilter()),
                moduleNids(),
                modulePriorityNidList()));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptFacade> observablePathConcept,
                                    ConceptFacade oldPathConcept,
                                    ConceptFacade newPathConcept) {
        this.setValue(StampCoordinateRecord.make(allowedStates(),
                StampPositionRecord.make(timeProperty.longValue(), newPathConcept.nid()),
                moduleNids(),
                modulePriorityNidList()));
    }

    @Override
    public LongProperty timeProperty() {
        return this.timeProperty;
    }

    @Override
    public ObjectProperty<ConceptFacade> pathConceptProperty() {
        return this.pathConceptProperty;
    }

    @Override
    public SetProperty<ConceptFacade> moduleSpecificationsProperty() {
        return this.moduleSpecificationsProperty;
    }

    /**
     *
     * @return the specified modules property
     */
    public SetProperty<ConceptFacade> excludedModuleSpecificationsProperty() {
        return this.excludedModuleSpecificationsProperty;
    }


    @Override
    public ListProperty<ConceptFacade> modulePriorityOrderProperty() {
        return this.modulePriorityOrderProperty;
    }

    /**
     * Allowed states property.
     *
     * @return the set property
     */
    @Override
    public ObjectProperty<StateSet> allowedStatesProperty() {
        return this.allowedStatusProperty;
    }

    @Override
    public StampCoordinate getStampFilter() {
        return getValue();
    }

    private void moduleSetChanged(SetChangeListener.Change<? extends ConceptFacade> c) {
        this.setValue(StampCoordinateRecord.make(allowedStates(),
                stampPosition(),
                IntIds.set.of(c.getSet().stream().mapToInt(value -> value.nid()).toArray()),
                excludedModuleNids(),
                modulePriorityNidList()));
    }

    private void excludedModuleSetChanged(SetChangeListener.Change<? extends ConceptFacade> c) {
        this.setValue(StampCoordinateRecord.make(allowedStates(),
                stampPosition(),
                moduleNids(),
                IntIds.set.of(c.getSet().stream().mapToInt(value -> value.nid()).toArray()),
                modulePriorityNidList()));
    }

    private void modulePreferenceOrderChanged(ListChangeListener.Change<? extends ConceptFacade> c) {
        this.setValue(StampCoordinateRecord.make(allowedStates(),
                stampPosition(),
                moduleNids(),
                excludedModuleNids(),
                IntIds.list.of(c.getList().stream().mapToInt(value -> value.nid()).toArray())));
    }

    private void statusSetChanged(ObservableValue<? extends StateSet> observableStatusSet,
                                  StateSet oldStatusSet,
                                  StateSet newStatusSet) {
        this.setValue(StampCoordinateRecord.make(newStatusSet,
                stampPosition(),
                moduleNids(),
                excludedModuleNids(),
                modulePriorityNidList()));
    }

    @Override
    public IntIdSet excludedModuleNids() {
        return getValue().excludedModuleNids();
    }

}