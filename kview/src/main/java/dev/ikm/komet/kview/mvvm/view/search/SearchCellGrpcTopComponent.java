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
package dev.ikm.komet.kview.mvvm.view.search;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.grpc.GrpcSearchService;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.UUID;

/**
 * A {@link ListCell} that renders a {@link GrpcSearchService.GroupedResult} using
 * the same visual structure as the local-search {@code SearchCellTopComponent}, but driven
 * entirely by data returned over gRPC (no local entity store required).
 *
 * <p>Uses raw {@link ListCell} (not typed) to avoid the JavaFX cell-reuse bug where defunct
 * cells from a previous cell factory still receive item updates of the wrong type.
 */
@SuppressWarnings("rawtypes")
public class SearchCellGrpcTopComponent extends ListCell {

    private final VBox root = new VBox();
    private final ImageView identiconView = new ImageView();
    private final StackPane identiconContainer = new StackPane(identiconView);
    private final Label retiredLabel = new Label("Retired");
    private final Text componentText = new Text();
    private final TextFlow componentTextFlow = new TextFlow(componentText);
    private final HBox headerHBox = new HBox(11);
    private final ListView<GrpcSearchService.MatchingSemantic> descriptionsListView = new ListView<>();
    private final TitledPane titledPane = new TitledPane("DESCRIPTION SEMANTICS", descriptionsListView);

    public SearchCellGrpcTopComponent() {
        identiconView.setFitHeight(20);
        identiconView.setFitWidth(20);
        identiconView.setPreserveRatio(true);
        identiconContainer.getStyleClass().add("identicon-container");

        retiredLabel.getStyleClass().add("search-retired-label");
        retiredLabel.setPadding(new Insets(6, 3, 6, 3));

        componentTextFlow.getStyleClass().add("search-entry-text-container");
        componentTextFlow.setPrefWidth(253);
        componentText.getStyleClass().add("search-entry-text");

        headerHBox.getStyleClass().add("search-entry-header");
        headerHBox.setMaxWidth(Double.MAX_VALUE);
        headerHBox.setPrefWidth(346);

        titledPane.setAnimated(false);
        titledPane.setExpanded(false);
        titledPane.getStyleClass().add("search-entry-title-pane");

        descriptionsListView.setMaxHeight(426);
        descriptionsListView.setFixedCellSize(40);
        descriptionsListView.getStyleClass().add("descriptions-list-view");
        descriptionsListView.setCellFactory(lv -> new GrpcDescriptionCell());

        BorderPane headerPane = new BorderPane(headerHBox);
        root.getStyleClass().add("search-entry-container");
        root.setPrefWidth(332);
        root.getChildren().addAll(headerPane, titledPane);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty || !(item instanceof GrpcSearchService.GroupedResult)) {
            setGraphic(null);
            return;
        }
        GrpcSearchService.GroupedResult grpcItem = (GrpcSearchService.GroupedResult) item;

        // Identicon from public UUIDs
        if (grpcItem.publicId() != null && !grpcItem.publicId().isEmpty()) {
            try {
                UUID[] uuids = grpcItem.publicId().stream()
                        .map(UUID::fromString)
                        .toArray(UUID[]::new);
                identiconView.setImage(Identicon.generateIdenticonImage(PublicIds.of(uuids)));
            } catch (Exception e) {
                identiconView.setImage(null);
            }
        }

        // Concept name
        componentText.setText(grpcItem.fullyQualifiedName());

        // Active / Retired header row
        headerHBox.getChildren().clear();
        headerHBox.getChildren().add(identiconContainer);
        if (!grpcItem.active()) {
            headerHBox.getChildren().add(retiredLabel);
        }
        headerHBox.getChildren().add(componentTextFlow);

        // Description semantics list
        List<GrpcSearchService.MatchingSemantic> matches = grpcItem.matchingSemantics();
        descriptionsListView.getItems().setAll(matches != null ? matches : List.of());
        int count = matches != null ? matches.size() : 0;
        descriptionsListView.setPrefHeight(Math.min(count * 43.0, 426));

        setGraphic(root);
    }

    // Inner cell for each matching semantic entry
    private static class GrpcDescriptionCell extends ListCell<GrpcSearchService.MatchingSemantic> {

        private final HBox container = new HBox();
        private final TextFlow textFlow = new TextFlow();

        public GrpcDescriptionCell() {
            container.getChildren().add(textFlow);
            container.getStyleClass().add("cell-container");
            textFlow.getStyleClass().add("text-container");
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(GrpcSearchService.MatchingSemantic item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setGraphic(null);
                return;
            }
            String text = item.highlightedText() != null && !item.highlightedText().isEmpty()
                    ? item.highlightedText()
                    : item.plainText() != null ? item.plainText() : "";
            updateTextFlow(text);
            setGraphic(container);
        }

        private void updateTextFlow(String highlighted) {
            textFlow.getChildren().clear();
            String[] words = highlighted.split(" ");
            for (String word : words) {
                Text text = new Text();
                StackPane wordContainer = new StackPane(text);
                if (word.contains("<B>") || word.contains("<b>")) {
                    text.setText(word.replaceAll("</?[Bb]>", "").replaceAll("\\s+", " "));
                    wordContainer.getStyleClass().add("highlight");
                } else {
                    text.setText(word);
                }
                wordContainer.getStyleClass().add("word-container");
                textFlow.getChildren().add(wordContainer);
            }
        }
    }
}
