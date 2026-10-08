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
package dev.ikm.komet.framework.observable.read;

import dev.ikm.tinkar.common.id.LongIdList;
import dev.ikm.tinkar.common.id.LongIdSet;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Concept sets read through a view calculator's navigation: the descendants, leaf
 * descendants and children of a concept, as the concept entities a chooser lists.
 * Every read is bound to the calculator the caller passes, never a default view.
 */
public final class NavigationReads {

    private NavigationReads() {
    }

    /** Every concept below {@code concept} under the calculator's navigation. */
    public static Set<ConceptEntity> descendantsOf(ViewCalculator viewCalculator, EntityFacade concept) {
        Objects.requireNonNull(viewCalculator, "View calculator cannot be null");
        Objects.requireNonNull(concept, "Concept cannot be null");
        return concepts(viewCalculator.descendentsOf(concept.nid()));
    }

    /** The concepts below {@code concept} that have no children of their own. */
    public static Set<ConceptEntity> leafDescendantsOf(ViewCalculator viewCalculator, EntityFacade concept) {
        Objects.requireNonNull(viewCalculator, "View calculator cannot be null");
        Objects.requireNonNull(concept, "Concept cannot be null");
        return viewCalculator.descendentsOf(concept.nid()).longStream()
                .filter(nid -> viewCalculator.childrenOf(nid).isEmpty())
                .mapToObj(nid -> EntityHandle.get(nid).expectConcept())
                .collect(Collectors.toSet());
    }

    /** The concepts directly below {@code concept}. */
    public static Set<ConceptEntity> childrenOf(ViewCalculator viewCalculator, EntityFacade concept) {
        Objects.requireNonNull(viewCalculator, "View calculator cannot be null");
        Objects.requireNonNull(concept, "Concept cannot be null");
        LongIdList children = viewCalculator.navigationCalculator().childrenOf(concept.nid());
        return children.longStream()
                .mapToObj(nid -> EntityHandle.get(nid).expectConcept())
                .collect(Collectors.toSet());
    }

    private static Set<ConceptEntity> concepts(LongIdSet nids) {
        return nids.longStream()
                .mapToObj(nid -> EntityHandle.get(nid).expectConcept())
                .collect(Collectors.toSet());
    }
}
