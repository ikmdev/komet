package dev.ikm.komet.kview.klauthoring.editable.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLFloatControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlFloatField;

public class KlEditableFloatField extends BaseDefaultKlField<Float> implements KlFloatField {

    public KlEditableFloatField(ObservableField.Editable<Float> observableFloatFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        final KLFloatControl node = new KLFloatControl();
        super(observableFloatFieldEditable, observableView, stamp4field, node);
        node.valueProperty().bindBidirectional(observableFloatFieldEditable.editableValueProperty());
        node.setTitle(getTitle());
        observableFloatFieldEditable
                .editableValueProperty()
                .subscribe(newValue -> {
            if (newValue != null) {
                observableFloatFieldEditable.setValue(newValue);
            }
        });
    }
}
