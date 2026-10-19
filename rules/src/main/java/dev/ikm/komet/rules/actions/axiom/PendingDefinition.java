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
package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;

import java.util.Optional;

/**
 * Writes an axiom action's updated definition as a pending version. It is never committed here:
 * the version stays pending until the person publishes or discards it (IKE-Network/ike-issues#1308).
 * A pending version under the author, module and path this edit writes is continued, so successive
 * edits of one definition land on its stamp, as the composer's do; a pending version under any other
 * is refused, since replacing it would rewrite someone else's edit and a version beside it would not
 * supersede it.
 */
public final class PendingDefinition {

    private PendingDefinition() {
    }

    /**
     * @param semanticNid    the semantic holding the definition
     * @param moduleNid      the module of the version written, the edited version's own
     * @param pathNid        the path of the version written, the edited version's own
     * @param newTree        the updated definition
     * @param editCoordinate whose author writes the version
     * @param viewCalculator for the names in a refusal
     * @param actionText     the action, naming the transaction a new edit opens
     * @return the version written, uncommitted
     * @throws IllegalStateException when the definition has a pending version under another author, module or path
     */
    public static SemanticVersionRecord write(long semanticNid, long moduleNid, long pathNid, DiTreeEntity newTree,
                                              EditCoordinateRecord editCoordinate, ViewCalculator viewCalculator,
                                              String actionText) {
        SemanticRecord semantic = EntityHandle.get(semanticNid).expectSemanticRecord();
        long authorNid = editCoordinate.getAuthorNidForChanges();
        Transaction transaction = transactionFor(semantic, authorNid, moduleNid, pathNid, viewCalculator, actionText);
        StampEntity<?> stamp = transaction.getStamp(State.ACTIVE, Long.MAX_VALUE, authorNid, moduleNid, pathNid);
        SemanticVersionRecord newVersion = new SemanticVersionRecord(semantic, stamp.nid(), Lists.immutable.of(newTree));
        Entity.provider().putEntity(semantic.with(newVersion).build());
        return newVersion;
    }

    private static Transaction transactionFor(SemanticRecord semantic, long authorNid, long moduleNid, long pathNid,
                                              ViewCalculator viewCalculator, String actionText) {
        Transaction owner = null;
        for (SemanticVersionRecord version : semantic.versions()) {
            if (!version.uncommitted()) {
                continue;
            }
            StampEntity<?> stamp = version.stamp();
            Optional<Transaction> versionOwner = Transaction.forVersion(version);
            if (stamp.authorNid() != authorNid || stamp.moduleNid() != moduleNid || stamp.pathNid() != pathNid
                    || stamp.state() != State.ACTIVE || versionOwner.isEmpty()) {
                throw new IllegalStateException(viewCalculator.getDescriptionTextOrNid(semantic.referencedComponentNid())
                        + " has an uncommitted change to its definition"
                        + (stamp.authorNid() == authorNid ? " in another module or path"
                                : " by " + viewCalculator.getDescriptionTextOrNid(stamp.authorNid()))
                        + "; publish or discard it first.");
            }
            owner = versionOwner.get();
        }
        if (owner != null) {
            return owner;
        }
        Transaction transaction = Transaction.make(actionText + ": "
                + viewCalculator.getDescriptionTextOrNid(semantic.referencedComponentNid()));
        transaction.addComponent(semantic.nid());
        return transaction;
    }
}
