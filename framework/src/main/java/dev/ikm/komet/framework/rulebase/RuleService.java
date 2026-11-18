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
package dev.ikm.komet.framework.rulebase;

import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;
import java.util.UUID;

/**
 * Komet's rules: fires them on what is observed, lists them, and takes rules written while
 * Komet runs.
 *
 * <p>The rules come from three places: Komet's own rule classes, the rule classes plugins bring
 * ({@link RuleProvider}), and rule sources ({@link RuleSource}) compiled as they are put in use.
 * A rule source that does not compile, that the rule engine will not take, or whose rule throws
 * when it fires is kept out of use with its findings; the other rules go on firing.
 *
 * <p>A rule source comes into use in one of two ways. A person's own source is put in use at
 * once ({@link #putRuleSource(RuleSource)}). A source someone else wrote for the person, the
 * Komet Assistant for one, is proposed ({@link #proposeRuleSource(RuleSource)}): it is compiled
 * and listed, and none of its code runs until the person accepts it
 * ({@link #acceptRuleSource(UUID)}).
 */
public interface RuleService {

    /**
     * The version of the rules API: what a rule source is compiled against, which is the class
     * its rules extend, the facts a rule's conditions read and the actions a rule offers. A rule
     * source that is kept is kept with the version it was written for. The version changes when
     * a rule source written for the version before may no longer compile.
     */
    String RULES_API_VERSION = "1";

    /**
     * Answers the rule service Komet runs on.
     *
     * @return the rule service
     */
    static RuleService get() {
        return RuleServiceFinder.INSTANCE.get();
    }

    /**
     * Fires the rules in use on the statements and answers what they concluded.
     *
     * @param knowledgeBaseName a name for the occasion; the rules fired do not depend on it
     * @param statements        what is observed or requested
     * @param viewProperties    the view the rules read the knowledge base through
     * @param editCoordinate    the author, module and path a rule's action would write under
     * @return what the rules concluded, in their order of precedence
     */
    ImmutableList<Consequence<?>> execute(String knowledgeBaseName,
                                          ImmutableList<Statement> statements,
                                          ViewProperties viewProperties,
                                          EditCoordinate editCoordinate);

    /**
     * Lists every rule set the service knows, in use or not: Komet's own, each plugin's, each
     * rule source a person accepted, and each proposed rule source, in that order.
     *
     * @return the rule sets as they stand now
     */
    ImmutableList<ListedRuleSet> ruleSets();

    /**
     * Compiles a rule source and says what putting it in use would give, without changing the
     * rules in use.
     *
     * @param source the rule source to check
     * @return the rule set as it would be listed: in use with its rules named, or out of use
     *         with its findings
     */
    ListedRuleSet checkRuleSource(RuleSource source);

    /**
     * Compiles a rule source and puts its rules in use at once, in place of the rule source with
     * the same identity. A source with findings is kept all the same, out of use, so that it is
     * listed with them; the rules of the source it replaced are no longer in use either.
     *
     * @param source the rule source to put in use
     * @return the rule set as it is now listed
     */
    ListedRuleSet putRuleSource(RuleSource source);

    /**
     * Removes a rule source, taking its rules out of use. A proposal with the same identity
     * stays, as the proposal of a new rule source.
     *
     * @param id the identity of the rule source
     * @return whether there was an accepted rule source with that identity
     */
    boolean removeRuleSource(UUID id);

    /**
     * Compiles a rule source and lists it as proposed, in place of the proposal with the same
     * identity. None of its code runs: a proposed rule source is out of use until a person
     * accepts it, and the accepted rule source with its identity, if there is one, stays in use
     * as it is until then.
     *
     * @param source the rule source to propose
     * @return the proposal as it is now listed: not accepted, with its rules named, or with the
     *         findings that would keep it out of use
     */
    ListedRuleSet proposeRuleSource(RuleSource source);

    /**
     * Accepts a proposed rule source for the person, putting its rules in use in place of the
     * accepted rule source with the same identity.
     *
     * @param id the identity of the proposed rule source
     * @return the rule set as it is now listed, or empty when no rule source with that identity
     *         is proposed
     */
    Optional<ListedRuleSet> acceptRuleSource(UUID id);

    /**
     * Discards a proposed rule source. The accepted rule source with its identity, if there is
     * one, stays as it is.
     *
     * @param id the identity of the proposed rule source
     * @return whether a rule source with that identity was proposed
     */
    boolean discardProposedRuleSource(UUID id);
}
