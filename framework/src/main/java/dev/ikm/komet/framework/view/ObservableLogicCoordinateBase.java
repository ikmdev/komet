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

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinate;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;

/**
 * The Class ObservableLogicCoordinateBase.
 *
 * 
 */
public abstract class ObservableLogicCoordinateBase
        extends ObservableCoordinateAbstract<LogicCoordinateRecord>
        implements ObservableLogicCoordinate {

    /**
     * The stated assemblage property.
     */
    final ObjectProperty<PatternFacade> statedAxiomPatternProperty;

    /**
     * The inferred assemblage property.
     */
    final ObjectProperty<PatternFacade> inferredAxiomsPatternProperty;

    /**
     * The description logic profile property.
     */
    final ObjectProperty<ConceptFacade> descriptionLogicProfileProperty;

    /**
     * The classifier property.
     */
    final ObjectProperty<ConceptFacade> classifierProperty;

    /**
     * The concept assemblage property.
     */
    final ObjectProperty<PatternFacade> conceptMemberPatternProperty;

    /**
     * The concept assemblage property.
     */
    final ObjectProperty<PatternFacade> statedNavigationPatternProperty;

    final ObjectProperty<PatternFacade> inferredNavigationPatternProperty;
    /**
     * The classifier property.
     */
    final ObjectProperty<ConceptFacade> rootConceptProperty;

    //~--- constructors --------------------------------------------------------
    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptFacade> classifierListener = this::classifierConceptChanged;
    private final ChangeListener<PatternFacade> conceptMemberPatternListener = this::conceptMemberPatternChanged;
    private final ChangeListener<ConceptFacade> descriptionLogicProfileListener = this::descriptionLogicProfileChanged;
    private final ChangeListener<PatternFacade> inferredAxiomsPatternListener = this::inferredAxiomPatternChanged;
    private final ChangeListener<PatternFacade> statedAxiomPatternListener = this::statedAxiomPatternChanged;
    private final ChangeListener<PatternFacade> statedNavigationPatternListener = this::statedNavigationPatternChanged;
    private final ChangeListener<PatternFacade> inferredNavigationPatternListener = this::inferredNavigationPatternChanged;
    private final ChangeListener<ConceptFacade> rootConceptListener = this::rootConceptChanged;

    /**
     * Instantiates a new observable logic coordinate impl.
     *
     * @param logicCoordinate the logic coordinate
     */
    protected ObservableLogicCoordinateBase(LogicCoordinate logicCoordinate, String coordinateName) {
        super(logicCoordinate.toLogicCoordinateRecord(), coordinateName);

        this.classifierProperty = makeClassifierProperty(logicCoordinate);

        this.conceptMemberPatternProperty = makeConceptMemberPattern(logicCoordinate);

        this.descriptionLogicProfileProperty = makeDescriptionLogicProfileProperty(logicCoordinate);

        this.inferredAxiomsPatternProperty = makeInferredAxiomPatternProperty(logicCoordinate);

        this.statedAxiomPatternProperty = makeStatedAxiomPatternProperty(logicCoordinate);

        this.statedNavigationPatternProperty = makeStatedNavigationPatternProperty(logicCoordinate);

        this.inferredNavigationPatternProperty = makeInferredNavigationPatternProperty(logicCoordinate);

        this.rootConceptProperty = makeRootConceptProperty(logicCoordinate);

        addListeners();

    }

    protected abstract ObjectProperty<ConceptFacade> makeClassifierProperty(LogicCoordinate logicCoordinate);

    protected abstract ObjectProperty<PatternFacade> makeConceptMemberPattern(LogicCoordinate logicCoordinate);

    protected abstract ObjectProperty<ConceptFacade> makeDescriptionLogicProfileProperty(LogicCoordinate logicCoordinate);

    protected abstract ObjectProperty<PatternFacade> makeInferredAxiomPatternProperty(LogicCoordinate logicCoordinate);

    protected abstract ObjectProperty<PatternFacade> makeStatedAxiomPatternProperty(LogicCoordinate logicCoordinate);

    protected abstract ObjectProperty<PatternFacade> makeInferredNavigationPatternProperty(LogicCoordinate logicCoordinate);

    protected abstract ObjectProperty<PatternFacade> makeStatedNavigationPatternProperty(LogicCoordinate logicCoordinate);

    protected abstract ObjectProperty<ConceptFacade> makeRootConceptProperty(LogicCoordinate logicCoordinate);

    @Override
    protected void addListeners() {
        this.classifierProperty.addListener(this.classifierListener);
        this.conceptMemberPatternProperty.addListener(this.conceptMemberPatternListener);
        this.descriptionLogicProfileProperty.addListener(this.descriptionLogicProfileListener);
        this.inferredAxiomsPatternProperty.addListener(this.inferredAxiomsPatternListener);
        this.statedAxiomPatternProperty.addListener(this.statedAxiomPatternListener);
        this.inferredNavigationPatternProperty.addListener(this.inferredNavigationPatternListener);
        this.statedNavigationPatternProperty.addListener(this.statedNavigationPatternListener);
        this.rootConceptProperty.addListener(this.rootConceptListener);
    }

    @Override
    protected void removeListeners() {
        this.classifierProperty.removeListener(this.classifierListener);
        this.conceptMemberPatternProperty.removeListener(this.conceptMemberPatternListener);
        this.descriptionLogicProfileProperty.removeListener(this.descriptionLogicProfileListener);
        this.inferredAxiomsPatternProperty.removeListener(this.inferredAxiomsPatternListener);
        this.statedAxiomPatternProperty.removeListener(this.statedAxiomPatternListener);
        this.inferredNavigationPatternProperty.removeListener(this.inferredNavigationPatternListener);
        this.statedNavigationPatternProperty.removeListener(this.statedNavigationPatternListener);
        this.rootConceptProperty.removeListener(this.rootConceptListener);
    }

    //~--- methods -------------------------------------------------------------
    private void statedNavigationPatternChanged(ObservableValue<? extends PatternFacade> observable,
                                                PatternFacade oldStatedNavigationPattern,
                                                PatternFacade newStatedNavigationPattern) {
        this.setValue(LogicCoordinateRecord.make(classifierNid(),
                descriptionLogicProfileNid(),
                inferredAxiomsPatternNid(),
                statedAxiomsPatternNid(),
                conceptMemberPatternNid(),
                newStatedNavigationPattern.nid(),
                inferredNavigationPatternNid(),
                rootNid()));
    }

    private void inferredNavigationPatternChanged(ObservableValue<? extends PatternFacade> observable,
                                                PatternFacade oldInferredNavigationPattern,
                                                PatternFacade newInferredNavigationPattern) {
        this.setValue(LogicCoordinateRecord.make(classifierNid(),
                descriptionLogicProfileNid(),
                inferredAxiomsPatternNid(),
                statedAxiomsPatternNid(),
                conceptMemberPatternNid(),
                statedNavigationPatternNid(),
                newInferredNavigationPattern.nid(),
                rootNid()));
    }

    private void statedAxiomPatternChanged(ObservableValue<? extends PatternFacade> observable,
                                           PatternFacade oldStatedPattern,
                                           PatternFacade newStatedPattern) {
        this.setValue(LogicCoordinateRecord.make(classifierNid(),
                descriptionLogicProfileNid(),
                inferredAxiomsPatternNid(),
                newStatedPattern.nid(),
                conceptMemberPatternNid(),
                statedNavigationPatternNid(),
                inferredNavigationPatternNid(),
                rootNid()));
    }

    private void inferredAxiomPatternChanged(ObservableValue<? extends PatternFacade> observable,
                                             PatternFacade oldInferredAssemblage,
                                             PatternFacade newInferredAxiomPattern) {
        this.setValue(LogicCoordinateRecord.make(classifierNid(),
                descriptionLogicProfileNid(),
                newInferredAxiomPattern.nid(),
                statedAxiomsPatternNid(),
                conceptMemberPatternNid(),
                statedNavigationPatternNid(),
                inferredNavigationPatternNid(),
                rootNid()));
    }

    private void descriptionLogicProfileChanged(ObservableValue<? extends ConceptFacade> observable,
                                                ConceptFacade oldDescriptionLogicProfile,
                                                ConceptFacade newDescriptionLogicProfile) {
        this.setValue(LogicCoordinateRecord.make(classifierNid(),
                newDescriptionLogicProfile.nid(),
                inferredAxiomsPatternNid(),
                statedAxiomsPatternNid(),
                conceptMemberPatternNid(),
                statedNavigationPatternNid(),
                inferredNavigationPatternNid(),
                rootNid()));
    }

    private void conceptMemberPatternChanged(ObservableValue<? extends PatternFacade> observable,
                                             PatternFacade oldConceptPatternConcept,
                                             PatternFacade newConceptPatternConcept) {
        this.setValue(LogicCoordinateRecord.make(classifierNid(),
                descriptionLogicProfileNid(),
                inferredAxiomsPatternNid(),
                statedAxiomsPatternNid(),
                newConceptPatternConcept.nid(),
                statedNavigationPatternNid(),
                inferredNavigationPatternNid(),
                rootNid()));
    }

    private void classifierConceptChanged(ObservableValue<? extends ConceptFacade> observable,
                                          ConceptFacade oldClassifierConcept,
                                          ConceptFacade newClassifierConcept) {
        this.setValue(LogicCoordinateRecord.make(newClassifierConcept.nid(),
                descriptionLogicProfileNid(),
                inferredAxiomsPatternNid(),
                statedAxiomsPatternNid(),
                conceptMemberPatternNid(),
                statedNavigationPatternNid(),
                inferredNavigationPatternNid(),
                rootNid()));
    }

    private void rootConceptChanged(ObservableValue<? extends ConceptFacade> observable,
                                    ConceptFacade oldRootConcept,
                                    ConceptFacade newRootConcept) {
        this.setValue(LogicCoordinateRecord.make(classifierNid(),
                descriptionLogicProfileNid(),
                inferredAxiomsPatternNid(),
                statedAxiomsPatternNid(),
                conceptMemberPatternNid(),
                statedNavigationPatternNid(),
                inferredNavigationPatternNid(),
                newRootConcept.nid()));
    }

    @Override
    public LogicCoordinateRecord getLogicCoordinate() {
        return getValue();
    }

    /**
     * Classifier property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<ConceptFacade> classifierProperty() {
        return this.classifierProperty;
    }

    @Override
    public ObjectProperty<PatternFacade> conceptMemberPatternProperty() {
        return this.conceptMemberPatternProperty;
    }

    /**
     * Description logic profile property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<ConceptFacade> descriptionLogicProfileProperty() {
        return this.descriptionLogicProfileProperty;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ObservableLogicCoordinateBase) {
            return this.getValue().equals(((ObservableLogicCoordinateBase) obj).getValue());
        }
        return this.getValue().equals(obj);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    /**
     * Inferred assemblage property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<PatternFacade> inferredAxiomsPatternProperty() {
        return this.inferredAxiomsPatternProperty;
    }

    /**
     * Stated assemblage property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<PatternFacade> statedAxiomsPatternProperty() {
        return this.statedAxiomPatternProperty;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ObservableLogicCoordinateBase{" + this.getValue().toString() + '}';
    }


    @Override
    public String toUserString() {
        return this.getValue().toUserString();
    }

    @Override
    public LogicCoordinateRecord toLogicCoordinateRecord() {
        return this.getValue();
    }

    @Override
    public ObjectProperty<PatternFacade> statedNavigationPatternProperty() {
        return this.statedNavigationPatternProperty;
    }
    @Override
    public ObjectProperty<PatternFacade> inferredNavigationPatternProperty() {
        return this.inferredNavigationPatternProperty;
    }

    @Override
    public ObjectProperty<ConceptFacade> rootConceptProperty() {
        return this.rootConceptProperty;
    }
}

