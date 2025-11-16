package dev.ikm.komet.kview.klauthoring.editable.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLStringControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlStringField;


public class KlEditableStringField extends BaseDefaultKlField<String> implements KlStringField {

    public KlEditableStringField(ObservableField.Editable<String> observableStringFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        final KLStringControl node = new KLStringControl();
        super(observableStringFieldEditable, observableView, stamp4field, node);


        node.textProperty().bindBidirectional(observableStringFieldEditable.editableValueProperty());
        observableStringFieldEditable
                .editableValueProperty()
                .subscribe(newValue -> {
            if (newValue != null) {
                observableStringFieldEditable.setValue(newValue);
            }
        });
        node.setTitle(getTitle());
    }
}