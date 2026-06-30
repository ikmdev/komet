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
package dev.ikm.komet.kview.mvvm.viewmodel.test;

import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampProperties;
import dev.ikm.tinkar.entity.ConceptEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression guard for <a href="https://github.com/ikmdev/komet-desktop/issues/21">komet-desktop#21</a>.
 *
 * <p>Changing a concept's STAMP status (e.g. Active &rarr; Inactive) through the STAMP
 * slideout and pressing Submit threw
 * {@code ClassCastException: Property 'AUTHOR' expected ObservableConcept but found ConceptRecord}.
 * The {@code AUTHOR}, {@code MODULE}, and {@code PATH} properties were registered with a
 * runtime type of {@code ObservableConcept}, but their write sites store plain
 * {@link ConceptEntity} values &mdash; the edit coordinate's author
 * ({@code getAuthorForChanges()}) is a {@code ConceptRecord}, and module/path combo-box
 * selections are plain concepts from the available lists. Because
 * {@code TypedProperty.getFrom} enforces the registered runtime type, submit failed.
 *
 * <p>The fix widens the registered type to {@link ConceptEntity}, which every reader and
 * the downstream {@code ObservableComposer.create(EntityFacade...)} already accept (an
 * {@code ObservableConcept} is itself a {@code ConceptEntity}, so values resolved from the
 * database still pass). These assertions fail if the type is ever narrowed back.
 */
public class StampPropertiesTypeTest {

    @Test
    public void authorAcceptsAnyConceptEntity() {
        assertEquals(ConceptEntity.class, StampProperties.Keys.AUTHOR.property().getType(),
                "AUTHOR must accept any ConceptEntity, not only ObservableConcept (komet-desktop#21)");
    }

    @Test
    public void moduleAcceptsAnyConceptEntity() {
        assertEquals(ConceptEntity.class, StampProperties.Keys.MODULE.property().getType(),
                "MODULE must accept any ConceptEntity, not only ObservableConcept (komet-desktop#21)");
    }

    @Test
    public void pathAcceptsAnyConceptEntity() {
        assertEquals(ConceptEntity.class, StampProperties.Keys.PATH.property().getType(),
                "PATH must accept any ConceptEntity, not only ObservableConcept (komet-desktop#21)");
    }
}
