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

import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.actions.component.ActivateComponentAction;
import dev.ikm.komet.rules.actions.component.InactivateComponentAction;
import dev.ikm.komet.rules.actions.membership.AddToKometBaseModelAction;
import dev.ikm.komet.rules.actions.membership.AddToTinkarBaseModelAction;
import dev.ikm.komet.rules.actions.membership.RemoveFromKometBaseModelAction;
import dev.ikm.komet.rules.actions.membership.RemoveFromTinkarBaseModelAction;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * The java compiler needs the -parameters argument
 * see: https://www.evrete.org/docs/ajr/
 */

@RuleSet(value = "Component focus rules")
public class ComponentFocusRules {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentFocusRules.class);

    @Rule(value = "Component focused and active")
    @Where(value = {"$observation.topic() == Topic.COMPONENT_FOCUSED",
                    "$observation.subject() instanceof EntityVersion entityVersion && entityVersion.active()"})
    public void componentFocusedAndActive(ObservationRecord $observation,
                                 ConcurrentHashSet<Consequence<?>> $actionList,
                                 ViewProperties $viewProperties,
                                 EditCoordinate $editCoordinate,
                                 RhsContext ctx) {
        if ($observation.subject() instanceof EntityVersion entityVersion) {
            InactivateComponentAction generatedAction
                    = new InactivateComponentAction(entityVersion, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), generatedAction));
        }
    }

    @Rule(value = "Component focused and inactive")
    @Where(value = {"$observation.topic() == Topic.COMPONENT_FOCUSED",
            "$observation.subject() instanceof EntityVersion entityVersion && entityVersion.inactive()"})
    public void componentFocusedAndInactive(ObservationRecord $observation,
                                          ConcurrentHashSet<Consequence<?>> $actionList,
                                          ViewProperties $viewProperties,
                                          EditCoordinate $editCoordinate,
                                          RhsContext ctx) {
        if ($observation.subject() instanceof EntityVersion entityVersion) {
            ActivateComponentAction generatedAction
                    = new ActivateComponentAction(entityVersion, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), generatedAction));
        }
    }


    @Rule(value = "Concept version focused")
    @Where(value = {"$observation.topic() == Topic.COMPONENT_FOCUSED",
            "$observation.subject() instanceof ConceptEntityVersion"})
    public void conceptVersionFocused(ObservationRecord $observation,
                               ConcurrentHashSet<Consequence<?>> $actionList,
                               ViewProperties $viewProperties,
                               EditCoordinate $editCoordinate,
                               RhsContext ctx) {
        //TODO see if we can get more in the @Where annotation, and maybe split into multiple rules.
        if ($observation.subject() instanceof ConceptEntityVersion conceptVersion) {
            int[] tinkarSemanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptVersion.nid(),
                    TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN.nid());
            int[] kometSemanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptVersion.nid(),
                    TinkarTerm.KOMET_BASE_MODEL_COMPONENT_PATTERN.nid());
            // case 1: never a member of tinkar or komet
            if (tinkarSemanticNidsForComponent.length == 0 && kometSemanticNidsForComponent.length == 0) {
                addToTinkar($actionList, $viewProperties, $editCoordinate, conceptVersion);
                addToKomet($actionList, $viewProperties, $editCoordinate, conceptVersion);
            } else {
                if (tinkarSemanticNidsForComponent.length == 1 && kometSemanticNidsForComponent.length == 0) {
                    // case 2: a member of tinkar only but maybe inactive
                    addRemoveTinkarBasedOnActive($actionList, $viewProperties, $editCoordinate, conceptVersion, tinkarSemanticNidsForComponent);
                    addToKomet($actionList, $viewProperties, $editCoordinate, conceptVersion);
                } else if (tinkarSemanticNidsForComponent.length == 0 && kometSemanticNidsForComponent.length == 1) {
                    // case 3: a member of komet only but maybe inactive
                    addToTinkar($actionList, $viewProperties, $editCoordinate, conceptVersion);
                    addRemoveKometBasedOnActive($actionList, $viewProperties, $editCoordinate, conceptVersion, kometSemanticNidsForComponent);
                } else if (tinkarSemanticNidsForComponent.length == 1 && kometSemanticNidsForComponent.length == 1) {
                    // case 4: a member of both tinkar and komet
                    addRemoveTinkarBasedOnActive($actionList, $viewProperties, $editCoordinate, conceptVersion, tinkarSemanticNidsForComponent);
                    addRemoveKometBasedOnActive($actionList, $viewProperties, $editCoordinate, conceptVersion, kometSemanticNidsForComponent);
                }

            }
        }
    }

    private void addRemoveKometBasedOnActive(ConcurrentHashSet<Consequence<?>> $actionList, ViewProperties $viewProperties, EditCoordinate $editCoordinate, ConceptEntityVersion conceptVersion, int[] kometSemanticNidsForComponent) {
        Latest<EntityVersion> latestKometSemanticVersion = $viewProperties.calculator().latest(kometSemanticNidsForComponent[0]);
        if (latestKometSemanticVersion.isPresent()) {
            if (latestKometSemanticVersion.get().active()) {
                removeFromKomet($actionList, $viewProperties, $editCoordinate, conceptVersion);
            } else {
                addToKomet($actionList, $viewProperties, $editCoordinate, conceptVersion);
            }
        }
    }

    private void addRemoveTinkarBasedOnActive(ConcurrentHashSet<Consequence<?>> $actionList, ViewProperties $viewProperties, EditCoordinate $editCoordinate, ConceptEntityVersion conceptVersion, int[] tinkarSemanticNidsForComponent) {
        Latest<EntityVersion> latestTinkarSemanticVersion = $viewProperties.calculator().latest(tinkarSemanticNidsForComponent[0]);
        if (latestTinkarSemanticVersion.isPresent()) {
            if (latestTinkarSemanticVersion.get().active()) {
                removeFromTinkar($actionList, $viewProperties, $editCoordinate, conceptVersion);
            } else {
                addToTinkar($actionList, $viewProperties, $editCoordinate, conceptVersion);
            }
        }
    }

    private void removeFromKomet(ConcurrentHashSet<Consequence<?>> $actionList, ViewProperties $viewProperties, EditCoordinate $editCoordinate, ConceptEntityVersion conceptVersion) {
        RemoveFromKometBaseModelAction removeKometAction =
                new RemoveFromKometBaseModelAction(conceptVersion, $viewProperties.calculator(), $editCoordinate);
        $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                Thread.currentThread().getStackTrace()[1].toString(), removeKometAction));
    }

    private void removeFromTinkar(ConcurrentHashSet<Consequence<?>> $actionList, ViewProperties $viewProperties, EditCoordinate $editCoordinate, ConceptEntityVersion conceptVersion) {
        RemoveFromTinkarBaseModelAction generatedAction =
                new RemoveFromTinkarBaseModelAction(conceptVersion, $viewProperties.calculator(), $editCoordinate);
        $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                Thread.currentThread().getStackTrace()[1].toString(), generatedAction));
    }

    private void addToTinkar(ConcurrentHashSet<Consequence<?>> $actionList, ViewProperties $viewProperties, EditCoordinate $editCoordinate, ConceptEntityVersion conceptVersion) {
        AddToTinkarBaseModelAction addTinkerAction =
                new AddToTinkarBaseModelAction(conceptVersion, $viewProperties.calculator(), $editCoordinate);
        $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                Thread.currentThread().getStackTrace()[1].toString(), addTinkerAction));
    }

    private void addToKomet(ConcurrentHashSet<Consequence<?>> $actionList, ViewProperties $viewProperties, EditCoordinate $editCoordinate, ConceptEntityVersion conceptVersion) {
        AddToKometBaseModelAction addKometAction =
                new AddToKometBaseModelAction(conceptVersion, $viewProperties.calculator(), $editCoordinate);
        $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                Thread.currentThread().getStackTrace()[1].toString(), addKometAction));
    }

}
