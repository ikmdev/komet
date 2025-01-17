package dev.ikm.komet.kview.klfields.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLFloatControl;
import dev.ikm.komet.kview.controls.KLReadOnlyStringControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlFloatField;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Node;

public class DefaultKlFloatField extends BaseDefaultKlField<Float> implements KlFloatField {

    public DefaultKlFloatField(ObservableField<Float> observableFloatField, ObservableView observableView, boolean isEditable) {
        super(observableFloatField, observableView, isEditable);

        Node node;
        if (isEditable) {
            KLFloatControl floatControl = new KLFloatControl();
            floatControl.valueProperty().bindBidirectional(observableFloatField.valueProperty());
            floatControl.setTitle(getTitle());
            node = floatControl;
        } else {
            KLReadOnlyStringControl readOnlyStringControl = new KLReadOnlyStringControl();

            readOnlyStringControl.textProperty().bind(new ObjectBinding<>() {
                {super.bind(observableFloatField.valueProperty());}
                @Override
                protected String computeValue() {
                    return String.valueOf(observableFloatField.value());
                }
            });
            readOnlyStringControl.setTitle(getTitle());
            node = readOnlyStringControl;
        }
        setKlWidget(node);
    }
}
