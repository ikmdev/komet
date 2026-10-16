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
package dev.ikm.komet.actions.component;

import dev.ikm.komet.actions.KometAction;
import dev.ikm.komet.actions.KometActionInput;
import dev.ikm.komet.actions.KometActionResult;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;

/**
 * Activation and inactivation: a new version of the subject in the target state, with every field
 * as the latest version in the view has it. The version keeps the subject's module, as an edit to
 * existing content does, and is written on the edit coordinate's path by its author. It is left
 * uncommitted, for the person to commit with their other edits. A subject whose latest version is
 * itself uncommitted is refused: a second uncommitted version would not supersede the first, and
 * replacing it would rewrite someone's pending edit.
 */
abstract sealed class ComponentStateAction implements KometAction permits ActivateComponent, InactivateComponent {

    private final State target;

    ComponentStateAction(State target) {
        this.target = target;
    }

    @Override
    public final KometActionResult perform(KometActionInput input, ViewCalculator view, EditCoordinate edit) {
        if (!EntityFacade.same(input.action(), concept())) {
            return KometActionResult.refused(input, "This input asks for " + input.action().description()
                    + ", not " + concept().description() + ".");
        }
        EntityFacade subject = input.subject();
        String name = view.getDescriptionTextOrNid(subject.nid());
        Entity<?> entity = EntityHandle.get(subject.nid()).orNull();
        if (entity == null) {
            return KometActionResult.refused(input, name + " is not in this knowledge base.");
        }
        if (entity instanceof StampEntity<?>) {
            return KometActionResult.refused(input, "A STAMP is not activated or inactivated.");
        }
        Latest<EntityVersion> latest = view.latest(subject.nid());
        if (latest.isAbsent()) {
            return KometActionResult.refused(input, "No version of " + name + " is visible in this view.");
        }
        EntityVersion head = latest.get();
        if (head.uncommitted()) {
            return KometActionResult.refused(input, name + " has an uncommitted change; commit or cancel it first.");
        }
        if (head.state() == target) {
            return KometActionResult.refused(input, name + " is already " + stateWord() + ".");
        }

        Transaction transaction = Transaction.make(concept().description() + ": " + name);
        try {
            StampEntity<?> stamp = transaction.getStamp(target, edit.getAuthorNidForChanges(), head.moduleNid(),
                    edit.getDefaultPathNid());
            Entity<?> analogue = switch (entity) {
                case ConceptRecord concept -> concept.with(new ConceptVersionRecord(concept, stamp.nid())).build();
                case SemanticRecord semantic -> semantic.with(new SemanticVersionRecord(semantic, stamp.nid(),
                        ((SemanticVersionRecord) head).fieldValues())).build();
                case PatternRecord pattern -> {
                    PatternVersionRecord latestPattern = (PatternVersionRecord) head;
                    yield pattern.with(new PatternVersionRecord(pattern, stamp.nid(),
                            latestPattern.semanticPurposeNid(), latestPattern.semanticMeaningNid(),
                            latestPattern.fieldDefinitions())).build();
                }
                default -> null;
            };
            if (analogue == null) {
                transaction.cancel();
                return KometActionResult.refused(input, name + " is not a concept, semantic or pattern.");
            }
            transaction.addComponent(analogue);
            Entity.provider().putEntity(analogue);
            return KometActionResult.completed(input, stamp, name + " is " + stateWord()
                    + " in an uncommitted version; commit to keep it.");
        } catch (RuntimeException e) {
            transaction.cancel();
            return KometActionResult.failed(input, "Could not make " + name + " " + stateWord() + ": " + e.getMessage());
        }
    }

    private String stateWord() {
        return target == State.ACTIVE ? "active" : "inactive";
    }
}
