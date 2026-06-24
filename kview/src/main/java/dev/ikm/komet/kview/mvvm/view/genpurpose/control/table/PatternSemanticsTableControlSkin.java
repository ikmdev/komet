package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticComponentCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticComponentCollectionCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticIdenticonCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticStandardCell;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.Subscription;

import java.util.List;

import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_LIST_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_SET_FIELD;

public class PatternSemanticsTableControlSkin extends SkinBase<PatternSemanticsTableControl> {
    public static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");
    public static final PseudoClass PREVIEW_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("preview-mode");

    private static final String TABLE_HEADER_ROW = "TableHeaderRow";
    private static final String SHOW_GRID_LINES_STYLE_CLASS = "show-grid-lines";

    private final TableView<SemanticRow> tableView = new TableView<>();

    private boolean tableViewInitialized = false;

    /**
     * Constructor for all PatternSemanticsTableControlSkin instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public PatternSemanticsTableControlSkin(PatternSemanticsTableControl control) {
        super(control);

        getChildren().add(tableView);

        tableView.setTableMenuButtonVisible(true);

        tableView.setSelectionModel(null);

        tableView.setRowFactory(createRowFactory());

        tableView.setItems(control.getSemantics());

        control.getSemantics().addListener(this::onSemanticsChanged);
        if (!tableViewInitialized) {
            if (!control.getSemantics().isEmpty()) {
                initializeTableView(control.getSemantics().getFirst());
            }
        }

        // Honor the factory-configured properties.
        control.headerVisibleProperty().subscribe(this::updateHeaderVisibility);
        control.gridLinesVisibleProperty().subscribe(this::updateGridLinesVisibility);
    }

    private void updateHeaderVisibility(boolean visible) {
        // The header row only exists once the TableView's own skin has been built, so re-apply both
        // when the property changes and when the header node first appears.
        Region header = (Region) tableView.lookup(TABLE_HEADER_ROW);
        if (header == null) {
            tableView.skinProperty().subscribe(skin -> {
                if (skin != null) {
                    updateHeaderVisibility(getSkinnable().isHeaderVisible());
                }
            });
            return;
        }
        header.setVisible(visible);
        header.setManaged(visible);
        double height = visible ? Region.USE_COMPUTED_SIZE : 0;
        header.setMinHeight(height);
        header.setPrefHeight(height);
        header.setMaxHeight(height);
    }

    private void updateGridLinesVisibility(boolean visible) {
        tableView.getStyleClass().remove(SHOW_GRID_LINES_STYLE_CLASS);
        if (visible) {
            tableView.getStyleClass().add(SHOW_GRID_LINES_STYLE_CLASS);
        }
    }

    private static Callback<TableView<SemanticRow>, TableRow<SemanticRow>> createRowFactory() {
        return new Callback<>() {
            private Subscription lastSubscription;

            @Override
            public TableRow<SemanticRow> call(TableView<SemanticRow> semanticRowTableView) {
                TableRow<SemanticRow> row = new TableRow<>();

                row.itemProperty().subscribe((oldItem, newItem) -> {
                    if (oldItem != null) {
                        if (lastSubscription != null) {
                            lastSubscription.unsubscribe();
                        }
                    }

                    if (newItem != null) {
                        lastSubscription = newItem.editModeProperty().subscribe(isEditMode -> {
                            row.pseudoClassStateChanged(EDIT_MODE_PSEUDO_CLASS, isEditMode);
                        });
                        lastSubscription = Subscription.combine(lastSubscription,
                                newItem.previewModeProperty().subscribe(isPreviewMode -> {
                                    row.pseudoClassStateChanged(PREVIEW_MODE_PSEUDO_CLASS, isPreviewMode);
                                })
                        );
                    } else {
                        row.pseudoClassStateChanged(EDIT_MODE_PSEUDO_CLASS, false);
                        row.pseudoClassStateChanged(PREVIEW_MODE_PSEUDO_CLASS, false);
                    }
                });

                return row;
            }
        };
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
        // Identicon
        var identiconColumn = createSemanticIdenticonColumn();

        tableView.getColumns().add(identiconColumn);

        // Fields
        for (SemanticField field : row.getFields()) {
            final TableColumn<SemanticRow, ?> tableColumn = createSemanticFieldColumn(row, field);
            tableView.getColumns().add(tableColumn);
        }

        tableViewInitialized = true;
    }

    private TableColumn<SemanticRow, Integer> createSemanticIdenticonColumn() {
        TableColumn<SemanticRow, Integer> identiconColumn = new TableColumn<>();
        identiconColumn.setCellValueFactory(cellData ->
                cellData.getValue().semanticNidProperty());
        identiconColumn.setCellFactory(_ -> new SemanticIdenticonCell(getSkinnable().getNidToComponentItem()));

        final int identiconColumnWidth = 40;
        identiconColumn.setPrefWidth(identiconColumnWidth);
        identiconColumn.setMinWidth(identiconColumnWidth);
        identiconColumn.setMaxWidth(identiconColumnWidth);
        identiconColumn.getStyleClass().add("identicon-column");
        return identiconColumn;
    }

    private TableColumn<SemanticRow, ?> createSemanticFieldColumn(SemanticRow row, SemanticField field) {
        final TableColumn<SemanticRow, ?> tableColumn;

        if (field.dataType() == COMPONENT_ID_SET_FIELD.nid() || field.dataType() == COMPONENT_ID_LIST_FIELD.nid()) {
            TableColumn<SemanticRow, IntIdCollection> col = new TableColumn<>();
            col.setCellValueFactory(cellData ->
                    (ObservableValue<IntIdCollection>) cellData.getValue().getFields().get(row.getFields().indexOf(field)).observableFieldProperty());
            col.setCellFactory(_ -> new SemanticComponentCollectionCell(getSkinnable().getNidToComponentItem()));
            tableColumn = col;
        } else if (field.dataType() == COMPONENT_FIELD.nid()) {
            TableColumn<SemanticRow, EntityProxy> col = new TableColumn<>();
            col.setCellValueFactory(cellData ->
                    (ObservableValue<EntityProxy>) cellData.getValue().getFields().get(row.getFields().indexOf(field)).observableFieldProperty());
            col.setCellFactory(_ -> new SemanticComponentCell(getSkinnable().getEntityProxyToComponentItem()));
            tableColumn = col;
        } else {
            TableColumn<SemanticRow, Object> col = new TableColumn<>();
            col.setCellValueFactory(cellData ->
                    cellData.getValue().getFields().get(row.getFields().indexOf(field)).observableFieldProperty());
            col.setCellFactory(_ -> new SemanticStandardCell());
            tableColumn = col;
        }

        // Column header
        Label headerLabel = new Label(field.fieldTitle());
        headerLabel.setTooltip(new Tooltip(field.fieldPurpose()));
        tableColumn.setGraphic(headerLabel);

        return tableColumn;
    }
}