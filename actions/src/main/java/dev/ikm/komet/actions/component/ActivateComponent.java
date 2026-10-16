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
package dev.ikm.komet.actions.component;

import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;

/**
 * {@link KometConcept#ACTIVATE_COMPONENT}: Makes an inactive component active again, in a new uncommitted version with every field as
 * the latest version has it.
 */
public final class ActivateComponent extends ComponentStateAction {

    public ActivateComponent() {
        super(State.ACTIVE);
    }

    @Override
    public ConceptFacade concept() {
        return KometConcept.ACTIVATE_COMPONENT;
    }
}
