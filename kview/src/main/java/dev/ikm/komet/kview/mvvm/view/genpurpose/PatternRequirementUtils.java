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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternRequirement;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * The rules behind the patterns marked Required in the KL editor: when a required pattern counts
 * as satisfied, when a requirement refinement ({@link EditorPatternRequirement}) is met, and which
 * of a section's "Add …" entries meets an unmet requirement. In create mode these gate the actual
 * creation (commit) of a window's component and drive the REQUIRED / "✓ REQUIREMENT MET" chips.
 *
 * <p>Pure: every rule takes the semantics and the calculator it needs as arguments and touches no
 * scene graph, event bus or window state, so the rules are tested with plain JUnit.
 */
public final class PatternRequirementUtils {

    private PatternRequirementUtils() {
    }

    /**
     * One "Add …" entry of a section's edit popup as {@link #getDirectRunEntry} sees it: the pattern it
     * creates a semantic of, and the field values that new semantic starts out with (the
     * constraints of the display filter the entry was built from, keyed by field index — empty
     * for an unfiltered pattern).
     */
    public interface SeededEntry {
        EditorPatternModel pattern();

        Map<Integer, EntityProxy> fieldSeeds();
    }

    /**
     * Whether a required pattern's requirement is met by the passed in semantics — the pattern's
     * semantics against its section's resolved reference component. See
     * {@link #isPatternSatisfied(int, List, List, StampCalculator, int)}.
     */
    public static boolean isPatternSatisfied(EditorPatternModel pattern, List<EntityFacade> semantics,
                                      StampCalculator calculator, int statedAxiomsPatternNid) {
        return isPatternSatisfied(pattern.getNid(), pattern.getRequirements(), semantics, calculator, statedAxiomsPatternNid);
    }

    /**
     * Whether a required pattern's requirement is met: at least one semantic, plus — when the
     * pattern carries requirement refinements authored in the KL editor — at least each
     * refinement's minimum count of semantics matching its field constraints. The stated
     * definition pattern demands more — its definition must contain a necessary or sufficient
     * set, the same condition the classic concept window enforces before creating a concept
     * ({@code ConceptViewModel}'s AXIOM validation) — so removing the definition's last set flips
     * the requirement back to unmet.
     *
     * <p>Uncommitted semantics count: they are found by the entity service once saved, and the
     * calculator's latest version includes them.
     *
     * @param patternNid             the required pattern
     * @param requirements           the pattern's requirement refinements, possibly none
     * @param semantics              the pattern's semantics against the resolved reference component
     * @param calculator             resolves each semantic's latest version
     * @param statedAxiomsPatternNid the stated definition pattern per the view's logic coordinate
     */
    public static boolean isPatternSatisfied(int patternNid, List<EditorPatternRequirement> requirements,
                                      List<EntityFacade> semantics, StampCalculator calculator,
                                      int statedAxiomsPatternNid) {
        if (semantics.isEmpty()) {
            return false;
        }
        if (patternNid == statedAxiomsPatternNid
                && semantics.stream().noneMatch(semantic -> definesNecessaryOrSufficientSet(semantic, calculator))) {
            return false;
        }
        return requirements.stream().allMatch(requirement -> hasEnoughMatchingSemantics(semantics, requirement, calculator));
    }

    /**
     * Whether at least the requirement's minimum count of the passed in semantics match its field
     * constraints ({@link #semanticMatches}).
     */
    public static boolean hasEnoughMatchingSemantics(List<EntityFacade> semantics, EditorPatternRequirement requirement,
                                           StampCalculator calculator) {
        return semantics.stream().filter(semantic -> semanticMatches(semantic, requirement, calculator)).count()
                >= requirement.getMinCount();
    }

    /**
     * Whether the semantic's latest version (uncommitted versions count, like the
     * semantic-existence check) matches the requirement's field constraints.
     */
    public static boolean semanticMatches(EntityFacade semantic, EditorPatternRequirement requirement,
                                             StampCalculator calculator) {
        Latest<SemanticEntityVersion> latestVersion = calculator.latest(semantic.nid());
        return latestVersion.isPresent() && requirement.matches(latestVersion.get().fieldValues());
    }

