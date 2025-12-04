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
