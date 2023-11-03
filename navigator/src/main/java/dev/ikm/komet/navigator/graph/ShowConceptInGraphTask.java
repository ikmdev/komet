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
package dev.ikm.komet.navigator.graph;

 import dev.ikm.komet.navigator.graph.MultiParentGraphViewController;
 import dev.ikm.tinkar.common.id.IntIdList;
 import dev.ikm.tinkar.common.id.IntIds;
 import dev.ikm.tinkar.common.service.PrimitiveData;
 import javafx.application.Platform;
 import org.eclipse.collections.api.factory.Lists;
 import org.eclipse.collections.api.list.ImmutableList;
 import org.eclipse.collections.api.list.MutableList;
 import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
 import java.util.SortedSet;
 import java.util.TreeSet;

/**
 * 
 */
public class ShowConceptInGraphTask extends TrackingCallable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ShowConceptInGraphTask.class);

    private final MultiParentGraphViewController multiParentGraphView;
    private final int conceptNid;
    private final String conceptDescription;

    public ShowConceptInGraphTask(MultiParentGraphViewController multiParentGraphView, int conceptNid) {
        this.multiParentGraphView = multiParentGraphView;
        this.conceptNid = conceptNid;
        this.conceptDescription = multiParentGraphView.getViewCalculator().getDescriptionTextOrNid(this.conceptNid);
        updateTitle("Expanding taxonomy to: " + conceptDescription);
    }

    @Override
    protected Void compute() throws Exception {
        // await() init() completion.

        IntIdList pathSeed = IntIds.list.of(conceptNid);
        ImmutableList<IntIdList> pathsToRoot = findPathsToRoot(conceptNid, Lists.immutable.of(pathSeed));

        SortedSet<PathToRootWithScore> sortedListsForReturn = new TreeSet<>();
        for (IntIdList pathToRoot: pathsToRoot) {
            int score = 0;
            for (int pathConceptNid: pathToRoot.toArray()) {
                IntIdList childCount = multiParentGraphView.getNavigator().getViewCalculator().unsortedUnversionedChildrenOf(pathConceptNid);
                score += childCount.size();
            }
            sortedListsForReturn.add(new PathToRootWithScore(pathToRoot, score));
        }
        // TODO: One of the sortedListsForReturn for Urine Homocystine Measurement, did not end at Solor (ended earlier)
        // Need to understand why. Workaround for now. Possibly data error in the parent for Amino acids measurement.
        // 17:26:33,328 [INFO  ] t.navigator.graph.ShowConceptInGraphTask - Calculated root path: [Amino acids measurement, Homocystine measurement, Urine homocystine measurement]
        // There are other options in the list that include SOLOR_CONCEPT, so we will find the lowest score that ends with
        // Solor concept
        IntIdList bestPath = sortedListsForReturn.first().pathToRoot;
        if (bestPath.get(bestPath.size()-1) != TinkarTerm.SOLOR_CONCEPT.nid()) {
            for (PathToRootWithScore pathToRootWithScore: sortedListsForReturn) {
                if (pathToRootWithScore.pathToRoot.get(pathToRootWithScore.pathToRoot.size()-1)
                        == TinkarTerm.SOLOR_CONCEPT.nid()) {
                    bestPath = pathToRootWithScore.pathToRoot;
                    break;
                }
            }
        }


        final IntIdList reversedPathToRoot = IntIds.list.of(IntLists.mutable.of(bestPath.toArray()).reverseThis().toArray());
        LOG.atInfo().log(() -> String.format("Calculated root path: " + Arrays.toString(PrimitiveData.textList(reversedPathToRoot.toArray()).toArray())));
        this.multiParentGraphView.expandAndSelect(reversedPathToRoot);
        return null;
    }

    private ImmutableList<IntIdList> findPathsToRoot(int conceptNid, ImmutableList<IntIdList> incomingLists) {
        int[] parentNids = multiParentGraphView.getNavigator().getParentNids(conceptNid);
        if (parentNids.length == 0) {
            return incomingLists; // Nothing to add
        }
        MutableList<IntIdList> listsForReturn = Lists.mutable.ofInitialCapacity(incomingLists.size() * parentNids.length);

        for (int parentNid: parentNids) {
            MutableList<IntIdList> listsWithAddedParent = Lists.mutable.ofInitialCapacity(incomingLists.size());
            for (IntIdList incomingList: incomingLists) {
                listsWithAddedParent.add(incomingList.with(parentNid));
            }
            listsForReturn.addAll(findPathsToRoot(parentNid, listsWithAddedParent.toImmutableList()).castToList());
        }
        return listsForReturn.toImmutableList();
    }

    private record PathToRootWithScore(IntIdList pathToRoot, int score) implements Comparable<PathToRootWithScore> {

        @Override
        public int compareTo(PathToRootWithScore o) {
            return Integer.compare(this.score, o.score);
        }
    }
}
