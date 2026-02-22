package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.SectionTitledPane;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class SectionTitledPaneSkin<T> extends TitledPaneSkin {
    private EditButton editButton;
    private StackPane titleRegion;

    private GridPane contentContainer;

    private ComboBox<T> referenceComponentSemanticsCB;

    /**
     * Creates a new TitledPaneSkin instance, installing the necessary child
     * nodes into the Control {@link Control#getChildren() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public SectionTitledPaneSkin(SectionTitledPane<T> control) {
        super(control);

        contentContainer = new GridPane();

        editButton = new EditButton(control);

        createReferenceComponentCB(control);

        titleRegion = (StackPane) control.lookup(".title");

        getChildren().addAll(
                editButton,
                referenceComponentSemanticsCB
        );
    }

    private void createReferenceComponentCB(SectionTitledPane<T> control) {
        referenceComponentSemanticsCB = new ComboBox<>();

        referenceComponentSemanticsCB.getStyleClass().add("section-combo-box");

        referenceComponentSemanticsCB.setItems(control.getReferenceComponents());

        referenceComponentSemanticsCB.cellFactoryProperty().bind(control.referenceComponentCellFactoryProperty());
        referenceComponentSemanticsCB.buttonCellProperty().bind(control.referenceComponentButtonCellFactoryProperty());
        referenceComponentSemanticsCB.valueProperty().bindBidirectional(control.selectedReferenceComponentProperty());

        ObservableList<T> refs = control.getReferenceComponents();
        referenceComponentSemanticsCB.visibleProperty().bind(Bindings.isNotEmpty(refs));
        referenceComponentSemanticsCB.managedProperty().bind(Bindings.isNotEmpty(refs));
    }

    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);

        final double titleRegionHeight = titleRegion.getHeight();

        // Edit Button
        final double editButtonWidth = editButton.prefWidth(titleRegionHeight);
        editButton.resize(editButtonWidth, titleRegionHeight);
        editButton.setLayoutX(titleRegion.getLayoutX() + titleRegion.getWidth() - titleRegion.snappedRightInset() - editButtonWidth);
        editButton.setLayoutY(titleRegion.getLayoutY());

        // Reference Component Semantics Combobox
        double cbWidth = referenceComponentSemanticsCB.prefWidth(-1);
        double cbHeight = referenceComponentSemanticsCB.prefHeight(cbWidth);
        double cbX = titleRegion.getLayoutX() + titleRegion.getWidth() - titleRegion.snappedRightInset() - editButtonWidth - cbWidth;
        double cbY = titleRegionHeight / 2d - cbHeight / 2d;
        referenceComponentSemanticsCB.resize(cbWidth, cbHeight);
        referenceComponentSemanticsCB.relocate(cbX, cbY);
    }

    /*******************************************************************************
     *                                                                             *
     * Supporting Classes                                                          *
     *                                                                             *
     ******************************************************************************/

    private static class EditButton extends Pane {
        private final HBox mainContainer = new HBox();

        private final Separator separator = new Separator();
        private final Button button = new Button();
        private final Tooltip tooltip = new Tooltip();

        public EditButton(SectionTitledPane titledPane) {
            separator.setPrefHeight(10);
            separator.setOrientation(Orientation.VERTICAL);

            button.getStyleClass().add("add-pencil-button");
            Region graphic = new Region();
            graphic.setPrefHeight(32);
            graphic.setPrefWidth(32);
            graphic.getStyleClass().add("add-pencil");
            button.setGraphic(graphic);
            button.onActionProperty().bind(titledPane.onEditActionProperty());

            tooltip.setText("Edit Fields");
            button.setTooltip(tooltip);

            mainContainer.getChildren().addAll(separator, button);
            getChildren().add(mainContainer);
        }
    }
}
