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

import dev.ikm.komet.framework.performance.Observation;
import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.rulebase.RuntimeRule;
import dev.ikm.komet.framework.rulebase.actions.AbstractActionGenerated;
import dev.ikm.komet.framework.rulebase.actions.ActivateComponentActionGenerated;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.Optional;

@RuntimeRule
public class ActivateComponentRule extends AbstractComponentRule {

    @Override
    boolean conditionsMet(Statement statement, ViewCalculator viewCalculator) {
        if (statement.subject() instanceof EntityVersion entityVersion) {
            if (!entityVersion.active()) {
                return true;
            }
        }
        return false;
    }

    @Override
    Optional<AbstractActionGenerated> makeAction(Statement statement, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        if (statement instanceof Observation observation && observation.isPresent() && statement.subject() instanceof EntityVersion entityVersion) {
            return Optional.of(new ActivateComponentActionGenerated(entityVersion, viewCalculator, editCoordinate));
        }
        return Optional.empty();
    }

    @Override
    public String name() {
        return "Activate component";
    }

    @Override
    public String description() {
        return "An action that will activate a retired component, leaving component in an uncommitted state";
    }

    @Override
    public Topic topicToProcess() {
        return Topic.COMPONENT_FOCUSED;
    }
}
