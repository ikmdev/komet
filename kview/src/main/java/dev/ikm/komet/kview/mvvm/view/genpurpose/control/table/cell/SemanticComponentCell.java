package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.skin.ComponentItemNode;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticRow;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

import java.util.function.Function;

import static dev.ikm.komet.terms.KometTerm.BLANK_CONCEPT;

public class SemanticComponentCell extends TableCell<SemanticRow, EntityProxy> {
    private final Function<EntityProxy, ComponentItem> entityProxyToComponentItem;

    private final ComponentItemNode componentItemNode;
    private final ComponentItem componentItem = new ComponentItem();

    public SemanticComponentCell(Function<EntityProxy, ComponentItem> entityProxyToComponentItem) {
        this.entityProxyToComponentItem = entityProxyToComponentItem;

        componentItemNode = new ComponentItemNode(componentItem);

        setGraphic(componentItemNode);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(EntityProxy entityProxy, boolean empty) {
        super.updateItem(entityProxy, empty);

        if (empty || entityProxy == null) {
            setGraphic(null);
            return;
        }

        if (entityProxy.nid() == BLANK_CONCEPT.nid()) {
            setGraphic(null);
            return;
        }

        ComponentItem componentItem = entityProxyToComponentItem.apply(entityProxy);
        componentItemNode.setComponentItem(componentItem);

        setGraphic(componentItemNode);
    }
}