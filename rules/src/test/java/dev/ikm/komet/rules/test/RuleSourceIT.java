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
package dev.ikm.komet.rules.test;

import dev.ikm.komet.framework.performance.Measures;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.ListedRuleSet;
import dev.ikm.komet.framework.rulebase.RuleFinding;
import dev.ikm.komet.framework.rulebase.RuleSource;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.actions.KometActionMenuItem;
import dev.ikm.komet.rules.evrete.EvreteRulesService;
import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rules written while Komet runs: a {@link RuleSource} is compiled when it is put in use, its
 * rule fires beside Komet's own on a focused component of the starter set, and a source that
 * cannot take part is kept out of use with its findings while the other rules go on firing.
 *
 * <p>The rule the sources here carry is one no rule of Komet's has: it offers to activate a
 * component that is active. Its offer is thereby told apart from Komet's own, which offers to
 * inactivate that component.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RuleSourceIT {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

    private static final String KOMETS_OFFER = "Inactivate component";
    private static final String THE_SOURCES_OFFER = "Activate component";
    private static final String RULE_NAME = "Offer to activate an active component";

    /** The line of {@link #sourceWith} that a test replaces. */
    private static final String THE_OFFER = "offer(KometConcept.ACTIVATE_COMPONENT, version.entity());";

    private ViewProperties viewProperties;
    private EvreteRulesService rules;

    @BeforeAll
    void startStore() {
        assertTrue(PB_STARTER_DATA.exists(), "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath());
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
        viewProperties = new ObservableViewNoOverride(Coordinates.View.DefaultView()).makeOverridableViewProperties();
    }

    @AfterAll
    void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void startRulesWithNoSource() throws Exception {
        rules = new EvreteRulesService();
    }

    @Test
    void aRuleSourcePutInUseFiresBesideKometsOwnRules() {
        ListedRuleSet listed = rules.putRuleSource(new RuleSource(UUID.randomUUID(), "Activation rules", source()));

        assertTrue(listed.inUse(), () -> "in use, but found: " + listed.findings());
        assertEquals(Lists.immutable.of(RULE_NAME), listed.ruleNames());
        assertEquals(List.of(THE_SOURCES_OFFER, KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN));
    }

    @Test
    void theRuleSetsListedAreKometsOwnThenTheRuleSource() {
        RuleSource source = new RuleSource(UUID.randomUUID(), "Activation rules", source());
        rules.putRuleSource(source);

        ImmutableList<ListedRuleSet> listed = rules.ruleSets();

        ListedRuleSet kometsFirst = listed.getFirst();
        assertEquals(ListedRuleSet.Origin.KOMET, kometsFirst.origin());
        assertEquals("Component focus rules", kometsFirst.name());
        assertTrue(kometsFirst.inUse());
        assertTrue(kometsFirst.ruleNames().contains("Component focused and active"),
                () -> "its rules are named: " + kometsFirst.ruleNames());
        assertEquals(new ListedRuleSet(ListedRuleSet.Origin.SOURCE, "Activation rules", Lists.immutable.of(RULE_NAME),
                true, true, Lists.immutable.empty(), Optional.of(source)), listed.getLast());
    }

    @Test
    void aSourceThatDoesNotCompileIsKeptOutOfUseWithTheCompilersLine() {
        String unknownType = "NoSuchType nothing = null;";
        String broken = sourceWith(unknownType);

        ListedRuleSet listed = rules.putRuleSource(new RuleSource(UUID.randomUUID(), "Broken rules", broken));

        assertFalse(listed.inUse());
        assertEquals(1, listed.findings().size(), () -> "one finding: " + listed.findings());
        RuleFinding finding = listed.findings().getFirst();
        assertEquals(lineOf(unknownType, broken), finding.line());
        assertTrue(finding.message().contains("cannot find symbol"), finding::message);
        assertEquals(listed, rules.ruleSets().getLast(), "the source stays listed, with its findings");
        assertEquals(List.of(KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN));
    }

    @Test
    void anEditedSourceTakesThePlaceOfTheOneWithItsIdentity() {
        UUID id = UUID.randomUUID();
        rules.putRuleSource(new RuleSource(id, "Activation rules", source()));

        ListedRuleSet edited = rules.putRuleSource(new RuleSource(id, "Activation rules",
                source().replace("isComponentActive", "isComponentInactive")));

        assertTrue(edited.inUse(), () -> "in use, but found: " + edited.findings());
        assertEquals(1, rules.ruleSets().count(ruleSet -> ruleSet.origin() == ListedRuleSet.Origin.SOURCE));
        assertEquals(List.of(KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN),
                "the edited rule fires on an inactive component only");
    }

    @Test
    void aRemovedSourceNoLongerFiresAndIsNoLongerListed() {
        UUID id = UUID.randomUUID();
        rules.putRuleSource(new RuleSource(id, "Activation rules", source()));

        assertTrue(rules.removeRuleSource(id));

        assertEquals(0, rules.ruleSets().count(ruleSet -> ruleSet.origin() == ListedRuleSet.Origin.SOURCE));
        assertEquals(List.of(KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN));
        assertFalse(rules.removeRuleSource(id), "there is nothing left to remove");
    }

    @Test
    void checkingASourceSaysWhatItWouldGiveWithoutPuttingItInUse() {
        ListedRuleSet checked = rules.checkRuleSource(new RuleSource(UUID.randomUUID(), "Activation rules", source()));

        assertTrue(checked.inUse(), () -> "would be in use, but found: " + checked.findings());
        assertEquals(Lists.immutable.of(RULE_NAME), checked.ruleNames());
        assertEquals(0, rules.ruleSets().count(ruleSet -> ruleSet.origin() == ListedRuleSet.Origin.SOURCE));
        assertEquals(List.of(KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN));
    }

    @Test
    void aRuleThatThrowsWhenFiredIsTakenOutOfUseAndTheOtherRulesStillFire() {
        String throwing = "throw new IllegalStateException(\"no offer today\");";
        String source = sourceWith(throwing);
        ListedRuleSet put = rules.putRuleSource(new RuleSource(UUID.randomUUID(), "Throwing rules", source));
        assertTrue(put.inUse(), () -> "it compiles, so it starts in use: " + put.findings());

        List<String> offered = offeredOn(KometConcept.GREATER_THAN);

        assertEquals(List.of(KOMETS_OFFER), offered);
        ListedRuleSet listed = rules.ruleSets().getLast();
        assertFalse(listed.inUse());
        RuleFinding finding = listed.findings().getFirst();
        assertEquals(lineOf(throwing, source), finding.line());
        assertTrue(finding.message().contains("no offer today"), finding::message);
    }

    @Test
    void aRuleNamedAsOneInUseWaitsUntilThatNameIsFree() {
        UUID first = UUID.randomUUID();
        rules.putRuleSource(new RuleSource(first, "First rules", source()));

        ListedRuleSet second = rules.putRuleSource(new RuleSource(UUID.randomUUID(), "Second rules",
                source().replace("class OfferActivation", "class OfferActivationAgain")));

        assertFalse(second.inUse());
        assertTrue(second.findings().getFirst().message().contains("\"" + RULE_NAME + "\" is already in use"),
                () -> second.findings().getFirst().message());
        assertTrue(second.findings().getFirst().message().contains("First rules"));

        rules.removeRuleSource(first);

        assertTrue(rules.ruleSets().getLast().inUse(), "with the name free, the second source is in use");
        assertEquals("Second rules", rules.ruleSets().getLast().name());
    }

    @Test
    void aConditionWrittenAsTextIsRefusedWithWhatToWriteInstead() {
        String literal = source().replace(
                "@Where(methods = {",
                "@Where(value = \"$observation.subject() != null\", methods = {");

        ListedRuleSet listed = rules.putRuleSource(new RuleSource(UUID.randomUUID(), "Literal rules", literal));

        assertFalse(listed.inUse());
        assertTrue(listed.findings().getFirst().message().contains("Write the condition as a boolean method"),
                () -> listed.findings().getFirst().message());
    }

    @Test
    void textThatIsNotJavaIsRefused() {
        ListedRuleSet listed = rules.putRuleSource(
                new RuleSource(UUID.randomUUID(), "Not rules", "Offer to activate what is active."));

        assertFalse(listed.inUse());
        assertEquals(1, listed.findings().size());
    }

    @Test
    void aBraceInACommentAboveTheClassDoesNotCutTheClassNameShort() {
        // Wherever the brace falls: the class name was once read up to the place of the first brace
        // in the text, counted with its comments, in the text without them. The comment's own
        // "class" is not the declaration either.
        for (int length = 0; length <= 40; length++) {
            String comment = "/** " + "x".repeat(length) + " {@code RulesBase} holds the conditions of this class of rules. */\n";
            String commented = source().replace("public class OfferActivation", comment + "public class OfferActivation");
            ListedRuleSet checked = rules.checkRuleSource(new RuleSource(UUID.randomUUID(), "Commented rules", commented));
            int before = length;
            assertTrue(checked.findings().isEmpty(),
                    () -> "with " + before + " characters before the brace in the comment: " + checked.findings());
            assertEquals(Lists.immutable.of(RULE_NAME), checked.ruleNames());
        }
    }

    @Test
    void aProposedRuleSourceIsCompiledAndListedButItsRuleDoesNotFire() {
        RuleSource source = new RuleSource(UUID.randomUUID(), "Activation rules", source());

        ListedRuleSet proposed = rules.proposeRuleSource(source);

        assertEquals(new ListedRuleSet(ListedRuleSet.Origin.SOURCE, "Activation rules", Lists.immutable.of(RULE_NAME),
                false, false, Lists.immutable.empty(), Optional.of(source)), proposed);
        assertEquals(proposed, rules.ruleSets().getLast(), "the proposal is listed");
        assertEquals(List.of(KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN));
    }

    @Test
    void aProposedRuleSourceRunsNoneOfItsCodeUntilItIsAcceptedAndFired() {
        String ran = "rule.source.it." + UUID.randomUUID();
        String tattling = source().replace("public class OfferActivation extends RulesBase {", """
                public class OfferActivation extends RulesBase {
                    static { System.setProperty("%1$s.static", "ran"); }
                    public OfferActivation() { System.setProperty("%1$s.constructor", "ran"); }
                """.formatted(ran));
        UUID id = UUID.randomUUID();

        ListedRuleSet proposed = rules.proposeRuleSource(new RuleSource(id, "Activation rules", tattling));
        offeredOn(KometConcept.GREATER_THAN);

        assertTrue(proposed.findings().isEmpty(), () -> "it compiles: " + proposed.findings());
        assertEquals(null, System.getProperty(ran + ".static"), "its class is not initialized");
        assertEquals(null, System.getProperty(ran + ".constructor"), "nothing of it is made");

        rules.acceptRuleSource(id);
        offeredOn(KometConcept.GREATER_THAN);

        assertEquals("ran", System.getProperty(ran + ".constructor"), "accepted and fired, it runs");
    }

    @Test
    void anAcceptedProposalIsInUseAndItsRuleFires() {
        UUID id = UUID.randomUUID();
        rules.proposeRuleSource(new RuleSource(id, "Activation rules", source()));

        Optional<ListedRuleSet> accepted = rules.acceptRuleSource(id);

        assertTrue(accepted.orElseThrow().accepted());
        assertTrue(accepted.orElseThrow().inUse());
        assertEquals(1, rules.ruleSets().count(ruleSet -> ruleSet.origin() == ListedRuleSet.Origin.SOURCE),
                "it is listed once, as accepted, and no longer as a proposal");
        assertEquals(List.of(THE_SOURCES_OFFER, KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN));
        assertEquals(Optional.empty(), rules.acceptRuleSource(id), "there is nothing left to accept");
    }

    @Test
    void aProposedEditLeavesTheRuleInUseAsItIsUntilTheEditIsAccepted() {
        UUID id = UUID.randomUUID();
        rules.putRuleSource(new RuleSource(id, "Activation rules", source()));

        ListedRuleSet proposedEdit = rules.proposeRuleSource(new RuleSource(id, "Activation rules",
                source().replace("isComponentActive", "isComponentInactive")));

        assertTrue(proposedEdit.findings().isEmpty(),
                () -> "the rule's name is no clash with the source it would replace: " + proposedEdit.findings());
        assertEquals(List.of(THE_SOURCES_OFFER, KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN),
                "the rule in use still fires as it was");

        rules.acceptRuleSource(id);

        assertEquals(List.of(KOMETS_OFFER), offeredOn(KometConcept.GREATER_THAN),
                "the accepted edit fires on an inactive component only");
    }

    @Test
    void aDiscardedProposalIsNoLongerListedAndTheRuleInUseStays() {
        UUID id = UUID.randomUUID();
        rules.putRuleSource(new RuleSource(id, "Activation rules", source()));
        rules.proposeRuleSource(new RuleSource(id, "Activation rules", sourceWith("NoSuchType nothing = null;")));

        assertTrue(rules.discardProposedRuleSource(id));

        assertEquals(1, rules.ruleSets().count(ruleSet -> ruleSet.origin() == ListedRuleSet.Origin.SOURCE));
        assertTrue(rules.ruleSets().getLast().inUse());
        assertFalse(rules.discardProposedRuleSource(id), "there is nothing left to discard");
    }

    @Test
    void aProposalThatDoesNotCompileIsListedWithTheCompilersLine() {
        String unknownType = "NoSuchType nothing = null;";
        String broken = sourceWith(unknownType);

        ListedRuleSet proposed = rules.proposeRuleSource(new RuleSource(UUID.randomUUID(), "Broken rules", broken));

        assertFalse(proposed.accepted());
        assertEquals(lineOf(unknownType, broken), proposed.findings().getFirst().line());
    }

    /** The offers made on the latest version of a focused component, by name, in name order. */
    private List<String> offeredOn(ConceptFacade subject) {
        EntityVersion latest = viewProperties.calculator().<EntityVersion>latest(subject.nid()).get();
        ObservationRecord observation = new ObservationRecord(Topic.COMPONENT_FOCUSED, latest, Measures.present());
        List<Consequence<?>> consequences = rules.execute("Component context menu", Lists.immutable.of(observation),
                viewProperties, viewProperties.nodeView().editCoordinate()).castToList();
        return consequences.stream()
                .filter(c -> c instanceof ConsequenceAction a && a.generatedAction() instanceof KometActionMenuItem)
                .map(c -> ((KometActionMenuItem) ((ConsequenceAction) c).generatedAction()).getText())
                .sorted()
                .toList();
    }

    private static String source() {
        return sourceWith(THE_OFFER);
    }

    /** Rule source as a person or the Assistant would write it, with one statement in the rule's body. */
    private static String sourceWith(String statement) {
        return """
                package authored.rules;

                import dev.ikm.komet.framework.performance.impl.ObservationRecord;
                import dev.ikm.komet.rules.annotated.RulesBase;
                import dev.ikm.komet.terms.KometConcept;
                import dev.ikm.tinkar.entity.EntityVersion;
                import org.evrete.dsl.annotation.MethodPredicate;
                import org.evrete.dsl.annotation.Rule;
                import org.evrete.dsl.annotation.Where;

                public class OfferActivation extends RulesBase {

                    @Rule("%s")
                    @Where(methods = {
                            @MethodPredicate(method = "isComponentFocused", args = {"$observation"}),
                            @MethodPredicate(method = "isComponentActive", args = {"$observation"})})
                    public void offerActivation(ObservationRecord $observation) {
                        if ($observation.subject() instanceof EntityVersion version) {
                            %s
                        }
                    }
                }
                """.formatted(RULE_NAME, statement);
    }

    private static int lineOf(String statement, String source) {
        return (int) source.substring(0, source.indexOf(statement)).lines().count();
    }
}
