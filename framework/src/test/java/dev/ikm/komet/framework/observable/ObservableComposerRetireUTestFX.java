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
import dev.ikm.tinkar.fixtures.StarterDataEphemeralProvider;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Retiring an existing semantic: a composer whose default state is {@link State#INACTIVE} edits a
 * committed semantic, changing nothing but its state, and saves. The save must leave an
 * uncommitted inactive version, which the commit then publishes. Komet retires a pattern
 * membership this way ({@code ConceptController.setMembership}), and the assistant would retire a
 * description the same way.
 */
@ExtendWith({JavaFXThreadExtension.class, StarterDataEphemeralProvider.class})
@RunOnJavaFXThread
class ObservableComposerRetireUTestFX {

    private static ObservableComposer newComposer(State state, String comment) {
        return ObservableComposer.create(Calculators.View.Default(), state, KernelConcept.USER,
                KernelConcept.PRIMORDIAL_MODULE, KernelConcept.DEVELOPMENT_PATH, comment);
    }

    /** A committed concept with one committed description semantic, the semantic returned. */
    private static ObservableSemantic newCommittedDescription(String text) {
        ObservableComposer composer = newComposer(State.ACTIVE, "create");
        ObservableComposer.EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> conceptComposer =
                composer.composeConcept(PublicIds.newRandom());
        conceptComposer.save();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticComposer =
                composer.composeSemantic(PublicIds.newRandom(), conceptComposer.getEntity(), KernelPattern.DESCRIPTION_PATTERN);
        setText(semanticComposer, text);
        semanticComposer.save();
        composer.commit();
        assertTrue(EntityHandle.get(semanticComposer.getEntity().nid()).expectEntity().committed());
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

    /** The semantic's uncommitted versions, as the store holds them. */
    private static List<EntityVersion> uncommitted(ObservableSemantic semantic) {
        return EntityHandle.get(semantic.nid()).expectEntity().versions().stream()
                .map(EntityVersion.class::cast).filter(EntityVersion::uncommitted).toList();
    }

    /** The states of the semantic's versions, as the store holds them. */
    private static List<State> states(ObservableSemantic semantic) {
        return EntityHandle.get(semantic.nid()).expectEntity().versions().stream()
                .map(EntityVersion.class::cast).map(EntityVersion::state).toList();
    }

    @Test
    @DisplayName("Retiring with the editable version opened first saves an inactive uncommitted version")
    void retiringWithTheEditableVersionOpenedSavesAnInactiveVersion() {
        ObservableSemantic description = newCommittedDescription("to retire");
        ObservableComposer retirer = newComposer(State.INACTIVE, "retire");

        var editor = editorOf(retirer, description);
        editor.getEditableVersion();
        editor.save();

        List<EntityVersion> saved = uncommitted(description);
        assertEquals(1, saved.size(), "the save left one uncommitted version; versions: " + states(description));
        assertEquals(State.INACTIVE, saved.getFirst().state());

        retirer.commit();
        assertTrue(states(description).contains(State.INACTIVE),
                "the commit published an inactive version; versions: " + states(description));
    }

    @Test
    @DisplayName("Retiring as setMembership does, compose then save, saves an inactive uncommitted version")
    void retiringByComposeAndSaveSavesAnInactiveVersion() {
        ObservableSemantic description = newCommittedDescription("to retire");
        ObservableComposer retirer = newComposer(State.INACTIVE, "retire");

        editorOf(retirer, description).save();

        List<EntityVersion> saved = uncommitted(description);
        assertEquals(1, saved.size(), "the save left one uncommitted version; versions: " + states(description));
        assertEquals(State.INACTIVE, saved.getFirst().state());

        retirer.commit();
        assertTrue(states(description).contains(State.INACTIVE),
                "the commit published an inactive version; versions: " + states(description));
    }

    @Test
    @DisplayName("Control: retiring while also changing a field saves an inactive uncommitted version")
    void retiringWithAFieldChangeSavesAnInactiveVersion() {
        ObservableSemantic description = newCommittedDescription("to retire");
        ObservableComposer retirer = newComposer(State.INACTIVE, "retire");

        var editor = editorOf(retirer, description);
        setText(editor, "retired, with new text");
        editor.save();

        List<EntityVersion> saved = uncommitted(description);
        assertEquals(1, saved.size(), "the save left one uncommitted version; versions: " + states(description));
        assertEquals(State.INACTIVE, saved.getFirst().state());
    }
}
