package dev.ikm.komet.kview.klauthoring.editable.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentComboBoxControl;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.kview.klfields.ComponentFieldOptions;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.Property;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.Optional;

public class KlEditableComponentField extends BaseDefaultKlField<EntityProxy> {
    /**
     * Constructor for KlEditableComponentField
     * @param observableFieldEditable observable component editable field
     * @param observableView observable view
     * @param stamp4field observable stamp for field
     */
    public KlEditableComponentField(ObservableField.Editable<EntityProxy> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        // A field constrained to a predefined set of concepts (like the classic details windows'
        // drop-downs) is edited with a combo box of those options; any other field is free-form.
        FeatureDefinition fieldDefinition = observableFieldEditable.getObservableFeature().definition(observableView.calculator());
        Optional<List<EntityProxy>> componentOptions = ComponentFieldOptions.componentOptions(observableView.calculator(), fieldDefinition);
        Region node = componentOptions
                .<Region>map(options -> KLComponentControlFactory.createComponentComboBoxControl(observableView.calculator(), options))
                .orElseGet(() -> KLComponentControlFactory.createComponentControl(observableView.calculator()));
        super(observableFieldEditable, observableView, stamp4field, node);

        // title
        String title = observableView.calculator().getDescriptionText(fieldDefinition.meaningNid()).orElse("Blank Title");
        switch (node) {
            case KLComponentComboBoxControl componentComboBoxControl -> componentComboBoxControl.setTitle(title);
            case KLComponentControl componentControl -> componentControl.setTitle(title);
            default -> throw new IllegalStateException("Unexpected component field control: " + node);
        }

        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(entityValueProperty(), observableFieldEditable);
    }

    /**
     * The UI control's component value property, whichever control renders this field.
     */
    private Property<EntityProxy> entityValueProperty() {
        return switch (fxObject()) {
            case KLComponentComboBoxControl componentComboBoxControl -> componentComboBoxControl.valueProperty();
            case KLComponentControl componentControl -> componentControl.entityProperty();
            default -> throw new IllegalStateException("Unexpected component field control: " + fxObject());
        };
    }

    /**
     * Unbinds UI control's and prior ObservableField.Editable. Next, rebinds and updates
     * properties with new JavaFX subscriptions (change listeners).
     * @param newFieldEditable A new field editable to rebind with an editable UI control.
     */
    @Override
    public void rebind(ObservableField.Editable<EntityProxy> newFieldEditable) {
        // Unbind both directions editValueProperty <-> uiControl.entityValueProperty
        // Assign new observable field and unsubscribe all previous subscriptions.
        // Rebind bi-directionally UI Control <-> editableValueProperty.
        // Re-add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty()
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(entityValueProperty(), newFieldEditable);
    }
}
