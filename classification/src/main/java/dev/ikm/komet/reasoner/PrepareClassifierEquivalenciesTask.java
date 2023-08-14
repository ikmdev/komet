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

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.ImmutableSet;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.terms.EntityProxy;

public class PrepareClassifierEquivalenciesTask extends TrackingCallable<Void> {

    final ImmutableSet<ImmutableIntList> equivalentSets;
    final TreeView<StringWithOptionalConceptFacade> equivalenciesTree;

    final ViewProperties viewProperties;

    public PrepareClassifierEquivalenciesTask(ImmutableSet<ImmutableIntList> equivalentSets,
                                              TreeView<StringWithOptionalConceptFacade> equivalenciesTree,
                                              ViewProperties viewProperties) {
        this.equivalentSets = equivalentSets;
        this.equivalenciesTree = equivalenciesTree;
        this.viewProperties = viewProperties;
    }

    @Override
    protected Void compute() throws Exception {
        TreeItem<StringWithOptionalConceptFacade> root = new TreeItem<>(new StringWithOptionalConceptFacade("Equivalences Root"));
        root.setExpanded(true);
        for (ImmutableIntList elements : equivalentSets) {
            String conceptDescriptionText = viewProperties.calculator().getDescriptionTextOrNid(elements.getFirst());
            if (conceptDescriptionText.startsWith("Product containing precisely ")) {
                conceptDescriptionText = conceptDescriptionText.replace("Product containing precisely ", "");
            }
            TreeItem<StringWithOptionalConceptFacade> setItem = new TreeItem<>(new StringWithOptionalConceptFacade(
                    "Set of " + elements.size() + " containing: " + conceptDescriptionText, EntityProxy.Concept.make(elements.getFirst())));
            root.getChildren().add(setItem);
            elements.forEach(nid -> {
                TreeItem<StringWithOptionalConceptFacade> equivalentItem = new TreeItem<>(
                        new StringWithOptionalConceptFacade(viewProperties.calculator().getDescriptionTextOrNid(nid),
                                EntityProxy.Concept.make(nid)));
                setItem.getChildren().add(equivalentItem);
            });
            setItem.getChildren().sort((o1, o2) -> NaturalOrder.compareStrings(o1.getValue().label, o2.getValue().label));
        }

        root.getChildren().sort((o1, o2) -> NaturalOrder.compareStrings(
                viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(o1.getValue().conceptFacade),
                viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(o2.getValue().conceptFacade)));
        Platform.runLater(() -> {
            equivalenciesTree.setRoot(root);
            equivalenciesTree.setShowRoot(false);
        });
        return null;
    }
}