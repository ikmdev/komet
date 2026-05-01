package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class PatternSemanticsTableControlSkin extends SkinBase<PatternSemanticsTableControl> {

    private final TableView<SemanticRow> tableView = new TableView<>();

    private boolean tableViewInitialized = false;

    /**
     * Constructor for all PatternSemanticsTableControlSkin instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    protected PatternSemanticsTableControlSkin(PatternSemanticsTableControl control) {
        super(control);

        getChildren().add(tableView);

        tableView.setItems(control.getSemantics());

        control.getSemantics().addListener(this::onSemanticsChanged);
        if (!tableViewInitialized) {
            initializeTableView(control.getSemantics().getFirst());
        }
    }

    private void onSemanticsChanged(ListChangeListener.Change<? extends SemanticRow> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                onSemanticsAdded(change.getAddedSubList());
            }
        }
    }

    private void onSemanticsAdded(List<? extends SemanticRow> addedSubList) {
        if (!tableViewInitialized) {
            initializeTableView(addedSubList.getFirst());
        }
    }

    private void initializeTableView(SemanticRow row) {
        for (SemanticField field : row.getFields()) {
            TableColumn<SemanticRow, Object> tableColumn = new TableColumn<>(field.getFieldTitle());

            tableColumn.setCellValueFactory(cellData ->
                    cellData.getValue().getFields().get(row.getFields().indexOf(field)).observableFieldProperty());
            tableColumn.setCellFactory(tColumn -> new SemanticTableCell(field));

            tableView.getColumns().add(tableColumn);
        }

        tableViewInitialized = true;
    }

    private static class SemanticTableCell extends TableCell<SemanticRow, Object> {
        private final SemanticField field;

        public SemanticTableCell(SemanticField field) {
            this.field = field;
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            setText(item.toString());
            setGraphic(null);
        }
    }
}