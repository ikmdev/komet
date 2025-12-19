package dev.ikm.komet.kview.klauthoring.readonly.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.image.Image;

public class KlReadOnlyComponentField extends BaseDefaultKlField<EntityFacade> {

    public KlReadOnlyComponentField(ObservableField<EntityFacade> observableComponentField, ObservableView observableView, ObservableStamp stamp4field) {
        KLReadOnlyComponentControl node = new KLReadOnlyComponentControl();
        super(observableComponentField, observableView, stamp4field, node);

        FeatureDefinition fieldDefinition = field().definition(observableView.calculator());
        // title
        String title = observableView.calculator().getDescriptionText(fieldDefinition.meaningNid()).orElse("Blank Title");
        node.setTitle(title);
        // value
        updateControlValue(observableComponentField.editableValueProperty().get(), node);
        // Listen and update when EntityFacade changes
        observableComponentField.editableValueProperty().subscribe(newEntity -> {
            updateControlValue(newEntity, node);
        });
    }

    private void updateControlValue(EntityFacade entityFacade, KLReadOnlyComponentControl klReadOnlyComponentControl) {
        ComponentItem componentItem;
        if (KLComponentControl.isEmpty(entityFacade)) {
            componentItem = null;
        } else {
            String description = observableView.calculator().languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade.nid());
            Image identicon = Identicon.generateIdenticonImage(observableField.valueProperty().get().publicId());
            componentItem = new ComponentItem(description, identicon, entityFacade.nid());
        }

        klReadOnlyComponentControl.setValue(componentItem);
    }
}
