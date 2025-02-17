package dev.ikm.komet.kview.klfields.componentlistfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlComponentListField;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.image.Image;

public class DefaultKlComponentListField extends BaseDefaultKlField<IntIdList> implements KlComponentListField {

    public DefaultKlComponentListField(ObservableField<IntIdList> observableComponentListField, ObservableView observableView, boolean isEditable) {
        super(observableComponentListField, observableView, isEditable);
        Parent node;
        ObjectProperty<IntIdList> observableProperty = observableComponentListField.valueProperty();
        if (isEditable) {
            KLComponentListControl klComponentListControl = new KLComponentListControl();
            klComponentListControl.setTitle(getTitle());
            klComponentListControl.valueProperty().bindBidirectional(observableProperty);
            node = klComponentListControl;
        } else {
            KLReadOnlyComponentListControl klReadOnlyComponentListControl = new KLReadOnlyComponentListControl();

            klReadOnlyComponentListControl.setTitle(getTitle());

            observableComponentListField.valueProperty().subscribe(intIdSet -> {
                klReadOnlyComponentListControl.getItems().clear();
                intIdSet.forEach(nid -> {
                    if (nid != 0) {
                        EntityProxy entityProxy = EntityProxy.make(nid);
                        Image icon = Identicon.generateIdenticonImage(entityProxy.publicId());

                        ComponentItem componentItem = new ComponentItem(entityProxy.description(), icon);
                        klReadOnlyComponentListControl.getItems().add(componentItem);
                    }
                });
            });

            node = klReadOnlyComponentListControl;
        }
        setKlWidget(node);
    }
}
