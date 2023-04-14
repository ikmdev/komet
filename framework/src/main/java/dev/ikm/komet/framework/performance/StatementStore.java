package dev.ikm.komet.framework.performance;

import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.performance.impl.StatementHashStore;

public interface StatementStore {
    static StatementStore make(Statement... statements) {
        return new StatementHashStore(statements);
    }

    ImmutableList<Statement> statementsForTopic(Topic topic);

    void addStatement(Statement statement);
}
