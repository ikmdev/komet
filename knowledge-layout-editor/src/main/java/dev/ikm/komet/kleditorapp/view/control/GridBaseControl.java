package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.GridPane;

public abstract class GridBaseControl extends EditorWindowBaseControl {

    protected GridBaseControl() {
        // Init GridPane properties with the existing default values
        GridPane.setColumnIndex(this, getColumnIndex());
        GridPane.setRowIndex(this, getRowIndex());
        GridPane.setColumnSpan(this, getColumnSpan());
    }

    // -- column Index
    private final IntegerProperty columnIndex = new SimpleIntegerProperty(0) {
        @Override
        protected void invalidated() {
            GridPane.setColumnIndex(GridBaseControl.this, get());
        }
    };
    public int getColumnIndex() { return columnIndex.get(); }
    public IntegerProperty columnIndexProperty() { return columnIndex; }
    public void setColumnIndex(int index) { columnIndex.set(index); }

    // -- row index
    private final IntegerProperty rowIndex = new SimpleIntegerProperty(0) {
        @Override
        protected void invalidated() {
            GridPane.setRowIndex(GridBaseControl.this, get());
        }
    };
    public int getRowIndex() { return rowIndex.get(); }
    public IntegerProperty rowIndexProperty() { return rowIndex; }
    public void setRowIndex(int index) { rowIndex.set(index); }

    // -- column span
    private final IntegerProperty columnSpan = new SimpleIntegerProperty(1){
        @Override
        protected void invalidated() {
            GridPane.setColumnSpan(GridBaseControl.this, get());
        }
    };
    public int getColumnSpan() { return columnSpan.get(); }
    public IntegerProperty columnSpanProperty() { return columnSpan; }
    public void setColumnSpan(int index) { columnSpan.set(index); }

    // -- row span
    private final IntegerProperty rowSpan = new SimpleIntegerProperty(1){
        @Override
        protected void invalidated() {
            GridPane.setRowSpan(GridBaseControl.this, get());
        }
    };
    public int getRowSpan() { return rowSpan.get(); }
    public IntegerProperty rowSpanProperty() { return rowSpan; }
    public void setRowSpan(int span) { this.rowSpan.set(span); }
}