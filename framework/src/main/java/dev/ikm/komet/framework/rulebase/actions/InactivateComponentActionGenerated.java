package dev.ikm.komet.framework.rulebase.actions;

import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;
import javafx.event.ActionEvent;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

import static dev.ikm.tinkar.terms.TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN;

public class InactivateComponentActionGenerated extends AbstractActionSuggested {

    final EntityVersion entityVersion;

    public InactivateComponentActionGenerated(EntityVersion entityVersion, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super("Inactivate", viewCalculator, editCoordinate);
        this.entityVersion = entityVersion;
    }

    public final void doAction(ActionEvent actionEvent, EditCoordinateRecord editCoordinate) {
        appendNewVersion(entityVersion.nid(), editCoordinate.toEditCoordinateRecord());
    }

    private void appendNewVersion(int entityNid, EditCoordinateRecord editCoordinateRecord) {
        Entity entity = Entity.getFast(entityNid);

        Transaction transaction = Transaction.make();
        ViewCoordinateRecord viewRecord = viewCalculator.viewCoordinateRecord();

        Latest<PatternEntityVersion> latestPatternVersion = viewCalculator.latestPatternEntityVersion(TINKAR_BASE_MODEL_COMPONENT_PATTERN);
        latestPatternVersion.ifPresentOrElse(patternEntityVersion -> {
            // Create a new stamp with inactive state
            StampEntity stampEntity = transaction.getStamp(State.INACTIVE, System.currentTimeMillis(), editCoordinateRecord.getAuthorNidForChanges(),
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
            }
            CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
            TinkExecutor.threadPool().submit(commitTransactionTask);
        }, () -> {
            throw new IllegalStateException("No latest pattern version for: " + Entity.getFast(TINKAR_BASE_MODEL_COMPONENT_PATTERN));
        });


    }

}
