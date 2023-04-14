package dev.ikm.komet.framework.rulebase.rules;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.performance.StatementStore;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.Rule;
import dev.ikm.komet.framework.rulebase.actions.AbstractActionGenerated;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractComponentRule implements Rule {

    @Override
    public final ImmutableList<Consequence<?>> execute(StatementStore statements, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        MutableList<Consequence<?>> consequences = Lists.mutable.empty();

        for (Statement statement : statements.statementsForTopic(topicToProcess())) {
            if (conditionsMet(statement, viewCalculator)) {
                makeAction(statement, viewCalculator, editCoordinate).ifPresent(generatedAction -> {
                    generatedAction.setLongText(description());
                    generatedAction.setText(name());
                    consequences.add(new ConsequenceAction(UUID.randomUUID(),
                            ruleUUID(), generatedAction));
                });
            }
        }

        return consequences.toImmutable();
    }

    abstract boolean conditionsMet(Statement statement, ViewCalculator viewCalculator);

    abstract Optional<AbstractActionGenerated> makeAction(Statement statement, ViewCalculator viewCalculator, EditCoordinate editCoordinate);

}
