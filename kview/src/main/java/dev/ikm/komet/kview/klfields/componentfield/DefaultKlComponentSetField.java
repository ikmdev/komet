package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentSetControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlComponentSetField;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Set;

public class DefaultKlComponentSetField extends BaseDefaultKlField<Set<EntityProxy>> implements KlComponentSetField {

    public DefaultKlComponentSetField(ObservableField<Set<EntityProxy>> observableComponentSetField, ObservableView observableView, boolean isEditable) {
        super(observableComponentSetField, observableView, isEditable);
        Node node;
        ObjectProperty<Set<EntityProxy>> observablePropertySet = observableComponentSetField.valueProperty();
        IntIdSet entities = (IntIdSet) observablePropertySet.get();
        if (isEditable) {
            KLComponentSetControl klComponentSetControl = new KLComponentSetControl();
            klComponentSetControl.setTitle(getTitle());
            entities.forEach(nid -> {
                EntityProxy entityProxy = EntityProxy.make(nid);
                klComponentSetControl.getEntitiesSet().add(entityProxy);
            });

            //TODO The data is bound with IntIdSet will need this to writeToDatabase. Commenting below code for now.

            // klComponentSetControl.entitiesProperty().bindBidirectional(observablePropertySet);

            node = klComponentSetControl;
        } else {
            VBox vBox = new VBox();
            vBox.getChildren().add(new Label(getTitle()));
            entities.forEach(nid -> {
                KLReadOnlyComponentControl readOnlyComponentControl = new KLReadOnlyComponentControl();
                EntityProxy entityProxy = EntityProxy.make(nid);
                readOnlyComponentControl.setText(entityProxy.description());
                readOnlyComponentControl.setIcon(Identicon.generateIdenticonImage(entityProxy.publicId()));
                vBox.getChildren().add(readOnlyComponentControl);
            });
            node = vBox;
        }
        setKlWidget(node);
    }


}
