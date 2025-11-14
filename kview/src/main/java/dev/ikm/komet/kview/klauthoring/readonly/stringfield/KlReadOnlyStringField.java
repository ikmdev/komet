package dev.ikm.komet.kview.klauthoring.readonly.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlStringField;


public class KlReadOnlyStringField extends BaseDefaultKlField<String> implements KlStringField {

    public KlReadOnlyStringField(ObservableField<String> observableStringField, ObservableView observableView, ObservableStamp stamp4field) {
        final KLReadOnlyDataTypeControl node = new KLReadOnlyDataTypeControl<>(String.class);
        super(observableStringField, observableView, stamp4field, node);
        node.valueProperty().bind(observableStringField.editableValueProperty());
        node.setTitle(getTitle());
    }
}