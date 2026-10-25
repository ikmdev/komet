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
import dev.ikm.tinkar.fixtures.StarterDataEphemeralProvider;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A saved edit stays saved when the version it was saved as comes back from the store. When the
 * transaction ends elsewhere (published from the journal's unpublished-changes panel), the held
 * observable takes the stored version in place of the one the editable wrote; the editable must
 * still read as having nothing unsaved, or its next save opens a transaction of its own and leaves
 * a second version.
 */
@ExtendWith({JavaFXThreadExtension.class, StarterDataEphemeralProvider.class})
@RunOnJavaFXThread
class ObservableEditableSavedEditUTestFX {

    @Test
    @DisplayName("A saved edit reads as saved after its transaction is published elsewhere")
    void aSavedEditStaysSavedWhenPublishedElsewhere() {
        ObservableComposer composer = ObservableComposer.create(Calculators.View.Default(), State.ACTIVE,
                KernelConcept.USER, KernelConcept.PRIMORDIAL_MODULE, KernelConcept.DEVELOPMENT_PATH, "saved edit");
        var conceptComposer = composer.composeConcept(PublicIds.newRandom());
        conceptComposer.save();
        var semanticComposer = composer.composeSemantic(PublicIds.newRandom(), conceptComposer.getEntity(),
                KernelPattern.DESCRIPTION_PATTERN);
        ObservableSemanticVersion.Editable editable = semanticComposer.getEditableVersion();
        for (var field : editable.getEditableFields()) {
            if (field.getValue() instanceof String) {
                field.setObjectValue("published elsewhere");
            }
        }
        semanticComposer.save();
        long semanticNid = semanticComposer.getEntity().nid();

        composer.getTransaction().commit();
        ObservableEntity.refresh(semanticNid);

        assertFalse(editable.hasUnsavedChanges(), "the edit was saved, and its stored version is the same");
        editable.save();
        assertEquals(1, EntityHandle.get(semanticNid).expectEntity().versions().size(),
                "saving again, with nothing unsaved, writes no second version");
    }
}
