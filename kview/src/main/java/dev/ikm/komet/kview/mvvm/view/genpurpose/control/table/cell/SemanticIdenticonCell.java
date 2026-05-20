package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.skin.ComponentItemNode;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticRow;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;

import java.util.function.Function;

public class SemanticIdenticonCell extends TableCell<SemanticRow, Integer> {
    private final Function<Integer, ComponentItem> nidToComponentItem;
    private final ComponentItemNode componentItemNode;

    private ComponentItem currentComponentItem;

    public SemanticIdenticonCell(Function<Integer, ComponentItem> nidToComponentItem) {
        this.nidToComponentItem = nidToComponentItem;

        componentItemNode = new ComponentItemNode();

        componentItemNode.setDragImageSupplier(() -> {
            if (currentComponentItem == null) {
                return null;
            }

            // Temporarily set the component Item ot its full (including having text) just for
            // the snapshot for the drag image
            componentItemNode.setComponentItem(currentComponentItem);

            Image snapShot = componentItemNode.snapshot(null, null);

            // Restore the component Item to its original (without text)
            componentItemNode.setComponentItem(newComponentItemCopyWithoutText(currentComponentItem));

            return snapShot;
        });

        setGraphic(componentItemNode);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        getStyleClass().add("semantic-identicon-cell");
    }

    @Override
    protected void updateItem(Integer semanticNid, boolean empty) {
        super.updateItem(semanticNid, empty);

        if (empty || semanticNid == null) {
            setGraphic(null);
            return;
        }

        currentComponentItem = nidToComponentItem.apply(semanticNid);

        ComponentItem componentItemDup = newComponentItemCopyWithoutText(currentComponentItem);
        componentItemNode.setComponentItem(componentItemDup);

        setGraphic(componentItemNode);
    }

    private ComponentItem newComponentItemCopyWithoutText(ComponentItem componentItem) {
        ComponentItem componentItemCopy = new ComponentItem(componentItem);
        componentItemCopy.setText("");
        return componentItemCopy;
    }
}