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
package dev.ikm.komet.framework.observable;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.fixtures.StarterDataEphemeralProvider;
import dev.ikm.tinkar.terms.KernelTerm;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Withdrawing a saved version: {@link ObservableComposer#withdraw} takes one entity's version
 * out of a transaction that keeps its versions of every other entity, so the commit publishes
 * the rest without it, and the entity's observable drops it.
 */
@ExtendWith({JavaFXThreadExtension.class, StarterDataEphemeralProvider.class})
@RunOnJavaFXThread
class ObservableComposerWithdrawUTestFX {

    private static ObservableComposer newComposer(String comment) {
        return ObservableComposer.create(Calculators.View.Default(), State.ACTIVE, KernelTerm.USER,
                KernelTerm.PRIMORDIAL_MODULE, KernelTerm.DEVELOPMENT_PATH, comment);
    }

    /** A committed concept to hang description semantics on. */
    private static ObservableConcept newCommittedConcept() {
        ObservableComposer composer = newComposer("concept");
        ObservableComposer.EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> conceptComposer =
                composer.composeConcept(PublicIds.newRandom());
        conceptComposer.save();
        composer.commit();
        return conceptComposer.getEntity();
    }

    /** The description pattern's text field: the one holding a string. */
    private static int textFieldIndex(ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticComposer) {
        var fields = semanticComposer.getEditableVersion().getEditableFields();
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getValue() instanceof String) {
                return i;
            }
        }
        throw new IllegalStateException("The description pattern has no string field");
    }

    private static void setText(ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticComposer,
                                String text) {
        semanticComposer.getEditableVersion().getEditableFields().get(textFieldIndex(semanticComposer)).setObjectValue(text);
    }

    /** A description semantic of the concept, saved uncommitted in the composer's transaction with the given text. */
    private static ObservableSemantic saveDescription(ObservableComposer composer, ObservableConcept concept, String text) {
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticComposer =
                composer.composeSemantic(PublicIds.newRandom(), concept, KernelTerm.DESCRIPTION_PATTERN);
        setText(semanticComposer, text);
        semanticComposer.save();
        return semanticComposer.getEntity();
    }

    private static String latestText(long semanticNid) {
        Latest<SemanticEntityVersion> latest = Calculators.View.Default().latest(semanticNid);
        assertTrue(latest.isPresent(), "the semantic has a latest version in the view");
        for (Object value : latest.get().fieldValues()) {
            if (value instanceof String text) {
                return text;
            }
        }
        throw new IllegalStateException("No text field");
    }

    private static Entity<?> entity(long nid) {
        return EntityHandle.get(nid).expectEntity();
    }

    /** The open transactions holding the entities' uncommitted versions. */
    private static Set<Transaction> transactionsOf(long... nids) {
        Set<Transaction> transactions = new HashSet<>();
        for (long nid : nids) {
            for (EntityVersion version : entity(nid).versions()) {
                if (version.uncommitted()) {
                    Transaction.forVersion(version).ifPresent(transactions::add);
                }
            }
        }
        return transactions;
    }

    @Test
    @DisplayName("Withdrawing a new semantic from a shared transaction cancels it and nothing else")
    void withdrawingANewSemanticLeavesTheRestOfTheTransaction() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer composer = newComposer("shared");
        ObservableSemantic kept = saveDescription(composer, concept, "kept");
        ObservableSemantic withdrawn = saveDescription(composer, concept, "withdrawn");

        assertTrue(composer.withdraw(withdrawn.nid()));

        assertTrue(entity(withdrawn.nid()).canceled(), "the withdrawn semantic's only version is canceled");
        ObservableEntity<?> reread = ObservableEntity.packagePrivateGet(withdrawn.nid());
        assertTrue(reread.versions().stream().noneMatch(version -> version.getVersionRecord().uncommitted()),
                "an observable read now holds no uncommitted version of it");
        assertTrue(entity(kept.nid()).uncommitted(), "the rest of the transaction is untouched");
        assertEquals(1, transactionsOf(kept.nid(), withdrawn.nid()).size(),
                "what remains is still the one shared transaction");
        assertTrue(composer.hasUncommittedChanges());

        composer.commit();

        assertEquals("kept", latestText(kept.nid()));
        assertTrue(entity(kept.nid()).committed());
        assertTrue(entity(withdrawn.nid()).canceled(), "the commit did not bring the withdrawn semantic back");
    }

    @Test
    @DisplayName("Withdrawing an edit updates the observable a caller already holds")
    void withdrawingUpdatesTheHeldInstance() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer creator = newComposer("create");
        ObservableSemantic description = saveDescription(creator, concept, "original");
        creator.commit();

        // What a KL window holds: the observable looked up before the edit.
        ObservableSemantic held = ObservableEntityHandle.get(description.nid()).expectSemantic();
        ObservableComposer editor = newComposer("edit");
        saveDescription(editor, concept, "other");
        var semanticComposer = editor.composeSemantic(description.publicId(), concept, KernelTerm.DESCRIPTION_PATTERN);
        setText(semanticComposer, "edited, then withdrawn");
        semanticComposer.save();
        assertEquals(2, held.versions().size(), "the held observable shows the saved edit");
        assertTrue(held.versions().anySatisfy(version -> version.getVersionRecord().uncommitted()));

        assertTrue(editor.withdraw(description.nid()));

        assertEquals(entity(description.nid()).versions().collect(EntityVersion::stampNid).toSet(),
                held.versionPropertyMap().keySet().collect(Long::valueOf).toSet(),
                "the held observable holds the versions the store does");
        assertTrue(held.versions().noneSatisfy(version -> version.getVersionRecord().uncommitted()));
        assertSame(held, ObservableEntityHandle.get(description.nid()).expectSemantic());
    }

    @Test
    @DisplayName("Withdrawing an edit of a committed semantic leaves the committed version standing")
    void withdrawingAnEditKeepsTheCommittedVersion() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer creator = newComposer("create");
        ObservableSemantic description = saveDescription(creator, concept, "original");
        creator.commit();

        ObservableComposer editor = newComposer("edit");
        ObservableSemantic other = saveDescription(editor, concept, "other");
        var semanticComposer = editor.composeSemantic(description.publicId(), concept, KernelTerm.DESCRIPTION_PATTERN);
        setText(semanticComposer, "edited, then withdrawn");
        semanticComposer.save();
        assertEquals("edited, then withdrawn", latestText(description.nid()));

        assertTrue(editor.withdraw(description.nid()));

        assertEquals("original", latestText(description.nid()));
        assertFalse(entity(description.nid()).uncommitted(), "only the committed version is left");
        editor.commit();
        assertEquals("original", latestText(description.nid()));
        assertEquals("other", latestText(other.nid()));
    }

    @Test
    @DisplayName("Withdrawing a transaction's only version cancels the transaction, and the composer starts another")
    void withdrawingTheOnlyVersionCancelsTheTransaction() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer composer = newComposer("alone");
        ObservableSemantic withdrawn = saveDescription(composer, concept, "alone");

        assertTrue(composer.withdraw(withdrawn.nid()));

        assertTrue(entity(withdrawn.nid()).canceled());
        assertFalse(composer.hasUncommittedChanges());
        assertEquals(ObservableComposer.TransactionState.ROLLED_BACK, composer.getTransactionState());

        ObservableSemantic next = saveDescription(composer, concept, "next");
        composer.commit();
        assertEquals("next", latestText(next.nid()));
    }

    @Test
    @DisplayName("Withdrawing what the transaction never saved withdraws nothing")
    void withdrawingWhatWasNotSavedWithdrawsNothing() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer creator = newComposer("create");
        ObservableSemantic description = saveDescription(creator, concept, "original");
        creator.commit();

        ObservableComposer composer = newComposer("other");
        saveDescription(composer, concept, "unrelated");
        assertFalse(composer.withdraw(description.nid()));
        assertEquals("original", latestText(description.nid()));
    }

    @Test
    @DisplayName("A concept's version is not withdrawn")
    void aConceptIsNotWithdrawn() {
        ObservableComposer composer = newComposer("concept");
        var conceptComposer = composer.composeConcept(PublicIds.newRandom());
        conceptComposer.save();
        assertThrows(IllegalArgumentException.class, () -> composer.withdraw(conceptComposer.getEntity().nid()));
    }
}
