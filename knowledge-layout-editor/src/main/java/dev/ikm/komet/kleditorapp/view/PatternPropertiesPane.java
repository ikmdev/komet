package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.PatternViewControl;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class PatternPropertiesPane extends ControlBasePropertiesPane<PatternViewControl> {
    public static final String DEFAULT_STYLE_CLASS = "pattern-properties";

    private final VBox patternMainContainer = new VBox();

    private final TextField titleTextField;

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
        col2.setPrefWidth(100);

        gridPane.getColumnConstraints().addAll(col1, col2);

        // Row constraints (3 rows as in your original FXML)
        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10);
            row.setPrefHeight(30);
            gridPane.getRowConstraints().add(row);
        }

        // "Column(s)" label in grid

        patternMainContainer.getChildren().addAll(
                titleContainer,
                separator,
                gridPane
        );

        mainContainer.setCenter(patternMainContainer);

        // CSS
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        patternMainContainer.getStyleClass().add("pattern-main-container");
    }

    @Override
    protected void doInit(PatternViewControl control) {
        titleTextField.setText(control.getTitle());
    }
}