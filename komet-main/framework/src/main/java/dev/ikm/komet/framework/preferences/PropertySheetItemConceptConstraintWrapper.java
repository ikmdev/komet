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
package dev.ikm.komet.framework.preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.ProxyFactory;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 
 */
public class PropertySheetItemConceptConstraintWrapper implements PropertySheet.Item, PreferenceChanged {

    private final BooleanProperty changedProperty = new SimpleBooleanProperty(this, "changed", false);

    ;
    SimpleObjectProperty<PropertySheetItemConceptWrapper> constraint
            = new SimpleObjectProperty<>(this, TinkarTerm.CONCEPT_CONSTRAINTS.toXmlFragment());
    ViewCoordinate viewCoordinate;
    String name;

    public PropertySheetItemConceptConstraintWrapper(PropertySheetItemConceptWrapper conceptWrapper, ViewCoordinate viewCoordinate, String name) {
        this.viewCoordinate = viewCoordinate;
        this.constraint.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                bindPropertySheetItemConceptWrapper(newValue);
            }
        });
        this.constraint.setValue(conceptWrapper);

        this.name = name;
    }

    private void bindPropertySheetItemConceptWrapper(PropertySheetItemConceptWrapper conceptWrapper) {
        conceptWrapper.changedProperty().set(false);
        conceptWrapper.changedProperty().addListener((observable, oldValue, conceptChanged) -> {
            if (conceptChanged) {
                conceptWrapper.changedProperty().set(false);
                this.changedProperty.set(true);
            }
        });
    }

    @Override
    public BooleanProperty changedProperty() {
        return changedProperty;
    }

    @Override
    public Class<?> getType() {
        return PropertySheetItemConceptWrapper.class;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "Select the constraints to use when editing this concept. ";
    }

    @Override
    public PropertySheetItemConceptWrapper getValue() {
        return constraint.get();
    }

    @Override
    public void setValue(Object value) {
        this.constraint.setValue((PropertySheetItemConceptWrapper) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.constraint);
    }

    public ConceptFacade getFieldConcept() {
        PropertySheetItemConceptWrapper conceptWrapper = constraint.get();
        SimpleObjectProperty<ConceptFacade> conceptProperty = (SimpleObjectProperty<ConceptFacade>) conceptWrapper.getObservableValue().get();
        return ProxyFactory.fromXmlFragment(conceptProperty.getName());
    }

    public void addSecondaryProperties() {

    }

    public void writeToPreferences(KometPreferences node) {
        ArrayList<String> constraintList = new ArrayList<>();
        constraintList.add(Boolean.toString(constraint.get().allowHistory()));
        constraintList.add(Boolean.toString(constraint.get().allowSearch()));
        constraintList.add(constraint.get().getValue().toXmlFragment());
        constraintList.add(Integer.toString(constraint.get().getAllowedValues().size()));
        for (ConceptFacade allowedValue : constraint.get().getAllowedValues()) {
            constraintList.add(allowedValue.toXmlFragment());
        }
        node.putList(Keys.CONSTRAINT_LIST, constraintList);
    }

    public void readFromPreferences(KometPreferences node) {
        List<String> defaultList = new ArrayList<>();
        defaultList.add(Boolean.toString(true)); // allowHistory
        defaultList.add(Boolean.toString(true)); // allowSearch
        defaultList.add(TinkarTerm.UNINITIALIZED_COMPONENT.toXmlFragment()); //Default value
        defaultList.add(Integer.toString(1));
        defaultList.add(TinkarTerm.UNINITIALIZED_COMPONENT.toXmlFragment()); //Allowed value

        List<String> constraintList = node.getList(Keys.CONSTRAINT_LIST, defaultList);
        PropertySheetItemConceptWrapper conceptWrapper = constraint.get();
        conceptWrapper.setAllowHistory(Boolean.getBoolean(constraintList.get(0)));
        conceptWrapper.setAllowSearch(Boolean.getBoolean(constraintList.get(1)));
        conceptWrapper.setValue(ProxyFactory.fromXmlFragment(constraintList.get(2)));
        int allowedValueCount = Integer.parseInt(constraintList.get(3));
        conceptWrapper.getAllowedValues().clear();
        for (int i = 4; i < allowedValueCount + 4; i++) {
            conceptWrapper.getAllowedValues().add(ProxyFactory.fromXmlFragment(constraintList.get(i)));
        }
    }

    @Override
    public String toString() {
        return "PropertySheetItemConceptConstraintWrapper{" + "constraint=" + constraint.getName() + ", name=" + name + '}';
    }

    public enum Keys {
        CONSTRAINT_LIST
    }

}
