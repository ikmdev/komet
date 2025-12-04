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
package dev.ikm.komet.rules.actions.concept;

import static dev.ikm.komet.framework.activity.ActivityStreams.BUILDER;

import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.builder.AxiomBuilderRecord;
import dev.ikm.komet.framework.builder.ConceptEntityBuilder;
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
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewConceptFromTextAction extends AbstractActionImmediate implements GeneratedActionImmediate {
    private static final Logger LOG = LoggerFactory.getLogger(NewConceptFromTextAction.class);
    final Request newConceptRequest;
    final String newConceptText;

    public NewConceptFromTextAction(Request newConceptRequest, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super("New concept from text", viewCalculator, editCoordinate);
        this.newConceptRequest = newConceptRequest;
        if (newConceptRequest != null && newConceptRequest.subject() instanceof String newConceptText
            && newConceptRequest.subject() != "") {
            this.newConceptText = newConceptText;
        } else {
            throw new IllegalStateException("newConceptRequest.subject() is not instanceof String");
        }
    }

    public final void doAction(ActionEvent actionEvent, EditCoordinateRecord editCoordinate) {
        TinkExecutor.threadPool().execute(() -> {
            Transaction transaction = Transaction.make("New concept for: " + newConceptText);
            StampEntity stampEntity = transaction.getStamp(State.ACTIVE, editCoordinate.getAuthorNidForChanges(),
                    editCoordinate.getDefaultModuleNid(), editCoordinate.getDefaultPathNid());
            Entity.provider().putStamp(stampEntity);

            ConceptEntityBuilder newConceptBuilder = ConceptEntityBuilder.builder(stampEntity);
            // TODO automate UK dialect at some point.
            newConceptBuilder.makeRegularName(newConceptText);

            AxiomBuilderRecord ab = newConceptBuilder.axiomBuilder();
            ab.withNecessarySet(
//                    ab.makeConceptReference(TinkarTerm.LANGUAGE),
//                    ab.makeConceptReference(TinkarTerm.DESCRIPTION_ASSEMBLAGE),
                    ab.makeRoleGroup(
                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.ANONYMOUS_CONCEPT)
//                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.UNMODELED_ROLE_CONCEPT),
//                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.LANGUAGE)
                    )
            );


            ab.withSufficientSet(
//                    ab.makeConceptReference(TinkarTerm.LANGUAGE),
//                    ab.makeConceptReference(TinkarTerm.DESCRIPTION_ASSEMBLAGE),
//                    ab.makeSome(TinkarTerm.ROLE_TYPE, TinkarTerm.GB_ENGLISH_DIALECT),
                    ab.makeRoleGroup(
                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.ANONYMOUS_CONCEPT)
//                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.DYNAMIC_REFERENCED_COMPONENT_RESTRICTION),
//                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.INTRINSIC_ROLE)
                    ));

            // Build turns into EntityRecords, which get written, added to transaction, but not committed.
            // Have build return a list of entity records, with the top component returned first.

            ImmutableList<EntityFacade> builtEntities = newConceptBuilder.build();
            LOG.info("Built: " + builtEntities);
            // focus the panel on the new concept somehow... ?
            // Associate the transaction with the action event somehow... ?
            /*
            CommitTask commitTask = new CommitTask(transaction);
            Future<Void> future = Executor.threadPool().submit(commitTask);
            try {
                future.get();
            } catch (Exception e) {
                AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            }
            */
            ActivityStreams.get(BUILDER).dispatch(builtEntities);
            //AlertStreams.getRoot().dispatch(AlertObject.makeError(new UnsupportedOperationException("NewConceptFromTextActionGenerated is not completely validated yet. ")));
        });

    }

}
