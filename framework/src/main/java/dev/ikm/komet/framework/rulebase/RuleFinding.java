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

import java.util.Objects;

/**
 * What keeps a rule set out of use: a line the compiler refused, a rule the rule engine would not
 * take, or what a rule threw when it fired.
 *
 * @param line    the line of the rule source the finding is about, counted from 1, or 0 when it
 *                is about no one line
 * @param message what was found, in the compiler's or the rule engine's own words
 */
public record RuleFinding(int line, String message) {

    /**
     * Refuses a negative line and a finding that says nothing.
     *
     * @param line    the line of the rule source the finding is about, counted from 1, or 0 when
     *                it is about no one line
     * @param message what was found, in the compiler's or the rule engine's own words
     */
    public RuleFinding {
        if (line < 0) {
            throw new IllegalArgumentException("A finding's line is 1 or more, or 0 for no one line: " + line);
        }
        Objects.requireNonNull(message, "message");
    }
}
