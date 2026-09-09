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
package dev.ikm.komet.kview.mvvm.view.genpurpose.test;

import dev.ikm.komet.kview.mvvm.view.genpurpose.PatternRequirementUtils;
import dev.ikm.komet.kview.mvvm.view.genpurpose.StatedDefinitionSeeds;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternRequirement;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorDelegate;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.LongLists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The required-pattern rules, exercised on hand-built semantic versions through a stamp
 * calculator that answers only {@code latest(nid)}. The ephemeral store is only there so the
 * term nids (description types, the stated axioms pattern, the set meanings) resolve.
 */
class PatternRequirementUtilsTest {

    // Resolved once the store runs: a term nid asks the store.
    private static int DESCRIPTION_PATTERN;
    private static int STATED_PATTERN;
    private static int FQN;
    private static int REGULAR;
    private static int STAMP;
    private static int COMPONENT;

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("pattern-requirements-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        DESCRIPTION_PATTERN = TinkarTerm.DESCRIPTION_PATTERN.nid();
        STATED_PATTERN = TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid();
        FQN = TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid();
        REGULAR = TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE.nid();
        STAMP = TinkarTerm.ACTIVE_STATE.nid();
        COMPONENT = TinkarTerm.ANONYMOUS_CONCEPT.nid();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    // -- seedsWouldMatch

    @Test
    @DisplayName("A requirement constraining nothing is met by any seeds")
    void unconstrainedRequirementIsMetByAnySeeds() {
        assertTrue(PatternRequirementUtils.seedsWouldMatch(Map.of(), requirement(Map.of(), 1)));
    }

    @Test
    @DisplayName("Seeds meet a requirement when every constrained field is seeded with the constraint's concept")
    void seedsWouldMatchWhenEveryConstraintIsSeeded() {
        EditorPatternRequirement fqnRequired = requirement(Map.of(1, FQN), 1);
        assertTrue(PatternRequirementUtils.seedsWouldMatch(Map.of(1, EntityProxy.Concept.make(FQN)), fqnRequired));
        assertFalse(PatternRequirementUtils.seedsWouldMatch(Map.of(1, EntityProxy.Concept.make(REGULAR)), fqnRequired));
        assertFalse(PatternRequirementUtils.seedsWouldMatch(Map.of(), fqnRequired));
    }

    // -- semanticMatches / hasEnoughMatchingSemantics

    @Test
    @DisplayName("A semantic matches a requirement by its latest version's field values")
    void semanticMatchesByLatestVersion() {
        FakeCalculator calculator = new FakeCalculator();
        EntityFacade fqn = calculator.semantic(DESCRIPTION_PATTERN, "Name", EntityProxy.Concept.make(FQN));
        EntityFacade regular = calculator.semantic(DESCRIPTION_PATTERN, "Name", EntityProxy.Concept.make(REGULAR));
        EditorPatternRequirement fqnRequired = requirement(Map.of(1, FQN), 1);

        assertTrue(PatternRequirementUtils.semanticMatches(fqn, fqnRequired, calculator));
        assertFalse(PatternRequirementUtils.semanticMatches(regular, fqnRequired, calculator));
    }

    @Test
    @DisplayName("A semantic with no latest version under the coordinate matches nothing")
    void semanticWithoutLatestVersionMatchesNothing() {
        FakeCalculator calculator = new FakeCalculator();
        EntityFacade unresolved = EntityProxy.Semantic.make(newNid());

        assertFalse(PatternRequirementUtils.semanticMatches(unresolved, requirement(Map.of(), 1), calculator));
    }

    @Test
    @DisplayName("A requirement is met once its minimum count of matching semantics exists")
    void requirementIsMetByMinimumCount() {
        FakeCalculator calculator = new FakeCalculator();
        List<EntityFacade> semantics = List.of(
                calculator.semantic(DESCRIPTION_PATTERN, "Name", EntityProxy.Concept.make(FQN)),
                calculator.semantic(DESCRIPTION_PATTERN, "Other name", EntityProxy.Concept.make(REGULAR)));

        assertTrue(PatternRequirementUtils.hasEnoughMatchingSemantics(semantics, requirement(Map.of(1, FQN), 1), calculator));
        assertFalse(PatternRequirementUtils.hasEnoughMatchingSemantics(semantics, requirement(Map.of(1, FQN), 2), calculator));
        assertTrue(PatternRequirementUtils.hasEnoughMatchingSemantics(semantics, requirement(Map.of(), 2), calculator));
    }

    // -- isPatternSatisfied

    @Test
    @DisplayName("A required pattern with no semantics is never satisfied")
    void noSemanticsIsNotSatisfied() {
        assertFalse(PatternRequirementUtils.isPatternSatisfied(DESCRIPTION_PATTERN, List.of(), List.of(),
                new FakeCalculator(), STATED_PATTERN));
    }

    @Test
    @DisplayName("A required pattern without refinements is satisfied by any semantic")
    void anySemanticSatisfiesAnUnrefinedPattern() {
        FakeCalculator calculator = new FakeCalculator();
        List<EntityFacade> semantics = List.of(
                calculator.semantic(DESCRIPTION_PATTERN, "Name", EntityProxy.Concept.make(REGULAR)));

        assertTrue(PatternRequirementUtils.isPatternSatisfied(DESCRIPTION_PATTERN, List.of(), semantics, calculator, STATED_PATTERN));
    }

    @Test
    @DisplayName("Every refinement of a required pattern has to be met")
    void everyRefinementHasToBeMet() {
        FakeCalculator calculator = new FakeCalculator();
        List<EntityFacade> semantics = List.of(
                calculator.semantic(DESCRIPTION_PATTERN, "Name", EntityProxy.Concept.make(FQN)));
        EditorPatternRequirement fqnRequired = requirement(Map.of(1, FQN), 1);
        EditorPatternRequirement regularRequired = requirement(Map.of(1, REGULAR), 1);

        assertTrue(PatternRequirementUtils.isPatternSatisfied(DESCRIPTION_PATTERN, List.of(fqnRequired), semantics,
                calculator, STATED_PATTERN));
        assertFalse(PatternRequirementUtils.isPatternSatisfied(DESCRIPTION_PATTERN, List.of(fqnRequired, regularRequired),
                semantics, calculator, STATED_PATTERN));
    }

    @Test
    @DisplayName("The stated definition pattern is satisfied only by a definition holding a set")
    void statedPatternNeedsASet() {
        FakeCalculator calculator = new FakeCalculator();
        List<EntityFacade> noSets = List.of(calculator.semantic(STATED_PATTERN, emptyDefinition()));
        List<EntityFacade> necessary = List.of(calculator.semantic(STATED_PATTERN,
                StatedDefinitionSeeds.seedDefinition(true)));
        List<EntityFacade> sufficient = List.of(calculator.semantic(STATED_PATTERN,
                StatedDefinitionSeeds.seedDefinition(false)));

        assertFalse(PatternRequirementUtils.isPatternSatisfied(STATED_PATTERN, List.of(), noSets, calculator, STATED_PATTERN));
        assertTrue(PatternRequirementUtils.isPatternSatisfied(STATED_PATTERN, List.of(), necessary, calculator, STATED_PATTERN));
        assertTrue(PatternRequirementUtils.isPatternSatisfied(STATED_PATTERN, List.of(), sufficient, calculator, STATED_PATTERN));
    }

    @Test
    @DisplayName("Only the stated definition pattern is held to the set condition")
    void onlyTheStatedPatternNeedsASet() {
        FakeCalculator calculator = new FakeCalculator();
        List<EntityFacade> noSets = List.of(calculator.semantic(DESCRIPTION_PATTERN, emptyDefinition()));

        assertTrue(PatternRequirementUtils.isPatternSatisfied(DESCRIPTION_PATTERN, List.of(), noSets, calculator, STATED_PATTERN));
    }

    // -- definesNecessaryOrSufficientSet

    @Test
    @DisplayName("A version whose first field is not a definition defines no set")
    void nonDefinitionFieldDefinesNoSet() {
        FakeCalculator calculator = new FakeCalculator();
        EntityFacade semantic = calculator.semantic(STATED_PATTERN, "not a definition");

        assertFalse(PatternRequirementUtils.definesNecessaryOrSufficientSet(semantic, calculator));
    }

    // -- directEntry (the branches that need no pattern model)

    @Test
    @DisplayName("A lone create entry runs directly")
    void loneEntryRunsDirectly() {
        Entry only = new Entry(null, Map.of());

        assertSame(only, PatternRequirementUtils.getDirectRunEntry(List.of(), List.of(only),
                _ -> List.of(), new FakeCalculator(), STATED_PATTERN).orElseThrow());
    }

    @Test
    @DisplayName("Several entries with no required pattern to decide leave the choice to the popup")
    void severalEntriesWithoutRequiredPatternLeaveTheChoice() {
        List<Entry> entries = List.of(new Entry(null, Map.of()), new Entry(null, Map.of()));

        assertTrue(PatternRequirementUtils.getDirectRunEntry(List.of(), entries,
                _ -> List.of(), new FakeCalculator(), STATED_PATTERN).isEmpty());
    }

    // -- fixtures

    private record Entry(EditorPatternModel pattern, Map<Integer, EntityProxy> fieldSeeds)
            implements PatternRequirementUtils.SeededEntry {
    }

    private static EditorPatternRequirement requirement(Map<Integer, Integer> constraints, int minCount) {
        EditorPatternRequirement requirement = new EditorPatternRequirement();
        constraints.forEach((fieldIndex, conceptNid) ->
                requirement.getFieldConstraints().put(fieldIndex, EntityProxy.Concept.make(conceptNid)));
        requirement.setMinCount(minCount);
        return requirement;
    }

    /** A definition with no set at all — the tree a builder yields before any axiom is added. */
    private static DiTreeEntity emptyDefinition() {
        return switch (new LogicalExpressionBuilder().build().sourceGraph()) {
            case DiTreeEntity tree -> tree;
            case DiTreeEntity.Builder treeBuilder -> treeBuilder.build();
            default -> throw new IllegalStateException("Unexpected source graph type");
        };
    }

    private static int newNid() {
        return PrimitiveData.nid(PublicIds.newRandom());
    }

    /**
     * A stamp calculator that answers {@code latest(nid)} from the semantic versions handed to
     * it and nothing else.
     */
    private static final class FakeCalculator implements StampCalculatorDelegate {
        private final Map<Integer, SemanticEntityVersion> latestByNid = new HashMap<>();

        /** Registers a semantic of the pattern whose latest version holds the field values; returns it. */
        EntityFacade semantic(int patternNid, Object... fieldValues) {
            int nid = newNid();
            UUID uuid = UUID.randomUUID();
            SemanticRecord chronology = new SemanticRecord(uuid.getMostSignificantBits(),
                    uuid.getLeastSignificantBits(), LongLists.immutable.empty(), nid, patternNid, COMPONENT,
                    Lists.immutable.empty());
            latestByNid.put(nid, new SemanticVersionRecord(chronology, STAMP, Lists.immutable.of(fieldValues)));
            return EntityProxy.Semantic.make(nid);
        }

        @Override
        public StampCalculator stampCalculator() {
            throw new UnsupportedOperationException("The rules only ask for latest(nid)");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V extends EntityVersion> Latest<V> latest(int nid) {
            SemanticEntityVersion version = latestByNid.get(nid);
            return version == null ? Latest.empty() : (Latest<V>) Latest.of(version);
        }
    }
}
