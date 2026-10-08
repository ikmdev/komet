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

import org.eclipse.collections.api.list.primitive.MutableLongList;

import dev.ikm.komet.navigator.graph.MultiParentVertexImpl;
import dev.ikm.tinkar.common.id.LongIdList;
import dev.ikm.tinkar.common.id.LongIds;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.map.primitive.ImmutableLongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.factory.primitive.LongObjectMaps;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

public record RestoreNavigationRecord(ImmutableLongObjectMap<RestoreVertexRecord> restoreVertexRecordMap, LongIdList selections) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        TreeSortedSet.newSet(restoreVertexRecordMap.values())
                .forEach(restoreVertexRecord -> sb.append(restoreVertexRecord).append("\n"));
        return "RestoreNavigationRecord{\n" + sb + "\n}";
    }

    public static RestoreNavigationRecord create(TreeView<ConceptFacade> tree) {
        MutableLongObjectMap<RestoreVertexRecord> vertexRecords =LongObjectMaps.mutable.empty();
        for (TreeItem<ConceptFacade> childVertex: tree.getRoot().getChildren()) {
            if (childVertex instanceof MultiParentVertexImpl multiParentChildVertex) {
                depthFirstSearchVisit(multiParentChildVertex, vertexRecords);
            }
        }
        MutableLongList selectedNids = LongLists.mutable.empty();
        tree.getSelectionModel().getSelectedItems().forEach(conceptFacadeTreeItem -> selectedNids.add(conceptFacadeTreeItem.getValue().nid()));
        return new RestoreNavigationRecord(vertexRecords.toImmutable(), LongIds.list.of(selectedNids.toArray()));
    }

    private static void depthFirstSearchVisit(MultiParentVertexImpl vertex,
                                              MutableLongObjectMap<RestoreVertexRecord> vertexRecords) {
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
