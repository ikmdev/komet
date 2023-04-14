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
