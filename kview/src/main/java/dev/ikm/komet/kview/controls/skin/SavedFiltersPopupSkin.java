package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.SavedFiltersPopup;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SavedFiltersPopupSkin implements Skin<SavedFiltersPopup> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final String STYLE_SHEETS = FilterOptionsPopup.class.getResource("filter-options-popup.css").toExternalForm();

    private final SavedFiltersPopup control;
    private final VBox root;

    public SavedFiltersPopupSkin(SavedFiltersPopup control, Consumer<String> onAccept, Consumer<String> onRemove) {
        this.control = control;

        Label title = new Label(resources.getString("saved.filters.title"));
        title.getStyleClass().add("filter-title");

        StackPane separatorRegion = new StackPane(new IconRegion("line"));
        separatorRegion.getStyleClass().add("separator-region");

        ListView<String> listView = new ListView<>();
        listView.setCellFactory(_ -> new ListCell<>() {

            private String item;
            {
                setOnContextMenuRequested(_ -> {
                    DeleteContextMenu contextMenu = new DeleteContextMenu(() -> {
                        if (onRemove != null && item != null) {
                            onRemove.accept(item);
                            control.hide();
                        }
                    });
                    Bounds bounds = localToScreen(getLayoutBounds());
                    contextMenu.show(control.getScene().getWindow(), bounds.getMaxX(), bounds.getMinY());
                });
                setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        if (onAccept != null && item != null) {
                            onAccept.accept(item);
                        }
                        control.hide();
                    }
                });
            }
            @Override
            protected void updateItem(String string, boolean b) {
                super.updateItem(string, b);
                item = string;
                if (string != null && !string.isEmpty()) {
                    setText(MessageFormat.format(resources.getString("saved.filter.name"), string));
                } else {
                    setText(null);
                }
            }
        });
        listView.getStyleClass().add("saved-filters-list-view");
        listView.setItems(control.getSavedFiltersList());
        listView.setPlaceholder(new Label(resources.getString("saved.filters.empty")));

        root = new VBox(title, separatorRegion, listView);
        root.getStyleClass().add("saved-filters-popup");
        root.getStylesheets().add(STYLE_SHEETS);
    }

    @Override
    public SavedFiltersPopup getSkinnable() {
        return control;
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public void dispose() {

    }

    private static class DeleteContextMenu extends ContextMenu {

        public DeleteContextMenu(Runnable runnable) {
            setAutoHide(true);

            MenuItem menuItem = new MenuItem(resources.getString("saved.filters.delete"), new IconRegion("icon", "delete"));
            menuItem.setOnAction(_ -> {
                if (runnable != null) {
                    runnable.run();
                }
            });
            getItems().addAll(menuItem);
        }

        @Override
        public void show(Window ownerWindow, double anchorX, double anchorY) {
            super.show(ownerWindow, anchorX, anchorY);
            if (!getScene().getStylesheets().contains(STYLE_SHEETS)) {
                getScene().getStylesheets().add(STYLE_SHEETS);
            }
        }
    }
}
