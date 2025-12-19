package dev.ikm.komet.kview.klauthoring.editable.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.terms.EntityFacade;

public class KlEditableComponentField extends BaseDefaultKlField<EntityFacade> {
    /**
     * Constructor for KlEditableComponentField
     * @param observableFieldEditable observable component editable field
     * @param observableView observable view
     * @param stamp4field observable stamp for field
     */
    public KlEditableComponentField(ObservableField.Editable<EntityFacade> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        KLComponentControl node = KLComponentControlFactory.createComponentControl(observableView.calculator());
        super(observableFieldEditable, observableView, stamp4field, node);

        FeatureDefinition fieldDefinition = field().definition(observableView.calculator());
        // title
        String title = observableView.calculator().getDescriptionText(fieldDefinition.meaningNid()).orElse("Blank Title");
        node.setTitle(title);

        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(node.entityProperty(), observableFieldEditable);
    }

    /**
     * Unbinds UI control's and prior ObservableField.Editable. Next, rebinds and updates
     * properties with new JavaFX subscriptions (change listeners).
     * @param newFieldEditable A new field editable to rebind with an editable UI control.
     */
    @Override
    public void rebind(ObservableField.Editable<EntityFacade> newFieldEditable) {
        // Obtain UI control
        KLComponentControl uiControl = (KLComponentControl) fxObject();
        // Unbind both directions editValueProperty <-> uiControl.entityValueProperty
        // Assign new observable field and unsubscribe all previous subscriptions.
        // Rebind bi-directionally UI Control <-> editableValueProperty.
        // Re-add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty()
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(uiControl.entityProperty(), newFieldEditable);
    }
}
