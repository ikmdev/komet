package dev.ikm.komet.kview.klfields.componentsetfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentSetControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlComponentSetField;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.image.Image;
import javafx.scene.Parent;

public class DefaultKlComponentSetField extends BaseDefaultKlField<IntIdSet> implements KlComponentSetField {

    public DefaultKlComponentSetField(ObservableField<IntIdSet> observableComponentSetField, ObservableView observableView, boolean isEditable) {
        super(observableComponentSetField, observableView, isEditable);
        Parent node;
        if (isEditable) {
            KLComponentSetControl klComponentSetControl = new KLComponentSetControl();
            klComponentSetControl.setTitle(getTitle());
            klComponentSetControl.valueProperty().bindBidirectional(observableComponentSetField.valueProperty());
            node = klComponentSetControl;
        } else {
            KLReadOnlyComponentSetControl klReadOnlyComponentSetControl = new KLReadOnlyComponentSetControl();

            klReadOnlyComponentSetControl.setTitle(getTitle());

            observableComponentSetField.valueProperty().subscribe(intIdSet -> {
                klReadOnlyComponentSetControl.getItems().clear();
                intIdSet.forEach(nid -> {
                    EntityProxy entityProxy = EntityProxy.make(nid);
                    Image icon = Identicon.generateIdenticonImage(entityProxy.publicId());

                    ComponentItem componentItem = new ComponentItem(entityProxy.description(), icon);
                    klReadOnlyComponentSetControl.getItems().add(componentItem);
                });
            });

            node = klReadOnlyComponentSetControl;
        }

        setKlWidget(node);
    }
}
