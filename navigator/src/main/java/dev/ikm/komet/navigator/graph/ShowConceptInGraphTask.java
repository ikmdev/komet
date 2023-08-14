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
 import javafx.application.Platform;
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

        final MutableIntList pathToRoot = IntLists.mutable.empty();

        pathToRoot.add(conceptNid);

        // Walk up taxonomy to origin until no parent found.
        int currentNid = conceptNid;

        while (true) {
            Optional<? extends ConceptEntity> conceptOptional = Entity.get(currentNid);

            if (!conceptOptional.isPresent()) {
                // Must be a "pending concept".
                // Not handled yet.
                multiParentGraphView.dispatchAlert(AlertObject.makeWarning("Concept is not in datastore",
                        "Could be a pending uncommitted concept: " +
                                multiParentGraphView.getViewCalculator().getDescriptionTextOrNid(currentNid)));
                return null;
            }

            ConceptEntity concept = conceptOptional.get();

            // Look for an IS_A relationship to origin.
            boolean found = false;

            for (int parent : multiParentGraphView.getNavigator().getParentNids(concept.nid())) {
                currentNid = parent;
                pathToRoot.add(currentNid);
                found = true;
                break;
            }

            // No parent IS_A relationship found, stop looking.
            if (!found) {
                if (concept.nid() != TinkarTerm.ROOT_VERTEX.nid()) {
                    multiParentGraphView.dispatchAlert(AlertObject.makeWarning("No parents for concept",
                            multiParentGraphView.getViewCalculator().getDescriptionTextOrNid(concept)));
                }
                break;
            }
        }

        final MutableIntList reversedPathToRoot = pathToRoot.reverseThis();
        LOG.atDebug().log(() -> String.format("Calculated root path {}", Arrays.toString(reversedPathToRoot.toArray())));
        Platform.runLater(() -> {
            this.multiParentGraphView.expandAndSelect(reversedPathToRoot);
        });
        return null;
    }
}
