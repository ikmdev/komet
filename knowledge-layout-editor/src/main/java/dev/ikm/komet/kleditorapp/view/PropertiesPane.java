package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowBaseControl;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class PropertiesPane extends VBox {
    private final TextField sectionNameTextField;
    private final ComboBox<Integer> columnsComboBox;

    private SectionViewControl previouslySelectedSection;

    public PropertiesPane() {
        // Root VBox setup
        setPrefWidth(100);
        setPrefHeight(200);
        getStyleClass().add("right-pane");
        setSpacing(8);                 // optional, tweak as needed
        setPadding(new Insets(10));    // optional, tweak as needed

        // Title
        Label titleLabel = new Label();
        titleLabel.getStyleClass().add("title");
        titleLabel.textProperty().bind(title);

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
        Label columnsLabel = new Label("Column(s)");
        GridPane.setHalignment(columnsLabel, HPos.RIGHT);
        gridPane.add(columnsLabel, 0, 0);

        // ComboBox in grid
        columnsComboBox = new ComboBox<>();
        columnsComboBox.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(columnsComboBox, 1, 0);

        // Assemble everything into root VBox
        getChildren().addAll(
                titleLabel,
                sectionNameContainer,
                separator,
                gridTitleLabel,
                gridPane
        );
    }

    public void init(SelectionManager selectionManager) {
        selectionManager.selectedControlProperty().subscribe(() -> {
            EditorWindowBaseControl control = selectionManager.getSelectedControl();
            switch (control) {
                case SectionViewControl section -> {
                    titleProperty().unbind();
                    titleProperty().bind(section.tagTextProperty());

                    if (previouslySelectedSection != null) {
                        sectionNameTextField.textProperty().unbindBidirectional(previouslySelectedSection.nameProperty());
                    }
                    sectionNameTextField.textProperty().bindBidirectional(section.nameProperty());

                    previouslySelectedSection = section;
                }
                default -> System.out.println("TODO...");
            }

        });
    }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }
}