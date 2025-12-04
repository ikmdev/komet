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
package dev.ikm.tinkar.coordinate.stamp.calculator;

import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.NonExistentValue;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.PrimitiveDataSearchResult;
import dev.ikm.tinkar.common.util.functional.QuadConsumer;
import dev.ikm.tinkar.common.util.functional.TriConsumer;
import dev.ikm.tinkar.component.ConceptVersion;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.change.ChangeChronology;
import dev.ikm.tinkar.coordinate.stamp.change.FieldChangeRecord;
import dev.ikm.tinkar.coordinate.stamp.change.VersionChangeRecord;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeVersion;
import dev.ikm.tinkar.entity.graph.VersionVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static dev.ikm.tinkar.terms.TinkarTerm.STAMP_PATTERN;

public interface StampCalculator {
    Logger LOG = LoggerFactory.getLogger(StampCalculator.class);
    default Stream<Latest<SemanticEntityVersion>> streamLatestVersionForPattern(PatternFacade patternFacade) {
        return streamLatestVersionForPattern(patternFacade.nid());
    }

    /**
     * Determines the first stamp based only on time order from a given set of stamp identifiers.
     * The method iterates through the provided stamp IDs and compares their relative time positions,
     * identifying the one that occurs first in time.
     *
     * @param stampNids a set of integers representing stamp identifiers
     * @return the integer stamp ID that occurs first based on time order
     */
    static int firstStampTimeOnly(IntIdSet stampNids) {
        int[] stampNidsArray = stampNids.toArray();
        int first = stampNidsArray[0];
        for (int i = 1; i < stampNidsArray.length; i++) {
            switch (getRelativePositionTimeOnly(first, stampNidsArray[i])) {
                case BEFORE, EQUAL, CONTRADICTION, UNREACHABLE -> {}
                case AFTER -> first = stampNidsArray[i];
            }
        }
        return first;
    }

    /**
     * Determines the relative position of two stamps based only on their time values.
     *
     * @param stampNid1 the identifier of the first stamp
     * @param stampNid2 the identifier of the second stamp
     * @return the relative position of the first stamp compared to the second stamp,
     *         indicating whether the first stamp is BEFORE, AFTER, or EQUAL in terms of time.
     */
    static RelativePosition getRelativePositionTimeOnly(int stampNid1, int stampNid2) {
        if (stampNid1 == stampNid2) {
            return RelativePosition.EQUAL;
        }
        StampEntity stamp1 = Entity.getStamp(stampNid1);
        StampEntity stamp2 = Entity.getStamp(stampNid2);
        if (stamp1.time() < stamp2.time()) {
            return RelativePosition.BEFORE;
        }

        if (stamp1.time() > stamp2.time()) {
            return RelativePosition.AFTER;
        }

        return RelativePosition.EQUAL;
    }

    StampCoordinate stampCoordinate();


        Stream<Latest<SemanticEntityVersion>> streamLatestVersionForPattern(int patternNid);

    default Stream<SemanticEntityVersion> streamLatestActiveVersionForPattern(PatternFacade patternFacade) {
        return streamLatestActiveVersionForPattern(patternFacade.nid());
    }

    default Stream<SemanticEntityVersion> streamLatestActiveVersionForPattern(int patternNid) {
        return streamLatestVersionForPattern(patternNid)
                .filter(latestVersion -> latestVersion.ifAbsentOrFunction(() -> false, latest -> latest.active()))
                .map(semanticEntityVersionLatest -> semanticEntityVersionLatest.get());
    }

    default Stream<Entity> streamReferencedComponentIfSemanticActiveForPattern(PatternFacade patternFacade) {
        return streamReferencedComponentIfSemanticActiveForPattern(patternFacade.nid());
    }

    default Stream<Entity> streamReferencedComponentIfSemanticActiveForPattern(int patternNid) {
        return streamLatestActiveVersionForPattern(patternNid)
                .map(semanticEntityVersion -> EntityHandle.get(semanticEntityVersion.referencedComponentNid()).expectEntity());
    }

    default List<ConceptEntity> referencedConceptsIfSemanticActiveForPattern(PatternFacade patternFacade) {
        return referencedConceptsIfSemanticActiveForPattern(patternFacade.nid());
    }

