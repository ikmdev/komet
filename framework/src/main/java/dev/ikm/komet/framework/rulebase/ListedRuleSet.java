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

import org.eclipse.collections.api.list.ImmutableList;

import java.util.Objects;
import java.util.Optional;

/**
 * A rule set as the {@link RuleService} lists it: where it came from, the rules in it, whether
 * a person has accepted it, whether its rules are in use, and what was found when they are not.
 *
 * <p>A rule set is in use when it is accepted and has no findings. Komet's own rule sets and a
 * plugin's are always accepted, and so is a rule source a person put in use. A proposed rule
 * source is not, until a person accepts it.
 *
 * @param origin    where the rule set came from
 * @param name      the rule set's name
 * @param ruleNames the names of its rules; empty when the rule set did not get as far as naming
 *                  them
 * @param accepted  whether a person has accepted it; false for a rule source that is proposed
 * @param inUse     whether its rules take part when the rules are fired
 * @param findings  what keeps it out of use, or would if it were accepted; empty when it is in
 *                  use
 * @param source    its source, when it was written while Komet runs
 */
public record ListedRuleSet(Origin origin,
                            String name,
                            ImmutableList<String> ruleNames,
                            boolean accepted,
                            boolean inUse,
                            ImmutableList<RuleFinding> findings,
                            Optional<RuleSource> source) {

    /** Where a rule set came from. */
    public enum Origin {
        /** Part of Komet. */
        KOMET,
        /** Brought by a plugin. */
        PLUGIN,
        /** Written while Komet runs. */
        SOURCE
    }

    /**
     * Refuses a listing with a part missing, and one that is in use despite a finding or
     * without having been accepted.
     *
     * @param origin    where the rule set came from
     * @param name      the rule set's name
     * @param ruleNames the names of its rules; empty when the rule set did not get as far as
     *                  naming them
     * @param accepted  whether a person has accepted it; false for a rule source that is
     *                  proposed
     * @param inUse     whether its rules take part when the rules are fired
     * @param findings  what keeps it out of use, or would if it were accepted; empty when it is
     *                  in use
     * @param source    its source, when it was written while Komet runs
     */
    public ListedRuleSet {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(ruleNames, "ruleNames");
        Objects.requireNonNull(findings, "findings");
        Objects.requireNonNull(source, "source");
        if (inUse && findings.notEmpty()) {
            throw new IllegalArgumentException("A rule set in use has no findings: " + name + " " + findings);
        }
        if (inUse && !accepted) {
            throw new IllegalArgumentException("A rule set in use has been accepted: " + name);
        }
    }
}
