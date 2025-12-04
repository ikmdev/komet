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
package dev.ikm.komet.framework.search;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SearchTreeView extends TreeView<Object> {

    List<Consumer<Object>> doubleCLickConsumers = new ArrayList<>();

    public SearchTreeView() {
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<Object> item = getSelectionModel().getSelectedItem();
                switch (item.getValue()) {
                    case LatestVersionSearchResult latestVersionSearchResult -> doubleCLickConsumers.forEach(objectConsumer -> objectConsumer.accept(latestVersionSearchResult.latestVersion().get()));
                    default -> doubleCLickConsumers.forEach(objectConsumer -> objectConsumer.accept(item.getValue()));
                }
            }
        });
    }

    public List<Consumer<Object>> getDoubleCLickConsumers() {
        return doubleCLickConsumers;
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
    }
}
