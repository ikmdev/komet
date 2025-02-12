package dev.ikm.komet.kview.klfields.componentlistfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlComponentListField;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
            VBox vBox = new VBox();
            observableProperty.subscribe(intIdList -> {
                vBox.getChildren().clear();
                vBox.getChildren().add(new Label(getTitle()));
                intIdList.forEach(nid -> {
                    if (nid != 0) {
                        KLReadOnlyComponentControl readOnlyComponentControl = new KLReadOnlyComponentControl();
                        EntityProxy entityProxy = EntityProxy.make(nid);
                        readOnlyComponentControl.setText(entityProxy.description());
                        readOnlyComponentControl.setIcon(Identicon.generateIdenticonImage(entityProxy.publicId()));
                        vBox.getChildren().add(readOnlyComponentControl);
                    }
                });
            });
            node = vBox;
        }
        setKlWidget(node);
    }
}
