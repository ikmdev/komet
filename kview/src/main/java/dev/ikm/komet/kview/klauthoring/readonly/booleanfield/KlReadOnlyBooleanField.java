package dev.ikm.komet.kview.klauthoring.readonly.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlBooleanField;
import javafx.scene.layout.Region;

public class KlReadOnlyBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {
    public KlReadOnlyBooleanField(ObservableField<Boolean> observableBooleanField, ObservableView observableView, ObservableStamp stamp4field) {
        KLReadOnlyDataTypeControl node = new KLReadOnlyDataTypeControl<>(Boolean.class);
        super(observableBooleanField, observableView, stamp4field, node);
        node.valueProperty().bind(observableBooleanField.valueProperty());
        node.setTitle(getTitle());
    }
}
