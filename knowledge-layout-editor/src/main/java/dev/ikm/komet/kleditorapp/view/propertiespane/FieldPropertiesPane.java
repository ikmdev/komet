package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kleditorapp.view.control.FieldViewControl;
import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class FieldPropertiesPane extends GridNodePropertiesPane<FieldViewControl> {
    public static final String DEFAULT_STYLE_CLASS = "field-properties";

    private final VBox fieldMainContainer = new VBox();
    private final ComboBox displayComboBox;

    public FieldPropertiesPane() {
        VBox titleContainer = new VBox();
        titleContainer.getStyleClass().add("title-container");
        titleContainer.setSpacing(4);

        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(200);

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
}
