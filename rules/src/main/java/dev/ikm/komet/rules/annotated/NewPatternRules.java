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
package dev.ikm.komet.rules.annotated;

import dev.ikm.komet.framework.performance.Request;
import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.rules.actions.pattern.NewPatternAction;
import org.evrete.dsl.annotation.*;

/**
 * Rules relater to pattern-related statements
 */
@RuleSet("New pattern rules")
public class NewPatternRules extends RulesBase {

    /**
     * @see RulesBase#isNewPatternRequest(Statement)
     * @see RulesBase#requestWithStringSubject(Statement)
     */
    @Rule("New pattern rule")
    @Where(methods = {
            @MethodPredicate(method = "isNewPatternRequest", args = {"$request"}),
            @MethodPredicate(method = "requestWithStringSubject", args = {"$request"})
    })
    public void newPatternRule(Statement $request) {
        if ($request instanceof Request request) {
            NewPatternAction generatedAction =
                    new NewPatternAction(request, calculator(), editCoordinate());
            addGeneratedActions(generatedAction);
        }
    }
}