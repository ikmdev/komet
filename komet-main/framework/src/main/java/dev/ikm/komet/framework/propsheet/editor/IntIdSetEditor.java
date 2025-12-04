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
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

public class IntIdSetEditor extends IntIdCollectionEditor<IntIdSet> {

    public IntIdSetEditor(ViewProperties viewProperties, SimpleObjectProperty<IntIdSet> intIdSetProperty) {
        super(viewProperties, intIdSetProperty);

    }

    void updateListView(IntIdSet newValue) {
        TinkExecutor.threadPool().execute(() -> {
            MutableIntList nidList;
            if (newValue == null) {
                nidList = IntLists.mutable.empty();
            } else {
                nidList = IntLists.mutable.ofAll(newValue.intStream());
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
    void deleteSelectedItems(MultipleSelectionModel<Integer> selectionModel) {
        MutableIntSet intsToDelete = IntSets.mutable.empty();
        for (Integer nid : listView.getSelectionModel().getSelectedItems()) {
            intsToDelete.add(nid);
        }
        IntIdSet remainingNidSet = IntIds.set.of(IntSets.mutable.ofAll(getValue().intStream()).withoutAll(intsToDelete).toArray());
        setValue(remainingNidSet);
    }
}


