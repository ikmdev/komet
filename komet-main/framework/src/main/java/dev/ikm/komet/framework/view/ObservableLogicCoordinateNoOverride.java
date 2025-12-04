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
import dev.ikm.komet.terms.KometTerm;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinate;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;

public class ObservableLogicCoordinateNoOverride extends ObservableLogicCoordinateBase  {

    public ObservableLogicCoordinateNoOverride(LogicCoordinate logicCoordinate, String coordinateName) {
        super(logicCoordinate, coordinateName);
    }

    public ObservableLogicCoordinateNoOverride(LogicCoordinate logicCoordinate) {
        super(logicCoordinate, "Logic coordinate");
    }

    @Override
    protected ObjectProperty<ConceptFacade> makeClassifierProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                KometTerm.CLASSIFIER_FOR_LOGIC_COORDINATE.toXmlFragment(),
                logicCoordinate.classifier());
    }

    @Override
    public void setExceptOverrides(LogicCoordinateRecord updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    protected ObjectProperty<PatternFacade> makeConceptMemberPattern(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                KometTerm.CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE.toXmlFragment(),
                EntityProxy.Pattern.make(logicCoordinate.conceptMemberPatternNid()));
    }

    @Override
    protected ObjectProperty<ConceptFacade> makeDescriptionLogicProfileProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                KometTerm.DESCRIPTION_LOGIC_PROFILE_FOR_LOGIC_COORDINATE.toXmlFragment(),
                logicCoordinate.descriptionLogicProfile());
    }

    @Override
    protected ObjectProperty<PatternFacade> makeInferredAxiomPatternProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                KometTerm.INFERRED_ASSEMBLAGE_FOR_LOGIC_COORDINATE.toXmlFragment(),
                logicCoordinate.inferredAxiomsPattern());
    }

    @Override
    protected ObjectProperty<PatternFacade> makeStatedAxiomPatternProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                KometTerm.STATED_ASSEMBLAGE_FOR_LOGIC_COORDINATE.toXmlFragment(),
                logicCoordinate.statedAxiomsPattern());
    }

    @Override
    protected ObjectProperty<PatternFacade> makeInferredNavigationPatternProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                "Inferred navigation pattern (fix)",
                logicCoordinate.inferredNavigationPattern());
    }

    @Override
    protected ObjectProperty<PatternFacade> makeStatedNavigationPatternProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                "Stated navigation pattern (fix)",
                logicCoordinate.statedNavigationPattern());
    }

    @Override
    protected ObjectProperty<ConceptFacade> makeRootConceptProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                KometTerm.ROOT_FOR_LOGIC_COORDINATE.toXmlFragment(),
                logicCoordinate.root());
    }

    @Override
    protected LogicCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends LogicCoordinateRecord> observable,
                                                                          LogicCoordinateRecord oldValue,
                                                                          LogicCoordinateRecord newValue) {
        this.classifierProperty().setValue(newValue.classifier());
        this.conceptMemberPatternProperty().setValue(newValue.conceptMemberPattern());
        this.descriptionLogicProfileProperty().setValue(newValue.descriptionLogicProfile());
        this.inferredAxiomsPatternProperty().setValue(newValue.inferredAxiomsPattern());
        this.statedAxiomsPatternProperty().setValue(newValue.statedAxiomsPattern());
        this.inferredNavigationPatternProperty().setValue(newValue.inferredNavigationPattern());
        this.statedNavigationPatternProperty().setValue(newValue.statedNavigationPattern());
        this.rootConceptProperty().setValue(newValue.root());
        return newValue;
    }

    @Override
    public LogicCoordinateRecord getOriginalValue() {
        return getValue();
    }
}
