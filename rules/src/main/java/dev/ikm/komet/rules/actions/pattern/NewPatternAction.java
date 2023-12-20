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
package dev.ikm.komet.rules.actions.pattern;

import static dev.ikm.komet.framework.activity.ActivityStreams.BUILDER;

import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.builder.PatternBuilder;
import dev.ikm.komet.framework.performance.Request;
import dev.ikm.komet.framework.rulebase.GeneratedActionImmediate;
import dev.ikm.komet.rules.actions.AbstractActionImmediate;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.event.ActionEvent;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creating a new Pattern based on an Evrete rule's action
 */
public class NewPatternAction extends AbstractActionImmediate implements GeneratedActionImmediate {

    private static final Logger LOG = LoggerFactory.getLogger(NewPatternAction.class);

    final Request newPatternRequest;
    final String newPatternText;

    /**
     * construct a new pattern request
     * @param newPatternRequest request object from rules service
     * @param viewCalculator viewCalculator needed for a Pattern
     * @param editCoordinate editCoordinate needed for a Pattern
     */
    public NewPatternAction(Request newPatternRequest, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super("New pattern from text", viewCalculator, editCoordinate);
        this.newPatternRequest = newPatternRequest;
        if (newPatternRequest != null && newPatternRequest.subject() instanceof String newPatternText
            && newPatternRequest.subject() != "") {
            this.newPatternText = newPatternText;
        } else {
            throw new IllegalStateException("newConceptRequest.subject() is not instanceof String");
        }
    }

    /**
     * @param actionEvent the event, create a new pattern
     * @param editCoordinate the edit coordinates needed for the new pattern
     */
    @Override
    public void doAction(ActionEvent actionEvent, EditCoordinateRecord editCoordinate) {
        TinkExecutor.threadPool().execute(() -> {
            Transaction transaction = Transaction.make("New pattern for: " + newPatternText);
            StampEntity stampEntity = transaction.getStamp(State.ACTIVE, editCoordinate.getAuthorNidForChanges(),
                    editCoordinate.getDefaultModuleNid(), editCoordinate.getDefaultPathNid());
            Entity.provider().putStamp(stampEntity);

            PatternBuilder newPatternBuilder = PatternBuilder.builder(stampEntity);

            newPatternBuilder.makeRegularName(newPatternText);

            ImmutableList<EntityFacade> builtEntities = newPatternBuilder.build();
            LOG.info("Built: " + builtEntities);

            ActivityStreams.get(BUILDER).dispatch(builtEntities);
        });
    }
}
