package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kleditorapp.view.ControlBasePropertiesPane;
import dev.ikm.komet.kleditorapp.view.control.FieldViewControl;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.List;

public class FieldPropertiesPane extends ControlBasePropertiesPane<FieldViewControl> {
    public static final String DEFAULT_STYLE_CLASS = "field-properties";

    private final VBox fieldMainContainer = new VBox();
    private final ComboBox displayComboBox;

    private final ComboBox<Integer> columnPositionCB = new ComboBox<>();
    private final ComboBox<Integer> rowPositionCB = new ComboBox<>();
    private final ComboBox<Integer> columnSpanCB = new ComboBox<>();

    private ObjectProperty<Integer> previousControlColumnSpanProperty;
    private Subscription columnIndexSubscription;
    private Subscription rowIndexSubscription;

    public FieldPropertiesPane() {
        VBox titleContainer = new VBox();
        titleContainer.getStyleClass().add("title-container");
        titleContainer.setSpacing(4);

        // "POSITIONING" label
        Label positioningLabel = new Label("POSITIONING");
        positioningLabel.getStyleClass().add("group-title");

        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(200);

        // GridPane
        GridPane positioningGridPane = new GridPane();
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

        // Column Position ComboBox
        columnPositionCB.setItems(FXCollections.observableArrayList(List.of(1, 2, 3)));
        columnPositionCB.setMaxWidth(Double.MAX_VALUE);
        positioningGridPane.add(columnPositionCB, 1, 0);

        // Row Position
        Label rowLabel = new Label("Row Position");
        GridPane.setHalignment(rowLabel, HPos.RIGHT);
        positioningGridPane.add(rowLabel, 0, 1);

        // Row Position ComboBox
        rowPositionCB.setItems((FXCollections.observableArrayList(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))));
        rowPositionCB.setMaxWidth(Double.MAX_VALUE);
        positioningGridPane.add(rowPositionCB, 1, 1);

        // Column Span
        Label columnSpanLabel = new Label("Column Span");
        GridPane.setHalignment(columnSpanLabel, HPos.RIGHT);
        positioningGridPane.add(columnSpanLabel, 0, 2);

        // Column Span ComboBox
        columnSpanCB.setItems((FXCollections.observableArrayList(List.of(1, 2, 3))));
        columnSpanCB.setMaxWidth(Double.MAX_VALUE);
        positioningGridPane.add(columnSpanCB, 1, 2);

        // Separator
        Separator separator2 = new Separator();
        separator2.setPrefWidth(200);

        // "INTERACTION" label
        Label gridTitleLabel = new Label("INTERACTION");
        gridTitleLabel.getStyleClass().add("group-title");

        // GridPane
        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);

        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(10);
        col1.setPrefWidth(100);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(10);
        col2.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(col1, col2);

        // Row constraints
        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10);
            row.setPrefHeight(30);
            gridPane.getRowConstraints().add(row);
        }

        // "Display" label in grid
        Label displayLabel = new Label("Display");
        GridPane.setHalignment(displayLabel, HPos.RIGHT);
        gridPane.add(displayLabel, 0, 0);

        // "Display" ComboBox in grid
        displayComboBox = new ComboBox<>();
        displayComboBox.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(displayComboBox, 1, 0);

        fieldMainContainer.getChildren().addAll(
                titleContainer,
                separator,
                positioningLabel,
                positioningGridPane,
                separator2,
                gridTitleLabel,
                gridPane
        );

        mainContainer.setCenter(fieldMainContainer);

        // CSS
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        fieldMainContainer.getStyleClass().add("field-main-container");
    }

    @Override
    protected void doInit(FieldViewControl control) {
        if (previouslyShownControl != null) {
            columnIndexSubscription.unsubscribe();
            rowIndexSubscription.unsubscribe();
            columnSpanCB.valueProperty().unbindBidirectional(previousControlColumnSpanProperty);
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
    }
}
