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

//~--- JDK imports ------------------------------------------------------------

import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableManifoldCoordinateImpl.
 *
 * 
 */
public class ObservableViewNoOverride extends ObservableViewBase {

    public ObservableViewNoOverride(ViewCoordinate viewRecord, String name) {
        super(viewRecord, name);
    }

    public ObservableViewNoOverride(ViewCoordinate viewRecord) {
        super(viewRecord);
    }

    public ViewProperties makeOverridableViewProperties() {
        return new ViewProperties(new ObservableViewWithOverride(this), this);
    }

    public ViewProperties makeOverridableViewProperties(String name) {
        return new ViewProperties(new ObservableViewWithOverride(this, name), this);
    }

    @Override
    protected ObservableStampCoordinateBase makeStampCoordinateObservable(ViewCoordinate viewRecord) {
        return new ObservableStampCoordinateNoOverride(viewRecord.stampCoordinate());
    }

    @Override
    protected ObservableNavigationCoordinateNoOverride makeNavigationCoordinateObservable(ViewCoordinate viewRecord) {
        return new ObservableNavigationCoordinateNoOverride(viewRecord.navigationCoordinate());
    }


    @Override
    protected ListProperty<ObservableLanguageCoordinateBase> makeLanguageCoordinateListProperty(ViewCoordinate viewRecord) {
        ObservableList<ObservableLanguageCoordinateBase> languageCoordinateList = FXCollections.observableArrayList();
        viewRecord.languageCoordinateIterable().forEach(languageCoordinateRecord ->
                languageCoordinateList.add(new ObservableLanguageCoordinateNoOverride(languageCoordinateRecord)));

        ListProperty<ObservableLanguageCoordinateBase> languageListProperty = new SimpleEqualityBasedListProperty<>(this,
                "", languageCoordinateList);
        return languageListProperty;
    }

    @Override
    protected ObservableLogicCoordinateBase makeLogicCoordinateObservable(ViewCoordinate viewRecord) {
        return new ObservableLogicCoordinateNoOverride(viewRecord.logicCoordinate());
    }

    @Override
    protected ObservableEditCoordinateBase makeEditCoordinateObservable(ViewCoordinate viewRecord) {
        return new ObservableEditCoordinateNoOverride(viewRecord);
    }

    public void removeOverrides() {
        // nothing to do, this coordinate cannot be overridden.
    }

    @Override
    public void setExceptOverrides(ViewCoordinateRecord viewRecord) {
        setValue(viewRecord.toViewCoordinateRecord());
    }

    @Override
    public ViewCoordinateRecord getOriginalValue() {
        return getValue();
    }

    /// Set the value of the language coordinates to the updatedCoordinate value
    private void setLanguageCoordinates(ViewCoordinateRecord updatedCoordinate) {
        for (int i = 0; i < languageCoordinates.size(); i++) {
            languageCoordinates.get(i).setValue(
                    updatedCoordinate.languageCoordinates().get(i).toLanguageCoordinateRecord());
        }
    }

    @Override
    protected ViewCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends ViewCoordinateRecord> observable,
                                                                         ViewCoordinateRecord oldValue, ViewCoordinateRecord newValue) {
        this.stampCoordinateObservable.setValue(newValue.stampCoordinate());

        setLanguageCoordinates(newValue);

        this.navigationCoordinateObservable.setValue(newValue.navigationCoordinate());
        this.logicCoordinateObservable.setValue(newValue.logicCoordinate());
        return newValue;
    }

}


