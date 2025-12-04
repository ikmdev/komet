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
package dev.ikm.tinkar.ext.lang.owl;


import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;

public class OwlToLogicAxiomTransformerAndWriter extends TrackingCallable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(OwlToLogicAxiomTransformerAndWriter.class);

    private final int destinationPatternNid;
    private final List<TransformationGroup> transformationRecords;
    private Transaction transaction;
    private int authorNid = TinkarTerm.USER.nid();
    private int moduleNid = Integer.MAX_VALUE;
    private int pathNid = Integer.MAX_VALUE;

    /**
	 * @param transaction           - if supplied, this does NOT commit the
	 *                              transaction. If not supplied, this creates (and
	 *                              commits) its own transaction.
     * @param transformationRecords
     */
    public OwlToLogicAxiomTransformerAndWriter(Transaction transaction, List<TransformationGroup> transformationRecords,
                                               int destinationPatternNid) {

        this.transaction = transaction;
        this.transformationRecords = transformationRecords;
        this.destinationPatternNid = destinationPatternNid;
        updateTitle("EL++ OWL transformation");
        updateMessage("");
        addToTotalWork(transformationRecords.size());
    }

    public OwlToLogicAxiomTransformerAndWriter(Transaction transaction, List<TransformationGroup> transformationRecords,
                                               int destinationPatternNid, int authorNid, int moduleNid, int pathNid) {
        this(transaction, transformationRecords, destinationPatternNid);
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
    }

    @Override
    public Void compute() throws Exception {
        boolean commitTransaction = this.transaction == null;
        if (commitTransaction) {
            this.transaction = Transaction.make("OwlTransformerAndWriter");
        }
        int count = 0;

        LOG.debug("starting batch transform of {} records", transformationRecords.size());
        for (TransformationGroup transformationGroup : transformationRecords) {
            try {
                transformOwlExpressions(transformationGroup.conceptNid, transformationGroup.semanticNids, transformationGroup.getPremiseType());
            } catch (Exception e) {
                LOG.error("Error in Owl Transform: ", e);
            }
            if (count % 1000 == 0) {
                updateMessage("Processing concept: " + PrimitiveData.text(transformationGroup.conceptNid));
                LOG.trace("Processing concept: {}", PrimitiveData.text(transformationGroup.conceptNid));
            }
            count++;
            completedUnitOfWork();
        }
        if (commitTransaction) {
            transaction.commit();
        }
        LOG.debug("Finished processing batch of: {}", count);
        return null;
    }

    /**
     * Transform relationships.
     *
     * @param premiseType the stated
     */
    private void transformOwlExpressions(int conceptNid, int[] owlNids, PremiseType premiseType) throws Exception {
        updateMessage("Converting " + premiseType + " Owl expressions");

        List<SemanticEntity> owlEntitiesForConcept = new ArrayList<>();
        Set<StampCoordinateRecord> stampCoordinates = new HashSet<>();

        for (int owlNid : owlNids) {
            EntityService.get().getEntity(owlNid).ifPresent(owlSemantic -> {
                owlEntitiesForConcept.add((SemanticEntity) owlSemantic);
            });
        }

        for (SemanticEntity<? extends SemanticEntityVersion> owlChronology : owlEntitiesForConcept) {
            for (int stampNid : owlChronology.stampNids().toArray()) {
                StampEntity<? extends StampEntityVersion> stamp = EntityService.get().getStampFast(stampNid);
                StampPositionRecord stampPos = StampPositionRecord.make(stamp.time(), stamp.pathNid());
                int[] moduleWithOrigins = StampCalculatorWithCache.getModuleWithOrigins(stampPos, stamp.moduleNid());
                StampCoordinateRecord stampCoordinate = StampCoordinateRecord.make(StateSet.ACTIVE, stampPos)
                        .withModuleNids(IntIds.set.of(moduleWithOrigins))
                        .withModulePriorityNidList(IntIds.list.of(moduleWithOrigins));
                int writeModuleNid = (moduleNid != Integer.MAX_VALUE ? moduleNid : stamp.moduleNid());
                int writePathNid = (pathNid != Integer.MAX_VALUE ? pathNid : stamp.pathNid());
                StampVersionRecordBuilder writeStampBuilder = StampVersionRecordBuilder.builder()
                        .stateNid(State.ACTIVE.nid())
                        .time(stamp.time())
                        .authorNid(authorNid)
                        .moduleNid(writeModuleNid)
                        .pathNid(writePathNid);

                if (stampCoordinates.contains(stampCoordinate)) {
                    // Continue if the logical definition at this stamp has already been written
                    // Possible when a module has more than one owl axiom and both have been updated in the same release
                    continue;
                }
                stampCoordinates.add(stampCoordinate);

                LogicalExpression logicalExpression = generateLogicalExpression(conceptNid, owlEntitiesForConcept, stampCoordinate);
                if (logicalExpression == null) {
                    // When the logical expression is null, write a version with the STAMP's original (likely Inactive) status
                    stampCoordinate = StampCoordinateRecord.make(StateSet.of(stamp.state()), stampPos)
                            .withModuleNids(IntIds.set.of(moduleWithOrigins))
                            .withModulePriorityNidList(IntIds.list.of(moduleWithOrigins));
                    writeStampBuilder.stateNid(stamp.stateNid());
                    logicalExpression = generateLogicalExpression(conceptNid, owlEntitiesForConcept, stampCoordinate);
                }

                if (logicalExpression != null) {
                    if (logicalExpression.nodesOfType(LogicalAxiom.LogicalSet.NecessarySet.class).size() > 1) {
                        // Need to merge necessary sets.
                        LOG.warn("\n\n{} has expression with multiple necessary sets: {}\n\n",
                                PrimitiveData.text(conceptNid), logicalExpression);
                    }

                    // See if a semantic already exists in this pattern referencing this concept...
                    int[] destinationSemanticNids = EntityService.get().semanticNidsForComponentOfPattern(conceptNid, destinationPatternNid);
                    switch (destinationSemanticNids.length) {
                        case 0 -> newSemanticWithVersion(conceptNid, logicalExpression, writeStampBuilder.build());
                        case 1 -> addSemanticVersionIfAbsent(conceptNid, logicalExpression, writeStampBuilder.build(), stampCoordinate, destinationSemanticNids[0]);
                        default -> throw new IllegalStateException("To many graphs for component: " + PrimitiveData.text(conceptNid));
                    }
                }
            }
        }
    }

    private LogicalExpression generateLogicalExpression(int ConceptNid, List<SemanticEntity> owlEntities, StampCoordinateRecord stampCoordinate) {
        List<String> owlExpressionsToProcess = new ArrayList<>();
        StampCalculator stampCalc = stampCoordinate.stampCalculator();
        int owlSyntaxIdx = stampCalc.getIndexForMeaning(TinkarTerm.OWL_AXIOM_SYNTAX_PATTERN.nid(), TinkarTerm.AXIOM_SYNTAX.nid()).orElse(0);
        for (SemanticEntity<SemanticEntityVersion> owlEntity : owlEntities) {
            stampCalc.latest(owlEntity).ifPresent(latestVersion -> {
                owlExpressionsToProcess.add((String) latestVersion.fieldValues().get(owlSyntaxIdx));
            });
        }
        if (owlExpressionsToProcess.isEmpty()) {
            return null;
        }

        LogicalExpression logicalExpression = null;
        OwlElExpressionToLogicalExpression transformer = new OwlElExpressionToLogicalExpression(
                owlExpressionsToProcess, ConceptNid);
        try {
            logicalExpression = transformer.build();
        } catch (Exception ex) {
            LOG.error("Error: ", ex);
        }
        return logicalExpression;
    }

    private void newSemanticWithVersion(int conceptNid, LogicalExpression logicalExpression, StampVersionRecord writeStamp) {
        // Create UUID from seed and assign SemanticBuilder the value
        Entity<EntityVersion> patternEntity = EntityService.get().getEntityFast(destinationPatternNid);
        UUID generartedSemanticUuid = UuidT5Generator.singleSemanticUuid(patternEntity,
                EntityService.get().getEntityFast(conceptNid));
        int semanticNid = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, patternEntity.publicId())
                .call(() -> PrimitiveData.nid(generartedSemanticUuid));

        SemanticRecordBuilder newSemanticBuilder = SemanticRecordBuilder.builder()
                .nid(semanticNid)
                .mostSignificantBits(generartedSemanticUuid.getMostSignificantBits())
                .leastSignificantBits(generartedSemanticUuid.getLeastSignificantBits())
                .patternNid(destinationPatternNid)
                .referencedComponentNid(conceptNid)
                .versions(Lists.immutable.empty());

        addNewVersion(logicalExpression, newSemanticBuilder.build(), writeStamp);
    }

    private void addSemanticVersionIfAbsent(int conceptNid, LogicalExpression logicalExpression, StampVersionRecord writeStamp,
                                            StampCoordinateRecord stampCoordinate, int semanticNid) {
        SemanticRecord existingSemantic = EntityService.get().getEntityFast(semanticNid);
        Latest<SemanticEntityVersion> latestSemanticVersion = stampCoordinate.stampCalculator().latest(semanticNid);
        if (latestSemanticVersion.isPresent()) {
            StampEntityVersion existingStampVer = latestSemanticVersion.get().stamp().lastVersion();
            // Equal checks ordered for performance - time and module most likely to be unequal
            if (existingStampVer.time() == writeStamp.time() &&
                existingStampVer.moduleNid() == writeStamp.moduleNid() &&
                existingStampVer.stateNid() == writeStamp.stateNid() &&
                existingStampVer.authorNid() == writeStamp.authorNid() &&
                existingStampVer.pathNid() == writeStamp.pathNid()) {
                    DiTreeEntity latestExpression = (DiTreeEntity) latestSemanticVersion.get().fieldValues().get(0);
					/*
					 * if (LOG.isWarnEnabled()) { LOG.
					 * warn("Skipping write of new version: Logical Definition Semantic Version with this STAMP already exists for Concept: {}\nExisting STAMP: {}\nExisting: {}\nNew STAMP: {}\nNew: {}"
					 * , EntityService.get().getEntityFast(conceptNid).publicId().idString(),
					 * existingStampVer.describe(), stampCoordinate, latestExpression,
					 * logicalExpression); }
					 */
            } else {
                addNewVersion(logicalExpression, SemanticRecordBuilder.builder(existingSemantic).build(), writeStamp);
            }
        } else {
            // Add a new version if no version exists at or before the current STAMP - this case already guarantees there is no conflicting stamp / version
            // Possible when a Semantic Version with a later timestamp has already been written
            addNewVersion(logicalExpression, SemanticRecordBuilder.builder(existingSemantic).build(), writeStamp);
        }
    }

    private void addNewVersion(LogicalExpression logicalExpression, SemanticRecord semanticRecord, StampVersionRecord writeStamp) {
        State status = State.fromConceptNid(writeStamp.stateNid());
        StampEntity transactionStamp = transaction.getStamp(status, writeStamp.time(), writeStamp.authorNid(), writeStamp.moduleNid(), writeStamp.pathNid());

        SemanticVersionRecordBuilder semanticVersionBuilder = SemanticVersionRecordBuilder.builder()
                .fieldValues(Lists.immutable.of(logicalExpression.sourceGraph()))
                .stampNid(transactionStamp.nid())
                .chronology(semanticRecord);

        SemanticRecord newSemanticRecord = semanticRecord.analogueBuilder().with(semanticVersionBuilder.build()).build();
        EntityService.get().putEntity(newSemanticRecord);
    }
}
