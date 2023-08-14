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

import javafx.application.Platform;
import org.eclipse.collections.api.collection.ImmutableCollection;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.util.thread.TaskCountManager;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class FetchChildren extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(FetchChildren.class);
    private static final AtomicInteger FETCHER_SEQUENCE = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, FetchChildren> FETCHER_MAP = new ConcurrentHashMap<>();

    private final CountDownLatch childrenLoadedLatch;
    private final MultiParentVertexImpl parentGraphItem;
    private final int fetcherId = FETCHER_SEQUENCE.incrementAndGet();
    private final String parentName;
    private final ViewCalculator viewCalculator;
    private int childrenFound = 0;

    public FetchChildren(CountDownLatch childrenLoadedLatch,
                         MultiParentVertexImpl parentGraphItem) {
        super(false, true);
        this.childrenLoadedLatch = childrenLoadedLatch;
        this.parentGraphItem = parentGraphItem;
        this.viewCalculator = parentGraphItem.getViewCalculator();
        if (parentGraphItem.getValue() != null) {
            this.parentName = this.viewCalculator.getDescriptionTextOrNid(parentGraphItem.getValue().nid());
        } else {
            this.parentName = parentGraphItem.toString();
        }
        updateTitle("Fetching children for: " + this.parentName);

        FetchChildren oldFetcher = FETCHER_MAP.put(parentGraphItem.getValue().nid(), this);

        if (oldFetcher != null) {
            oldFetcher.cancel();  //Interrupts are bad for code that uses NIO.
        }
    }

    @Override
    public Void compute() throws Exception {
        try {
            final ConceptFacade conceptFacade = parentGraphItem.getValue();

            if (conceptFacade == null) {
                LOG.debug("addChildren(): ConceptEntity={}", conceptFacade);
            } else {  // if (ConceptEntity != null)
                // Gather the children
                ConcurrentSkipListSet<MultiParentVertexImpl> childrenToAdd = new ConcurrentSkipListSet<>();
                Navigator navigator = parentGraphItem.getGraphController().getNavigator();
                ObservableView observableView = parentGraphItem.getGraphController().getObservableView();
                ImmutableCollection<Edge> children = navigator.getChildEdges(conceptFacade.nid());

                addToTotalWork(children.size() + 1);

                TaskCountManager taskCountManager = TaskCountManager.get();
                for (Edge childLink : children) {
                    taskCountManager.acquire();
                    TinkExecutor.threadPool().execute(() -> {
                        try {
                            ConceptEntity childChronology = Entity.getFast(childLink.destinationNid());
                            MultiParentVertexImpl childItem = new MultiParentVertexImpl(childChronology, parentGraphItem.getGraphController(), childLink.typeNids(), null);
                            childItem.setDefined(this.viewCalculator.hasSufficientSet(childChronology));
                            childItem.toString();
                            childItem.setMultiParent(navigator.getParentNids(childLink.destinationNid()).length > 1);
                            childItem.isLeaf();

                            if (childItem.shouldDisplay()) {
                                childrenToAdd.add(childItem);
                            } else {
                                LOG.debug(
                                        "item.shouldDisplay() == false: not adding " + childItem.getConceptPublicId() + " as child of "
                                                + parentGraphItem.getConceptPublicId());
                            }
                        } finally {
                            taskCountManager.release();
                        }
                    });

                    completedUnitOfWork();
                    if (isCancelled()) return null;
                }
                taskCountManager.waitForCompletion();
                if (isCancelled()) return null;
                Platform.runLater(
                        () -> {
                            if (!FetchChildren.this.isCancelled()) {
                                LOG.trace("Adding children for: " + parentGraphItem.getValue().nid()
                                        + " from: " + fetcherId);
                                parentGraphItem.getChildren().setAll(childrenToAdd);
                                parentGraphItem.setExpanded(true);
                                completedUnitOfWork();
                            }

                        });

                childrenFound = childrenToAdd.size();
            }
            updateTitle("Fetched " + childrenFound + " children for " + this.parentName);
            updateMessage("In " + durationString());
            return null;
        } finally {
            childrenLoadedLatch.countDown();
            FETCHER_MAP.remove(parentGraphItem.getValue().nid());
            if (FetchChildren.this.isCancelled()) {
                LOG.debug("Canceled Adding children for: " + parentGraphItem.getValue().nid()
                        + " from: " + fetcherId);
            } else {
                LOG.trace("Finished Adding children for: " + parentGraphItem.getValue().nid()
                        + " from: " + fetcherId);
            }
        }
    }
}