    /**
     * Whether the given stated-axiom semantic's latest definition (uncommitted versions count,
     * like the semantic-existence check) contains a necessary or sufficient set.
     */
    public static boolean definesNecessaryOrSufficientSet(EntityFacade semantic, StampCalculator calculator) {
        Latest<SemanticEntityVersion> latestVersion = calculator.latest(semantic.nid());
        return latestVersion.isPresent() && definesNecessaryOrSufficientSet(latestVersion.get());
    }

    /**
     * Whether the stated-axiom version's definition — its first and only field — contains a
     * necessary or sufficient set.
     */
    public static boolean definesNecessaryOrSufficientSet(SemanticEntityVersion version) {
        return version.fieldValues().get(0) instanceof DiTreeEntity definition
                && (definition.containsVertexWithMeaning(TinkarTerm.NECESSARY_SET)
                        || definition.containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET));
    }

    /**
     * Whether a semantic seeded with the passed in field values (field index to concept) would
     * match the requirement's field constraints.
     */
    public static boolean seedsWouldMatch(Map<Integer, EntityProxy> fieldSeeds, EditorPatternRequirement requirement) {
        return requirement.getFieldConstraints().entrySet().stream().allMatch(constraint -> {
            EntityProxy seed = fieldSeeds.get(constraint.getKey());
            return seed != null && seed.nid() == constraint.getValue().nid();
        });
    }

    /**
     * The create entry a section's pencil button runs right away, skipping the popup, when the
     * section has no semantic to edit — or empty when the user has to choose. A lone entry is the
     * only thing the popup could offer, so it runs. Otherwise the section's unmet required
     * patterns decide: the entry seeded to meet the pattern's first unmet requirement, or the
     * pattern's single entry when it has no refinement or no entry is seeded to meet it (the user
     * then picks the field values in the form). Several candidates — the stated definition's two
     * set seeds, a required pattern with two filters and no refinement — leave the choice to the
     * popup.
     *
     * @param sectionPatterns        the section's patterns, in section order
     * @param entries                the popup's create entries
     * @param semanticsOfPattern     the pattern's semantics against the section's resolved reference component
     * @param calculator             resolves each semantic's latest version
     * @param statedAxiomsPatternNid the stated definition pattern per the view's logic coordinate
     */
    public static <E extends SeededEntry> Optional<E> getDirectRunEntry(List<? extends EditorPatternModel> sectionPatterns,
                                                                        List<E> entries,
                                                                        Function<EditorPatternModel, List<EntityFacade>> semanticsOfPattern,
                                                                        StampCalculator calculator,
                                                                        int statedAxiomsPatternNid) {
        if (entries.size() == 1) {
            return Optional.of(entries.getFirst());
        }

        for (EditorPatternModel pattern : sectionPatterns) {
            if (!pattern.isRequired()) {
                continue;
            }
            List<EntityFacade> semantics = semanticsOfPattern.apply(pattern);
            if (isPatternSatisfied(pattern, semantics, calculator, statedAxiomsPatternNid)) {
                continue;
            }
            List<E> patternEntries = entries.stream()
                    .filter(entry -> entry.pattern() == pattern)
                    .toList();
            List<E> candidates = patternEntries;

            for (EditorPatternRequirement requirement : pattern.getRequirements()) {
                if (hasEnoughMatchingSemantics(semantics, requirement, calculator)) {
                    continue;
                }
                List<E> meetingRequirement = patternEntries.stream()
                        .filter(entry -> seedsWouldMatch(entry.fieldSeeds(), requirement))
                        .toList();
                if (!meetingRequirement.isEmpty()) {
                    candidates = meetingRequirement;
                }
                break;
            }

            if (candidates.size() == 1) {
                return Optional.of(candidates.getFirst());
            }
        }
        return Optional.empty();
    }
}
