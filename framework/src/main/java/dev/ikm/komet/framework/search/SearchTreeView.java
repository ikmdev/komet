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
