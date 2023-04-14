package dev.ikm.komet.table;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import dev.ikm.komet.framework.graphics.Icon;

public class SemanticTreeTableCell extends TreeTableCell {
    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else if (item instanceof String string) {
            setContentDisplay(ContentDisplay.LEFT);
            setGraphic(Icon.PATTERN.makeIcon());
            setText(string);
        } else {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            GridPane gridPane = new GridPane();
            Label spacer = new Label("   ");
            GridPane.setConstraints(spacer, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
            Node icon = Icon.PAPER_CLIP.makeIcon();
            GridPane.setConstraints(icon, 1, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER);
            gridPane.getChildren().setAll(spacer, icon);
            setGraphic(gridPane);
            setText("    ");
        }
    }
}
