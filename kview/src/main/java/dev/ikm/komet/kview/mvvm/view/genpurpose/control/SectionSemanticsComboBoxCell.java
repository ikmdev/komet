package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KometLabel;
import dev.ikm.komet.kview.mvvm.view.navigation.PatternSemanticListCell;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Combobox Cell that shows a Semantic. This is used in a combobox in a Section to choose the Reference Component
 * used in that Section.
 */
public class SectionSemanticsComboBoxCell extends ListCell<EntityFacade> {

    private static final Logger LOG = LoggerFactory.getLogger(PatternSemanticListCell.class);

    private EntityHandle currentEntityHandle;
    private final KometLabel label;

    public SectionSemanticsComboBoxCell(ViewProperties viewProperties) {
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        label = new KometLabel(viewProperties);
        label.setShowTooltip(true);

        getStyleClass().add("semantic-list-cell");
    }

    @Override
    protected void updateItem(EntityFacade item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            int nid = item.nid();
            currentEntityHandle = EntityHandle.get(nid);
            currentEntityHandle.ifPresent(entity -> {
                label.setEntity(entity);
                setGraphic(label);
            });
        } else {
            setGraphic(null);
        }
    }
}