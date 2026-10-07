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
package dev.ikm.komet.reasoner;

import dev.ikm.komet.framework.observable.collection.ObservableLongList;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.set.primitive.ImmutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.util.text.NaturalOrder;

import java.util.concurrent.ConcurrentSkipListSet;

public class PrepareConceptSetTask extends TrackingCallable<Void> {


    private final ImmutableLongList affectedConceptList;
    private final ObservableList<Long> affectedConceptsForDisplay;
    final ViewProperties viewProperties;

    public PrepareConceptSetTask(String title, ImmutableLongSet affectedConcepts,
                                 ObservableList<Long> affectedConceptsForDisplay,
                                 final ViewProperties viewProperties) {
        this(title, LongLists.immutable.of(affectedConcepts.toArray()), affectedConceptsForDisplay, viewProperties);
    }
    public PrepareConceptSetTask(String title, ImmutableLongList affectedConcepts,
                                 ObservableList<Long> affectedConceptsForDisplay,
                                 final ViewProperties viewProperties) {
        this.affectedConceptList = affectedConcepts;
        this.affectedConceptsForDisplay = affectedConceptsForDisplay;
        this.viewProperties = viewProperties;
        this.updateTitle(title);
        this.addToTotalWork(affectedConcepts.size());
        Platform.runLater(() -> {
            affectedConceptsForDisplay.setAll(affectedConcepts.toList().collect(i -> Long.valueOf(i)));
        });
    }

    @Override
    protected Void compute() throws Exception {

        // TODO: Remove performance hack, and get sorting to work better/faster.
        if (affectedConceptList.size() < 5000) {
            //TODO: This sorting needs much more efficient algorithms and data structures...
            //TODO: we need an iterator that will go through the descriptions in concept order, for cache locality.
            ConcurrentSkipListSet<Long> concurrentSortedSet = new ConcurrentSkipListSet<>(this::compare);

            this.affectedConceptList.primitiveParallelStream().forEach(nid -> {
                concurrentSortedSet.add(nid);
                this.completedUnitOfWork();
            });
            Platform.runLater(() -> {
                this.affectedConceptsForDisplay.setAll(concurrentSortedSet);
            });
        } else {
            if (affectedConceptsForDisplay instanceof ObservableLongList observableLongList) {
                observableLongList.setAll(affectedConceptList.toList().collect(i -> Long.valueOf(i)));
            }
        }
            return null;
    }

    private int compare(Long o1, Long o2) {

        return NaturalOrder.compareStrings(
                this.viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(o1),
                this.viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(o2));
    }
}
