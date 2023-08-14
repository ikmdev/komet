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

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinate;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;

public class ObservableLogicCoordinateWithOverride extends ObservableLogicCoordinateBase {

    public ObservableLogicCoordinateWithOverride(ObservableLogicCoordinate logicCoordinate, String coordinateName) {
        super(logicCoordinate, coordinateName);
        if (logicCoordinate instanceof ObservableLogicCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }
    }

    public ObservableLogicCoordinateWithOverride(ObservableLogicCoordinate logicCoordinate) {
        this(logicCoordinate, logicCoordinate.getName());
    }

    @Override
    public void setExceptOverrides(LogicCoordinateRecord updatedCoordinate) {
        int rootNid = updatedCoordinate.rootNid();
        if (rootConceptProperty().isOverridden()) {
            rootNid = rootConceptProperty().get().nid();
        }

        int classifierNid = updatedCoordinate.classifierNid();
        if (classifierProperty().isOverridden()) {
            classifierNid = classifierProperty().get().nid();
        }
        int conceptMemberPatternNid = updatedCoordinate.conceptMemberPatternNid();
        if (this.conceptMemberPatternProperty().isOverridden()) {
            conceptMemberPatternNid = conceptMemberPatternProperty.get().nid();
        }
        int descriptionLogicProfileNid = updatedCoordinate.descriptionLogicProfileNid();
        if (descriptionLogicProfileProperty().isOverridden()) {
            descriptionLogicProfileNid = descriptionLogicProfileProperty().get().nid();
        }
        int inferredAxiomsPatternNid = updatedCoordinate.inferredAxiomsPatternNid();
        if (this.inferredAxiomsPatternProperty().isOverridden()) {
            inferredAxiomsPatternNid = this.inferredAxiomsPatternProperty().get().nid();
        }
        int statedAxiomsPatternNid = updatedCoordinate.statedAxiomsPatternNid();
        if (this.statedAxiomsPatternProperty().isOverridden()) {
            statedAxiomsPatternNid = this.statedAxiomsPatternProperty().get().nid();
        }
        int inferredNavigationPatternNid = updatedCoordinate.inferredNavigationPatternNid();
        if (inferredNavigationPatternProperty().isOverridden()) {
            inferredNavigationPatternNid = statedNavigationPatternProperty().get().nid();
        }
        int statedNavigationPatternNid = updatedCoordinate.statedNavigationPatternNid();
        if (statedNavigationPatternProperty().isOverridden()) {
            statedNavigationPatternNid = statedNavigationPatternProperty().get().nid();
        }
        /*
                                             int classifierNid,
                                             int descriptionLogicProfileNid,
                                             int inferredAxiomsPatternNid,
                                             int statedAxiomsPatternNid,
                                             int conceptAssemblageNid,
                                             int statedNavigationPatternNid,
                                             int inferredNavigationPatternNid,
                                             int rootNid
                                             */
        setValue(LogicCoordinateRecord.make(classifierNid, descriptionLogicProfileNid, inferredAxiomsPatternNid,
                statedAxiomsPatternNid, conceptMemberPatternNid, statedNavigationPatternNid, inferredNavigationPatternNid, rootNid));
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> classifierProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.classifierProperty();
    }

    @Override
    public ObjectPropertyWithOverride<PatternFacade> conceptMemberPatternProperty() {
        return (ObjectPropertyWithOverride<PatternFacade>) super.conceptMemberPatternProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> descriptionLogicProfileProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.descriptionLogicProfileProperty();
    }

    @Override
    public ObjectPropertyWithOverride<PatternFacade> inferredAxiomsPatternProperty() {
        return (ObjectPropertyWithOverride<PatternFacade>) super.inferredAxiomsPatternProperty();
    }

    @Override
    public ObjectPropertyWithOverride<PatternFacade> statedAxiomsPatternProperty() {
        return (ObjectPropertyWithOverride<PatternFacade>) super.statedAxiomsPatternProperty();
    }

