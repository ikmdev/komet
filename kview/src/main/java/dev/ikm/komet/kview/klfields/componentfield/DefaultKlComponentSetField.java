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
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
            VBox vBox = new VBox();
            observableComponentSetField.valueProperty().subscribe(intIdSet -> {
                vBox.getChildren().clear();
                vBox.getChildren().add(new Label(getTitle()));
                intIdSet.forEach(nid -> {
                    KLReadOnlyComponentControl readOnlyComponentControl = new KLReadOnlyComponentControl();
                    EntityProxy entityProxy = EntityProxy.make(nid);
                    readOnlyComponentControl.setText(entityProxy.description());
                    readOnlyComponentControl.setIcon(Identicon.generateIdenticonImage(entityProxy.publicId()));
                    vBox.getChildren().add(readOnlyComponentControl);
                });
            });
            node = vBox;

        }
        setKlWidget(node);
    }
}
