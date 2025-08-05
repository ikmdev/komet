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
package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.EditedConceptTracker;
import dev.ikm.tinkar.events.AxiomChangeEvent;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.komet.rules.actions.AbstractActionSuggested;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;
import javafx.event.ActionEvent;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static dev.ikm.tinkar.events.FrameworkTopics.RULES_TOPIC;
import static dev.ikm.tinkar.terms.TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN;

public abstract class AbstractAxiomAction extends AbstractActionSuggested {
	
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAxiomAction.class);

    private final AxiomSubjectRecord axiomSubjectRecord;

    public AbstractAxiomAction(String text, AxiomSubjectRecord axiomSubjectRecord,
                               ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, viewCalculator, editCoordinate);
        this.axiomSubjectRecord = axiomSubjectRecord;
    }

    @Override
    public final void doAction(ActionEvent t, EditCoordinateRecord editCoordinate) {
        doAction(t, axiomSubjectRecord, editCoordinate);
        EvtBusFactory.getDefaultEvtBus().publish(RULES_TOPIC, new AxiomChangeEvent(ChangeSetType.class, AxiomChangeEvent.ANY_CHANGE));
    }

    public abstract void doAction(ActionEvent t, AxiomSubjectRecord axiomSubjectRecord, EditCoordinateRecord editCoordinate);
    
    protected void putUpdatedLogicalExpression(EditCoordinateRecord editCoordinate, LogicalExpression logicalExpression) {
        switch (logicalExpression.sourceGraph()) {
            case DiTreeEntity diTreeEntity -> putUpdatedDiTree(this.axiomSubjectRecord, editCoordinate, diTreeEntity);
            case DiTreeEntity.Builder builder -> putUpdatedDiTree(this.axiomSubjectRecord, editCoordinate, builder.build());
            default -> throw new IllegalStateException("Unexpected value: " + logicalExpression.sourceGraph());
        }
    }
    protected void putUpdatedDiTree(AxiomSubjectRecord axiomSubjectRecord, EditCoordinateRecord editCoordinate, DiTreeEntity newTree) {
        SemanticRecord semanticContainingAxiom = Entity.getFast(axiomSubjectRecord.semanticContainingAxiom().nid());
        Optional<Transaction> optionalTransaction = Transaction.forVersion(axiomSubjectRecord.semanticContainingAxiom().version());
        Transaction transaction;
        if (optionalTransaction.isPresent()) {
            transaction = optionalTransaction.get();
        } else {
            transaction = Transaction.make();
            transaction.addComponent(axiomSubjectRecord.semanticContainingAxiom().nid());
        }

        Latest<PatternEntityVersion> latestAxiomPatternVersion = viewCalculator.latestPatternEntityVersion(axiomSubjectRecord.semanticContainingAxiom().patternNid());
        latestAxiomPatternVersion.ifPresentOrElse(patternEntityVersion -> {
            StampEntity stampEntity = transaction.getStamp(State.ACTIVE, Long.MAX_VALUE,
                    editCoordinate.getAuthorNidForChanges(),
                    axiomSubjectRecord.semanticContainingAxiom().moduleNid(),
                    axiomSubjectRecord.semanticContainingAxiom().pathNid());
            SemanticVersionRecord newSemanticVersion = new SemanticVersionRecord(semanticContainingAxiom, stampEntity.nid(), Lists.immutable.of(newTree));
            SemanticRecord analogue = semanticContainingAxiom.with(newSemanticVersion).build();
            Entity.provider().putEntity(analogue);
            // Incremental reasoner
        	LOG.info(">>>>>" + "putUpdatedDiTree" + newSemanticVersion);
            EditedConceptTracker.addEdit(newSemanticVersion);
            //TODO need to surface transactions in the journal, then turn off this "auto commit"...
            transaction.commit();
        }, () -> {
            throw new IllegalStateException("No latest pattern version for: " + Entity.getFast(TINKAR_BASE_MODEL_COMPONENT_PATTERN));
        });
    }
}
