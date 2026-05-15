package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.skin.ComponentItemNode;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticRow;
import dev.ikm.tinkar.common.id.IntIdCollection;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

import java.util.function.Function;

public class SemanticComponentCollectionCell extends TableCell<SemanticRow, IntIdCollection> {
    private final Function<Integer, ComponentItem> nidToComponentItem;
    private final VBox componentContainer = new VBox();

    public SemanticComponentCollectionCell(Function<Integer, ComponentItem> nidToComponentItem) {
        this.nidToComponentItem = nidToComponentItem;

        componentContainer.getStyleClass().add("component-container");

        setGraphic(componentContainer);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(IntIdCollection intIdCollection, boolean empty) {
        super.updateItem(intIdCollection, empty);

        if (empty || intIdCollection == null) {
            setGraphic(null);
            return;
        }

        componentContainer.getChildren().clear();

        intIdCollection.forEach(nid -> {
            ComponentItem componentItem = nidToComponentItem.apply(nid);

            ComponentItemNode componentItemNode = new ComponentItemNode(componentItem);
            componentContainer.getChildren().add(componentItemNode);
        });

        setGraphic(componentContainer);
    }
}