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
package dev.ikm.komet.rules.builder;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.actions.axiom.RemoveAxiomAction;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.api.RhsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AxiomFocusedRulesBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(AxiomFocusedRulesBuilder.class);

    static final KnowledgeService service = new KnowledgeService();
    static final Knowledge knowledge;

    static {
        knowledge = service
                .newKnowledge()
                .builder()
                .newRule("Axiom of interest is not the definition root")
                .forEach(
                        "$observation", ObservationRecord.class,
                        "$actionList", ConcurrentHashSet.class,
                        "$viewProperties", ViewProperties.class,
                        "$editCoordinate", EditCoordinate.class,
                        "$ctx", RhsContext.class)
                .where("$observation.topic() == Topic.AXIOM_FOCUSED")
                .where("""
                     $observation.subject() instanceof AxiomSubjectRecord axiomSubject && 
                     axiomSubject.axiomMeaningNid() != TinkarTerm.DEFINITION_ROOT.nid()
                    """)
                .execute(ctx -> {
                    ObservationRecord $observation = ctx.get("$observation");
                    ConcurrentHashSet<Consequence<?>> $actionList = ctx.get("$actionList");
                    ViewProperties $viewProperties = ctx.get("$viewProperties");
                    EditCoordinate $editCoordinate = ctx.get("$editCoordinate");

// TODO would be nice if Everete recognized the pattern variable "axiomSubject" and could pass it as a parameter.
                    if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
                        RemoveAxiomAction removeAxiomAction = new RemoveAxiomAction("Remove axiom",
                                axiomSubjectRecord,
                                $viewProperties.calculator(),
                                $editCoordinate);
                        $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                                Thread.currentThread().getStackTrace()[1].toString(), removeAxiomAction));
                    }
                })
                .build();
    }
}
