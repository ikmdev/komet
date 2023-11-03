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

import dev.ikm.komet.navigator.graph.MultiParentVertexImpl;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

public record RestoreNavigationRecord(ImmutableIntObjectMap<RestoreVertexRecord> restoreVertexRecordMap, IntIdList selections) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        TreeSortedSet.newSet(restoreVertexRecordMap.values())
                .forEach(restoreVertexRecord -> sb.append(restoreVertexRecord).append("\n"));
        return "RestoreNavigationRecord{\n" + sb + "\n}";
    }

    public static RestoreNavigationRecord create(TreeView<ConceptFacade> tree) {
        MutableIntObjectMap<RestoreVertexRecord> vertexRecords =IntObjectMaps.mutable.empty();
        for (TreeItem<ConceptFacade> childVertex: tree.getRoot().getChildren()) {
            if (childVertex instanceof MultiParentVertexImpl multiParentChildVertex) {
                depthFirstSearchVisit(multiParentChildVertex, vertexRecords);
            }
        }
        MutableIntList selectedNids = IntLists.mutable.empty();
        tree.getSelectionModel().getSelectedItems().forEach(conceptFacadeTreeItem -> selectedNids.add(conceptFacadeTreeItem.getValue().nid()));
        return new RestoreNavigationRecord(vertexRecords.toImmutable(), IntIds.list.of(selectedNids.toArray()));
    }

    private static void depthFirstSearchVisit(MultiParentVertexImpl vertex,
                                              MutableIntObjectMap<RestoreVertexRecord> vertexRecords) {
        if (vertex.isExpanded() || vertex.isSecondaryParentOpened()) {
            vertexRecords.put(vertex.getConceptNid(), new RestoreVertexRecord(vertex.getConceptNid(),
                    vertex.isExpanded(), vertex.isSecondaryParentOpened()));
        }
        for (TreeItem<ConceptFacade> childVertex: vertex.getChildren()) {
            if (childVertex instanceof MultiParentVertexImpl multiParentChildVertex) {
                depthFirstSearchVisit(multiParentChildVertex, vertexRecords);
            }
        }
    }
}
