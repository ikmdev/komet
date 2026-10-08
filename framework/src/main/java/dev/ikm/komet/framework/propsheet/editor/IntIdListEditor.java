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
import org.eclipse.collections.impl.factory.primitive.LongLists;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.LongIdList;
import dev.ikm.tinkar.common.id.LongIds;
import dev.ikm.tinkar.common.service.TinkExecutor;

public class IntIdListEditor extends IntIdCollectionEditor<LongIdList> {

    public IntIdListEditor(ViewProperties viewProperties, SimpleObjectProperty<LongIdList> intIdListProperty) {
        super(viewProperties, intIdListProperty);

    }

    void updateListView(LongIdList newValue) {
        TinkExecutor.threadPool().execute(() -> {
            Platform.runLater(() -> {
                listView.getItems().clear();
                if (newValue != null) {
                    listView.getItems().addAll(newValue.longStream().mapToObj(nid -> nid).toList());
                }
            });
        });
    }

    @Override
    void deleteSelectedItems(MultipleSelectionModel<Long> selectionModel) {
        MutableLongList currentList = LongLists.mutable.of(getValue().toArray());
        for (Integer index : listView.getSelectionModel().getSelectedIndices().sorted((x, y) -> Integer.compare(y, x))) {
            currentList.removeAtIndex(index);
        }
        setValue(LongIds.list.of(currentList.toArray()));
    }
}
