package dev.ikm.komet.kview.klauthoring.editable.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlBooleanField;

public class KlEditableBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {

    public KlEditableBooleanField(ObservableField<Boolean> observableBooleanField, ObservableView observableView, ObservableStamp stamp4field) {
        KLBooleanControl node = new KLBooleanControl();
        super(observableBooleanField, observableView, stamp4field, node);
        node.valueProperty().bindBidirectional(observableBooleanField.editableValueProperty());
        node.setTitle(getTitle());
    }
    public KlEditableBooleanField(ObservableField.Editable<Boolean> observableBooleanField, ObservableView observableView, ObservableStamp stamp4field) {
        KLBooleanControl node = new KLBooleanControl();
        super(observableBooleanField, observableView, stamp4field, node);
        node.valueProperty().bindBidirectional(observableBooleanField.editableValueProperty());
        node.setTitle(getTitle());
        observableBooleanField
                .editableValueProperty()
                .addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                observableBooleanField.setValue(newValue);
            }
        });
    }
}
