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
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static dev.ikm.tinkar.terms.TinkarTerm.*;

public class DataModelHelper {



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
                Entity.getFast(LONG.nid())
        );
    }

    public static Set<ConceptEntity> fetchStatusOpions(){
        return Set.of(
                Entity.getFast(ACTIVE_STATE.nid()),
                Entity.getFast(INACTIVE_STATE.nid()),
                Entity.getFast(WITHDRAWN_STATE.nid()),
                Entity.getFast(CANCELED_STATE.nid()),
                Entity.getFast(PRIMORDIAL_STATE.nid())
        );

    }

    public static Set<ConceptEntity> fetchDescriptionTypes(){
        return Set.of(
                Entity.getFast(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid()),
                Entity.getFast(REGULAR_NAME_DESCRIPTION_TYPE.nid())
        );
    }

    public static Set<ConceptEntity> fetchDescendentsOfConcept(ViewProperties viewProperties, PublicId publicId) {
        IntIdSet decendents = viewProperties.calculator().descendentsOf(EntityService.get().nidForPublicId(publicId));
        Set<ConceptEntity> allDecendents = decendents.intStream()
                .mapToObj(decendentNid -> (ConceptEntity) Entity.getFast(decendentNid))
                .collect(Collectors.toSet());
        return allDecendents;
    }

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

    public static boolean isInMembershipPattern(int conceptNid, int patternNid) {
        int[] semanticCount = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid, patternNid);
        // if the semantic count is empty then it is not a member of that pattern
        return semanticCount.length > 0;
    }

    public static void addToMembershipPattern(EntityFacade concept, EntityFacade pattern, ViewCalculator viewCalculator) {
        EditCoordinate editCoordinate = viewCalculator.viewCoordinateRecord().editCoordinate();
        int[] semanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(concept.nid(), pattern.nid());
        if (semanticNidsForComponent.length == 0) {
            // case 1: never a member
            createSemantic(concept, pattern, editCoordinate.toEditCoordinateRecord(), viewCalculator);
        } else {
            // a member, need to change to inactive.
            updateSemantic(pattern, semanticNidsForComponent[0], editCoordinate.toEditCoordinateRecord(), viewCalculator);
        }
    }

    public static void removeFromMembershipPattern(EntityFacade concept, EntityFacade pattern, ViewCalculator viewCalculator) {
        EditCoordinate editCoordinate = viewCalculator.viewCoordinateRecord().editCoordinate();
        int[] semanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(concept.nid(), pattern.nid());
        if (semanticNidsForComponent.length == 0) {
            // case 1: never a member
            throw new IllegalStateException("Asking to retire element that was never a member...");
        } else {
            updateSemantic(pattern, semanticNidsForComponent[0], editCoordinate.toEditCoordinateRecord(), viewCalculator);
        }
    }

    private static SemanticRecord createSemantic(EntityFacade concept, EntityFacade pattern, EditCoordinateRecord editCoordinateRecord, ViewCalculator viewCalculator) {
        PublicId newSemanticId = PublicIds.singleSemanticId(pattern.publicId(), concept.publicId());
        RecordListBuilder versionListBuilder = RecordListBuilder.make();
        //FIXME will casting to PatternFacade work here??? or will pattern.toProxy() work???
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

    private static void updateSemantic(EntityFacade pattern, int semanticNid, EditCoordinateRecord editCoordinateRecord, ViewCalculator viewCalculator) {
        SemanticRecord semanticEntity = Entity.getFast(semanticNid);
        Transaction transaction = Transaction.make();
        ViewCoordinateRecord viewRecord = viewCalculator.viewCoordinateRecord();

        Latest<PatternEntityVersion> latestPatternVersion = viewCalculator.latestPatternEntityVersion(pattern.toProxy());
        latestPatternVersion.ifPresentOrElse(patternEntityVersion -> {
            StampEntity stampEntity = transaction.getStamp(State.ACTIVE, Long.MAX_VALUE, editCoordinateRecord.getAuthorNidForChanges(),
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

    public static boolean isComponentActive(EntityFacade entityFacade) {
        return getLastEntityVersion(entityFacade).active();
    }

    public static boolean isComponentInActive(EntityFacade entityFacade) {
        return getLastEntityVersion(entityFacade).inactive();
    }

    public static EntityVersion getLastEntityVersion(EntityFacade entityFacade) {
        //FIXME will this work? EntityFacade and I want an EntityVersion to check active/inactive
        Entity entity = EntityService.get().getEntityFast(entityFacade);
        ImmutableList versions = entity.versions();
        return  (EntityVersion) versions.getLastOptional().get();
    }
}
