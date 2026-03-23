package dev.ikm.komet.layout.editor.model;

import javafx.beans.property.IntegerProperty;

public interface ParentGridModel {
    // -- number columns
    /**
     * The number of columns that this Parent grid has.
     */
    IntegerProperty numberColumnsProperty();
    default int getNumberColumns() {
        return numberColumnsProperty().get();
    }
    default void setNumberColumns(int number) {
        numberColumnsProperty().set(number);
    }
}