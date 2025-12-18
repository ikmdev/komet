package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.PatternViewControl;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.util.List;

public class PatternPropertiesPane extends ControlBasePropertiesPane<PatternViewControl> {
    public static final String DEFAULT_STYLE_CLASS = "pattern-properties";

    private final VBox patternMainContainer = new VBox();

    private final TextField titleTextField;

    private final ComboBox<Integer> columnsComboBox = new ComboBox<>();

    private final ComboBox<Integer> columnPositionCB = new ComboBox<>();
    private final ComboBox<Integer> rowPositionCB = new ComboBox<>();
    private final ComboBox<Integer> columnSpanCB = new ComboBox<>();

    public PatternPropertiesPane() {
        // Section name container
        VBox titleContainer = new VBox();
        titleContainer.getStyleClass().add("title-container");
        titleContainer.setSpacing(4);

        Label nameLabel = new Label("Title:");
        titleTextField = new TextField();

        titleContainer.getChildren().addAll(nameLabel, titleTextField);

        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(200);

        // "GRID LAYOUT" label
        Label gridTitleLabel = new Label("GRID LAYOUT");
        gridTitleLabel.getStyleClass().add("group-title");

        // GridPane
        GridPane gridLayoutGridPane = new GridPane();
        gridLayoutGridPane.setHgap(8);
        gridLayoutGridPane.setVgap(8);

        // Column constraints
        ColumnConstraints columnsComboBoxConstraints1 = new ColumnConstraints();
        columnsComboBoxConstraints1.setMinWidth(10);
        columnsComboBoxConstraints1.setPrefWidth(100);

        ColumnConstraints columnsComboBoxConstraints2 = new ColumnConstraints();
        columnsComboBoxConstraints2.setMinWidth(10);
        columnsComboBoxConstraints2.setHgrow(Priority.ALWAYS);

        gridLayoutGridPane.getColumnConstraints().addAll(columnsComboBoxConstraints1, columnsComboBoxConstraints2);

        // Row constraints
        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10);
            row.setPrefHeight(30);
            gridLayoutGridPane.getRowConstraints().add(row);
        }

        // "Column(s)" label in grid
        Label columnsLabel = new Label("Column(s)");
        GridPane.setHalignment(columnsLabel, HPos.RIGHT);
        gridLayoutGridPane.add(columnsLabel, 0, 0);

        // Columns ComboBox in grid
        columnsComboBox.setItems(FXCollections.observableArrayList(List.of(1, 2, 3)));
        columnsComboBox.getSelectionModel().select((Integer)1);
        columnsComboBox.setMaxWidth(Double.MAX_VALUE);
        gridLayoutGridPane.add(columnsComboBox, 1, 0);

        // "POSITIONING" label
        Label positioningLabel = new Label("POSITIONING");
        positioningLabel.getStyleClass().add("group-title");

        // Separator
        Separator separator2 = new Separator();
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

        patternMainContainer.getChildren().addAll(
                titleContainer,
                separator,
                gridTitleLabel,
                gridLayoutGridPane,
                separator2,
                positioningLabel,
                positioningGridPane
        );

        mainContainer.setCenter(patternMainContainer);

        // CSS
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        patternMainContainer.getStyleClass().add("pattern-main-container");
    }

    @Override
    protected void doInit(PatternViewControl control) {
        if (previouslyShownControl != null) {
            previouslyShownControl.columnIndexProperty().unbind();
            previouslyShownControl.rowIndexProperty().unbind();
            previouslyShownControl.columnSpanProperty().unbind();
        }

        titleTextField.setText(control.getTitle());

        control.numberColumnsProperty().bind(columnsComboBox.valueProperty());

        columnPositionCB.setValue(control.getColumnIndex() + 1);
        control.columnIndexProperty().bind(Bindings.createIntegerBinding(
                () -> columnPositionCB.getValue() - 1,
                columnPositionCB.valueProperty()
        ));

        rowPositionCB.setValue(control.getRowIndex() + 1);
        control.rowIndexProperty().bind(Bindings.createIntegerBinding(
                () -> rowPositionCB.getValue() - 1,
                rowPositionCB.valueProperty()
        ));

        columnSpanCB.setValue(control.getColumnSpan());
        control.columnSpanProperty().bind(columnSpanCB.valueProperty());
    }
}