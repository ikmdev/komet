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
import dev.ikm.komet.rules.actions.component.ActivateComponentAction;
import dev.ikm.komet.rules.actions.component.InactivateComponentAction;
import dev.ikm.komet.rules.actions.membership.AddToKometBaseModelAction;
import dev.ikm.komet.rules.actions.membership.AddToTinkarBaseModelAction;
import dev.ikm.komet.rules.actions.membership.RemoveFromKometBaseModelAction;
import dev.ikm.komet.rules.actions.membership.RemoveFromTinkarBaseModelAction;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.evrete.dsl.annotation.FieldDeclaration;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;

/**
 * Rules related to component-related observations.
 * <p>
 * To simplify the conditions of the rules, this ruleset employs custom field declarations through
 * the use of the {@link FieldDeclaration} annotation.
 * </p>
 * <p>
 * Custom field declarations provide an additional abstraction layer for the domain classes and allow
 * for changing the conditions easily should the domain classes change. And, as a side benefit,
 * we no longer need to include now unnecessary imports via the
 * {@link org.evrete.api.Knowledge#addImport(Class)} method.
 * </p>
 * <p>
 * Custom fields are better placed in a common parent class so they could be reused
 * by multiple rulesets.
 * </p>
 *
 * Note: The java compiler needs the -parameters argument
 * see: <a href="https://www.evrete.org/docs#ajr">https://www.evrete.org/docs/#ajr</a>
 */

@RuleSet("Component focus rules")
public class ComponentFocusRules extends RulesBase {

    //private static final Logger LOG = LoggerFactory.getLogger(ComponentFocusRules.class);

    /**
     * @see RulesBase#isComponentFocused(ObservationRecord)
     * @see RulesBase#isComponentActive(ObservationRecord)
     */
    @Rule("Component focused and active")
    @Where({
            "$observation.focusedComponent",
            "$observation.isComponentActive"
    })
    public void componentFocusedAndActive(ObservationRecord $observation) {
        if ($observation.subject() instanceof EntityVersion entityVersion) {
            InactivateComponentAction generatedAction
                    = new InactivateComponentAction(entityVersion, calculator(), editCoordinate());
            addGeneratedActions(generatedAction);
        }
    }

    /**
     * @see RulesBase#isComponentFocused(ObservationRecord)
     * @see RulesBase#isComponentInactive(ObservationRecord)
     */
    @Rule("Component focused and inactive")
    @Where({
            "$observation.focusedComponent",
            "$observation.isComponentInactive"
    })
    public void componentFocusedAndInactive(ObservationRecord $observation) {
        if ($observation.subject() instanceof EntityVersion entityVersion) {
            ActivateComponentAction generatedAction
                    = new ActivateComponentAction(entityVersion, calculator(), editCoordinate());
            addGeneratedActions(generatedAction);
        }
    }

    /**
     * @see RulesBase#isComponentFocused(ObservationRecord)
     * @see RulesBase#isConceptVersion(ObservationRecord)
     */
    @Rule("Concept version focused")
    @Where({
            "$observation.focusedComponent",
            "$observation.isConceptVersion"
    })
    public void conceptVersionFocused(ObservationRecord $observation) {
        //TODO see if we can get more in the @Where annotation, and maybe split into multiple rules.
        if ($observation.subject() instanceof ConceptEntityVersion conceptVersion) {
            int[] tinkarSemanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptVersion.nid(),
                    TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN.nid());
            int[] kometSemanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptVersion.nid(),
                    TinkarTerm.KOMET_BASE_MODEL_COMPONENT_PATTERN.nid());
            // case 1: never a member of tinkar or komet
            if (tinkarSemanticNidsForComponent.length == 0 && kometSemanticNidsForComponent.length == 0) {
                addToTinkar(conceptVersion);
                addToKomet(conceptVersion);
            } else {
                if (tinkarSemanticNidsForComponent.length == 1 && kometSemanticNidsForComponent.length == 0) {
                    // case 2: a member of tinkar only but maybe inactive
                    addRemoveTinkarBasedOnActive(conceptVersion, tinkarSemanticNidsForComponent);
                    addToKomet(conceptVersion);
                } else if (tinkarSemanticNidsForComponent.length == 0 && kometSemanticNidsForComponent.length == 1) {
                    // case 3: a member of komet only but maybe inactive
                    addToTinkar(conceptVersion);
                    addRemoveKometBasedOnActive(conceptVersion, kometSemanticNidsForComponent);
                } else if (tinkarSemanticNidsForComponent.length == 1 && kometSemanticNidsForComponent.length == 1) {
                    // case 4: a member of both tinkar and komet
                    addRemoveTinkarBasedOnActive(conceptVersion, tinkarSemanticNidsForComponent);
                    addRemoveKometBasedOnActive(conceptVersion, kometSemanticNidsForComponent);
                }

            }
        }
    }

    private void addRemoveKometBasedOnActive(ConceptEntityVersion conceptVersion, int[] kometSemanticNidsForComponent) {
        Latest<EntityVersion> latestKometSemanticVersion = calculator().latest(kometSemanticNidsForComponent[0]);
        if (latestKometSemanticVersion.isPresent()) {
            if (latestKometSemanticVersion.get().active()) {
                removeFromKomet(conceptVersion);
            } else {
                addToKomet(conceptVersion);
            }
        }
    }

    private void addRemoveTinkarBasedOnActive(ConceptEntityVersion conceptVersion, int[] tinkarSemanticNidsForComponent) {
        Latest<EntityVersion> latestTinkarSemanticVersion = calculator().latest(tinkarSemanticNidsForComponent[0]);
        if (latestTinkarSemanticVersion.isPresent()) {
            if (latestTinkarSemanticVersion.get().active()) {
                removeFromTinkar(conceptVersion);
            } else {
                addToTinkar(conceptVersion);
            }
        }
    }

    private void removeFromKomet(ConceptEntityVersion conceptVersion) {
        RemoveFromKometBaseModelAction removeKometAction =
                new RemoveFromKometBaseModelAction(conceptVersion, calculator(), editCoordinate());
        addGeneratedActions(removeKometAction);
    }

    private void removeFromTinkar(ConceptEntityVersion conceptVersion) {
        RemoveFromTinkarBaseModelAction generatedAction =
                new RemoveFromTinkarBaseModelAction(conceptVersion, calculator(), editCoordinate());
        addGeneratedActions(generatedAction);
    }

    private void addToTinkar(ConceptEntityVersion conceptVersion) {
        AddToTinkarBaseModelAction addTinkerAction =
                new AddToTinkarBaseModelAction(conceptVersion, calculator(), editCoordinate());
        addGeneratedActions(addTinkerAction);
    }

    private void addToKomet(ConceptEntityVersion conceptVersion) {
        AddToKometBaseModelAction addKometAction =
                new AddToKometBaseModelAction(conceptVersion, calculator(), editCoordinate());
        addGeneratedActions(addKometAction);
    }

}
