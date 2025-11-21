package dev.ikm.komet.kview.klauthoring.editable.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlBooleanField;

public class KlEditableBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {

    public KlEditableBooleanField(ObservableField.Editable<Boolean> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        KLBooleanControl node = new KLBooleanControl();
        super(observableFieldEditable, observableView, stamp4field, node);
        node.setTitle(getTitle());

        // bind bidirectionally the ui control and editableValueProperty.
        rebindValueProperty(node.valueProperty(), observableFieldEditable);
    }

    /**
     * Unbinds UI control's and prior ObservableField.Editable. Next, rebinds and updates
     * properties with new JavaFX subscriptions (change listeners).
     * @param newFieldEditable
     */
    @Override
    public void rebind(ObservableField.Editable<Boolean> newFieldEditable) {
        // Obtain UI control
        KLBooleanControl uiControl = (KLBooleanControl) fxObject();

        // Unbind both directions editValueProperty <-> uiControl.entityValueProperty
        // Assign new observable field and unsubscribe all previous subscriptions.
        // Rebind bi-directionally UI Control <-> editableValueProperty.
        // Re-add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty()
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(uiControl.valueProperty(), newFieldEditable);
    }
}
