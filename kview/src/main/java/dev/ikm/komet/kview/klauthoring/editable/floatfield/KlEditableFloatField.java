package dev.ikm.komet.kview.klauthoring.editable.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLFloatControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlFloatField;

public class KlEditableFloatField extends BaseDefaultKlField<Float> implements KlFloatField {

    public KlEditableFloatField(ObservableField.Editable<Float> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        final KLFloatControl node = new KLFloatControl();
        super(observableFieldEditable, observableView, stamp4field, node);

        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(node.valueProperty(), observableFieldEditable);

        // set title
        node.setTitle(getTitle());
    }

    /**
     * Unbinds UI control's and the prior ObservableField.Editable. Next, rebinds and updates
     * properties with new JavaFX subscriptions (change listeners).
     *
     * @param newFieldEditable A new ObservableField.Editable instance.
     */
    @Override
    public void rebind(ObservableField.Editable<Float> newFieldEditable) {
        // Obtain UI control
        KLFloatControl uiControl = (KLFloatControl) fxObject();

        // Unbind both directions editValueProperty <-> uiControl.entityValueProperty
        // Assign new observable field and unsubscribe all previous subscriptions.
        // Rebind bi-directionally UI Control <-> editableValueProperty.
        // Re-add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty()
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(uiControl.valueProperty(), newFieldEditable);
    }
}