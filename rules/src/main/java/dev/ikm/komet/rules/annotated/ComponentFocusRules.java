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
import dev.ikm.komet.rules.actions.membership.AddToTinkarBaseModelAction;
import dev.ikm.komet.rules.actions.component.InactivateComponentAction;
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
            int[] semanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptVersion.nid(), TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN.nid());
            // case 1: never a member
            if (semanticNidsForComponent.length == 0) {
                AddToTinkarBaseModelAction generatedAction =
                        new AddToTinkarBaseModelAction(conceptVersion, $viewProperties.calculator(), $editCoordinate);
                $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                        Thread.currentThread().getStackTrace()[1].toString(), generatedAction));
            } else {
                // case 2: a member, but maybe inactive.
                Latest<EntityVersion> latestSemanticVersion = $viewProperties.calculator().latest(semanticNidsForComponent[0]);
                if (latestSemanticVersion.isPresent()) {
                    if (latestSemanticVersion.get().active()) {
                        RemoveFromTinkarBaseModelAction generatedAction =
                                new RemoveFromTinkarBaseModelAction(conceptVersion, $viewProperties.calculator(), $editCoordinate);
                        $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                                Thread.currentThread().getStackTrace()[1].toString(), generatedAction));

                    } else {
                        AddToTinkarBaseModelAction generatedAction =
                                new AddToTinkarBaseModelAction(conceptVersion, $viewProperties.calculator(), $editCoordinate);
                        $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                                Thread.currentThread().getStackTrace()[1].toString(), generatedAction));
                    }
                }
            }
        }
    }
}
