package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_COLUMN_INDEX;
import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_COLUMN_SPAN;
import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_ROW_INDEX;

public abstract class EditorGridNodeModel {

    protected void saveGridNodeDetails(KometPreferences preferences) {
        preferences.putInt(KL_GRID_COLUMN_INDEX, getColumnIndex());
        preferences.putInt(KL_GRID_ROW_INDEX, getRowIndex());
        preferences.putInt(KL_GRID_COLUMN_SPAN, getColumnSpan());
    }

    protected void loadGridNodeDetails(KometPreferences preferences) {
        preferences.getInt(KL_GRID_COLUMN_INDEX).ifPresent(this::setColumnIndex);
        preferences.getInt(KL_GRID_ROW_INDEX).ifPresent(this::setRowIndex);
        preferences.getInt(KL_GRID_COLUMN_SPAN).ifPresent(this::setColumnSpan);
    }

    // -- column Index
    private final IntegerProperty columnIndex = new SimpleIntegerProperty();
    public int getColumnIndex() { return columnIndex.get(); }
    public IntegerProperty columnIndexProperty() { return columnIndex; }
    public void setColumnIndex(int index) { columnIndex.set(index); }

    // -- row index
    private final IntegerProperty rowIndex = new SimpleIntegerProperty();
    public int getRowIndex() { return rowIndex.get(); }
    public IntegerProperty rowIndexProperty() { return rowIndex; }
    public void setRowIndex(int index) { rowIndex.set(index); }

    // -- column span
    private final IntegerProperty columnSpan = new SimpleIntegerProperty(1);
    public int getColumnSpan() { return columnSpan.get(); }
    public IntegerProperty columnSpanProperty() { return columnSpan; }
    public void setColumnSpan(int index) { columnSpan.set(index); }
}