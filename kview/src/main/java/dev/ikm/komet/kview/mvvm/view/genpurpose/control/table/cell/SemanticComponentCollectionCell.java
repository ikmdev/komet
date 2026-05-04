package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.kview.controls.skin.ComponentItemNode;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticField;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticRow;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

public class SemanticComponentCollectionCell extends TableCell<SemanticRow, Object> {
    private final ViewCalculator viewCalculator;

    private final VBox componentContainer = new VBox();

    public SemanticComponentCollectionCell(ViewCalculator viewCalculator) {
        this.viewCalculator = viewCalculator;

        componentContainer.getStyleClass().add("component-container");

        setGraphic(componentContainer);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        componentContainer.getChildren().clear();

        IntIdSet intIdSet = (IntIdSet) item;
        intIdSet.forEach(nid -> {
            EntityProxy entityProxy = EntityProxy.make(nid);
            Image icon = Identicon.generateIdenticonImage(entityProxy.publicId());

            String description = viewCalculator.languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());

            ComponentItemNode componentItemNode = new ComponentItemNode(description, icon);
            componentContainer.getChildren().add(componentItemNode);
        });

        setGraphic(componentContainer);
    }
}