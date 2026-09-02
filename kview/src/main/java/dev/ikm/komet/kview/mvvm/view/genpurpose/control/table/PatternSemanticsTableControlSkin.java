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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.TableColumnHeader;
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
    private static final String TABLE_ROW_CELL_STYLE_CLASS = ".table-row-cell";
    private static final String SCROLL_BAR_STYLE_CLASS = ".scroll-bar";

    /** Number of empty filler rows kept visible below the last semantic row. */
    private static final int TRAILING_EMPTY_ROW_COUNT = 2;

    /** Row-height stand-in until a first row has been laid out; self-corrects from then on. */
    private static final double INITIAL_ROW_HEIGHT_ESTIMATE = 44;

    /**
     * Ceiling on the content-derived table height. Beyond it the table scrolls internally instead
     * of growing — a table sized to hundreds of semantics would realize a row node for every one
     * of them, forfeiting the virtualization.
     */
    private static final double MAX_TABLE_HEIGHT = 600;

    private static final String COLUMN_HEADER_STYLE_CLASS = ".column-header";
    private static final String FIELD_PURPOSE_KEY = "fieldPurpose";
    private static final String TOOLTIP_INSTALLED_KEY = "fieldPurposeTooltipInstalled";

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

        // Header decorations (tooltips, menu-only titles) live on the TableColumnHeader nodes,
        // which only exist once the table's skin has built and laid out. Re-run when the skin
        // appears and whenever columns are (re)added.
        tableView.skinProperty().subscribe(skin -> {
            if (skin != null) {
                installHeaderTooltips();
            }
        });
        tableView.getColumns().addListener((ListChangeListener<TableColumn<SemanticRow, ?>>)
                _ -> installHeaderTooltips());

        // Honor the factory-configured properties.
        control.headerVisibleProperty().subscribe(this::updateHeaderVisibility);
        control.gridLinesVisibleProperty().subscribe(this::updateGridLinesVisibility);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        updateTableHeightToContent();
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        // A taller parent must not stretch the table: any extra height would fill up with
        // additional empty filler rows beyond the TRAILING_EMPTY_ROW_COUNT sized for.
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /**
     * Sizes the table to its content — every semantic row plus {@link #TRAILING_EMPTY_ROW_COUNT}
     * empty filler rows — instead of the fixed pref height a TableView defaults to, which fills
     * whatever is left over with empty rows. Rows are measured from what the previous layout pass
     * realized (their heights vary: a component-collection cell stacks its components), with the
     * average standing in for rows not realized yet, so the height set here converges within a
     * pulse or two of rows appearing or changing. Growth stops at {@link #MAX_TABLE_HEIGHT}; past
     * it the table scrolls its rows internally.
     */
    private void updateTableHeightToContent() {
        int itemCount = tableView.getItems().size();

        double dataRowsHeight = 0;
        int measuredDataRows = 0;
        double emptyRowHeight = 0;
        for (Node node : tableView.lookupAll(TABLE_ROW_CELL_STYLE_CLASS)) {
            if (node instanceof TableRow<?> row && row.isVisible() && row.getIndex() >= 0 && row.getHeight() > 0) {
                if (row.getIndex() < itemCount) {
                    dataRowsHeight += row.getHeight();
                    measuredDataRows++;
                } else {
                    emptyRowHeight = row.getHeight();
                }
            }
        }

        double estimatedRowHeight = measuredDataRows > 0
                ? dataRowsHeight / measuredDataRows : INITIAL_ROW_HEIGHT_ESTIMATE;
        dataRowsHeight += (itemCount - measuredDataRows) * estimatedRowHeight;
        if (emptyRowHeight == 0) {
            emptyRowHeight = estimatedRowHeight;
        }

        double desiredHeight = snapSizeY(Math.min(MAX_TABLE_HEIGHT,
                tableView.getInsets().getTop() + tableView.getInsets().getBottom()
                        + getHeaderHeight() + dataRowsHeight + TRAILING_EMPTY_ROW_COUNT * emptyRowHeight
                        + getHorizontalScrollBarHeight()));
        if (Math.abs(tableView.getPrefHeight() - desiredHeight) > 1) {
            tableView.setPrefHeight(desiredHeight);
        }
    }

    private double getHeaderHeight() {
        Region header = (Region) tableView.lookup(TABLE_HEADER_ROW);
        return (header == null || !header.isVisible()) ? 0 : header.getHeight();
    }

    private double getHorizontalScrollBarHeight() {
        for (Node node : tableView.lookupAll(SCROLL_BAR_STYLE_CLASS)) {
            if (node instanceof ScrollBar scrollBar
                    && scrollBar.getOrientation() == Orientation.HORIZONTAL && scrollBar.isVisible()) {
                return scrollBar.getHeight();
            }
        }
        return 0;
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
        // Named "Semantic icon" so the show/hide-columns menu lists it; the header cell itself
        // stays icon-only via CSS (.column-header.identicon-column .label is graphic-only).
        TableColumn<SemanticRow, Integer> identiconColumn = new TableColumn<>("Semantic icon");
        identiconColumn.setCellValueFactory(cellData ->
                cellData.getValue().semanticNidProperty());
        identiconColumn.setCellFactory(_ -> new SemanticIdenticonCell(getSkinnable().getNidToComponentItem()));

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

        // Column header. Use the column's own text rather than a graphic Label so the
        // built-in table-menu-button (show/hide columns) lists each column by name: its
        // CheckMenuItems bind to TableColumn.textProperty(), which a graphic-only header
        // leaves blank. The purpose is stashed so the tooltip can be installed on the
        // header node once it exists (see installHeaderTooltips()).
        tableColumn.setText(field.fieldTitle());
        tableColumn.getProperties().put(FIELD_PURPOSE_KEY, field.fieldPurpose());

        return tableColumn;
    }

    /**
     * Installs a tooltip describing each field's purpose on its column header. The built-in
     * column header has no tooltip API, so the tooltip is attached to the {@link TableColumnHeader}
     * node directly. Idempotent: a header is only decorated once.
     */
    private void installHeaderTooltips() {
        for (Node node : tableView.lookupAll(COLUMN_HEADER_STYLE_CLASS)) {
            if (node instanceof TableColumnHeader header) {
                TableColumnBase<?, ?> column = header.getTableColumn();
                if (column == null) {
                    continue;
                }
                Object purpose = column.getProperties().get(FIELD_PURPOSE_KEY);
                if (purpose != null
                        && header.getProperties().putIfAbsent(TOOLTIP_INSTALLED_KEY, Boolean.TRUE) == null) {
                    Tooltip.install(header, new Tooltip(purpose.toString()));
                }
            }
        }
    }
}