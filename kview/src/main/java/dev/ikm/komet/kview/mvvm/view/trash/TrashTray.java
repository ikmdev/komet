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
package dev.ikm.komet.kview.mvvm.view.trash;

import dev.ikm.komet.layout.trash.KlTrash;
import dev.ikm.komet.layout.trash.KlTrashItem;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The journal's Trash tray (KEC 2026-10-23): everything in Komet's Trash, from every
 * {@link dev.ikm.komet.layout.trash.KlTrashProvider}, with Put Back, Delete and Empty Trash, and
 * the preference to empty it when Komet quits. It is drawn like the unpublished-changes tray
 * beside it, and its count rides the sidebar's Trash icon.
 */
public final class TrashTray {

    /** The id of the journal sidebar's Trash toggle, which a card selects to open this tray. */
    public static final String TOGGLE_ID = "trashToggleButton";

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final VBox root = new VBox();
    private final ObservableList<KlTrashItem> items = FXCollections.observableArrayList();
    private final ListView<KlTrashItem> list = new ListView<>(items);
    private final Label countLabel = new Label("0");
    private final Label status = new Label();
    private final CheckBox emptyOnQuit = new CheckBox("Empty the Trash when Komet quits");
    private final ReadOnlyIntegerWrapper count = new ReadOnlyIntegerWrapper();
    private final Runnable changed = () -> Platform.runLater(this::refresh);

