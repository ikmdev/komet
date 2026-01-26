package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.ToggleSwitch;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import javafx.beans.property.ObjectProperty;
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

public class PatternPropertiesPane extends GridNodePropertiesPane<EditorPatternModel> {
    public static final String DEFAULT_STYLE_CLASS = "pattern-properties";

    private final VBox patternMainContainer = new VBox();

    private final TextField titleTextField;
    private final ToggleSwitch titleVisibleTSwitch;

    private final ComboBox<Integer> columnsComboBox = new ComboBox<>();

    private ObjectProperty<Integer> previousControlColumnsObjProperty;

    public PatternPropertiesPane() {
        super(true);

        // Section name container
        VBox titleContainer = new VBox();
        titleContainer.getStyleClass().add("title-container");
        titleContainer.setSpacing(4);

        Label titleLabel = new Label("Pattern Title:");
        titleTextField = new TextField();

        titleContainer.getChildren().addAll(titleLabel, titleTextField);

        // Visible
        titleVisibleTSwitch = new ToggleSwitch();
        titleVisibleTSwitch.setText("Visible");
        titleVisibleTSwitch.setSelected(false);
        titleVisibleTSwitch.getStyleClass().add("title-visible");

        titleTextField.editableProperty().bind(titleVisibleTSwitch.selectedProperty());

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

        // Separator
        Separator separator2 = new Separator();
        separator.setPrefWidth(200);

        patternMainContainer.getChildren().addAll(
                titleContainer,
                titleVisibleTSwitch,
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
    protected void doInit(EditorPatternModel modelObject) {
        super.doInit(modelObject);

        if (previouslyShownModel != null) {
            columnsComboBox.valueProperty().unbindBidirectional(previousControlColumnsObjProperty);
            titleVisibleTSwitch.selectedProperty().unbindBidirectional(previouslyShownModel.titleVisibleProperty());
        }

        titleTextField.setText(modelObject.getTitle());
        titleVisibleTSwitch.selectedProperty().bindBidirectional(modelObject.titleVisibleProperty());

        // Columns ComboBox
        previousControlColumnsObjProperty = modelObject.numberColumnsProperty().asObject();
        columnsComboBox.valueProperty().bindBidirectional(previousControlColumnsObjProperty);
    }
}