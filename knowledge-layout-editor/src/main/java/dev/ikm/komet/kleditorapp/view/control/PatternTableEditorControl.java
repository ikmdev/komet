package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Editor-side representation of a pattern displayed as a table — the design-time counterpart of the
 * journal control produced by {@code KlPatternSemanticsTableFactory.createJournalControl} (a
 * {@code PatternSemanticsTableControl}). Its body is an actual {@link TableView} whose columns are the
 * pattern's fields, so it reads like the journal-side table (each semantic becomes a row).
 * <p>
 * Unlike {@link PatternStandardEditorControl} it holds no {@link FieldViewControl}s: a table renders its fields as
 * column headers, not as editable field tiles.
 */
public class PatternTableEditorControl extends PatternEditorControlBase {
    public static final String DEFAULT_STYLE_CLASS = "pattern-table-view";

    private final TableView<Object> tableView = new TableView<>();

    PatternTableEditorControl() {
        tableView.getStyleClass().add("pattern-table");

        setContent(tableView);

        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    /**
     * Adds a column to the table whose header tracks the given field title, and returns it so the
     * caller can associate it with what it stands for (via {@link TableColumn#setUserData(Object)}).
     */
    TableColumn<Object, Object> addColumn(ObservableValue<String> titleBinding) {
        TableColumn<Object, Object> column = new TableColumn<>();
        column.textProperty().bind(titleBinding);
        tableView.getColumns().add(column);
        return column;
    }

    /**
     * The table's columns, in display order. Reordered when the user drags a column header, so a
     * caller can listen to it to follow the order of the fields the columns stand for.
     */
    ObservableList<TableColumn<Object, ?>> getColumns() {
        return tableView.getColumns();
    }
}
