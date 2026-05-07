package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticComponentCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticComponentCollectionCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticStandardCell;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_SET_FIELD;

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

        tableView.setTableMenuButtonVisible(true);

        tableView.setItems(control.getSemantics());

        control.getSemantics().addListener(this::onSemanticsChanged);
        if (!tableViewInitialized) {
            if (!control.getSemantics().isEmpty()) {
                initializeTableView(control.getSemantics().getFirst());
            }
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

            tableColumn.setCellFactory(tColumn -> {
                if (field.getDataType() == COMPONENT_ID_SET_FIELD.nid()) {
                    return new SemanticComponentCollectionCell(getSkinnable().getViewCalculator());
                } else if (field.getDataType() == COMPONENT_FIELD.nid()) {
                    return new SemanticComponentCell(getSkinnable().getViewCalculator());
                } else {
                    return new SemanticStandardCell();
                }
            });

            tableView.getColumns().add(tableColumn);
        }

        tableViewInitialized = true;
    }
}