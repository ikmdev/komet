package dev.ikm.komet.kleditorapp.view.propertiespane;

import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * The two-column grid the properties panes lay their label/control rows out in: labels in column 0,
 * controls in column 1.
 *
 * <p>The label column takes whatever width the control column leaves, so a label is never cut short
 * by a fixed column width. The control column sizes to its widest control and is anchored to the
 * pane's right edge, so controls hold the width they need rather than stretching across the pane
 * (the widths themselves are set in CSS, under {@code .property-grid}). Within a row, label and
 * control are centered on each other vertically, whatever their heights.
 */
public class PropertyGridPane extends GridPane {
    public static final String DEFAULT_STYLE_CLASS = "property-grid";

    public PropertyGridPane() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setHgap(8);
        setVgap(8);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(10);
        labelColumn.setHgrow(Priority.ALWAYS);

        ColumnConstraints controlColumn = new ColumnConstraints();
        controlColumn.setHgrow(Priority.NEVER);
        controlColumn.setHalignment(HPos.RIGHT);

        getColumnConstraints().addAll(labelColumn, controlColumn);

        // Center every child in its row: a label is shorter than the combo box or text field
        // beside it, and a toggle switch shorter than either. Set per child so it holds whatever
        // row constraints a pane adds.
        getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                for (Node added : change.getAddedSubList()) {
                    GridPane.setValignment(added, VPos.CENTER);
                }
            }
        });
    }
}