    default List<ConceptEntity> referencedConceptsIfSemanticActiveForPattern(int patternNid) {
        return streamReferencedComponentIfSemanticActiveForPattern(patternNid)
                .filter(entity -> entity instanceof ConceptEntity)
                .map(entity -> (ConceptEntity) entity).toList();
    }

    default boolean isLatestActive(EntityFacade facade) {
        return isLatestActive(facade.nid());
    }

    default boolean isLatestActive(int nid) {
        Latest<EntityVersion> latest = latest(nid);
        if (latest.isPresent()) {
            return StateSet.ACTIVE.contains(latest.get().stamp().state());
        }
        return false;
    }

    <V extends EntityVersion> Latest<V> latest(int nid);

    <V extends EntityVersion> List<DiTreeVersion<V>> getVersionGraphList(Entity<V> chronicle);

    /**
     * @param semanticNid identifier of the semantic to test its latest version against the provided fields.
     * @param fields      Fields of the current state, which will be used to create a new version if necessary.
     * @param stampNid    stampNid of the new version that will contain the changed fields.
     * @return Optional new semantic record containing new version. Caller is responsible to write to entity store
     * and manage associated transaction.
     */
    default Optional<SemanticRecord> updateIfFieldsChanged(int semanticNid, ImmutableList<Object> fields, int stampNid) {
        return updateIfFieldsChanged(EntityHandle.get(semanticNid).expectSemanticRecord(), fields, stampNid);
    }

