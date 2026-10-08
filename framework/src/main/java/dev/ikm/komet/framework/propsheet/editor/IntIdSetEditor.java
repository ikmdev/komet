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
package dev.ikm.komet.framework.propsheet.editor;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.MultipleSelectionModel;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.LongIdSet;
import dev.ikm.tinkar.common.id.LongIds;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

public class IntIdSetEditor extends IntIdCollectionEditor<LongIdSet> {

    public IntIdSetEditor(ViewProperties viewProperties, SimpleObjectProperty<LongIdSet> intIdSetProperty) {
        super(viewProperties, intIdSetProperty);

    }

    void updateListView(LongIdSet newValue) {
        TinkExecutor.threadPool().execute(() -> {
            MutableLongList nidList;
            if (newValue == null) {
                nidList = LongLists.mutable.empty();
            } else {
                nidList = LongLists.mutable.ofAll(newValue.longStream());
            }

            ViewCalculator calculator = viewProperties.calculator();
            nidList.sortThis((nid1, nid2) -> calculator.getDescriptionTextOrNid(nid1)
                    .compareTo(calculator.getDescriptionTextOrNid(nid2)));
            Platform.runLater(() -> {
                listView.getItems().clear();
                listView.getItems().addAll(nidList.toList().primitiveStream().mapToObj(nid -> nid).toList());

            });
        });
    }

    @Override
    void deleteSelectedItems(MultipleSelectionModel<Long> selectionModel) {
        MutableLongSet intsToDelete = LongSets.mutable.empty();
        for (Long nid : listView.getSelectionModel().getSelectedItems()) {
            intsToDelete.add(nid);
        }
        LongIdSet remainingNidSet = LongIds.set.of(LongSets.mutable.ofAll(getValue().longStream()).withoutAll(intsToDelete).toArray());
        setValue(remainingNidSet);
    }
}


