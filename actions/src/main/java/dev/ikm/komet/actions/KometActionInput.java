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

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Objects;

/**
 * One run of an action, as asked for: the shape of a semantic of the action's input pattern. The
 * subject is the semantic's referenced component, the action its {@code Performed action} field,
 * and {@link #fields} whatever else the action's input pattern holds. The id is the input
 * semantic's public id, minted when the run is asked for, so a result can refer to its input.
 *
 * @param id      the input's public id
 * @param action  the action performed, a concept beneath {@code Komet action}
 * @param subject what the action is performed on: a component, a block's element semantic, or an
 *                axiom semantic
 * @param fields  the input pattern's fields beyond the performed action, in order; empty for an
 *                action that takes only its subject
 */
public record KometActionInput(PublicId id, ConceptFacade action, EntityFacade subject, ImmutableList<Object> fields) {

    public KometActionInput {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(fields, "fields");
    }

    /**
     * @param action  the action performed
     * @param subject what it is performed on
     * @return a new input for an action that takes only its subject
     */
    public static KometActionInput of(ConceptFacade action, EntityFacade subject) {
        return new KometActionInput(PublicIds.newRandom(), action, subject, Lists.immutable.empty());
    }
}
