package dev.ikm.komet.kview.klauthoring.editable.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLStringControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlStringField;


public class KlEditableStringField extends BaseDefaultKlField<String> implements KlStringField {

    public KlEditableStringField(ObservableField.Editable<String> observableStringFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        final KLStringControl uiControl = new KLStringControl();
        super(observableStringFieldEditable, observableView, stamp4field, uiControl);

        // Bind bidirectionally the UI control and editableValueProperty
        rebindValueProperty(uiControl.textProperty(), observableStringFieldEditable);

        // set title
        uiControl.setTitle(getTitle());
    }

    /**
     * Unbinds UI control's and prior ObservableField.Editable. Next, rebinds and updates
     * properties with new JavaFX subscriptions (change listeners).
     * @param newFieldEditable
     */
    @Override
    public void rebind(ObservableField.Editable<String> newFieldEditable) {
        // Obtain UI control
        KLStringControl uiControl = (KLStringControl) fxObject();

        // Unbind both directions editValueProperty <-> uiControl.entityValueProperty
        // Assign new observable field and unsubscribe all previous subscriptions.
        // Rebind bi-directionally UI Control <-> editableValueProperty.
        // Re-add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty()
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(uiControl.textProperty(), newFieldEditable);
    }
}