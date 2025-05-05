package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.image.Image;

public class DefaultKlComponentField extends BaseDefaultKlField<EntityProxy> {

    public DefaultKlComponentField(ObservableField<EntityProxy> observableComponentField, ObservableView observableView, boolean isEditable) {
        super(observableComponentField, observableView, isEditable);

        Parent node;
        if (isEditable) {
            KLComponentControl componentControl = KLComponentControlFactory.createTypeAheadComponentControl(
                    observableView.calculator().navigationCalculator());

            // title
            componentControl.setTitle(field().meaning().description());

            // entity
            EntityProxy entity = field().value();
            componentControl.setEntity(entity);

            componentControl.entityProperty().subscribe(newEntity -> {
                field().valueProperty().set(newEntity);
                componentControl.setTitle(field().field().meaning().description());
                updateTooltipText();
            });

            node = componentControl;
        } else {
            KLReadOnlyComponentControl readOnlyComponentControl = new KLReadOnlyComponentControl();
            ObjectProperty<EntityProxy> valueProperty = observableComponentField.valueProperty();

            // title
            String title = observableView.calculator().languageCalculator().getDescriptionText(observableComponentField.meaningNid()).orElse("Blank Title");
            readOnlyComponentControl.setTitle(title);

            // value
            updateControlValue(valueProperty.get(), readOnlyComponentControl);

            // Listen and update when EntityProxy changes
            valueProperty.subscribe(newEntity -> {
                updateControlValue(newEntity, readOnlyComponentControl);
            });

            node = readOnlyComponentControl;
        }

        setKlWidget(node);
    }

    private void updateControlValue(EntityProxy entityProxy, KLReadOnlyComponentControl klReadOnlyComponentControl) {
        ComponentItem componentItem;
        if (entityProxy == null) {
            componentItem = null;
        } else {
            String description = entityProxy.description();
            Image identicon = Identicon.generateIdenticonImage(observableField.valueProperty().get().publicId());
            componentItem = new ComponentItem(description, identicon, entityProxy.nid());
        }

        klReadOnlyComponentControl.setValue(componentItem);
    }
}
