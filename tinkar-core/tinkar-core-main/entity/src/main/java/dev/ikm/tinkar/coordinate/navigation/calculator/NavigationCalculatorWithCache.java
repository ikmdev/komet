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
package dev.ikm.tinkar.coordinate.navigation.calculator;

import dev.ikm.tinkar.collection.ConcurrentReferenceHashMap;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.coordinate.view.VertexSortNaturalOrder;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO: Filter vertex concepts by status values.
 * TODO: Sort based on patterns in addition to natural order
 *  TODO: add cache of descendents, ancestors, and similar.
 */
public class NavigationCalculatorWithCache implements NavigationCalculator {
    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NavigationCalculatorWithCache.class);
    private static final ConcurrentReferenceHashMap<StampLangNavRecord, NavigationCalculatorWithCache> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);
    private final StampCalculatorWithCache stampCalculator;
    private final StampCalculatorWithCache vertexStampCalculator;
    private final LanguageCalculatorWithCache languageCalculator;
    private final NavigationCoordinateRecord navigationCoordinate;

    public NavigationCalculatorWithCache(StampCoordinateRecord stampFilter,
                                         ImmutableList<LanguageCoordinateRecord> languageCoordinateList,
                                         NavigationCoordinateRecord navigationCoordinate) {
        this.stampCalculator = StampCalculatorWithCache.getCalculator(stampFilter);
        this.languageCalculator = LanguageCalculatorWithCache.getCalculator(stampFilter, languageCoordinateList);
        this.navigationCoordinate = navigationCoordinate;
        this.vertexStampCalculator = StampCalculatorWithCache.getCalculator(stampFilter.withAllowedStates(navigationCoordinate.vertexStates()));
    }

    /**
     * Gets the stampCoordinateRecord.
     *
     * @return the stampCoordinateRecord
     */
    public static NavigationCalculatorWithCache getCalculator(StampCoordinateRecord stampFilter,
                                                              ImmutableList<LanguageCoordinateRecord> languageCoordinateList,
                                                              NavigationCoordinateRecord navigationCoordinate) {
        return SINGLETONS.computeIfAbsent(new StampLangNavRecord(stampFilter, languageCoordinateList, navigationCoordinate),
                filterKey -> new NavigationCalculatorWithCache(stampFilter,
                        languageCoordinateList, navigationCoordinate));
    }

    @Override
    public ImmutableList<LanguageCoordinateRecord> languageCoordinateList() {
        return languageCalculator.languageCoordinateList();
    }

    @Override
    public LanguageCalculator languageCalculator() {
        return languageCalculator;
    }

    private void addDescendents(int conceptNid, MutableIntSet nidSet) {
        if (!nidSet.contains(conceptNid)) {
            childrenOf(conceptNid).forEach(childNid -> {
                addDescendents(childNid, nidSet);
                nidSet.add(childNid);
            });
        }
    }

    private void addAncestors(int conceptNid, MutableIntSet nidSet) {
        if (!nidSet.contains(conceptNid)) {
            parentsOf(conceptNid).forEach(parentNid -> {
                addAncestors(parentNid, nidSet);
                nidSet.add(parentNid);
            });
        }
    }

    @Override
    public StampCalculatorWithCache vertexStampCalculator() {
        return this.vertexStampCalculator;
    }

    @Override
    public StateSet allowedVertexStates() {
        return vertexStampCalculator.allowedStates();
    }

    @Override
    public boolean sortVertices() {
        return navigationCoordinate.sortVertices();
    }

    @Override
    public IntIdList sortedParentsOf(int conceptNid) {
        return IntIds.list.of(VertexSortNaturalOrder.SINGLETON.sortVertexes(unsortedParentsOf(conceptNid).toArray(), this));
    }

    @Override
    public IntIdList unsortedParentsOf(int conceptNid) {
        return getIntIdListForMeaning(conceptNid, TinkarTerm.RELATIONSHIP_ORIGIN);
    }

    @Override
    public IntIdSet descendentsOf(int conceptNid) {
        MutableIntSet nidSet = IntSets.mutable.empty();
        addDescendents(conceptNid, nidSet);
        return IntIds.set.of(nidSet.toArray());
    }

    @Override
    public IntIdSet ancestorsOf(int conceptNid) {
        MutableIntSet nidSet = IntSets.mutable.empty();
        addAncestors(conceptNid, nidSet);
        return IntIds.set.of(nidSet.toArray());
    }

    @Override
    public IntIdSet kindOf(int conceptNid) {
        MutableIntSet kindOfSet = IntSets.mutable.of(conceptNid);
        kindOfSet.addAll(descendentsOf(conceptNid).toArray());
        return IntIds.set.of(kindOfSet.toArray());
    }

    @Override
    public ImmutableList<Edge> sortedChildEdges(int conceptNid) {
        return VertexSortNaturalOrder.SINGLETON.sortEdges(unsortedChildEdges(conceptNid), this);
    }

    @Override
    public ImmutableList<Edge> unsortedChildEdges(int conceptNid) {
        return getEdges(conceptNid, TinkarTerm.RELATIONSHIP_DESTINATION);
    }

    @Override
    public ImmutableList<Edge> sortedParentEdges(int conceptNid) {
        return VertexSortNaturalOrder.SINGLETON.sortEdges(unsortedParentEdges(conceptNid), this);
    }

    @Override
    public ImmutableList<Edge> unsortedParentEdges(int conceptNid) {
        return getEdges(conceptNid, TinkarTerm.RELATIONSHIP_ORIGIN);
    }

    @Override
    public IntIdList sortedChildrenOf(int conceptNid) {
        return IntIds.list.of(VertexSortNaturalOrder.SINGLETON.sortVertexes(unsortedChildrenOf(conceptNid).toArray(), this));
    }

    @Override
    public IntIdList unsortedChildrenOf(int conceptNid) {
        return getIntIdListForMeaning(conceptNid, TinkarTerm.RELATIONSHIP_DESTINATION);
    }
    @Override
    public IntIdList unsortedUnversionedChildrenOf(int conceptNid) {
        return getIntIdListForMeaningUnversioned(conceptNid, TinkarTerm.RELATIONSHIP_DESTINATION);
    }
    @Override
    public IntIdList unsortedUnversionedParentsOf(int conceptNid) {
        return getIntIdListForMeaningUnversioned(conceptNid, TinkarTerm.RELATIONSHIP_ORIGIN);
    }

    @Override
    public IntIdList toSortedList(IntIdList inputList) {
        // TODO add pattern sort...
        return IntIds.list.of(VertexSortNaturalOrder.SINGLETON.sortVertexes(inputList.toArray(), this));
    }

    @Override
    public NavigationCoordinateRecord navigationCoordinate() {
        return this.navigationCoordinate;
    }

    @Override
    public IntIdList unsortedParentsOf(int conceptNid, int patternNid) {
        return getIntIdListForMeaningFromPattern(conceptNid, TinkarTerm.RELATIONSHIP_ORIGIN, patternNid);
    }

    private ImmutableList<Edge> getEdges(int conceptNid, EntityProxy.Concept relationshipDirection) {
        MutableIntObjectMap<MutableEdge> edges = IntObjectMaps.mutable.empty();
        for (int patternNid : navigationCoordinate.navigationPatternNids().toArray()) {
            stampCalculator.latestPatternEntityVersion(patternNid).ifPresent(patternEntityVersion -> {
                int typeNid = patternEntityVersion.semanticMeaningNid();
                IntIdList parents = getIntIdListForMeaningFromPattern(conceptNid, relationshipDirection, patternNid);
                for (int parentNid : parents.toArray()) {
                    edges.updateValue(parentNid, () -> new MutableEdge(IntSets.mutable.empty(), parentNid, this.languageCalculator), mutableEdge -> {
                        mutableEdge.types.add(typeNid);
                        return mutableEdge;
                    });
                }
            });
        }
        return Lists.immutable.ofAll(edges.stream().map(mutableEdge -> mutableEdge.toEdge()).toList());
    }

    private IntIdList getIntIdListForMeaningFromPattern(int referencedComponentNid, EntityProxy.Concept fieldMeaning, int patternNid) {
        MutableIntSet nidsInList = IntSets.mutable.empty();
        intIdListForMeaningFromPattern(referencedComponentNid, fieldMeaning, patternNid, nidsInList, navigationCoordinate.vertexStates(), true);
        return IntIds.list.of(nidsInList.toArray());
    }

    private IntIdList getIntIdListForMeaning(int referencedComponentNid, EntityProxy.Concept fieldMeaning) {
        IntIdSet navigationPatternNids = navigationCoordinate.navigationPatternNids();
        MutableIntSet nidsInList = IntSets.mutable.empty();
        navigationPatternNids.forEach(navPatternNid -> {
            intIdListForMeaningFromPattern(referencedComponentNid, fieldMeaning, navPatternNid, nidsInList, navigationCoordinate.vertexStates(), true);
        });
        return IntIds.list.of(nidsInList.toArray());
    }

    private IntIdList getIntIdListForMeaningUnversioned(int referencedComponentNid, EntityProxy.Concept fieldMeaning) {
        IntIdSet navigationPatternNids = navigationCoordinate.navigationPatternNids();
        MutableIntSet nidsInList = IntSets.mutable.empty();
        navigationPatternNids.forEach(navPatternNid -> {
            intIdListForMeaningFromPattern(referencedComponentNid, fieldMeaning, navPatternNid, nidsInList, navigationCoordinate.vertexStates(), false);
        });
        return IntIds.list.of(nidsInList.toArray());
    }

    private void intIdListForMeaningFromPattern(int referencedComponentNid, EntityProxy.Concept fieldMeaning,
                                                int patternNid, MutableIntSet nidsInList, StateSet states, boolean versioned) {
        Latest<PatternEntityVersion> latestPatternEntityVersion = stampCalculator().latest(patternNid);
        latestPatternEntityVersion.ifPresentOrElse(
                (patternEntityVersion) -> {
                    int indexForMeaning = patternEntityVersion.indexForMeaning(fieldMeaning);
                    int[] semantics = PrimitiveData.get().semanticNidsForComponentOfPattern(referencedComponentNid, patternNid);
                    if (semantics.length > 1) {
                        throw new IllegalStateException("More than one navigation semantic for concept: " +
                                PrimitiveData.text(referencedComponentNid) + " in " + PrimitiveData.text(patternNid));
                    } else if (semantics.length == 0) {
                        // Nothing to add...
                    } else {
                        Latest<SemanticEntityVersion> latestNavigationSemantic = stampCalculator().latest(semantics[0]);
                        latestNavigationSemantic.ifPresent(semanticEntityVersion -> {
                            SemanticEntityVersion navigationSemantic = latestNavigationSemantic.get();
                            IntIdCollection intIdSet = (IntIdCollection) navigationSemantic.fieldValues().get(indexForMeaning);
                            // Filter here by allowed vertex state...
                            if (versioned && states != StateSet.ACTIVE_INACTIVE_AND_WITHDRAWN) {
                                intIdSet.forEach(nid ->
                                        vertexStampCalculator.latest(nid).ifPresent(entityVersion -> nidsInList.add(entityVersion.nid())));
                            } else {
                                nidsInList.addAll(intIdSet.toArray());
                            }
                        });
                    }
                },
                () -> {
                    throw new IllegalStateException("No active pattern version. " + latestPatternEntityVersion);
                });
    }

    @Override
    public StampCalculator stampCalculator() {
        return stampCalculator;
    }

    public static class CacheProvider implements CachingService {
        // TODO: this has implicit assumption that no one will hold on to a calculator... Should we be defensive?
        @Override
        public void reset() {
            SINGLETONS.clear();
        }
    }

    private record StampLangNavRecord(StampCoordinateRecord stampFilter,
                                             ImmutableList<LanguageCoordinateRecord> languageCoordinateList,
                                             NavigationCoordinateRecord navigationCoordinate) {
    }

    record MutableEdge(MutableIntSet types, int destinationNid, LanguageCalculator languageCalculator) {
        EdgeRecord toEdge() {
            return new EdgeRecord(IntIds.set.of(types.toArray()), destinationNid, languageCalculator);
        }
    }

}
