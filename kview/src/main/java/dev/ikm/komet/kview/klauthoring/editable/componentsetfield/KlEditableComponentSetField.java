package dev.ikm.komet.kview.klauthoring.editable.componentsetfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlComponentSetField;
import dev.ikm.tinkar.common.id.IntIdSet;

public class KlEditableComponentSetField extends BaseDefaultKlField<IntIdSet> implements KlComponentSetField {

    /**
     * Creates an editable component set field.
     * @param observableFieldEditable the observable editable field
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     */
    public KlEditableComponentSetField(ObservableField.Editable<IntIdSet> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        final KLComponentCollectionControl node = KLComponentControlFactory.createComponentListControl(observableView.calculator());
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
    public void rebind(ObservableField.Editable<IntIdSet> newFieldEditable) {
        // Obtain UI control
        KLComponentCollectionControl uiControl = (KLComponentCollectionControl) fxObject();

        // Unbind both directions editValueProperty <-> uiControl.entityValueProperty
        // Assign new observable field and unsubscribe all previous subscriptions.
        // Rebind bi-directionally UI Control <-> editableValueProperty.
        // Re-add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty()
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(uiControl.valueProperty(), newFieldEditable);
    }
}