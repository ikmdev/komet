package dev.ikm.komet.framework.performance.impl;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.performance.StatementStore;
import dev.ikm.komet.framework.performance.Topic;

import java.util.HashMap;

public class StatementHashStore implements StatementStore {
    HashMap<Topic, MutableList<Statement>> observationMap = new HashMap<>();

    public StatementHashStore(Statement... statements) {
        addStatements(statements);
    }

    private void addStatements(Statement... statements) {
        for (Statement statement : statements) {
            observationMap.computeIfAbsent(statement.topic(), topic -> Lists.mutable.empty()).add(statement);
        }
    }

    @Override
    public ImmutableList<Statement> statementsForTopic(Topic topic) {
        return observationMap.getOrDefault(topic, Lists.mutable.empty()).toImmutable();
    }

    @Override
    public void addStatement(Statement statement) {
        addStatements(statement);
    }
}
