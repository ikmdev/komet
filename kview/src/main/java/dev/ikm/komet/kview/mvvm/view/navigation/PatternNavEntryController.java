package dev.ikm.komet.kview.mvvm.view.navigation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;

public class PatternNavEntryController {

    @FXML
    private HBox patternEntryHBox;

    @FXML
    private ImageView identicon;

    @FXML
    private Text patternName;

    @FXML
    private Button showContextButton;

    @FXML
    private ContextMenu contextMenu;


    @FXML
    private TitledPane instancesTitledPane;

    @FXML
    private ListView patternInstancesListView;

    @FXML
    private void initialize() {
        instancesTitledPane.setExpanded(false);
        showContextButton.setVisible(false);
        contextMenu.setHideOnEscape(true);
        patternInstancesListView.setSelectionModel(new NoSelectionModel());
        patternEntryHBox.setOnMouseEntered(mouseEvent -> showContextButton.setVisible(true));
        patternEntryHBox.setOnMouseExited(mouseEvent -> {
            if (!contextMenu.isShowing()) {
                showContextButton.setVisible(false);
            }
        });
        showContextButton.setOnAction(event -> contextMenu.show(showContextButton, Side.BOTTOM, 0, 0));
    }

    private class NoSelectionModel<T> extends MultipleSelectionModel<T> {

        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList getSelectedItems() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public void selectIndices(int i, int... ints) {

        }

        @Override
        public void selectAll() {

        }

        @Override
        public void clearAndSelect(int i) {

        }

        @Override
        public void select(int i) {

        }

        @Override
        public void select(Object o) {

        }

        @Override
        public void clearSelection(int i) {

        }

        @Override
        public void clearSelection() {

        }

        @Override
        public boolean isSelected(int i) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void selectPrevious() {

        }

        @Override
        public void selectNext() {

        }

        @Override
        public void selectFirst() {

        }

        @Override
        public void selectLast() {

        }
    }

    public void setPatternName(String patternName) {
        this.patternName.setText(patternName);
    }

    public ListView getPatternInstancesListView() {
        return this.patternInstancesListView;
    }

    public void disableInstancesListView() {
        instancesTitledPane.setVisible(false);
        instancesTitledPane.setManaged(false);
    }

}
