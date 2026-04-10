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
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.UUID;

/**
 * A {@link ListCell} that renders a {@link GrpcSearchService.SemanticResult} using
 * the same visual structure as the local-search {@code SearchCellDescriptionSemantic}, but
 * driven entirely by data returned over gRPC (no local entity store required).
 *
 * <p>Uses raw {@link ListCell} (not typed) to avoid the JavaFX cell-reuse bug where defunct
 * cells from a previous cell factory still receive item updates of the wrong type.
 */
@SuppressWarnings("rawtypes")
public class SearchCellGrpcSemanticResult extends ListCell {

    private final BorderPane root = new BorderPane();
    private final ImageView identiconView = new ImageView();
    private final Label retiredLabel = new Label("Retired");
    private final Text semanticText = new Text();
    private final TextFlow textFlow = new TextFlow(semanticText);
    private final HBox retiredHBox = new HBox();

    public SearchCellGrpcSemanticResult() {
        identiconView.setFitHeight(24);
        identiconView.setFitWidth(20);
        identiconView.setPreserveRatio(true);

        retiredLabel.getStyleClass().add("search-retired-label");
        retiredLabel.setPadding(new Insets(6, 3, 6, 3));

        textFlow.getStyleClass().add("semantic-text-container");
        textFlow.setPrefWidth(164);
        semanticText.getStyleClass().add("search-semantic-entry-text");

        retiredHBox.getStyleClass().add("search-entry-left-pane");
        retiredHBox.setMaxWidth(Double.MAX_VALUE);
        retiredHBox.setPrefWidth(346);
        retiredHBox.setPadding(new Insets(3, 3, 3, 3));

        root.getStyleClass().add("search-entry-hbox");
        root.setPrefWidth(332);
        root.setPrefHeight(30);
        root.setCenter(retiredHBox);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty || !(item instanceof GrpcSearchService.SemanticResult)) {
            setGraphic(null);
            return;
        }
        GrpcSearchService.SemanticResult result = (GrpcSearchService.SemanticResult) item;

        // Identicon from concept public UUIDs
        if (result.publicId() != null && !result.publicId().isEmpty()) {
            try {
                UUID[] uuids = result.publicId().stream()
                        .map(UUID::fromString)
                        .toArray(UUID[]::new);
                identiconView.setImage(Identicon.generateIdenticonImage(PublicIds.of(uuids)));
            } catch (Exception e) {
                identiconView.setImage(null);
            }
        }

        // Semantic text: strip HTML bold tags for the flat view
        String text = result.highlightedText() != null && !result.highlightedText().isEmpty()
                ? result.highlightedText().replaceAll("</?[Bb]>", "").replaceAll("\\s+", " ")
                : result.fullyQualifiedName() != null ? result.fullyQualifiedName() : "";
        semanticText.setText(text);

        // Rebuild header row: identicon, optional retired badge, text
        retiredHBox.getChildren().clear();
        HBox.setMargin(identiconView, new Insets(4, 0, 4, 6));
        retiredHBox.getChildren().add(identiconView);
        if (!result.active()) {
            HBox.setMargin(retiredLabel, new Insets(4, 0, 0, 0));
            retiredHBox.getChildren().add(retiredLabel);
        } else {
            textFlow.getStyleClass().add("search-semantic-active");
        }
        HBox.setMargin(textFlow, new Insets(8, 36, 8, 2));
        retiredHBox.getChildren().add(textFlow);

        setGraphic(root);
    }
}
