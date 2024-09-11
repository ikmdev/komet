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
package dev.ikm.komet.kview.mvvm.model;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.ikm.tinkar.terms.TinkarTerm.*;

/**
 * utitity class for accessing and modifying common data operations
 */
public class DataModelHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DataModelHelper.class);

    /**
     * data types for field definitions
     * @return field definitions
     */
    public static Set<ConceptEntity> fetchFieldDefinitionDataTypes() {

        return Set.of(
                Entity.getFast(STRING.nid()),
                Entity.getFast(COMPONENT_FIELD.nid()),
                Entity.getFast(COMPONENT_ID_SET_FIELD.nid()),
                Entity.getFast(COMPONENT_ID_LIST_FIELD.nid()),
                Entity.getFast(DITREE_FIELD.nid()),
                Entity.getFast(DIGRAPH_FIELD.nid()),
                Entity.getFast(CONCEPT_FIELD.nid()),
                Entity.getFast(SEMANTIC_FIELD_TYPE.nid()),
                Entity.getFast(INTEGER_FIELD.nid()),
                Entity.getFast(FLOAT_FIELD.nid()),
                Entity.getFast(BOOLEAN_FIELD.nid()),
                Entity.getFast(BYTE_ARRAY_FIELD.nid()),
                Entity.getFast(ARRAY_FIELD.nid()),
                Entity.getFast(INSTANT_LITERAL.nid()),
                Entity.getFast(LONG.nid()),
                Entity.getFast(VERTEX_FIELD.nid()),
//                Entity.getFast(PLANAR_POINT.nid()),
//                Entity.getFast(SPATIAL_POINT.nid()),
                Entity.getFast(UUID_DATA_TYPE.nid())
        );
    }

    /**
     * return description types
     * @return description types
     */
    public static Set<ConceptEntity> fetchDescriptionTypes(){
        return Set.of(
                Entity.getFast(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid()),
                Entity.getFast(REGULAR_NAME_DESCRIPTION_TYPE.nid())
        );
    }

    /**
     * return distinct collection of the descendants of a concept
     * @param viewProperties viewProperties
     * @param publicId public id for a concept
     * @return distinct collection of the descendants of a concept
     */
    public static Set<ConceptEntity> fetchDescendentsOfConcept(ViewProperties viewProperties, PublicId publicId) {
        IntIdSet decendents = viewProperties.calculator().descendentsOf(EntityService.get().nidForPublicId(publicId));
        Set<ConceptEntity> allDecendents = decendents.intStream()
                .mapToObj(decendentNid -> (ConceptEntity) Entity.getFast(decendentNid))
                .collect(Collectors.toSet());
        return allDecendents;
    }

    /**
     * return the available membership patterns
     * @return collection of membership patterns
     */
    public static List<PatternEntityVersion> getMembershipPatterns() {
        List<PatternEntityVersion> membershipPatternList = new ArrayList<>();
        PrimitiveData.get().forEachPatternNid((patternNid) -> {
            PatternEntityVersion patternEntityVersion = (PatternEntityVersion)
                    Calculators.View.Default().stampCalculator().latest(patternNid).get();
            if (patternEntityVersion.semanticPurposeNid() == TinkarTerm.MEMBERSHIP_SEMANTIC.nid()) {
                membershipPatternList.add(patternEntityVersion);
            }
        });
        return membershipPatternList;
    }

    /**
     * a concept has many semantics, one can be a membership semantic
     * in that semantic, it will have a chronology with at least one version
     * that version can be active
     * to the user it is called remove, but in the database we are really appending a version
     * if we activate/inactivate we are appending versions
     * @param conceptNid nid from the conceptFacade
     * @param patternNid nid from the patternFacade
     * @param viewCalculator viewCaclculator for querying
     * @return true if in the membership pattern, false otherwise
     */
    public static boolean isInMembershipPattern(int conceptNid, int patternNid, ViewCalculator viewCalculator) {
        int[] semanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid, patternNid);
        return semanticNidsForComponent.length > 0 && viewCalculator.stampCalculator().isLatestActive(semanticNidsForComponent[0]);
    }

    /**
     * operation to add a concept into a membership pattern
     * @param concept entityFacade for a concept that we are adding
     * @param pattern the membership pattern that we are adding the concept to
     * @param viewCalculator viewCaclculator for querying
     */
    public static void addToMembershipPattern(EntityFacade concept, EntityFacade pattern, ViewCalculator viewCalculator) {
        EditCoordinate editCoordinate = viewCalculator.viewCoordinateRecord().editCoordinate();
        int[] semanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(concept.nid(), pattern.nid());
        if (semanticNidsForComponent.length == 0) {
            // case 1: never a member
            createSemantic(concept, pattern, editCoordinate.toEditCoordinateRecord(), viewCalculator);
        } else {
            // a member, need to change to inactive.
            updateSemantic(pattern, semanticNidsForComponent[0], editCoordinate.toEditCoordinateRecord(), viewCalculator, true);
        }
    }

    /**
     * operation to remove a concept into a membership pattern
     * @param conceptNid nid from the conceptFacade
     * @param pattern the membership pattern that we are adding the concept from
     * @param viewCalculator viewCaclculator for querying
     */
    public static void removeFromMembershipPattern(int conceptNid, EntityFacade pattern, ViewCalculator viewCalculator) {
        EditCoordinate editCoordinate = viewCalculator.viewCoordinateRecord().editCoordinate();
        int[] semanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid, pattern.nid());
        if (semanticNidsForComponent.length == 0) {
            // case 1: never a member
            throw new IllegalStateException("Asking to retire element that was never a member...");
        } else {
            updateSemantic(pattern, semanticNidsForComponent[0], editCoordinate.toEditCoordinateRecord(), viewCalculator, false);
        }
    }

    private static SemanticRecord createSemantic(EntityFacade concept, EntityFacade pattern, EditCoordinateRecord editCoordinateRecord, ViewCalculator viewCalculator) {
        PublicId newSemanticId = PublicIds.singleSemanticId(pattern.publicId(), concept.publicId());
        RecordListBuilder versionListBuilder = RecordListBuilder.make();
        SemanticRecord newSemantic = SemanticRecord.makeNew(newSemanticId, pattern.toProxy(), concept.nid(), versionListBuilder);
        Transaction transaction = Transaction.make();
        ViewCoordinateRecord viewRecord = viewCalculator.viewCoordinateRecord();

        Latest<PatternEntityVersion> latestPatternVersion = viewCalculator.latestPatternEntityVersion(pattern.toProxy());
        latestPatternVersion.ifPresentOrElse(patternEntityVersion -> {
            StampEntity stampEntity = transaction.getStamp(State.ACTIVE, Long.MAX_VALUE, editCoordinateRecord.getAuthorNidForChanges(),
                    patternEntityVersion.moduleNid(), viewRecord.stampCoordinate().pathNidForFilter());
            SemanticVersionRecord newSemanticVersion = new SemanticVersionRecord(newSemantic, stampEntity.nid(), Lists.immutable.empty());

            versionListBuilder.add(newSemanticVersion).build();
            transaction.addComponent(newSemantic);
            Entity.provider().putEntity(newSemantic);
        }, () -> {
            throw new IllegalStateException("No latest pattern version for: " + Entity.getFast(pattern));
        });
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);
        return newSemantic;
    }

    private static void updateSemantic(EntityFacade pattern, int semanticNid, EditCoordinateRecord editCoordinateRecord, ViewCalculator viewCalculator, boolean active) {
        SemanticRecord semanticEntity = Entity.getFast(semanticNid);
        Transaction transaction = Transaction.make();
        ViewCoordinateRecord viewRecord = viewCalculator.viewCoordinateRecord();

        Latest<PatternEntityVersion> latestPatternVersion = viewCalculator.latestPatternEntityVersion(pattern.toProxy());
        latestPatternVersion.ifPresentOrElse(patternEntityVersion -> {
            State state = active ? State.ACTIVE : State.INACTIVE;
            StampEntity stampEntity = transaction.getStamp(state, Long.MAX_VALUE, editCoordinateRecord.getAuthorNidForChanges(),
                    patternEntityVersion.moduleNid(), viewRecord.stampCoordinate().pathNidForFilter());
            SemanticVersionRecord newSemanticVersion = new SemanticVersionRecord(semanticEntity, stampEntity.nid(), Lists.immutable.empty());
            SemanticRecord analogue = semanticEntity.with(newSemanticVersion).build();
            transaction.addComponent(analogue);
            Entity.provider().putEntity(analogue);
        }, () -> {
            throw new IllegalStateException("No latest pattern version for: " + Entity.getFast(pattern));
        });
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);
    }

    public static List<String> getIdsToAppend(ViewCalculator viewCalc, EntityProxy componentInDetailsViewer) {
        Latest<PatternEntityVersion> latestIdPattern = viewCalc.latestPatternEntityVersion(TinkarTerm.IDENTIFIER_PATTERN);
        List<String> identifiersToAppend = new ArrayList<>();

        EntityService.get().forEachSemanticForComponentOfPattern(componentInDetailsViewer.nid(), TinkarTerm.IDENTIFIER_PATTERN.nid(), (semanticEntity) -> {
            viewCalc.latest(semanticEntity).ifPresent((latestSemanticVersion -> {
                EntityProxy identifierSource = latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestSemanticVersion);
                if (!PublicId.equals(identifierSource, TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)) {
                    try {
                        String idSourceName = viewCalc.getPreferredDescriptionTextWithFallbackOrNid(identifierSource);
                        String idValue = latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, latestSemanticVersion);

                        identifiersToAppend.add("%s: %s".formatted(idSourceName, idValue));
                    } catch (IndexOutOfBoundsException exception) {
                        // ignore. TODO: getFieldWithMeaning() should handle gracefully when a meaning isn't found
                        // The issue is that the starter data's identifier symantec's idValue field's meaning id does not match.
                        // When this is ignored the identifier field will just have the normal public ids.
                    }
                }
            }));
        });
        return identifiersToAppend;
    }
}
