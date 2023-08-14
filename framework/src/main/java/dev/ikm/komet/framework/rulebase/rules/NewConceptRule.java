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
package dev.ikm.komet.framework.rulebase.rules;

import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.RequestRecord;
import dev.ikm.komet.framework.rulebase.RuntimeRule;
import dev.ikm.komet.framework.rulebase.actions.AbstractActionGenerated;
import dev.ikm.komet.framework.rulebase.actions.NewConceptFromTextActionGenerated;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

import java.util.Optional;

@RuntimeRule
public class NewConceptRule extends AbstractComponentRule {
    @Override
    public String name() {
        return "New concept from text";
    }

    @Override
    public String description() {
        return "Create a new concept from provided text";
    }

    @Override
    public Topic topicToProcess() {
        return Topic.NEW_CONCEPT_REQUEST;
    }

    @Override
    boolean conditionsMet(Statement statement, ViewCalculator viewCalculator) {
        return statement.topic().equals(Topic.NEW_CONCEPT_REQUEST) && statement.subject() instanceof String;
    }

    @Override
    Optional<AbstractActionGenerated> makeAction(Statement statement, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        if (statement.topic().equals(Topic.NEW_CONCEPT_REQUEST) &&
                statement instanceof RequestRecord request &&
                request.subject() instanceof String newConceptText) {
            return Optional.of(new NewConceptFromTextActionGenerated(request, viewCalculator, editCoordinate));
        }
        return Optional.empty();
    }
}
