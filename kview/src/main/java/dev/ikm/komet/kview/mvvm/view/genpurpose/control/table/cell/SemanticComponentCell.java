package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.skin.ComponentItemNode;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticRow;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;

import static dev.ikm.komet.terms.KometTerm.BLANK_CONCEPT;

public class SemanticComponentCell extends TableCell<SemanticRow, Object> {
    private final ViewCalculator viewCalculator;

    private final ComponentItemNode componentItemNode;
    private final ComponentItem componentItem = new ComponentItem();

    public SemanticComponentCell(ViewCalculator viewCalculator) {
        this.viewCalculator = viewCalculator;

        componentItemNode = new ComponentItemNode(componentItem);

        setGraphic(componentItemNode);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        EntityProxy entityProxy = (EntityProxy) item;
        if (entityProxy.nid() == BLANK_CONCEPT.nid()) {
            setGraphic(null);
            return;
        }

        String description = viewCalculator.languageCalculator()
                .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());
        Image identicon = Identicon.generateIdenticonImage(entityProxy.publicId());
        componentItem.setText(description);
        componentItem.setIcon(identicon);

        setGraphic(componentItemNode);
    }
}