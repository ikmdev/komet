package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kleditorapp.view.ControlBasePropertiesPane;
import dev.ikm.komet.kleditorapp.view.control.GridBaseControl;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.util.Subscription;

import java.util.List;

public class GridNodePropertiesPane<D extends GridBaseControl> extends ControlBasePropertiesPane<D> {

    protected final ComboBox<Integer> columnPositionCB = new ComboBox<>();
    protected final ComboBox<Integer> rowPositionCB = new ComboBox<>();
    protected final ComboBox<Integer> columnSpanCB = new ComboBox<>();
    protected final ComboBox<Integer> rowSpanCB = new ComboBox<>();

    protected ObjectProperty<Integer> previousControlColumnSpanProperty;
    protected ObjectProperty<Integer> previousControlRowSpanProperty;
    protected Subscription columnIndexSubscription;
    protected Subscription rowIndexSubscription;

    protected final Label positioningLabel;
    protected final GridPane positioningGridPane;

    public GridNodePropertiesPane() {
        // "POSITIONING" label
        positioningLabel = new Label("POSITIONING");
        positioningLabel.getStyleClass().add("group-title");

        // GridPane
        positioningGridPane = new GridPane();
        positioningGridPane.setHgap(8);
        positioningGridPane.setVgap(8);

        // Column constraints
        ColumnConstraints positionColumnConstraints1 = new ColumnConstraints();
        positionColumnConstraints1.setMinWidth(10);
        positionColumnConstraints1.setPrefWidth(100);

        ColumnConstraints positionColumnConstraints2 = new ColumnConstraints();
        positionColumnConstraints2.setMinWidth(10);
        positionColumnConstraints2.setHgrow(Priority.ALWAYS);

        positioningGridPane.getColumnConstraints().addAll(positionColumnConstraints1, positionColumnConstraints2);

        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10);
            row.setPrefHeight(30);
            positioningGridPane.getRowConstraints().add(row);
        }

        // Column Position
        Label columnPositionLabel = new Label("Column Position");
        GridPane.setHalignment(columnPositionLabel, HPos.RIGHT);
        positioningGridPane.add(columnPositionLabel, 0, 0);

        // - Column Position ComboBox
        columnPositionCB.setItems(FXCollections.observableArrayList(List.of(1, 2, 3)));
        columnPositionCB.setMaxWidth(Double.MAX_VALUE);
        positioningGridPane.add(columnPositionCB, 1, 0);

        // Row Position
        Label rowLabel = new Label("Row Position");
        GridPane.setHalignment(rowLabel, HPos.RIGHT);
        positioningGridPane.add(rowLabel, 0, 1);

        // - Row Position ComboBox
        rowPositionCB.setItems((FXCollections.observableArrayList(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))));
        rowPositionCB.setMaxWidth(Double.MAX_VALUE);
        positioningGridPane.add(rowPositionCB, 1, 1);

        // Column Span
        Label columnSpanLabel = new Label("Column Span");
        GridPane.setHalignment(columnSpanLabel, HPos.RIGHT);
        positioningGridPane.add(columnSpanLabel, 0, 2);

        // - Column Span ComboBox
        columnSpanCB.setItems((FXCollections.observableArrayList(List.of(1, 2, 3))));
        columnSpanCB.setMaxWidth(Double.MAX_VALUE);
        positioningGridPane.add(columnSpanCB, 1, 2);

        // Row Span
        Label rowSpanLabel = new Label("Row Span");
        GridPane.setHalignment(rowSpanLabel, HPos.RIGHT);
        positioningGridPane.add(rowSpanLabel, 0, 3);

        // - Row Span ComboBox
        rowSpanCB.setItems((FXCollections.observableArrayList(List.of(1, 2, 3))));
        rowSpanCB.setMaxWidth(Double.MAX_VALUE);
        positioningGridPane.add(rowSpanCB, 1, 3);

    }

    @Override
    protected void doInit(D control) {
        if (previouslyShownControl != null) {
            columnIndexSubscription.unsubscribe();
            rowIndexSubscription.unsubscribe();

            columnSpanCB.valueProperty().unbindBidirectional(previousControlColumnSpanProperty);
            rowSpanCB.valueProperty().unbindBidirectional(previousControlRowSpanProperty);
        }

        // Column Index
        columnIndexSubscription = bindBidirectionalWithConverter(
                control.columnIndexProperty(),
                columnPositionCB.valueProperty(),
                val -> val - 1,
                val -> val.intValue() + 1);

        // Row Index
        rowIndexSubscription = bindBidirectionalWithConverter(
                control.rowIndexProperty(),
                rowPositionCB.valueProperty(),
                val -> val - 1,
                val -> val.intValue() + 1);

        // Column Span
        previousControlColumnSpanProperty = control.columnSpanProperty().asObject();
        columnSpanCB.valueProperty().bindBidirectional(previousControlColumnSpanProperty);

        // Row Span
        previousControlRowSpanProperty = control.rowSpanProperty().asObject();
        rowSpanCB.valueProperty().bindBidirectional(previousControlRowSpanProperty);
    }
}