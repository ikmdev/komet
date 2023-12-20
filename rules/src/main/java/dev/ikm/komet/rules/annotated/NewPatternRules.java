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
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.actions.pattern.NewPatternAction;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import java.util.UUID;
import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;

@RuleSet(value = "New pattern rules")
public class NewPatternRules {
    @Rule(value = "New pattern rule")
    @Where(value = {"$request.topic() == Topic.NEW_PATTERN_REQUEST",
    "$request instanceof Request request && request.subject() instanceof String",
    })
    public void newPatternRule(Statement $request,
                               ConcurrentHashSet<Consequence<?>> $actionList,
                               ViewProperties $viewProperties,
                               EditCoordinate $editCoordinate,
                               RhsContext ctx) {
        if ($request instanceof Request request) {
            NewPatternAction generatedAction =
                    new NewPatternAction(request, $viewProperties.calculator(), $editCoordinate);
            $actionList.add(new ConsequenceAction(UUID.randomUUID(),
                    Thread.currentThread().getStackTrace()[1].toString(), generatedAction));
        }
    }
}