    @Override
    public ObjectPropertyWithOverride<PatternFacade> statedNavigationPatternProperty() {
        return (ObjectPropertyWithOverride<PatternFacade>) super.statedNavigationPatternProperty();
    }

    @Override
    public ObjectPropertyWithOverride<PatternFacade> inferredNavigationPatternProperty() {
        return (ObjectPropertyWithOverride<PatternFacade>) super.inferredNavigationPatternProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> rootConceptProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.rootConceptProperty();
    }

    @Override
    protected ObjectProperty<ConceptFacade> makeClassifierProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.classifierProperty(), this);
    }

    @Override
    protected ObjectProperty<PatternFacade> makeConceptMemberPattern(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.conceptMemberPatternProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptFacade> makeDescriptionLogicProfileProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.descriptionLogicProfileProperty(), this);
    }

    @Override
    protected ObjectProperty<PatternFacade> makeInferredAxiomPatternProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.inferredAxiomsPatternProperty(), this);
    }

    @Override
    protected ObjectProperty<PatternFacade> makeStatedAxiomPatternProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.statedAxiomsPatternProperty(), this);
    }

    @Override
    protected ObjectProperty<PatternFacade> makeInferredNavigationPatternProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.inferredNavigationPatternProperty(), this);
    }

    @Override
    protected ObjectProperty<PatternFacade> makeStatedNavigationPatternProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.statedNavigationPatternProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptFacade> makeRootConceptProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.rootConceptProperty(), this);
    }
    @Override
    public LogicCoordinateRecord getOriginalValue() {
        return LogicCoordinateRecord.make(classifierProperty().getOriginalValue(),
                this.descriptionLogicProfileProperty().getOriginalValue(),
                this.inferredAxiomsPatternProperty().getOriginalValue(),
                this.statedAxiomsPatternProperty().getOriginalValue(),
                this.conceptMemberPatternProperty().getOriginalValue(),
                this.statedNavigationPatternProperty().getOriginalValue(),
                this.inferredNavigationPatternProperty().getOriginalValue(),
                rootConceptProperty().getOriginalValue());
    }


    @Override
    protected LogicCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends LogicCoordinateRecord> observable, LogicCoordinateRecord oldValue, LogicCoordinateRecord newValue) {
        if (!this.classifierProperty().isOverridden()) {
            this.classifierProperty().setValue(newValue.classifier());
        }
        if (!this.conceptMemberPatternProperty().isOverridden()) {
            this.conceptMemberPatternProperty().setValue(newValue.conceptMemberPattern());
        }
        if (!this.descriptionLogicProfileProperty().isOverridden()) {
            this.descriptionLogicProfileProperty().setValue(newValue.descriptionLogicProfile());
        }
        if (!this.inferredAxiomsPatternProperty().isOverridden()) {
            this.inferredAxiomsPatternProperty().setValue(newValue.inferredAxiomsPattern());
        }
        if (!this.statedAxiomsPatternProperty().isOverridden()) {
            this.statedAxiomsPatternProperty().setValue(newValue.statedAxiomsPattern());
        }
        if (!this.statedNavigationPatternProperty().isOverridden()) {
            this.statedNavigationPatternProperty().setValue(newValue.statedNavigationPattern());
        }
        if (!this.inferredNavigationPatternProperty().isOverridden()) {
            this.inferredNavigationPatternProperty().setValue(newValue.inferredNavigationPattern());
        }
        if (!this.rootConceptProperty().isOverridden()) {
            this.rootConceptProperty().setValue(newValue.root());
        }
        return LogicCoordinateRecord.make(
                this.classifierProperty().get(),
                this.descriptionLogicProfileProperty().get(),
                this.inferredAxiomsPatternProperty().get(),
                this.statedAxiomsPatternProperty().get(),
                this.conceptMemberPatternProperty().get(),
                this.statedNavigationPatternProperty().get(),
                this.inferredNavigationPatternProperty().get(),
                this.rootConceptProperty().get());
    }
}
