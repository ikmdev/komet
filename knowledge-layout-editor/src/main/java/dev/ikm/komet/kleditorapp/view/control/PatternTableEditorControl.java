package dev.ikm.komet.kleditorapp.view.control;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;

import java.util.List;

/**
 * Editor-side representation of a pattern displayed as a table — the design-time counterpart of the
 * journal control produced by {@code KlPatternSemanticsTableFactory.createJournalControl} (a
 * {@code PatternSemanticsTableControl}). Its body is an actual {@link TableView} whose columns are the
 * pattern's fields, so it reads like the journal-side table (each semantic becomes a row).
 * <p>
 * Unlike {@link PatternStandardEditorControl} it holds no {@link FieldViewControl}s: a table renders its
 * fields as columns, each a {@link FieldColumnControl} (see {@link #getFields()}). The table's columns
 * follow that list, and dragging a column header to reorder the columns reorders the list.
 */
public class PatternTableEditorControl extends PatternEditorControlBase {
    public static final String DEFAULT_STYLE_CLASS = "pattern-table-view";

    private final TableView<Object> tableView = new TableView<>();

    PatternTableEditorControl() {
        tableView.getStyleClass().add("pattern-table");

        fields.addListener(this::onFieldsChanged);
        tableView.getColumns().subscribe(this::onColumnsChanged);

        setContent(tableView);

        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    /**
     * The field whose column header the given node — typically a mouse event's target — is part of, or
     * {@code null} when the node isn't within a column header (a cell, the table placeholder, the title...).
     */
    public FieldColumnControl fieldAt(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current instanceof TableColumnHeader header && header.getTableColumn() instanceof FieldColumnControl field) {
                return field;
            }
        }
        return null;
    }

    private void onFieldsChanged(ListChangeListener.Change<? extends FieldColumnControl> change) {
        // A reorder coming from the columns themselves (see onColumnsChanged) is already reflected.
        if (fields.equals(tableView.getColumns())) {
            return;
        }
        while (change.next()) {
            if (change.wasRemoved()) {
                tableView.getColumns().removeAll(change.getRemoved());
            }
            if (change.wasAdded()) {
                tableView.getColumns().addAll(change.getFrom(), change.getAddedSubList());
            }
        }
    }

    /**
     * Follows the user dragging a column header: once the columns are the fields in a new order, reorders
     * the fields to match. Intermediate states while the columns are being brought in step with the fields
     * (see {@link #onFieldsChanged}) are not reorders and are left alone.
     */
    private void onColumnsChanged() {
        List<FieldColumnControl> columnOrder = tableView.getColumns().stream()
                .map(FieldColumnControl.class::cast)
                .toList();
        boolean sameFields = columnOrder.size() == fields.size() && fields.containsAll(columnOrder);
        if (sameFields && !columnOrder.equals(fields)) {
            fields.setAll(columnOrder);
        }
    }

    // -- fields
    /**
     * The fields shown as this table's columns, in column (display) order.
     */
    private final ObservableList<FieldColumnControl> fields = FXCollections.observableArrayList();
    public ObservableList<FieldColumnControl> getFields() { return fields; }
}
