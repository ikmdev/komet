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
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.image.Image;

public class KlReadOnlyComponentField extends BaseDefaultKlField<EntityProxy> {

    public KlReadOnlyComponentField(ObservableField<EntityProxy> observableComponentField, ObservableView observableView, ObservableStamp stamp4field) {
        KLReadOnlyComponentControl node = new KLReadOnlyComponentControl();
        super(observableComponentField, observableView, stamp4field, node);

        FeatureDefinition fieldDefinition = field().definition(observableView.calculator());
        // title
        String title = observableView.calculator().getDescriptionText(fieldDefinition.meaningNid()).orElse("Blank Title");
        node.setTitle(title);
        // value
        updateControlValue(observableComponentField.editableValueProperty().get(), node);
        // Listen and update when EntityProxy changes
        observableComponentField.editableValueProperty().subscribe(newEntity -> {
            updateControlValue(newEntity, node);
        });
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
