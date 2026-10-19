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
package dev.ikm.komet.rules.test;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.actions.axiom.PendingDefinition;
import dev.ikm.komet.rules.actions.membership.AddToKometBaseModelAction;
import dev.ikm.komet.rules.actions.membership.AddToTinkarBaseModelAction;
import dev.ikm.komet.rules.actions.membership.RemoveFromKometBaseModelAction;
import dev.ikm.komet.rules.actions.membership.RemoveFromTinkarBaseModelAction;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.ikm.tinkar.terms.KernelPattern.KOMET_BASE_MODEL_COMPONENT_PATTERN;
import static dev.ikm.tinkar.terms.KernelPattern.TINKAR_BASE_MODEL_COMPONENT_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rules engine's writers leave their work pending and never settle someone else's
 * (IKE-Network/ike-issues#1308): pending work ends only when someone publishes or discards it. An
 * axiom action's updated definition stays uncommitted, continues the pending version under its own
 * author, module and path, including one a restart rebuilt, and refuses one under any other. A
 * membership action's version stays uncommitted, and a membership whose latest version is
 * uncommitted is refused.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PendingWorkActionsIT {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

    private ViewProperties viewProperties;
    private ViewCalculator view;
    private EditCoordinateRecord edit;
    /** Stated definitions with a committed latest version, one per test that edits one. */
    private final List<Long> definitions = new ArrayList<>();
    private final List<Transaction> opened = new ArrayList<>();

    @BeforeAll
    void startStore() throws Exception {
        assertTrue(PB_STARTER_DATA.exists(), "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath());
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
        viewProperties = new ObservableViewNoOverride(Coordinates.View.DefaultView()).makeOverridableViewProperties();
        view = viewProperties.calculator();
        edit = viewProperties.nodeView().editCoordinate().toEditCoordinateRecord();
        EntityService.get().forEachSemanticOfPattern(view.logicCoordinateRecord().statedAxiomsPatternNid(), semantic -> {
            if (definitions.size() < 4 && view.latest(semantic).isPresent() && !view.latest(semantic).get().uncommitted()) {
                definitions.add(semantic.nid());
            }
        });
        assertEquals(4, definitions.size(), "the starter set has stated definitions to edit");
    }

    @AfterEach
    void settleWhatTheTestLeft() {
        opened.forEach(transaction -> {
            if (transaction.isOpen()) {
                transaction.cancel();
            }
        });
        opened.clear();
    }

    @AfterAll
    void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    void aDefinitionEditIsLeftPendingAndTheNextEditContinuesIt() {
        long definition = definitions.get(0);

        Transaction first = restate(definition, edit);
        Transaction second = restate(definition, edit);

        assertSame(first, second, "the second edit continues the pending version's transaction");
        assertTrue(second.isOpen(), "nothing committed it");
        assertEquals(1, uncommittedVersions(definition), "the edits land on one pending version");
    }

    @Test
    void anotherAuthorsPendingDefinitionIsRefused() {
        long definition = definitions.get(1);
        Transaction pending = restate(definition, edit);
        int versions = versions(definition);
        EditCoordinateRecord otherAuthor = EditCoordinateRecord.make(KernelConcept.KOMET_USER.nid(),
                edit.getDefaultModuleNid(), edit.getDefaultPathNid());

        IllegalStateException refusal = assertThrows(IllegalStateException.class, () -> restate(definition, otherAuthor));

        assertTrue(refusal.getMessage().contains("publish or discard it first"), refusal.getMessage());
        assertEquals(versions, versions(definition), "the refused edit wrote nothing");
        assertTrue(pending.isOpen(), "the pending work is left to its author");
    }

    @Test
    void aDefinitionPendingAcrossARestartIsContinuedNotCommitted() {
        long definition = definitions.get(2);
        Transaction before = restate(definition, edit);
        restart();
        opened.remove(before);
        Transaction rebuilt = Transaction.forVersion(latest(definition)).orElseThrow();
        assertTrue(rebuilt.rebuiltFromRecord(), "the pending definition's transaction is rebuilt from its record");
        assertEquals(before.transactionUuid(), rebuilt.transactionUuid());

        Transaction continued = restate(definition, edit);

        assertSame(rebuilt, continued, "the edit continues the rebuilt transaction");
        assertTrue(continued.isOpen(), "nothing committed it");
        assertEquals(1, uncommittedVersions(definition), "no second pending version beside it");
    }

    @Test
    void anEditOfACommittedDefinitionOpensATransactionOfItsOwn() {
        long definition = definitions.get(3);
        Transaction other = track(Transaction.make("Someone else's pending work"));

        Transaction own = restate(definition, edit);

        assertFalse(other == own, "an unrelated open transaction is not taken over");
        assertTrue(own.getTransactionName().contains("Restate"), "named for the action: " + own.getTransactionName());
        assertTrue(own.isOpen());
    }

    @Test
    void addingToAndRemovingFromTheKometBaseModelIsLeftPendingAndRefusedOverPendingWork() {
        membershipIsLeftPendingAndRefusedOverPendingWork(KOMET_BASE_MODEL_COMPONENT_PATTERN.nid(),
                concept -> new AddToKometBaseModelAction(concept, view, edit).doAction(null, edit),
                concept -> new RemoveFromKometBaseModelAction(concept, view, edit).doAction(null, edit));
    }

    @Test
    void addingToAndRemovingFromTheTinkarBaseModelIsLeftPendingAndRefusedOverPendingWork() {
        membershipIsLeftPendingAndRefusedOverPendingWork(TINKAR_BASE_MODEL_COMPONENT_PATTERN.nid(),
                concept -> new AddToTinkarBaseModelAction(concept, view, edit).doAction(null, edit),
                concept -> new RemoveFromTinkarBaseModelAction(concept, view, edit).doAction(null, edit));
    }

    private void membershipIsLeftPendingAndRefusedOverPendingWork(long patternNid,
                                                                  Consumer<ConceptEntityVersion> add,
                                                                  Consumer<ConceptEntityVersion> remove) {
        ConceptEntityVersion member = conceptWithCommittedMembership(patternNid, add);
        remove.accept(member);
        SemanticEntity<SemanticEntityVersion> membership = membership(member, patternNid).orElseThrow();
        Latest<SemanticEntityVersion> removed = view.latest(membership);
        assertTrue(removed.get().uncommitted(), "the removal is left pending");
        assertEquals(State.INACTIVE, removed.get().state());
        Transaction removal = track(Transaction.forVersion(removed.get()).orElseThrow());
        assertTrue(removal.isOpen(), "nothing committed the removal");

        int versions = versions(membership.nid());
        IllegalStateException refusal = assertThrows(IllegalStateException.class, () -> add.accept(member));
        assertTrue(refusal.getMessage().contains("publish or discard it first"), refusal.getMessage());
        assertEquals(versions, versions(membership.nid()), "the refused addition wrote nothing");
        assertTrue(removal.isOpen(), "the pending removal is left to the person");

        ConceptEntityVersion nonMember = conceptWithoutMembership(patternNid);
        add.accept(nonMember);
        SemanticEntity<SemanticEntityVersion> added = membership(nonMember, patternNid).orElseThrow();
        Latest<SemanticEntityVersion> addedVersion = view.latest(added);
        assertTrue(addedVersion.get().uncommitted(), "the addition is left pending");
        Transaction addition = track(Transaction.forVersion(addedVersion.get()).orElseThrow());
        assertTrue(addition.isOpen(), "nothing committed the addition");
        assertThrows(IllegalStateException.class, () -> remove.accept(nonMember), "removing a pending addition is refused");
        assertThrows(IllegalStateException.class, () -> add.accept(nonMember), "adding over a pending addition is refused");
        assertEquals(1, versions(added.nid()), "no second pending version beside the first");
    }

    /** Puts a definition's tree back unchanged, as an axiom action writing directly does. */
    private Transaction restate(long definition, EditCoordinateRecord editCoordinate) {
        EntityVersion latest = latest(definition);
        DiTreeEntity tree = (DiTreeEntity) ((SemanticEntityVersion) latest).fieldValues().get(0);
        PendingDefinition.write(definition, latest.moduleNid(), latest.pathNid(), tree, editCoordinate, view, "Restate");
        EntityVersion written = latest(definition);
        assertTrue(written.uncommitted(), "the edit is left pending");
        return track(Transaction.forVersion(written).orElseThrow());
    }

    /** As a store's stop and start do: records written, transactions dropped, then rebuilt from records. */
    private static void restart() {
        Transaction.checkpointOpen(EntityService.get());
        Transaction.forgetAll();
        Transaction.restoreOpen(EntityService.get());
    }

    private Transaction track(Transaction transaction) {
        if (!opened.contains(transaction)) {
            opened.add(transaction);
        }
        return transaction;
    }

    private EntityVersion latest(long nid) {
        Latest<EntityVersion> latest = view.latest(nid);
        assertTrue(latest.isPresent());
        return latest.get();
    }

    private static int versions(long nid) {
        return EntityHandle.get(nid).expectEntity().versions().size();
    }

    private static int uncommittedVersions(long nid) {
        int uncommitted = 0;
        for (Object version : EntityHandle.get(nid).expectEntity().versions()) {
            if (((EntityVersion) version).uncommitted()) {
                uncommitted++;
            }
        }
        return uncommitted;
    }

    private static Optional<SemanticEntity<SemanticEntityVersion>> membership(ConceptEntityVersion concept, long patternNid) {
        return EntityService.get().semanticsForComponentOfPattern(concept.nid(), patternNid).findFirst();
    }

    /** A member whose membership is committed; where the starter set has none, one added and published here. */
    private ConceptEntityVersion conceptWithCommittedMembership(long patternNid, Consumer<ConceptEntityVersion> add) {
        List<ConceptEntityVersion> found = new ArrayList<>();
        EntityService.get().forEachSemanticOfPattern(patternNid, semantic -> {
            Latest<SemanticEntityVersion> latest = view.latest(semantic);
            if (found.isEmpty() && latest.isPresent() && latest.get().active() && !latest.get().uncommitted()
                    && view.latest(semantic.referencedComponentNid()).orElse(null) instanceof ConceptEntityVersion concept) {
                found.add(concept);
            }
        });
        if (!found.isEmpty()) {
            return found.getFirst();
        }
        ConceptEntityVersion concept = conceptWithoutMembership(patternNid);
        add.accept(concept);
        SemanticEntityVersion added = view.latest(membership(concept, patternNid).orElseThrow()).get();
        Transaction.forVersion(added).orElseThrow().commit();
        assertFalse(view.latest(membership(concept, patternNid).orElseThrow()).get().uncommitted(), "published");
        return concept;
    }

    private ConceptEntityVersion conceptWithoutMembership(long patternNid) {
        for (long definition : definitions) {
            long conceptNid = EntityHandle.get(definition).expectSemanticRecord().referencedComponentNid();
            if (EntityService.get().semanticsForComponentOfPattern(conceptNid, patternNid).findFirst().isEmpty()
                    && view.latest(conceptNid).orElse(null) instanceof ConceptEntityVersion concept) {
                return concept;
            }
        }
        throw new AssertionError("No stated concept outside " + view.getDescriptionTextOrNid(patternNid));
    }
}
