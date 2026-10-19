package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.ComponentItemNodeFactory;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticRow;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

import java.util.function.Function;

import static dev.ikm.komet.terms.KometConcept.BLANK_CONCEPT;

public class SemanticComponentCell extends TableCell<SemanticRow, EntityFacade> {
    private final Function<EntityFacade, ComponentItem> entityToComponentItem;

    private final ComponentItemNode componentItemNode;
    private final ComponentItem componentItem = new ComponentItem();

    public SemanticComponentCell(Function<EntityFacade, ComponentItem> entityToComponentItem) {
        this.entityToComponentItem = entityToComponentItem;

        componentItemNode = ComponentItemNodeFactory.create(componentItem);

        setGraphic(componentItemNode);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(EntityFacade entity, boolean empty) {
        super.updateItem(entity, empty);

        if (empty || entity == null) {
            setGraphic(null);
            return;
        }

        if (entity.nid() == BLANK_CONCEPT.nid()) {
            setGraphic(null);
            return;
        }

        ComponentItem componentItem = entityToComponentItem.apply(entity);
        componentItemNode.setComponentItem(componentItem);

        setGraphic(componentItemNode);
    }
}