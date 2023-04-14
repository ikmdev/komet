package dev.ikm.komet.framework.rulebase.actions;

import javafx.event.ActionEvent;
import org.eclipse.collections.api.factory.Lists;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;

import static dev.ikm.tinkar.terms.TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN;

public class AddToTinkarBaseModelActionGenerated extends AbstractActionSuggested {
    final ConceptEntityVersion conceptVersion;

    public AddToTinkarBaseModelActionGenerated(ConceptEntityVersion conceptVersion, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super("Add to Tinkar base model", viewCalculator, editCoordinate);
        this.conceptVersion = conceptVersion;
    }

    @Override
    public void doAction(ActionEvent actionEvent, EditCoordinateRecord editCoordinate) {
        // See if semantic already exists, and needs a new version...
        int[] semanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptVersion.nid(), TINKAR_BASE_MODEL_COMPONENT_PATTERN.nid());
        if (semanticNidsForComponent.length == 0) {
            // case 1: never a member
            createSemantic(editCoordinate.toEditCoordinateRecord());
        } else {
            // a member, need to change to inactive.
            updateSemantic(semanticNidsForComponent[0], editCoordinate.toEditCoordinateRecord());
        }
    }

    private SemanticRecord createSemantic(EditCoordinateRecord editCoordinateRecord) {
        PublicId newSemanticId = PublicIds.singleSemanticId(TINKAR_BASE_MODEL_COMPONENT_PATTERN, conceptVersion.publicId());
        RecordListBuilder versionListBuilder = new RecordListBuilder();
        SemanticRecord newSemantic = SemanticRecord.makeNew(newSemanticId, TINKAR_BASE_MODEL_COMPONENT_PATTERN, conceptVersion.nid(), versionListBuilder);
        Transaction transaction = Transaction.make();
        ViewCoordinateRecord viewRecord = viewCalculator.viewCoordinateRecord();

        Latest<PatternEntityVersion> latestPatternVersion = viewCalculator.latestPatternEntityVersion(TINKAR_BASE_MODEL_COMPONENT_PATTERN);
        latestPatternVersion.ifPresentOrElse(patternEntityVersion -> {
            StampEntity stampEntity = transaction.getStamp(State.ACTIVE, Long.MAX_VALUE, editCoordinateRecord.getAuthorNidForChanges(),
                    patternEntityVersion.moduleNid(), viewRecord.stampCoordinate().pathNidForFilter());
            SemanticVersionRecord newSemanticVersion = new SemanticVersionRecord(newSemantic, stampEntity.nid(), Lists.immutable.empty());

            versionListBuilder.add(newSemanticVersion);
            versionListBuilder.build();
            transaction.addComponent(newSemantic);
            Entity.provider().putEntity(newSemantic);
        }, () -> {
            throw new IllegalStateException("No latest pattern version for: " + Entity.getFast(TINKAR_BASE_MODEL_COMPONENT_PATTERN));
        });
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);
        return newSemantic;
    }

    private void updateSemantic(int semanticNid, EditCoordinateRecord editCoordinateRecord) {
        SemanticRecord semanticEntity = Entity.getFast(semanticNid);
        Transaction transaction = Transaction.make();
        ViewCoordinateRecord viewRecord = viewCalculator.viewCoordinateRecord();

        Latest<PatternEntityVersion> latestPatternVersion = viewCalculator.latestPatternEntityVersion(TINKAR_BASE_MODEL_COMPONENT_PATTERN);
        latestPatternVersion.ifPresentOrElse(patternEntityVersion -> {
            StampEntity stampEntity = transaction.getStamp(State.ACTIVE, Long.MAX_VALUE, editCoordinateRecord.getAuthorNidForChanges(),
                    patternEntityVersion.moduleNid(), viewRecord.stampCoordinate().pathNidForFilter());
            SemanticVersionRecord newSemanticVersion = new SemanticVersionRecord(semanticEntity, stampEntity.nid(), Lists.immutable.empty());
            SemanticRecord analogue = semanticEntity.with(newSemanticVersion).build();
            transaction.addComponent(analogue);
            Entity.provider().putEntity(analogue);
        }, () -> {
            throw new IllegalStateException("No latest pattern version for: " + Entity.getFast(TINKAR_BASE_MODEL_COMPONENT_PATTERN));
        });
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);
    }
}
