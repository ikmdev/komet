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
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.fixtures.StarterDataEphemeralProvider;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The canonical instance pool: every caller holding an entity's observable holds the same one,
 * and a change to the entity in the store reaches it.
 */
@ExtendWith({JavaFXThreadExtension.class, StarterDataEphemeralProvider.class})
@RunOnJavaFXThread
class ObservableEntityCanonicalUTestFX {

    private static ObservableComposer newComposer(String comment) {
        return ObservableComposer.create(Calculators.View.Default(), State.ACTIVE, KernelConcept.USER,
                KernelConcept.PRIMORDIAL_MODULE, KernelConcept.DEVELOPMENT_PATH, comment);
    }

    /** A committed description semantic with the given text, on a new committed concept. */
    private static long newCommittedDescription(String text) {
        ObservableComposer composer = newComposer("canonical");
        var conceptComposer = composer.composeConcept(PublicIds.newRandom());
        conceptComposer.save();
        var semanticComposer = composer.composeSemantic(PublicIds.newRandom(), conceptComposer.getEntity(),
                KernelPattern.DESCRIPTION_PATTERN);
        for (var field : semanticComposer.getEditableVersion().getEditableFields()) {
            if (field.getValue() instanceof String) {
                field.setObjectValue(text);
            }
        }
        semanticComposer.save();
        composer.commit();
        return semanticComposer.getEntity().nid();
    }

    private static String text(ObservableSemanticVersion version) {
        for (Object value : version.fieldValues()) {
            if (value instanceof String text) {
                return text;
            }
        }
        throw new IllegalStateException("No text field");
    }

    @Test
    @DisplayName("Two lookups of a nid return the same observable")
    void twoLookupsReturnTheSameInstance() {
        long semanticNid = newCommittedDescription("same");
        ObservableSemantic first = ObservableEntityHandle.get(semanticNid).expectSemantic();
        ObservableSemantic second = ObservableEntityHandle.get(semanticNid).expectSemantic();
        assertSame(first, second);
        assertSame(first, ObservableEntity.packagePrivateGet(EntityHandle.get(semanticNid).expectEntity()),
                "wrapping the stored entity yields the canonical instance too");
    }

    @Test
    @DisplayName("A version written to the store outside any observable reaches the instance a caller holds")
    void aStoreChangeUpdatesTheHeldInstance() {
        long semanticNid = newCommittedDescription("before");
        ObservableSemantic held = ObservableEntityHandle.get(semanticNid).expectSemantic();
        assertEquals(1, held.versions().size());

        SemanticRecord stored = (SemanticRecord) EntityHandle.get(semanticNid).expectEntity();
        MutableList<Object> fields = Lists.mutable.ofAll(stored.versions().getLast().fieldValues());
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i) instanceof String) {
                fields.set(i, "changed elsewhere");
            }
        }
        Transaction elsewhere = new Transaction("elsewhere");
        StampEntity<?> stamp = elsewhere.getStamp(State.ACTIVE, KernelConcept.USER.nid(),
                KernelConcept.PRIMORDIAL_MODULE.nid(), KernelConcept.DEVELOPMENT_PATH.nid());
        SemanticRecord changed = stored.with(new SemanticVersionRecord(stored, stamp.nid(), fields.toImmutable())).build();
        elsewhere.addComponent(changed);
        Entity.provider().putEntity(changed);

        assertEquals(2, held.versions().size(), "the held observable took the new version");
        ObservableSemanticVersion newVersion = held.versionPropertyMap().get(stamp.nid());
        assertEquals("changed elsewhere", text(newVersion));
        assertEquals(Long.MAX_VALUE, newVersion.timeProperty().get());

        elsewhere.commit();
        ObservableEntity.refresh(semanticNid);
        assertNotEquals(Long.MAX_VALUE, newVersion.timeProperty().get(),
                "the version the held observable took is updated in place when its stamp commits");
        assertSame(held, ObservableEntityHandle.get(semanticNid).expectSemantic());
    }

    @Test
    @DisplayName("A committed stamp's observable refreshes without a time mismatch")
    void aCommittedStampRefreshes() {
        long semanticNid = newCommittedDescription("stamp");
        long stampNid = EntityHandle.get(semanticNid).expectEntity().versions().getLast().stampNid();
        // A committed stamp keeps the uncommitted version it began with beside its commit.
        ObservableStamp held = ObservableEntityHandle.get(stampNid).expectStamp();
        assertEquals(1, held.versions().size(), "the commit supersedes the uncommitted version");
        assertDoesNotThrow(() -> ObservableEntity.refresh(stampNid));
        assertDoesNotThrow(() -> ObservableEntity.refresh(stampNid));
        assertTrue(held.versions().getFirst().timeProperty().get() != Long.MAX_VALUE);
        assertSame(held, ObservableEntityHandle.get(stampNid).expectStamp());
    }
}
