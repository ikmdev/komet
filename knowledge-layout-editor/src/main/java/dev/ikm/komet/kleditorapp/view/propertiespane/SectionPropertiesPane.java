package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kleditorapp.view.ControlBasePropertiesPane;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
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

public class SectionPropertiesPane extends ControlBasePropertiesPane<SectionViewControl> {
    public static final String DEFAULT_STYLE_CLASS = "section-properties";

    private final VBox sectionMainContainer = new VBox();

    private final TextField sectionNameTextField;
    private final ComboBox<Integer> columnsComboBox;

    private ObjectProperty<Integer> lastColumnsSectionProperty;

    public SectionPropertiesPane() {
        super(true);

        // Section name container
        VBox sectionNameContainer = new VBox();
        sectionNameContainer.getStyleClass().add("section-name-container");
        sectionNameContainer.setSpacing(4);

        Label nameLabel = new Label("Name of the section:");
        sectionNameTextField = new TextField();

        sectionNameContainer.getChildren().addAll(nameLabel, sectionNameTextField);

        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(200);

        // "GRID LAYOUT" label
        Label gridTitleLabel = new Label("GRID LAYOUT");
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

        // "Column(s)" label in grid
        Label columnsLabel = new Label("Column(s)");
        GridPane.setHalignment(columnsLabel, HPos.RIGHT);
        gridPane.add(columnsLabel, 0, 0);

        // ComboBox in grid
        columnsComboBox = new ComboBox<>();
        columnsComboBox.setItems(FXCollections.observableArrayList(List.of(1, 2, 3)));
        columnsComboBox.getSelectionModel().select((Integer)1);
        columnsComboBox.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(columnsComboBox, 1, 0);

        sectionMainContainer.getChildren().addAll(
            sectionNameContainer,
            separator,
            gridTitleLabel,
            gridPane
        );

        mainContainer.setCenter(sectionMainContainer);

        // CSS
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        sectionMainContainer.getStyleClass().add("section-main-container");
    }

    @Override
    public void doInit(SectionViewControl section) {
        if (previouslyShownControl != null) {
            sectionNameTextField.textProperty().unbindBidirectional(previouslyShownControl.nameProperty());
            columnsComboBox.valueProperty().unbindBidirectional(lastColumnsSectionProperty);
        }
        sectionNameTextField.textProperty().bindBidirectional(section.nameProperty());

        // Bind number of columns property
        lastColumnsSectionProperty = section.numberColumnsProperty().asObject();
        columnsComboBox.valueProperty().bindBidirectional(lastColumnsSectionProperty);
    }
}