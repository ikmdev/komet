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
package dev.ikm.komet.rules.annotated;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.komet.framework.performance.Request;
import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.ConsequenceMenu;
import dev.ikm.komet.framework.rulebase.GeneratedAction;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.control.Menu;
import org.evrete.api.annotations.RuleElement;
import org.evrete.api.events.EnvironmentChangeEvent;
import org.evrete.dsl.annotation.EventSubscription;

import java.util.Objects;
import java.util.UUID;

import static dev.ikm.komet.rules.evrete.EvreteRulesService.*;

/**
 * The base rule class that contains utility methods and custom field definitions for rule facts.
 */
public abstract class RulesBase {

    private ConcurrentHashSet<Consequence<?>> consequences;
    private EditCoordinate editCoordinate;
    private ViewProperties viewProperties;

    /**
     * Environment listener
     *
     * @param event environment change event
     */
    @EventSubscription
    @SuppressWarnings("unchecked")
    public void onGlobalsChange(EnvironmentChangeEvent event) {
        Object envValue = event.getValue();
        switch (event.getProperty()) {
            case ENV_CONSEQUENCES:
                this.consequences = (ConcurrentHashSet<Consequence<?>>) envValue;
                break;
            case ENV_EDIT_COORDINATE:
                this.editCoordinate = (EditCoordinate) envValue;
                break;
            case ENV_VIEW_PROPERTIES:
                this.viewProperties = (ViewProperties) envValue;
                break;
        }
    }

    public EditCoordinate editCoordinate() {
        return Objects.requireNonNull(editCoordinate);
    }

    public ViewProperties viewProperties() {
        return Objects.requireNonNull(viewProperties);
    }

    public ViewCalculator calculator() {
        return viewProperties().calculator();
    }

    protected void addGeneratedActions(GeneratedAction... actions) {
        // As we now use a shared method, the stacktrace index is now 2 instead of 1
        String ruleMethod = Thread.currentThread().getStackTrace()[2].toString();
        for (GeneratedAction action : actions) {
            Objects.requireNonNull(consequences).add(new ConsequenceAction(UUID.randomUUID(), ruleMethod, action));
        }
    }

