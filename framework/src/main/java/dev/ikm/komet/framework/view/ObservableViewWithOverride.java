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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObservableViewWithOverride extends ObservableViewBase {

    /**
     * The parent view this override observes. Retained — together with the two listeners below as {@code final}
     * fields — so they can be removed in {@link #dispose()}. The parent outlives this override (e.g. inner/journal
     * windows and popups that churn), so without teardown each discarded override leaks via the parent's listener
     * list (ike-issues#693).
     */
    private final ObservableViewBase parentView;
    private final ChangeListener<ViewCoordinateRecord> overriddenBaseChangedListener = this::overriddenBaseChanged;
    private final ChangeListener<Boolean> parentListeningListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            this.addListeners();
        } else {
            this.removeListeners();
        }
    };

    public ObservableViewWithOverride(ObservableViewBase observableViewBase) {
        this(observableViewBase, null);
    }

    public ObservableViewWithOverride(ObservableViewBase observableViewBase, String name) {
        super(observableViewBase, name);
        // Depth-independent override nesting (ike-issues#663): an override may wrap another override.
        this.parentView = observableViewBase;
        observableViewBase.baseCoordinateProperty().addListener(overriddenBaseChangedListener);
        observableViewBase.listening.addListener(parentListeningListener);
    }

    /**
     * Detaches the two listeners this override registered on its parent view, releasing the reference the parent
     * otherwise holds to this child for the child's whole lifetime. Call when the override (e.g. a closed inner or
     * journal window's coordinate, or a dismissed View Options popup's working view) is discarded; otherwise the
     * parent's listener list accumulates one entry per discarded override over a session (ike-issues#693).
     */
    public void dispose() {
        parentView.baseCoordinateProperty().removeListener(overriddenBaseChangedListener);
        parentView.listening.removeListener(parentListeningListener);
    }

    // TODO when the story is worked to compare the view coordinate change within the child
    // there will be a lot of work around this method and the setExceptOverrides() to determine
    // if the change will now match the parent, and to clear any overridden flags that match
    private void overriddenBaseChanged(ObservableValue<? extends ViewCoordinateRecord> observableValue,
                                       ViewCoordinateRecord oldValue,
                                       ViewCoordinateRecord newValue) {
        var name = super.getName();

        if (this.hasOverrides()) {
            // TODO need to check the overrides and compare with the newValue
            // if the objects are the same, then the child is no longer overridden
            setExceptOverrides(newValue);

            if (!this.hasOverrides()) {
                // TODO need to remove the override indicator in the view coordinate menu

                LOG.debug("{} view coordinate menu no longer has overrides", getName());
            }
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
            editCoordinate().setExceptOverrides(updatedCoordinate.editCoordinate());
        } else {
            this.setValue(updatedCoordinate);
        }
    }

    /// Set the value of the language coordinates to the updatedCoordinate value, but only if not overridden
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

    /**
     * Applies {@code coordinateWithOverrides} as this view's override state by delegating to the
     * {@code setOverrides} of each constituent coordinate (stamp, language, logic, navigation, edit): every
     * dimension is set, and {@link OverrideOf#set} pins it only where its value
     * differs from the inherited parent (clearing the pin where it equals the parent). This is the inverse
     * of {@link #setExceptOverrides} and the re-apply side of the persist/restore round-trip — a dimension
     * that matches the parent stays inherited, so the cascade still tracks parent (e.g. journal) changes for
     * everything that was not genuinely pinned.
     *
     * @param coordinateWithOverrides the desired resolved view coordinate
     */
    public void setOverrides(ViewCoordinateRecord coordinateWithOverrides) {
        stampCoordinate().setOverrides(coordinateWithOverrides.stampCoordinate());
        setLanguageCoordinatesOverrides(coordinateWithOverrides);
        navigationCoordinate().setOverrides(coordinateWithOverrides.navigationCoordinate());
        logicCoordinate().setOverrides(coordinateWithOverrides.logicCoordinate());
        ((ObservableEditCoordinateWithOverride) editCoordinate())
                .setOverrides(coordinateWithOverrides.editCoordinate());
    }

    /// Apply per-language overrides for each language-coordinate position, mirroring setLanguageCoordinatesExceptOverrides.
    private void setLanguageCoordinatesOverrides(ViewCoordinateRecord updatedCoordinate) {
        for (int i = 0; i < languageCoordinates.size() && i < updatedCoordinate.languageCoordinates().size(); i++) {
            ((ObservableLanguageCoordinateWithOverride) languageCoordinates.get(i)).setOverrides(
                    updatedCoordinate.languageCoordinates().get(i).toLanguageCoordinateRecord());
        }
    }

    /**
     * Re-applies a persisted override as a DELTA against the current parent: each constituent re-pins only the
     * dimensions that genuinely differed at capture — where {@code resolved} (the captured override) differs from
     * {@code baseline} (the inherited parent at capture time) — and leaves every merely-inherited dimension
     * untouched, so it tracks the current (possibly changed) parent rather than freezing at the stale captured
     * value. This is what lets a restored card-level override survive a journal-coordinate change between
     * sessions (IKE-Network/ike-issues#745). Inverse of capturing {@code (getValue(), getOriginalValue())}.
     *
     * @param resolved the captured resolved view coordinate ({@code getValue()} at capture time)
     * @param baseline the inherited parent view coordinate at capture ({@code getOriginalValue()} at capture time)
     */
    public void setOverridesFromDelta(ViewCoordinateRecord resolved, ViewCoordinateRecord baseline) {
        stampCoordinate().setOverridesFromDelta(resolved.stampCoordinate(), baseline.stampCoordinate());
        setLanguageCoordinatesOverridesFromDelta(resolved, baseline);
        navigationCoordinate().setOverridesFromDelta(resolved.navigationCoordinate(), baseline.navigationCoordinate());
        logicCoordinate().setOverridesFromDelta(resolved.logicCoordinate(), baseline.logicCoordinate());
        ((ObservableEditCoordinateWithOverride) editCoordinate())
                .setOverridesFromDelta(resolved.editCoordinate(), baseline.editCoordinate());
    }

    /// Apply per-language override deltas for each language-coordinate position, mirroring setLanguageCoordinatesOverrides.
    private void setLanguageCoordinatesOverridesFromDelta(ViewCoordinateRecord resolved, ViewCoordinateRecord baseline) {
        for (int i = 0; i < languageCoordinates.size()
                && i < resolved.languageCoordinates().size() && i < baseline.languageCoordinates().size(); i++) {
            ((ObservableLanguageCoordinateWithOverride) languageCoordinates.get(i)).setOverridesFromDelta(
                    resolved.languageCoordinates().get(i).toLanguageCoordinateRecord(),
                    baseline.languageCoordinates().get(i).toLanguageCoordinateRecord());
        }
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
        this.editCoordinateObservable.setExceptOverrides(newValue.editCoordinate());
        return ViewCoordinateRecord.make(this.stampCoordinateObservable.getValue(),
                this.languageCoordinates.getValue(),
                this.logicCoordinateObservable.getValue(),
                this.navigationCoordinateObservable.getValue(),
                this.editCoordinateObservable.getValue());
    }

}
