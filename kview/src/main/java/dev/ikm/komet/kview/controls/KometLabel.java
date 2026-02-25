package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

import java.util.function.Function;

/**
 * A Label that shows an EntityFacade. It shows an Identicon plus a text description that is generated with the help of
 * ViewProperties.
 */
public class KometLabel extends Region {
    private final Label label = new Label();

    private ViewProperties viewProperties;

    public KometLabel(EntityFacade entityFacade, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;

        getChildren().add(label);

        this.entity.set(entityFacade);
        this.entity.subscribe(this::updateEntity);
    }

    private void updateEntity(EntityFacade entity) {
        Image identicon = Identicon.generateIdenticonImage(entity.publicId());
        ImageView imageView = new ImageView(identicon);
        imageView.fitWidthProperty().bind(identiconSize);
        imageView.fitHeightProperty().bind(identiconSize);
        label.setGraphic(imageView);

        Function<Integer, String> fetchDescriptionFunction = ViewCalculatorUtils.getFetchSemanticDescriptionFunction(viewProperties);
        String description = fetchDescriptionFunction.apply(entity.nid());
        label.setText(description);
    }

    // -- identicon size
    IntegerProperty identiconSize = new SimpleIntegerProperty(12);
    public int getIdenticonSize() { return identiconSize.get(); }
    public IntegerProperty identiconSizeProperty() { return identiconSize; }
    public void setIdenticonSize(int identiconSize) { this.identiconSize.set(identiconSize); }

    // -- entity
    private ObjectProperty<EntityFacade> entity = new SimpleObjectProperty<>();
    public EntityFacade getEntity() { return entity.get(); }
    public ObjectProperty<EntityFacade> entityProperty() { return entity; }
    public void setEntity(EntityFacade entity) { this.entity.set(entity); }
}
