package dev.ikm.komet.kleditorapp.view;

public class GridDropInfo {
    private final int rowIndex;
    private final int columnIndex;

    public GridDropInfo(int rowIndex, int columnIndex) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}