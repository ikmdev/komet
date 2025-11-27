package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.SectionTitledPane;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class SectionTitledPaneSkin extends TitledPaneSkin {
    private EditButton editButton;
    private StackPane titleRegion;

    /**
     * Creates a new TitledPaneSkin instance, installing the necessary child
     * nodes into the Control {@link Control#getChildren() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public SectionTitledPaneSkin(SectionTitledPane control) {
        super(control);

        editButton = new EditButton(control);

        titleRegion = (StackPane) control.lookup(".title");

        getChildren().add(editButton);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);

        final double editButtonHeight = titleRegion.getHeight();
        final double editButtonWidth = editButton.prefWidth(editButtonHeight);

        editButton.resize(editButtonWidth, editButtonHeight);
        editButton.setLayoutX(titleRegion.getLayoutX() + titleRegion.getWidth() - titleRegion.snappedRightInset() - editButtonWidth);
        editButton.setLayoutY(titleRegion.getLayoutY());
    }

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
            titledPane.onEditActionProperty().bind(button.onActionProperty());

            tooltip.setText("Edit Fields");
            button.setTooltip(tooltip);

            mainContainer.getChildren().addAll(separator, button);
            getChildren().add(mainContainer);
        }
    }
}
