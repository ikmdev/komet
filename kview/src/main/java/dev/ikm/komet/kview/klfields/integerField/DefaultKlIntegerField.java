package dev.ikm.komet.kview.klfields.integerField;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLIntegerControl;
import dev.ikm.komet.kview.controls.KLReadOnlyStringControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlIntegerField;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Node;

public class DefaultKlIntegerField extends BaseDefaultKlField<Integer> implements KlIntegerField {

    public DefaultKlIntegerField(ObservableField<Integer> observableIntegerField, ObservableView observableView, boolean isEditable) {
        super(observableIntegerField, observableView, isEditable);

        Node node;
        if (isEditable) {
            KLIntegerControl integerControl = new KLIntegerControl();
            integerControl.valueProperty().bindBidirectional(observableIntegerField.valueProperty());
            integerControl.setTitle(getTitle());
            node = integerControl;
        } else {
            KLReadOnlyStringControl readOnlyStringControl = new KLReadOnlyStringControl();
            readOnlyStringControl.textProperty().bind(new ObjectBinding<>() {
                {super.bind(observableIntegerField.valueProperty());}
                @Override
                protected String computeValue() {
                    return String.valueOf(observableIntegerField.value());
                }
            });
            readOnlyStringControl.setTitle(getTitle());
            node = readOnlyStringControl;
        }
        setKlWidget(node);
    }
}