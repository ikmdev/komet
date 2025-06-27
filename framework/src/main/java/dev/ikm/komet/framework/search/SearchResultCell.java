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

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.dnd.DragDetectedCellEventHandler;
import dev.ikm.komet.framework.dnd.DragDoneEventHandler;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;

import static dev.ikm.komet.framework.PseudoClasses.INACTIVE_PSEUDO_CLASS;
import static dev.ikm.komet.framework.StyleClasses.*;

public class SearchResultCell extends TreeCell<Object> {

    private static int columnWidth = 24;
    double width = -1;

    public SearchResultCell() {
        this.setMinHeight(USE_COMPUTED_SIZE);
        this.setPrefHeight(USE_COMPUTED_SIZE);
        this.setMaxHeight(USE_COMPUTED_SIZE);

        this.setOnDragDetected(new DragDetectedCellEventHandler());
        this.setOnDragDone(new DragDoneEventHandler());
    }

    int depth() {
        TreeItem thisItem = getTreeItem();

        // 1 to account for the hidden root node
        int depth = 1;
        while (thisItem != null) {
            depth++;
            thisItem = thisItem.getParent();
        }
        return depth;
    }

    int insetWidth() {
        return depth() * columnWidth;
    }

    public void updateItem(Object item, boolean empty, double width) {
        width = width;
    }

    @Override
    public void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        setText("");
        setGraphic(null);
        if (item != null) {
            setGraphic(null);
            if (item instanceof LatestVersionSearchResult latestVersionSearchResult) {
                TextFlow textFlow = newTextFlow();

                String matchedText = "";
                if (latestVersionSearchResult.highlightedString() != null) {
                    matchedText = latestVersionSearchResult.highlightedString();
                }
                String startTokenToMatch = "<B>";
                String endTokenToMatch = "</B>";

                int startMatchIndex = matchedText.indexOf(startTokenToMatch);
                while (startMatchIndex != -1) {
                    if (startMatchIndex != 0) {
                        String noHighlightText = matchedText.substring(0, startMatchIndex);
                        Text t = new Text(noHighlightText);
                        t.getStyleClass().add(SEARCH_NOT_MATCHED.toString());
                        textFlow.getChildren().add(t);
                    }
                    int endMatchIndex = matchedText.indexOf(endTokenToMatch);
                    if (endMatchIndex == -1) {
                        // Malformed. Highlight to the end...
                        String highlightText = matchedText.substring(startMatchIndex + startTokenToMatch.length());
                        Text t = new Text(highlightText);
                        t.getStyleClass().add(SEARCH_MATCH.toString());
                        textFlow.getChildren().add(t);
                        matchedText = "";
                        startMatchIndex = -1;
                    } else {
                        String highlightText = matchedText.substring(startMatchIndex + startTokenToMatch.length(), endMatchIndex);
                        Text t = new Text(highlightText);
                        t.getStyleClass().add(SEARCH_MATCH.toString());
                        textFlow.getChildren().add(t);
                        matchedText = matchedText.substring(endMatchIndex + endTokenToMatch.length());
                        startMatchIndex = matchedText.indexOf(startTokenToMatch);
                    }
                }
                if (!matchedText.isBlank()) {
                    Text t = new Text(matchedText);
                    t.getStyleClass().add(SEARCH_NOT_MATCHED.toString());
                    textFlow.getChildren().add(t);
                }
                HBox hBox = new HBox(textFlow);
                setGraphic(hBox);
                latestVersionSearchResult.latestVersion().ifPresent(semanticEntityVersion -> {
                    pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !semanticEntityVersion.active());
                });

            } else if (item instanceof String itemString) {
                setTextFlow(itemString);
            } else if (item instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                setTextFlow(nidTextRecord.text(), SEARCH_TOP_COMPONENT);
                pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !nidTextRecord.active());
            } else {
                setTextFlow(item.toString());
                pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, false);
            }
        }
    }

    TextFlow newTextFlow() {
        TextFlow textFlow = new TextFlow();
        textFlow.setMinHeight(USE_COMPUTED_SIZE);
        textFlow.setPrefHeight(USE_COMPUTED_SIZE);
        textFlow.setMaxHeight(USE_COMPUTED_SIZE);
        if (width != -1) {
            textFlow.setMaxWidth(width - insetWidth());
        } else {
            textFlow.setMaxWidth(this.getTreeView().getWidth() - insetWidth());
        }
        return textFlow;
    }

    void setTextFlow(String text, StyleClasses styleClass) {
        TextFlow textFlow = newTextFlow();
        Text t = new Text(text);
        if (styleClass != null) {
            t.getStyleClass().add(styleClass.toString());
        }
        textFlow.getChildren().add(t);
        HBox hBox = new HBox(textFlow);
        setGraphic(hBox);
    }

    void setTextFlow(String text) {
        TextFlow textFlow = newTextFlow();
        Text t = new Text(text);
        textFlow.getChildren().add(t);
        HBox hBox = new HBox(textFlow);
        setGraphic(hBox);
    }
}
