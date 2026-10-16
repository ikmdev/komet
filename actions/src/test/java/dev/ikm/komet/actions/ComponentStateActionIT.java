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
package dev.ikm.komet.actions;

import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Activation and inactivation against the IKE starter set in an ephemeral store: what a completed
 * run writes, that it copies the latest version rather than the first, and that every refusal
 * writes nothing. Each test acts on a component no other test touches.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentStateActionIT {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

    private ViewCalculator view;
    private EditCoordinateRecord edit;

    @BeforeAll
    void startStore() {
        assertTrue(PB_STARTER_DATA.exists(), "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath()
                + " (copied by maven-dependency-plugin in process-test-resources).");
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
        view = Calculators.View.Default();
        edit = Coordinates.Edit.Default();
    }

    @AfterAll
    void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    void inactivateWritesAnUncommittedInactiveVersionInTheSubjectsModule() {
        ConceptFacade subject = KometConcept.GREATER_THAN;
        EntityVersion before = latest(subject);
        int versionsBefore = versionCount(subject);

        KometActionResult result = perform(KometConcept.INACTIVATE_COMPONENT, subject);

        assertTrue(result.completed(), result.message());
        assertEquals(versionsBefore + 1, versionCount(subject), "one version written");
        StampEntity<?> stamp = EntityHandle.get(result.writtenVersion().orElseThrow().nid()).expectStamp();
        assertEquals(State.INACTIVE, stamp.state());
        assertEquals(Long.MAX_VALUE, stamp.time(), "left uncommitted, for the person to commit");
        assertEquals(before.moduleNid(), stamp.moduleNid(), "an edit to existing content keeps its module");
        assertEquals(edit.getDefaultPathNid(), stamp.pathNid(), "written on the edit coordinate's path");
        assertEquals(edit.getAuthorNidForChanges(), stamp.authorNid(), "by the edit coordinate's author");
    }

    @Test
    void activateRestoresACommittedInactiveComponent() {
        ConceptFacade subject = KometConcept.LESS_THAN_OR_EQUAL_TO;
        commit(perform(KometConcept.INACTIVATE_COMPONENT, subject));

        KometActionResult result = perform(KometConcept.ACTIVATE_COMPONENT, subject);

        assertTrue(result.completed(), result.message());
        StampEntity<?> stamp = EntityHandle.get(result.writtenVersion().orElseThrow().nid()).expectStamp();
        assertEquals(State.ACTIVE, stamp.state());
        assertEquals(State.ACTIVE, latest(subject).state(), "the view reads the new version");
    }

    /** A second uncommitted version would not supersede the first, so a change on a pending one is refused. */
    @Test
    void aChangeOnAnUncommittedChangeIsRefusedAndWritesNothing() {
        ConceptFacade subject = KometConcept.IS_A;
        assertTrue(perform(KometConcept.INACTIVATE_COMPONENT, subject).completed());
        int versionsBefore = versionCount(subject);

        KometActionResult result = perform(KometConcept.ACTIVATE_COMPONENT, subject);

        assertTrue(EntityFacade.same(KometConcept.ACTION_REFUSED, result.outcome()), result.message());
        assertTrue(result.message().contains("uncommitted change"), result.message());
        assertEquals(versionsBefore, versionCount(subject), "a refusal writes nothing");
    }

    /** The new version carries the latest version's fields; Komet's earlier action copied the first. */
    @Test
    void inactivateCopiesTheLatestVersionsFields() {
        SemanticEntity<SemanticEntityVersion> description = EntityService.get()
                .semanticsForComponentOfPattern(KometConcept.LESS_THAN.nid(), KernelPattern.DESCRIPTION_PATTERN.nid())
                .findFirst().orElseThrow();
        SemanticRecord record = (SemanticRecord) EntityHandle.get(description.nid()).orNull();
        SemanticVersionRecord first = record.versions().getFirst();
        MutableList<Object> changed = first.fieldValues().toList();
        int text = indexOfString(first.fieldValues());
        changed.set(text, first.fieldValues().get(text) + " (revised)");
        Transaction revise = Transaction.make("revise a description");
        StampEntity<?> reviseStamp = revise.getStamp(State.ACTIVE, edit.getAuthorNidForChanges(),
                first.stamp().moduleNid(), edit.getDefaultPathNid());
        SemanticRecord revised = record.with(new SemanticVersionRecord(record, reviseStamp.nid(), changed.toImmutable())).build();
        revise.addComponent(revised);
        Entity.provider().putEntity(revised);
        revise.commit();

        KometActionResult result = perform(KometConcept.INACTIVATE_COMPONENT, EntityProxy.Semantic.make(description.publicId()));

        assertTrue(result.completed(), result.message());
        SemanticRecord after = (SemanticRecord) EntityHandle.get(description.nid()).orNull();
        SemanticVersionRecord written = after.versions().stream()
                .filter(v -> v.stampNid() == result.writtenVersion().orElseThrow().nid())
                .findFirst().orElseThrow();
        assertEquals(changed, written.fieldValues().toList(), "the fields of the latest version, not the first");
    }

    @Test
    void activatingAnActiveComponentIsRefusedAndWritesNothing() {
        ConceptFacade subject = KometConcept.GREATER_THAN_OR_EQUAL_TO;
        int versionsBefore = versionCount(subject);

        KometActionResult result = perform(KometConcept.ACTIVATE_COMPONENT, subject);

        assertTrue(EntityFacade.same(KometConcept.ACTION_REFUSED, result.outcome()), result.message());
        assertTrue(result.writtenVersion().isEmpty());
        assertTrue(result.message().contains("already active"), result.message());
        assertEquals(versionsBefore, versionCount(subject), "a refusal writes nothing");
    }

    @Test
    void anInputForAnotherActionIsRefused() {
        ConceptFacade subject = KometConcept.PART_OF;
        int versionsBefore = versionCount(subject);
        KometAction activate = KometActions.expect(KometConcept.ACTIVATE_COMPONENT);

        KometActionResult result = activate.perform(KometActionInput.of(KometConcept.INACTIVATE_COMPONENT, subject), view, edit);

        assertTrue(EntityFacade.same(KometConcept.ACTION_REFUSED, result.outcome()), result.message());
        assertEquals(versionsBefore, versionCount(subject));
    }

    @Test
    void aStampIsRefused() {
        long stampNid = latest(KometConcept.PART_OF).stampNid();
        EntityFacade stamp = EntityHandle.get(stampNid).expectStamp();

        KometActionResult result = perform(KometConcept.INACTIVATE_COMPONENT, stamp);

        assertTrue(EntityFacade.same(KometConcept.ACTION_REFUSED, result.outcome()), result.message());
    }

    @Test
    void anOfferPerformsItsActionOnItsSubject() {
        ConceptFacade subject = KometConcept.NOT_APPLICABLE;
        KometActionOffer offer = new KometActionOffer(KometActions.expect(KometConcept.INACTIVATE_COMPONENT), subject);

        KometActionResult result = offer.perform(view, edit);

        assertTrue(result.completed(), result.message());
        assertEquals(State.INACTIVE, latest(subject).state());
    }

    private KometActionResult perform(ConceptFacade action, EntityFacade subject) {
        return KometActions.expect(action).perform(KometActionInput.of(action, subject), view, edit);
    }

    private static void commit(KometActionResult result) {
        assertTrue(result.completed(), result.message());
        Transaction.forStamp(result.writtenVersion().orElseThrow().publicId()).orElseThrow().commit();
    }

    private EntityVersion latest(EntityFacade component) {
        return view.<EntityVersion>latest(component.nid()).get();
    }

    private static int versionCount(EntityFacade component) {
        return EntityHandle.getEntityOrThrow(component.nid()).versions().size();
    }

    private static int indexOfString(ImmutableList<Object> fields) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i) instanceof String) {
                return i;
            }
        }
        throw new IllegalStateException("no text field in " + fields);
    }
}
