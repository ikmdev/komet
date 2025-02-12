package dev.ikm.komet.kview.klfields.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLFloatControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlFloatField;
import javafx.scene.Parent;

public class DefaultKlFloatField extends BaseDefaultKlField<Float> implements KlFloatField {

    public DefaultKlFloatField(ObservableField<Float> observableFloatField, ObservableView observableView, boolean isEditable) {
        super(observableFloatField, observableView, isEditable);

        Parent node;
        if (isEditable) {
            KLFloatControl floatControl = new KLFloatControl();

            floatControl.valueProperty().bindBidirectional(observableFloatField.valueProperty());
            floatControl.setTitle(getTitle());

            node = floatControl;
        } else {
            KLReadOnlyDataTypeControl<Float> readOnlyStringControl = new KLReadOnlyDataTypeControl<>();

            readOnlyStringControl.valueProperty().bindBidirectional(observableFloatField.valueProperty());
            readOnlyStringControl.setTitle(getTitle());

            node = readOnlyStringControl;
        }
        setKlWidget(node);
    }
}
