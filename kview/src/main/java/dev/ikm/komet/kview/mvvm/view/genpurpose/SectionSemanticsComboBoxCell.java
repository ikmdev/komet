package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.view.navigation.PatternSemanticListCell;
import dev.ikm.komet.kview.mvvm.view.navigation.SemanticTooltip;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class SectionSemanticsComboBoxCell extends ListCell<EntityFacade> {

    private static final Logger LOG = LoggerFactory.getLogger(PatternSemanticListCell.class);

    private Function<Integer, String> fetchDescriptionByNid;
    private ViewProperties viewProperties;

    private SemanticTooltip tooltip;

    private EntityHandle currentEntityHandle;
    private String currentSemanticTitle;

    public SectionSemanticsComboBoxCell(Function<Integer, String> fetchDescriptionByNid,
                                   ViewProperties viewProperties) {

        this.fetchDescriptionByNid = fetchDescriptionByNid;
        this.viewProperties = viewProperties;

        tooltip = new SemanticTooltip(viewProperties);
        tooltip.setOnShowing(windowEvent ->
                currentEntityHandle.ifSemantic(currentSemanticEntity ->
                        tooltip.update(currentSemanticEntity, currentSemanticTitle)));


        setTooltip(tooltip);

        getStyleClass().add("semantic-list-cell");
    }

    @Override
    protected void updateItem(EntityFacade item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            int nid = item.nid();
            String entityDescriptionText = fetchDescriptionByNid.apply(nid);
            currentEntityHandle = EntityHandle.get(nid);

            currentSemanticTitle = entityDescriptionText;
            setText(currentSemanticTitle);

            currentEntityHandle.ifPresent(entity -> {
                Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                ImageView imageView = new ImageView(identicon);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                setGraphic(imageView);
            });
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}