    protected void addConsequenceMenu(Menu... menus) {
        // As we now use a shared method, the stacktrace index is now 2 instead of 1
        String ruleMethod = Thread.currentThread().getStackTrace()[2].toString();
        for (Menu menu : menus) {
            Objects.requireNonNull(consequences).add(new ConsequenceMenu(UUID.randomUUID(), ruleMethod, menu));
        }
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is a {@link AxiomSubjectRecord} and not a definition root.
     *
     * @param observation the observation record
     * @return true if the observation's subject is not a definition root, false otherwise
     */
    @RuleElement
    public boolean isNotDefinitionRoot(ObservationRecord observation) {
        return observation.subject() instanceof AxiomSubjectRecord axiomSubject
                &&
                axiomSubject.axiomMeaningNid() != TinkarTerm.DEFINITION_ROOT.nid();
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is a {@link AxiomSubjectRecord} and a definition root.
     *
     * @param observation the observation record
     * @return true if the observation's subject is a definition root, false otherwise
     */
    @RuleElement
    public boolean isDefinitionRoot(ObservationRecord observation) {
        return observation.subject() instanceof AxiomSubjectRecord axiomSubject
                &&
                axiomSubject.axiomMeaningNid() == TinkarTerm.DEFINITION_ROOT.nid();
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is a {@link AxiomSubjectRecord} and a set.
     *
     * @param observation the observation record
     * @return true if the observation's subject is a set
     */
    @RuleElement
    public boolean isAxiomSet(ObservationRecord observation) {
        return observation.subject() instanceof AxiomSubjectRecord axiomSubject
                &&
                axiomSubject.axiomMeaningMatchesAny(TinkarTerm.NECESSARY_SET, TinkarTerm.SUFFICIENT_SET);
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is a {@link AxiomSubjectRecord} and a concept.
     *
     * @param observation the observation record
     * @return true if the observation's subject is a concept
     */
    @RuleElement
    public boolean isAxiomConcept(ObservationRecord observation) {
        return observation.subject() instanceof AxiomSubjectRecord axiomSubject
                &&
                axiomSubject.axiomMeaningNid() == TinkarTerm.CONCEPT_REFERENCE.nid();
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is a {@link AxiomSubjectRecord} and a role group.
     *
     * @param observation the observation record
     * @return true if the observation's subject is a role group
     */
    @RuleElement
    public boolean isAxiomRoleGroup(ObservationRecord observation) {
        return observation.subject() instanceof AxiomSubjectRecord axiomSubject &&
                axiomSubject.axiomMeaningNid() == TinkarTerm.ROLE.nid()
                &&
                axiomSubject.vertexPropertyEquals(TinkarTerm.ROLE_TYPE, TinkarTerm.ROLE_GROUP);
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is a {@link AxiomSubjectRecord} and a role (but not a role group).
     *
     * @param observation the observation record
     * @return true if the observation's subject is a role but not a role group
     */
    @RuleElement
    public boolean isAxiomRoleOnly(ObservationRecord observation) {
        return observation.subject() instanceof AxiomSubjectRecord axiomSubject
                &&
                axiomSubject.axiomMeaningNid() == TinkarTerm.ROLE.nid()
                &&
                !axiomSubject.vertexPropertyEquals(TinkarTerm.ROLE_TYPE, TinkarTerm.ROLE_GROUP);
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is a {@link AxiomSubjectRecord} and a feature.
     *
     * @param observation the observation record
     * @return true if the observation's subject is a feature
     */
    @RuleElement
    public boolean isAxiomFeature(ObservationRecord observation) {
        return observation.subject() instanceof AxiomSubjectRecord axiomSubject
                &&
                axiomSubject.axiomMeaningNid() == TinkarTerm.FEATURE.nid();
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's topic is {@link Topic#AXIOM_FOCUSED}.
     *
     * @param observation the observation record
     * @return true if the observation's topic is {@link Topic#AXIOM_FOCUSED}
     */
    @RuleElement
    public boolean isAxiomFocused(ObservationRecord observation) {
        return observation.topic() == Topic.AXIOM_FOCUSED;
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's topic is {@link Topic#COMPONENT_FOCUSED}.
     *
     * @param observation the observation record
     * @return true if the observation's topic is {@link Topic#COMPONENT_FOCUSED}
     */
    @RuleElement
    public boolean isComponentFocused(ObservationRecord observation) {
        return observation.topic() == Topic.COMPONENT_FOCUSED;
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is an active {@link EntityVersion}.
     *
     * @param observation the observation record
     * @return true if the observation's subject is an active {@link EntityVersion}
     */
    @RuleElement
    public boolean isComponentActive(ObservationRecord observation) {
        return observation.subject() instanceof EntityVersion entityVersion
                &&
                entityVersion.active();
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is an inactive {@link EntityVersion}.
     *
     * @param observation the observation record
     * @return true if the observation's subject is an inactive {@link EntityVersion}
     */
    @RuleElement
    public boolean isComponentInactive(ObservationRecord observation) {
        return observation.subject() instanceof EntityVersion entityVersion
                &&
                entityVersion.inactive();
    }

    /**
     * Condition method on {@link ObservationRecord} that returns true if
     * the given observation's subject is a {@link ConceptEntityVersion}.
     *
     * @param observation the observation record
     * @return true if the observation's subject is a {@link ConceptEntityVersion}
     */
    @RuleElement
    public boolean isConceptVersion(ObservationRecord observation) {
        return observation.subject() instanceof ConceptEntityVersion;
    }

    /**
     * Condition method on {@link Statement} that returns true if
     * the given statement is a {@link Request} with a String subject.
     *
     * @param statement the statement being tested
     * @return true if the statement is a {@link Request} with a String subject
     */
    @RuleElement
    public boolean requestWithStringSubject(Statement statement) {
        return statement instanceof Request request
                &&
                request.subject() instanceof String;
    }

    /**
     * Condition method on {@link Statement} that returns {@code true} if
     * the given statement's topic is {@link Topic#NEW_PATTERN_REQUEST}.
     *
     * @param statement the statement being tested
     * @return true if the statement's topic is {@link Topic#NEW_PATTERN_REQUEST}.
     */
    @RuleElement
    public boolean isNewPatternRequest(Statement statement) {
        return statement.topic() == Topic.NEW_PATTERN_REQUEST;
    }

    /**
     * Condition method on {@link Statement} that returns true if
     * the given statement's topic is {@link Topic#NEW_CONCEPT_REQUEST}.
     *
     * @param statement the statement being tested
     * @return true if the statement's topic is {@link Topic#NEW_CONCEPT_REQUEST}.
     */
    @RuleElement
    public boolean isNewConceptRequest(Statement statement) {
        return statement.topic() == Topic.NEW_CONCEPT_REQUEST;
    }

}