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

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The actions Komet and its plugins implement, found through {@link PluggableService}, so an
 * action in a plugin layer is found like one in Komet itself.
 */
public final class KometActions {

    private KometActions() {
    }

    /**
     * @return every implementation found, in discovery order
     */
    public static List<KometAction> all() {
        return PluggableService.load(KometAction.class).stream().map(ServiceLoader.Provider::get).toList();
    }

    /**
     * @param concept an action concept
     * @return the implementation bound to it, if any
     */
    public static Optional<KometAction> forConcept(ConceptFacade concept) {
        return all().stream().filter(action -> EntityFacade.same(action.concept(), concept)).findFirst();
    }

    /**
     * @param concept an action concept Komet implements
     * @return the implementation bound to it
     * @throws IllegalStateException when nothing implements it
     */
    public static KometAction expect(ConceptFacade concept) {
        return forConcept(concept).orElseThrow(
                () -> new IllegalStateException("No KometAction implements " + concept.description()));
    }
}
