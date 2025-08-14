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

import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    public ObservableViewWithOverride(ObservableViewBase observableViewBase, String name) {
        super(observableViewBase, name);
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
        var name = super.getName();

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

        // convert the ObservableLanguageCoordinateNoOverride to ObservableLanguageCoordinateWithOverride
        // before putting into the ListPropertyWithOverride

        ObservableList<ObservableLanguageCoordinateBase> languageCoordinateList = FXCollections.observableArrayList();

        if (observableView.languageCoordinates() != null) {
            observableView.languageCoordinates().forEach(languageCoord -> {
                languageCoordinateList.add(new ObservableLanguageCoordinateWithOverride(languageCoord));
            });
        }

        ListProperty<ObservableLanguageCoordinateBase> languageListProperty = new SimpleEqualityBasedListProperty<>(this,
                "", languageCoordinateList);

        return new ListPropertyWithOverride<>(languageListProperty, this);
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
