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
package dev.ikm.komet.navigator.graph.treetasks;

import dev.ikm.komet.navigator.graph.MultiParentGraphViewController;
import dev.ikm.komet.navigator.graph.MultiParentVertexImpl;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import javafx.application.Platform;
import javafx.scene.Node;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class ExpandTask extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(ExpandTask.class);
    private final MultiParentGraphViewController multiParentGraphViewController;
    final IntIdList expansionPath;

    public ExpandTask(MultiParentGraphViewController multiParentGraphViewController, IntIdList expansionPath) {
        this.multiParentGraphViewController = multiParentGraphViewController;
        this.expansionPath = expansionPath;
        Platform.runLater(() -> {
            multiParentGraphViewController.getTreeView().getSelectionModel().clearSelection();
            multiParentGraphViewController.getTreeView().getRoot().getChildren().clear();
        });
    }

    @Override
    protected Void compute() {
        LOG.info("Starting expansion of: " + expansionPath);

        int conceptNid = expansionPath.get(0);
        MutableList<MutableList<MultiParentVertexImpl>> siblingLists = Lists.mutable.ofInitialCapacity(expansionPath.size());
        MutableList<MultiParentVertexImpl> pathParentList = Lists.mutable.ofInitialCapacity(expansionPath.size());
        final MultiParentVertexImpl newTreeTop = new MultiParentVertexImpl(
                Entity.getFast(conceptNid),
                multiParentGraphViewController,
                IntIds.set.empty(),
                null
        );

        AtomicReference<MultiParentVertexImpl> pathParent = new AtomicReference<>(newTreeTop);
        siblingLists.add(Lists.mutable.of(pathParent.get()));

        for (int i = 1; i < expansionPath.size(); i++) {
            ImmutableCollection<Edge> childrenEdges = multiParentGraphViewController.getNavigator().getChildEdges(pathParent.get().getConceptNid());

            MutableList<MultiParentVertexImpl> childrenVertexes = Lists.mutable.ofInitialCapacity(childrenEdges.size());
            siblingLists.add(childrenVertexes);
            Node vertexGraphic = null;

            int nextParentNid = expansionPath.get(i);

            AtomicReference<MultiParentVertexImpl> newPathParent = new AtomicReference<>();
            for (Edge edge : childrenEdges) {
                ConceptEntity conceptEntity = Entity.getFast(edge.destinationNid());
                MultiParentVertexImpl childVertex = new MultiParentVertexImpl(conceptEntity,
                        multiParentGraphViewController, edge.typeNids(), vertexGraphic);
                childVertex.updateDescription();
                childVertex.setGraphic(childVertex.computeGraphic());
                childVertex.setMultiParent(multiParentGraphViewController.getViewCalculator().isMultiparent(edge.destinationNid()));
                childrenVertexes.add(childVertex);
                if (conceptEntity.nid() == nextParentNid) {
                    newPathParent.set(childVertex);
                    if (i < expansionPath.size() - 1) {
                        childVertex.setExpanded(true);
                    }
                    pathParentList.add(childVertex);
                }
            }
            // Add the children to each vertex.
            pathParent.get().getChildren().addAll(childrenVertexes);
            pathParent.set(newPathParent.get());
        }
        // Add the tinkar root node and select the root given vertex,
        // expand the selected node and scroll to the concept.
        Platform.runLater(() -> {
            multiParentGraphViewController.getTreeView().getRoot().getChildren().add(newTreeTop);
            newTreeTop.setExpanded(true);
            MultiParentVertexImpl vertexToSelect = pathParentList.get(pathParentList.size() - 1);
            multiParentGraphViewController.getTreeView().getSelectionModel().select(vertexToSelect);
            multiParentGraphViewController.getTreeView().getSelectionModel().getSelectedItems().getFirst().setExpanded(true);
            int selectedIndex = multiParentGraphViewController.getTreeView().getSelectionModel().getSelectedIndex();
            multiParentGraphViewController.getTreeView().scrollTo(selectedIndex);
        });
        return null;
    }

}