    /**
     * @param chronicle
     * @param fields
     * @param stampNid
     * @return a new SemanticRecord with the new SemanticVersionRecord added. It is the responsibility of the caller
     * to write to the store, and manage transactions.
     */
    default Optional<SemanticRecord> updateIfFieldsChanged(SemanticRecord chronicle, ImmutableList<Object> fields, int stampNid) {
        Latest<SemanticVersionRecord> latest = latest(chronicle);
        if (latest.isPresent()) {
            for (SemanticVersionRecord version : latest.getWithContradictions()) {
                if (version.fieldValues().equals(fields)) {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(chronicle.with(new SemanticVersionRecord(chronicle, stampNid, fields)).build());
    }

    default SemanticRecord updateFields(int semanticNid, ImmutableList<Object> fields, int stampNid) {
        return updateFields(EntityHandle.get(semanticNid).expectSemanticRecord(), fields, stampNid);
    }

    // TODO: maybe change references to Fields to SemanticFields in API? STAMP VALUES may also be considered fields.
    default SemanticRecord updateFields(SemanticRecord chronicle, ImmutableList<Object> fields, int stampNid) {
        return chronicle.with(new SemanticVersionRecord(chronicle, stampNid, fields)).build();
    }

    <V extends EntityVersion> Latest<V> latest(Entity<V> chronicle);

    default Optional<SemanticRecord> updateIfFieldsChanged(int semanticNid, ImmutableList<Object> fields, StampEntity stampEntity) {
        return updateIfFieldsChanged(EntityHandle.get(semanticNid).expectSemanticRecord(), fields, stampEntity.nid());
    }

    StateSet allowedStates();

    default RelativePosition relativePosition(EntityVersion v1, EntityVersion v2) {
        return relativePosition(v1.stampNid(), v2.stampNid());
    }

    RelativePosition relativePosition(int stampNid, int stampNid2);

    /**
     * Return a comparison result compatible with java.lang.Comparable used
     * by the collections API. Note that java.lang.Comparable cannot handle
     * comparisons of paths which may contain contradictions and unreachable
     * positions. Contradictions returns 0 for equals, and unreachable returns
     * Integer.MIN_VALUE so that it will sort before the reachable components. The developer may
     * consider removing all unreachable components prior to a position comparison.
     * @param stampNid
     * @param stampNid2
     */
    default int comparePositions(int stampNid, int stampNid2) {
        return switch (relativePosition(stampNid, stampNid2)) {
            case AFTER -> 1;
            case EQUAL, CONTRADICTION -> 0;
            case BEFORE -> -1;
            case UNREACHABLE -> Integer.MIN_VALUE;
        };
    }
    default RelativePosition relativePosition(StampEntity stamp1, StampEntity stamp2) {
        return relativePosition(stamp1.nid(), stamp2.nid());
    }

    default <V extends EntityVersion> Latest<V> latest(EntityFacade entityFacade) {
        return latest(entityFacade.nid());
    }

    default void forEachSemanticVersionOfPattern(PatternFacade patternFacade,
                                                 BiConsumer<SemanticEntityVersion, PatternEntityVersion> procedure) {
        forEachSemanticVersionOfPattern(patternFacade.nid(), procedure);

    }

    void forEachSemanticVersionOfPattern(int patternNid, BiConsumer<SemanticEntityVersion, PatternEntityVersion> procedure);

    void forEachSemanticVersionOfPatternParallel(int patternNid, BiConsumer<SemanticEntityVersion, PatternEntityVersion> procedure);

    default void forEachSemanticVersionForComponent(EntityFacade component,
                                                    BiConsumer<SemanticEntityVersion, EntityVersion> procedure) {
        forEachSemanticVersionForComponent(component.nid(), procedure);
    }

    void forEachSemanticVersionForComponent(int componentNid,
                                            BiConsumer<SemanticEntityVersion, EntityVersion> procedure);

    default void forEachSemanticVersionForComponentOfPattern(EntityFacade component,
                                                             PatternFacade patternFacade,
                                                             TriConsumer<SemanticEntityVersion, EntityVersion, PatternEntityVersion> procedure) {
        forEachSemanticVersionForComponentOfPattern(component.nid(), patternFacade.nid(), procedure);
    }

    void forEachSemanticVersionForComponentOfPattern(int componentNid, int patternNid, TriConsumer<SemanticEntityVersion, EntityVersion, PatternEntityVersion> procedure);

    default void forEachSemanticVersionWithFieldsOfPattern(PatternFacade patternFacade,
                                                           TriConsumer<SemanticEntityVersion, ImmutableList<? extends Field>, PatternEntityVersion> procedure) {
        forEachSemanticVersionWithFieldsOfPattern(patternFacade.nid(), procedure);
    }

    default void forEachSemanticVersionWithFieldsOfPattern(int patternNid, TriConsumer<SemanticEntityVersion, ImmutableList<? extends Field>, PatternEntityVersion> procedure) {
        forEachSemanticVersionOfPattern(patternNid, (semanticEntityVersion, patternVersion) -> procedure.accept(semanticEntityVersion, semanticEntityVersion.fields(), patternVersion));
    }

    default void forEachSemanticVersionWithFieldsForComponentOfPattern(EntityFacade component,
                                                                       PatternFacade patternFacade,
                                                                       QuadConsumer<SemanticEntityVersion, ImmutableList<? extends Field>, EntityVersion, PatternEntityVersion> procedure) {
        forEachSemanticVersionWithFieldsForComponentOfPattern(component.nid(), patternFacade.nid(), procedure);
    }

    default void forEachSemanticVersionWithFieldsForComponentOfPattern(int componentNid, int patternNid, QuadConsumer<SemanticEntityVersion, ImmutableList<? extends Field>, EntityVersion, PatternEntityVersion> procedure) {
        forEachSemanticVersionForComponentOfPattern(componentNid, patternNid, (semanticEntityVersion, entityVersion, patternEntityVersion) -> procedure.accept(semanticEntityVersion, semanticEntityVersion.fields(), entityVersion, patternEntityVersion));
    }

    default void forEachSemanticVersionWithFieldsForComponent(EntityFacade component,
                                                              TriConsumer<SemanticEntityVersion, ImmutableList<? extends Field>, EntityVersion> procedure) {
        forEachSemanticVersionWithFieldsForComponent(component.nid(), procedure);
    }

    void forEachSemanticVersionWithFieldsForComponent(int componentNid,
                                                      TriConsumer<SemanticEntityVersion, ImmutableList<? extends Field>, EntityVersion> procedure);

    default Latest<PatternEntityVersion> latestPatternEntityVersion(PatternFacade patternFacade) {
        return latestPatternEntityVersion(patternFacade.nid());
    }

    Latest<PatternEntityVersion> latestPatternEntityVersion(int patternNid);

    default Latest<ConceptEntityVersion> latestConceptVersion(ConceptFacade conceptFacade) {
        return latest(conceptFacade.nid());
    }

    default Latest<ConceptEntityVersion> latestConceptVersion(int conceptNid) {
        return latest(conceptNid);
    }

    default Latest<SemanticEntityVersion> latestSemanticVersion(int semanticNid) {
        return latest(semanticNid);
    }

    default Latest<SemanticEntityVersion> latestSemanticVersion(SemanticEntity<?> semanticEntity) {
        return (Latest<SemanticEntityVersion>) latest(semanticEntity);
    }



    OptionalInt getIndexForMeaning(int patternNid, int meaningNid);

    OptionalInt getIndexForPurpose(int patternNid, int meaningNid);

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithMeaning(SemanticEntityVersion semanticVersion, EntityFacade meaning) {
        return getFieldForSemanticWithMeaning(Latest.of(semanticVersion), meaning);
    }
    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithPurpose(SemanticEntityVersion semanticVersion, EntityFacade purpose) {
        if (semanticVersion == null) {
            return Latest.empty();
        }
        return getFieldForSemanticWithPurpose(Latest.of(semanticVersion), purpose);
    }

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithMeaning(Latest<SemanticEntityVersion> latestSemantic, EntityFacade meaning) {
        return getFieldForSemantic(latestSemantic, meaning.nid(), FieldCriterion.MEANING);
    }
    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithPurpose(Latest<SemanticEntityVersion> latestSemantic, EntityFacade meaning) {
        return getFieldForSemantic(latestSemantic, meaning.nid(), FieldCriterion.PURPOSE);
    }

    <T> Latest<Field<T>> getFieldForSemantic(Latest<SemanticEntityVersion> latestSemanticVersion, int criterionNid, FieldCriterion fieldCriterion);

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithMeaning(SemanticEntityVersion semanticVersion, int meaningNid) {
        return getFieldForSemantic(Latest.of(semanticVersion), meaningNid, FieldCriterion.MEANING);
    }
    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithPurpose(SemanticEntityVersion semanticVersion, int purposeNid) {
        return getFieldForSemantic(Latest.of(semanticVersion), purposeNid, FieldCriterion.PURPOSE);
    }

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithPurpose(Latest<SemanticEntityVersion> latestSemantic, int meaningNid) {
        return getFieldForSemantic(latestSemantic, meaningNid, FieldCriterion.PURPOSE);
    }

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithMeaning(Latest<SemanticEntityVersion> latestSemantic, int meaningNid) {
        return getFieldForSemantic(latestSemantic, meaningNid, FieldCriterion.MEANING);
    }

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithMeaning(int componentNid, EntityFacade meaning) {
        return getFieldForSemantic(componentNid, meaning.nid(), FieldCriterion.MEANING);
    }

    <T> Latest<Field<T>> getFieldForSemantic(int componentNid, int criterionNid, FieldCriterion fieldCriterion);

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithMeaning(EntityFacade component, EntityFacade meaning) {
        return getFieldForSemantic(component.nid(), meaning.nid(), FieldCriterion.MEANING);
    }

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithPurpose(int componentNid, EntityFacade purpose) {
        return getFieldForSemantic(componentNid, purpose.nid(), FieldCriterion.PURPOSE);
    }

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithPurpose(int componentNid, int purposeNid) {
        return getFieldForSemantic(componentNid, purposeNid, FieldCriterion.PURPOSE);
    }

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithPurpose(EntityFacade component, EntityFacade purpose) {
        return getFieldForSemantic(component.nid(), purpose.nid(), FieldCriterion.PURPOSE);
    }

    default <T extends Object> Latest<Field<T>> getFieldForSemanticWithMeaning(int componentNid, int meaningNid) {
        return getFieldForSemantic(componentNid, meaningNid, FieldCriterion.MEANING);
    }

    /**
     * Removes duplicate search results with the same component identifier. Necessary because
     * deletion of documents is expensive in Lucene, and also the index would not change since
     * we use an append only model of versions. Historic versions will still be in the index.
     * This method makes sure that if there are duplicate matches for the same component,
     * only the one with the highest score is returned. This method assumes that there is only
     * index being searched. It is possible in the future that multiple indexes with different
     * fields for the same component will return different results since different fields may be
     * indexed. In such case, this method will need revision to handle multiple results from multiple
     * indexes properly.
     * @param query
     * @param maxResultSize
     * @return
     * @throws Exception
     */
    default ImmutableList<LatestVersionSearchResult> search(String query, int maxResultSize) throws Exception {
        PrimitiveDataSearchResult[] primitiveResults = PrimitiveData.get().search(query, maxResultSize);
        final MutableIntObjectMap<LatestVersionSearchResult> semanticNidSearchResultMap = IntObjectMaps.mutable.ofInitialCapacity(primitiveResults.length);
        final AtomicInteger duplicates = new AtomicInteger();
        for (PrimitiveDataSearchResult primitiveResult : primitiveResults) {
            if (semanticNidSearchResultMap.containsKey(primitiveResult.nid())) {
                duplicates.incrementAndGet();
                LatestVersionSearchResult currentResult = semanticNidSearchResultMap.get(primitiveResult.nid());
                if (currentResult.score() < primitiveResult.score()) {
                    semanticNidSearchResultMap.put(primitiveResult.nid(),
                            currentResult
                                    .withScore(primitiveResult.score())
                                    .withHighlightedString(primitiveResult.highlightedString())
                    );
                }
            } else {
                Latest<SemanticEntityVersion> latestVersion = latest(primitiveResult.nid());
                latestVersion.ifPresent(semanticVersion -> semanticNidSearchResultMap.put(primitiveResult.nid(),
                        new LatestVersionSearchResult(latestVersion, primitiveResult.fieldIndex(), primitiveResult.score(),
                                primitiveResult.highlightedString())));
            }
        }
        ImmutableList<LatestVersionSearchResult> filteredResults = Lists.immutable.ofAll(semanticNidSearchResultMap.values());
        LOG.debug("Removed " + duplicates.intValue() + " duplicates. Latest result count: " + filteredResults.size());
        return filteredResults;
    }

    /**
     * Performs a lucene based search using the {@link #search(String, int)} method but applies an additional constraint
     * which restricts the search results to only those concepts that qualify as descendants of the passed in ancestor.
     * This search initializes a default navigation calculator.
     *
     * @param ancestor  Concept that is ancestor to all returned search results
     * @param query Search string
     * @param maxResultSize Search results size limit
     * @return  Immutable list of LatestVersionSearchResult records
     * @throws Exception
     */
    default ImmutableList<LatestVersionSearchResult> searchDescendants(PublicId ancestor, String query, int maxResultSize) throws Exception {
        var stampCoordinate = Coordinates.Stamp.DevelopmentLatestActiveOnly();
        var languageCoordinate = Coordinates.Language.UsEnglishRegularName();
        var navigationCoordinate = Coordinates.Navigation.inferred().toNavigationCoordinateRecord();
        var navigationCalculator = NavigationCalculatorWithCache.getCalculator(stampCoordinate, Lists.immutable.of(languageCoordinate), navigationCoordinate);
        return searchDescendants(navigationCalculator, ancestor, query, maxResultSize);
    }

    /**
     * Performs a lucene based search using the {@link #search(String, int)} method but applies an additional constraint
     * which restricts the search results to only those concepts that qualify as descendants of the passed in ancestor.
     *
     * @param navigationCalculator Navigation calculator used to compute the latest descendants of filter
     * @param ancestor Concept that is ancestor to all returned search results
     * @param query Search string
     * @param maxResultSize Search results size limit
     * @return  Immutable list of LatestVersionSearchResult records
     * @throws Exception
     */
    default ImmutableList<LatestVersionSearchResult> searchDescendants(NavigationCalculator navigationCalculator, PublicId ancestor, String query, int maxResultSize) throws Exception {
        ImmutableList<LatestVersionSearchResult> latestVersionSearchResults = search(query, maxResultSize);
        MutableList<LatestVersionSearchResult> latestVersionSearchResultMutableList = Lists.mutable.empty();

        IntIdSet descendantNids = navigationCalculator.descendentsOf(EntityService.get().nidForPublicId(ancestor));

        latestVersionSearchResults.forEach(latestVersionSearchResult -> {
            int chronologyNid = latestVersionSearchResult.latestVersion().get().chronology().referencedComponent().nid();
            if (descendantNids.contains(chronologyNid)) {
                latestVersionSearchResultMutableList.add(latestVersionSearchResult);
            }
        });

        return latestVersionSearchResultMutableList.toImmutable();
    }

    default boolean latestIsActive(Entity entity) {
        Latest<EntityVersion> latest = latest(entity);
        if (latest.isPresent()) {
            return latest.get().active();
        }
        return false;
    }

    default boolean latestIsActive(int nid) {
        Latest<EntityVersion> latest = latest(nid);
        if (latest.isPresent()) {
            return latest.get().active();
        }
        return false;
    }

    /**
     * Create a ChangeChronology for the provided nid.
     * NOTE: The returned change chronology is sorted by path origin order then just time order. Not just time order.
     * @param nid native identifier for the component.
     * @return the ChangeChronology
     */
    default ChangeChronology changeChronology(int nid) {
        return changeChronology(EntityHandle.getEntityOrThrow(nid));
    }

    /**
     * Create a ChangeChronology for the provided entity.
     * NOTE: The returned change chronology is sorted by path origin order then just time order. Not just time order.
     * @param entity
     * @return the ChangeChronology
     */
    default ChangeChronology changeChronology(Entity<?> entity) {
        MutableList<VersionChangeRecord> versionChangeRecords = Lists.mutable.withInitialCapacity(entity.versions().size());
        List<DiTreeVersion<EntityVersion>> versionGraphList = getVersionGraphList((Entity<EntityVersion>) entity);
        Latest<PatternEntityVersion> latestStampPattern = latest(STAMP_PATTERN.nid());

        latestStampPattern.ifPresent(stampPatternVersion -> {
            PatternEntityVersion patternForSemantic =
                    (entity instanceof SemanticEntity<?> semanticEntity) ? (PatternEntityVersion) latest(semanticEntity.patternNid()).get() : null;
            for (DiTreeVersion<EntityVersion> versionGraph: versionGraphList) {
                VersionVertex root = versionGraph.root();
                processVersionRecursive(versionGraph, root.vertexIndex(), versionChangeRecords,
                        stampPatternVersion, patternForSemantic);
            }
        });
        // Note PATH then TIME ordering, not strictly TIME only ordering
        versionChangeRecords.sort((o1, o2) -> comparePositions(o2.stampNid(), o1.stampNid()));

        return new ChangeChronology(entity.nid(), versionChangeRecords.toImmutable());
    }

    private static void processVersionRecursive(DiTreeVersion<EntityVersion> versionGraph, int nodeIndexToProcess,
                                                MutableList<VersionChangeRecord> versionChangeRecords,
                                                PatternEntityVersion latestPatternForStamp,
                                                PatternEntityVersion latestPatternForSemantic) {
        VersionVertex versionVertexToProcess = versionGraph.vertex(nodeIndexToProcess);
        EntityVersion newVersion = versionVertexToProcess.version();
        StampRecord newVersionStamp = Entity.getStamp(newVersion.stampNid());
        MutableList<FieldChangeRecord> fieldChanges = Lists.mutable.empty();
        versionGraph.predecessor(nodeIndexToProcess).ifPresentOrElse(predecessorIndex -> {
            EntityVersion predecessorVersion = versionGraph.vertex(predecessorIndex).version();
            StampRecord predecessorStamp = Entity.getStamp(predecessorVersion.stampNid());
            if (latestPatternForSemantic != null &&
                    newVersion instanceof SemanticEntityVersion newSemanticVersion &&
                    predecessorVersion instanceof SemanticEntityVersion predecessorSemanticVersion) {
                addChangesForSemanticFieldsForPattern(latestPatternForSemantic, newVersionStamp, predecessorStamp, fieldChanges,
                        predecessorSemanticVersion, newSemanticVersion);
            }
            addChangesForStampFieldsForPattern(latestPatternForStamp, newVersionStamp, predecessorStamp, fieldChanges, newVersion);
        }, () -> {
            if (latestPatternForSemantic != null &&
                    newVersion instanceof SemanticEntityVersion newSemanticVersion) {

                addChangesForSemanticFieldsForPattern(latestPatternForSemantic, newVersionStamp, StampRecord.nonExistentStamp(),
                        fieldChanges, null, newSemanticVersion);
            }
            addChangesForStampFieldsForPattern(latestPatternForStamp, newVersionStamp, StampRecord.nonExistentStamp(), fieldChanges, newVersion);
        });
        versionChangeRecords.add(new VersionChangeRecord(newVersion.stampNid(),
                fieldChanges.toImmutable()));
        for (int successorIndex: versionGraph.successors(nodeIndexToProcess).toArray()) {
            processVersionRecursive(versionGraph, successorIndex, versionChangeRecords, latestPatternForStamp, latestPatternForSemantic);
        }
    }

    private static void addChangesForSemanticFieldsForPattern(PatternEntityVersion latestPatternVersion, StampRecord newVersionStamp,
                                                              StampRecord predecessorStamp, MutableList<FieldChangeRecord> fieldChanges,
                                                              SemanticEntityVersion priorVersion, SemanticEntityVersion newVersion) {
        for (int fieldIndex = 0; fieldIndex < latestPatternVersion.fieldDefinitions().size(); fieldIndex++) {
            FieldDefinitionForEntity fieldDefinitionRecord = latestPatternVersion.fieldDefinitions().get(fieldIndex);
            Object newVersionValue = (newVersion != null) ? newVersion.fieldValues().get(fieldIndex): NonExistentValue.get();
            Object priorVersionValue = (priorVersion != null) ? priorVersion.fieldValues().get(fieldIndex): NonExistentValue.get();
            if (!Objects.deepEquals(newVersionValue, priorVersionValue)) {
                fieldChanges.add(makeFieldChangeRecord(newVersionValue, newVersion,
                        fieldDefinitionRecord, priorVersionValue, predecessorStamp));
            }
        }
    }

    private static void addChangesForStampFieldsForPattern(PatternEntityVersion latestPatternVersion, StampRecord newVersionStamp,
                                                           StampRecord predecessorStamp, MutableList<FieldChangeRecord> fieldChanges,
                                                           EntityVersion newVersion) {
        for (int fieldIndex = 0; fieldIndex < latestPatternVersion.fieldDefinitions().size(); fieldIndex++) {
            FieldDefinitionForEntity fieldDefinitionRecord = latestPatternVersion.fieldDefinitions().get(fieldIndex);
            Object newVersionValue = newVersionStamp.fieldValues().get(fieldIndex);
            Object priorVersionValue = predecessorStamp.fieldValues().get(fieldIndex);
            if (!Objects.deepEquals(newVersionValue, priorVersionValue)) {
                fieldChanges.add(makeFieldChangeRecord(newVersionValue, newVersion,
                        fieldDefinitionRecord, priorVersionValue, predecessorStamp));
            }
        }
    }

    private static FieldChangeRecord makeFieldChangeRecord(Object newVersionValue, EntityVersion newVersion,
                                                           FieldDefinitionForEntity fieldDefinitionRecord,
                                                           Object priorVersionValue, StampEntity predecessorStamp) {
        FieldRecord currentStatusRecord = new FieldRecord(newVersionValue,
                newVersion.nid(), newVersion.stampNid(), fieldDefinitionRecord.patternNid(), fieldDefinitionRecord.indexInPattern());
        FieldRecord predecessorStatusRecord = new FieldRecord(priorVersionValue,
                newVersion.nid(), predecessorStamp.nid(), fieldDefinitionRecord.patternNid(), fieldDefinitionRecord.indexInPattern());
        return new FieldChangeRecord(predecessorStatusRecord, currentStatusRecord);
    }

    enum FieldCriterion {MEANING, PURPOSE}
}
