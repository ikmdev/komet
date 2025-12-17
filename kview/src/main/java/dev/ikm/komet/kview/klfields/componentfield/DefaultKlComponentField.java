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
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class DefaultKlComponentField extends BaseDefaultKlField<EntityProxy> {

    public DefaultKlComponentField(ObservableField<EntityProxy> observableComponentField, ObservableView observableView, ObservableStamp stamp4field) {
        Region node = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> KLComponentControlFactory.createComponentControl(
                    observableView.calculator());
            case false -> new KLReadOnlyComponentControl();
        };
/*
TODO: Carl to review behaviour with new editing paradigm.
The issue is that DefaultKlComponentField should always return a read-only control when used in this context. The switch logic at line 22-26 in DefaultKlComponentField.java should be removed or the condition should be changed to always create a read-only control for this use case.
Looking at the code in GenEditingDetailsController.java:509-571, the populateSemanticDetails() method is specifically creating read-only views that become editable via the properties panel, not inline editable controls.
The fix: Change DefaultKlComponentField.java to always create KLReadOnlyComponentControl instead of conditionally creating KLComponentControl:
 */
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
