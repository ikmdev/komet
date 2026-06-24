/*
 * Copyright © 2024 Integrated Knowledge Management (support@ikm.dev)
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
package dev.ikm.komet.layout.controls.skin;

import dev.ikm.komet.framework.controls.KonceptBadge;
import dev.ikm.komet.framework.dnd.DropHelper;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.controls.KlConceptField;
import dev.ikm.komet.layout.controls.KlConceptField.Completer;
import dev.ikm.komet.layout.controls.KlConceptField.Value;
import dev.ikm.tinkar.common.service.PrimitiveData;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.List;

/**
 * Skin for {@link KlConceptField}: a stable {@link HBox} root holding a swap area (the concept badge
 * or, when empty, a search text field) plus a trailing clear button. The root never leaves the scene
 * graph, so clearing a field keeps the user's place to type. Search results show in a small auto-hiding
 * {@link Popup} (capped, cheap label rows). The badge keeps its own built-in drag source; a click that
 * is not a drag opens the concept's detail.
 */
public class KlConceptFieldSkin extends SkinBase<KlConceptField> {

    /** Hard cap on result rows (keeps the popup cheap and legible). */
    private static final int MAX_RESULTS = 8;
    private static final Duration SEARCH_DEBOUNCE = Duration.millis(180);

    private final HBox root = new HBox(4);
    private final StackPane swap = new StackPane();
    private final TextField search = new TextField();
    private final Button clearButton = new Button("✕");
    private final Popup popup = new Popup();
    private final ListView<Completer.Result> results = new ListView<>();
    private final PauseTransition debounce = new PauseTransition(SEARCH_DEBOUNCE);

    private KonceptBadge badge;

    /**
     * Builds the skin.
     *
     * @param control the field
     */
    public KlConceptFieldSkin(KlConceptField control) {
        super(control);

        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("kl-concept-field-root");
        HBox.setHgrow(swap, Priority.ALWAYS);
        swap.setAlignment(Pos.CENTER_LEFT);

        search.setPromptText("type to search a concept…");
        search.getStyleClass().add("kl-concept-field-search");
        HBox.setHgrow(search, Priority.ALWAYS);

        clearButton.getStyleClass().add("kl-concept-field-clear");
        clearButton.setFocusTraversable(false);
        clearButton.setOnAction(e -> {
            getSkinnable().commitClear();
            showSearch();
            search.requestFocus();
        });

        results.getStyleClass().add("kl-concept-field-results");
        results.setMaxHeight(220);
        results.setCellFactory(view -> new ResultCell());
        results.setOnMouseClicked(e -> commitSelected());
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.getContent().add(results);

        root.getChildren().addAll(swap, clearButton);
        getChildren().add(root);

        // Drop a Komet concept anywhere on the field → set it (host re-grounds the label).
        new DropHelper(root,
                db -> KometClipboard.conceptNid(db).ifPresent(nid -> getSkinnable().commitConcept(nid, "")),
                e -> true,
                () -> false);

        // Search wiring (debounced; the completer runs off-FX and calls back on-FX).
        debounce.setOnFinished(e -> runSearch());
        search.textProperty().addListener((obs, old, text) -> {
            if (text == null || text.isBlank()) {
                popup.hide();
            } else {
                debounce.playFromStart();
            }
        });
        search.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DOWN -> moveSelection(1);
                case UP -> moveSelection(-1);
                case ENTER -> commitSelected();
                case ESCAPE -> popup.hide();
                default -> {
                }
            }
        });

        control.valueProperty().addListener((obs, old, value) -> updateDisplay());
        control.clearableProperty().addListener((obs, old, value) -> updateDisplay());
        control.iconSizeProperty().addListener((obs, old, size) -> {
            if (badge != null) {
                badge.setIconSize(size.doubleValue());
            }
        });
        updateDisplay();
    }

    /** Renders the current value: a concept badge (with the clear button) or the search field. */
    private void updateDisplay() {
        if (getSkinnable().getValue() instanceof Value.Concept concept) {
            showBadge(concept);
        } else {
            showSearch();
        }
    }

    private void showBadge(Value.Concept concept) {
        ViewProperties view = getSkinnable().viewPropertiesProperty().get();
        badge = (view != null)
                ? new KonceptBadge(concept.nid(), view)
                : new KonceptBadge(PrimitiveData.publicId(concept.nid()), concept.label());
        badge.setIconSize(getSkinnable().iconSizeProperty().get());
        badge.getStyleClass().add("kl-concept-field-badge");
        // A click that is not a drag opens detail (JavaFX delivers no MOUSE_CLICKED after a drag
        // gesture, so this never fires for a drag-out — the badge keeps its own built-in drag).
        badge.setOnMouseClicked(e -> getSkinnable().requestDetail());
        swap.getChildren().setAll(badge);
        boolean clearable = getSkinnable().isClearable();
        clearButton.setVisible(clearable);
        clearButton.setManaged(clearable);
        popup.hide();
    }

    private void showSearch() {
        badge = null;
        swap.getChildren().setAll(search);
        search.clear();
        clearButton.setVisible(false);
        clearButton.setManaged(false);
    }

    private void runSearch() {
        Completer completer = getSkinnable().completerProperty().get();
        String query = search.getText();
        if (completer == null || query == null || query.isBlank()) {
            popup.hide();
            return;
        }
        completer.complete(query.trim(), MAX_RESULTS, rows -> showResults(rows == null ? List.of() : rows));
    }

    private void showResults(List<Completer.Result> rows) {
        if (rows.isEmpty() || !search.isFocused()) {
            popup.hide();
            return;
        }
        results.getItems().setAll(rows);
        results.getSelectionModel().selectFirst();
        if (search.getScene() != null && search.getScene().getWindow() != null && !popup.isShowing()) {
            javafx.geometry.Point2D below = search.localToScreen(0, search.getHeight());
            results.setPrefWidth(Math.max(search.getWidth(), 240));
            popup.show(search, below.getX(), below.getY());
        }
    }

    private void moveSelection(int delta) {
        if (!popup.isShowing() || results.getItems().isEmpty()) {
            return;
        }
        int next = results.getSelectionModel().getSelectedIndex() + delta;
        if (next >= 0 && next < results.getItems().size()) {
            results.getSelectionModel().select(next);
        }
    }

    private void commitSelected() {
        Completer.Result selected = results.getSelectionModel().getSelectedItem();
        if (selected != null) {
            popup.hide();
            getSkinnable().commitConcept(selected.nid(), selected.label());
        }
    }

    /** A cheap result row — label text only, so repopulating the list never builds badges or hits the store. */
    private static final class ResultCell extends ListCell<Completer.Result> {
        @Override
        protected void updateItem(Completer.Result item, boolean empty) {
            super.updateItem(item, empty);
            setText((empty || item == null) ? null : item.label());
        }
    }

    @Override
    public void dispose() {
        debounce.stop();
        popup.hide();
        super.dispose();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        return root.prefHeight(width) + topInset + bottomInset;
    }
}
