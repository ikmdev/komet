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
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.fixtures.StarterDataEphemeralProvider;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Modify before commit (IKE-Network/ike-issues#1326): a composer that
 * {@linkplain ObservableComposer#joinPendingWork() joins pending work}, as a KL window's and the
 * assistant's do, edits a component another such composer changed, under the same author, module
 * and path. The edit must land in that change's transaction and replace its pending version, so
 * the component has one pending version and one commit publishes everything; two transactions
 * would leave two pending versions that commit in no fixed order.
 */
@ExtendWith({JavaFXThreadExtension.class, StarterDataEphemeralProvider.class})
@RunOnJavaFXThread
class ObservableComposerJoinPendingWorkUTestFX {

    private static ObservableComposer newComposer(String comment, EntityFacade module) {
        return ObservableComposer.create(Calculators.View.Default(), State.ACTIVE, KernelConcept.USER,
                module, KernelConcept.DEVELOPMENT_PATH, comment);
    }

    /** A composer that joins pending work, as a window's or the assistant's is. */
    private static ObservableComposer joining(String comment) {
        return newComposer(comment, KernelConcept.PRIMORDIAL_MODULE).joinPendingWork();
    }

    /** A committed concept with one committed description semantic, the semantic returned. */
    private static ObservableSemantic newCommittedDescription(String text) {
        ObservableComposer composer = newComposer("create", KernelConcept.PRIMORDIAL_MODULE);
        ObservableComposer.EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> conceptComposer =
                composer.composeConcept(PublicIds.newRandom());
        conceptComposer.save();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticComposer =
                composer.composeSemantic(PublicIds.newRandom(), conceptComposer.getEntity(), KernelPattern.DESCRIPTION_PATTERN);
        setText(semanticComposer, text);
        semanticComposer.save();
        composer.commit();
        return semanticComposer.getEntity();
    }

    private static ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editorOf(
            ObservableComposer composer, ObservableSemantic semantic) {
        return composer.composeSemantic(semantic.publicId(),
                EntityHandle.get(semantic.referencedComponentNid()).expectEntity(), KernelPattern.DESCRIPTION_PATTERN);
    }

    private static void setText(ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticComposer,
                                String text) {
        var fields = semanticComposer.getEditableVersion().getEditableFields();
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getValue() instanceof String) {
                fields.get(i).setObjectValue(text);
                return;
            }
        }
        throw new IllegalStateException("The description pattern has no string field");
    }

    /** Edits the description's text through the composer and saves. */
    private static void edit(ObservableComposer composer, ObservableSemantic description, String text) {
        var editor = editorOf(composer, description);
        setText(editor, text);
        editor.save();
    }

    /** Retires the description through the composer, as the assistant's change does. */
    private static void retire(ObservableComposer composer, ObservableSemantic description) {
        composer.composeSemantic(description.publicId(), EntityHandle.get(description.referencedComponentNid()).expectEntity(),
                KernelPattern.DESCRIPTION_PATTERN, State.INACTIVE).save();
    }

    /** The semantic's uncommitted versions, as the store holds them. */
    private static List<EntityVersion> uncommitted(ObservableSemantic semantic) {
        return EntityHandle.get(semantic.nid()).expectEntity().versions().stream()
                .map(EntityVersion.class::cast).filter(EntityVersion::uncommitted).toList();
    }

    /** The text of the semantic's one pending version. */
    private static String pendingText(ObservableSemantic semantic) {
        List<EntityVersion> pending = uncommitted(semantic);
        assertEquals(1, pending.size(), "one pending version; versions: " + pending);
        return text(pending.getFirst());
    }

    /** The semantic's latest version on the default view. */
    private static EntityVersion latest(ObservableSemantic semantic) {
        return Calculators.View.Default().latest(semantic.nid()).get();
    }

    private static String text(EntityVersion version) {
        for (Object value : ((SemanticEntityVersion) version).fieldValues()) {
            if (value instanceof String text) {
                return text;
            }
        }
        throw new IllegalStateException("no text in " + version);
    }

    /** The transaction owning the semantic's one pending version. */
    private static Transaction owner(ObservableSemantic semantic) {
        List<EntityVersion> pending = uncommitted(semantic);
        assertEquals(1, pending.size(), "one pending version; versions: " + pending);
        Optional<Transaction> owner = Transaction.forVersion(pending.getFirst());
        assertTrue(owner.isPresent(), "the pending version has an owning transaction");
        return owner.get();
    }

    @Test
    @DisplayName("A window with nothing written edits what the assistant changed: one transaction, one pending version")
    void aWindowWithNothingWrittenContinuesTheAssistantsTransaction() {
        ObservableSemantic changed = newCommittedDescription("as published");
        ObservableSemantic alsoChanged = newCommittedDescription("also as published");
        ObservableComposer assistant = joining("Assistant change");
        edit(assistant, changed, "as the assistant left it");
        edit(assistant, alsoChanged, "also changed by the assistant");
        Transaction assistantTransaction = owner(changed);

        ObservableComposer window = joining("Edit Semantic Details");
        edit(window, changed, "as the person left it");

        assertEquals("as the person left it", pendingText(changed), "the person's edit replaced the assistant's version");
        assertSame(assistantTransaction, owner(changed), "the edit landed in the assistant's transaction");
        assertSame(assistantTransaction, window.getTransaction());

        window.commit();
        assertTrue(uncommitted(changed).isEmpty() && uncommitted(alsoChanged).isEmpty(),
                "one commit published the person's edit and the assistant's other change");
        assistant.commit();     // ended elsewhere: nothing more to do
        assertEquals(ObservableComposer.TransactionState.NONE, assistant.getTransactionState());
    }

    @Test
    @DisplayName("A window with edits of its own moves them into the assistant's transaction before editing what it changed")
    void aWindowWithItsOwnEditsMovesThemIntoTheAssistantsTransaction() {
        ObservableSemantic changed = newCommittedDescription("as published");
        ObservableSemantic windowOnly = newCommittedDescription("the window's, as published");
        ObservableComposer assistant = joining("Assistant change");
        edit(assistant, changed, "as the assistant left it");
        Transaction assistantTransaction = owner(changed);

        ObservableComposer window = joining("Edit Semantic Details");
        var windowOnlyEditor = editorOf(window, windowOnly);
        setText(windowOnlyEditor, "the window's first edit");
        windowOnlyEditor.save();
        Transaction windowTransaction = owner(windowOnly);
        assertNotSame(assistantTransaction, windowTransaction);

        edit(window, changed, "as the person left it");

        assertFalse(windowTransaction.isOpen(), "the window's own transaction was emptied and ended");
        assertSame(assistantTransaction, windowTransaction.continuation());
        assertSame(assistantTransaction, owner(windowOnly), "the window's earlier edit moved with it");
        assertEquals("the window's first edit", pendingText(windowOnly), "the move kept the edit as it was");
        assertSame(assistantTransaction, owner(changed));
        assertEquals("as the person left it", pendingText(changed));

        // The editor the window opened before the move goes on saving, now into the joined transaction.
        setText(windowOnlyEditor, "the window's second edit");
        windowOnlyEditor.save();
        assertSame(assistantTransaction, owner(windowOnly));
        assertEquals("the window's second edit", pendingText(windowOnly));

        window.commit();
        assertTrue(uncommitted(changed).isEmpty() && uncommitted(windowOnly).isEmpty(), "one commit published all of it");
        assertFalse(assistantTransaction.isOpen());
    }

    @Test
    @DisplayName("The assistant with work of its own moves it into the window's transaction to edit what the window changed")
    void theAssistantMovesItsWorkIntoTheWindowsTransaction() {
        ObservableSemantic windowChanged = newCommittedDescription("as published");
        ObservableSemantic assistantOnly = newCommittedDescription("the assistant's, as published");
        ObservableComposer window = joining("Edit Semantic Details");
        edit(window, windowChanged, "as the person left it");
        Transaction windowTransaction = owner(windowChanged);

        ObservableComposer assistant = joining("Assistant change");
        edit(assistant, assistantOnly, "changed by the assistant");
        Transaction assistantTransaction = owner(assistantOnly);
        edit(assistant, windowChanged, "as the assistant left it");

        assertFalse(assistantTransaction.isOpen());
        assertSame(windowTransaction, owner(assistantOnly));
        assertSame(windowTransaction, owner(windowChanged));
        assertEquals("as the assistant left it", pendingText(windowChanged));
        assertSame(windowTransaction, assistant.getTransaction());

        window.commit();
        assertTrue(uncommitted(windowChanged).isEmpty() && uncommitted(assistantOnly).isEmpty(),
                "the window's commit published the assistant's change too");
        assertTrue(windowTransaction.committed());
    }

    @Test
    @DisplayName("A window's edit of a description the assistant retired replaces the retirement: one pending version")
    void aWindowEditOfARetiredDescriptionReplacesTheRetirement() {
        ObservableSemantic retired = newCommittedDescription("as published");
        ObservableComposer assistant = joining("Assistant change");
        retire(assistant, retired);
        Transaction assistantTransaction = owner(retired);
        assertEquals(State.INACTIVE, uncommitted(retired).getFirst().state());

        ObservableComposer window = joining("Edit Semantic Details");
        edit(window, retired, "as the person left it");

        assertEquals("as the person left it", pendingText(retired), "the person's edit is the one pending version");
        assertEquals(State.ACTIVE, uncommitted(retired).getFirst().state(), "an edit in an active window is active");
        assertSame(assistantTransaction, owner(retired));

        window.commit();
        assertTrue(uncommitted(retired).isEmpty());
        assertEquals(2, EntityHandle.get(retired.nid()).expectEntity().versions().size(),
                "the published version and the one commit, not two versions committed at one time");
        EntityVersion published = latest(retired);
        assertEquals(State.ACTIVE, published.state());
        assertEquals("as the person left it", text(published));
    }

    @Test
    @DisplayName("The assistant's retirement of a description the window edited keeps the person's text, retired")
    void theAssistantsRetirementOfAWindowEditReplacesIt() {
        ObservableSemantic edited = newCommittedDescription("as published");
        ObservableComposer window = joining("Edit Semantic Details");
        edit(window, edited, "as the person left it");
        Transaction windowTransaction = owner(edited);

        ObservableComposer assistant = joining("Assistant change");
        retire(assistant, edited);

        assertEquals("as the person left it", pendingText(edited), "the retirement starts from the person's edit");
        assertEquals(State.INACTIVE, uncommitted(edited).getFirst().state());
        assertSame(windowTransaction, owner(edited));

        window.commit();
        assertTrue(uncommitted(edited).isEmpty());
        assertEquals(2, EntityHandle.get(edited.nid()).expectEntity().versions().size(),
                "the window's editor, whose version the retirement replaced, wrote nothing more at the commit");
        EntityVersion published = latest(edited);
        assertEquals(State.INACTIVE, published.state(), "the retirement was published");
        assertEquals("as the person left it", text(published));
    }

    @Test
    @DisplayName("A window's rollback after joining discards the change it joined")
    void aRollbackAfterJoiningDiscardsTheJoinedChange() {
        ObservableSemantic changed = newCommittedDescription("as published");
        ObservableComposer assistant = joining("Assistant change");
        edit(assistant, changed, "as the assistant left it");

        ObservableComposer window = joining("Edit Semantic Details");
        edit(window, changed, "as the person left it");
        window.rollback();

        assertTrue(uncommitted(changed).isEmpty(), "nothing of either edit is pending");
        assertEquals(ObservableComposer.TransactionState.ROLLED_BACK, window.getTransactionState());
    }

    @Test
    @DisplayName("A composer that does not join pending work opens a transaction of its own, as before")
    void aComposerThatDoesNotJoinOpensItsOwnTransaction() {
        ObservableSemantic changed = newCommittedDescription("as published");
        ObservableComposer assistant = joining("Assistant change");
        edit(assistant, changed, "as the assistant left it");
        Transaction assistantTransaction = owner(changed);

        ObservableComposer committingAtOnce = newComposer("commits at once", KernelConcept.PRIMORDIAL_MODULE);
        edit(committingAtOnce, changed, "committed at once");

        assertNotSame(assistantTransaction, committingAtOnce.getTransaction(),
                "a composer committing at once never takes over pending work");
        assertTrue(assistantTransaction.isOpen());
        assistant.rollback();
        committingAtOnce.rollback();
    }

    @Test
    @DisplayName("Pending work under another module is not joined")
    void pendingWorkUnderAnotherModuleIsNotJoined() {
        ObservableSemantic changed = newCommittedDescription("as published");
        ObservableComposer assistant = joining("Assistant change");
        edit(assistant, changed, "as the assistant left it");
        Transaction assistantTransaction = owner(changed);

        ObservableComposer otherModule = newComposer("another module", KernelConcept.SOLOR_MODULE).joinPendingWork();
        edit(otherModule, changed, "in another module");

        assertNotSame(assistantTransaction, otherModule.getTransaction());
        assistant.rollback();
        otherModule.rollback();
    }

    @Test
    @DisplayName("Two pending versions of one component are not merged: the work stays where it was")
    void twoPendingVersionsOfOneComponentAreNotMerged() {
        ObservableSemantic both = newCommittedDescription("as published");
        ObservableSemantic changed = newCommittedDescription("changed by the assistant later");
        // Two pending versions of one component, as only a composer that does not join could leave them.
        ObservableComposer assistant = joining("Assistant change");
        edit(assistant, both, "the assistant's version");
        ObservableComposer beside = newComposer("beside", KernelConcept.PRIMORDIAL_MODULE);
        edit(beside, both, "a version beside it");
        Transaction besideTransaction = beside.getTransaction();
        besideTransaction.leavePending();
        edit(assistant, changed, "the assistant's other change");
        Transaction assistantTransaction = assistant.getTransaction();

        assertFalse(besideTransaction.canMoveInto(assistantTransaction));
        assertFalse(besideTransaction.moveInto(assistantTransaction), "nothing moves");
        assertTrue(besideTransaction.isOpen() && assistantTransaction.isOpen());
        assertEquals(2, uncommitted(both).size(), "both pending versions are left as they were");

        assistant.rollback();
        beside.rollback();
    }
}
