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
package dev.ikm.komet.rules.actions.component;

import dev.ikm.komet.rules.actions.AbstractActionSuggested;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;
import javafx.event.ActionEvent;
import org.eclipse.collections.api.list.ImmutableList;

import static dev.ikm.tinkar.terms.TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN;

public class InactivateComponentAction extends AbstractActionSuggested {

    final EntityVersion entityVersion;

    public InactivateComponentAction(EntityVersion entityVersion, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super("Inactivate component", viewCalculator, editCoordinate);
        this.entityVersion = entityVersion;
        this.setLongText("An action that will inactivate a component, leaving component in an uncommitted state");
    }

    public final void doAction(ActionEvent actionEvent, EditCoordinateRecord editCoordinate) {
        appendInactiveVersion(entityVersion.nid(), editCoordinate.toEditCoordinateRecord());
    }

    private void appendInactiveVersion(int entityNid, EditCoordinateRecord editCoordinateRecord) {
        Entity entity = Entity.getFast(entityNid);

        Transaction transaction = Transaction.make();
        ViewCoordinateRecord viewRecord = viewCalculator.viewCoordinateRecord();

        Latest<PatternEntityVersion> latestPatternVersion = viewCalculator.latestPatternEntityVersion(TINKAR_BASE_MODEL_COMPONENT_PATTERN);
        latestPatternVersion.ifPresentOrElse(patternEntityVersion -> {
            // Create a new stamp with inactive state
            StampEntity
                    stampEntity = transaction.getStamp(State.INACTIVE, System.currentTimeMillis(), editCoordinateRecord.getAuthorNidForChanges(),
                    patternEntityVersion.moduleNid(), viewRecord.stampCoordinate().pathNidForFilter());

            // Create Entity version Record adding new version
            if (entity instanceof SemanticRecord semanticRecord) {
                // add fields from existing semantic version before retiring semantic version
                SemanticVersionRecord newSemanticVersion = new SemanticVersionRecord(semanticRecord, stampEntity.nid(), semanticRecord.versions().get(0).fieldValues());
                SemanticRecord analogue = semanticRecord.with(newSemanticVersion).build();
                transaction.addComponent(analogue);
                Entity.provider().putEntity(analogue);
            } else if (entity instanceof ConceptRecord conceptRecord) {
                ConceptVersionRecord newConceptVersion = new ConceptVersionRecord(conceptRecord, stampEntity.nid());
                ConceptRecord analogue = conceptRecord.with(newConceptVersion).build();
                transaction.addComponent(analogue);
                Entity.provider().putEntity(analogue);
            } else if (entity instanceof PatternRecord patternRecord) {
                PatternVersionRecord newPatternVersion = new PatternVersionRecord(patternRecord, stampEntity.nid(),
                        patternEntityVersion.semanticPurposeNid(), patternEntityVersion.semanticMeaningNid(),
                        patternRecord.versions().get(0).fieldDefinitions());
                PatternRecord analogue = patternRecord.with(newPatternVersion).build();
                transaction.addComponent(analogue);
                Entity.provider().putEntity(analogue);
            }
            CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
            TinkExecutor.threadPool().submit(commitTransactionTask);
        }, () -> {
            throw new IllegalStateException("No latest pattern version for: " + Entity.getFast(TINKAR_BASE_MODEL_COMPONENT_PATTERN));
        });


    }



}
