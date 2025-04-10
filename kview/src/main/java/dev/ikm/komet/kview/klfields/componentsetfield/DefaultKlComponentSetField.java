package dev.ikm.komet.kview.klfields.componentsetfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentListControl;
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
            KLComponentListControl<IntIdSet> klComponentListControl = new KLComponentListControl<>();
            klComponentListControl.setTitle(getTitle());
            klComponentListControl.valueProperty().bindBidirectional(observableComponentSetField.valueProperty());
            node = klComponentListControl;
        } else {
            KLReadOnlyComponentSetControl klReadOnlyComponentSetControl = new KLReadOnlyComponentSetControl();

            klReadOnlyComponentSetControl.setTitle(getTitle());

            observableComponentSetField.valueProperty().addListener(observable ->
                    updateReadOnlyIntIdSet(klReadOnlyComponentSetControl, observableComponentSetField.valueProperty().get()));
            updateReadOnlyIntIdSet(klReadOnlyComponentSetControl, observableComponentSetField.valueProperty().get());

            node = klReadOnlyComponentSetControl;
        }

        setKlWidget(node);
    }

    private void updateReadOnlyIntIdSet(KLReadOnlyComponentSetControl klReadOnlyComponentSetControl, IntIdSet newIntIdSet) {
        klReadOnlyComponentSetControl.getItems().clear();
        newIntIdSet.forEach(nid -> {
            EntityProxy entityProxy = EntityProxy.make(nid);
            Image icon = Identicon.generateIdenticonImage(entityProxy.publicId());

            ComponentItem componentItem = new ComponentItem(entityProxy.description(), icon, nid);
            klReadOnlyComponentSetControl.getItems().add(componentItem);
        });
    }
}
