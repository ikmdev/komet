package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell;

import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticRow;
import javafx.scene.control.TableCell;

public class SemanticStandardCell extends TableCell<SemanticRow, Object> {

    public SemanticStandardCell() {
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        setText(item.toString());
        setGraphic(null);
    }
}