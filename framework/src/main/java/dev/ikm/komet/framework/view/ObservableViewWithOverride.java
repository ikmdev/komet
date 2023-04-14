package dev.ikm.komet.framework.view;

import javafx.beans.value.ObservableValue;
import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;

public class ObservableViewWithOverride extends ObservableViewBase {

    public ObservableViewWithOverride(ObservableViewBase observableViewBase) {
        super(observableViewBase);
        if (observableViewBase instanceof ObservableViewWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }
        observableViewBase.baseCoordinateProperty().addListener(this::overriddenBaseChanged);
        observableViewBase.listening.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.addListeners();
            } else {
                this.removeListeners();
            }

        });
    }

    private void overriddenBaseChanged(ObservableValue<? extends ViewCoordinateRecord> observableValue,
                                       ViewCoordinateRecord oldValue,
                                       ViewCoordinateRecord newValue) {
        if (this.hasOverrides()) {
            setExceptOverrides(newValue);
        } else {
            setValue(newValue);
        }
    }

    @Override
    public void setExceptOverrides(ViewCoordinateRecord updatedCoordinate) {
        if (hasOverrides()) {
            stampCoordinate().setExceptOverrides(updatedCoordinate.stampCoordinate());
            setLanguageCoordinatesExceptOverrides(updatedCoordinate);
            navigationCoordinate().setExceptOverrides(updatedCoordinate.navigationCoordinate());
            logicCoordinate().setExceptOverrides(updatedCoordinate.logicCoordinate());
        } else {
            this.setValue(updatedCoordinate);
        }
    }

    private void setLanguageCoordinatesExceptOverrides(ViewCoordinateRecord updatedCoordinate) {
        if (!languageCoordinates().isOverridden()) {
            for (int i = 0; i < languageCoordinates.size(); i++) {
                languageCoordinates.get(i).setExceptOverrides(
                        updatedCoordinate.languageCoordinates().get(i).toLanguageCoordinateRecord());
            }
        }
    }

    @Override
    public ViewCoordinateRecord getOriginalValue() {
        return ViewCoordinateRecord.make(
                this.stampCoordinate().getOriginalValue(),
                this.languageCoordinates().getOriginalValue(),
                this.logicCoordinate().getOriginalValue(),
                this.navigationCoordinate().getOriginalValue(),
                this.editCoordinate().getOriginalValue());
    }

    public void setOverrides(ViewCoordinateRecord coordinateWithOverrides) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ObservableStampCoordinateBase makeStampCoordinateObservable(ViewCoordinate viewRecord) {
        ObservableView observableView = (ObservableView) viewRecord;
        return new ObservableStampCoordinateWithOverride(observableView.stampCoordinate());
    }

    @Override
    protected ObservableNavigationCoordinateBase makeNavigationCoordinateObservable(ViewCoordinate viewRecord) {
        ObservableView observableView = (ObservableView) viewRecord;
        return new ObservableNavigationCoordinateWithOverride(observableView.navigationCoordinate());
    }

    @Override
    protected ListPropertyWithOverride<ObservableLanguageCoordinateBase> makeLanguageCoordinateListProperty(ViewCoordinate viewRecord) {
        ObservableView observableView = (ObservableView) viewRecord;
        return new ListPropertyWithOverride(observableView.languageCoordinates(), this);
    }

    @Override
    protected ObservableLogicCoordinateBase makeLogicCoordinateObservable(ViewCoordinate viewRecord) {
        ObservableView observableView = (ObservableView) viewRecord;
        return new ObservableLogicCoordinateWithOverride(observableView.logicCoordinate());
    }

    @Override
    protected ObservableEditCoordinateBase makeEditCoordinateObservable(ViewCoordinate viewRecord) {
        ObservableView observableView = (ObservableView) viewRecord;
        return new ObservableEditCoordinateWithOverride(observableView.editCoordinate());
    }

    @Override
    public ObservableLogicCoordinateWithOverride logicCoordinate() {
        return (ObservableLogicCoordinateWithOverride) this.logicCoordinateObservable;
    }

    @Override
    public ObservableNavigationCoordinateWithOverride navigationCoordinate() {
        return (ObservableNavigationCoordinateWithOverride) this.navigationCoordinateObservable;
    }

    @Override
    public ListPropertyWithOverride<ObservableLanguageCoordinateBase> languageCoordinates() {
        return (ListPropertyWithOverride<ObservableLanguageCoordinateBase>) this.languageCoordinates;
    }

    @Override
    public ObservableStampCoordinateWithOverride stampCoordinate() {
        return (ObservableStampCoordinateWithOverride) this.stampCoordinateObservable;
    }

    @Override
    protected ViewCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends ViewCoordinateRecord> observable,
                                                                         ViewCoordinateRecord oldValue, ViewCoordinateRecord newValue) {
        this.stampCoordinateObservable.setExceptOverrides(newValue.stampCoordinate());
        setLanguageCoordinatesExceptOverrides(newValue);
        this.logicCoordinateObservable.setExceptOverrides(newValue.logicCoordinate());
        this.navigationCoordinateObservable.setExceptOverrides(newValue.navigationCoordinate());
        return ViewCoordinateRecord.make(this.stampCoordinateObservable.getValue(),
                this.languageCoordinates.getValue(),
                this.logicCoordinateObservable.getValue(),
                this.navigationCoordinateObservable.getValue(),
                this.editCoordinateObservable.getValue());
    }

}
