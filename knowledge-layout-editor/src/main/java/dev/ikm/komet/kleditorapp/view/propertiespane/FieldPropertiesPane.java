package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kleditorapp.view.control.FieldViewControl;
import dev.ikm.komet.layout.KlRestorable;
import dev.ikm.komet.layout.area.KlAreaForBoolean;
import dev.ikm.komet.layout.area.KlAreaForComponent;
import dev.ikm.komet.layout.area.KlAreaForFloat;
import dev.ikm.komet.layout.area.KlAreaForImage;
import dev.ikm.komet.layout.area.KlAreaForIntIdList;
import dev.ikm.komet.layout.area.KlAreaForIntIdSet;
import dev.ikm.komet.layout.area.KlAreaForInteger;
import dev.ikm.komet.layout.area.KlAreaForString;
import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.util.ServiceLoader;

import static dev.ikm.tinkar.terms.TinkarTerm.BOOLEAN_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_LIST_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_SET_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.CONCEPT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.FLOAT;
import static dev.ikm.tinkar.terms.TinkarTerm.FLOAT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.IMAGE_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.INTEGER_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.STRING;
import static dev.ikm.tinkar.terms.TinkarTerm.STRING_FIELD;

public class FieldPropertiesPane extends GridNodePropertiesPane<FieldViewControl> {
    public static final String DEFAULT_STYLE_CLASS = "field-properties";

    private final VBox fieldMainContainer = new VBox();
    private final ComboBox<String> displayComboBox;

    public FieldPropertiesPane() {
        super(false);

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

    private void populateDisplayComboBox(FieldViewControl control) {
        displayComboBox.getItems().clear();

        int dataTypeNid = control.getDataTypeNid();

        ServiceLoader<? extends KlRestorable.Factory> loader = null;
        if (dataTypeNid == COMPONENT_FIELD.nid()) {
            loader = ServiceLoader.load(KlAreaForComponent.Factory.class);
        } else if (dataTypeNid == CONCEPT_FIELD.nid()) {
            loader = ServiceLoader.load(KlAreaForComponent.Factory.class);
        } else if (dataTypeNid == STRING_FIELD.nid() || dataTypeNid == STRING.nid()) {
            loader = ServiceLoader.load(KlAreaForString.Factory.class);
        } else if (dataTypeNid == COMPONENT_ID_SET_FIELD.nid()) {
            ServiceLoader.load(KlAreaForIntIdSet.Factory.class);
        } else if (dataTypeNid == COMPONENT_ID_LIST_FIELD.nid()) {
            ServiceLoader.load(KlAreaForIntIdList.Factory.class);
        } else if (dataTypeNid == FLOAT_FIELD.nid() || dataTypeNid == FLOAT.nid()) {
            loader = ServiceLoader.load(KlAreaForFloat.Factory.class);
        } else if (dataTypeNid == INTEGER_FIELD.nid()) {
            loader = ServiceLoader.load(KlAreaForInteger.Factory.class);
        } else if (dataTypeNid == BOOLEAN_FIELD.nid()) {
            loader = ServiceLoader.load(KlAreaForBoolean.Factory.class);
        } else if (dataTypeNid == IMAGE_FIELD.nid()) {
            loader = ServiceLoader.load(KlAreaForImage.Factory.class);
//        } else if (dataTypeNid == BYTE_ARRAY_FIELD.nid()) {
//            loader = ServiceLoader.load(KlAreaForB.Factory.class);
        } else {
            loader = ServiceLoader.load(KlAreaForString.Factory.class);
        }

        if (loader != null) {
            for (KlRestorable.Factory factory : loader) {
                displayComboBox.getItems().add(factory.factoryName());
            }
        }
    }

    @Override
    protected void doInit(FieldViewControl control) {
        super.doInit(control);

        populateDisplayComboBox(control);
    }
}
