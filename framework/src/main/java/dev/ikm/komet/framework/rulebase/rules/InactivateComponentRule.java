package dev.ikm.komet.framework.rulebase.rules;

import dev.ikm.komet.framework.performance.Observation;
import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.rulebase.RuntimeRule;
import dev.ikm.komet.framework.rulebase.actions.AbstractActionGenerated;
import dev.ikm.komet.framework.rulebase.actions.InactivateComponentActionGenerated;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.Optional;

@RuntimeRule
public class InactivateComponentRule extends AbstractComponentRule {

    @Override
    public String name() {
        return "Retire component";
    }

    @Override
    public String description() {
        return "An action that will retire an active component, leaving component in an uncommitted state";
    }

    @Override
    public Topic topicToProcess() {
        return Topic.COMPONENT_FOCUSED;
    }

    @Override
    boolean conditionsMet(Statement statement, ViewCalculator viewCalculator) {
        if (statement instanceof Observation observation && observation.isPresent() && statement.subject() instanceof EntityVersion entityVersion) {
            if (entityVersion.active()) {
                return true;
            }
        }
        return false;
    }

    @Override
    Optional<AbstractActionGenerated> makeAction(Statement statement, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        if (statement.subject() instanceof EntityVersion entityVersion) {
            return Optional.of(new InactivateComponentActionGenerated(entityVersion, viewCalculator, editCoordinate));
        }
        return Optional.empty();
    }
}
