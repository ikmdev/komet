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
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.fixtures.StarterDataEphemeralProvider;
import dev.ikm.tinkar.terms.KernelTerm;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cancelling an edit form: {@link ObservableComposer#discardUnsavedEdits} drops one entity's
 * unsaved edits from a transaction that keeps its edits to every other entity, and a new
 * semantic built in a transaction of its own goes away with that transaction — or stays, edited
 * through another, once that transaction holds its submitted values.
 */
@ExtendWith({JavaFXThreadExtension.class, StarterDataEphemeralProvider.class})
@RunOnJavaFXThread
class ObservableComposerDiscardUTestFX {

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

    @Test
    @DisplayName("Discarding unsaved edits untracks the opened editor: the edit is neither saved nor committed")
    void discardUnsavedEditsDropsAnOpenedEditorsEdit() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer creator = newComposer("create");
        ObservableSemantic description = saveDescription(creator, concept, "original");
        creator.commit();
        int versionsBefore = entity(description.nid()).versions().size();

        ObservableComposer editor = newComposer("edit");
        var semanticComposer = editor.composeSemantic(description.publicId(), concept, KernelTerm.DESCRIPTION_PATTERN);
        setText(semanticComposer, "edited and cancelled");
        assertTrue(editor.hasUncommittedChanges(), "opening an editor tracks the entity");

        editor.discardUnsavedEdits(description.nid());

        assertFalse(editor.hasUncommittedChanges(), "nothing of the entity is left to commit");
        editor.commit();
        assertEquals("original", latestText(description.nid()));
        assertEquals(versionsBefore, entity(description.nid()).versions().size(), "no version was written for the cancelled edit");
    }

    @Test
    @DisplayName("Discarding unsaved edits keeps the version the entity saved uncommitted, which the commit publishes")
    void discardUnsavedEditsKeepsASavedVersion() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer creator = newComposer("create");
        ObservableSemantic description = saveDescription(creator, concept, "original");
        creator.commit();

        ObservableComposer editor = newComposer("edit");
        var semanticComposer = editor.composeSemantic(description.publicId(), concept, KernelTerm.DESCRIPTION_PATTERN);
        setText(semanticComposer, "saved");
        semanticComposer.save();
        setText(semanticComposer, "edited after saving, then cancelled");

        editor.discardUnsavedEdits(description.nid());
        editor.commit();

        assertEquals("saved", latestText(description.nid()));
        assertTrue(entity(description.nid()).committed(), "the saved version was published by the commit");
    }

    @Test
    @DisplayName("Cancelling a new semantic's own transaction cancels it and nothing else")
    void cancellingANewSemanticsOwnTransactionCancelsOnlyIt() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer window = newComposer("window");
        ObservableSemantic kept = saveDescription(window, concept, "kept");
        ObservableComposer newSemantic = newComposer("new semantic");
        ObservableSemantic dropped = saveDescription(newSemantic, concept, "dropped");

        newSemantic.cancel();

        assertTrue(entity(dropped.nid()).canceled(), "the cancelled semantic's only version is canceled");
        assertFalse(entity(dropped.nid()).uncommitted());
        assertFalse(entity(kept.nid()).canceled());

        window.commit();

        assertEquals("kept", latestText(kept.nid()));
        assertTrue(entity(kept.nid()).committed());
        assertTrue(entity(dropped.nid()).canceled(), "the commit did not bring the cancelled semantic back");
    }

    @Test
    @DisplayName("A new semantic submitted through another transaction survives its own transaction's cancel")
    void submittedNewSemanticSurvivesItsOwnTransactionsCancel() {
        ObservableConcept concept = newCommittedConcept();
        ObservableComposer newSemantic = newComposer("new semantic");
        ObservableSemantic description = saveDescription(newSemantic, concept, "seeded");

        ObservableComposer window = newComposer("window");
        var semanticComposer = window.composeSemantic(description.publicId(), concept, KernelTerm.DESCRIPTION_PATTERN);
        assertEquals("seeded", semanticComposer.getEditableVersion().getEditableFields().get(textFieldIndex(semanticComposer)).getValue(),
                "the window edits the version the new semantic was seeded with");
        setText(semanticComposer, "submitted");
        semanticComposer.save();
        newSemantic.cancel();

        assertFalse(entity(description.nid()).canceled(), "the submitted version keeps the semantic alive");
        assertEquals("submitted", latestText(description.nid()));

        window.commit();

        assertEquals("submitted", latestText(description.nid()));
        assertTrue(entity(description.nid()).committed());
        assertFalse(entity(description.nid()).canceled());
    }
}
