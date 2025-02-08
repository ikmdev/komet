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

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.terms.ConceptFacade;

public abstract class ObservableViewBase
        extends ObservableCoordinateAbstract<ViewCoordinateRecord>
        implements ObservableView {

    protected final SimpleBooleanProperty listening = new SimpleBooleanProperty(this, "Listening for changes", false);

    protected final ObservableStampCoordinateBase stampCoordinateObservable;

    protected final ListProperty<ObservableLanguageCoordinateBase> languageCoordinates;

    protected final ObservableNavigationCoordinateBase navigationCoordinateObservable;

    protected final ObservableLogicCoordinateBase logicCoordinateObservable;

    protected final ObservableEditCoordinateBase editCoordinateObservable;
    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     *
     * TODO: updating to use the new Subscription interface (since JavaFX 21 https://download.java.net/java/early_access/javafx23/docs/api/javafx.base/javafx/util/Subscription.html)
     * may be a better solution, and worth a refactor.
     */
    private final ChangeListener<StampCoordinateRecord> stampChangeListener = this::stampChanged;
    private final ChangeListener<NavigationCoordinateRecord> navigationChangedListener = this::navigationChanged;
    private final ListChangeListener<ObservableLanguageCoordinateBase> languageCoordinateListListener = this::languageCoordinateListChanged;
    private final ChangeListener<LanguageCoordinateRecord>  languageCoordinateChangeListener = this::languageCoordinateChanged;

    private final ChangeListener<LogicCoordinateRecord> logicCoordinateListener = this::logicChanged;
    private final ChangeListener<EditCoordinateRecord> editCoordinateListener = this::editChanged;

    private ViewCalculator viewCalculator;

    /**
     * Instantiates a new observable taxonomy coordinate impl.
     *
     * @param viewRecord the taxonomy coordinate
     */
    public ObservableViewBase(ViewCoordinate viewRecord) {
        this(viewRecord, "View");
    }

    //~--- constructors --------------------------------------------------------
    public ObservableViewBase(ViewCoordinate viewRecord, String name) {
        super(viewRecord.toViewCoordinateRecord(), name);
        this.stampCoordinateObservable = makeStampCoordinateObservable(viewRecord);
        this.navigationCoordinateObservable = makeNavigationCoordinateObservable(viewRecord);
        this.languageCoordinates = makeLanguageCoordinateListProperty(viewRecord);
        this.logicCoordinateObservable = makeLogicCoordinateObservable(viewRecord);
        this.editCoordinateObservable = makeEditCoordinateObservable(viewRecord);
        addListeners();
        this.viewCalculator = ViewCalculatorWithCache.getCalculator(viewRecord.toViewCoordinateRecord());
        addListener((observable, oldValue, newValue) -> {
            this.viewCalculator = ViewCalculatorWithCache.getCalculator(newValue);
        });
    }

    protected abstract ObservableStampCoordinateBase makeStampCoordinateObservable(ViewCoordinate viewRecord);

    protected abstract ObservableNavigationCoordinateBase makeNavigationCoordinateObservable(ViewCoordinate viewRecord);

    protected abstract ListProperty<ObservableLanguageCoordinateBase> makeLanguageCoordinateListProperty(ViewCoordinate viewRecord);

    protected abstract ObservableLogicCoordinateBase makeLogicCoordinateObservable(ViewCoordinate viewRecord);

    protected abstract ObservableEditCoordinateBase makeEditCoordinateObservable(ViewCoordinate viewRecord);

    @Override
    public ImmutableList<LanguageCoordinateRecord> languageCoordinateList() {
        return getValue().languageCoordinateList();
    }

    @Override
    public ViewCoordinateRecord toViewCoordinateRecord() {
        return getValue();
    }

    @Override
    public Iterable<ObservableLanguageCoordinateBase> languageCoordinateIterable() {
        return this.languageCoordinates;
    }    @Override
    public ViewCalculator calculator() {
        return ViewCalculatorWithCache.getCalculator(getValue());
    }

    private void languageCoordinateListChanged(ListChangeListener.Change<? extends ObservableLanguageCoordinateBase> c) {
        MutableList<LanguageCoordinateRecord> languageRecordList = Lists.mutable.empty();
        c.getList().forEach(observableLanguageCoordinateBase -> languageRecordList.add(observableLanguageCoordinateBase.getValue()));
        this.setValue(this.getValue().withLanguageCoordinateList(languageRecordList.toImmutable()));
    }

    private void languageCoordinateChanged(ObservableValue<? extends LanguageCoordinateRecord> observableValue,
                                           LanguageCoordinateRecord oldValue,
                                           LanguageCoordinateRecord newValue) {
        LOG.info("Language coordinate changed: {}", newValue);
        MutableList<LanguageCoordinateRecord> languageRecordList = Lists.mutable.empty();
        languageCoordinates.forEach(observableLanguageCoordinateBase -> languageRecordList.add(observableLanguageCoordinateBase.getValue()));
        this.setValue(this.getValue().withLanguageCoordinateList(languageRecordList.toImmutable()));
    }

    private void stampChanged(ObservableValue<? extends StampCoordinateRecord> observable,
                              StampCoordinateRecord oldValue,
                              StampCoordinateRecord newValue) {
        this.setValue(getValue().withStampCoordinate(newValue.toStampCoordinateRecord()));
    }

    private void navigationChanged(ObservableValue<? extends NavigationCoordinateRecord> observable,
                                   NavigationCoordinateRecord oldValue,
                                   NavigationCoordinateRecord newValue) {
        this.setValue(getValue().withNavigationCoordinate(newValue.toNavigationCoordinateRecord()));
    }

    private void logicChanged(ObservableValue<? extends LogicCoordinateRecord> observable,
                              LogicCoordinateRecord oldValue,
                              LogicCoordinateRecord newValue) {
        this.setValue(getValue().withLogicCoordinate(newValue.toLogicCoordinateRecord()));
    }

    private void editChanged(ObservableValue<? extends EditCoordinateRecord> observable,
                             EditCoordinateRecord oldValue,
                             EditCoordinateRecord newValue) {
        this.setValue(getValue().withEditCoordinate(newValue.toEditCoordinateRecord()));
    }

    public ViewCoordinateRecord toViewRecord() {
        return this.getValue();
    }

    @Override
    public ViewCoordinate getViewCoordinate() {
        return this;
    }




    @Override
    public void setViewPath(ConceptFacade pathConcept) {
        this.removeListeners();
        StampCoordinateRecord newStampCoordinate = stampCoordinate().getValue().withPath(pathConcept);

        this.stampCoordinate().pathConceptProperty().set(pathConcept);
        MutableList<LanguageCoordinateRecord> languageCoordinateRecords = Lists.mutable.empty();
        languageCoordinates.forEach(observableLanguageCoordinateBase -> languageCoordinateRecords.add(observableLanguageCoordinateBase.getValue()));
        ViewCoordinateRecord viewRecord = new ViewCoordinateRecord(
                this.stampCoordinate().toStampCoordinateRecord(),
                languageCoordinateRecords.toImmutable(),
                this.logicCoordinate().toLogicCoordinateRecord(),
                this.navigationCoordinate().toNavigationCoordinateRecord(),
                this.editCoordinate().toEditCoordinateRecord());
        this.addListeners();
        this.setValue(viewRecord);
    }


    @Override
    public void setAllowedStates(StateSet stateSet) {
        ViewCoordinateRecord newView = getValue().withStampCoordinate(stampCoordinate().getValue().withAllowedStates(stateSet));
        newView = newView.withNavigationCoordinate(navigationCoordinate().getValue().withVertexStates(stateSet));

        this.setValue(newView);
    }


    @Override
    protected void addListeners() {
        this.stampCoordinateObservable.addListener(this.stampChangeListener);
        this.navigationCoordinateObservable.addListener(this.navigationChangedListener);
        this.languageCoordinates.addListener(this.languageCoordinateListListener);
        for (ObservableLanguageCoordinateBase languageCoordinate : this.languageCoordinates) {
            languageCoordinate.addListener( this.languageCoordinateChangeListener);
        }
        this.logicCoordinateObservable.addListener(this.logicCoordinateListener);
        listening.set(true);
    }


    @Override
    protected void removeListeners() {
        this.stampCoordinateObservable.removeListener(this.stampChangeListener);
        this.navigationCoordinateObservable.removeListener(this.navigationChangedListener);
        this.languageCoordinates.removeListener(this.languageCoordinateListListener);
        for (ObservableLanguageCoordinateBase languageCoordinate : this.languageCoordinates) {
            languageCoordinate.removeListener( this.languageCoordinateChangeListener);
        }
        this.logicCoordinateObservable.removeListener(this.logicCoordinateListener);
        listening.set(false);
    }


    //~--- methods -------------------------------------------------------------


    @Override
    public ObservableLogicCoordinate logicCoordinate() {
        return this.logicCoordinateObservable;
    }

    @Override
    public ObservableNavigationCoordinate navigationCoordinate() {
        return this.navigationCoordinateObservable;
    }

    @Override
    public ListProperty<ObservableLanguageCoordinateBase> languageCoordinates() {
        return this.languageCoordinates;
    }

    @Override
    public ObservableEditCoordinate editCoordinate() {
        return this.editCoordinateObservable;
    }

    @Override
    public ObservableStampCoordinate stampCoordinate() {
        return this.stampCoordinateObservable;
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\n" +
                getValue().toString() +
                "\n}";
    }
}