    /** Builds the tray and starts following the Trash. */
    public TrashTray() {
        root.getStyleClass().addAll("unpublished-changes-container", "trash-container");
        root.setPrefWidth(372);
        root.setId("trashTray");

        Label title = new Label("Trash");
        title.getStyleClass().add("title-label");
        countLabel.getStyleClass().add("count-pill");
        HBox titleRow = new HBox(title, countLabel);
        titleRow.getStyleClass().add("title-row");
        Label subtitle = new Label("What was moved to the Trash, until it is put back or deleted for good");
        subtitle.getStyleClass().add("subtitle-label");
        subtitle.setWrapText(true);
        VBox header = new VBox(titleRow, subtitle);
        header.getStyleClass().add("header");

        list.setId("trashList");
        list.getStyleClass().add("trash-list");
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.setCellFactory(view -> new ItemCell());
        Region emptyIcon = new Region();
        emptyIcon.getStyleClass().addAll("empty-state-icon", "trash-empty-icon");
        Label emptyTitle = new Label("The Trash is empty");
        emptyTitle.getStyleClass().add("empty-title");
        Label emptyText = new Label("What you move to the Trash from a card waits here until it is put back or the Trash is emptied.");
        emptyText.getStyleClass().add("empty-text");
        emptyText.setWrapText(true);
        VBox emptyState = new VBox(emptyIcon, emptyTitle, emptyText);
        emptyState.getStyleClass().add("empty-state");
        emptyState.visibleProperty().bind(Bindings.isEmpty(items));
        list.visibleProperty().bind(Bindings.isNotEmpty(items));
        StackPane area = new StackPane(list, emptyState);
        area.getStyleClass().add("changes-area");
        VBox.setVgrow(area, Priority.ALWAYS);

        Button putBack = button("Put Back", "trashPutBack", "Return the selected items to where they were");
        Button delete = button("Delete…", "trashDelete", "Delete the selected items for good");
        Button empty = button("Empty Trash…", "trashEmpty", "Delete everything in the Trash for good");
        putBack.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        delete.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        empty.disableProperty().bind(Bindings.isEmpty(items));
        putBack.setOnAction(e -> {
            List<KlTrashItem> selected = List.copyOf(list.getSelectionModel().getSelectedItems());
            KlTrash.putBack(selected);
            status.setText("Put back " + count(selected.size()) + ".");
            refresh();
        });
        delete.setOnAction(e -> {
            List<KlTrashItem> selected = List.copyOf(list.getSelectionModel().getSelectedItems());
            if (confirm("Delete " + count(selected.size()) + " for good?")) {
                report(KlTrash.delete(selected));
            }
        });
        empty.setOnAction(e -> {
            if (confirm("Empty the Trash? The " + count(items.size()) + " in it will be deleted for good.")) {
                report(KlTrash.empty());
            }
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(putBack, delete, spacer, empty);
        buttons.getStyleClass().add("footer-buttons");

        emptyOnQuit.setId("trashEmptyOnQuit");
        emptyOnQuit.getStyleClass().add("trash-empty-on-quit");
        emptyOnQuit.selectedProperty().addListener((obs, was, is) -> {
            if (is != KlTrash.emptyOnQuit()) {
                KlTrash.setEmptyOnQuit(is);
            }
        });
        status.getStyleClass().add("subtitle-label");
        status.setWrapText(true);
        status.setMinHeight(Region.USE_PREF_SIZE);
        VBox footer = new VBox(buttons, emptyOnQuit, status);
        footer.getStyleClass().add("footer");

        root.getChildren().addAll(header, area, footer);
        URL css = TrashTray.class.getResource("/dev/ikm/komet/kview/mvvm/view/kview.css");
        if (css != null) {
            root.getStylesheets().add(css.toExternalForm());
        }
        KlTrash.onChange(changed);
        refresh();
    }

    /** @return the tray's node */
    public VBox fxObject() {
        return root;
    }

    /** @return how many items are in the Trash, for the sidebar's badge */
    public ReadOnlyIntegerProperty countProperty() {
        return count.getReadOnlyProperty();
    }

    /** Reads the Trash again. */
    public void refresh() {
        List<KlTrashItem> now = KlTrash.items();
        items.setAll(now);
        count.set(now.size());
        countLabel.setText(Integer.toString(now.size()));
        emptyOnQuit.setSelected(KlTrash.emptyOnQuit());
    }

    /** Stops following the Trash. */
    public void shutdown() {
        KlTrash.stopListening(changed);
    }

    /**
     * Opens the Trash from a card: selects the journal sidebar's Trash toggle when the card is in
     * a journal, so the tray slides out there, or shows the tray in a window of its own.
     *
     * @param from a node of the card
     */
    public static void open(Node from) {
        Scene scene = from == null ? null : from.getScene();
        if (scene != null && scene.lookup("#" + TOGGLE_ID) instanceof ToggleButton toggle) {
            toggle.setSelected(true);
            return;
        }
        TrashTray tray = new TrashTray();
        Stage stage = new Stage();
        stage.setTitle("Trash");
        Window owner = scene == null ? null : scene.getWindow();
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setScene(new Scene(tray.fxObject(), 400, 520));
        stage.setOnHidden(e -> tray.shutdown());
        stage.show();
    }

    private boolean confirm(String question) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "What is deleted cannot be put back. A conversation's journal in the knowledge base is deleted with it.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Delete for good");
        confirm.setHeaderText(question);
        if (root.getScene() != null) {
            confirm.initOwner(root.getScene().getWindow());
        }
        return confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void report(KlTrash.Deleted deleted) {
        refresh();
        if (deleted.problems().isEmpty()) {
            status.setText("Deleted " + count(deleted.deleted()) + ".");
            return;
        }
        String problems = String.join("\n", deleted.problems());
        status.setText("Deleted " + count(deleted.deleted()) + "; " + count(deleted.problems().size())
                + " could not be deleted and stay in the Trash.");
        AlertStreams.getRoot().dispatch(AlertObject.makeWarning("Some items stayed in the Trash", problems));
    }

    private static Button button(String text, String id, String tip) {
        Button button = new Button(text);
        button.setId(id);
        button.getStyleClass().add("discard-all-button");
        button.setTooltip(new Tooltip(tip));
        return button;
    }

    private static String count(int n) {
        return n + (n == 1 ? " item" : " items");
    }

    /** A Trash line: the item's name, and beneath it what it is, where from and when it was trashed. */
    private static final class ItemCell extends ListCell<KlTrashItem> {
        @Override
        protected void updateItem(KlTrashItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            Label name = new Label(item.name());
            name.getStyleClass().add("trash-item-name");
            Label detail = new Label(item.kind() + " · " + item.provider().sourceName()
                    + (item.trashedAt() > 0 ? " · moved " + WHEN.format(Instant.ofEpochMilli(item.trashedAt())) : ""));
            detail.getStyleClass().add("trash-item-detail");
            VBox lines = new VBox(name, detail);
            lines.getStyleClass().add("trash-item");
            setText(null);
            setGraphic(lines);
        }
    }
}
