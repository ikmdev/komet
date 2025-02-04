package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlComponentListField;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultKlComponentListField extends BaseDefaultKlField<IntIdList> implements KlComponentListField {

    public DefaultKlComponentListField(ObservableField<IntIdList> observableComponentListField, ObservableView observableView, boolean isEditable) {
        super(observableComponentListField, observableView, isEditable);
        Node node;
        ObjectProperty<IntIdList> observablePropertyList = observableComponentListField.valueProperty();
        IntIdList entities = observablePropertyList.get();
        if (isEditable) {
            KLComponentListControl klComponentListControl = new KLComponentListControl();
            klComponentListControl.setTitle(getTitle());
            klComponentListControl.entitiesProperty().bindBidirectional(observablePropertyList);

            node = klComponentListControl;
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
