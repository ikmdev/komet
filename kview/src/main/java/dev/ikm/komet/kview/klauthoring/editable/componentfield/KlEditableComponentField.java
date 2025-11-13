package dev.ikm.komet.kview.klauthoring.editable.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.terms.EntityProxy;

public class KlEditableComponentField extends BaseDefaultKlField<EntityProxy> {

    /**
     * Constructor for read-only field
     * @param observableComponentField
     * @param observableView
     * @param stamp4field
     * @deprecated use the Editable constructor {@link KlEditableComponentField}.
     */
    public KlEditableComponentField(ObservableField<EntityProxy> observableComponentField, ObservableView observableView, ObservableStamp stamp4field) {
        KLComponentControl node = KLComponentControlFactory.createTypeAheadComponentControl(observableView.calculator());
        super(observableComponentField, observableView, stamp4field, node);

        FeatureDefinition fieldDefinition = field().definition(observableView.calculator());
        // title
        String title = observableView.calculator().getDescriptionText(fieldDefinition.meaningNid()).orElse("Blank Title");
        node.setTitle(title);

        // entity
        node.entityProperty().bindBidirectional(observableComponentField.editableValueProperty());
    }

    public KlEditableComponentField(ObservableField.Editable<EntityProxy> observableComponentFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        KLComponentControl node = KLComponentControlFactory.createTypeAheadComponentControl(observableView.calculator());
        super(observableComponentFieldEditable, observableView, stamp4field, node);

        FeatureDefinition fieldDefinition = field().definition(observableView.calculator());
        // title
        String title = observableView.calculator().getDescriptionText(fieldDefinition.meaningNid()).orElse("Blank Title");
        node.setTitle(title);

        // entity
        node.entityProperty().bindBidirectional(observableComponentFieldEditable.getObservableFeature().editableValueProperty());

        // Listen for changes in the control and update the observable field (is this the right way to do this?)
        node.entityProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                observableComponentFieldEditable.setValue(newValue);
            }
        });

    }
}
