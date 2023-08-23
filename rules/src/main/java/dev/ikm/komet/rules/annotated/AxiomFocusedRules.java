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
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.ConsequenceMenu;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.actions.axiom.*;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RuleSet(value = "Axiom focus rules")
public class AxiomFocusedRules {
    private static final Logger LOG = LoggerFactory.getLogger(AxiomFocusedRules.class);

    @Rule(value = "Axiom of interest is not the definition root")
    @Where(value = {"$observation.topic() == Topic.AXIOM_FOCUSED",
            """
                     $observation.subject() instanceof AxiomSubjectRecord axiomSubject && 
                     axiomSubject.axiomMeaningNid() != TinkarTerm.DEFINITION_ROOT.nid()
                    """
    })
    public void axiomIsNotDefinitionRoot(ObservationRecord $observation,
                                         ConcurrentHashSet<Consequence<?>> $actionList,
                                         ViewProperties $viewProperties,
                                         EditCoordinate $editCoordinate,
                                         RhsContext ctx) {
// TODO would be nice if Everete recognized the pattern variable "axiomSubject" and could pass it as a parameter.
        if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
            RemoveAxiomAction removeAxiomAction = new RemoveAxiomAction("Remove axiom", axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), removeAxiomAction));
        }
    }

    @Rule(value = "Axiom of interest is the definition root")
    @Where(value = {"$observation.topic() == Topic.AXIOM_FOCUSED",
            """
                     $observation.subject() instanceof AxiomSubjectRecord axiomSubject && 
                     axiomSubject.axiomMeaningNid() == TinkarTerm.DEFINITION_ROOT.nid()
                    """
    })
    public void axiomIsDefinitionRoot(ObservationRecord $observation,
                                      ConcurrentHashSet<Consequence<?>> $actionList,
                                      ViewProperties $viewProperties,
                                      EditCoordinate $editCoordinate,
                                      RhsContext ctx) {
        if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
            if (!axiomSubjectRecord.axiomTree().containsVertexWithMeaning(TinkarTerm.NECESSARY_SET)) {
                // allow addition of necessary set
                AddNecessarySet addNecessarySet = new AddNecessarySet("Add necessary set", axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
                $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                        Thread.currentThread().getStackTrace()[1].toString(), addNecessarySet));
            }
            // always allow addition of sufficient set (multiple sufficient sets allowed)
            AddSufficientSet addSufficientSet = new AddSufficientSet("Add sufficient set", axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), addSufficientSet));
        }
    }


    @Rule(value = "Axiom of interest is a set")
    @Where(value = {"$observation.topic() == Topic.AXIOM_FOCUSED",
            """
                     $observation.subject() instanceof AxiomSubjectRecord axiomSubject && 
                     axiomSubject.axiomMeaningMatchesAny(TinkarTerm.NECESSARY_SET, TinkarTerm.SUFFICIENT_SET)
                    """
    })
    public void axiomIsSet(ObservationRecord $observation,
                           ConcurrentHashSet<Consequence<?>> $actionList,
                           ViewProperties $viewProperties,
                           EditCoordinate $editCoordinate,
                           RhsContext ctx) {
        if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
            AddIsA addIsA = new AddIsA("Add is-a", axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), addIsA));

            AddSomeRole addSomeRole = new AddSomeRole("Add role", axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), addSomeRole));

            AddRoleGroup addRoleGroup = new AddRoleGroup("Add role group", axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), addRoleGroup));

            AddFeature addFeature = new AddFeature("Add feature", axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), addFeature));
        }
    }

    @Rule(value = "Axiom of interest is a concept axiom")
    @Where(value = {"$observation.topic() == Topic.AXIOM_FOCUSED",
            """
                     $observation.subject() instanceof AxiomSubjectRecord axiomSubject && 
                     axiomSubject.axiomMeaningNid() == TinkarTerm.CONCEPT_REFERENCE.nid()
                    """
    })
    public void axiomIsConceptAxiom(ObservationRecord $observation,
                                    ConcurrentHashSet<Consequence<?>> $actionList,
                                    ViewProperties $viewProperties,
                                    EditCoordinate $editCoordinate,
                                    RhsContext ctx) {

        if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
            $actionList.add(new ConsequenceMenu(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(),
                    new ChooseConceptMenu("Choose replacement is-a", $viewProperties.calculator(), axiomSubjectRecord.nodeForPopover(),
                            $viewProperties, o -> {
                        ChangeConcept changeConcept = new ChangeConcept("Change is-a", o, axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
                        changeConcept.doAction();
                    })
            ));
        }
    }

    @Rule(value = "Axiom of interest is a role group")
    @Where(value = {"$observation.topic() == Topic.AXIOM_FOCUSED",
            """
                     $observation.subject() instanceof AxiomSubjectRecord axiomSubject && 
                     axiomSubject.axiomMeaningNid() == TinkarTerm.ROLE_TYPE.nid() &&
                     axiomSubject.vertexPropertyEquals(TinkarTerm.ROLE_TYPE, TinkarTerm.ROLE_GROUP)
                    """
    })
    public void axiomIsRoleGroup(ObservationRecord $observation,
                                 ConcurrentHashSet<Consequence<?>> $actionList,
                                 ViewProperties $viewProperties,
                                 EditCoordinate $editCoordinate,
                                 RhsContext ctx) {

        if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
            AddSomeRole addRole = new AddSomeRole("Add role", axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), addRole));
        }
    }

    @Rule(value = "Axiom of interest is a role but not a role group")
    @Where(value = {"$observation.topic() == Topic.AXIOM_FOCUSED",
            """
                     $observation.subject() instanceof AxiomSubjectRecord axiomSubject && 
                     axiomSubject.axiomMeaningNid() == TinkarTerm.ROLE_TYPE.nid() &&
                     !axiomSubject.vertexPropertyEquals(TinkarTerm.ROLE_TYPE, TinkarTerm.ROLE_GROUP)
                    """
    })
    public void axiomIsRoleButNotARoleGroup(ObservationRecord $observation,
                                            ConcurrentHashSet<Consequence<?>> $actionList,
                                            ViewProperties $viewProperties,
                                            EditCoordinate $editCoordinate,
                                            RhsContext ctx) {

        if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
            $actionList.add(new ConsequenceMenu(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(),
                    new ChooseConceptMenu("Choose role type", $viewProperties.calculator(), axiomSubjectRecord.nodeForPopover(),
                            $viewProperties, o -> {
                        ChangeRoleType changeRoleType = new ChangeRoleType("Change role type", o, axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
                        changeRoleType.doAction();
                    })
            ));
            $actionList.add(new ConsequenceMenu(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(),
                    new ChooseConceptMenu("Choose role restriction", $viewProperties.calculator(), axiomSubjectRecord.nodeForPopover(),
                            $viewProperties, o -> {
                        ChangeRoleRestriction changeRoleRestriction = new ChangeRoleRestriction("Change role restriction", o, axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
                        changeRoleRestriction.doAction();
                    })
            ));
        }
    }

    @Rule(value = "Axiom of interest is a feature")
    @Where(value = {"$observation.topic() == Topic.AXIOM_FOCUSED",
            """
                     $observation.subject() instanceof AxiomSubjectRecord axiomSubject && 
                     axiomSubject.axiomMeaningNid() == TinkarTerm.FEATURE.nid()
                    """
    })
    public void axiomIsFeature(ObservationRecord $observation,
                                 ConcurrentHashSet<Consequence<?>> $actionList,
                                 ViewProperties $viewProperties,
                                 EditCoordinate $editCoordinate,
                                 RhsContext ctx) {

        if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
            $actionList.add(new ConsequenceMenu(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(),
                    new ChooseConceptMenu("Choose feature type", $viewProperties.calculator(), axiomSubjectRecord.nodeForPopover(),
                            $viewProperties, o -> {
                        ChangeFeatureType changeFeatureType = new ChangeFeatureType("Change feature type", o, axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
                        changeFeatureType.doAction();
                    })
            ));

            $actionList.add(new ConsequenceMenu(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(),
                    new ChooseConcreteOperatorMenu("Choose comparison", $viewProperties.calculator(),
                            o -> {
                        ChangeFeatureOperator changeFeatureOperator = new ChangeFeatureOperator("Change comparison", o, axiomSubjectRecord, $viewProperties.calculator(), $editCoordinate);
                        changeFeatureOperator.doAction();
                    })
            ));


        }
    }

}
