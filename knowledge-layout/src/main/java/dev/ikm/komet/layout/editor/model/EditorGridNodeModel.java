package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import static dev.ikm.komet.preferences.KLEditorPreferences.DataPropertyKey.KL_REQUIRED;
import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_COLUMN_INDEX;
import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_COLUMN_SPAN;
import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_ROW_INDEX;

/**
 * Base class for any Editor model that stores in grid cell specific settings. That is a model that is placed inside a
 * grid.
 */
public abstract class EditorGridNodeModel extends EditorModelBase {

    protected void saveGridNodeDetails(KometPreferences preferences) {
        preferences.putInt(KL_GRID_COLUMN_INDEX, getColumnIndex());
        preferences.putInt(KL_GRID_ROW_INDEX, getRowIndex());
        preferences.putInt(KL_GRID_COLUMN_SPAN, getColumnSpan());
        preferences.putBoolean(KL_REQUIRED, isRequired());
    }

    protected void loadGridNodeDetails(KometPreferences preferences) {
        preferences.getInt(KL_GRID_COLUMN_INDEX).ifPresent(this::setColumnIndex);
        preferences.getInt(KL_GRID_ROW_INDEX).ifPresent(this::setRowIndex);
        preferences.getInt(KL_GRID_COLUMN_SPAN).ifPresent(this::setColumnSpan);
        preferences.getBoolean(KL_REQUIRED).ifPresent(this::setRequired);
    }

    /*******************************************************************************
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     ******************************************************************************/

    // -- parent grid
    private final ObjectProperty<ParentGridModel> parentGrid = new SimpleObjectProperty<>();
    public ParentGridModel getParentGrid() { return parentGrid.get(); }
    public ObjectProperty<ParentGridModel> parentGridProperty() { return parentGrid; }
    void setParentGrid(ParentGridModel parentGrid) { this.parentGrid.set(parentGrid); }

    // -- column Index
    /**
     * Column index for this Node inside the Grid.
     */
    private final IntegerProperty columnIndex = new SimpleIntegerProperty();
    public int getColumnIndex() { return columnIndex.get(); }
    public IntegerProperty columnIndexProperty() { return columnIndex; }
    public void setColumnIndex(int index) { columnIndex.set(index); }

    // -- row index
    /**
     * Row index for this Node inside the Grid.
     */
    private final IntegerProperty rowIndex = new SimpleIntegerProperty();
    public int getRowIndex() { return rowIndex.get(); }
    public IntegerProperty rowIndexProperty() { return rowIndex; }
    public void setRowIndex(int index) { rowIndex.set(index); }

    // -- column span
    /**
     * The number of columns this Node should span inside the Grid.
     */
    private final IntegerProperty columnSpan = new SimpleIntegerProperty(1);
    public int getColumnSpan() { return columnSpan.get(); }
    public IntegerProperty columnSpanProperty() { return columnSpan; }
    public void setColumnSpan(int index) { columnSpan.set(index); }

    // -- row span
    /**
     * The number of rows this Node should span inside the Grid.
     */
    private final IntegerProperty rowSpan = new SimpleIntegerProperty(1);
    public int getRowSpan() { return rowSpan.get(); }
    public IntegerProperty rowSpanProperty() { return rowSpan; }
    public void setRowSpan(int index) { rowSpan.set(index); }

    // -- required
    /**
     * Whether this grid node must be filled out when its window is opened in create mode in the Journal.
     * Applies to anything placed in a Section (patterns, supplemental areas, ...).
     */
    private final BooleanProperty required = new SimpleBooleanProperty(false);
    public boolean isRequired() { return required.get(); }
    public BooleanProperty requiredProperty() { return required; }
    public void setRequired(boolean required) { this.required.set(required); }
}