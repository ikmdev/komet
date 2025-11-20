package dev.ikm.komet.kview.klauthoring.editable.integerfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLIntegerControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlIntegerField;

public class KlEditableIntegerField extends BaseDefaultKlField<Integer> implements KlIntegerField {

    public KlEditableIntegerField(ObservableField.Editable<Integer> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        final KLIntegerControl node = new KLIntegerControl();
        super(observableFieldEditable, observableView, stamp4field, node);

        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(node.valueProperty(), observableFieldEditable);

        // set title
        node.setTitle(getTitle());
    }

    /**
     * Unbinds UI control's and the prior ObservableField.Editable. Next, rebinds and updates
     * properties with new JavaFX subscriptions (change listeners).
     * @param newFieldEditable A new ObservableField.Editable instance.
     */
    @Override
    public void rebind(ObservableField.Editable<Integer> newFieldEditable) {
        // Obtain UI control
        KLIntegerControl uiControl = (KLIntegerControl) fxObject();

        // Unbind both directions editValueProperty <-> uiControl.entityValueProperty
        // Assign new observable field and unsubscribe all previous subscriptions.
        // Rebind bi-directionally UI Control <-> editableValueProperty.
        // Re-add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty()
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(uiControl.valueProperty(), newFieldEditable);
    }
}
