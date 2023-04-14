package dev.ikm.komet.framework.rulebase;

import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.performance.StatementStore;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public interface Rule {
    default UUID ruleUUID() {
        return UUID.nameUUIDFromBytes(this.getClass().getName().getBytes(StandardCharsets.UTF_8));
    }

    String name();

    String description();

    Topic topicToProcess();

    ImmutableList<Consequence<?>> execute(StatementStore observations, ViewCalculator viewCalculator, EditCoordinate editCoordinate);
}
