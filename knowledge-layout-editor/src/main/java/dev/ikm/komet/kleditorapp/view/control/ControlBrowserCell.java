package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.framework.dnd.KonceptDragSource;
import dev.ikm.komet.kleditorapp.view.ControlBrowserItem;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

/**
 * A cell that renders a placeable supplemental area in the editor's "Controls" palette and acts
 * as the drag source for adding that area to a section. The dragboard carries the area's factory
 * class name under {@link #KL_EDITOR_AREA_FACTORY}.
 */
public class ControlBrowserCell extends ListCell<ControlBrowserItem> {

    /**
     * Dragboard key carrying the supplemental-area factory class name (a {@link String}).
     * The section drop handler reads this to create the corresponding placed area.
     */
    public static final DataFormat KL_EDITOR_AREA_FACTORY =
            new DataFormat("kl-editor/komet-area-factory-classname");

    private final HBox mainContainer;
    private final Label titleLabel;

    private String currentFactoryClassName;

    /**
     * Creates the cell, wiring up its drag source.
     */
    public ControlBrowserCell() {
        mainContainer = new HBox();
        titleLabel = new Label();
        mainContainer.getChildren().setAll(titleLabel);

        setGraphic(mainContainer);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        setUpDragAndDrop();

        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title-label");
    }

    @Override
    protected void updateItem(ControlBrowserItem item, boolean empty) {
        super.updateItem(item, empty);
        if (!isEmpty() && item != null) {
            titleLabel.setText(item.getLabel());
            currentFactoryClassName = item.getFactoryClassName();
        } else {
            titleLabel.setText("");
            currentFactoryClassName = null;
        }
    }

    private void setUpDragAndDrop() {
        setOnDragDetected(mouseEvent -> {
            if (currentFactoryClassName == null) {
                return;
            }
            Dragboard dragboard = startDragAndDrop(TransferMode.COPY);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.put(KL_EDITOR_AREA_FACTORY, currentFactoryClassName);

            // Standard-size drag image with canonical cursor placement (right of the identicon).
            KonceptDragSource.setDragView(dragboard, this);
            dragboard.setContent(clipboardContent);
            mouseEvent.consume();
        });
    }
}
