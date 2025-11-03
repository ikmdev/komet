package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class DefaultKlComponentField extends BaseDefaultKlField<EntityProxy> {

    public DefaultKlComponentField(ObservableField<EntityProxy> observableComponentField, ObservableView observableView, ObservableStamp stamp4field) {
        Region node = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> KLComponentControlFactory.createTypeAheadComponentControl(
                    observableView.calculator());
            case false -> new KLReadOnlyComponentControl();
        };
        super(observableComponentField, observableView, stamp4field, node);

        FeatureDefinition fieldDefinition = field().definition(observableView.calculator());
        switch (node) {
            case KLComponentControl componentControl -> {
                // title
                componentControl.setTitle(calculatorForContext().getDescriptionTextOrNid(fieldDefinition.meaningNid()));
                // entity
                componentControl.entityProperty().bindBidirectional(observableComponentField.editableValueProperty());
            }
            case KLReadOnlyComponentControl readOnlyComponentControl -> {
                // title
                String title = observableView.calculator().getDescriptionText(fieldDefinition.meaningNid()).orElse("Blank Title");
                readOnlyComponentControl.setTitle(title);
                // value
                updateControlValue(observableComponentField.valueProperty().get(), readOnlyComponentControl);
                // Listen and update when EntityProxy changes
                observableComponentField.valueProperty().subscribe(newEntity -> {
                    updateControlValue(newEntity, readOnlyComponentControl);
                });
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    private void updateControlValue(EntityProxy entityProxy, KLReadOnlyComponentControl klReadOnlyComponentControl) {
        ComponentItem componentItem;
        if (KLComponentControl.isEmpty(entityProxy)) {
            componentItem = null;
        } else {
            String description = observableView.calculator().languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());
            Image identicon = Identicon.generateIdenticonImage(observableField.valueProperty().get().publicId());
            componentItem = new ComponentItem(description, identicon, entityProxy.nid());
        }

        klReadOnlyComponentControl.setValue(componentItem);
    }
}
