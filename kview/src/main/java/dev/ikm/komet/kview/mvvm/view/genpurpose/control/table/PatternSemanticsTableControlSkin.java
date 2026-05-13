package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticComponentCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticComponentCollectionCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticIdenticonCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell.SemanticStandardCell;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.util.Callback;
import javafx.util.Subscription;

import java.util.List;

import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_LIST_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_SET_FIELD;

public class PatternSemanticsTableControlSkin extends SkinBase<PatternSemanticsTableControl> {
    public static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");
    public static final PseudoClass PREVIEW_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("preview-mode");

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

        tableView.setRowFactory(createRowFactory());

        tableView.setItems(control.getSemantics());

        control.getSemantics().addListener(this::onSemanticsChanged);
        if (!tableViewInitialized) {
            if (!control.getSemantics().isEmpty()) {
                initializeTableView(control.getSemantics().getFirst());
            }
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
        TableColumn<SemanticRow, Image> identiconColumn = new TableColumn<>();
        identiconColumn.setCellValueFactory(cellData ->
                cellData.getValue().identiconProperty());
        identiconColumn.setCellFactory(_ -> new SemanticIdenticonCell());

        final int identiconColumnWidth = 40;
        identiconColumn.setPrefWidth(identiconColumnWidth);
        identiconColumn.setMinWidth(identiconColumnWidth);
        identiconColumn.setMaxWidth(identiconColumnWidth);
        identiconColumn.getStyleClass().add("identicon-column");

        tableView.getColumns().add(identiconColumn);

        // Fields
        for (SemanticField field : row.getFields()) {
            TableColumn<SemanticRow, Object> tableColumn = new TableColumn<>(field.getFieldTitle());

            tableColumn.setCellValueFactory(cellData ->
                    cellData.getValue().getFields().get(row.getFields().indexOf(field)).observableFieldProperty());

            tableColumn.setCellFactory(tColumn -> {
                if (field.getDataType() == COMPONENT_ID_SET_FIELD.nid() || field.getDataType() == COMPONENT_ID_LIST_FIELD.nid()) {
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