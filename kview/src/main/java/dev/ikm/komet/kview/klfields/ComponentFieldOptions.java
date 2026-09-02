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
package dev.ikm.komet.kview.klfields;

import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static dev.ikm.tinkar.terms.TinkarTerm.DEFINITION_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_ACCEPTABILITY;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;
import static dev.ikm.tinkar.terms.TinkarTerm.MODULE;
import static dev.ikm.tinkar.terms.TinkarTerm.PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.STATUS_VALUE;

/**
 * Decides, for a component field of a semantic, whether the user must choose from a
 * predefined set of concepts or may enter any component at all.
 *
 * <p>The classic details windows constrain their description drop-downs to the descendants
 * of a well-known concept (case significance to the descendants of
 * {@code DESCRIPTION_CASE_SIGNIFICANCE}, language to those of {@code LANGUAGE}, and so on).
 * This class carries that same rule set for the pattern-driven semantic editing panels:
 * hand {@link #componentOptions(ViewCalculator, FeatureDefinition)} the field definition of
 * the pattern's field being edited, and it either returns the allowed choices or an empty
 * {@link Optional} meaning the field is unconstrained.
 *
 * <p>A field is matched by its meaning concept first, then by its purpose concept, so
 * pattern-specific meanings (such as a dialect pattern's acceptability field, whose purpose
 * is {@code DESCRIPTION_ACCEPTABILITY}) resolve without per-pattern rules.
 */
public final class ComponentFieldOptions {

    private ComponentFieldOptions() {
    }

    /**
     * The predefined concepts the user may choose from for the given field of a pattern,
     * or an empty {@link Optional} when the field accepts any component.
     *
     * @param viewCalculator  the calculator of the view the field is edited under
     * @param fieldDefinition the pattern's definition of the field (carries the pattern nid,
     *                        field index, meaning and purpose)
     * @return the allowed choices sorted by their description text, or empty for a free-form field
     */
    public static Optional<List<EntityProxy>> componentOptions(ViewCalculator viewCalculator,
                                                               FeatureDefinition fieldDefinition) {
        return optionsForConcept(viewCalculator, fieldDefinition.meaningNid())
                .or(() -> optionsForConcept(viewCalculator, fieldDefinition.purposeNid()));
    }

    private static Optional<List<EntityProxy>> optionsForConcept(ViewCalculator viewCalculator, int conceptNid) {
        // Description types are deliberately NOT the descendants of DESCRIPTION_TYPE — that subtree
        // also holds unrelated metadata concepts (extended relationship type, inferred navigation, ...).
        // Use a fixed set instead: the classic windows' pair (DataModelHelper.fetchDescriptionTypes)
        // plus the definition description type.
        if (conceptNid == DESCRIPTION_TYPE.nid()) {
            return Optional.of(sortedByName(viewCalculator,
                    Stream.of(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, REGULAR_NAME_DESCRIPTION_TYPE,
                            DEFINITION_DESCRIPTION_TYPE)));
        }
        return optionsParentForConcept(conceptNid)
                .map(parent -> sortedByName(viewCalculator, fetchDescendents(viewCalculator, parent)));
    }

    /**
     * The concept whose descendants form the allowed choices for a field matched by the given
     * meaning or purpose concept, or empty when no rule matches.
     */
    private static Optional<ConceptFacade> optionsParentForConcept(int conceptNid) {
        if (conceptNid == DESCRIPTION_CASE_SIGNIFICANCE.nid()) {
            return Optional.of(DESCRIPTION_CASE_SIGNIFICANCE);
        }
        if (conceptNid == LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION.nid() || conceptNid == LANGUAGE.nid()) {
            return Optional.of(LANGUAGE);
        }
        if (conceptNid == DESCRIPTION_ACCEPTABILITY.nid()) {
            return Optional.of(DESCRIPTION_ACCEPTABILITY);
        }
        if (conceptNid == MODULE.nid()) {
            return Optional.of(MODULE);
        }
        if (conceptNid == STATUS_VALUE.nid()) {
            return Optional.of(STATUS_VALUE);
        }
        if (conceptNid == PATH.nid()) {
            return Optional.of(PATH);
        }
        return Optional.empty();
    }

    private static Stream<EntityProxy> fetchDescendents(ViewCalculator viewCalculator, ConceptFacade parent) {
        return viewCalculator.descendentsOf(parent.nid()).intStream()
                .mapToObj(nid -> EntityHandle.get(nid).expectConcept().toProxy());
    }

    private static List<EntityProxy> sortedByName(ViewCalculator viewCalculator, Stream<? extends EntityProxy> options) {
        return options
                .sorted(Comparator.comparing(
                        proxy -> viewCalculator.languageCalculator().getDescriptionTextOrNid(proxy.nid()),
                        String.CASE_INSENSITIVE_ORDER))
                .map(EntityProxy.class::cast)
                .toList();
    }
}
